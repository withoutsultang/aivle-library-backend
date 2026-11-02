resource "aws_s3_bucket" "main" {
  for_each      = local.bucket_names
  bucket        = each.value
  force_destroy = false
}

resource "aws_s3_bucket_public_access_block" "main" {
  for_each                = local.bucket_names
  bucket                  = aws_s3_bucket.main[each.key].id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_ownership_controls" "main" {
  for_each = local.bucket_names
  bucket   = aws_s3_bucket.main[each.key].id
  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "main" {
  for_each = local.bucket_names
  bucket   = aws_s3_bucket.main[each.key].id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "main" {
  for_each = local.bucket_names
  bucket   = aws_s3_bucket.main[each.key].id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_policy" "artifacts_tls" {
  bucket = aws_s3_bucket.main["artifacts"].id
  policy = jsonencode({
    Version = "2012-10-17", Statement = [{
      Sid      = "DenyInsecureTransport", Effect = "Deny", Principal = "*", Action = "s3:*",
      Resource = [aws_s3_bucket.main["artifacts"].arn, "${aws_s3_bucket.main["artifacts"].arn}/*"],
      Condition = {
        Bool = {
          "aws:SecureTransport" = "false"
        }
      }
      }
    ]
    }
  )
}

resource "aws_s3_bucket_policy" "covers_tls" {
  bucket = aws_s3_bucket.main["covers"].id
  policy = jsonencode({
    Version = "2012-10-17", Statement = [{
      Sid      = "DenyInsecureTransport", Effect = "Deny", Principal = "*", Action = "s3:*",
      Resource = [aws_s3_bucket.main["covers"].arn, "${aws_s3_bucket.main["covers"].arn}/*"],
      Condition = {
        Bool = {
          "aws:SecureTransport" = "false"
        }
      }
      }
    ]
    }
  )
}

resource "aws_s3_object" "default_cover" {
  bucket       = aws_s3_bucket.main["covers"].id
  key          = "ai-images/default-cover.png"
  source       = "${path.module}/assets/default-cover.png"
  source_hash  = filemd5("${path.module}/assets/default-cover.png")
  content_type = "image/png"
  depends_on   = [aws_s3_bucket_server_side_encryption_configuration.main]
}

resource "aws_dynamodb_table" "views" {
  name         = "${var.name}-views"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "book_id"
  attribute {
    name = "book_id"
    type = "N"
  }
  point_in_time_recovery {
    enabled = true
  }
  server_side_encryption {
    enabled = true
  }
}

resource "aws_db_subnet_group" "main" {
  name       = var.name
  subnet_ids = aws_subnet.db[*].id
}

resource "aws_db_instance" "main" {
  identifier                  = var.name
  engine                      = "mysql"
  instance_class              = var.db_instance_class
  allocated_storage           = 20
  max_allocated_storage       = 50
  storage_type                = "gp3"
  storage_encrypted           = true
  db_name                     = "library"
  username                    = "libraryadmin"
  manage_master_user_password = true
  db_subnet_group_name        = aws_db_subnet_group.main.name
  vpc_security_group_ids      = [aws_security_group.db.id]
  publicly_accessible         = false
  multi_az                    = var.db_multi_az
  backup_retention_period     = 7
  deletion_protection         = true
  skip_final_snapshot         = false
  final_snapshot_identifier   = "${var.name}-final"
  auto_minor_version_upgrade  = true
}

resource "aws_cloudwatch_log_group" "main" {
  for_each          = toset(["application", "bootstrap", "build-backend", "build-frontend"])
  name              = "/${var.name}/${each.value}"
  retention_in_days = var.log_retention_days
}
