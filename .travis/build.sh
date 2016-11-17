#!/usr/bin/env bash

trap 'kill -9 $(pgrep -g $$ | grep -v $$) >& /dev/null' EXIT SIGINT SIGTERM

. tools/batfish_functions.sh
batfish_build_all || exit 1

echo -e "\n  ..... Running parsing tests"
allinone -cmdfile test_rigs/parsing-test.cmds || exit 1

echo -e "\n  ..... Running java client tests"
allinone -cmdfile tests/java/commands || exit 1

echo -e "\n  ..... Running python client tests"
coordinator &
batfish -servicemode -register -coordinatorhost localhost -loglevel output &
pybatfish tests/python/commands.py || exit 1

echo -e "\n  ..... Running java demo tests"
allinone -cmdfile demo/java/commands > demo/java/commands.ref.testout || exit 1
rm demo/java/commands.ref.testout
#export diffcount=`diff demo/java/commands.{ref,ref.testout}`
#if [ $diffcount == 100]; then 
#	rm demo/java/commands.ref.testout
#fi

echo -e "\n  ..... Running java demo tests"
pybatfish demo/python/commands.py > demo/python/commands.ref.testout || exit 1
rm demo/python/commands.ref.testout
#export diffcount=`diff demo/java/commands.{ref,ref.testout}`
#if [ $diffcount == 186]; then 
#	rm demo/java/commands.ref.testout
#fi

echo -e "\n .... Failed tests: "
find -name *.testout

echo -e "\n .... Diffing failed tests:"
for i in $(find -name *.testout); do
   echo -e "\n $i"; diff -u ${i%.testout} $i
done

#exit with exit code 1 if any test failed
if [ -n "$(find -name '*.testout')" ]; then
   exit 1
fi

