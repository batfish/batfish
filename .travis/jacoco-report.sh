#!/usr/bin/env bash

# usage: $0 [junit]
#
# build a jacoco coverage report.
# 
# if junit flag is present, only report coverage of junit tests.
# otherwise, report coverage of junit and ref tests.

TRAVIS_DIR="$(dirname $0)"

. "$TRAVIS_DIR/common.sh"

arg=$1

if [ -z "$("$GNU_FIND" -name 'jacoco*.exec')" ]
then
    echo "No coverage data files found. Try again after running .travis/build.sh"
    exit 1
fi

cp -r projects/*/target/classes/* "$JACOCO_CLASSES_DIR"

if [ "$arg" = 'junit' ]; then
  java -jar "$JACOCO_CLI_JAR" merge $("$GNU_FIND" -name 'jacoco.exec') --destfile "$JACOCO_JUNIT_DESTFILE"
  JACOCO_REPORT_DESTFILE="$JACOCO_JUNIT_DESTFILE"
else
  JACOCO_REPORT_DESTFILE="$JACOCO_ALL_DESTFILE"
fi    

java -jar "$JACOCO_CLI_JAR" report "$JACOCO_REPORT_DESTFILE"  --classfiles "$JACOCO_CLASSES_DIR" --html "$JACOCO_COVERAGE_REPORT_HTML"

open "$JACOCO_COVERAGE_REPORT_HTML/index.html"
