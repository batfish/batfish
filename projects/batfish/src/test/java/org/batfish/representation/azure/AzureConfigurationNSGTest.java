package org.batfish.representation.azure;

import static org.batfish.representation.azure.AzureConfigurationTestUtils.testSetup;
import static org.batfish.representation.azure.AzureConfigurationTestUtils.testTrace;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AzureConfigurationNSGTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/azure/test-nsg";

  private static final List<String> fileNames =
      ImmutableList.of(
          "vnet.json",
          "vm1.json",
          "vm2.json",
          "vm3.json",
          "vm1346.json",
          "vm235.json",
          "vm345.json",
          "nsg-vm2.json",
          "nsg-subnet2.json");

  // various entities in the configs
  // private static final String NAT_GATEWAY =
  // "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_private_internet-gateway";
  // private static final String ISP = "isp_8075";
  private static final String VNET =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet";
  private static final String SUBNET_1 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_default";
  private static final String SUBNET_2 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_private";
  private static final String VM_1 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm1";
  private static final Ip VM_1_IP = Ip.parse("10.0.0.4");
  // private static final String VM_1_IFACE = "vm1346";
  private static final String VM_2 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm2";
  private static final Ip VM_2_IP = Ip.parse("10.0.1.5");
  // private static final String VM_2_IFACE = "vm235";
  private static final String VM_3 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm3";
  private static final Ip VM_3_IP = Ip.parse("10.0.1.6");
  // private static final String VM_3_IFACE = "vm345";
  private static final Ip CLOUDFLARE_DNS_IP = Ip.parse("1.1.1.1");
  // private static final Ip ANOTHER_VNET_IP = Ip.parse("10.2.0.6");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testHostToHost() {

    // ping from VM1 to VM2
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setDstIp(VM_2_IP)
            .setSrcIp(VM_1_IP)
            .setIpProtocol(IpProtocol.ICMP)
            .setIcmpType(IcmpType.ECHO_REQUEST)
            .setIcmpCode(0)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, VM_2),
        _batfish);

    // ping from VM2 to VM1
    testTrace(
        Flow.builder()
            .setIngressNode(VM_2)
            .setSrcIp(VM_2_IP)
            .setDstIp(VM_1_IP)
            .setIpProtocol(IpProtocol.ICMP)
            .setIcmpType(IcmpType.ECHO_REQUEST)
            .setIcmpCode(0)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_2, SUBNET_2, VNET, SUBNET_1, VM_1),
        _batfish);
  }

  @Test
  public void testVm1toVm2SSH() {

    // ssh from VM1 to VM2
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setDstIp(VM_2_IP)
            .setDstPort(22)
            .setSrcPort(1025)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, VM_2),
        _batfish);
  }

  @Test
  public void testVm1toVm2Http() {

    // http from VM1 to VM2
    testTrace(
        Flow.builder()
            .setDstIp(VM_2_IP)
            .setIngressNode(VM_1)
            .setDstPort(8080)
            .setSrcPort(1025)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, VM_2),
        _batfish);
  }

  @Test
  public void testIntraSubnetFiltering() {

    // http from VM3 to VM2 (same subnet but nsg-subnet2.json should apply and deny)
    testTrace(
        Flow.builder()
            .setDstIp(VM_2_IP)
            .setIngressNode(VM_3)
            .setDstPort(22)
            .setSrcPort(1025)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(VM_3, SUBNET_2),
        _batfish);

    // http from VM2 to VM3 (same subnet but nsg-subnet2.json should apply and deny)
    testTrace(
        Flow.builder()
            .setDstIp(VM_3_IP)
            .setIngressNode(VM_2)
            .setDstPort(22)
            .setSrcPort(1025)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(VM_2, SUBNET_2),
        _batfish);
  }

  @Test
  public void testVm2toCloudflare() {

    // VM2 to cloudflare dns server
    // expected to fail because security rule "BlockCloudFlareDns" is expected to deny it.
    testTrace(
        Flow.builder()
            .setDstIp(CLOUDFLARE_DNS_IP)
            .setIngressNode(VM_2)
            .setDstPort(53)
            .setSrcPort(1025)
            .setIpProtocol(IpProtocol.UDP)
            .build(),
        FlowDisposition.DENIED_IN,
        ImmutableList.of(VM_2, SUBNET_2),
        _batfish);
  }

  // should be denied once SERVICE_TAGS are fully supported
  /*
  @Test
  public void testAnotherVnet() {
      // VM2 to an ip belonging to another VNet
      // expected to fail because security rule "DenyAllOutbound" (nsg-subnet2) should apply and deny it.
      testTrace(
              Flow.builder()
                      .setDstIp(ANOTHER_VNET_IP)
                      .setSrcIp(VM_2_IP)
                      .setIngressNode(VM_2)
                      .setDstPort(22)
                      .setSrcPort(1025)
                      .setIpProtocol(IpProtocol.TCP)
                      .build(),
              FlowDisposition.DENIED_IN,
              ImmutableList.of(VM_2, SUBNET_2),
              _batfish
      );
  }*/
}
