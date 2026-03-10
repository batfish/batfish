package org.batfish.representation.azure;

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

public class AzureConfigurationPostgresTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/azure/test-postgres";

  private static final List<String> fileNames =
      ImmutableList.of("vnet.json", "db.json", "vm1.json", "vm1346.json");

  private static final String VNET =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet";
  private static final String SUBNET_1 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_default";
  private static final String SUBNET_2 =
      "resourcegroups_test_providers_microsoft.network_virtualnetworks_vm1-vnet_subnets_private";
  private static final String VM_1 =
      "resourcegroups_test_providers_microsoft.compute_virtualmachines_vm1";
  private static final Ip VM_1_IP = Ip.parse("10.0.0.4");
  private static final String DB = "testdb_id";
  private static final Ip DB_IP = Ip.parse("10.0.1.2"); // first IP after subnet router

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testVm1ToDb() {
    testTrace(
        Flow.builder()
            .setIngressNode(VM_1)
            .setSrcIp(VM_1_IP)
            .setDstIp(DB_IP)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1025)
            .setDstPort(80)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(VM_1, SUBNET_1, VNET, SUBNET_2, DB),
        _batfish);
  }

  @Test
  public void testDbToVm1() {
    testTrace(
        Flow.builder()
            .setIngressNode(DB)
            .setSrcIp(DB_IP)
            .setDstIp(VM_1_IP)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1025)
            .setDstPort(80)
            .build(),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(DB, SUBNET_2, VNET, SUBNET_1, VM_1),
        _batfish);
  }
}
