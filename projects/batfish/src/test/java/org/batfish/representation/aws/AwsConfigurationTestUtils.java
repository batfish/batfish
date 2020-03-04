package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/** Collection of utilities for AWS e2e tests */
public final class AwsConfigurationTestUtils {

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

  /** Returns the names of the nodes in the trace */
  static List<String> getTraceHops(Trace trace) {
    return trace.getHops().stream()
        .map(h -> h.getNode().getName())
        .collect(ImmutableList.toImmutableList());
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

  /** Tests that the traces has the expected disposition and list of node names. */
  static void testTrace(
      Trace trace, FlowDisposition expectedDisposition, List<String> expectedNodes) {
    assertThat(getTraceHops(trace), equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  /** Tests that the flow has the expected disposition and list of node names. */
  static void testTrace(
      Flow flow,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes,
      IBatfish batfish) {
    List<Trace> traces =
        batfish
            .getTracerouteEngine(batfish.getSnapshot())
            .computeTraces(ImmutableSet.of(flow), false)
            .get(flow);
    testTrace(getOnlyElement(traces.iterator()), expectedDisposition, expectedNodes);
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
}
