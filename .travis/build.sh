#!/usr/bin/env bash
. tools/batfish_functions.sh
batfish_build_all || return 1
#allinone -cmdfile tests/java/commands || return 1
#allinone -cmdfile test_rigs/parsing-test.cmds || return 1
coordinator &
batfish -servicemode -register -coordinatorhost localhost -loglevel output &
pybatfish tests/python/commands.py  || return 1


