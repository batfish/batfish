package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfiguration.AWS_BACKBONE_HOSTNAME;
import static org.batfish.representation.aws.AwsConfiguration.AWS_SERVICES_GATEWAY_NODE_NAME;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTcpFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testBidirectionalTrace;
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
 * E2e tests for VPC endpoints of type gateway. There is a single VPC with two subnets, one public
 * and one private. Both subnets have one instance each.
 *
 * <p>The configuration was pulled * manually after deployment using the setup.tf file in {@link
 * #TESTCONFIGS_DIR}.
 */
public class AwsConfigurationVpcEndpointGatewayTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-vpc-endpoint-gateway";

  private static final List<String> fileNames =
      ImmutableList.of(
          "us-east-1/Addresses.json",
          "us-east-1/AvailabilityZones.json",
          "us-east-1/InternetGateways.json",
          "us-east-1/NetworkAcls.json",
          "us-east-1/NetworkInterfaces.json",
          "us-east-1/PrefixLists.json",
          "us-east-1/Reservations.json",
          "us-east-1/RouteTables.json",
          "us-east-1/SecurityGroups.json",
          "us-east-1/Subnets.json",
          "us-east-1/TransitGatewayAttachments.json",
          "us-east-1/TransitGatewayPropagations.json",
          "us-east-1/TransitGatewayRouteTables.json",
          "us-east-1/TransitGateways.json",
          "us-east-1/TransitGatewayStaticRoutes.json",
          "us-east-1/TransitGatewayVpcAttachments.json",
          "us-east-1/VpcEndpoints.json",
          "us-east-1/VpcPeeringConnections.json",
          "us-east-1/Vpcs.json");

  // various entities in the configs
  private static String _vpc = "vpc-05d56c7295fca139b";
  private static String _vpceGateway = "vpce-0b7357542bb7b83ec";

  private static String _subnetPublic = "subnet-04391f04b912d0a73";
  private static String _subnetPrivate = "subnet-01e5f0945f6d6f47c";

  private static String _instancePublic = "i-0464d440ca0ee9e34";
  private static String _instancePrivate = "i-01c6c0647fdccc89e";

  private static String _igw = "igw-0c8afe4471084ccba";

  // one of the IPs in service prefixes for which we configured the gateway
  private static Ip _configuredServiceIp = Ip.parse("52.216.237.77");

  // one of the IPs in service prefixes for which we configured the gateway
  private static Ip _otherAwsServiceIp = Ip.parse("54.222.57.1");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testFromSubnetPrivate_configuredAwsService() {
    testBidirectionalTrace(
        getTcpFlow(_instancePrivate, _configuredServiceIp, 443, _batfish),
        ImmutableList.of(_instancePrivate, _subnetPrivate, _vpc, _vpceGateway),
        ImmutableList.of(_vpceGateway, _vpc, _subnetPrivate, _instancePrivate),
        _batfish);
  }

  @Test
  public void testFromSubnetPublic_configuredAwsService() {
    testBidirectionalTrace(
        getTcpFlow(_instancePublic, _configuredServiceIp, 443, _batfish),
        ImmutableList.of(_instancePublic, _subnetPublic, _vpc, _vpceGateway),
        ImmutableList.of(_vpceGateway, _vpc, _subnetPublic, _instancePublic),
        _batfish);
  }

  @Test
  public void testFromSubnetPrivate_otherAwsServices() {
    testTrace(
        getTcpFlow(_instancePrivate, _otherAwsServiceIp, 443, _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instancePrivate, _subnetPrivate),
        _batfish);
  }

  @Test
  public void testFromSubnetPublic_otherAwsServices() {
    testBidirectionalTrace(
        getTcpFlow(_instancePublic, _otherAwsServiceIp, 443, _batfish),
        ImmutableList.of(
            _instancePublic,
            _subnetPublic,
            _vpc,
            _igw,
            AWS_BACKBONE_HOSTNAME,
            AWS_SERVICES_GATEWAY_NODE_NAME),
        ImmutableList.of(
            AWS_SERVICES_GATEWAY_NODE_NAME,
            AWS_BACKBONE_HOSTNAME,
            _igw,
            _vpc,
            _subnetPublic,
            _instancePublic),
        _batfish);
  }
}
