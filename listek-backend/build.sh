 #!/bin/bash
    
    echo "running ng build"
    cd ../listek-frontend
    ng build


    cd ../listek-admin
    mvn clean install -DskipTests
    

    # Filebeat refuses configuration files writable by group or other users.
    chmod go-w ../filebeat/filebeat.yml

    echo "Starting Docker Compose services with rebuild..."
    read -rsp 'Vault token: ' VAULT_TOKEN
    export VAULT_TOKEN
    echo
    sh docker-compose-vault.sh up --build
    unset VAULT_TOKEN
