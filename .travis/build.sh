#!/usr/bin/env bash
. tools/batfish_functions.sh
batfish_build_all || return 1
allinone -cmdfile tests/commands || return 1
allinone -cmdfile test_rigs/parsing-test.cmds || return 1

