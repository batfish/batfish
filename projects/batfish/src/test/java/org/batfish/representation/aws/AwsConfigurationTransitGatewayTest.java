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
 * E2e tests for transit gateway. The configuration was pulled after creating two VPCs A and B and
 * connecting them via a transit gateway. VPC A has two subnets in different availability zones; the
 * routing table of only of those points to the transit gateway attachment. VPC B has only one
 * subnet, which is connected to the attachment.
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

  // various entities in the configs
  private static String _tgw = "tgw-044be4464fcc69aff";
  private static String _vpcA = "vpc-0404e08ceddf7f650";
  private static String _vpcB = "vpc-00a31ce9d0c06675c";

  private static String _subnetA = "subnet-006a19c846f047bd7";
  private static String _subnetA2 = "subnet-0b5b8ddd5a69fcfcd"; // does not point to the gateway
  private static String _subnetB = "subnet-0ebf6378a79a3e534";

  private static String _instanceA = "i-06ba034d88c84ef07";
  private static String _instanceA2 = "i-075e846ed41670385";
  private static String _instanceB = "i-0b14080af811fda3d";

  private static Ip _instanceAIp = Ip.parse("10.10.10.157");
  private static Ip _instanceA2Ip = Ip.parse("10.10.20.154");
  private static Ip _instanceBIp = Ip.parse("192.168.1.106");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  /** Test connectivity from A to B */
  @Test
  public void testFromAtoB() {
    testTrace(
        getAnyFlow(_instanceA, _instanceBIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceA, _subnetA, _vpcA, _tgw, _vpcB, _subnetB, _instanceB),
        _batfish);
  }

  /** Test connectivity from B to A */
  @Test
  public void testFromBtoA() {
    testTrace(
        getAnyFlow(_instanceB, _instanceAIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _tgw, _vpcA, _subnetA, _instanceA),
        _batfish);
  }

  /** Test connectivity from A2 to B -- should die at the subnet router */
  @Test
  public void testFromA2toB() {
    testTrace(
        getAnyFlow(_instanceA2, _instanceBIp, _batfish),
        FlowDisposition.NULL_ROUTED,
        ImmutableList.of(_instanceA2, _subnetA2),
        _batfish);
  }

  /**
   * Test connectivity from B to A2 -- should get to A2. A2 not pointing to the TGW only impacts
   * outgoing traffic, not incoming traffic.
   */
  @Test
  public void testFromBtoA2() {
    testTrace(
        getAnyFlow(_instanceB, _instanceA2Ip, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _tgw, _vpcA, _subnetA2, _instanceA2),
        _batfish);
  }
}
