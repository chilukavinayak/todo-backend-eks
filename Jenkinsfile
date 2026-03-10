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
                    env.DEPLOY_ENV = env.GIT_BRANCH == 'main' ? 'prod' : 
                                     env.GIT_BRANCH == 'staging' ? 'staging' : 'dev'
                    env.IMAGE_TAG = "${DEPLOY_ENV}-${BUILD_NUMBER}"
                    echo "Branch: ${GIT_BRANCH}, Deploy Env: ${DEPLOY_ENV}, Image Tag: ${IMAGE_TAG}"
                }
            }
        }
        
        stage('Build & Test') {
            steps {
                sh '''
                    ./mvnw clean package -DskipTests
                    ./mvnw test
                '''
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec'
                }
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                script {
                    sh '''
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REPO}
                    '''
                    
                    sh '''
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}/${IMAGE_NAME}:latest
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}/${IMAGE_NAME}:${DEPLOY_ENV}
                    '''
                    
                    sh '''
                        docker push ${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${ECR_REPO}/${IMAGE_NAME}:latest
                        docker push ${ECR_REPO}/${IMAGE_NAME}:${DEPLOY_ENV}
                    '''
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                sh '''
                    which trivy || curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh
                    trivy image --severity HIGH,CRITICAL --exit-code 0 \
                      ${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG} || true
                '''
            }
        }
        
        stage('Deploy to Dev') {
            when { branch 'develop' }
            steps { deployHelm('dev') }
        }
        
        stage('Deploy to Staging') {
            when { branch 'staging' }
            steps { deployHelm('staging') }
        }
        
        stage('Approval for Production') {
            when { branch 'main' }
            steps { input message: 'Deploy to Production?', ok: 'Deploy' }
        }
        
        stage('Deploy to Production') {
            when { branch 'main' }
            steps { deployHelm('prod') }
        }
    }
    
    post {
        success {
            echo "✅ Backend Build ${BUILD_NUMBER} successful!"
        }
        failure {
            echo "❌ Backend Build ${BUILD_NUMBER} failed!"
        }
        always {
            cleanWs()
        }
    }
}

def deployHelm(environment) {
    sh """
        aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-${environment}
        
        helm upgrade --install ${APP_NAME} \
          ../infra-eks-terraform/helm_charts/todo-backend \
          --namespace backend \
          --set image.repository=${ECR_REPO}/${IMAGE_NAME} \
          --set image.tag=${IMAGE_TAG} \
          --set replicaCount=${environment == 'prod' ? 3 : 2} \
          --set ingress.hosts[0].host=api-${environment}.tresvita.local \
          --wait \
          --timeout 5m \
          --atomic
        
        kubectl rollout status deployment/${APP_NAME} -n backend --timeout=300s
    """
}
