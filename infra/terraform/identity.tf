locals {
  runtime_trust = jsonencode({
    Version = "2012-10-17", Statement = [{
      Effect = "Allow", Principal = {
        Service = "ec2.amazonaws.com"
      },
      Action = "sts:AssumeRole"
      }
    ]
    }
  )
}

resource "aws_iam_role" "runtime" {
  name               = "${var.name}-runtime"
  assume_role_policy = local.runtime_trust
}

resource "aws_iam_instance_profile" "runtime" {
  name = var.name
  role = aws_iam_role.runtime.name
}

resource "aws_iam_role_policy_attachment" "ssm_agent" {
  role       = aws_iam_role.runtime.name
  policy_arn = "${local.prefix}:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "runtime" {
  name = "library-runtime"
  role = aws_iam_role.runtime.id
  policy = jsonencode({
    Version = "2012-10-17", Statement = [
      {
        Effect = "Allow", Action = ["ssm:GetParametersByPath"], Resource = "${local.prefix}:ssm:${var.region}:${local.account}:parameter${local.parameter_path}*"
      },
      {
        Effect = "Allow", Action = ["secretsmanager:GetSecretValue"], Resource = aws_db_instance.main.master_user_secret[0].secret_arn
      },
      {
        Effect = "Allow", Action = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"], Resource = "${aws_s3_bucket.main["covers"].arn}/ai-images/*"
      },
      {
        Effect = "Allow", Action = ["s3:GetObject", "s3:GetObjectVersion"], Resource = "${aws_s3_bucket.main["artifacts"].arn}/*"
      },
      {
        Effect = "Allow", Action = ["s3:ListBucket", "s3:GetBucketLocation"], Resource = aws_s3_bucket.main["artifacts"].arn
      },
      {
        Effect = "Allow", Action = ["dynamodb:UpdateItem", "dynamodb:Scan"], Resource = aws_dynamodb_table.views.arn
      },
      {
        Effect = "Allow", Action = ["logs:CreateLogStream", "logs:PutLogEvents", "logs:DescribeLogStreams"], Resource = [for key in ["application", "bootstrap"] : "${aws_cloudwatch_log_group.main[key].arn}:*"]
      },
      {
        Effect = "Allow", Action = ["cloudwatch:PutMetricData"], Resource = "*", Condition = {
          StringEquals = {
            "cloudwatch:namespace" = "${var.name}/runtime"
          }
        }
      }
    ]
    }
  )
}

resource "aws_iam_role" "deploy" {
  name = "${var.name}-deploy"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{
      Effect = "Allow", Principal = {
        Service = "codedeploy.amazonaws.com"
      },
      Action = "sts:AssumeRole"
      }
    ]
    }
  )
}

resource "aws_iam_role_policy_attachment" "deploy" {
  role       = aws_iam_role.deploy.name
  policy_arn = "${local.prefix}:iam::aws:policy/service-role/AWSCodeDeployRole"
}
