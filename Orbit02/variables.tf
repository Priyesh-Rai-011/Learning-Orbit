# terraform-modules/waf/variables.tf

variable "aws_region" {
  type    = string
  default = "us-west-2"
}

variable "environment" {
  type        = string
  description = "dev, qa, or prod"
}

variable "project_name" {
  type    = string
  default = "vbl"
}

variable "web_acl_name" {
  type    = string
  default = "verbal-web-acl"
}

variable "scope" {
  type    = string
  default = "REGIONAL"
  validation {
    condition     = contains(["CLOUDFRONT", "REGIONAL"], var.scope)
    error_message = "Scope must be either CLOUDFRONT or REGIONAL."
  }
}

# --- Custom Rules Configuration ---
variable "enable_retool_allowlist" {
  type    = bool
  default = false
}

variable "retool_ip_addresses" {
  type    = list(string)
  default = ["3.77.79.248/30", "54.169.175.71/32", "44.208.168.68/30", "35.90.103.132/30", "47.128.165.252/32", "54.255.139.141/32"]
}

variable "ip_set_name" {
  type    = string
  default = "retool-ip-set"
}

variable "enable_geo_blocking" {
  type    = bool
  default = false
}

variable "allowed_countries" {
  type    = list(string)
  default = ["IN", "US", "PK"]
}

variable "enable_method_blocking" {
  type    = bool
  default = false
}

variable "block_method_regex" {
  type    = string
  default = "(?i)^TRACK$"
}

variable "regex_pattern_set_name" {
  type    = string
  default = "bad-methods-regex"
}

# --- AWS Managed Rules Toggles ---
variable "enable_admin_protection" {
  type    = bool
  default = false
}

variable "enable_ip_reputation" {
  type    = bool
  default = false
}

variable "enable_anonymous_ip" {
  type    = bool
  default = false
}

variable "enable_known_bad_inputs" {
  type    = bool
  default = false
}

variable "enable_linux_rule_set" {
  type    = bool
  default = false
}

variable "enable_sqli_rule_set" {
  type    = bool
  default = false
}

variable "enable_common_rule_set" {
  type    = bool
  default = false
}

# --- Logging ---
variable "enable_waf_logging" {
  type    = bool
  default = false
}

variable "tags" {
  type = map(string)
  default = {
    ManagedBy = "Terraform"
  }
}