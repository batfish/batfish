package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.common.util.IspModelingUtils.INTERNET_HOST_NAME;
import static org.batfish.common.util.IspModelingUtils.INTERNET_OUT_ADDRESS;
import static org.batfish.representation.aws.InternetGateway.AWS_BACKBONE_NODE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests of NAT behavior. The snapshot was created using the terraform definition file that is
 * checked into {@link #TESTCONFIGS_DIR}.
 *
 * <p>TODO: description of the test
 */
public class AwsConfigurationNatGatewayTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-nat-gateway";

  private static final List<String> fileNames =
      ImmutableList.of(
          "InternetGateways.json",
          "NatGateways.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "Vpcs.json");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  private static TracerouteEngine _tracerouteEngine;

  // various entities in the configs
  private static String _vpc = "vpc-0008a7b45e3ddf1dd";
  private static String _natGateway = "nat-07ab4846da51f4612";
  private static String _subnetNat = "subnet-0428892a357fa1f94";
  private static Ip _publicIpNat = Ip.parse("3.135.127.225");
  private static String _internetGateway = "igw-071753b9c23d8a9b2";

  private static String _instanceS1 = "i-0a128d26e59be60f3"; // private subnet
  private static String _subnetS1 = "subnet-06f469bcee42e408e";

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

  private static Flow getTcpFlow(String ingressNode, Ip dstIp) {
    return Flow.builder()
        .setIngressNode(ingressNode)
        .setSrcIp(getNodeIp(ingressNode))
        .setDstIp(dstIp)
        .setIpProtocol(IpProtocol.TCP)
        .setDstPort(80)
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

  @Test
  public void testInstanceToInternet_bidirectional() {
    Flow flow = getTcpFlow(_instanceS1, INTERNET_OUT_ADDRESS);
    testBidirectionalTrace(
        flow,
        ImmutableList.of(
            _instanceS1,
            _subnetS1,
            _vpc,
            _natGateway,
            _subnetNat,
            _internetGateway,
            AWS_BACKBONE_NODE_NAME,
            INTERNET_HOST_NAME),
        ImmutableList.of(
            INTERNET_HOST_NAME,
            AWS_BACKBONE_NODE_NAME,
            _internetGateway,
            _subnetNat,
            _natGateway,
            _subnetNat,
            _internetGateway,
            AWS_BACKBONE_NODE_NAME,
            INTERNET_HOST_NAME));
  }

  /** Test that packets for unsupported IP protocols are dropped at the NAT */
  @Test
  public void testUnsupportedIpProtocol() {
    Flow flow =
        Flow.builder()
            .setIngressNode(_instanceS1)
            .setSrcIp(getNodeIp(_instanceS1))
            .setDstIp(INTERNET_OUT_ADDRESS)
            .setIpProtocol(IpProtocol.AN)
            .build();
    testUnidirectionalTrace(
        flow,
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceS1, _subnetS1, _vpc, _natGateway));
  }

  /** Test that packets that come into the NAT without an installed session are dropped */
  @Test
  public void testNonSessionPacket() {
    Flow flow =
        Flow.builder()
            .setIngressNode(INTERNET_HOST_NAME)
            .setSrcIp(INTERNET_OUT_ADDRESS)
            .setDstIp(_publicIpNat)
            .setIpProtocol(IpProtocol.TCP)
            .setDstPort(80)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .build();
    testUnidirectionalTrace(
        flow,
        FlowDisposition.DENIED_IN,
        ImmutableList.of(
            INTERNET_HOST_NAME, AWS_BACKBONE_NODE_NAME, _internetGateway, _subnetNat, _natGateway));
  }
}
