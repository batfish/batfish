#!/usr/bin/env bash

if [[ $(uname) == 'Darwin' ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

trap 'kill -9 $(pgrep -g $$ | grep -v $$) >& /dev/null' EXIT SIGINT SIGTERM

. tools/batfish_functions.sh
batfish_test_all || exit 1

echo -e "\n  ..... Running parsing tests"
allinone -cmdfile test_rigs/parsing-tests/commands || exit 1

echo -e "\n  ..... Running parsing tests with error"
allinone -cmdfile test_rigs/parsing-errors-tests/commands || exit 1

echo -e "\n  ..... Running basic client tests"
allinone -cmdfile tests/basic/commands || exit 1

echo -e "\n  ..... Running jsonpath-addons tests"
allinone -cmdfile tests/jsonpath-addons/commands || exit 1

echo -e "\n  ..... Running ui-focused client tests"
allinone -cmdfile tests/ui-focused/commands || exit 1

echo -e "\n  ..... Running aws client tests"
allinone -cmdfile tests/aws/commands || exit 1

echo -e "\n  ..... Running java-smt client tests"
allinone -cmdfile tests/java-smt/commands || exit 1

#Test running separately
coordinator &
batfish -servicemode -register -coordinatorhost localhost -loglevel output &

echo -e "\n  ..... Running java demo tests"
if ! batfish_client -cmdfile demos/example/commands -coordinatorhost localhost > demos/example/commands.ref.testout; then
   echo "DEMO FAILED!" 1>&2
else
   rm demos/example/commands.ref.testout
fi

echo -e "\n .... Failed tests: "
$GNU_FIND -name *.testout

echo -e "\n .... Diffing failed tests:"
for i in $($GNU_FIND -name *.testout); do
   echo -e "\n $i"; diff -u ${i%.testout} $i
done

#exit with exit code 1 if any test failed
if [ -n "$($GNU_FIND -name '*.testout')" ]; then
   exit 1
fi

echo 'Success!'
