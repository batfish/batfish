package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTcpFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NamedPort;
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
public class AwsConfigurationTransitGatewayMultiAccountTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-transit-gateway-multi-account";

  private static final List<String> fileNames =
      ImmutableList.of(
          "accounts/028403472736/us-east-1/NetworkInterfaces.json",
          "accounts/028403472736/us-east-1/Reservations.json",
          "accounts/028403472736/us-east-1/RouteTables.json",
          "accounts/028403472736/us-east-1/SecurityGroups.json",
          "accounts/028403472736/us-east-1/Subnets.json",
          "accounts/028403472736/us-east-1/TransitGatewayAttachments.json",
          "accounts/028403472736/us-east-1/TransitGatewayPropagations.json",
          "accounts/028403472736/us-east-1/TransitGatewayRouteTables.json",
          "accounts/028403472736/us-east-1/TransitGateways.json",
          "accounts/028403472736/us-east-1/TransitGatewayStaticRoutes.json",
          "accounts/028403472736/us-east-1/TransitGatewayVpcAttachments.json",
          "accounts/028403472736/us-east-1/Vpcs.json",
          "accounts/951601349076/us-east-1/NetworkInterfaces.json",
          "accounts/951601349076/us-east-1/Reservations.json",
          "accounts/951601349076/us-east-1/RouteTables.json",
          "accounts/951601349076/us-east-1/SecurityGroups.json",
          "accounts/951601349076/us-east-1/Subnets.json",
          "accounts/951601349076/us-east-1/TransitGatewayAttachments.json",
          "accounts/951601349076/us-east-1/TransitGatewayPropagations.json",
          "accounts/951601349076/us-east-1/TransitGatewayRouteTables.json",
          "accounts/951601349076/us-east-1/TransitGateways.json",
          "accounts/951601349076/us-east-1/TransitGatewayStaticRoutes.json",
          "accounts/951601349076/us-east-1/TransitGatewayVpcAttachments.json",
          "accounts/951601349076/us-east-1/Vpcs.json");

  // various entities in the configs
  private static String _tgw = "tgw-0efdeab79e9a8e490";
  private static String _vpcA = "vpc-04048d4bfba9c2289";
  private static String _vpcB = "vpc-08287a9084472b276";

  private static String _subnetA = "subnet-0a68915f31d3ca69f";
  private static String _subnetB = "subnet-0c2cb08833b3c9921";

  private static String _instanceA = "i-0bdf35d561be3d962";
  private static String _instanceB = "i-03df168b3f73cc262";

  private static Ip _instanceAIp = Ip.parse("10.1.1.100");
  private static Ip _instanceBIp = Ip.parse("10.2.1.183");

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
        getTcpFlow(_instanceA, _instanceBIp, NamedPort.SSH.number(), _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instanceA, _subnetA, _vpcA, _tgw, _vpcB, _subnetB, _instanceB),
        _batfish);
  }

  /** Test connectivity from B to A */
  @Test
  public void testFromBtoA() {
    testTrace(
        getTcpFlow(_instanceB, _instanceAIp, NamedPort.SSH.number(), _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _tgw, _vpcA, _subnetA, _instanceA),
        _batfish);
  }
}
