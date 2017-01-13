#!/usr/bin/env bash

if [[ $(uname) == 'Darwin' ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

trap 'kill -9 $(pgrep -g $$ | grep -v $$) >& /dev/null' EXIT SIGINT SIGTERM

. tools/batfish_functions.sh
batfish_build_all || exit 1

echo -e "\n  ..... Running parsing tests"
allinone -cmdfile test_rigs/parsing-tests/commands || exit 1

echo -e "\n  ..... Running java client tests"
allinone -cmdfile tests/java/commands || exit 1

#Test running separately
coordinator &
batfish -servicemode -register -coordinatorhost localhost -loglevel output &

echo -e "\n  ..... Running java demo tests"
batfish_client -cmdfile demos/java/commands -coordinatorhost localhost > demos/java/commands.ref.testout || exit 1
rm demos/java/commands.ref.testout

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
