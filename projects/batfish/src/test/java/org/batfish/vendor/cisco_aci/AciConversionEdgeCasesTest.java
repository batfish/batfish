package org.batfish.vendor.cisco_aci;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.SortedMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.batfish.vendor.cisco_aci.representation.AciVrfModel;
import org.junit.Test;

/** Tests for {@link AciConversion} edge cases and error scenarios. */
public class AciConversionEdgeCasesTest {

  /** Creates a basic fabric node for testing. */
  private AciConfiguration.FabricNode createFabricNode(
      String nodeId, String name, String podId, String role) {
    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId(nodeId);
    node.setName(name);
    node.setPodId(podId);
    node.setRole(role);
    return node;
  }

  /** Test conversion with no fabric nodes. */
  @Test
  public void testNoFabricNodes() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("no-nodes-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should create a single configuration for the fabric hostname
    assertThat(viConfigs, aMapWithSize(1));
    assertThat(viConfigs, hasKey("no-nodes-fabric"));

    Configuration fabricConfig = viConfigs.get("no-nodes-fabric");
    assertNotNull(fabricConfig);
    assertThat(fabricConfig.getConfigurationFormat(), equalTo(ConfigurationFormat.CISCO_ACI));
    assertThat(fabricConfig.getVrfs(), hasKey(DEFAULT_VRF_NAME));
  }

  /** Test conversion with fabric node missing name. */
  @Test
  public void testFabricNodeWithoutName() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId("101");
    // Name is not set
    node.setPodId("1");
    node.setRole("spine");

