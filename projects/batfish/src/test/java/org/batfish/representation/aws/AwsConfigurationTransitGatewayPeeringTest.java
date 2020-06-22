package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for transit gateway peering. The test deployment is spread across two accounts, called
 * bat and fish, and two regions. Each account has a TGW, and the two TGWs peer. The bat account has
 * two VPCs, called bat and bat2, and the fish account has one VPC, called fish. Each VPC attaches
 * to its local TGW and has one subnet and one instance.
 *
 * <p>The routing configuration, described below, includes functioning connectivity as well as
 * potential errors.
 *
 * <ul>
 *   <li>VPC bat associates with the default routing table on TGW bat that does NOT have a route to
 *       VPC fish's prefix. So, VPC bat cannot reach VPC fish, and flows should stop at this table.
 *   <li>VPC bat2 associates with routing table bat2 (not default) on TGW bat that has a route to
 *       VPC fish's prefix. So, VPC bat2 can reach VPC fish.
 *   <li>VPC bat2 does NOT propagate its routes to the default routing table on TGW bat. Now,
 *       because the peering attachment associates to this table, VPC fish cannot reach VPC bat2.
 * </ul>
 *
 * <p>The configuration was pulled after deployment using the setup.tf file in {@link
 * #TESTCONFIGS_DIR}.
 */
public class AwsConfigurationTransitGatewayPeeringTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-transit-gateway-peering";

  private static final List<String> fileNames =
      ImmutableList.of(
          "accounts/732168539940/us-east-1/Addresses.json",
          "accounts/732168539940/us-east-1/NetworkAcls.json",
          "accounts/732168539940/us-east-1/NetworkInterfaces.json",
          "accounts/732168539940/us-east-1/Reservations.json",
          "accounts/732168539940/us-east-1/RouteTables.json",
          "accounts/732168539940/us-east-1/SecurityGroups.json",
          "accounts/732168539940/us-east-1/Subnets.json",
          "accounts/732168539940/us-east-1/TransitGatewayAttachments.json",
          "accounts/732168539940/us-east-1/TransitGatewayPropagations.json",
          "accounts/732168539940/us-east-1/TransitGatewayRouteTables.json",
          "accounts/732168539940/us-east-1/TransitGateways.json",
          "accounts/732168539940/us-east-1/TransitGatewayStaticRoutes.json",
          "accounts/732168539940/us-east-1/TransitGatewayVpcAttachments.json",
          "accounts/732168539940/us-east-1/VpcPeeringConnections.json",
          "accounts/732168539940/us-east-1/Vpcs.json",
          "accounts/804819755326/us-west-1/Addresses.json",
          "accounts/804819755326/us-west-1/NetworkAcls.json",
          "accounts/804819755326/us-west-1/NetworkInterfaces.json",
          "accounts/804819755326/us-west-1/Reservations.json",
          "accounts/804819755326/us-west-1/RouteTables.json",
          "accounts/804819755326/us-west-1/SecurityGroups.json",
          "accounts/804819755326/us-west-1/Subnets.json",
          "accounts/804819755326/us-west-1/TransitGatewayAttachments.json",
          "accounts/804819755326/us-west-1/TransitGatewayPropagations.json",
          "accounts/804819755326/us-west-1/TransitGatewayRouteTables.json",
          "accounts/804819755326/us-west-1/TransitGateways.json",
          "accounts/804819755326/us-west-1/TransitGatewayStaticRoutes.json",
          "accounts/804819755326/us-west-1/TransitGatewayVpcAttachments.json",
          "accounts/804819755326/us-west-1/VpcPeeringConnections.json",
          "accounts/804819755326/us-west-1/Vpcs.json");

  // various entities in the configs
  private static String _tgwBat = "tgw-0e20010ebf43d07a2";
  private static String _tgwFish = "tgw-03201f1960ddb0c25";

  private static String _vpcBat = "vpc-065ea457071dd67bb";
  private static String _vpcBat2 = "vpc-03ccec9d7758a283a";
  private static String _vpcFish = "vpc-0ecbcbd110fd839d1";

  private static String _subnetBat = "subnet-06818d8a511df9b3d";
  private static String _subnetBat2 = "subnet-0934a46c6eb5542d9";
  private static String _subnetFish = "subnet-0080bd39fe3626e89";

  private static String _instanceBat = "i-08f3abb6471637c66";
  private static String _instanceBat2 = "i-05e25e43074216229";
  private static String _instanceFish = "i-0afb0a59f6bf36397";

  private static Ip _instanceBatIp = Ip.parse("10.10.1.100");
  private static Ip _instanceBat2Ip = Ip.parse("10.20.1.100");
  private static Ip _instanceFishIp = Ip.parse("192.168.1.100");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  /**
   * Traces from instance bat to fish must stop at the TGW bat because of the missing static route
   * to VPC fish's prefix.
   */
  @Test
  public void testBatToFish() {
    testTrace(
        getAnyFlow(_instanceBat, _instanceFishIp, _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instanceBat, _subnetBat, _vpcBat, _tgwBat),
        _batfish);
  }

  /**
   * Traces from instance bat2 to fish must reach the instance fish (but denied because of security
   * groups).
   */
  @Test
  public void testBat2ToFish() {
    testTrace(
        getAnyFlow(_instanceBat2, _instanceFishIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(
            _instanceBat2,
            _subnetBat2,
            _vpcBat2,
            _tgwBat,
            _tgwFish,
            _vpcFish,
            _subnetFish,
            _instanceFish),
        _batfish);
  }

  /**
   * Traces from instance fish to bat must stop at the TGW fish because of the missing static route.
   */
  @Test
  public void testFishToBat() {
    testTrace(
        getAnyFlow(_instanceFish, _instanceBatIp, _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instanceFish, _subnetFish, _vpcFish, _tgwFish),
        _batfish);
  }

  /**
   * Traces from instance fish to bat2 must stop at the TGW bat (after crossing the peering link)
   * because of the missing propagation from bat2 to the routing table associated with the peering
   * attachment.
   */
  @Test
  public void testFishToBat2() {
    testTrace(
        getAnyFlow(_instanceFish, _instanceBat2Ip, _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instanceFish, _subnetFish, _vpcFish, _tgwFish, _tgwBat),
        _batfish);
  }
}
