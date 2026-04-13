resource "aws_sns_topic" "alerts" {
  count = var.enable_monitoring_alarms && var.alert_email != "" ? 1 : 0
  name  = "${var.project}-${var.environment}-alerts"
}

resource "aws_sns_topic_subscription" "email" {
  count     = var.enable_monitoring_alarms && var.alert_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.alerts[0].arn
  protocol  = "email"
  endpoint  = var.alert_email
}

resource "aws_cloudwatch_metric_alarm" "ec2_cpu" {
  count               = var.enable_monitoring_alarms ? 1 : 0
  alarm_name          = "${var.project}-${var.environment}-ec2-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors ec2 cpu utilization"

  dimensions = {
    InstanceId = aws_instance.backend.id
  }

  alarm_actions = var.alert_email != "" ? [aws_sns_topic.alerts[0].arn] : []
}

resource "aws_cloudwatch_metric_alarm" "rds_connections" {
  count               = var.enable_monitoring_alarms ? 1 : 0
  alarm_name          = "${var.project}-${var.environment}-rds-connections-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "10"
  alarm_description   = "This metric monitors rds connections"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.db.id
  }

  alarm_actions = var.alert_email != "" ? [aws_sns_topic.alerts[0].arn] : []
}

resource "aws_cloudwatch_metric_alarm" "rds_free_storage" {
  count               = var.enable_monitoring_alarms ? 1 : 0
  alarm_name          = "${var.project}-${var.environment}-rds-free-storage-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  # 1GB in bytes = 1,073,741,824
  threshold           = "1073741824"
  alarm_description   = "This metric monitors rds free storage space (under 1GB)"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.db.id
  }

  alarm_actions = var.alert_email != "" ? [aws_sns_topic.alerts[0].arn] : []
}
