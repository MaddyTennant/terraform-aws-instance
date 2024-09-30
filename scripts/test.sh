#!/bin/bash

INSTANCE_ID=$(terraform output -raw instance_id)
INSTANCE_STATE=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --query "Reservations[*].Instances[*].State.Name" --output text)
if [ "$INSTANCE_STATE" != "running" ]; then
  echo "EC2 instance is not running. Current state: $INSTANCE_STATE"
  exit 1
else
  echo "EC2 instance is running."
fi