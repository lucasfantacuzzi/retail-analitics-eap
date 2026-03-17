# Kubernetes / OpenShift – ambiente `application`

## Ordem de aplicação

1. **Ambiente (namespace + PostgreSQL)**  
   Aplicado **manualmente** no servidor (o Jenkinsfile não gerencia isso).

   ```bash
   oc apply -f namespace.yaml
   oc apply -f postgres/secret.yaml
   oc apply -f postgres/pvc.yaml
   oc apply -f postgres/deployment.yaml
   oc apply -f postgres/service.yaml
   ```

   Ou, a partir da pasta `k8s`:

   ```bash
   ./apply-env.sh
   ```

2. **Aplicação (retail-analytics)**  
   O **pipeline** aplica só `deployment.yaml` e `service.yaml` (atualiza imagem e recursos da app no namespace `application`).  
   A aplicação usa o datasource `java:jboss/datasources/RetailDS` apontando para o service `postgres` (variáveis de ambiente vindas do Secret `postgres`).

## Permissões para o Jenkins

Para o pipeline conseguir publicar imagem no namespace `application` e criar recursos:

```bash
# Permissão de push no registry (namespace application)
oc adm policy add-role-to-user system:image-builder system:serviceaccount:jenkins:default -n application

# Permissão de editar recursos no namespace application (deploy)
oc adm policy add-role-to-user edit system:serviceaccount:jenkins:default -n application
```

Substitua `jenkins:default` pelo service account que o Jenkins usa no seu cluster, se for outro.

## Estrutura

| Arquivo | Descrição |
|---------|-----------|
| `namespace.yaml` | Namespace `application` |
| `postgres/secret.yaml` | Secret com usuário/senha/DB do PostgreSQL |
| `postgres/pvc.yaml` | PVC para dados do PostgreSQL |
| `postgres/deployment.yaml` | Deployment do PostgreSQL |
| `postgres/service.yaml` | Service `postgres` (porta 5432) |
| `deployment.yaml` | Deployment da aplicação retail-analytics |
| `service.yaml` | Service da aplicação |
