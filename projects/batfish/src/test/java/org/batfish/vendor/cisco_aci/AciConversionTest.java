package org.batfish.vendor.cisco_aci;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.SortedMap;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.batfish.vendor.cisco_aci.representation.AciVrfModel;
import org.junit.Test;

/** Tests of {@link AciConversion} and ACI configuration conversion. */
public class AciConversionTest {

  /**
   * Creates a test ACI configuration with sample fabric nodes, VRFs, bridge domains, and contracts.
   */
  private AciConfiguration createTestAciConfiguration() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Create fabric nodes (nodeId, name, podId, role)
    AciConfiguration.FabricNode spine1 = createFabricNode("101", "spine1", "1", "spine");
    AciConfiguration.FabricNode spine2 = createFabricNode("102", "spine2", "1", "spine");
    AciConfiguration.FabricNode leaf1 = createFabricNode("201", "leaf1", "2", "leaf");
    AciConfiguration.FabricNode leaf2 = createFabricNode("202", "leaf2", "2", "leaf");

    config.getFabricNodes().put("101", spine1);
    config.getFabricNodes().put("102", spine2);
    config.getFabricNodes().put("201", leaf1);
    config.getFabricNodes().put("202", leaf2);

    // Create VRFs
    AciVrfModel vrf1 = new AciVrfModel("vrf1");
    vrf1.setTenant("tenant1");
    vrf1.setDescription("Test VRF 1");

    AciVrfModel vrf2 = new AciVrfModel("vrf2");
    vrf2.setTenant("tenant1");
    vrf2.setDescription("Test VRF 2");

    config.getVrfs().put("vrf1", vrf1);
    config.getVrfs().put("vrf2", vrf2);

