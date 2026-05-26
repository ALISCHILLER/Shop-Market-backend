#!/bin/sh
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
  exec java -jar "$WRAPPER_JAR" "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
printf '%s\n' 'Gradle wrapper jar is not present in this generated archive.'
printf '%s\n' 'Install Gradle 9.1+ and run: gradle wrapper --gradle-version 9.1.0'
printf '%s\n' 'Then run: ./gradlew clean bootRun'
exit 1
