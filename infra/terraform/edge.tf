resource "aws_lb" "api" {
  name                       = var.name
  internal                   = true
  load_balancer_type         = "application"
  security_groups            = [aws_security_group.alb.id]
  subnets                    = aws_subnet.app[*].id
  drop_invalid_header_fields = true
  idle_timeout               = 150
}

resource "aws_lb_target_group" "api" {
  name                 = var.name
  port                 = 8080
  protocol             = "HTTP"
  vpc_id               = aws_vpc.main.id
  deregistration_delay = 30
  health_check {
    path                = "/api/v1/health"
    matcher             = "200"
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

resource "aws_lb_listener" "api" {
  load_balancer_arn = aws_lb.api.arn
  port              = 80
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api.arn
  }
}

resource "aws_cloudfront_vpc_origin" "api" {
  vpc_origin_endpoint_config {
    name                   = var.name
    arn                    = aws_lb.api.arn
    http_port              = 80
    https_port             = 443
    origin_protocol_policy = "http-only"
    origin_ssl_protocols {
      items    = ["TLSv1.2"]
      quantity = 1
    }
  }
  depends_on = [aws_lb_listener.api, aws_vpc_security_group_ingress_rule.cloudfront_to_alb]
}

resource "aws_cloudfront_origin_access_control" "frontend" {
  name                              = var.name
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_function" "spa" {
  name    = "${var.name}-spa"
  runtime = "cloudfront-js-2.0"
  publish = true
  code    = file("${path.module}/templates/spa-router.js")
}

resource "aws_cloudfront_distribution" "web" {
  enabled             = true
  default_root_object = "index.html"
  price_class         = "PriceClass_100"
  origin {
    domain_name              = aws_s3_bucket.main["frontend"].bucket_regional_domain_name
    origin_id                = "frontend"
    origin_access_control_id = aws_cloudfront_origin_access_control.frontend.id
  }
  origin {
    domain_name = aws_lb.api.dns_name
    origin_id   = "api"
    vpc_origin_config {
      vpc_origin_id       = aws_cloudfront_vpc_origin.api.id
      origin_read_timeout = 120
    }
  }
  default_cache_behavior {
    target_origin_id           = "frontend"
    viewer_protocol_policy     = "redirect-to-https"
    allowed_methods            = ["GET", "HEAD", "OPTIONS"]
    cached_methods             = ["GET", "HEAD"]
    compress                   = true
    cache_policy_id            = "413f160d-3a1f-4e93-9d70-1d4ad70fbd0a"
    response_headers_policy_id = "67f7725c-6f97-4210-82d7-5512b31e9d03"
    function_association {
      event_type   = "viewer-request"
      function_arn = aws_cloudfront_function.spa.arn
    }
  }
  ordered_cache_behavior {
    path_pattern               = "/api/v1/*"
    target_origin_id           = "api"
    viewer_protocol_policy     = "https-only"
    allowed_methods            = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods             = ["GET", "HEAD"]
    compress                   = true
    cache_policy_id            = "413f160d-3a1f-4e93-9d70-1d4ad70fbd0a"
    origin_request_policy_id   = "b689b0a8-53d0-40ab-baf2-68738e2966ac"
    response_headers_policy_id = "67f7725c-6f97-4210-82d7-5512b31e9d03"
  }
  ordered_cache_behavior {
    path_pattern               = "/assets/*"
    target_origin_id           = "frontend"
    viewer_protocol_policy     = "redirect-to-https"
    allowed_methods            = ["GET", "HEAD", "OPTIONS"]
    cached_methods             = ["GET", "HEAD"]
    compress                   = true
    cache_policy_id            = "658327ea-f89d-4fab-a63d-7e88639e58f6"
    response_headers_policy_id = "67f7725c-6f97-4210-82d7-5512b31e9d03"
  }
  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
  viewer_certificate {
    cloudfront_default_certificate = true
    minimum_protocol_version       = "TLSv1.2_2021"
  }
}

resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.main["frontend"].id
  policy = jsonencode({
    Version = "2012-10-17", Statement = [
      {
        Sid = "CloudFrontOnly", Effect = "Allow", Principal = {
          Service = "cloudfront.amazonaws.com"
        },
        Action = "s3:GetObject", Resource = "${aws_s3_bucket.main["frontend"].arn}/*",
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.web.arn
          }
        }
      },
      {
        Sid      = "DenyInsecureTransport", Effect = "Deny", Principal = "*", Action = "s3:*",
        Resource = [aws_s3_bucket.main["frontend"].arn, "${aws_s3_bucket.main["frontend"].arn}/*"],
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      }
    ]
    }
  )
  depends_on = [aws_s3_bucket_public_access_block.main]
}
