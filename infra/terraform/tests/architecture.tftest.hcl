mock_provider "aws" {
  mock_resource "aws_launch_template" { defaults = { id = "lt-0123456789abcdef0" } }
  mock_resource "aws_sns_topic" { defaults = { arn = "arn:aws:sns:us-west-1:123456789012:mock" } }
  mock_resource "aws_lb" {
    defaults = { arn = "arn:aws:elasticloadbalancing:us-west-1:123456789012:loadbalancer/app/mock/1234567890123456" }
  }
  mock_resource "aws_lb_target_group" {
    defaults = { arn = "arn:aws:elasticloadbalancing:us-west-1:123456789012:targetgroup/mock/1234567890123456" }
  }
  mock_resource "aws_iam_role" { defaults = { arn = "arn:aws:iam::123456789012:role/mock" } }
  mock_resource "aws_cloudfront_function" { defaults = { arn = "arn:aws:cloudfront::123456789012:function/mock" } }
  mock_resource "aws_codebuild_project" { defaults = { arn = "arn:aws:codebuild:us-west-1:123456789012:project/mock" } }
  mock_data "aws_caller_identity" {
    defaults = { account_id = "123456789012", arn = "arn:aws:iam::123456789012:user/mock", user_id = "mock" }
  }
  mock_data "aws_partition" { defaults = { partition = "aws" } }
  mock_data "aws_availability_zones" { defaults = { names = ["us-west-1a", "us-west-1b"] } }
  mock_data "aws_ssm_parameter" { defaults = { value = "ami-0123456789abcdef0" } }
  mock_data "aws_ec2_managed_prefix_list" { defaults = { id = "pl-0123456789abcdef0" } }
  mock_resource "aws_db_instance" {
    defaults = {
      endpoint = "mock-db.example.test:3306"
      master_user_secret = [{
        secret_arn    = "arn:aws:secretsmanager:us-west-1:123456789012:secret:mock-rds-secret"
        kms_key_id    = "arn:aws:kms:us-west-1:123456789012:key/00000000-0000-0000-0000-000000000000"
        secret_status = "active"
      }]
    }
  }
}

run "private_network_and_storage" {
  command = apply
  assert {
    condition     = aws_lb.api.internal && !aws_db_instance.main.publicly_accessible
    error_message = "The ALB and database must remain private."
  }
  assert {
    condition     = length(aws_subnet.app) == 2 && length(aws_subnet.db) == 2 && length(aws_route_table.db.route) == 0
    error_message = "Use two application and isolated database subnets."
  }
  assert {
    condition     = alltrue([for bucket in aws_s3_bucket_public_access_block.main : bucket.block_public_acls && bucket.block_public_policy && bucket.ignore_public_acls && bucket.restrict_public_buckets])
    error_message = "Every bucket must block public access."
  }
  assert {
    condition     = aws_db_instance.main.manage_master_user_password && aws_db_instance.main.storage_encrypted && aws_db_instance.main.deletion_protection
    error_message = "RDS credentials must be service-managed, with encryption and deletion protection."
  }
  assert {
    condition     = aws_dynamodb_table.views.hash_key == "book_id" && one(aws_dynamodb_table.views.attribute).type == "N"
    error_message = "DynamoDB must match the application's numeric book_id key."
  }
  assert {
    condition     = aws_lb_target_group.api.health_check[0].path == "/api/v1/health" && aws_lb_target_group.api.health_check[0].matcher == "200"
    error_message = "ALB readiness must use the application's exact health contract."
  }
  assert {
    condition     = aws_launch_template.app.metadata_options[0].http_tokens == "required"
    error_message = "EC2 must require IMDSv2."
  }
  assert {
    condition     = aws_vpc_security_group_ingress_rule.alb_to_app.from_port == 8080 && aws_vpc_security_group_ingress_rule.app_to_db.from_port == 3306
    error_message = "Application and database ingress ports must match the app."
  }
  assert {
    condition     = !contains(keys(aws_ssm_parameter.runtime), "DB_PASSWORD") && !contains(keys(aws_ssm_parameter.runtime), "JWT_SECRET")
    error_message = "Terraform must not manage application secret values."
  }
  assert {
    condition     = length(aws_codepipeline.delivery) == 0
    error_message = "Live source pipelines must remain disabled until account setup."
  }
}

run "authenticated_api_and_split_cache" {
  command = apply
  assert {
    condition     = aws_cloudfront_distribution.web.default_cache_behavior[0].cache_policy_id == "413f160d-3a1f-4e93-9d70-1d4ad70fbd0a" && aws_cloudfront_distribution.web.ordered_cache_behavior[1].path_pattern == "/assets/*"
    error_message = "HTML must refresh immediately while hashed asset files can be cached."
  }
  assert {
    condition     = aws_cloudfront_distribution.web.ordered_cache_behavior[0].path_pattern == "/api/v1/*" && aws_cloudfront_distribution.web.ordered_cache_behavior[0].cache_policy_id == "413f160d-3a1f-4e93-9d70-1d4ad70fbd0a"
    error_message = "Authenticated API responses must never use the static cache policy."
  }
  assert {
    condition     = aws_cloudfront_distribution.web.ordered_cache_behavior[0].origin_request_policy_id == "b689b0a8-53d0-40ab-baf2-68738e2966ac"
    error_message = "Forward viewer query strings and Authorization to the API origin."
  }
  assert {
    condition     = startswith(aws_ssm_parameter.runtime["NAVER_REDIRECT_URI"].value, "https://") && endswith(aws_ssm_parameter.runtime["NAVER_REDIRECT_URI"].value, "/api/v1/auth/naver/callback")
    error_message = "OAuth callback must use the shared HTTPS CloudFront endpoint."
  }
}

run "two_delivery_pipelines" {
  command = apply
  variables {
    enable_pipelines          = true
    elb_health_checks_enabled = true
    connection_arn            = "arn:aws:codeconnections:us-west-1:123456789012:connection/00000000-0000-0000-0000-000000000000"
  }
  assert {
    condition     = aws_autoscaling_group.app.health_check_type == "ELB" && aws_codedeploy_deployment_group.backend.alarm_configuration[0].enabled
    error_message = "After bootstrap, ALB checks must control instance replacement and deployment alarms."
  }
  assert {
    condition     = length(aws_codepipeline.delivery) == 2 && length(aws_codebuild_project.verify_build) == 4
    error_message = "Both repositories need separate test/build projects."
  }
  assert {
    condition     = alltrue([for pipeline in aws_codepipeline.delivery : [for stage in pipeline.stage : stage.name] == ["Source", "Test", "Build", "Approval", "Deploy"]])
    error_message = "Test, build and manual approval must precede deployment."
  }
  assert {
    condition     = one(aws_codepipeline.delivery["frontend"].stage[4].action).provider == "S3" && one(aws_codepipeline.delivery["backend"].stage[4].action).provider == "CodeDeploy"
    error_message = "Frontend assets and backend JARs require their respective deployment paths."
  }
}

run "reject_invalid_capacity" {
  command = plan
  variables {
    min_instances     = 3
    desired_instances = 2
  }
  expect_failures = [aws_autoscaling_group.app]
}
