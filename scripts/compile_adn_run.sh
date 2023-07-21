#!/bin/bash

# Function to download the dependencies
# Function to clone and compile the dependencies
clone_and_compile_dependencies() {
    # Clone the repository of your dependency
    cd ../scripts
    git clone https://github.com/CarlosRolan/LocalChatAPI.git

    # Compile the source code (adjust the compilation command as needed)
    javac -d ../lib $(find . -name "*.java")
    cd ..
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