    config.getFabricNodes().put("101", node);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should still create a configuration, using node ID as fallback
    assertThat(viConfigs, aMapWithSize(1));
    Configuration nodeConfig = viConfigs.get("test-fabric-101");
    assertNotNull(nodeConfig);
    assertEquals("test-fabric-101", nodeConfig.getHostname());
  }

  /** Test conversion with duplicate aci prefix in fabric hostname. */
  @Test
  public void testFabricNodeFallbackCollapsesDuplicateAciPrefix() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("aci-aci-dc2-ce2.json");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId("1204");
    node.setPodId("1");
    node.setRole("leaf");
    config.getFabricNodes().put("1204", node);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration nodeConfig = viConfigs.get("aci-dc2-ce2-1204");
    assertNotNull(nodeConfig);
    assertEquals("aci-dc2-ce2-1204", nodeConfig.getHostname());
  }

  /** Test conversion with EPG without bridge domain. */
  @Test
  public void testEpgWithoutBridgeDomain() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create EPG without bridge domain - using getOrCreateEpg
    config.getOrCreateEpg("tenant1:epg1");
    config.getEpgs().get("tenant1:epg1").setTenant("tenant1");
    // Bridge domain not set
    config.getEpgs().get("tenant1:epg1").setDescription("EPG without BD");

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should still create configuration
    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);
    // EPG without BD should not cause failure
  }

  /** Test conversion with contract without subjects. */
  @Test
  public void testContractWithoutSubjects() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create contract without subjects - using getOrCreateContract
    AciConfiguration.Contract contract = config.getOrCreateContract("tenant1:empty-contract");
    contract.setTenant("tenant1");
    contract.setDescription("Contract with no subjects");
    // Subjects list is empty by default

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should create configuration even with empty contract (no subjects = no ACL)
    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);
    // Contract without subjects doesn't create an ACL
  }

  /** Test conversion with L3Out without BGP. */
  @Test
  public void testL3OutWithoutBgp() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create VRF
    AciVrfModel vrf = config.getOrCreateVrf("tenant1:vrf1");
    vrf.setTenant("tenant1");

    // Create L3Out without BGP - using getOrCreateL3Out
    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");
    l3out.setVrf("tenant1:vrf1");
    // BGP process not set

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should create configuration without BGP
    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);
    // L3Out should be processed even without BGP
  }

  /** Test conversion with VRF not referenced by any bridge domain. */
  @Test
  public void testVrfNotReferencedByAnyBd() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create VRF that won't be referenced
    AciVrfModel orphanedVrf = config.getOrCreateVrf("tenant1:orphaned-vrf");
    orphanedVrf.setTenant("tenant1");
    orphanedVrf.setDescription("Orphaned VRF");

    // Create another VRF that will be used
    AciVrfModel usedVrf = config.getOrCreateVrf("tenant1:used-vrf");
    usedVrf.setTenant("tenant1");

    // Create bridge domain using only used-vrf
    AciConfiguration.BridgeDomain bd = config.getOrCreateBridgeDomain("tenant1:bd1");
    bd.setVrf("tenant1:used-vrf");
    bd.setSubnets(ImmutableList.of("10.1.1.0/24"));

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Both VRFs should be created in the vendor-independent config
    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);
    assertThat(leafConfig.getVrfs(), hasKey("tenant1:orphaned-vrf"));
    assertThat(leafConfig.getVrfs(), hasKey("tenant1:used-vrf"));
  }

  /** Test conversion with multiple bridge domains in same VRF. */
  @Test
  public void testMultipleBridgeDomainsSameVrf() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create VRF
    AciVrfModel vrf = config.getOrCreateVrf("tenant1:shared-vrf");
    vrf.setTenant("tenant1");

    // Create multiple bridge domains in same VRF
    AciConfiguration.BridgeDomain bd1 = config.getOrCreateBridgeDomain("tenant1:bd1");
    bd1.setVrf("tenant1:shared-vrf");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    AciConfiguration.BridgeDomain bd2 = config.getOrCreateBridgeDomain("tenant1:bd2");
    bd2.setVrf("tenant1:shared-vrf");
    bd2.setSubnets(ImmutableList.of("10.2.1.0/24"));

    AciConfiguration.BridgeDomain bd3 = config.getOrCreateBridgeDomain("tenant1:bd3");
    bd3.setVrf("tenant1:shared-vrf");
    bd3.setSubnets(ImmutableList.of("10.3.1.0/24"));

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);

    // Count VLAN interfaces with addresses
    int vlanInterfaceCount = 0;
    for (Interface iface : leafConfig.getAllInterfaces().values()) {
      if (iface.getInterfaceType() == org.batfish.datamodel.InterfaceType.VLAN
          && !iface.getAllAddresses().isEmpty()) {
        vlanInterfaceCount++;
      }
    }

    // Should have at least 3 VLAN interfaces for 3 BDs
    assertTrue(
        "Expected at least 3 VLAN interfaces, got " + vlanInterfaceCount, vlanInterfaceCount >= 3);
  }

  /** Test conversion with contract having deny action (filter with deny). */
  @Test
  public void testContractWithDenyAction() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create contract with deny action filter
    AciConfiguration.Contract contract = config.getOrCreateContract("tenant1:deny-contract");
    contract.setTenant("tenant1");

    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    subject.setName("deny-subject");

    AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
    filter.setName("deny-filter");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("23"));

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);

    String aclName = AciConversion.getContractAclName("tenant1:deny-contract");
    assertThat(leafConfig.getIpAccessLists(), hasKey(aclName));
  }

  /** Test conversion with empty tenant. */
  @Test
  public void testEmptyTenant() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // Create empty tenant (no VRFs, BDs, EPGs, or contracts)
    config.getOrCreateTenant("empty-tenant");

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should still create valid configuration
    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);
  }

  /** Test conversion with interface without VLAN. */
  @Test
  public void testInterfaceWithoutVlan() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node with interface
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");

    AciConfiguration.FabricNode.Interface iface = new AciConfiguration.FabricNode.Interface();
    iface.setName("ethernet1/1");
    iface.setType("ethernet");
    iface.setEnabled(true);
    // VLAN not set
    iface.setEpg(null);

    node.getInterfaces().put("ethernet1/1", iface);
    config.getFabricNodes().put("101", node);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);

    // Interface should exist even without VLAN
    Interface convertedIface = leafConfig.getAllInterfaces().get("ethernet1/1");
    assertNotNull(convertedIface);
    assertThat(
        convertedIface.getInterfaceType(), equalTo(org.batfish.datamodel.InterfaceType.PHYSICAL));
  }

  /** Test conversion with node missing role. */
  @Test
  public void testFabricNodeWithoutRole() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId("101");
    node.setName("node1");
    node.setPodId("1");
    // Role not set

    config.getFabricNodes().put("101", node);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Should still create configuration
    assertThat(viConfigs, aMapWithSize(1));
  }

  /** Test conversion with only default VRF present. */
  @Test
  public void testOnlyDefaultVrf() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Add fabric node
    AciConfiguration.FabricNode node = createFabricNode("101", "leaf1", "1", "leaf");
    config.getFabricNodes().put("101", node);

    // No custom VRFs defined
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> viConfigs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration leafConfig = viConfigs.get("leaf1");
    assertNotNull(leafConfig);

    // Should have default VRF
    assertThat(leafConfig.getVrfs(), hasKey(DEFAULT_VRF_NAME));
    Vrf defaultVrf = leafConfig.getVrfs().get(DEFAULT_VRF_NAME);
    assertNotNull(defaultVrf);
  }
}
