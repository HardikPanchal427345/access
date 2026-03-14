#!/bin/sh

##############################################################################
##
##  Gradle start up script for POSIX generated manually.
##
##############################################################################

APP_HOME=$(cd "${0%/*}" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
        exit 1
    fi
else
    JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1 ; then
    echo "ERROR: Java not found. Install JDK 17+ and set JAVA_HOME." >&2
    exit 1
fi

DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -Dorg.gradle.appname=gradlew \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
