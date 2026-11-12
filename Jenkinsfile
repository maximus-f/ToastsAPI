pipeline {
    agent any
    tools {
        maven 'Maven'
        jdk 'jdk25'
    }
    stages {
        stage('Build and verify') {
            steps {
                // The agent needs the BuildTools outputs listed in README.md.
                sh 'mvn -B -ntp clean verify'
            }
        }
    }
    post {
        always {
            junit testResults: '**/target/*-reports/TEST-*.xml', allowEmptyResults: false
        }
        success {
            archiveArtifacts artifacts: 'ToastAPI/target/toastapi-*.jar,TestPlugin/target/toastapi-test-plugin-*.jar'
        }
    }
}
