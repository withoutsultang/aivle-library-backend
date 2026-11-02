variable "region" {
  type        = string
  default     = "us-west-1"
  description = "AWS region supporting CloudFront VPC origins and EC2/RDS instance types."
}

variable "name" {
  type    = string
  default = "aivle-library-lab"
  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{1,22}[a-z0-9]$", var.name)) && !strcontains(var.name, "--")
    error_message = "Use 3-24 lowercase characters: start with a letter, end with a letter/number, and avoid consecutive hyphens."
  }
}

variable "vpc_cidr" {
  type    = string
  default = "10.42.0.0/16"
}

variable "instance_type" {
  type        = string
  default     = "t3.small"
  description = "x86_64 EC2 instance type matching the Ubuntu amd64 AMI."
}

variable "db_instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_multi_az" {
  type    = bool
  default = false
}

variable "min_instances" {
  type    = number
  default = 2
}

variable "desired_instances" {
  type    = number
  default = 2
}

variable "max_instances" {
  type    = number
  default = 4
}

variable "database_ddl_auto" {
  # Never drop a schema from the example configuration.
  type    = string
  default = "validate"
  validation {
    condition     = contains(["validate", "update"], var.database_ddl_auto)
    error_message = "Use update only for initial empty development schema, then validate."
  }
}

variable "elb_health_checks_enabled" {
  type        = bool
  default     = false
  description = "Enable ALB-based instance replacement and deployment alarms after the first healthy deployment."
}

variable "backend_repository" {
  type    = string
  default = "withoutsultang/aivle-library-backend"
}

variable "frontend_repository" {
  type    = string
  default = "withoutsultang/aivle-library-frontend"
}

variable "source_branch" {
  type    = string
  default = "dev"
}

variable "enable_pipelines" {
  type    = bool
  default = false
}

variable "connection_arn" {
  type        = string
  default     = ""
  description = "An already-authorized AWS CodeConnections GitHub connection ARN."
  validation {
    condition     = var.connection_arn == "" || can(regex("^arn:aws[a-z-]*:(codeconnections|codestar-connections):", var.connection_arn))
    error_message = "Supply an existing CodeConnections or CodeStar Connections ARN."
  }
}

variable "approval_email" {
  type    = string
  default = ""
}

variable "log_retention_days" {
  type    = number
  default = 14
}
