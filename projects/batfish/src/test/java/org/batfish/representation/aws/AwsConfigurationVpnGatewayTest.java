package org.batfish.representation.aws;

import static org.batfish.common.util.isp.IspModelingUtils.INTERNET_HOST_NAME;
import static org.batfish.representation.aws.AwsConfiguration.AWS_BACKBONE_HOSTNAME;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTcpFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests that connectivity to and from the on-prem network works as expected. The configs have a
 * single private subnet along with a VPN gateway and the customer gateway. The on-prem router
 * config was generated via AWS.
 */
public class AwsConfigurationVpnGatewayTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-vpn-gateway";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "AvailabilityZones.json",
          "CustomerGateways.json",
          "InternetGateways.json",
          "NatGateways.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "VpcEndpoints.json",
          "Vpcs.json",
          "VpnConnections.json",
          "VpnGateways.json");

  private static final String onPremRouterFile = "vpn-03701a53bba3c48b7.txt";

  // various entities in the configs
  private static String _instance = "i-099cf38911942421c";
  private static String _subnet = "subnet-0f263105946ad1a1d";
  private static String _vpc = "vpc-0b966fdeb36d5e43f";
  private static String _vgw = "vgw-0c09bd7fadac961bf";
  private static Ip _privateIp = Ip.parse("10.0.1.204");
  private static Ip _vgwUnderlayIp = Ip.parse("18.217.248.9");
  private static String _onPremRouter = onPremRouterFile; // no hostname in the file, so name = file

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
  public void testFromOnPrem() {
    testTrace(
        getAnyFlow(_onPremRouter, _privateIp, _batfish),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_onPremRouter, _vgw, _vpc, _subnet, _instance),
        _batfish);

    // to a private IP in the subnet
    testTrace(
        getAnyFlow(_onPremRouter, Ip.parse("10.0.1.205"), _batfish),
        FlowDisposition.NEIGHBOR_UNREACHABLE,
        ImmutableList.of(_onPremRouter, _vgw, _vpc, _subnet),
        _batfish);

    // to a private IP outside the subnet
    testTrace(
        getAnyFlow(_onPremRouter, Ip.parse("10.0.2.1"), _batfish),
        FlowDisposition.NULL_ROUTED,
        ImmutableList.of(_onPremRouter, _vgw, _vpc),
        _batfish);
  }

  @Test
  public void testToOnPrem() {
    testTrace(
        getAnyFlow(_instance, Ip.parse("8.8.8.8"), _batfish), // On prem announces default
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instance, _subnet, _vpc, _vgw, _onPremRouter),
        _batfish);
  }

  /** Packets to the underlay interface Ip on VGW should not end up at the VGW */
  @Test
  public void testInstanceToUnderlayInterfaceIp() {
    testTrace(
        getAnyFlow(_instance, _vgwUnderlayIp, _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(_instance, _subnet, _vpc, _vgw, _onPremRouter),
        _batfish);
  }

  /** Underlay interface Ip should be accessible from the Internet */
  @Test
  public void testInternetToUnderlayInterfaceIp() {
    testTrace(
        getTcpFlow(INTERNET_HOST_NAME, Ip.parse("8.8.8.8"), _vgwUnderlayIp, 80),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(INTERNET_HOST_NAME, AWS_BACKBONE_HOSTNAME, _vgw),
        _batfish);
  }
}
