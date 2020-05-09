package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTcpFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testBidirectionalTrace;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Ip;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for VPC Privates of type interface. There is a single VPC with two subnets, one public
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

  // one of the IPs in service prefixes
  private static Ip _serviceIp = Ip.parse("52.216.237.77");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testFromSubnetPrivate() {
    testBidirectionalTrace(
        getTcpFlow(_instancePrivate, _serviceIp, 443, _batfish),
        ImmutableList.of(_instancePrivate, _subnetPrivate, _vpc, _vpceGateway),
        ImmutableList.of(_vpceGateway, _vpc, _subnetPrivate, _instancePrivate),
        _batfish);
  }

  @Test
  public void testFromSubnetPublic() {
    testBidirectionalTrace(
        getTcpFlow(_instancePublic, _serviceIp, 443, _batfish),
        ImmutableList.of(_instancePublic, _subnetPublic, _vpc, _vpceGateway),
        ImmutableList.of(_vpceGateway, _vpc, _subnetPublic, _instancePublic),
        _batfish);
  }
}
