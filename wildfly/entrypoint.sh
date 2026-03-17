#!/usr/bin/env bash
set -euo pipefail

JBOSS_HOME=${JBOSS_HOME:-/opt/jboss/wildfly}
DB_HOST=${POSTGRES_SERVICE_HOST:-postgres}
DB_PORT=${POSTGRES_SERVICE_PORT:-5432}
DB_NAME=${POSTGRES_DB:-retail}
DB_USER=${POSTGRES_USER:-retail}
DB_PASSWORD=${POSTGRES_PASSWORD}

if [ -z "$DB_PASSWORD" ]; then
  echo "POSTGRES_PASSWORD not set"
  exit 1
fi

CONNECTION_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"

echo "Configuring PostgreSQL datasource RetailDS (offline)..."

CLI_FILE="$(mktemp)"
cat > "${CLI_FILE}" <<EOF
embed-server --std-out=echo --server-config=standalone.xml

if (outcome != success) of /subsystem=micrometer:read-resource
  /subsystem=micrometer:add
end-if

if (outcome != success) of /subsystem=micrometer/registry=prometheus:read-resource
  /subsystem=micrometer/registry=prometheus:add(context="/metrics",security-enabled=false)
end-if

if (outcome != success) of /subsystem=datasources/jdbc-driver=postgresql:read-resource
  /subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)
end-if

if (outcome != success) of /subsystem=datasources/data-source=RetailDS:read-resource
  data-source add --name=RetailDS --jndi-name=java:jboss/datasources/RetailDS --driver-name=postgresql --connection-url=${CONNECTION_URL} --user-name=${DB_USER} --password=${DB_PASSWORD} --enabled=true
end-if

stop-embedded-server
EOF

"$JBOSS_HOME/bin/jboss-cli.sh" --file="${CLI_FILE}"
rm -f "${CLI_FILE}"

echo "Starting WildFly..."
exec "$JBOSS_HOME/bin/standalone.sh" -b 0.0.0.0 -bmanagement 0.0.0.0 "$@"
