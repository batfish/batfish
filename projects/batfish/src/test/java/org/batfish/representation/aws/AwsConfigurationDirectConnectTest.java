package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
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
    // On-prem router advertises 10.0.0.0/8 to DXGW via BGP.
    // Instance is at 10.100.1.10, which is in VPC 10.100.0.0/16.
    // TGW propagates VPC CIDR, DXGW propagates it to on-prem via BGP.
    // Traffic from on-prem should reach the instance (security group allows all).
    testTrace(
        getAnyFlow(_onPremRouter, _instanceIp, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_onPremRouter, _dxgw, _tgw, _vpc, _subnet, _instance),
        _batfish);
  }

  @Test
  public void testFromInstanceToOnPrem() {
    // Instance sends traffic to 10.10.0.1 (on-prem loopback).
    // Subnet route table sends 0.0.0.0/0 to TGW. TGW learned 10.10.0.0/16 from
    // DXGW via BGP (originated by on-prem) and forwards toward on-prem.
    testTrace(
        getAnyFlow(_instance, Ip.parse("10.10.0.1"), _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instance, _subnet, _vpc, _tgw, _dxgw, _onPremRouter),
        _batfish);
  }

  /**
   * Verify that BGP routes received on the TGW from a DX peer are tagged with the DX-propagated
   * admin distance. This is the mechanism that ensures DX wins over VPN-propagated routes (which
   * use the default BGP admin distance) for the same prefix in TGW route selection.
   */
  @Test
  public void testTgwReceivesDxRoutesWithDxAdminDistance() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());
    String tgwVrfName = TransitGateway.vrfNameForRouteTable("tgw-rtb-dx01");
    Set<AbstractRoute> tgwRoutes =
        dp.getRibs().get(_tgw, tgwVrfName).getRoutes().stream()
            .filter(r -> r.getNetwork().equals(Prefix.parse("10.10.0.0/16")))
            .collect(Collectors.toSet());
    assertThat(tgwRoutes, hasSize(1));
    AbstractRoute route = tgwRoutes.iterator().next();
    assertThat(
        "BGP route propagated from DXGW should carry DX-propagated admin distance",
        route.getAdministrativeCost(),
        equalTo(Route.DIRECT_CONNECT_PROPAGATED_ROUTE_ADMIN));
  }
}
