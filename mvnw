#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for UNIX / Linux / macOS
# Compatible with Ubuntu EC2 environments
# ----------------------------------------------------------------------------

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
BASE_DIR=`dirname "$PRG"`

# Check Java executable
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
else
  JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1 ; then
  echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  echo "Please install OpenJDK 21 (e.g. 'sudo apt-get install -y openjdk-21-jdk')." >&2
  exit 1
fi

# 1. If 'mvn' is installed globally, use it
if command -v mvn >/dev/null 2>&1 ; then
  exec mvn "$@"
fi

# 2. Check ~/.m2/wrapper/ cache if present
USER_WRAPPER_MVN=$(find "$HOME/.m2/wrapper/dists" -name mvn -type f 2>/dev/null | head -n 1)
if [ -n "$USER_WRAPPER_MVN" ] && [ -x "$USER_WRAPPER_MVN" ] ; then
  exec "$USER_WRAPPER_MVN" "$@"
fi

echo "Error: 'mvn' is not found in PATH." >&2
echo "Please install maven on Ubuntu ('sudo apt-get update && sudo apt-get install -y maven')." >&2
exit 1
