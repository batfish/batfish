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
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for AWS Direct Connect Gateway modeling. The topology has an on-prem router connected
 * via a Transit VIF to a Direct Connect Gateway, which is attached to a Transit Gateway with a VPC.
 *
 * <p>Path: onprem-router -> DXGW -> TGW -> VPC -> subnet -> instance
 */
public class AwsConfigurationDirectConnectTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-direct-connect-gateway";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "CustomerGateways.json",
          "DirectConnectGatewayAssociations.json",
          "DirectConnectGateways.json",
          "InternetGateways.json",
          "NatGateways.json",
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
          "VirtualInterfaces.json",
          "VpcEndpoints.json",
          "Vpcs.json",
          "VpnConnections.json",
          "VpnGateways.json");

  private static final String onPremRouterFile = "onprem-router.txt";

  private static final String _onPremRouter = "onprem-router";
  private static final String _dxgw = "dxgw-dx01";
  private static final String _tgw = "tgw-dx01";
  private static final String _vpc = "vpc-dx01";
  private static final String _subnet = "subnet-dx01";
  private static final String _instance = "i-dx01";
  private static final Ip _instanceIp = Ip.parse("10.100.1.10");

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

  @Test
  public void testFromOnPremToInstance() {
    // Instance is at 10.100.1.10, in VPC 10.100.0.0/16. The on-prem router learns the VPC CIDR
    // via the VPN tunnel BGP session (TGW advertises VPC routes via the VPN attachment) and the
    // allowed-prefix summary 10.0.0.0/8 via the Direct Connect VIF. Longest-match selects the
    // VPN route, so traffic from on-prem to the VPC takes the VPN path. (Production deployments
    // typically use BGP communities on AWS to influence on-prem preference; that is out of scope
    // for this snapshot.) Traffic still reaches the instance and is accepted.
    testTrace(
        getAnyFlow(_onPremRouter, _instanceIp, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_onPremRouter, _tgw, _vpc, _subnet, _instance),
        _batfish);
  }

  /**
   * Verify AWS's documented TGW route preference: when both DX and VPN attachments propagate the
   * same prefix into the same TGW route table, traffic from inside AWS takes the DX path.
   *
   * <p>The snapshot includes a VPN attachment (vpn-dx-test) associated with tgw-rtb-dx01 alongside
   * the DX attachment. The on-prem router peers BGP with both the DXGW (via the Direct Connect VIF)
   * and the TGW (via the IPsec VPN tunnel), advertising 10.10.0.0/16 over both.
   *
   * <p>Mechanism: routes received on the TGW's DX BGP peer get tagged with {@link
   * Route#DIRECT_CONNECT_LOCAL_PREFERENCE} (200), higher than the default local-pref (100) used for
   * VPN-propagated BGP routes. BGP best-path selection on the TGW then picks the DX-tagged route
   * over the VPN-propagated route. The trace from the VPC instance toward 10.10.0.1 traverses the
   * DXGW node, not the TGW's VPN tunnel.
   */
  @Test
  public void testFromInstanceToOnPrem() {
    testTrace(
        getAnyFlow(_instance, Ip.parse("10.10.0.1"), _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instance, _subnet, _vpc, _tgw, _dxgw, _onPremRouter),
        _batfish);
  }

  /**
   * Verify that the DXGW only receives VPC CIDRs propagated to the TGW route table associated with
   * the DX attachment. The snapshot has two VPCs and two TGW route tables: vpc-dx01 propagates to
   * tgw-rtb-dx01 (the DX RT) and vpc-iso01 propagates only to tgw-rtb-iso01. The DXGW should know
   * about 10.100.0.0/16 (vpc-dx01) but NOT about 10.200.0.0/16 (vpc-iso01).
   */
  @Test
  public void testDxgwOnlySeesVpcsInDxRouteTable() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());
    Set<Prefix> dxgwRoutes =
        dp.getRibs().get(_dxgw, "default").getRoutes().stream()
            .map(AbstractRoute::getNetwork)
            .collect(Collectors.toSet());
    assertThat(
        "DXGW should have a route to vpc-dx01's CIDR (propagated to the DX route table)",
        dxgwRoutes,
        org.hamcrest.Matchers.hasItem(Prefix.parse("10.100.0.0/16")));
    assertThat(
        "DXGW must NOT have a route to vpc-iso01's CIDR (propagated only to a different "
            + "TGW route table)",
        dxgwRoutes,
        org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(Prefix.parse("10.200.0.0/16"))));
  }

  /**
   * Verify that BGP routes received on the TGW from a DX peer are tagged with the elevated DX
   * local-preference. This is the mechanism that ensures DX wins over VPN-propagated routes (which
   * use default local-pref 100) for the same prefix in TGW route selection.
   */
  @Test
  public void testTgwReceivesDxRoutesWithDxLocalPreference() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());
    String tgwVrfName = TransitGateway.vrfNameForRouteTable("tgw-rtb-dx01");
    Set<AbstractRoute> tgwRoutes =
        dp.getRibs().get(_tgw, tgwVrfName).getRoutes().stream()
            .filter(r -> r.getNetwork().equals(Prefix.parse("10.10.0.0/16")))
            .collect(Collectors.toSet());
    assertThat(tgwRoutes, hasSize(1));
    AbstractRoute route = tgwRoutes.iterator().next();
    assertThat("DX-propagated route should be a BGP route", route, instanceOf(Bgpv4Route.class));
    Bgpv4Route bgpRoute = (Bgpv4Route) route;
    assertThat(
        "BGP route propagated from DXGW should carry the DX local-preference",
        bgpRoute.getLocalPreference(),
        equalTo(Route.DIRECT_CONNECT_LOCAL_PREFERENCE));
  }
}
