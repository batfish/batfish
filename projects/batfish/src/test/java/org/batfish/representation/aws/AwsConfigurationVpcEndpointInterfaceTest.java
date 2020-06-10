package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTcpFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testBidirectionalTrace;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.batfish.representation.aws.VpcEndpointInterface.getNodeId;

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
 * E2e tests for VPC endpoints of type interface. There is a single VPC with two subnets. Both
 * subnets have one instance each, and one of the subnets has the endpoint interface. The security
 * group for the endpoint allows port 443, which we use in tests below.
 *
 * <p>The configuration was pulled * manually after deployment using the setup.tf file in {@link
 * #TESTCONFIGS_DIR}.
 */
public class AwsConfigurationVpcEndpointInterfaceTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-vpc-endpoint-interface";

  private static final List<String> fileNames =
      ImmutableList.of(
          "us-east-1/Addresses.json",
          "us-east-1/AvailabilityZones.json",
          "us-east-1/NetworkAcls.json",
          "us-east-1/NetworkInterfaces.json",
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
  private static String _vpc = "vpc-072d45b801aeffef2";
  private static String _vpcEndpointId = "vpce-083c683a6be29b889";

  private static String _subnetNotEndpoint = "subnet-0611b848137c513d1";
  private static String _subnetEndpoint = "subnet-0bb8615d8368ece31";

  // instance we create for the endpoint
  private static String _instanceEndpoint = getNodeId(_subnetEndpoint, _vpcEndpointId);

  private static String _instanceSubnetNotEndpoint = "i-0eb6eb395a0d09117";
  private static String _instanceSubnetEndpoint = "i-0a0fbd0cfa774970f";

  private static Ip _endpointIp = Ip.parse("10.1.101.97");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testFromSubnetEndpoint() {
    testBidirectionalTrace(
        getTcpFlow(_instanceSubnetEndpoint, _endpointIp, 443, _batfish),
        ImmutableList.of(_instanceSubnetEndpoint, _instanceEndpoint),
        ImmutableList.of(_instanceEndpoint, _instanceSubnetEndpoint),
        _batfish);
  }

  @Test
  public void testFromSubnetNonEndpoint() {
    testBidirectionalTrace(
        getTcpFlow(_instanceSubnetNotEndpoint, _endpointIp, 443, _batfish),
        ImmutableList.of(
            _instanceSubnetNotEndpoint,
            _subnetNotEndpoint,
            _vpc,
            _subnetEndpoint,
            _instanceEndpoint),
        ImmutableList.of(
            _instanceEndpoint,
            _subnetEndpoint,
            _vpc,
            _subnetNotEndpoint,
            _instanceSubnetNotEndpoint),
        _batfish);
  }
}
