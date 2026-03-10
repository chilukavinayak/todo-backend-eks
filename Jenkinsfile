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
                script {
                    env.GIT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    env.DEPLOY_ENV = env.GIT_BRANCH == 'main' ? 'prod' : env.GIT_BRANCH == 'staging' ? 'staging' : 'dev'
                    env.IMAGE_TAG = "${DEPLOY_ENV}-${BUILD_NUMBER}"
                    echo "Branch: ${GIT_BRANCH}, Deploy Env: ${DEPLOY_ENV}"
                }
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
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}/${IMAGE_NAME}:latest
                    docker push ${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker push ${ECR_REPO}/${IMAGE_NAME}:latest
                '''
            }
        }
        
        stage('Deploy to Dev') {
            when { branch 'develop' }
            steps {
                deployToDev()
            }
        }
        
        stage('Deploy to Staging') {
            when { branch 'staging' }
            steps {
                deployToEKS('staging')
            }
        }
        
        stage('Production Approval') {
            when { branch 'main' }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
            }
        }
        
        stage('Deploy to Production') {
            when { branch 'main' }
            steps {
                deployToEKS('prod')
            }
        }
    }
    
    post {
        success {
            echo "Backend Build ${BUILD_NUMBER} successful!"
        }
        failure {
            echo "Backend Build ${BUILD_NUMBER} failed!"
        }
        always {
            cleanWs()
        }
    }
}

def deployToDev() {
    sh """
        aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-dev
        
        echo "Deploying Backend to DEV environment..."
        helm upgrade --install ${APP_NAME} ../infra-eks-terraform/helm_charts/todo-backend \
          --namespace backend \
          --values ../infra-eks-terraform/helm_charts/todo-backend/values-dev.yaml \
          --set image.repository=${ECR_REPO}/${IMAGE_NAME} \
          --set image.tag=${IMAGE_TAG} \
          --wait --timeout 5m
        
        echo ""
        echo "Backend Deployment Status:"
        kubectl get pods -n backend
        kubectl get svc -n backend
        
        echo ""
        echo "Backend internal URL: http://${APP_NAME}.backend.svc.cluster.local:8080"
        echo "Health check endpoint: http://${APP_NAME}.backend.svc.cluster.local:8080/api/actuator/health"
    """
    
    echo ""
    echo "========================================="
    echo "BACKEND DEPLOYED TO DEV"
    echo "========================================="
    echo "Backend is accessible internally at:"
    echo "http://tresvita-todo-backend.backend.svc.cluster.local:8080/api"
    echo ""
    echo "To test from Jenkins:"
    echo "kubectl port-forward svc/tresvita-todo-backend 8080:8080 -n backend"
    echo "curl http://localhost:8080/api/actuator/health"
    echo "========================================="
}

def deployToEKS(environment) {
    sh """
        aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-${environment}
        helm upgrade --install ${APP_NAME} ../infra-eks-terraform/helm_charts/todo-backend \
          --namespace backend \
          --set image.repository=${ECR_REPO}/${IMAGE_NAME} \
          --set image.tag=${IMAGE_TAG} \
          --set replicaCount=${environment == 'prod' ? 3 : 2} \
          --wait --timeout 5m
    """
}
