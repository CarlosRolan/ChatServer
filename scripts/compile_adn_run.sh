#!/bin/bash

# Function to download the dependencies
download_dependencies() {
    # Download the JAR files to the lib directory using curl or wget
    # For example, using curl:
    curl -o ../lib/LocalChatAPI.jar "https://github.com/CarlosRolan/LocalChatAPI.git"
}

# Check if the "lib" directory exists. If not, create it.
if [ ! -d "lib" ]; then
    mkdir lib
fi

# Download dependencies
download_dependencies

# Compiling the project with the dependencies on the classpath
javac -cp ../lib/*:. src/*.java

# Running the project with the dependencies on the classpath
java -cp ../lib/*:. Main