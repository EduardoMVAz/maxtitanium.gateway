pipeline {
    agent any
    environment {
        K8S_PORT = 32769
        TARGET = 'aws'
    }
    stages {
        stage('Build Interface') {
            steps {
                build job: 'Auth', wait: true
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Post Security Scan') {
            steps {
                script {
                    // Retrieve the Git URL
                    def gitUrl = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()

                    // Hardcoded id_service
                    def idService = '87d7fa28-549a-4b45-8960-990855580827'

                    // Create the JSON payload using string manipulation
                    def newPayload = """
                    {
                        "repo_url": "${gitUrl}",
                        "id_service": "${idService}"
                    }
                    """

                    // echoes the payload to the console
                    echo newPayload

                    // Post the new JSON payload to the API
                    sh "curl -X POST -H 'Content-Type: application/json' -d '${newPayload}' https://scan-api-44s6izf3qa-uc.a.run.app/scan"
                }
            }
        }

        stage('Build Image') {
            steps {
                script {
                    account = docker.build("pejassinaturasdocker/gateway:${env.BUILD_ID}", "-- ", "-f Dockerfile .")
                }
            }
        }
        stage('Push Image'){
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credential') {
                        account.push("${env.BUILD_ID}")
                        account.push("latest")
                    }
                }
            }
        }
        stage('Deploy on k8s LOCAL') {
            when {
                environment name: 'TARGET', value: 'local'
            }
            steps {
                witheCredentials([ string(credentialsId: 'minikube-credential', variable: 'api_token') ]) {
                    sh 'kubectl --token $api_token --server https://host.docker.internal:${env.K8S_PORT} --insecure-skip-tls-verify=true apply -f ./k8s/deployment.yaml '
                    sh 'kubectl --token $api_token --server https://host.docker.internal:${env.K8S_PORT} --insecure-skip-tls-verify=true apply -f ./k8s/service.yaml '
                }
            }
        }
        stage('Deploy on AWS K8s') {
            when { 
                environment name: 'TARGET', value: 'aws' 
            }
            steps {
                withCredentials([ string(credentialsId: 'minikube-credential', variable: 'api_token') ]) {
                    sh "kubectl apply -f ./k8s/deployment.yaml"
                    sh "kubectl apply -f ./k8s/service.yaml"
                }
            }
        }
    }
}