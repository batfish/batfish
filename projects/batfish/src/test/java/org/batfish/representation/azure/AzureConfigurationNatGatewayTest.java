package org.batfish.representation.azure;

import static org.batfish.representation.azure.AzureConfigurationTestUtils.testBidirectionalTrace;
import static org.batfish.representation.azure.AzureConfigurationTestUtils.testSetup;
import static org.batfish.representation.azure.AzureConfigurationTestUtils.testTrace;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AzureConfigurationNatGatewayTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/azure/test-nat-gateway";

  private static final List<String> fileNames =
      ImmutableList.of(
          "nat-gateway-subnet1.json",
          "vnet.json",
          "vm1.json",
          "vm1346.json",
          "public-ip-address-1.json",
          "vm2.json",
          "vm235.json",
          "public-ip-address-2.json");

  private static final String NAT_GATEWAY_SUBNET_1 =
      "resourcegroup_test_providers_microsoft.network_natgateways_nat-gateway-subnet1";
  private static final String DEFAULT_NAT_GATEWAY_SUBNET_2 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_private_internet-gateway";
  private static final String ISP = "isp_8075";
  private static final String INTERNET = "internet";
  private static final String SUBNET_1 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_default";
  private static final String SUBNET_2 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_private";
  private static final String VM_1 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm1";
  private static final Ip VM_1_PRIVATE_IP = Ip.parse("10.0.0.4");
  private static final String VM_2 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm2";
  // private static final Ip VM_2_PRIVATE_IP = Ip.parse("10.0.1.5");
  private static final Ip VM_2_PUBLIC_IP = Ip.parse("80.41.19.20");
  private static final Ip DNS_CLOUDFLARE_IP = Ip.parse("1.1.1.1");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  // VM1 is using the subnet assigned Nat Gateway, with an applied public ip
  // therefore any traffic leaving VM1 towards Internet will use subnet 1's Nat Gateway public IP

  // VM2 has its own public ip address, while its subnet has no nat gateway applied
  // therefore any traffic leaving VM2 towards Internet will use its own public ip address

  @Test
  public void testVm1ToVm2() {
    testBidirectionalTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_PRIVATE_IP)
            .setDstIp(VM_2_PUBLIC_IP)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1025)
            .setDstPort(80)
            .build(),
        ImmutableList.of(
            VM_1,
            SUBNET_1,
            NAT_GATEWAY_SUBNET_1,
            ISP,
            DEFAULT_NAT_GATEWAY_SUBNET_2,
            SUBNET_2,
            VM_2),
        ImmutableList.of(
            VM_2,
            SUBNET_2,
            DEFAULT_NAT_GATEWAY_SUBNET_2,
            ISP,
            NAT_GATEWAY_SUBNET_1,
            SUBNET_1,
            VM_1),
        _batfish);
  }

  @Test
  public void testVm1ToVm2_uni() {
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_PRIVATE_IP)
            .setDstIp(VM_2_PUBLIC_IP)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1025)
            .setDstPort(80)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(
            VM_1,
            SUBNET_1,
            NAT_GATEWAY_SUBNET_1,
            ISP,
            DEFAULT_NAT_GATEWAY_SUBNET_2,
            SUBNET_2,
            VM_2),
        _batfish);
  }

  @Test
  public void testVm1ToCloudflare() {
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_PRIVATE_IP)
            .setDstIp(DNS_CLOUDFLARE_IP)
            .setIpProtocol(IpProtocol.UDP)
            .setSrcPort(1025)
            .setDstPort(53)
            .build(),
        // the cloudflare node do not really exist in this test
        // but if the packet goes to Internet then it's okay
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(VM_1, SUBNET_1, NAT_GATEWAY_SUBNET_1, ISP, INTERNET),
        _batfish);
  }
}
