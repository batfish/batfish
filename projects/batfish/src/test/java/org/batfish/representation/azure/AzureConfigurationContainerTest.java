package org.batfish.representation.azure;

import static org.batfish.representation.azure.AzureConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.azure.AzureConfigurationTestUtils.getAnyNodeIp;
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

public class AzureConfigurationContainerTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/azure/test-container";

  private static final List<String> fileNames =
      ImmutableList.of("vnet.json", "container.json", "vm1.json", "vm1346.json");

  private static final String VNET =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet";
  private static final String SUBNET_1 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_default";
  private static final String SUBNET_2 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_private";
  private static final String VM_1 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm1";
  private static final Ip VM_1_IP = Ip.parse("10.0.0.4");
  private static final String CONTAINER_GROUP =
      "resourcegroups_test_providers_microsoft.containerinstance_containergroups_alpha";
  private static final Ip CONTAINER_GROUP_IP = Ip.parse("10.0.1.4");
  private static final String CONTAINER_INSTANCE_1 =
      "resourcegroups_test_providers_microsoft.containerinstance_containergroups_alpha_test1";
  private static final String CONTAINER_INSTANCE_2 =
      "resourcegroups_test_providers_microsoft.containerinstance_containergroups_alpha_test2";

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testContainerToContainer() {
    // IPs are assigned dynamically so we must fetch these.
    Ip containerInstance1IP = getAnyNodeIp(CONTAINER_INSTANCE_1, _batfish);
    Ip containerInstance2IP = getAnyNodeIp(CONTAINER_INSTANCE_2, _batfish);

    testTrace(
        getAnyFlow(CONTAINER_INSTANCE_1, containerInstance2IP, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(CONTAINER_INSTANCE_1, CONTAINER_INSTANCE_2),
        _batfish);

    testTrace(
        getAnyFlow(CONTAINER_INSTANCE_2, containerInstance1IP, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(CONTAINER_INSTANCE_2, CONTAINER_INSTANCE_1),
        _batfish);
  }

  @Test
  public void testSNatContainerToVm1() {
    Ip containerInstance1IP = getAnyNodeIp(CONTAINER_INSTANCE_1, _batfish);

    testBidirectionalTrace(
        Flow.builder()
            .setIngressNode(CONTAINER_INSTANCE_1)
            .setSrcIp(containerInstance1IP)
            .setDstIp(VM_1_IP)
            .setSrcPort(1025)
            .setDstPort(80)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        ImmutableList.of(CONTAINER_INSTANCE_1, CONTAINER_GROUP, SUBNET_2, VNET, SUBNET_1, VM_1),
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, CONTAINER_GROUP, CONTAINER_INSTANCE_1),
        _batfish);
  }

  @Test
  public void testVm1toContainer1() {
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_IP)
            .setDstIp(CONTAINER_GROUP_IP)
            .setSrcPort(1025)
            .setDstPort(443)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, CONTAINER_GROUP, CONTAINER_INSTANCE_1),
        _batfish);

    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_IP)
            .setDstIp(CONTAINER_GROUP_IP)
            .setSrcPort(1025)
            .setDstPort(80)
            .setIpProtocol(IpProtocol.TCP)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, CONTAINER_GROUP, CONTAINER_INSTANCE_1),
        _batfish);
  }

  @Test
  public void testVm1toContainer2() {
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_IP)
            .setDstIp(CONTAINER_GROUP_IP)
            .setSrcPort(1025)
            .setDstPort(443)
            .setIpProtocol(IpProtocol.UDP)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, CONTAINER_GROUP, CONTAINER_INSTANCE_2),
        _batfish);

    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_IP)
            .setDstIp(CONTAINER_GROUP_IP)
            .setSrcPort(1025)
            .setDstPort(80)
            .setIpProtocol(IpProtocol.UDP)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, CONTAINER_GROUP, CONTAINER_INSTANCE_2),
        _batfish);
  }
}
