pipeline {
    agent any
    
    environment {
        AWS_REGION = 'us-west-2'
        AWS_ACCOUNT_ID = credentials('aws-account-id')
        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/tresvita-todo-backend"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        MAVEN_OPTS = '-Xmx1024m'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1'
            }
        }
        
        stage('Build & Test') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${ECR_REPO}:${IMAGE_TAG}")
                }
            }
        }
        
        stage('Push to ECR') {
            steps {
                script {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REPO}
                        
                        docker push ${ECR_REPO}:${IMAGE_TAG}
                        docker tag ${ECR_REPO}:${IMAGE_TAG} ${ECR_REPO}:latest
                        docker push ${ECR_REPO}:latest
                    """
                }
            }
        }
        
        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                sh """
                    aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-dev
                    
                    helm upgrade --install tresvita-todo-backend ../infra-eks-terraform/helm_charts/todo-backend \
                        --namespace backend \
                        --set image.repository=${ECR_REPO} \
                        --set image.tag=${IMAGE_TAG} \
                        --set replicaCount=2 \
                        --wait \
                        --timeout 5m
                    
                    kubectl rollout status deployment/tresvita-todo-backend -n backend --timeout=300s
                """
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
                
                sh """
                    aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-dev
                    
                    helm upgrade --install tresvita-todo-backend ../infra-eks-terraform/helm_charts/todo-backend \
                        --namespace backend \
                        --set image.repository=${ECR_REPO} \
                        --set image.tag=${IMAGE_TAG} \
                        --set replicaCount=3 \
                        --wait \
                        --timeout 10m
                    
                    kubectl rollout status deployment/tresvita-todo-backend -n backend --timeout=600s
                """
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Tresvita Backend Pipeline completed successfully!'
        }
        failure {
            echo '❌ Tresvita Backend Pipeline failed!'
        }
    }
}
