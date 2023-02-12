#!/usr/bin/env bash

sed -i "" 's/USE_NEW_METHOD = true/USE_NEW_METHOD = false/' projects/batfish-common-protocol/src/main/java/org/batfish/common/topology/L3Adjacencies.java
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET1 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET2 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET3 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET4 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET5 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET6 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET7 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET8 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET9 multipathConsistency
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET11 multipathConsistency

# Note: NET10 out of order
sed -i "" 's/USE_NEW_METHOD = false/USE_NEW_METHOD = true/' projects/batfish-common-protocol/src/main/java/org/batfish/common/topology/L3Adjacencies.java
bazel run //tools/stress_tests:snapshotBddStressTests --jvmopt=-Xmx56g -- /Users/dhalperin/sigcomm23/nets/NET10 multipathConsistency
