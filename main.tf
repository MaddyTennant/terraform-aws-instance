terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region  = "ap-southeast-2"
  profile = var.profile
}

resource "random_string" "suffix" {
  length  = 8
  special = false
}

locals {
  security_group_name = "allow_ssh_${var.environment}_${random_string.suffix.result}"
}

resource "aws_security_group" "allow_ssh" {
  name        = local.security_group_name
  description = "Allow SSH inbound traffic"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["147.161.216.202/32"]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["13.239.158.0/29"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# locals {
#   security_group = {
#     dev  = aws_security_group.allow_ssh_dev.name
#     test = aws_security_group.allow_ssh_test.name
#   }
# }

resource "aws_instance" "app_server" {
  ami             = "ami-0e8fd5cc56e4d158c"
  instance_type   = "t2.micro"
  security_groups = [aws_security_group.allow_ssh.name]
  tags = {
    Name = var.instance_name
  }
}
