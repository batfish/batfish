package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for transit gateway. The configuration was pulled after creating two VPCs A and B and
 * connecting them via a transit gateway. VPC A has two subnets in different availability zones,
 * only one of which is connected to the VPC's attachment to the gateway. VPC B has only subnet,
 * which is connected to the attachment.
 */
public class AwsConfigurationTransitGatewayTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-transit-gateway";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "AvailabilityZones.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "TransitGatewayAttachments.json",
          "TransitGatewayPropagations.json",
          "TransitGatewayRouteTables.json",
          "TransitGateways.json",
          "TransitGatewayStaticRoutes.json",
          "TransitGatewayVpcAttachments.json",
          "VpcPeeringConnections.json",
          "Vpcs.json");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  // various entities in the configs
  private static String _tgw = "tgw-044be4464fcc69aff";
  private static String _vpcA = "vpc-0404e08ceddf7f650";
  private static String _vpcB = "vpc-00a31ce9d0c06675c";

  private static String _subnetA = "subnet-006a19c846f047bd7";
  private static String _subnetA2 = "subnet-0b5b8ddd5a69fcfcd"; // not connected to the gateway
  private static String _subnetB = "subnet-0ebf6378a79a3e534";

  private static String _instanceA = "i-06ba034d88c84ef07";
  private static String _instanceA2 = "i-075e846ed41670385";
  private static String _instanceB = "i-0b14080af811fda3d";

  private static Ip _instanceAIp = Ip.parse("10.10.10.157");
  private static Ip _instanceA2Ip = Ip.parse("10.10.20.154");
  private static Ip _instanceBIp = Ip.parse("192.168.1.106");

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setAwsText(TESTCONFIGS_DIR, fileNames).build(), _folder);
    _batfish.computeDataPlane();
  }

  private static void testTrace(
      String ingressNode,
      Ip dstIp,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes) {
    SortedMap<Flow, List<Trace>> traces = getTraces(ingressNode, dstIp);
    Flow flow = getOnlyElement(traces.keySet().iterator());
    Trace flowTrace = getOnlyElement(traces.get(flow).iterator());
    testTrace(flowTrace, expectedDisposition, expectedNodes);
  }

  private static SortedMap<Flow, List<Trace>> getTraces(String ingressNode, Ip dstIp) {
    Flow flow =
        Flow.builder()
            .setTag("test")
            .setIngressNode(ingressNode)
            .setDstIp(dstIp) // this public IP does not exists in the network
            .build();
    return _batfish.getTracerouteEngine().computeTraces(ImmutableSet.of(flow), false);
  }

  private static void testTrace(
      Trace trace, FlowDisposition expectedDisposition, List<String> expectedNodes) {
    assertThat(
        trace.getHops().stream()
            .map(h -> h.getNode().getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  /** Test connectivity from A to B */
  @Test
  public void testFromAtoB() {
    testTrace(
        _instanceA,
        _instanceBIp,
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceA, _subnetA, _vpcA, _tgw, _vpcB, _subnetB, _instanceB));
  }

  /** Test connectivity from B to A */
  @Test
  public void testFromBtoA() {
    testTrace(
        _instanceB,
        _instanceAIp,
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _tgw, _vpcA, _subnetA, _instanceA));
  }

  /** Test connectivity from A2 to B -- should die at the subnet router */
  @Test
  public void testFromA2toB() {
    testTrace(
        _instanceA2,
        _instanceBIp,
        FlowDisposition.NULL_ROUTED,
        ImmutableList.of(_instanceA2, _subnetA2));
  }

  /** Test connectivity from B to A2 -- should die at the VPC */
  @Test
  public void testFromBtoA2() {
    testTrace(
        _instanceB,
        _instanceA2Ip,
        FlowDisposition.NULL_ROUTED,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _tgw, _vpcA));
  }
}
