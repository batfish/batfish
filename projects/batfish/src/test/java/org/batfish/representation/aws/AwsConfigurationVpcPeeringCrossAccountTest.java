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

public class AwsConfigurationVpcPeeringCrossAccountTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-vpc-peering-multi-account";

  private static final List<String> fileNames =
      ImmutableList.of(
          "accounts/123456789012/us-east-1/NetworkInterfaces.json",
          "accounts/123456789012/us-east-1/Reservations.json",
          "accounts/123456789012/us-east-1/RouteTables.json",
          "accounts/123456789012/us-east-1/SecurityGroups.json",
          "accounts/123456789012/us-east-1/Subnets.json",
          "accounts/123456789012/us-east-1/VpcPeeringConnections.json",
          "accounts/123456789012/us-east-1/Vpcs.json",
          "accounts/123456789013/us-east-1/NetworkInterfaces.json",
          "accounts/123456789013/us-east-1/Reservations.json",
          "accounts/123456789013/us-east-1/RouteTables.json",
          "accounts/123456789013/us-east-1/SecurityGroups.json",
          "accounts/123456789013/us-east-1/Subnets.json",
          "accounts/123456789013/us-east-1/VpcPeeringConnections.json",
          "accounts/123456789013/us-east-1/Vpcs.json");

  // various entities in the configs
  private static String _vpcA = "vpc-0f44fae3468d2b998";
  private static String _vpcB = "vpc-05951c91977125d2e";

  private static String _subnetA = "subnet-03a80d08017ee39ef";
  private static String _subnetB = "subnet-088d6408a3a6129fa";

  private static String _instanceA = "i-07ae69cc808e8804e";
  private static String _instanceB = "i-066ce0b1efbb5602c";

  private static Ip _instanceAIp = Ip.parse("10.1.1.100");
  private static Ip _instanceBIp = Ip.parse("10.2.1.252");

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
        ImmutableList.of(_instanceA, _subnetA, _vpcA, _vpcB, _subnetB, _instanceB),
        _batfish);
  }

  /** Test connectivity from B to A */
  @Test
  public void testFromBtoA() {
    testTrace(
        getTcpFlow(_instanceB, _instanceAIp, NamedPort.SSH.number(), _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instanceB, _subnetB, _vpcB, _vpcA, _subnetA, _instanceA),
        _batfish);
  }
}
