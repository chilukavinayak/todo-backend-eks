pipeline {
    agent any
    
    environment {
        AWS_REGION = 'us-west-2'
        AWS_ACCOUNT_ID = credentials('aws-account-id')
        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/tresvita-todo-backend"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        MAVEN_OPTS = '-Xmx1024m'
    }
    
    triggers {
        // Trigger pipeline on push to GitHub/GitLab webhook
        // Requires webhook to be configured in GitHub/GitLab repository settings
        githubPush()
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
                    # Configure kubectl to connect to EKS cluster
                    aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-dev
                    
                    # Deploy using Helm chart
                    # --install: Install if doesn't exist, upgrade if exists
                    # --namespace: Target namespace for deployment
                    # --values: Use development-specific configuration
                    # --set image.repository: Override ECR repository URL
                    # --set image.tag: Use build number as unique image tag
                    # --wait: Wait for deployment to complete
                    helm upgrade --install tresvita-todo-backend ../infra-eks-terraform/helm_charts/todo-backend \
                        --namespace backend \
                        --values ../infra-eks-terraform/helm_charts/todo-backend/values-dev.yaml \
                        --set image.repository=${ECR_REPO} \
                        --set image.tag=${IMAGE_TAG} \
                        --wait \
                        --timeout 5m
                    
                    # Verify deployment succeeded
                    kubectl rollout status deployment/tresvita-todo-backend -n backend --timeout=300s
                """
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                # Manual approval required before production deployment
                input message: 'Deploy to Production?', ok: 'Deploy'
                
                sh """
                    # Configure kubectl to connect to EKS cluster
                    aws eks update-kubeconfig --region ${AWS_REGION} --name tresvita-todo-app-dev
                    
                    # Deploy using Helm chart with production values
                    # Uses values-prod.yaml for production-specific configuration
                    # Higher replica count and resource limits
                    helm upgrade --install tresvita-todo-backend ../infra-eks-terraform/helm_charts/todo-backend \
                        --namespace backend \
                        --values ../infra-eks-terraform/helm_charts/todo-backend/values-prod.yaml \
                        --set image.repository=${ECR_REPO} \
                        --set image.tag=${IMAGE_TAG} \
                        --wait \
                        --timeout 10m
                    
                    # Verify deployment succeeded
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
