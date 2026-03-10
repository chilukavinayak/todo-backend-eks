#!/usr/bin/env groovy

pipeline {
    agent any
    
    environment {
        AWS_REGION = 'us-west-2'
        AWS_ACCOUNT_ID = credentials('aws-account-id')
        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com"
        IMAGE_NAME = 'tresvita-todo-backend'
        APP_NAME = 'tresvita-todo-backend'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    
    triggers {
        githubPush()
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Checkout Infra Repo') {
            steps {
                sh '''
                    rm -rf /tmp/infra-eks-terraform
                    git clone https://github.com/chilukavinayak/infra-eks-terraform.git /tmp/infra-eks-terraform
                    ls -la /tmp/infra-eks-terraform/helm_charts/todo-backend/
                '''
            }
        }
        
        stage('Build and Test') {
            steps {
                sh './mvnw clean package -DskipTests'
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Docker Build and Push') {
            steps {
                sh '''
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                    docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                    docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${ECR_REPO}/${IMAGE_NAME}:${BUILD_NUMBER}
                    docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${ECR_REPO}/${IMAGE_NAME}:latest
                    docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${ECR_REPO}/${IMAGE_NAME}:dev
                    docker push ${ECR_REPO}/${IMAGE_NAME}:${BUILD_NUMBER}
                    docker push ${ECR_REPO}/${IMAGE_NAME}:latest
                    docker push ${ECR_REPO}/${IMAGE_NAME}:dev
                '''
            }
        }
        
        stage('Deploy to Dev') {
            steps {
                deployToDev()
            }
        }
    }
    
    post {
        success {
            echo "SUCCESS: Build ${BUILD_NUMBER} completed!"
        }
        failure {
            echo "FAILED: Build ${BUILD_NUMBER}"
        }
        always {
            cleanWs()
        }
    }
}

def deployToDev() {
    sh """
        aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-dev
        
        echo "========================================"
        echo "DEPLOYING BACKEND TO DEV"
        echo "========================================"
        
        helm upgrade --install ${APP_NAME} /tmp/infra-eks-terraform/helm_charts/todo-backend \
          --namespace backend \
          --values /tmp/infra-eks-terraform/helm_charts/todo-backend/values-dev.yaml \
          --set image.repository=${ECR_REPO}/${IMAGE_NAME} \
          --set image.tag=dev \
          --wait --timeout 5m
        
        echo ""
        echo "Backend Status:"
        kubectl get pods -n backend
        kubectl get svc -n backend
    """
    
    echo ""
    echo "========================================"
    echo "BACKEND DEPLOYED TO DEV"
    echo "========================================"
    echo "API URL: http://tresvita-todo-backend.backend.svc.cluster.local:8080/api"
    echo ""
    echo "Test: kubectl port-forward svc/tresvita-todo-backend 8080:8080 -n backend"
    echo "========================================"
}
