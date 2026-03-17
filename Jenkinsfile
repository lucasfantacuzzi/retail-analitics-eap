pipeline {
    agent {
        kubernetes {
            defaultContainer 'oc'
            workspaceVolume emptyDirWorkspaceVolume()
            yaml """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: default
  securityContext:
    runAsUser: 0
  containers:
  - name: oc
    image: quay.io/openshift/origin-cli:4.15
    command: ['cat']
    tty: true
"""
        }
    }

    environment {
        APP_NAME     = "retail-analytics"
        NAMESPACE    = "application"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Push Image (OpenShift BuildConfig)') {
            steps {
                container('oc') {
                    sh """
                        set -e
                        oc project ${env.NAMESPACE}
                        oc apply -f k8s/buildconfig.yaml

                        # Binary build: envia o diretório do workspace como contexto do Dockerfile
                        oc start-build ${env.APP_NAME} --from-dir=. --follow --wait

                        # Cria/atualiza uma tag numerada igual ao BUILD_NUMBER (além de latest)
                        oc tag ${env.APP_NAME}:latest ${env.APP_NAME}:${env.BUILD_NUMBER}
                    """
                }
            }
        }

        stage('Deploy to OpenShift') {
            steps {
                container('oc') {
                    sh """
                        set -e
                        oc project ${env.NAMESPACE}
                        oc apply -f k8s/deployment.yaml
                        oc apply -f k8s/service.yaml

                        # Garante rollout (mesmo usando :latest no YAML)
                        oc rollout restart deployment/${env.APP_NAME} || true
                    """
                }
            }
        }
    }
}
