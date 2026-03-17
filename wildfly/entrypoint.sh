#!/bin/bash
set -e

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

# Start WildFly in background so we can add the datasource via CLI
"$JBOSS_HOME/bin/standalone.sh" -b 0.0.0.0 -bmanagement 0.0.0.0 "$@" &
PID=$!

# Wait for management interface
for i in $(seq 1 60); do
  if "$JBOSS_HOME/bin/jboss-cli.sh" -c ":read-attribute(name=server-state)" 2>/dev/null | grep -q "running"; then
    break
  fi
  sleep 2
done

# Add PostgreSQL driver and datasource via CLI (idempotent)
"$JBOSS_HOME/bin/jboss-cli.sh" -c "
  /subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)
" 2>/dev/null || true
"$JBOSS_HOME/bin/jboss-cli.sh" -c "
  data-source add --name=RetailDS \
    --jndi-name=java:jboss/datasources/RetailDS \
    --driver-name=postgresql \
    --connection-url=${CONNECTION_URL} \
    --user-name=${DB_USER} \
    --password=${DB_PASSWORD} \
    --enabled=true
" 2>/dev/null || true

# Wait for the server process (keep container running)
wait $PID
