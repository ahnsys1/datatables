FROM nginx:alpine

# Copy the built application from the builder stage
COPY dist/myapp/browser /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Command to start nginx
CMD ["nginx", "-g", "daemon off;"]
