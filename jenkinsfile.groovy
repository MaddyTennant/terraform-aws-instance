#!groovy

def call(Map pipelineParams) {
  String environment = pipelineParams['environment'] //dev
  String app = pipelineParams['app'] //dev
  String awsAccount = pipelineParams['awsAccount']
  String testURL = pipelineParams['testURL']
  String terraformWorkspace = pipelineParams['terraformWorkspace'] //dev

  pipeline {
    agent {
      kubernetes {
        yamlMergeStrategy merge()
        yaml '''
          apiVersion: v1
          kind: Pod
          spec:
            containers:
            - name: aws
              image: "registry.nz.thenational.com/cicd/build/aws-build-tools:1.2"
              imagePullPolicy: Always
              command: [ 'sleep' ]
              env:
                - name: HTTPS_PROXY
                  value: 'http://proxy.bnz.co.nz:10568'
                - name: no_proxy
                  value: '.nz.thenational.com'
              args: [ '99d' ]
              resources:
                limits:
                  cpu: 1
                  memory: 1024Mi
              requests:
                cpu: 0.5
                memory: 512Mi
            securityContext:
              runAsUser: 1001
              allowPrivilegeEscalation: false
              capabilities:
                drop: [ 'ALL' ]
        '''
      }
    }
    stages {
      stage("Deploy") {
        steps {
          script {
            container('aws') {
              // withBNZVault(secrets: [[ type: "vaultAWS", accountId: "$awsAccount", role: "$awsAccount" ]]) {
              // managed jenkins sealead secrect or vault
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: "credentials-id-here",
                    accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                    secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                ]]) {
                dir('.') {
                  if ((environment == "prod")) {
                    echo "this is a no prod environment, try again"
                    // sh "terraform init -backend-config=./environments/prod/bitbucket-backend-config.hcl"
                  } else {
                    sh "terraform init -backend-config=./environments/$environment/backend-config.hcl"
                  }
                  sh "terraform workspace select $terraformWorkspace"
                  sh "terraform plan -var-file=./environments/$environment/${app}.tfvars -out=tfplan"
                  input message: 'Please review the output of terraform plan and check the changes proposed. When ready, click Proceed.', ok: 'Proceed'
                  sh "terraform apply tfplan"
                }
              }
            }
          }
        }
      }
      stage("Check EC2 Instance State") {
        steps {
          script {
            container('aws') {
              dir('.') {
                sh '''
                  INSTANCE_ID=$(terraform output -raw instance_id)
                  INSTANCE_STATE=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --query "Reservations[*].Instances[*].State.Name" --output text)
                  if [ "$INSTANCE_STATE" != "running" ]; then
                    echo "EC2 instance is not running. Current state: $INSTANCE_STATE"
                    exit 1
                  else
                    echo "EC2 instance is running."
                  fi
                '''
              }
            }
          }
        }
      }
      stage("Test") {
        steps {
          script {
            container('aws') {
              dir('.') {
                sh("./test.sh")
              }
            }
          }
        }
      }
    }
  }
}