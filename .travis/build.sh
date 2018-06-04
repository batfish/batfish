#!/usr/bin/env bash
JACOCO_VERSION=0.8.1
JACOCO_AGENT_JAR_NAME="org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
JACOCO_AGENT_JAR="${HOME}/.m2/repository/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/${JACOCO_AGENT_JAR_NAME}"
JACOCO_CLI_JAR_NAME="org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar"
JACOCO_CLI_JAR="${HOME}/.m2/repository/org/jacoco/org.jacoco.cli/${JACOCO_VERSION}/${JACOCO_CLI_JAR_NAME}"

# the destfile for ref tests
JACOCO_REF_DESTFILE=projects/target/jacoco-ref.exec

# the destfile for the aggregate of the ref tests and all junit project tests
JACOCO_ALL_DESTFILE=projects/target/jacoco-all.exec

JACOCO_COVERAGE_REPORT_XML=coverage.xml


if [ ! -f ${JACOCO_AGENT_JAR} ]; then
  mvn dependency:get -Dartifact=org.jacoco:org.jacoco.agent:${JACOCO_VERSION}:jar:runtime
fi

if [ ! -f ${JACOCO_CLI_JAR} ]; then
  mvn dependency:get -Dartifact=org.jacoco:org.jacoco.cli:${JACOCO_VERSION}:jar:nodeps
fi

if [[ $(uname) == 'Darwin' && $(which gfind) ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

trap 'kill -9 $(pgrep -g $$ | grep -v $$) >& /dev/null' EXIT SIGINT SIGTERM

. tools/batfish_functions.sh


# Build batfish and run the Maven unit tests.
batfish_test_all || exit 1

# Configure arguments for allinone throughout later runs.
export ALLINONE_JAVA_ARGS=" \
  -enableassertions \
  -DbatfishCoordinatorPropertiesPath=${BATFISH_ROOT}/.travis/travis_coordinator.properties \
  -javaagent:${JACOCO_AGENT_JAR}=destfile=${JACOCO_REF_DESTFILE} \
"

exit_code=0
echo -e "\n  ..... Running parsing tests"
allinone -cmdfile tests/parsing-tests/commands || exit_code=$?

echo -e "\n  ..... Running parsing tests with error"
allinone -cmdfile tests/parsing-errors-tests/commands || exit_code=$?

echo -e "\n  ..... Running basic client tests"
allinone -cmdfile tests/basic/commands || exit_code=$?

echo -e "\n  ..... Running role functionality tests"
allinone -cmdfile tests/roles/commands || exit_code=$?

echo -e "\n  ..... Running jsonpath tests"
allinone -cmdfile tests/jsonpath-addons/commands || exit_code=$?
allinone -cmdfile tests/jsonpathtotable/commands || exit_code=$?

echo -e "\n  ..... Running ui-focused client tests"
allinone -cmdfile tests/ui-focused/commands || exit_code=$?

echo -e "\n  ..... Running aws client tests"
allinone -cmdfile tests/aws/commands || exit_code=$?

echo -e "\n  ..... Running java-smt client tests"
allinone -cmdfile tests/java-smt/commands || exit_code=$?

echo -e "\n  ..... Running watchdog tests"
allinone -cmdfile tests/watchdog/commands -batfishmode watchdog || exit_code=$?
sleep 5

echo -e "\n .... Aggregating coverage data"
java -jar $JACOCO_CLI_JAR merge $(find -name 'jacoco*.exec') --destfile $JACOCO_ALL_DESTFILE

echo -e "\n .... Building coverage report"
# have to collect all classes into one dir
JACOCO_CLASSES_DIR="$(mktemp -d)"
cp -r projects/*/target/classes/* $JACOCO_CLASSES_DIR
java -jar $JACOCO_CLI_JAR report $JACOCO_ALL_DESTFILE  --classfiles $JACOCO_CLASSES_DIR --xml $JACOCO_COVERAGE_REPORT_XML

echo -e "\n .... Failed tests: "
$GNU_FIND -name *.testout

echo -e "\n .... Diffing failed tests:"
for i in $($GNU_FIND -name *.testout); do
   echo -e "\n $i"; diff -u ${i%.testout} $i
done

exit $exit_code
