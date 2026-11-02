locals {
  runtime_parameters = {
    DB_URL               = "jdbc:mysql://${aws_db_instance.main.endpoint}/library?useSSL=true&requireSSL=true"
    DB_USERNAME          = "libraryadmin"
    DB_DDL_AUTO          = var.database_ddl_auto
    AWS_REGION           = var.region
    S3_BUCKET_NAME       = aws_s3_bucket.main["covers"].id
    DYNAMODB_TABLE       = aws_dynamodb_table.views.name
    FRONTEND_BASE_URL    = "https://${aws_cloudfront_distribution.web.domain_name}"
    CORS_ALLOWED_ORIGINS = "https://${aws_cloudfront_distribution.web.domain_name}"
    NAVER_REDIRECT_URI   = "https://${aws_cloudfront_distribution.web.domain_name}/api/v1/auth/naver/callback"
  }
}

resource "aws_ssm_parameter" "runtime" {
  for_each = local.runtime_parameters
  name     = "${local.parameter_path}${each.key}"
  type     = "String"
  value    = each.value
}
