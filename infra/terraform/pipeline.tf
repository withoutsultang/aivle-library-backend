locals {
  enabled_repos = var.enable_pipelines ? local.pipeline_repos : {}
  build_projects = var.enable_pipelines ? {
    for pair in setproduct(["backend", "frontend"], ["test", "build"]) :
    "${pair[0]}-${pair[1]}" => { role = pair[0], phase = pair[1] }
  } : {}
  buildspecs = {
    backend-test = {
      version = "0.2"
      phases = {
        install = { "runtime-versions" = { java = "corretto17" } }
        build   = { commands = ["chmod +x gradlew", "./gradlew test"] }
      }
    }
    backend-build = {
      version = "0.2"
      phases = {
        install = { "runtime-versions" = { java = "corretto17" } }
        build = { commands = [
          "chmod +x gradlew", "./gradlew bootJar -x test",
          "mkdir -p deploy/scripts", "cp build/libs/app.jar appspec.yml deploy/",
          "cp scripts/*.sh deploy/scripts/", "chmod +x deploy/scripts/*.sh"
        ] }
      }
      artifacts = { "base-directory" = "deploy", files = ["**/*"] }
    }
    frontend-test = {
      version = "0.2"
      phases = {
        install   = { "runtime-versions" = { nodejs = "22" } }
        pre_build = { commands = ["npm ci"] }
        build     = { commands = ["npm run lint", "npm test"] }
      }
    }
    frontend-build = {
      version = "0.2"
      phases = {
        install   = { "runtime-versions" = { nodejs = "22" } }
        pre_build = { commands = ["npm ci"] }
        build     = { commands = ["npm run build"] }
      }
      artifacts = { "base-directory" = "dist", files = ["**/*"] }
    }
  }
}

