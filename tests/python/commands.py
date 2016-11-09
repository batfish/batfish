'''
# This test file operates over the example test rigs in the batfish repository.
# It assumes that it runs from the top-level directory in the repository.

# To get relevant shell commands, do:
## % source tools/batfish_function.sh

# The client runs against the batfish service, hosted locally or on a remote server. 
# To start the service locally, do the following in a new shell: 
## % source tools/batfish_functions.sh
## % allinone -runmode interactive
# Then run the client:
## % batfish_pyclient -c demo-python/commands -s <server>
## replace <server> with service location (e.g., "localhost" or "www.batfish.org") 
'''
from org.batfish.client.commands import *
import os.path

def test(referenceFilename, testOutput):
	testPassed = False
	try: 
		if (os.path.isfile(referenceFilename)):
			referenceFile = file(referenceFilename)
			referenceOutput = referenceFile.read()

			if (referenceOutput == testOutput):
				testPassed = True

		if (not testPassed):
			testOutputFilename = referenceFilename + ".testout"
			testOutputFile = open(testOutputFilename, "w")
			testOutputFile.write(testOutput)

	except Error as e:
		print "Test " + referenceFilename + " encountered exception. " + str(e)

	print "Test " + referenceFilename + " passed? " + str(testPassed)
	print

test("tests/python/init.ref", bf_init_testrig("test_rigs/example"))
test("tests/python/init-delta.ref", bf_init_testrig("test_rigs/example-with-delta", doDelta=True))
test("tests/python/genDp.ref", bf_generate_dataplane())
test("tests/python/genDp-delta.ref", bf_generate_dataplane(doDelta=True))
test("tests/python/aclReachability.ref", bf_answer_type("aclreachability"))
test("tests/python/assert.ref", bf_answer_type("assert", assertions=[{"assertion":"(eq 15 (pathsize '$.nodes[*]'))"},{"assertion":"(eq 0 (pathsize '$.nodes[\"as1border\"]'))"},{"assertion":"(not (eq 0 (pathsize '$.nodes[\"as1border1\"]')))"}, {"assertion":"(eq (pathsize '$.nodes[*].aaaSettings.newModel') (pathsize '$.nodes[*].aaaSettings[?(@.newModel == true)]'))"}]))
test("tests/python/bgpSessionCheck.ref", bf_answer_type("bgpsessioncheck"))
test("tests/python/compareSameName.ref", bf_answer_type("comparesamename"))
test("tests/python/error.ref", bf_answer_type("error"))
test("tests/python/ipsecVpnCheck.ref", bf_answer_type("ipsecvpncheck"))
test("tests/python/isisLoopbacks.ref", bf_answer_type("isisloopbacks"))
test("tests/python/neighbors.ref", bf_answer_type("neighbors"))
test("tests/python/neighbors-ebgp.ref", bf_answer_type("neighbors", neighborType=["ebgp"]))
test("tests/python/nodes-summary.ref", bf_answer_type("nodes", summary=True))
test("tests/python/nodes.ref", bf_answer_type("nodes", summary=False))
test("tests/python/ospfLoopbacks.ref", bf_answer_type("ospfloopbacks"))
test("tests/python/pairwiseVpnConnectivity.ref", bf_answer_type("pairwisevpnconnectivity"))
test("tests/python/routes.ref", bf_answer_type("routes"))
test("tests/python/routes-diff.ref", bf_answer_type("routes", differential=True))
test("tests/python/selfAdjacencies.ref", bf_answer_type("selfadjacencies"))
test("tests/python/traceroute-1-2.ref", bf_answer_type("traceroute", ingressNode="as1core1", dstIp="2.128.0.101"))
test("tests/python/traceroute-2-1.ref", bf_answer_type("traceroute", ingressNode="host2", dstIp="1.0.1.1"))
test("tests/python/multipath-host1.ref", bf_answer_type("reachability", type="multipath", ingressNodeRegex="host1", srcIps=["2.128.0.0"], dstIps=["3.0.1.2"], ipProtocols=["TCP"], srcPorts=[0], dstPorts=[0]))
test("tests/python/multipath-host2.ref", bf_answer_type("reachability", type="multipath", ingressNodeRegex="host2", srcIps=["2.128.0.0"], dstIps=["1.0.1.1"], ipProtocols=["UDP"], srcPorts=[0], dstPorts=[0]))
test("tests/python/undefinedReferences.ref", bf_answer_type("undefinedreferences"))
test("tests/python/uniqueIpAssignments.ref", bf_answer_type("uniqueipassignments"))
test("tests/python/unusedStructures.ref", bf_answer_type("unusedstructures"))