    // Create bridge domains
    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setVrf("vrf1");
    bd1.setTenant("tenant1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24", "10.1.2.0/24"));
    bd1.setDescription("Bridge Domain 1");

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setVrf("vrf2");
    bd2.setTenant("tenant1");
    bd2.setSubnets(ImmutableList.of("10.2.1.0/24"));
    bd2.setDescription("Bridge Domain 2");

    config.getBridgeDomains().put("bd1", bd1);
    config.getBridgeDomains().put("bd2", bd2);

    // Create contracts
    AciConfiguration.Contract contract1 = new AciConfiguration.Contract("web_contract");
    contract1.setTenant("tenant1");
    contract1.setDescription("Web traffic contract");

    AciConfiguration.Contract.Subject subject1 = new AciConfiguration.Contract.Subject();
    subject1.setName("http_subject");

    AciConfiguration.Contract.Filter filter1 = new AciConfiguration.Contract.Filter();
    filter1.setName("http_filter");
    filter1.setIpProtocol("tcp");
    filter1.setDestinationPorts(ImmutableList.of("80", "443"));

    AciConfiguration.Contract.Filter filter2 = new AciConfiguration.Contract.Filter();
    filter2.setName("https_filter");
    filter2.setIpProtocol("tcp");
    filter2.setDestinationPorts(ImmutableList.of("8443"));

    subject1.setFilters(ImmutableList.of(filter1, filter2));
    contract1.setSubjects(ImmutableList.of(subject1));

    config.getContracts().put("web_contract", contract1);

    // Create EPGs
    AciConfiguration.Epg epg1 = new AciConfiguration.Epg("web_epg");
    epg1.setTenant("tenant1");
    epg1.setBridgeDomain("bd1");
    epg1.setProvidedContracts(ImmutableList.of("web_contract"));
    epg1.setDescription("Web EPG");

    AciConfiguration.Epg epg2 = new AciConfiguration.Epg("app_epg");
    epg2.setTenant("tenant1");
    epg2.setBridgeDomain("bd1");
    epg2.setConsumedContracts(ImmutableList.of("web_contract"));
    epg2.setDescription("App EPG");

    config.getEpgs().put("web_epg", epg1);
    config.getEpgs().put("app_epg", epg2);

    config.finalizeStructures();
    return config;
  }

  /** Creates a fabric node with the given attributes. */
  private AciConfiguration.FabricNode createFabricNode(
      String nodeId, String name, String podId, String role) {
    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId(nodeId);
    node.setName(name);
    node.setPodId(podId);
    node.setRole(role);

    // Add some interfaces
    AciConfiguration.FabricNode.Interface iface1 = new AciConfiguration.FabricNode.Interface();
    iface1.setName("ethernet1/1");
    iface1.setType("ethernet");
    iface1.setEnabled(true);
    iface1.setDescription("Interface 1/1");

    AciConfiguration.FabricNode.Interface iface2 = new AciConfiguration.FabricNode.Interface();
    iface2.setName("ethernet1/2");
    iface2.setType("ethernet");
    iface2.setEnabled(true);
    iface2.setDescription("Interface 1/2");

    node.getInterfaces().put("ethernet1/1", iface1);
    node.getInterfaces().put("ethernet1/2", iface2);

    return node;
  }

  @Test
  public void testToVendorIndependentConfigurations_fabricNodesToSeparateConfigurations() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Should have 4 configurations (4 fabric nodes)
    // Keys are now resolved hostnames.
    assertThat(configs.size(), equalTo(4));
    assertThat(configs, hasKey("spine1"));
    assertThat(configs, hasKey("spine2"));
    assertThat(configs, hasKey("leaf1"));
    assertThat(configs, hasKey("leaf2"));
  }

  @Test
  public void testToVendorIndependentConfigurations_vrfsConverted() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Check that VRFs are created in each configuration
    for (Configuration config : configs.values()) {
      // Should have default VRF
      assertThat(config.getVrfs(), hasKey(DEFAULT_VRF_NAME));

      // Should have custom VRFs
      assertThat(config.getVrfs(), hasKey("vrf1"));
      assertThat(config.getVrfs(), hasKey("vrf2"));

      // Verify VRF properties
      Vrf vrf1 = config.getVrfs().get("vrf1");
      assertNotNull(vrf1);
      assertEquals("vrf1", vrf1.getName());
    }
  }

  @Test
  public void testToVendorIndependentConfigurations_bridgeDomainsCreateInterfaces() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Check that VLAN interfaces are created for bridge domains
    Configuration leaf1Config = configs.get("leaf1");
    assertNotNull(leaf1Config);

    // Bridge domains should create VLAN interfaces with subnet addresses
    boolean hasVlanInterface = false;
    for (Interface iface : leaf1Config.getAllInterfaces().values()) {
      if (iface.getInterfaceType() == InterfaceType.VLAN) {
        hasVlanInterface = true;
        // Check that interface has addresses from bridge domain subnets
        assertFalse(iface.getAllAddresses().isEmpty());
        break;
      }
    }
    assertTrue("Expected VLAN interface for bridge domain", hasVlanInterface);
  }

  @Test
  public void testToVendorIndependentConfigurations_contractsCreateAcls() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Check that ACLs are created for contracts
    Configuration leaf1Config = configs.get("leaf1");
    assertNotNull(leaf1Config);

    String aclName = AciConversion.getContractAclName("web_contract");
    assertThat(leaf1Config.getIpAccessLists(), hasKey(aclName));

    IpAccessList acl = leaf1Config.getIpAccessLists().get(aclName);
    assertNotNull(acl);

    // Should have permit lines for the contract filters plus implicit deny
    assertTrue(acl.getLines().size() >= 3); // 2 permit lines + 1 deny
  }

  @Test
  public void testGetContractAclName() {
    assertThat(AciConversion.getContractAclName("web_contract"), equalTo("~CONTRACT~web_contract"));
    assertThat(AciConversion.getContractAclName("app_to_db"), equalTo("~CONTRACT~app_to_db"));
    // Verify consistent format
    assertTrue(AciConversion.getContractAclName("test").startsWith("~CONTRACT~"));
  }

  @Test
  public void testToInterfaceType() {
    // This tests the interface type conversion logic
    // Note: toInterfaceType is private, so we verify through the conversion result
    AciConfiguration aciConfig = createTestAciConfiguration();

    // Add different interface types
    AciConfiguration.FabricNode node = aciConfig.getFabricNodes().get("201");

    AciConfiguration.FabricNode.Interface vlanIface = new AciConfiguration.FabricNode.Interface();
    vlanIface.setName("vlan100");
    vlanIface.setType("vlan");
    vlanIface.setEnabled(true);
    node.getInterfaces().put("vlan100", vlanIface);

    AciConfiguration.FabricNode.Interface loopbackIface =
        new AciConfiguration.FabricNode.Interface();
    loopbackIface.setName("loopback0");
    loopbackIface.setType("loopback");
    loopbackIface.setEnabled(true);
    node.getInterfaces().put("loopback0", loopbackIface);

    AciConfiguration.FabricNode.Interface poIface = new AciConfiguration.FabricNode.Interface();
    poIface.setName("port-channel1");
    poIface.setType("portchannel");
    poIface.setEnabled(true);
    node.getInterfaces().put("port-channel1", poIface);

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    Configuration config = configs.get("leaf1");
    assertNotNull(config);

    // Verify interface types are correct
    Interface ethernetIface = config.getAllInterfaces().get("ethernet1/1");
    assertNotNull(ethernetIface);
    assertEquals(InterfaceType.PHYSICAL, ethernetIface.getInterfaceType());

    Interface vlanInterface = config.getAllInterfaces().get("vlan100");
    if (vlanInterface != null) {
      assertEquals(InterfaceType.VLAN, vlanInterface.getInterfaceType());
    }

    Interface loopbackInterface = config.getAllInterfaces().get("loopback0");
    if (loopbackInterface != null) {
      assertEquals(InterfaceType.LOOPBACK, loopbackInterface.getInterfaceType());
    }

    Interface poInterface = config.getAllInterfaces().get("port-channel1");
    if (poInterface != null) {
      assertEquals(InterfaceType.AGGREGATED, poInterface.getInterfaceType());
    }
  }

  @Test
  public void testToInterfaceAddress() {
    Ip ip = Ip.parse("192.168.1.1");
    int prefixLength = 24;

    var addr = AciConversion.toInterfaceAddress(ip, prefixLength);

    assertThat(addr.getIp(), equalTo(ip));
    assertThat(addr.getNetworkBits(), equalTo(24));
    assertThat(addr.getPrefix(), equalTo(Prefix.parse("192.168.1.0/24")));
  }

  @Test
  public void testToIpWildcard() {
    Ip prefix = Ip.parse("192.168.1.0");
    Ip wildcard = Ip.parse("0.0.0.255");

    var ipWildcard = AciConversion.toIpWildcard(prefix, wildcard);

    assertThat(ipWildcard.getIp(), equalTo(prefix));
    assertThat(ipWildcard.getWildcardMaskAsIp(), equalTo(wildcard));
  }

  @Test
  public void testCreateEdges_spineLeafTopology() {
    AciConfiguration aciConfig = createTestAciConfiguration();

    var edges = AciConversion.createLayer1Edges(aciConfig);

    // Should create edges between spines and leaves
    // 2 spines x 2 leaves = 4 edges
    assertThat(edges.size(), equalTo(4));

    // Verify edge structure
    for (Layer1Edge edge : edges) {
      // Edge should connect a leaf to a spine using hostnames.
      assertTrue(
          "Edge node1 should be leaf or spine (by hostname)",
          ImmutableList.of("leaf1", "leaf2", "spine1", "spine2")
              .contains(edge.getNode1().getHostname()));
      assertTrue(
          "Edge node2 should be leaf or spine (by hostname)",
          ImmutableList.of("leaf1", "leaf2", "spine1", "spine2")
              .contains(edge.getNode2().getHostname()));
    }
  }

  @Test
  public void testContractAclTrafficMatching() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    Configuration config = configs.get("leaf1");
    String aclName = AciConversion.getContractAclName("web_contract");
    IpAccessList acl = config.getIpAccessLists().get(aclName);
    assertNotNull(acl);

    // Create test flows
    Flow permittedHttpFlow =
        Flow.builder()
            .setIngressNode("leaf1")
            .setIngressInterface("ethernet1/1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.parse("10.1.1.10"))
            .setDstIp(Ip.parse("10.1.2.10"))
            .setSrcPort(12345)
            .setDstPort(80)
            .build();

    Flow permittedHttpsFlow =
        Flow.builder()
            .setIngressNode("leaf1")
            .setIngressInterface("ethernet1/1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.parse("10.1.1.10"))
            .setDstIp(Ip.parse("10.1.2.10"))
            .setSrcPort(12345)
            .setDstPort(443)
            .build();

    Flow deniedFlow =
        Flow.builder()
            .setIngressNode("leaf1")
            .setIngressInterface("ethernet1/1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.parse("10.1.1.10"))
            .setDstIp(Ip.parse("10.1.2.10"))
            .setSrcPort(12345)
            .setDstPort(22) // SSH not in contract
            .build();

    // Test ACL behavior
    assertThat(
        acl, accepts(permittedHttpFlow, "ethernet1/1", ImmutableMap.of(), ImmutableMap.of()));
    assertThat(
        acl, accepts(permittedHttpsFlow, "ethernet1/1", ImmutableMap.of(), ImmutableMap.of()));
    assertThat(acl, rejects(deniedFlow, "ethernet1/1", ImmutableMap.of(), ImmutableMap.of()));
  }

  @Test
  public void testEmptyConfiguration() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("empty-fabric");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    // Empty config (no fabric nodes) should create a single configuration for the fabric
    assertThat(configs.size(), equalTo(1));
    assertThat(configs, hasKey("empty-fabric"));

    // Verify the single config has expected structure
    Configuration emptyFabricConfig = configs.get("empty-fabric");
    assertNotNull(emptyFabricConfig);
    assertThat(emptyFabricConfig.getConfigurationFormat(), equalTo(ConfigurationFormat.CISCO_ACI));
    assertThat(emptyFabricConfig.getVrfs(), hasKey(DEFAULT_VRF_NAME));
  }

  @Test
  public void testConfigurationFormat() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // All configurations should have CISCO_ACI format
    for (Configuration config : configs.values()) {
      assertThat(config.getConfigurationFormat(), equalTo(ConfigurationFormat.CISCO_ACI));
    }
  }

  @Test
  public void testDefaultVrfAlwaysPresent() {
    AciConfiguration aciConfig = createTestAciConfiguration();
    Warnings warnings = new Warnings(false, true, true);

    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Default VRF should be present in all configurations
    for (Configuration config : configs.values()) {
      assertThat(config.getVrfs(), hasKey(DEFAULT_VRF_NAME));
      Vrf defaultVrf = config.getVrfs().get(DEFAULT_VRF_NAME);
      assertNotNull(defaultVrf);
      assertEquals(DEFAULT_VRF_NAME, defaultVrf.getName());
    }
  }

  @Test
  public void testMultipleBridgeDomainsSameVrf() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("multi-bd-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Create a fabric node (nodeId, name, podId, role)
    AciConfiguration.FabricNode node = createFabricNode("201", "leaf1", "2", "leaf");
    config.getFabricNodes().put("201", node);

    // Create VRF
    AciVrfModel vrf = new AciVrfModel("shared_vrf");
    vrf.setTenant("tenant1");
    config.getVrfs().put("shared_vrf", vrf);

    // Create multiple bridge domains in the same VRF
    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setVrf("shared_vrf");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setVrf("shared_vrf");
    bd2.setSubnets(ImmutableList.of("10.2.1.0/24"));

    AciConfiguration.BridgeDomain bd3 = new AciConfiguration.BridgeDomain("bd3");
    bd3.setVrf("shared_vrf");
    bd3.setSubnets(ImmutableList.of("10.3.1.0/24"));

    config.getBridgeDomains().put("bd1", bd1);
    config.getBridgeDomains().put("bd2", bd2);
    config.getBridgeDomains().put("bd3", bd3);

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration leafConfig = configs.get("leaf1");
    assertNotNull(leafConfig);

    // All bridge domain subnets should be accessible via interfaces
    int vlanInterfaceCount = 0;
    for (Interface iface : leafConfig.getAllInterfaces().values()) {
      if (iface.getInterfaceType() == InterfaceType.VLAN && !iface.getAllAddresses().isEmpty()) {
        vlanInterfaceCount++;
      }
    }

    // Should have at least 3 VLAN interfaces (one per bridge domain)
    assertTrue(
        "Expected at least 3 VLAN interfaces for 3 bridge domains, got " + vlanInterfaceCount,
        vlanInterfaceCount >= 3);
  }

  @Test
  public void testContractWithMultipleFilters() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("multi-filter-contract");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Create a fabric node (nodeId, name, podId, role)
    AciConfiguration.FabricNode node = createFabricNode("201", "leaf1", "2", "leaf");
    config.getFabricNodes().put("201", node);

    // Create contract with multiple subjects and filters
    AciConfiguration.Contract contract = new AciConfiguration.Contract("multi_filter_contract");
    contract.setTenant("tenant1");

    AciConfiguration.Contract.Subject subject1 = new AciConfiguration.Contract.Subject();
    subject1.setName("web_traffic");

    AciConfiguration.Contract.Filter httpFilter = new AciConfiguration.Contract.Filter();
    httpFilter.setName("http");
    httpFilter.setIpProtocol("tcp");
    httpFilter.setDestinationPorts(ImmutableList.of("80"));

    AciConfiguration.Contract.Filter httpsFilter = new AciConfiguration.Contract.Filter();
    httpsFilter.setName("https");
    httpsFilter.setIpProtocol("tcp");
    httpsFilter.setDestinationPorts(ImmutableList.of("443"));

    AciConfiguration.Contract.Filter icmpFilter = new AciConfiguration.Contract.Filter();
    icmpFilter.setName("icmp");
    icmpFilter.setIpProtocol("icmp");

    subject1.setFilters(ImmutableList.of(httpFilter, httpsFilter, icmpFilter));
    contract.setSubjects(ImmutableList.of(subject1));

    config.getContracts().put("multi_filter_contract", contract);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration leafConfig = configs.get("leaf1");
    String aclName = AciConversion.getContractAclName("multi_filter_contract");
    IpAccessList acl = leafConfig.getIpAccessLists().get(aclName);
    assertNotNull(acl);

    // Should have 3 permit lines (one per filter) plus implicit deny
    assertThat(acl.getLines().size(), equalTo(4));

    // Count permit lines
    long permitCount =
        acl.getLines().stream()
            .filter(line -> ((ExprAclLine) line).getAction() == LineAction.PERMIT)
            .count();
    assertThat(permitCount, equalTo(3L));
  }

  @Test
  public void testArpOpcodeUnspecifiedDoesNotWarn() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("arp-unspecified-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.getFabricNodes().put("201", createFabricNode("201", "leaf1", "2", "leaf"));

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:arp_contract");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
    filter.setName("arp_filter");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("22"));
    filter.setArpOpcode("unspecified");
    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:arp_contract", contract);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    AciConversion.toVendorIndependentConfigurations(config, warnings);

    assertFalse(
        warnings.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("ARP opcode specified in contract")));
  }

  @Test
  public void testArpOpcodeSpecificStillWarns() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("arp-specific-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.getFabricNodes().put("201", createFabricNode("201", "leaf1", "2", "leaf"));

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:arp_contract");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
    filter.setName("arp_filter");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("22"));
    filter.setArpOpcode("request");
    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:arp_contract", contract);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    AciConversion.toVendorIndependentConfigurations(config, warnings);

    assertTrue(
        warnings.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("ARP opcode specified in contract")));
  }

  @Test
  public void testUnspecifiedRangeEndpointsDoNotProduceInvalidPortWarnings() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("port-range-unspecified-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.getFabricNodes().put("201", createFabricNode("201", "leaf1", "2", "leaf"));

    AciConfiguration.Filter filterModel = new AciConfiguration.Filter("tenant1:port_filter");
    filterModel.setTenant("tenant1");
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setName("entry1");
    entry.setProtocol("tcp");
    entry.setDestinationFromPort("unspecified");
    entry.setDestinationToPort("unspecified");
    entry.setSourceFromPort("0");
    entry.setSourceToPort("0");
    filterModel.setEntries(ImmutableList.of(entry));
    config.getFilters().put("tenant1:port_filter", filterModel);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:test_contract");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("port_filter");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:test_contract", contract);

    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    AciConversion.toVendorIndependentConfigurations(config, warnings);

    assertFalse(
        warnings.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("Invalid destination port")));
    assertFalse(
        warnings.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("Invalid source port")));
  }

  @Test
  public void testUnspecifiedSinglePortZeroDoesNotConstrainAcl() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("port-single-unspecified-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.getFabricNodes().put("201", createFabricNode("201", "leaf1", "2", "leaf"));

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:test_contract");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
    filter.setName("tcp_any_dst");
    filter.setIpProtocol("tcp");
    // Placeholder value from upstream "unspecified" mapping.
    filter.setDestinationPorts(ImmutableList.of("0"));
    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:test_contract", contract);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration viConfig = configs.get("leaf1");
    String aclName = AciConversion.getContractAclName("tenant1:test_contract");
    IpAccessList acl = viConfig.getIpAccessLists().get(aclName);
    assertNotNull(acl);

    Flow tcp80 =
        Flow.builder()
            .setIngressNode("leaf1")
            .setIngressInterface("ethernet1/1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("10.0.0.2"))
            .setSrcPort(12345)
            .setDstPort(80)
            .build();

    assertThat(acl, accepts(tcp80, "ethernet1/1", ImmutableMap.of(), ImmutableMap.of()));
  }

  @Test
  public void testPlaceholderRangeTokenZeroZeroDoesNotConstrainAclOrWarn() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("port-range-zero-zero-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);
    config.getFabricNodes().put("201", createFabricNode("201", "leaf1", "2", "leaf"));

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:test_contract");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
    filter.setName("tcp_any_dst");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("0-0"));
    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:test_contract", contract);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration viConfig = configs.get("leaf1");
    String aclName = AciConversion.getContractAclName("tenant1:test_contract");
    IpAccessList acl = viConfig.getIpAccessLists().get(aclName);
    assertNotNull(acl);

    Flow tcp80 =
        Flow.builder()
            .setIngressNode("leaf1")
            .setIngressInterface("ethernet1/1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("10.0.0.2"))
            .setSrcPort(12345)
            .setDstPort(80)
            .build();
    assertThat(acl, accepts(tcp80, "ethernet1/1", ImmutableMap.of(), ImmutableMap.of()));

    assertFalse(
        warnings.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("Invalid destination port range")));
  }

  @Test
  public void testEpgPoliciesAttachedToInterfaceFilters() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("epg-policy-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId("201");
    node.setName("leaf1");
    node.setRole("leaf");

    AciConfiguration.FabricNode.Interface iface = new AciConfiguration.FabricNode.Interface();
    iface.setName("ethernet1/10");
    iface.setType("ethernet");
    iface.setEnabled(true);
    iface.setEpg("app_epg");
    node.getInterfaces().put("ethernet1/10", iface);
    config.getFabricNodes().put("201", node);

    AciConfiguration.Contract allowContract = new AciConfiguration.Contract("tenant1:web");
    allowContract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
    filter.setName("allow_http");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("80"));
    subject.setFilters(ImmutableList.of(filter));
    allowContract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:web", allowContract);

    AciConfiguration.Epg epg = new AciConfiguration.Epg("app_epg");
    epg.setTenant("tenant1");
    epg.setConsumedContracts(ImmutableList.of("tenant1:web"));
    epg.setProvidedContracts(ImmutableList.of("tenant1:web"));
    config.getEpgs().put("app_epg", epg);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration viConfig = configs.get("leaf1");
    Interface viIface = viConfig.getAllInterfaces().get("ethernet1/10");
    assertNotNull(viIface);
    assertNotNull(viIface.getIncomingFilter());
    assertNotNull(viIface.getOutgoingFilter());
  }

  @Test
  public void testTabooPolicyTakesPrecedenceOverPermittedContract() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("taboo-precedence-test");
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    AciConfiguration.FabricNode node = new AciConfiguration.FabricNode();
    node.setNodeId("201");
    node.setName("leaf1");
    node.setRole("leaf");

    AciConfiguration.FabricNode.Interface iface = new AciConfiguration.FabricNode.Interface();
    iface.setName("ethernet1/20");
    iface.setType("ethernet");
    iface.setEnabled(true);
    iface.setEpg("web_epg");
    node.getInterfaces().put("ethernet1/20", iface);
    config.getFabricNodes().put("201", node);

    AciConfiguration.Contract allow443 = new AciConfiguration.Contract("tenant1:allow443");
    allow443.setTenant("tenant1");
    AciConfiguration.Contract.Subject allowSubject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter allowFilter = new AciConfiguration.Contract.Filter();
    allowFilter.setName("allow443");
    allowFilter.setIpProtocol("tcp");
    allowFilter.setDestinationPorts(ImmutableList.of("443"));
    allowSubject.setFilters(ImmutableList.of(allowFilter));
    allow443.setSubjects(ImmutableList.of(allowSubject));
    config.getContracts().put("tenant1:allow443", allow443);

    AciConfiguration.TabooContract taboo443 =
        new AciConfiguration.TabooContract("tenant1:taboo443");
    taboo443.setTenant("tenant1");
    AciConfiguration.Contract.Subject tabooSubject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter tabooFilter = new AciConfiguration.Contract.Filter();
    tabooFilter.setName("taboo443");
    tabooFilter.setIpProtocol("tcp");
    tabooFilter.setDestinationPorts(ImmutableList.of("443"));
    tabooSubject.setFilters(ImmutableList.of(tabooFilter));
    taboo443.setSubjects(ImmutableList.of(tabooSubject));
    config.getTabooContracts().put("tenant1:taboo443", taboo443);

    AciConfiguration.Epg epg = new AciConfiguration.Epg("web_epg");
    epg.setTenant("tenant1");
    epg.setProvidedContracts(ImmutableList.of("tenant1:allow443"));
    epg.setProtectedByTaboos(ImmutableList.of("tenant1:taboo443"));
    config.getEpgs().put("web_epg", epg);
    config.finalizeStructures();

    Warnings warnings = new Warnings(false, true, true);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(config, warnings);

    Configuration viConfig = configs.get("leaf1");
    Interface viIface = viConfig.getAllInterfaces().get("ethernet1/20");
    assertNotNull(viIface);
    assertNotNull(viIface.getOutgoingFilter());
    assertThat(
        viConfig.getIpAccessLists(), hasKey(AciConversion.getTabooAclName("tenant1:taboo443")));

    Flow httpsFlow =
        Flow.builder()
            .setIngressNode("leaf1")
            .setIngressInterface("ethernet1/20")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("10.0.0.2"))
            .setSrcPort(12345)
            .setDstPort(443)
            .build();

    assertThat(
        viIface
            .getOutgoingFilter()
            .filter(httpsFlow, "ethernet1/20", viConfig.getIpAccessLists(), ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));
  }
}
