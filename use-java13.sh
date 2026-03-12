#!/usr/bin/env bash

export JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-13.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "JAVA_HOME=$JAVA_HOME"
java -version
