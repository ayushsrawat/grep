#!/bin/bash

PROJECT_ROOT="$(cd "$(dirname "$0")" && cd .. && pwd)"

#mvn -q clean
#mvn -q compile
#mvn -q package

DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
DEBUG=""

CLASSPATH="$PROJECT_ROOT/target/classes:"\
$HOME/.m2/repository/commons-cli/commons-cli/1.9.0/commons-cli-1.9.0.jar

java $DEBUG -classpath "$CLASSPATH" "$PROJECT_ROOT"/src/main/java/com/github/drsqrt/Grep.java "$@"