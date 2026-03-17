#!/bin/bash
# Aplica o ambiente (namespace + PostgreSQL) no cluster.
# Execute antes do primeiro deploy da aplicação ou quando quiser recriar só o ambiente.
set -e
echo "Criando namespace application..."
oc apply -f namespace.yaml
echo "Aplicando PostgreSQL (secret, pvc, deployment, service)..."
oc apply -f postgres/secret.yaml
oc apply -f postgres/pvc.yaml
oc apply -f postgres/deployment.yaml
oc apply -f postgres/service.yaml
echo "Ambiente aplicado. Aguarde o Postgres ficar Ready antes de subir a aplicação."
echo "Verifique com: oc get pods -n application -w"
