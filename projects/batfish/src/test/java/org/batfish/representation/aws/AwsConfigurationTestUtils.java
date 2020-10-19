package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.rules.TemporaryFolder;

/** Collection of utilities for AWS e2e tests */
public final class AwsConfigurationTestUtils {

  static IBatfish testSetup(String testconfigsDir, List<String> fileNames, TemporaryFolder folder)
      throws IOException {
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setAwsFiles(testconfigsDir, fileNames).build(), folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  /**
   * Returns some concrete IP address of the node.
   *
   * @throws IllegalArgumentException if no such address is found
   */
  static Ip getAnyNodeIp(String nodeName, IBatfish batfish) {
    return batfish
        .loadConfigurations(batfish.getSnapshot())
        .get(nodeName)
        .getAllInterfaces()
        .values()
        .stream()
        .flatMap(iface -> iface.getAllConcreteAddresses().stream())
        .findAny()
        .map(ConcreteInterfaceAddress::getIp)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Could not find an interface with a concrete address on " + nodeName));
  }

  /** Returns the IP address of the node. Assumes that the node has only one interface */
  static Ip getOnlyNodeIp(String nodeName, IBatfish batfish) {
    return getOnlyElement(
            batfish
                .loadConfigurations(batfish.getSnapshot())
                .get(nodeName)
                .getAllInterfaces()
                .values()
                .iterator())
        .getConcreteAddress()
        .getIp();
  }

  /** Returns the IP address of the given interface on the given node. */
  static Ip getInterfaceIp(String nodeName, String ifaceName, IBatfish batfish) {
    return batfish
        .loadConfigurations(batfish.getSnapshot())
        .get(nodeName)
        .getAllInterfaces()
        .get(ifaceName)
        .getConcreteAddress()
        .getIp();
  }

  /** Returns the names of the nodes in the trace */
  static List<String> getTraceHops(Trace trace) {
    return trace.getHops().stream()
        .map(h -> h.getNode().getName())
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a flow with the specified ingress node and dst Ip. Source ip is picked from one of the
   * ingress node IPs
   */
  static Flow getAnyFlow(String ingressNode, Ip dstIp, IBatfish batfish) {
    return Flow.builder()
        .setIngressNode(ingressNode)
        .setSrcIp(getAnyNodeIp(ingressNode, batfish))
        .setDstIp(dstIp)
        .build();
  }

  /**
   * Returns a TCP flow with the specified parameters. Source IP is set to the (assumed to be the
   * only) IP on the ingress node. Source port is picked from the ephemeral range.
   */
  static Flow getTcpFlow(String ingressNode, Ip dstIp, int dstPort, IBatfish batfish) {
    return Flow.builder()
        .setIngressNode(ingressNode)
        .setSrcIp(getOnlyNodeIp(ingressNode, batfish))
        .setDstIp(dstIp)
        .setIpProtocol(IpProtocol.TCP)
        .setDstPort(dstPort)
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .build();
  }

  /**
   * Returns a TCP flow with the specified parameters. Source port is picked from the ephemeral
   * range.
   */
  static Flow getTcpFlow(String ingressNode, Ip srcIp, Ip dstIp, int dstPort) {
    return Flow.builder()
        .setIngressNode(ingressNode)
        .setSrcIp(srcIp)
        .setDstIp(dstIp)
        .setIpProtocol(IpProtocol.TCP)
        .setDstPort(dstPort)
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .build();
  }

  static List<Trace> getTraces(Flow flow, IBatfish batfish) {
    return batfish
        .getTracerouteEngine(batfish.getSnapshot())
        .computeTraces(ImmutableSet.of(flow), false)
        .get(flow);
  }

  /** Tests that the traces has the expected disposition and list of node names. */
  static void testTrace(
      Trace trace, FlowDisposition expectedDisposition, List<String> expectedNodes) {
    assertThat(getTraceHops(trace), equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  /**
   * Tests that the flow has the expected disposition and list of node names. Assumes that there is
   * only one trace expected and returns that trace
   */
  static Trace testTrace(
      Flow flow,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes,
      IBatfish batfish) {
    Trace trace =
        getOnlyElement(
            batfish
                .getTracerouteEngine(batfish.getSnapshot())
                .computeTraces(ImmutableSet.of(flow), false)
                .get(flow)
                .iterator());
    testTrace(trace, expectedDisposition, expectedNodes);
    return trace;
  }

  /**
   * Tests that the flow has the expected list of nodes names in the both forward and reverse
   * directions and list of node names. The disposition of the forward trace must be ACCEPTED (for
   * the reverse flow to be generated).
   */
  static void testBidirectionalTrace(
      Flow flow,
      List<String> expectedForwardPath,
      List<String> expectedReversePath,
      IBatfish batfish) {
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(batfish.getSnapshot());
    List<TraceAndReverseFlow> forwardTraces =
        tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);

    TraceAndReverseFlow forwardTrace = getOnlyElement(forwardTraces.iterator());
    assertThat(getTraceHops(forwardTrace.getTrace()), equalTo(expectedForwardPath));
    assertThat(
        forwardTrace.getTrace().getDisposition(),
        anyOf(equalTo(FlowDisposition.ACCEPTED), equalTo(FlowDisposition.EXITS_NETWORK)));

    List<TraceAndReverseFlow> reverseTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(
                ImmutableSet.of(forwardTrace.getReverseFlow()),
                forwardTrace.getNewFirewallSessions(),
                false)
            .get(forwardTrace.getReverseFlow());

    Trace reverseTrace = getOnlyElement(reverseTraces.iterator()).getTrace();
    testTrace(reverseTrace, FlowDisposition.ACCEPTED, expectedReversePath);
  }

  static Subnet getTestSubnet(Prefix cidrblock, String subnetId, String vpcId) {
    return getTestSubnet(cidrblock, subnetId, vpcId, "zone");
  }

  static Subnet getTestSubnet(
      Prefix cidrblock, String subnetId, String vpcId, String availabilityZone) {
    return new Subnet(
        cidrblock, "ownerId", "subnetArn", subnetId, vpcId, availabilityZone, ImmutableMap.of());
  }

  static Vpc getTestVpc(String vpcId) {
    return getTestVpc(vpcId, ImmutableSet.of());
  }

  static Vpc getTestVpc(String vpcId, Set<Prefix> cidrBlockAssociations) {
    return new Vpc("owner", vpcId, cidrBlockAssociations, ImmutableMap.of());
  }
}
