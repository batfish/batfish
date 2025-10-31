package org.batfish.representation.azure;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.rules.TemporaryFolder;

public class AzureConfigurationTestUtils {

  static IBatfish testSetup(String testconfigsDir, List<String> fileNames, TemporaryFolder folder)
      throws IOException {
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setAzureFiles(testconfigsDir, fileNames).build(), folder);
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

  /** Returns the names of the nodes in the trace */
  static List<String> getTraceHops(Trace trace) {
    return trace.getHops().stream()
        .map(h -> h.getNode().getName())
        .collect(ImmutableList.toImmutableList());
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
}
