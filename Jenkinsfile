pipeline {
    agent any

    stages {
        stage("Init") {
            steps {
                checkout scm
            }
        }

        stage("Clean") {
            steps {
                sh "./gradlew clean"
            }
        }

        stage("Assemble") {
            steps {
                sh "./gradlew assemble --refresh-dependencies -i"
            }
        }

        stage("Deploy") {
            when {
                anyOf {
                    branch "master*"
                    branch "release*"
                }
            }
            steps {
                script {
                    sh "./gradlew publishMavenJavaPublicationToMavenRepository -x test"
                }
            }
        }
    }


    post {
        always {
            sh "./gradlew clean"
        }
    }
}


