pipeline {
    agent {
        label "worker"
    }
    
    environment {
        DOCKER_URL = 'https://registry.hub.docker.com/'
        DOCKER_CRED_ID = 'docker_pass'
        DOCKER_IMAGE = 'vlad201/project:latest'
        
        GIT_URL = 'https://github.com/vpryymak/project_2.git'
        GIT_BRANCH = 'main'
        GIT_CRED = 'git_pass'
    }
    
    stages {
        stage('Git pull') {
            steps {
                script {
                    git url: "${GIT_URL}",
                        branch: "${GIT_BRANCH}",
                        credentialsId: "${GIT_CRED}"
                }
            }
        }
        stage('Image build'){
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}")
                }
            }
        }
        stage('Run test'){
            steps {
                script {
                    try { 
                        sh 'docker run ${DOCKER_IMAGE} run test'
                    }
                    catch (Exception e) {
                        echo 'Tests failed'
                        currentBuild.result = 'FAILURE'
                        error('Failure test')
                    }
                }
            }
        }
        stage('Login docker hub') {
            steps {
                script {
                    try {
                        docker.withRegistry("${DOCKER_URL}", "${DOCKER_CRED_ID}") {
                            echo 'Succes login in Docker'  
                        } 
                    }
                    catch (Exception e) {
                        echo 'Docker login errors'
                        currentBuild.result = 'FAILURE'
                        error('Docker login failure!')
                    }
                }
            }
        }
        stage('Login and push docker') {
            steps {
                script {
                    try {
                        docker.withRegistry("${DOCKER_URL}", "${DOCKER_CRED_ID}") {
                            docker.image("${DOCKER_IMAGE}").push()
                            echo 'Image pushed'  
                        } 
                    }
                    catch (Exception e) {
                        echo 'Pushed errors'
                        currentBuild.result = 'FAILURE'
                        error('Docker image push failure!')
                    }
                }
            }
        }
    }
}