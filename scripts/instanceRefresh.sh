#!/bin/bash

# environment=$1
# terraformWorkspace=$2

# terraform init -backend-config=./environments/"$environment"/backend-config.hcl

# terraform workspace select "$terraformWorkspace"
# asg_name="$(terraform output asg_name)"
# asg_name="${asg_name%\"}"
# asg_name="${asg_name#\"}"
# aws autoscaling start-instance-refresh --auto-scaling-group-name "$asg_name" --strategy Rolling --region ap-southeast-2