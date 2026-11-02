output "site_url" {
  value = "https://${aws_cloudfront_distribution.web.domain_name}"
}

output "naver_callback_url" {
  value = local.runtime_parameters.NAVER_REDIRECT_URI
}

output "parameter_path" {
  value = local.parameter_path
}

output "region" {
  value = var.region
}

output "frontend_bucket" {
  value = aws_s3_bucket.main["frontend"].id
}

output "cover_bucket" {
  value = aws_s3_bucket.main["covers"].id
}

output "database_secret_arn" {
  value = aws_db_instance.main.master_user_secret[0].secret_arn
}

output "deployment_application" {
  value = aws_codedeploy_app.backend.name
}

output "deployment_group" {
  value = aws_codedeploy_deployment_group.backend.deployment_group_name
}

output "distribution_id" {
  value = aws_cloudfront_distribution.web.id
}
