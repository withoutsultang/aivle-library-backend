terraform {
  required_version = ">= 1.13.5, < 2.0.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.9.0"
    }
  }
}

provider "aws" {
  region = var.region
  default_tags {
    tags = {
      Project = var.name, ManagedBy = "Terraform", Purpose = "Portfolio"
    }
  }
}

data "aws_caller_identity" "current" {
}

data "aws_partition" "current" {
}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ssm_parameter" "ubuntu_ami" {
  name = "/aws/service/canonical/ubuntu/server/22.04/stable/current/amd64/hvm/ebs-gp2/ami-id"
}

locals {
  account        = data.aws_caller_identity.current.account_id
  partition      = data.aws_partition.current.partition
  prefix         = "arn:${local.partition}"
  parameter_path = "/${var.name}/runtime/"
  bucket_names = {
    for key in ["frontend", "covers", "artifacts"] :
    key => "${var.name}-${key}-${local.account}-${var.region}"
  }
  pipeline_repos = {
    backend = var.backend_repository, frontend = var.frontend_repository
  }
  pipeline_names = {
    for key in keys(local.pipeline_repos) :
    key => "${var.name}-${key}"
  }
  azs = slice(data.aws_availability_zones.available.names, 0, 2)
}
