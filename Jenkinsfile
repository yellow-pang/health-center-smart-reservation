// Archived VM deployment entry point. See docs/08_deploy/14_GitHub_Actions_Mac_CICD.md.
// This guard prevents an old polling Jenkins job from deploying main to the retired VM.
pipeline {
  agent none
  stages {
    stage('Deployment moved to GitHub Actions') {
      steps {
        error('VM deployment is retired. Use the root GitHub Actions CI and Deploy production workflows.')
      }
    }
  }
}
