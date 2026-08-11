 #!/bin/bash
    
    echo "running ng build"
    cd myapp-client
    ng build

    cd ../myapp-jpa-server
    mvn clean install -DskipTests
    

    # Filebeat refuses configuration files writable by group or other users.
    chmod go-w ../filebeat/filebeat.yml

    echo "Starting Docker Compose services with rebuild..."
    docker compose up --build
    if [ $? -eq 0 ]; then
        echo "Docker Compose services started successfully."
    else
        echo "Error starting Docker Compose services."
        exit 1
    fi