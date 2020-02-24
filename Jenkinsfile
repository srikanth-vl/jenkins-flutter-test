pipeline {
    agent any
    stages {
        stage ('Checkout') {
            steps {
                checkout scm
            }
        }
        stage ('Flutter init') {
            steps {
                echo "Running flutter clean"
                sh "flutter clean"
                echo "Running flutter doctor"
                sh "flutter doctor"
            }
        }
    }
}