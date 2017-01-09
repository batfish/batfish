#!/usr/bin/env bash

if [[ $(uname) == 'Darwin' ]]; then
   GNU_FIND=gfind
else
   GNU_FIND=find
fi

trap 'kill -9 $(pgrep -g $$ | grep -v $$) >& /dev/null' EXIT SIGINT SIGTERM

# Build and install pybatfish
pip install projects/pybatfish || exit 1

. tools/batfish_functions.sh
batfish_build_all || exit 1

echo -e "\n  ..... Running parsing tests"
allinone -cmdfile test_rigs/parsing-tests/commands || exit 1

echo -e "\n  ..... Running java client tests"
allinone -cmdfile tests/java/commands || exit 1

echo -e "\n  ..... Running python client tests"
coordinator &
batfish -servicemode -register -coordinatorhost localhost -loglevel output &
python tests/python/commands.py || exit 1

echo -e "\n  ..... Running java demo tests"
#using batfish_client since the coordinator is running due to python client testing
batfish_client -cmdfile demos/java/commands -coordinatorhost localhost > demos/java/commands.ref.testout || exit 1
rm demos/java/commands.ref.testout
#export diffcount=`diff demos/java/commands.{ref,ref.testout}`
#if [ $diffcount == 100]; then 
#	rm demos/java/commands.ref.testout
#fi

echo -e "\n  ..... Running python demo tests"
#coordinator should be running due to python client testing
python demos/python/commands.py > demos/python/commands.ref.testout || exit 1
rm demos/python/commands.ref.testout
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
