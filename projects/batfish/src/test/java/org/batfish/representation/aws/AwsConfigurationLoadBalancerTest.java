package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
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
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NullLocationSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests of load balancing behavior. The snapshot was created using the terraform definition
 * file that is checked into {@link #TESTCONFIGS_DIR}.
 *
 * <p>The snapshot has two subnets, with one instance each. The load balancer is enabled in both
 * subnets and sprays packets coming in on TCP port 80 to port 22. Cross-zone load balancing is
 * enabled. The load balancer is "internal" and there is also another subnet with a "jump host" that
 * serves as a client in the tests below.
 *
 * <p>The snapshot also has some elements such as an Internet gateway that are useful for testing on
 * AWS but not relevant here.
 */
public class AwsConfigurationLoadBalancerTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-load-balancer";

  private static final List<String> fileNames =
      ImmutableList.of(
          "LoadBalancerAttributes.json",
          "LoadBalancerListeners.json",
          "LoadBalancers.json",
          "LoadBalancerTargetHealth.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "PrefixLists.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "TargetGroups.json",
          "Vpcs.json");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  private static TracerouteEngine _tracerouteEngine;

  // various entities in the configs
  private static String _vpc = "vpc-015b20578b48c1349";
  private static String _instanceS1 = "i-055b0db7be365202c"; // first server
  private static String _subnetS1 = "subnet-090fe152cd18ced20";
  private static String _instanceS2 = "i-0f4af951d61a19245"; // second server
  private static String _subnetS2 = "subnet-0873d71031a560a88";
  private static String _instanceClient = "i-0c8ce8bbdd98cc4e8"; // jump host
  private static String _subnetClient = "subnet-065a3676df7fa9fd9";

  private static String _nodeLoadBalancer1 =
      LoadBalancer.getNodeId("test-lb-725b996cb6be8fba.elb.us-east-2.amazonaws.com", "us-east-2b");
  private static String _nodeLoadBalancer2 =
      LoadBalancer.getNodeId("test-lb-725b996cb6be8fba.elb.us-east-2.amazonaws.com", "us-east-2a");

  private static int _listenerPort = 80;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setAwsText(TESTCONFIGS_DIR, fileNames).build(), _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
    _tracerouteEngine = _batfish.getTracerouteEngine(_batfish.getSnapshot());
  }

  /** Returns the IP address of the node. Assumes that the node has only one interface */
  private static Ip getNodeIp(String nodeName) {
    return getOnlyElement(
            _batfish
                .loadConfigurations(_batfish.getSnapshot())
                .get(nodeName)
                .getAllInterfaces()
                .values()
                .iterator())
        .getConcreteAddress()
        .getIp();
  }

  private static Flow getTcpFlow(String ingressNode, Ip dstIp, int dstPort) {
    return Flow.builder()
        .setIngressNode(ingressNode)
        .setSrcIp(getNodeIp(ingressNode))
        .setDstIp(dstIp)
        .setIpProtocol(IpProtocol.TCP)
        .setDstPort(dstPort)
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .build();
  }

  private static List<String> getTraceHops(Trace trace) {
    return trace.getHops().stream()
        .map(h -> h.getNode().getName())
        .collect(ImmutableList.toImmutableList());
  }

  private static void testTrace(
      Trace trace, FlowDisposition expectedDisposition, List<String> expectedNodes) {
    assertThat(getTraceHops(trace), equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  private static void testUnidirectionalTrace(
      Flow flow, FlowDisposition expectedDisposition, List<String> expectedPath) {
    List<Trace> traces = _tracerouteEngine.computeTraces(ImmutableSet.of(flow), false).get(flow);
    testTrace(getOnlyElement(traces.iterator()), expectedDisposition, expectedPath);
  }

  private static void testBidirectionalTrace(
      Flow flow, List<String> expectedForwardPath, List<String> expectedReversePath) {
    List<TraceAndReverseFlow> forwardTraces =
        _tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);

    TraceAndReverseFlow forwardTrace = getOnlyElement(forwardTraces.iterator());
    testTrace(forwardTrace.getTrace(), FlowDisposition.ACCEPTED, expectedForwardPath);

    List<TraceAndReverseFlow> reverseTraces =
        _tracerouteEngine
            .computeTracesAndReverseFlows(
                ImmutableSet.of(forwardTrace.getReverseFlow()),
                forwardTrace.getNewFirewallSessions(),
                false)
            .get(forwardTrace.getReverseFlow());

    Trace reverseTrace = getOnlyElement(reverseTraces.iterator()).getTrace();
    testTrace(reverseTrace, FlowDisposition.ACCEPTED, expectedReversePath);
  }

  /** Tests that startNode can reach endNode */
  private static void testReachability(String startNode, String endNode) {
    LocationSpecifier startLocationSpecifier =
        SpecifierFactories.getLocationSpecifierOrDefault(startNode, NullLocationSpecifier.INSTANCE);
    NodeSpecifier endLocationSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(endNode, NoNodesNodeSpecifier.INSTANCE);

    // double check that specifiers were precise
    assertThat(
        startLocationSpecifier.resolve(_batfish.specifierContext(_batfish.getSnapshot())),
        hasSize(1));
    assertThat(
        endLocationSpecifier.resolve(_batfish.specifierContext(_batfish.getSnapshot())),
        hasSize(1));

    ReachabilityParameters parameters =
        ReachabilityParameters.builder()
            .setSourceLocationSpecifier(startLocationSpecifier)
            .setFinalNodesSpecifier(endLocationSpecifier)
            .setActions(ImmutableSortedSet.of(FlowDisposition.ACCEPTED))
            .build();

    TraceWrapperAsAnswerElement traceAnswer =
        (TraceWrapperAsAnswerElement) _batfish.standard(_batfish.getSnapshot(), parameters);

    // getting a flow means that there is reachability
    assertFalse(traceAnswer.getFlowTraces().isEmpty());
  }

  /** The client sends a packet to the load balancer on the listened port. Test both directions. */
  @Test
  public void testClientToLbTrace() {
    // traces could have gone to either server, but because of deterministic processing of
    // transformations server2 is hit. reachability test below tests that server1 can also be hit.
    testBidirectionalTrace(
        getTcpFlow(_instanceClient, getNodeIp(_nodeLoadBalancer1), _listenerPort), // to first LB IP
        ImmutableList.of(
            _instanceClient,
            _subnetClient,
            _vpc,
            _subnetS1,
            _nodeLoadBalancer1,
            _subnetS1,
            _vpc,
            _subnetS2,
            _instanceS2),
        ImmutableList.of(
            _instanceS2,
            _subnetS2,
            _vpc,
            _subnetS1,
            _nodeLoadBalancer1,
            _subnetS1,
            _vpc,
            _subnetClient,
            _instanceClient));
    testBidirectionalTrace(
        getTcpFlow(
            _instanceClient, getNodeIp(_nodeLoadBalancer2), _listenerPort), // to second LB IP
        ImmutableList.of(
            _instanceClient, _subnetClient, _vpc, _subnetS2, _nodeLoadBalancer2, _instanceS2),
        ImmutableList.of(
            _instanceS2, _nodeLoadBalancer2, _subnetS2, _vpc, _subnetClient, _instanceClient));
  }

  @Test
  public void testClientToServerReachability() {
    testReachability(_instanceClient, _instanceS1);
    testReachability(_instanceClient, _instanceS2);
  }

  /** The client sends a packet to the load balancer on a non-listened port. */
  @Test
  public void testClientToLb_nonListenedPort() {
    testUnidirectionalTrace(
        getTcpFlow(
            _instanceClient, getNodeIp(_nodeLoadBalancer1), _listenerPort + 1), // to first LB IP
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceClient, _subnetClient, _vpc, _subnetS1, _nodeLoadBalancer1));
    testUnidirectionalTrace(
        getTcpFlow(
            _instanceClient, getNodeIp(_nodeLoadBalancer2), _listenerPort + 1), // to second LB IP
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceClient, _subnetClient, _vpc, _subnetS2, _nodeLoadBalancer2));
  }
}
