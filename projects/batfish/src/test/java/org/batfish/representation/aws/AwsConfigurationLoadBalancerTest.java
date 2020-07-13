package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getInterfaceIp;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTcpFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testBidirectionalTrace;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
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
  private static String _nlb1Iface = "eni-01f261dccaba46564";
  private static String _nlb2Iface = "eni-0c82d6f7254ae6695";

  private static int _listenerPort = 80;

  private static IBatfish _batfish;

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
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
    Ip nlb1Ip = getInterfaceIp(_nodeLoadBalancer1, _nlb1Iface, _batfish);
    Ip nlb2Ip = getInterfaceIp(_nodeLoadBalancer2, _nlb2Iface, _batfish);
    testBidirectionalTrace(
        getTcpFlow(_instanceClient, nlb1Ip, _listenerPort, _batfish), // to first LB IP
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
            _instanceClient),
        _batfish);
    testBidirectionalTrace(
        getTcpFlow(_instanceClient, nlb2Ip, _listenerPort, _batfish), // to second LB IP
        ImmutableList.of(
            _instanceClient,
            _subnetClient,
            _vpc,
            _subnetS2,
            _nodeLoadBalancer2,
            _subnetS2,
            _instanceS2),
        ImmutableList.of(
            _instanceS2,
            _subnetS2,
            _nodeLoadBalancer2,
            _subnetS2,
            _vpc,
            _subnetClient,
            _instanceClient),
        _batfish);
  }

  @Test
  public void testClientToServerReachability() {
    testReachability(_instanceClient, _instanceS1);
    testReachability(_instanceClient, _instanceS2);
  }

  /** The client sends a packet to the load balancer on a non-listened port. */
  @Test
  public void testClientToLb_nonListenedPort() {
    Ip nlb1Ip = getInterfaceIp(_nodeLoadBalancer1, _nlb1Iface, _batfish);
    Ip nlb2Ip = getInterfaceIp(_nodeLoadBalancer2, _nlb2Iface, _batfish);
    testTrace(
        getTcpFlow(_instanceClient, nlb1Ip, _listenerPort + 1, _batfish), // to first LB IP
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceClient, _subnetClient, _vpc, _subnetS1, _nodeLoadBalancer1),
        _batfish);
    testTrace(
        getTcpFlow(_instanceClient, nlb2Ip, _listenerPort + 1, _batfish), // to second LB IP
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceClient, _subnetClient, _vpc, _subnetS2, _nodeLoadBalancer2),
        _batfish);
  }
}
