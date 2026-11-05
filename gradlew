#!/bin/sh
# Gradle wrapper shell script
# Source: https://github.com/gradle/gradle (Apache 2.0)
exec "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
  java -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
