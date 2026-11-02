resource "aws_launch_template" "app" {
  name_prefix            = "${var.name}-"
  image_id               = nonsensitive(data.aws_ssm_parameter.ubuntu_ami.value)
  instance_type          = var.instance_type
  update_default_version = true
  iam_instance_profile {
    name = aws_iam_instance_profile.runtime.name
  }
  vpc_security_group_ids = [aws_security_group.app.id]
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }
  block_device_mappings {
    device_name = "/dev/sda1"
    ebs {
      volume_size           = 16
      volume_type           = "gp3"
      encrypted             = true
      delete_on_termination = true
    }
  }
  user_data = base64encode(templatefile("${path.module}/templates/user-data.sh.tftpl", {
    region              = var.region, parameter_path = local.parameter_path,
    db_secret_arn       = aws_db_instance.main.master_user_secret[0].secret_arn,
    app_log_group       = aws_cloudwatch_log_group.main["application"].name,
    bootstrap_log_group = aws_cloudwatch_log_group.main["bootstrap"].name,
    namespace           = "${var.name}/runtime"
    }
  ))
  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "${var.name}-app", Project = var.name
    }
  }
}

resource "aws_autoscaling_group" "app" {
  name                = var.name
  min_size            = var.min_instances
  desired_capacity    = var.desired_instances
  max_size            = var.max_instances
  vpc_zone_identifier = aws_subnet.app[*].id
  target_group_arns   = [aws_lb_target_group.api.arn]
  # EC2 checks allow the first deployment before an application exists on the AMI.
  # Enable ALB replacement once the initial backend release is healthy.
  health_check_type         = var.elb_health_checks_enabled ? "ELB" : "EC2"
  enabled_metrics           = ["GroupDesiredCapacity", "GroupInServiceInstances"]
  health_check_grace_period = 600
  launch_template {
    id      = aws_launch_template.app.id
    version = "$Latest"
  }
  tag {
    key                 = "Name"
    value               = "${var.name}-app"
    propagate_at_launch = true
  }
  tag {
    key                 = "Project"
    value               = var.name
    propagate_at_launch = true
  }
  lifecycle {
    ignore_changes = [desired_capacity]
    precondition {
      condition     = var.min_instances >= 1 && var.min_instances <= var.desired_instances && var.desired_instances <= var.max_instances
      error_message = "Instance counts must satisfy 1 <= min <= desired <= max."
    }
  }
  depends_on = [aws_route_table_association.app, aws_iam_role_policy.runtime, aws_iam_role_policy_attachment.ssm_agent]
}

resource "aws_autoscaling_policy" "cpu" {
  name                      = "${var.name}-cpu"
  autoscaling_group_name    = aws_autoscaling_group.app.name
  policy_type               = "TargetTrackingScaling"
  estimated_instance_warmup = 600
  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }
    target_value = 60
  }
}

resource "aws_codedeploy_app" "backend" {
  name             = var.name
  compute_platform = "Server"
}

resource "aws_codedeploy_deployment_group" "backend" {
  app_name               = aws_codedeploy_app.backend.name
  deployment_group_name  = "${var.name}-backend"
  service_role_arn       = aws_iam_role.deploy.arn
  autoscaling_groups     = [aws_autoscaling_group.app.name]
  deployment_config_name = "CodeDeployDefault.OneAtATime"
  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
    deployment_type   = "IN_PLACE"
  }
  load_balancer_info {
    target_group_info {
      name = aws_lb_target_group.api.name
    }
  }
  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE", "DEPLOYMENT_STOP_ON_ALARM"]
  }
  alarm_configuration {
    enabled                   = var.elb_health_checks_enabled
    alarms                    = [aws_cloudwatch_metric_alarm.unhealthy.alarm_name]
    ignore_poll_alarm_failure = false
  }
  depends_on = [aws_iam_role_policy_attachment.deploy]
}

resource "aws_sns_topic" "operations" {
  name = "${var.name}-operations"
}

resource "aws_sns_topic_subscription" "email" {
  count     = var.approval_email == "" ? 0 : 1
  topic_arn = aws_sns_topic.operations.arn
  protocol  = "email"
  endpoint  = var.approval_email
}

resource "aws_cloudwatch_metric_alarm" "unhealthy" {
  alarm_name  = "${var.name}-unhealthy-targets"
  namespace   = "AWS/ApplicationELB"
  metric_name = "UnHealthyHostCount"
  dimensions = {
    LoadBalancer = aws_lb.api.arn_suffix, TargetGroup = aws_lb_target_group.api.arn_suffix
  }
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 3
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_sns_topic.operations.arn]
}

resource "aws_cloudwatch_dashboard" "operations" {
  dashboard_name = var.name
  dashboard_body = jsonencode({ widgets = [
    { type = "metric", x = 0, y = 0, width = 12, height = 6, properties = {
      title = "ALB target readiness", region = var.region, period = 60, stat = "Maximum",
      metrics = [
        ["AWS/ApplicationELB", "HealthyHostCount", "LoadBalancer", aws_lb.api.arn_suffix, "TargetGroup", aws_lb_target_group.api.arn_suffix],
        ["AWS/ApplicationELB", "UnHealthyHostCount", "LoadBalancer", aws_lb.api.arn_suffix, "TargetGroup", aws_lb_target_group.api.arn_suffix]
      ]
    } },
    { type = "metric", x = 12, y = 0, width = 12, height = 6, properties = {
      title = "ASG capacity", region = var.region, period = 60, stat = "Average",
      metrics = [
        ["AWS/AutoScaling", "GroupDesiredCapacity", "AutoScalingGroupName", aws_autoscaling_group.app.name],
        ["AWS/AutoScaling", "GroupInServiceInstances", "AutoScalingGroupName", aws_autoscaling_group.app.name]
      ]
    } }
  ] })
}
