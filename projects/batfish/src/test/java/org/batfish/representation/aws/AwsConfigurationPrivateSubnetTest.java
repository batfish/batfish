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
 * E2e tests that connectivity to and from the on-prem network works as expected. The configs have a
 * single private subnet along with a VPN gateway and the customer gateway. The on-prem router
 * config was generated via AWS.
 */
public class AwsConfigurationPrivateSubnetTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-private-subnet";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "AvailabilityZones.json",
          "CustomerGateways.json",
          "InternetGateways.json",
          "NatGateways.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "VpcEndpoints.json",
          "Vpcs.json",
          "VpnConnections.json",
          "VpnGateways.json");

  private static final String onPremRouterFile = "vpn-03701a53bba3c48b7.txt";

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  // various entities in the configs
  private static String _instance = "i-099cf38911942421c";
  private static String _subnet = "subnet-0f263105946ad1a1d";
  private static String _vgw = "vgw-0c09bd7fadac961bf";
  private static Ip _privateIp = Ip.parse("10.0.1.204");
  private static String _onPremRouter = onPremRouterFile; // no hostname in the file, so name = file

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsText(TESTCONFIGS_DIR, fileNames)
                .setConfigurationText(TESTCONFIGS_DIR, onPremRouterFile)
                .build(),
            _folder);
    _batfish.computeDataPlane();
  }

  private static void testTrace(
      String ingressNode,
      Ip dstIp,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes) {
    Flow flow =
        Flow.builder()
            .setTag("test")
            .setIngressNode(ingressNode)
            .setDstIp(dstIp) // this public IP does not exists in the network
            .build();
    SortedMap<Flow, List<Trace>> traces =
        _batfish.getTracerouteEngine().computeTraces(ImmutableSet.of(flow), false);

    Trace trace = getOnlyElement(traces.get(flow).iterator());

    assertThat(
        trace.getHops().stream()
            .map(h -> h.getNode().getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  @Test
  public void testFromOnPrem() {
    testTrace(
        _onPremRouter,
        _privateIp,
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_onPremRouter, _vgw, _subnet, _instance));

    // to a private IP in the subnet
    testTrace(
        _onPremRouter,
        Ip.parse("10.0.1.205"),
        FlowDisposition.DELIVERED_TO_SUBNET,
        ImmutableList.of(_onPremRouter, _vgw, _subnet));

    // to a private IP outside the subnet
    testTrace(
        _onPremRouter,
        Ip.parse("10.0.2.1"),
        FlowDisposition.NULL_ROUTED,
        ImmutableList.of(_onPremRouter, _vgw));
  }

  @Test
  public void testToOnPrem() {
    testTrace(
        _instance,
        Ip.parse("8.8.8.8"), // On prem announces default
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instance, _subnet, _vgw, _onPremRouter));
  }
}
