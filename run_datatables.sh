 #!/bin/bash
    
    echo "Starting DATATABLES..."
    cd myapp-jpa-server
    read -rsp 'Vault token: ' VAULT_TOKEN
    export VAULT_TOKEN
    echo "Starting Docker Compose with Vault token entered"
    sh docker-compose.sh up --build
    unset VAULT_TOKEN
