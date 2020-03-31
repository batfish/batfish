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
 * E2e tests for VPC peering. The configuration was pulled after creating three VPCs, A, B, and C.
 * VPC A has CIDR block 10.10.0.0/16, and B and C have the same block 192.168.0.0/16 (which helps
 * test proper routing). Each VPC has a subnet, and each subnet has an instance. The subnets of VPC
 * B and C have different /24s. VPC A peers with B and C. The route table at A's subnet is
 * configured to point appropriately point B and C subnets' /24 to the right peering connections.
 * The route table at B and C's subnet points the A's entire block to the peering connection.
 *
 * <p>Since B and C and symmetrically configured, most tests below ignore C. Its role in this setup
 * is to create potential for interference and misrouting.
 */
public class AwsConfigurationVpcPeeringTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-vpc-peering";

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
          "VpcPeeringConnections.json",
          "Vpcs.json");

  // various entities in the configs
  private static String _vpcA = "vpc-0404e08ceddf7f650";
  private static String _vpcB = "vpc-00a31ce9d0c06675c";

  private static String _subnetA = "subnet-006a19c846f047bd7";
  private static String _subnetB = "subnet-0ebf6378a79a3e534";

  private static String _instanceA = "i-06ba034d88c84ef07";
  private static String _instanceB = "i-0b14080af811fda3d";

  private static Ip _instanceAIp = Ip.parse("10.10.10.157");
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
        ImmutableList.of(_instanceA, _subnetA, _vpcA, _vpcB, _subnetB, _instanceB),
        _batfish);
  }

  /** Test connectivity from B to A */
  @Test
  public void testFromBtoA() {
    testTrace(
        getAnyFlow(_instanceB, _instanceAIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _vpcA, _subnetA, _instanceA),
        _batfish);
  }

  /**
   * Test connectivity from A to an unused subnet in the /16 that B and C share. Such packets should
   * be dropped at the source subnet itself.
   */
  @Test
  public void testFromAtoUnusedSubnet() {
    testTrace(
        getAnyFlow(_instanceA, Ip.parse("192.168.100.1"), _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instanceA, _subnetA),
        _batfish);
  }

  /**
   * Test connectivity from A to an unused subnet in A. Such packets should make it to A's VPC
   * router.
   */
  @Test
  public void testFromBtoUnusedSubnet() {
    testTrace(
        getAnyFlow(_instanceB, Ip.parse("10.10.100.1"), _batfish),
        FlowDisposition.NULL_ROUTED,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _vpcA),
        _batfish);
  }

  /**
   * Test connectivity from A to an unused Ip in A's subnet. Such packets should make it to A's
   * subnet router.
   */
  @Test
  public void testFromBtoUnusedIp() {
    testTrace(
        getAnyFlow(_instanceB, Ip.parse("10.10.10.11"), _batfish),
        FlowDisposition.NEIGHBOR_UNREACHABLE,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _vpcA, _subnetA),
        _batfish);
  }
}
