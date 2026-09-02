 #!/bin/bash
    
    echo "Starting Infrastructure..."
    cd infrastructure/rest
    read -rsp 'Vault token: ' VAULT_TOKEN
    export VAULT_TOKEN
    cd infrastructure/rest
    echo "Starting Docker Compose with Vault token entered"
    sh docker-compose-rest.sh up --build
    unset VAULT_TOKEN
