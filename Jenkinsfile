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
                    yaml """
apiVersion: v1
kind: Pod
spec:
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
                }
            }
        }

        stage('Build and Push Image') {
            agent {
                kubernetes {
                    yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: podman
    image: quay.io/buildah/podman:latest
    command: ['cat']
    tty: true
    securityContext:
      privileged: true
"""
                }
            }
            steps {
                container('podman') {
                    sh """
                        cat /var/run/secrets/kubernetes.io/serviceaccount/token | podman login -u unused -p stdin \$REGISTRY --tls-verify=false
                        podman build -t ${env.IMAGE_TAG} .
                        podman push ${env.IMAGE_TAG}
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
