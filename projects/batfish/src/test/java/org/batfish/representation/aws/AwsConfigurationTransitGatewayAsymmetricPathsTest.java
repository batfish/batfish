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
 * E2e tests for transit gateway configuration with multiple gateways and asymmetric paths. There
 * are two VPCs, called 'bat' and 'fish', and two TGWs, called 'bat' and 'fish'. Paths from vpc-bat
 * vpc-fish go via tgw-bat, and the other direction uses tgw-fish. To enable such connectivity,
 * vpc-bat associates to a routing table of tgw-bat, does NOT propagate to any routing table on
 * tgw-bat, and propagates to a routing table on tgw-fish. Vpc-fish is analogously configured.
 *
 * <p>The configuration was pulled * manually after deployment using the setup.tf file in {@link
 * #TESTCONFIGS_DIR}.
 */
public class AwsConfigurationTransitGatewayAsymmetricPathsTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-transit-gateway-asymmetric-paths";

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
  private static String _tgwBat = "tgw-01e19888e3ba041ac";
  private static String _tgwFish = "tgw-0a33c739a4f8978a8";

  private static String _vpcBat = "vpc-0c7ecdaddf451b56d";
  private static String _vpcFish = "vpc-0de868624d5f787db";

  private static String _subnetBat = "subnet-0156c57e17070dd17";
  private static String _subnetFish = "subnet-0b7957a706be58c47";

  private static String _instanceBat = "i-04bbbcaefdd1937f9";
  private static String _instanceFish = "i-0973ce90749865a7f";

  private static Ip _instanceBatIp = Ip.parse("10.1.1.100");
  private static Ip _instanceFishIp = Ip.parse("10.2.1.100");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testBatToFish() {
    testTrace(
        getAnyFlow(_instanceBat, _instanceFishIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(
            _instanceBat, _subnetBat, _vpcBat, _tgwBat, _vpcFish, _subnetFish, _instanceFish),
        _batfish);
  }

  @Test
  public void testFishToBat() {
    testTrace(
        getAnyFlow(_instanceFish, _instanceBatIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(
            _instanceFish, _subnetFish, _vpcFish, _tgwFish, _vpcBat, _subnetBat, _instanceBat),
        _batfish);
  }
}
