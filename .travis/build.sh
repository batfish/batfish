#!/usr/bin/env bash

. tools/batfish_functions.sh
batfish_build_all || exit 1

#echo -e "\n  ..... Running parsing tests"
#allinone -cmdfile test_rigs/parsing-test.cmds || return 1

#echo -e "\n  ..... Running java client tests"
#allinone -cmdfile tests/java/commands || return 1

echo -e "\n  ..... Running python client tests"
coordinator -loglevel debug &
batfish -servicemode -register -coordinatorhost localhost -loglevel debug &
sleep 60
pybatfish tests/python/commands.py  || exit 1

echo -e "\n .... Failed tests: "
find -name *.testout

#exit with exit code 1 if any test failed
if [ -n "$(find -name '*.testout')" ]; then exit 1; fi

