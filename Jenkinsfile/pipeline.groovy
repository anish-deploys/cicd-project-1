pipeline {
    agent any

    environment {
        TRIVY_PATH = 'C:\\Users\\anish\\Downloads\\trivy_0.58.2_windows-64bit'
        SONAR_SCANNER_PATH = 'C:\\Users\\anish\\Downloads\\sonar-scanner-cli-6.2.1.4610-windows-x64\\sonar-scanner-6.2.1.4610-windows-x64\\bin'
        DOCKER_CREDENTIALS_ID = 'docker-cred'
        SONARQUBE_CREDENTIALS = 'sonar' 
        GIT_CREDENTIALS_ID = 'git-token'
        DOCKER_IMAGE = 'anish0910/first-cicd:v1'
        CONTAINER_NAME = 'first-cicd-container'
    }

    stages {
        stage('Git Checkout') {
            steps {
                echo "Checking out the code from Git repository..."
                git branch: 'main', credentialsId: "${GIT_CREDENTIALS_ID}", url: "https://github.com/anish-deploys/cicd-project-1.git"
            }
        }

        stage('Trivy File System Scan') {
            steps {
                script {
                    echo "Running Trivy file system scan..."
                    withEnv(["PATH+EXTRA=${TRIVY_PATH}"]) {
                        bat "trivy fs --format table -o trivy-fs-report.html ."
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    echo "Running SonarQube analysis..."
                    withSonarQubeEnv("${SONARQUBE_CREDENTIALS}") { 
                        bat "${SONAR_SCANNER_PATH}\\sonar-scanner.bat -Dsonar.projectKey=first-cicd -Dsonar.projectName=first-cicd -Dsonar.sources=."
                    }
                }
            }
        }

        stage('Build and Tag Docker Image') {
            steps {
                script {
                    echo "Building and tagging Docker image..."
                    withDockerRegistry(credentialsId: "${DOCKER_CREDENTIALS_ID}", toolName: 'docker') {
                        bat "docker build -t ${DOCKER_IMAGE} ."
                    }
                }
            }
        }

        stage('Trivy Image Scan') {
            steps {
                script {
                    echo "Running Trivy image scan..."
                    withEnv(["PATH+EXTRA=${TRIVY_PATH}"]) {
                        bat "trivy image --format json -o trivy-image-report.json ${DOCKER_IMAGE}"
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    echo "Pushing Docker image to Docker Hub..."
                    withDockerRegistry(credentialsId: "${DOCKER_CREDENTIALS_ID}", toolName: 'docker') {
                        bat "docker push ${DOCKER_IMAGE}"
                    }
                }
            }
        }

        stage('Deploy to Container') {
            steps {
                script {
                    echo "Deploying Docker container..."
                    bat """
                        docker stop ${CONTAINER_NAME} || exit 0
                        docker rm ${CONTAINER_NAME} || exit 0
                        docker run -d -p 8082:80 --name ${CONTAINER_NAME} ${DOCKER_IMAGE}
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Archiving artifacts and finalizing pipeline execution..."
            archiveArtifacts artifacts: '**/*.html, **/*.json', allowEmptyArchive: true
        }
        success {
            echo "Pipeline succeeded!"
        }
        failure {
            echo "Pipeline failed. Please check the logs for more details."
        }
    }
}


