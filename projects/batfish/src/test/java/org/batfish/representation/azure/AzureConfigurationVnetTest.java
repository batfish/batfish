package org.batfish.representation.azure;

import static org.batfish.representation.azure.AzureConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.azure.AzureConfigurationTestUtils.testSetup;
import static org.batfish.representation.azure.AzureConfigurationTestUtils.testTrace;

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

public class AzureConfigurationVnetTest {
  private static final String TESTCONFIGS_DIR = "org/batfish/representation/azure/test-vnet";

  private static final List<String> fileNames =
      ImmutableList.of("vnet.json", "vm1.json", "vm2.json", "vm1346.json", "vm235.json");

  // various entities in the configs
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

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testHostToHost() {
    testTrace(
        getAnyFlow(VM_1, VM_2_IP, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, VM_2),
        _batfish);

    testTrace(
        getAnyFlow(VM_2, VM_1_IP, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_2, SUBNET_2, VNET, SUBNET_1, VM_1),
        _batfish);
  }

  @Test
  public void testHostToUnknownSubnet() {
    Ip fakeHostIp = Ip.parse("10.0.2.4");
    testTrace(
        getAnyFlow(VM_1, fakeHostIp, _batfish),
        FlowDisposition.NO_ROUTE,
        ImmutableList.of(VM_1, SUBNET_1, VNET),
        _batfish);
  }
}
