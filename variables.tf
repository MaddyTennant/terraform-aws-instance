variable "instance_name" {
  description = "Value of the Name tag for the EC2 instance"
  type        = string
  default     = "ExampleInstance"
}

variable "profile" {
  description = "AWS profile"
  type        = string
  default     = "terraform-eks"
}

variable "vpc_id" {
  description = "VPC id of the EC2 instance"
  default     = "vpc-01e80a0fd7b864816"
}

variable "environment" {
  description = "The environment for the resources (e.g., dev, staging, prod)"
  type        = string
}
