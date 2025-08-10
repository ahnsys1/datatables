# Use a minimal base image for running the application
FROM alpine/java:22-jdk

# Set the working directory
WORKDIR /app

# Copy the packaged application from the builder stage
COPY /target/*.jar app.jar

# Expose the port the application runs on (e.g., 9090)
EXPOSE 9090

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