resource "aws_iam_role" "build" {
  for_each = local.enabled_repos
  name     = "${var.name}-build-${each.key}"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "codebuild.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy" "build" {
  for_each = local.enabled_repos
  role     = aws_iam_role.build[each.key].id
  policy = jsonencode({ Version = "2012-10-17", Statement = [
    {
      Effect   = "Allow", Action = ["s3:GetObject", "s3:GetObjectVersion", "s3:PutObject"]
      Resource = "${aws_s3_bucket.main["artifacts"].arn}/*"
    },
    {
      Effect   = "Allow", Action = ["s3:GetBucketLocation", "s3:ListBucket"]
      Resource = aws_s3_bucket.main["artifacts"].arn
    },
    {
      Effect   = "Allow", Action = ["logs:CreateLogStream", "logs:PutLogEvents"]
      Resource = "${aws_cloudwatch_log_group.main["build-${each.key}"].arn}:*"
    }
  ] })
}

resource "aws_codebuild_project" "verify_build" {
  for_each      = local.build_projects
  name          = "${var.name}-${each.key}"
  service_role  = aws_iam_role.build[each.value.role].arn
  build_timeout = 20
  artifacts { type = "CODEPIPELINE" }
  environment {
    compute_type    = "BUILD_GENERAL1_SMALL"
    image           = "aws/codebuild/standard:7.0"
    type            = "LINUX_CONTAINER"
    privileged_mode = false
    environment_variable {
      name  = "VITE_API_BASE_URL"
      value = "/api/v1"
    }
  }
  source {
    type      = "CODEPIPELINE"
    buildspec = jsonencode(local.buildspecs[each.key])
  }
  logs_config {
    cloudwatch_logs {
      group_name  = aws_cloudwatch_log_group.main["build-${each.value.role}"].name
      stream_name = each.value.phase
    }
  }
  depends_on = [aws_iam_role_policy.build]
}

resource "aws_iam_role" "pipeline" {
  for_each = local.enabled_repos
  name     = "${var.name}-pipeline-${each.key}"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "codepipeline.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy" "pipeline" {
  for_each = local.enabled_repos
  role     = aws_iam_role.pipeline[each.key].id
  policy = jsonencode({ Version = "2012-10-17", Statement = concat([
    {
      Effect   = "Allow", Action = ["codeconnections:UseConnection", "codestar-connections:UseConnection"]
      Resource = var.connection_arn
    },
    {
      Effect   = "Allow", Action = ["s3:GetObject", "s3:GetObjectVersion", "s3:PutObject"]
      Resource = "${aws_s3_bucket.main["artifacts"].arn}/*"
    },
    {
      Effect   = "Allow", Action = ["s3:GetBucketLocation", "s3:GetBucketVersioning", "s3:ListBucket"]
      Resource = aws_s3_bucket.main["artifacts"].arn
    },
    {
      Effect   = "Allow", Action = ["codebuild:StartBuild", "codebuild:BatchGetBuilds"]
      Resource = [for phase in ["test", "build"] : aws_codebuild_project.verify_build["${each.key}-${phase}"].arn]
    },
    { Effect = "Allow", Action = ["sns:Publish"], Resource = aws_sns_topic.operations.arn }
    ], jsondecode(each.key == "frontend" ? jsonencode([
      {
        Effect   = "Allow", Action = ["s3:PutObject", "s3:GetObject", "s3:GetObjectVersion"]
        Resource = "${aws_s3_bucket.main["frontend"].arn}/*"
      },
      { Effect = "Allow", Action = ["s3:GetBucketLocation", "s3:GetBucketVersioning", "s3:ListBucket"], Resource = aws_s3_bucket.main["frontend"].arn }
      ]) : jsonencode([
      {
        Effect   = "Allow", Action = ["codedeploy:GetApplication", "codedeploy:GetApplicationRevision", "codedeploy:RegisterApplicationRevision"]
        Resource = "${local.prefix}:codedeploy:${var.region}:${local.account}:application:${aws_codedeploy_app.backend.name}"
      },
      {
        Effect   = "Allow", Action = ["codedeploy:CreateDeployment", "codedeploy:GetDeploymentGroup", "codedeploy:GetDeployment"]
        Resource = "${local.prefix}:codedeploy:${var.region}:${local.account}:deploymentgroup:${aws_codedeploy_app.backend.name}/${aws_codedeploy_deployment_group.backend.deployment_group_name}"
      },
      {
        Effect   = "Allow", Action = ["codedeploy:GetDeploymentConfig"]
        Resource = "${local.prefix}:codedeploy:${var.region}:${local.account}:deploymentconfig:CodeDeployDefault.OneAtATime"
      }
  ]))) })
}

resource "aws_codepipeline" "delivery" {
  for_each = local.enabled_repos
  name     = local.pipeline_names[each.key]
  role_arn = aws_iam_role.pipeline[each.key].arn
  artifact_store {
    location = aws_s3_bucket.main["artifacts"].id
    type     = "S3"
  }
  stage {
    name = "Source"
    action {
      name             = "GitHub"
      category         = "Source"
      owner            = "AWS"
      provider         = "CodeStarSourceConnection"
      version          = "1"
      output_artifacts = ["Source"]
      configuration    = { ConnectionArn = var.connection_arn, FullRepositoryId = each.value, BranchName = var.source_branch, DetectChanges = "true" }
    }
  }
  stage {
    name = "Test"
    action {
      name            = "Verify"
      category        = "Test"
      owner           = "AWS"
      provider        = "CodeBuild"
      version         = "1"
      input_artifacts = ["Source"]
      configuration   = { ProjectName = aws_codebuild_project.verify_build["${each.key}-test"].name }
    }
  }
  stage {
    name = "Build"
    action {
      name             = "Package"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      version          = "1"
      input_artifacts  = ["Source"]
      output_artifacts = ["Build"]
      configuration    = { ProjectName = aws_codebuild_project.verify_build["${each.key}-build"].name }
    }
  }
  stage {
    name = "Approval"
    action {
      name          = "Review"
      category      = "Approval"
      owner         = "AWS"
      provider      = "Manual"
      version       = "1"
      configuration = { NotificationArn = aws_sns_topic.operations.arn, CustomData = "Review test and build results before deployment." }
    }
  }
  stage {
    name = "Deploy"
    action {
      name            = "Deploy"
      category        = "Deploy"
      owner           = "AWS"
      provider        = each.key == "frontend" ? "S3" : "CodeDeploy"
      version         = "1"
      input_artifacts = ["Build"]
      configuration = each.key == "frontend" ? {
        BucketName = aws_s3_bucket.main["frontend"].id, Extract = "true"
        } : {
        ApplicationName     = aws_codedeploy_app.backend.name
        DeploymentGroupName = aws_codedeploy_deployment_group.backend.deployment_group_name
      }
    }
  }
  lifecycle {
    precondition {
      condition     = !var.enable_pipelines || var.connection_arn != ""
      error_message = "An already-authorized GitHub connection ARN is required to enable pipelines."
    }
  }
  depends_on = [aws_iam_role_policy.pipeline, aws_ssm_parameter.runtime]
}
