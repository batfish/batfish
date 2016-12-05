'''
# This demo operates over the example test rigs in the batfish repository.
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
import logging
from pybatfish.client.commands import *

bf_logger.setLevel(logging.DEBUG)

print "load the testrig"
print bf_init_testrig("test_rigs/example")

print "################"
print "# The configurations are converted to JSON using a vendor-independent data model:"
print bf_answer("nodes", summary=False)

print "# Some checks can be expressed as JsonPath queries on this JSON."
print "# For instance, to check whether the MTU of each interface is 1500,"
print "# we look for all interfaces on all nodes with an MTU that is NOT 1500:"
print bf_answer("nodespath", paths=[{"path":"$.nodes[*].interfaces[*][?(@.mtu != 1500)].mtu", "suffix":True}])

print "#####################"
print "# our logical representation of the network that can be queried in various ways. we have many queries and can write more"

print "# E.g., we can ask questions on adjacencies to ensure that they are as expected (i.e., interfaces are properly configured)"
print bf_answer("neighbors")

print "# we can also ask for protocol-level adjacencies to ensure that protocol sessions are configured correctly"
print bf_answer("neighbors", neighborType=["ebgp"])

print "##############"
print "# we can also do other simple checks based on best practices of the network"

print "# E.g., we can check if all interface ips are unique"
print bf_answer("uniqueipassignments")

print "# Or that all loopbacks are announced within OSPF"
print bf_answer("ospfloopbacks")

print "################"
print "# going deeper, we can ask questions about data flow, i.e., the end-to-end impact of all configuration snippets"

print "# E.g., we can see how host1 reaches a given IP address "
print "# this query will take time if the dataplane has not been comptued before"
print bf_answer("traceroute", ingressNode="host1", dstIp="1.0.2.2")
print "# --> unlike a regular traceroute we show multipath and interface information"

print "# Or, we can ensure that the dns server on host1 (2.128.0.101) is reachable using using protocol-specific traceroutes"
print bf_answer("traceroute", ingressNode="as2border1", dstIp="2.128.0.101", dstPort=53, ipProtocol="UDP")

print "################"
print "# while testing/emulation can do the above, we alone can be comprehensive "

print "# E.g., we can find *all* (starting node, packet header) combinations for which the DNS server is unreachable"
print bf_answer("reachability", dstIps=["2.128.0.101"], dstPorts=[53], ipProtocols=["UDP"], actions=["drop"])
print "# --> the output shows outsiders with spoofed source addresses cannot reach the DNS server (good)"
print "# --> it also shows that a bad ACL on host2 is blocking access (bad)"
print "# we can run a similar query to ensure that the SSH server on host2 is accessible"

print "# But suppose we want to ensure that *only* ssh traffic can reach host2 "
print bf_answer("reachability", actions=["ACCEPT"], dstIps=["2.128.1.101"], notDstPorts=[22], notIpProtocols=["TCP"])
print "# --> all good! hard to guarantee with testing but easy for us"

print "# another example: Ensure that outsiders can *never* reach the SSH server"
print "# we have ACLs on as2's border routers to prevent this. let's test that things are correctly implemented:"
print bf_answer("reachability", actions=["ACCEPT"], dstPorts=[22], dstIps=["2.128.1.101"], ingressNodeRegex="as(1|3)border1", ipProtocols=["TCP"])
print "# --> buggy ACL on as2border2!"

print "#############"
print "# comparing two sets of configs (e.g., current + planned) is a powerful debugging aid"

print "# initialize the delta testrig"
print bf_init_testrig("test_rigs/example-with-delta", doDelta=True)

print "# any query can be executed differentially, to see what changed"
print bf_answer("neighbors", differential=True)

print "# a particularly powerful query: reachability diff between two testrigs"
print bf_answer("reachability", type="reduced")
print "# --> any collateral damage is easy to see"

print "#############"
print "# fault-tolerance can be ensured by studying the impact of failures"

print "# E.g., this command creates a network view after an interface failure"
print bf_init_environment(interfaceBlacklist=[{"hostname" : "as2border2", "interface" : "FastEthernet0/0"}])

print "# we can see if reachability changes at all after this failure"
print bf_answer("reachability", type="reduced")
print "# --> any lack of fault tolerance is easy to see"

print "##############"
print "# finally, sanity checking can be done in the data plane too (e.g., valley-free routing in the DC, number of hops)"

print "# a powerful query: multipath consistency. "
print bf_answer("reachability", type="multipath")
print "# --> will catch hard-to-debug, bad interactions between routing and ACLs"
