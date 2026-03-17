pipeline {
    agent any

    environment {
        APP_NAME     = "retail-analytics"
        REGISTRY     = "image-registry.openshift-image-registry.svc.cluster.local:5000"
        NAMESPACE    = "application"
        IMAGE_TAG    = "${REGISTRY}/${NAMESPACE}/${APP_NAME}:${env.BUILD_NUMBER ?: 'latest'}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build WAR with Maven') {
            agent {
                kubernetes {
                    defaultContainer 'maven'
                    workspaceVolume emptyDirWorkspaceVolume()
                    yaml """
apiVersion: v1
kind: Pod
spec:
  securityContext:
    runAsUser: 0
  containers:
  - name: maven
    image: maven:3.9-eclipse-temurin-11
    command: ['cat']
    tty: true
"""
                }
            }
            steps {
                container('maven') {
                    sh 'mvn -q -e clean package'
                    stash name: 'build-context', includes: 'Dockerfile,pom.xml,wildfly/**,src/**'
                }
            }
        }

        stage('Build and Push Image') {
            agent {
                kubernetes {
                    defaultContainer 'kaniko'
                    workspaceVolume emptyDirWorkspaceVolume()
                    yaml """
apiVersion: v1
kind: Pod
spec:
  securityContext:
    runAsUser: 0
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:v1.23.2-debug
    command: ['cat']
    tty: true
"""
                }
            }
            steps {
                container('kaniko') {
                    unstash 'build-context'
                    sh """
                        mkdir -p /kaniko/.docker
                        TOKEN=\$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
                        AUTH=\$(echo -n "unused:\$TOKEN" | base64 -w 0)
                        echo "{\\"auths\\":{\\"${env.REGISTRY}\\":{\\"auth\\":\\"\$AUTH\\"}}}" > /kaniko/.docker/config.json
                        /kaniko/executor --context=\$(pwd) --dockerfile=Dockerfile --destination=${env.IMAGE_TAG} --insecure --skip-tls-verify
                    """
                }
            }
        }

        stage('Deploy to OpenShift') {
            steps {
                sh """
                    oc set image deployment/retail-analytics retail-analytics=${env.IMAGE_TAG} -n ${env.NAMESPACE} --record 2>/dev/null || true
                    oc apply -f k8s/deployment.yaml
                    oc apply -f k8s/service.yaml
                """
            }
        }
    }
}
