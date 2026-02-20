# terraform-modules/waf/main.tf

data "aws_region" "current" {}

locals {
  # AWS WAF S3 buckets MUST start with this prefix
  waf_log_bucket_name = lower("aws-waf-logs-${var.project_name}-${var.environment}-${var.web_acl_name}")
}

# -----------------------------------------------------------------------------------------------
# Supporting Resources (Conditional)
# -----------------------------------------------------------------------------------------------
resource "aws_wafv2_ip_set" "retool" {
  count              = var.enable_retool_allowlist ? 1 : 0
  name               = "${var.ip_set_name}-${var.environment}"
  scope              = var.scope
  ip_address_version = "IPV4"
  addresses          = var.retool_ip_addresses
  tags               = var.tags
}

resource "aws_wafv2_regex_pattern_set" "block_method" {
  count = var.enable_method_blocking ? 1 : 0
  name  = "${var.regex_pattern_set_name}-${var.environment}"
  scope = var.scope

  regular_expression {
    regex_string = var.block_method_regex
  }

  tags = var.tags
}

# -----------------------------------------------------------------------------------------------
# WAF Web ACL
# -----------------------------------------------------------------------------------------------
resource "aws_wafv2_web_acl" "this" {
  name  = "${var.web_acl_name}-${var.environment}"
  scope = var.scope

  default_action {
    allow {}
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.web_acl_name}-metric"
    sampled_requests_enabled   = true
  }

  # Priority 0: Allow Retool (Dynamic)
  dynamic "rule" {
    for_each = var.enable_retool_allowlist ? [1] : []
    content {
      name     = "Allow-Retool-access"
      priority = 0
      action { 
        allow {} 
        }
      statement {
        ip_set_reference_statement {
          arn = aws_wafv2_ip_set.retool[0].arn
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AllowRetool"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 1: Geo-Block (Dynamic)
  dynamic "rule" {
    for_each = var.enable_geo_blocking ? [1] : []
    content {
      name     = "Geo-Block"
      priority = 1
      action { 
        block {} 
        }
      statement {
        not_statement {
          statement {
            geo_match_statement {
              country_codes = var.allowed_countries
            }
          }
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "GeoBlock"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 2: Block TRACK Method (Dynamic)
  dynamic "rule" {
    for_each = var.enable_method_blocking ? [1] : []
    content {
      name     = "Block-HTTP-Method"
      priority = 2
      action { 
        block {} 
        }
      statement {
        regex_pattern_set_reference_statement {
          arn = aws_wafv2_regex_pattern_set.block_method[0].arn
          field_to_match { 
            method {} 
            }
          text_transformation {
            priority = 0
            type     = "NONE"
          }
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "BlockHTTPMethod"
        sampled_requests_enabled   = true
      }
    }
  }

  # Managed Rules (Priorities 3-9)
  # Priority 3: Admin Protection
  dynamic "rule" {
    for_each = var.enable_admin_protection ? [1] : []
    content {
      name     = "AWSManagedRulesAdminProtectionRuleSet"
      priority = 3
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesAdminProtectionRuleSet"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AdminProtection"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 4: IP Reputation
  dynamic "rule" {
    for_each = var.enable_ip_reputation ? [1] : []
    content {
      name     = "AWSManagedRulesAmazonIpReputationList"
      priority = 4
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesAmazonIpReputationList"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AmazonIpReputation"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 5: Anonymous IP
  dynamic "rule" {
    for_each = var.enable_anonymous_ip ? [1] : []
    content {
      name     = "AWSManagedRulesAnonymousIpList"
      priority = 5
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesAnonymousIpList"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AnonymousIP"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 6: Known Bad Inputs
  dynamic "rule" {
    for_each = var.enable_known_bad_inputs ? [1] : []
    content {
      name     = "AWSManagedRulesKnownBadInputsRuleSet"
      priority = 6
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesKnownBadInputsRuleSet"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "KnownBadInputs"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 7: Linux Rules
  dynamic "rule" {
    for_each = var.enable_linux_rule_set ? [1] : []
    content {
      name     = "AWSManagedRulesLinuxRuleSet"
      priority = 7
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesLinuxRuleSet"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "LinuxRuleSet"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 8: SQLi Rules
  dynamic "rule" {
    for_each = var.enable_sqli_rule_set ? [1] : []
    content {
      name     = "AWSManagedRulesSQLiRuleSet"
      priority = 8
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesSQLiRuleSet"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "SQLiRuleSet"
        sampled_requests_enabled   = true
      }
    }
  }

  # Priority 9: Common Rule Set
  dynamic "rule" {
    for_each = var.enable_common_rule_set ? [1] : []
    content {
      name     = "AWSManagedRulesCommonRuleSet"
      priority = 9
      override_action { 
        none {} 
        }
      statement {
        managed_rule_group_statement {
          vendor_name = "AWS"
          name        = "AWSManagedRulesCommonRuleSet"
        }
      }
      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "CommonRuleSet"
        sampled_requests_enabled   = true
      }
    }
  }

  tags = var.tags
}

# -----------------------------------------------------------------------------------------------
# Logging Configuration
# -----------------------------------------------------------------------------------------------
resource "aws_s3_bucket" "waf_logs" {
  count  = var.enable_waf_logging ? 1 : 0
  bucket = local.waf_log_bucket_name
  tags   = var.tags
}

resource "aws_s3_bucket_policy" "waf_logs" {
  count  = var.enable_waf_logging ? 1 : 0
  bucket = aws_s3_bucket.waf_logs[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AWSWAFLogsWrite"
        Effect = "Allow"
        Principal = { Service = "delivery.logs.amazonaws.com" }
        Action   = "s3:PutObject"
        Resource = "${aws_s3_bucket.waf_logs[0].arn}/AWSLogs/*"
        Condition = {
          StringEquals = { "s3:x-amz-acl" = "bucket-owner-full-control" }
        }
      },
      {
        Sid    = "AWSWAFLogsAclCheck"
        Effect = "Allow"
        Principal = { Service = "delivery.logs.amazonaws.com" }
        Action   = "s3:GetBucketAcl"
        Resource = aws_s3_bucket.waf_logs[0].arn
      }
    ]
  })
}

resource "aws_wafv2_web_acl_logging_configuration" "this" {
  count        = var.enable_waf_logging ? 1 : 0
  resource_arn = aws_wafv2_web_acl.this.arn
  log_destination_configs = [aws_s3_bucket.waf_logs[0].arn]

  depends_on = [aws_s3_bucket_policy.waf_logs]
}