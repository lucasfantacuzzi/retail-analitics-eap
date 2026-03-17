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
    resources:
      requests:
        cpu: "100m"
        memory: "256Mi"
      limits:
        cpu: "500m"
        memory: "512Mi"
  - name: jnlp
    image: jenkins/inbound-agent:3355.v388858a_47b_33-3-jdk21
    resources:
      requests:
        cpu: "100m"
        memory: "256Mi"
      limits:
        cpu: "500m"
        memory: "512Mi"
"""
        }
    }

    environment {
        APP_NAME     = "retail-analytics"
        REGISTRY     = "image-registry.openshift-image-registry.svc.cluster.local:5000"
        NAMESPACE    = "application"
        VERSION_TAG  = "${env.BUILD_NUMBER}"
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
                        oc tag ${env.APP_NAME}:latest ${env.APP_NAME}:${env.VERSION_TAG}
                    """
                }
            }
        }

        stage('Deploy to OpenShift') {
            options {
                timeout(time: 20, unit: 'MINUTES')
            }
            steps {
                container('oc') {
                    sh """
                        set -e
                        oc project ${env.NAMESPACE}
                        oc apply -f k8s/deployment.yaml
                        oc apply -f k8s/service.yaml

                        # Congela em 1 réplica (evita briga com HPA durante deploy)
                        oc scale deployment/${env.APP_NAME} -n ${env.NAMESPACE} --replicas=1

                        # Faz deploy de uma versão específica (tag numerada), sem depender de :latest
                        oc set image deployment/${env.APP_NAME} ${env.APP_NAME}=${env.REGISTRY}/${env.NAMESPACE}/${env.APP_NAME}:${env.VERSION_TAG} -n ${env.NAMESPACE} --record

                        # Sem timeout do oc (para debugar). Jenkins corta em 20min (stage timeout).
                        oc rollout status deployment/${env.APP_NAME} -n ${env.NAMESPACE}
                    """
                }
            }
            post {
                failure {
                    container('oc') {
                        sh """
                            oc project ${env.NAMESPACE}
                            oc rollout undo deployment/${env.APP_NAME} || true
                        """
                    }
                }
            }
        }
    }
}
