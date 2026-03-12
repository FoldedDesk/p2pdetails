#!/usr/bin/env bash

export JAVA_HOME="/opt/local/Library/Java/JavaVirtualMachines/openjdk17/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "JAVA_HOME=$JAVA_HOME"
java -version
