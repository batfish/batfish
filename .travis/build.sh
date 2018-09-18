#!/usr/bin/env bash
. "$(dirname "$0")/common.sh"

trap 'kill -9 $(pgrep -g $$ | grep -v $$) >& /dev/null' EXIT SIGINT SIGTERM

. "$(dirname "$0")/../tools/batfish_functions.sh"

# Run question formatting tests, and exit early if they fail.
echo -e "\n  ..... Running question formatting tests"
pushd tests
python3 -m pytest || exit 1
popd

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

for dir in 'stable' 'experimental'
do
  echo -e "\n  ..... Running ${dir} questions tests"
  allinone -cmdfile tests/questions/${dir}/commands || exit_code=$?
done

echo -e "\n  ..... Running aws client tests"
allinone -cmdfile tests/aws/commands || exit_code=$?

echo -e "\n  ..... Running java-smt client tests"
allinone -cmdfile tests/java-smt/commands || exit_code=$?

# echo -e "\n  ..... Running watchdog tests"
# allinone -cmdfile tests/watchdog/commands -batfishmode watchdog || exit_code=$?
# sleep 5

echo -e "\n .... Aggregating coverage data"
java -jar "$JACOCO_CLI_JAR" merge $("$GNU_FIND" -name 'jacoco*.exec') --destfile "$JACOCO_ALL_DESTFILE"

echo -e "\n .... Building coverage report"
# have to collect all classes into one dir
cp -r projects/*/target/classes/* "$JACOCO_CLASSES_DIR"
java -jar "$JACOCO_CLI_JAR" report "$JACOCO_ALL_DESTFILE"  --classfiles "$JACOCO_CLASSES_DIR" --xml "$JACOCO_COVERAGE_REPORT_XML"

echo -e "\n .... Failed tests: "
"$GNU_FIND" -name *.testout

echo -e "\n .... Diffing failed tests:"
for i in $("$GNU_FIND" -name *.testout); do
   echo -e "\n $i"; diff -u ${i%.testout} ${i}
done

exit ${exit_code}
