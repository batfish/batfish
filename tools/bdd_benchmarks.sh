#!/bin/bash

JSON_OUT_DIR="$1"
CSV_OUT="$2"
MODE="$3"

TGT="//projects/batfish/src/main/java/org/batfish/allinone/bdd/main:benchOrAll"
CMD="bazel run $TGT -- -rf json -jvmArgs -Xmx16G"

# extract the benchmark params
PARAMS=$($CMD -lp | sed -n 's/^.*param \"\(.*\)\".*$/\1/p' | sort | uniq | tr '\n' ',' | sed 's/,$//')

if [ -n "$PARAMS" ]
then
  PARAM_HEADERS=",$PARAMS"
  PARAMS_QUERY=",.params.$(echo $PARAMS | sed 's/,/, .params./g')"
else
  PARAM_HEADERS=""
  PARAMS_QUERY=""
fi

if [ "$MODE" = "dryrun" ]
then
  # do the minimial amount of work, not real benching
  CMD="$CMD -i 1 -f 1 -wi 1 -w 1 -r 1"
fi

function run() {
  BDD_FACTORY=$1
  THREADS=$2
  CMD_OUT="${JSON_OUT_DIR}/${BDD_FACTORY}-${THREADS}.json"
  $CMD -p _factoryName=$BDD_FACTORY -t $THREADS -rff $CMD_OUT
  jq -r ".[] | [.benchmark, .primaryMetric.score, .primaryMetric.scoreError$PARAMS_QUERY] | @csv" < $CMD_OUT \
    | sed "s/^/${THREADS},/" >> $CSV_OUT
}

echo "Threads,Benchmark,Score,Error$PARAM_HEADERS" > $CSV_OUT

run origJFactory 1 $b
run JFactory 1 $b
run JFactory 2 $b
run JFactory 3 $b
run JFactory 4 $b
run JFactory 5 $b
run JFactory 6 $b
run JFactory 7 $b
run JFactory 8 $b
run JFactory 10 $b
run JFactory 12 $b
run JFactory 16 $b
run JFactory 20 $b
