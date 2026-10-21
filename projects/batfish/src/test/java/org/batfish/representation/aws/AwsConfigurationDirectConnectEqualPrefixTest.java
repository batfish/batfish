package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for a Direct Connect Gateway whose association allows a prefix equal to the attached
 * VPC's CIDR (10.100.0.0/16) alongside a wider summary (10.0.0.0/8). There is no competing VPN
 * path, so on-prem reaches AWS only over the Transit VIF.
 *
 * <p>Path: onprem-router -> DXGW -> TGW -> VPC -> subnet -> instance
 *
 * <p>Regression tests for batfish/batfish#10447: an allowed prefix equal to a TGW-propagated VPC
 * CIDR must forward toward the TGW rather than discard on the DXGW.
 */
public class AwsConfigurationDirectConnectEqualPrefixTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-direct-connect-gateway-equal-prefix";

  private static final List<String> fileNames =
      ImmutableList.of(
          "DirectConnectGatewayAssociations.json",
          "DirectConnectGateways.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "TransitGatewayAttachments.json",
          "TransitGatewayPropagations.json",
          "TransitGatewayRouteTables.json",
          "TransitGatewayVpcAttachments.json",
          "TransitGateways.json",
          "VirtualInterfaces.json",
          "Vpcs.json");

  private static final String onPremRouterFile = "onprem-router.txt";

  private static final String _onPremRouter = "onprem-router";
  private static final String _dxgw =
      DirectConnectGateway.nodeName("9c3d5e7f-5678-4def-8abc-0123456789ab");
  private static final String _tgw = "tgw-dx02";
  private static final String _tgwRouteTable = "tgw-rtb-dx02";
  private static final String _vpc = "vpc-dx02";
  private static final String _subnet = "subnet-dx02";
  private static final String _instance = "i-dx02";
  private static final Ip _instanceIp = Ip.parse("10.100.1.10");
  private static final Ip _dxgwVifIp = Ip.parse("169.254.10.1");
  private static final Prefix _vpcCidr = Prefix.parse("10.100.0.0/16");
  private static final Prefix _summary = Prefix.parse("10.0.0.0/8");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsFiles(TESTCONFIGS_DIR, fileNames)
                .setConfigurationFiles(TESTCONFIGS_DIR, onPremRouterFile)
                .build(),
            _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  /**
   * The allowed prefix equals the VPC CIDR the TGW propagates to the DXGW. Traffic from on-prem
   * must reach the instance through the DXGW and TGW rather than be discarded on the DXGW.
   */
  @Test
  public void testFromOnPremToInstance() {
    testTrace(
        getAnyFlow(_onPremRouter, _instanceIp, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_onPremRouter, _dxgw, _tgw, _vpc, _subnet, _instance),
        _batfish);
  }

  @Test
  public void testFromInstanceToOnPrem() {
    testTrace(
        getAnyFlow(_instance, Ip.parse("10.10.0.1"), _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instance, _subnet, _vpc, _tgw, _dxgw, _onPremRouter),
        _batfish);
  }

  /** Both allowed prefixes are advertised to on-prem by the DXGW, as AWS does. */
  @Test
  public void testOnPremReceivesAllowedPrefixes() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());
    for (Prefix prefix : ImmutableList.of(_vpcCidr, _summary)) {
      List<AbstractRoute> routes = mainRibRoutes(dp, _onPremRouter, "default", prefix);
      assertThat(prefix.toString(), routes, hasSize(1));
      AbstractRoute route = routes.get(0);
      assertThat(route, instanceOf(Bgpv4Route.class));
      assertThat(route.getNextHopIp(), equalTo(_dxgwVifIp));
    }
  }

  /**
   * On the DXGW, each allowed prefix is a static route toward the TGW. The TGW also advertises the
   * VPC CIDR over BGP; the static must not shadow it with a discard.
   */
  @Test
  public void testDxgwRoutesAllowedPrefixesTowardTgw() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());
    String ifaceToTgw = Utils.interfaceNameToRemote(_tgw, _tgwRouteTable);
    for (Prefix prefix : ImmutableList.of(_vpcCidr, _summary)) {
      List<AbstractRoute> routes = mainRibRoutes(dp, _dxgw, "default", prefix);
      assertThat(prefix.toString(), routes, hasSize(1));
      AbstractRoute route = routes.get(0);
      assertThat(route, instanceOf(StaticRoute.class));
      assertThat(route.getNextHop(), instanceOf(NextHopInterface.class));
      assertThat(((NextHopInterface) route.getNextHop()).getInterfaceName(), equalTo(ifaceToTgw));
    }
  }

  /**
   * A destination inside the allowed summary with no route on the TGW is forwarded to the TGW and
   * dropped there, as in AWS, rather than discarded on the DXGW.
   */
  @Test
  public void testUnroutedDestinationInsideSummaryDropsAtTgw() {
    testTrace(
        getAnyFlow(_onPremRouter, Ip.parse("10.200.1.1"), _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_onPremRouter, _dxgw, _tgw),
        _batfish);
  }

  private static List<AbstractRoute> mainRibRoutes(
      DataPlane dp, String node, String vrf, Prefix prefix) {
    return dp.getRibs().get(node, vrf).getRoutes().stream()
        .filter(r -> r.getNetwork().equals(prefix))
        .collect(Collectors.toList());
  }
}
