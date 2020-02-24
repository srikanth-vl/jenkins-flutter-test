pipeline {
    agent any
    stages {
        stage ('Checkout') {
            steps {
                checkout scm
            }
        }
        stage ('Flutter doctor') {
            steps {
                sh "flutter doctor -v"
            }
        }
    }
}