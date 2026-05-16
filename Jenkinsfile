pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    DEPLOY_BRANCH = 'main'
    COMPOSE_PROJECT_NAME = 'health-center'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Check Branch') {
      steps {
        script {
          String currentBranch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
          currentBranch = currentBranch.replaceFirst(/^origin\//, '')
          if (!currentBranch?.trim()) {
            currentBranch = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
          }

          if (currentBranch != env.DEPLOY_BRANCH) {
            currentBuild.result = 'NOT_BUILT'
            error("Deploy is allowed only on ${env.DEPLOY_BRANCH}. Current branch: ${currentBranch}")
          }
        }
      }
    }

    stage('Tool Versions') {
      steps {
        sh 'java -version'
        sh 'mvn -version'
        sh 'node --version'
        sh 'npm --version'
        sh 'docker version'
        sh 'docker compose version'
      }
    }

    stage('Backend Test Compile') {
      steps {
        dir('backend') {
          sh 'mvn -q test-compile'
        }
      }
    }

    stage('Backend Package') {
      steps {
        dir('backend') {
          sh 'mvn -q -DskipTests package'
        }
      }
    }

    stage('Frontend Build') {
      steps {
        dir('frontend') {
          sh 'npm ci'
          sh 'npm run build'
        }
      }
    }

    stage('Prepare Env') {
      steps {
        withCredentials([file(credentialsId: 'health-center-env-file', variable: 'ENV_FILE')]) {
          sh '''
            cp "$ENV_FILE" .env
            chmod 600 .env
          '''
        }
      }
    }

    stage('Docker Compose Config') {
      steps {
        sh 'docker compose --env-file .env config'
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker compose --env-file .env build'
      }
    }

    stage('Deploy') {
      steps {
        sh 'docker compose --env-file .env up -d --remove-orphans'
        sh 'docker compose --env-file .env ps'
      }
    }

    stage('Cleanup') {
      steps {
        sh 'docker image prune -f'
      }
    }
  }
}
