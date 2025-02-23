#!/bin/bash

PROJECT_ROOT="$(cd "$(dirname "$0")" && cd .. && pwd)"
#echo "Project Root: $PROJECT_ROOT"

CLASSPATH="$PROJECT_ROOT/target/classes:"\
$HOME/.m2/repository/commons-cli/commons-cli/1.9.0/commons-cli-1.9.0.jar

java -classpath "$CLASSPATH" "$PROJECT_ROOT"/src/main/java/com/github/drsqrt/Grep.java -s verbose -f ~/cs/java/grep/src/