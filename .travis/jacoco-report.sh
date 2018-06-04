#!/usr/bin/env bash

TRAVIS_DIR=$(dirname $0)

source $TRAVIS_DIR/jacoco-common.sh

# usage: $0 [junit]
arg=$1

cp -r projects/*/target/classes/* $JACOCO_CLASSES_DIR

if [ $arg = 'junit' ]; then
  java -jar $JACOCO_CLI_JAR merge $(find -name 'jacoco.exec') --destfile $JACOCO_JUNIT_DESTFILE
  JACOCO_REPORT_DESTFILE=$JACOCO_JUNIT_DESTFILE
else
  JACOCO_REPORT_DESTFILE=$JACOCO_ALL_DESTFILE
fi    

java -jar $JACOCO_CLI_JAR report $JACOCO_REPORT_DESTFILE  --classfiles $JACOCO_CLASSES_DIR --html $JACOCO_COVERAGE_REPORT_HTML

open $JACOCO_COVERAGE_REPORT_HTML/index.html
