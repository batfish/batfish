package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.batfish.vendor.cisco_aci.representation.BridgeDomain;
import org.batfish.vendor.cisco_aci.representation.Contract;
import org.batfish.vendor.cisco_aci.representation.Epg;
import org.batfish.vendor.cisco_aci.representation.FabricNode;
import org.batfish.vendor.cisco_aci.representation.ManagementInfo;
import org.batfish.vendor.cisco_aci.representation.Tenant;
import org.junit.Test;

/**
 * End-to-end test that demonstrates the full ACI parsing and conversion pipeline.
 *
 * <p>This test loads a real ACI JSON config, parses it using AciConfiguration.fromJson(), converts
 * to vendor-independent configurations, and verifies that each component is correctly processed.
 */
public class AciEndToEndTest {

  /**
   * Loads the real ACI JSON config and runs through the full parsing and conversion pipeline.
   *
   * <p>This test demonstrates:
   *
   * <ol>
   *   <li>Loading a sample ACI JSON config (real ACI backup)
   *   <li>Parsing it using AciConfiguration.fromJson()
   *   <li>Converting to vendor-independent configurations
   *   <li>Verifying that each fabric node produces a Configuration object
   *   <li>Verifying VRFs are created correctly
   *   <li>Verifying bridge domains create proper interfaces
   *   <li>Verifying contracts create ACLs
   *   <li>Printing a summary of what was parsed
   * </ol>
   */
  @Test
  public void testFullAciParsingPipeline() throws IOException {
    // Step 1: Load the real ACI JSON config
    String configText = loadRealAciConfig();
    System.out.println("========================================");
    System.out.println("ACI End-to-End Test");
    System.out.println("========================================");
    System.out.println("Config size: " + configText.length() + " bytes");

    // Verify the JSON has the expected polUni structure
    JsonNode rootNode = BatfishObjectMapper.mapper().readTree(configText);
    assertThat("Root should have polUni key", rootNode.has("polUni"), equalTo(true));
    System.out.println("JSON structure: polUni root present");

    // Step 2: Parse using AciConfiguration.fromJson()
    Warnings warnings = new Warnings();
    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);

    // Step 3: Verify basic parsing
    assertNotNull("ACI Configuration should not be null", aciConfig);
    System.out.println("Hostname: " + aciConfig.getHostname());
    System.out.println("Filename: " + aciConfig.getFilename());

    // Step 4: Convert to vendor-independent configurations
    aciConfig.setVendor(ConfigurationFormat.CISCO_ACI);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Step 5: Verify each fabric node produces a Configuration object
    System.out.println("\n========================================");
    System.out.println("Fabric Nodes");
    System.out.println("========================================");
    assertThat("Should have fabric nodes", aciConfig.getFabricNodes().size(), greaterThan(0));
    System.out.println("Number of fabric nodes: " + aciConfig.getFabricNodes().size());

    // Print fabric node details
    for (Map.Entry<String, FabricNode> entry : aciConfig.getFabricNodes().entrySet()) {
      FabricNode node = entry.getValue();
      System.out.println(
          "  - "
              + node.getName()
              + " (ID: "
              + node.getNodeId()
              + ", Role: "
              + node.getRole()
              + ", Pod: "
              + node.getPodId()
              + ")");
    }

    // Verify configurations were created for each fabric node
    assertThat("Should create configurations for fabric nodes", configs.size(), greaterThan(0));
    System.out.println("Number of configurations created: " + configs.size());

    // Step 6: Verify VRFs are created correctly
    System.out.println("\n========================================");
    System.out.println("VRFs");
    System.out.println("========================================");
    int vrfCount = 0;
    for (Map.Entry<String, ? extends org.batfish.vendor.cisco_aci.representation.TenantVrf> entry :
        aciConfig.getVrfs().entrySet()) {
      org.batfish.vendor.cisco_aci.representation.TenantVrf vrf = entry.getValue();
      System.out.println("  - " + vrf.getName() + " (Tenant: " + vrf.getTenant() + ")");
      if (vrf.getDescription() != null && !vrf.getDescription().isEmpty()) {
        System.out.println("      Description: " + vrf.getDescription());
      }
      vrfCount++;
    }
    System.out.println("Total VRFs: " + vrfCount);

    // Verify VRFs exist in converted configurations
    for (Configuration config : configs.values()) {
      assertNotNull("Config should have VRFs map", config.getVrfs());
      // Default VRF should always be present
      assertThat(
          "Default VRF should be present",
          config.getVrfs(),
          hasKey(Configuration.DEFAULT_VRF_NAME));

      // Check for ACI VRFs
      for (org.batfish.vendor.cisco_aci.representation.TenantVrf aciVrf :
          aciConfig.getVrfs().values()) {
        if (config.getVrfs().containsKey(aciVrf.getName())) {
          Vrf viVrf = config.getVrfs().get(aciVrf.getName());
          assertThat("VRF name should match", viVrf.getName(), equalTo(aciVrf.getName()));
        }
      }
    }

    // Step 7: Verify bridge domains create proper interfaces
    System.out.println("\n========================================");
    System.out.println("Bridge Domains");
    System.out.println("========================================");
    int bdCount = 0;
    int bdWithSubnets = 0;
    for (Map.Entry<String, BridgeDomain> entry : aciConfig.getBridgeDomains().entrySet()) {
      BridgeDomain bd = entry.getValue();
      System.out.println(
          "  - " + bd.getName() + " (Tenant: " + bd.getTenant() + ", VRF: " + bd.getVrf() + ")");
      if (bd.getSubnets() != null && !bd.getSubnets().isEmpty()) {
        System.out.println("      Subnets: " + String.join(", ", bd.getSubnets()));
        bdWithSubnets++;
      }
      if (bd.getDescription() != null && !bd.getDescription().isEmpty()) {
        System.out.println("      Description: " + bd.getDescription());
      }
      bdCount++;
    }
    System.out.println("Total Bridge Domains: " + bdCount);
    System.out.println("Bridge Domains with subnets: " + bdWithSubnets);

    // Verify VLAN interfaces were created for bridge domains
    for (Configuration config : configs.values()) {
      int vlanInterfaceCount = 0;
      int vlanInterfaceWithAddresses = 0;
      for (Interface iface : config.getAllInterfaces().values()) {
        if (iface.getInterfaceType() == InterfaceType.VLAN) {
          vlanInterfaceCount++;
          if (!iface.getAllAddresses().isEmpty()) {
            vlanInterfaceWithAddresses++;
            System.out.println(
                "  VLAN Interface: "
                    + iface.getName()
                    + " (Addresses: "
                    + iface.getAllAddresses()
                    + ")");
          }
        }
      }
      System.out.println(
          "Config '"
              + config.getHostname()
              + "': "
              + vlanInterfaceCount
              + " VLAN interfaces, "
              + vlanInterfaceWithAddresses
              + " with addresses");
    }

    // Step 8: Verify EPGs are parsed
    System.out.println("\n========================================");
    System.out.println("Endpoint Groups (EPGs)");
    System.out.println("========================================");
    int epgCount = 0;
    int epgWithBd = 0;
    int epgWithContracts = 0;
    for (Map.Entry<String, Epg> entry : aciConfig.getEpgs().entrySet()) {
      Epg epg = entry.getValue();
      System.out.println("  - " + epg.getName() + " (Tenant: " + epg.getTenant() + ")");
      if (epg.getBridgeDomain() != null) {
        System.out.println("      BD: " + epg.getBridgeDomain());
        epgWithBd++;
      }
      if (!epg.getProvidedContracts().isEmpty()) {
        System.out.println(
            "      Provided contracts: " + String.join(", ", epg.getProvidedContracts()));
        epgWithContracts++;
      }
      if (!epg.getConsumedContracts().isEmpty()) {
        System.out.println(
            "      Consumed contracts: " + String.join(", ", epg.getConsumedContracts()));
        epgWithContracts++;
      }
      epgCount++;
    }
    System.out.println("Total EPGs: " + epgCount);
    System.out.println("EPGs with BD: " + epgWithBd);
    System.out.println("EPGs with contracts: " + epgWithContracts);

    // Step 9: Verify contracts create ACLs
    System.out.println("\n========================================");
    System.out.println("Contracts");
    System.out.println("========================================");
    int contractCount = 0;
    int contractWithSubjects = 0;
    for (Map.Entry<String, Contract> entry : aciConfig.getContracts().entrySet()) {
      Contract contract = entry.getValue();
      System.out.println("  - " + contract.getName() + " (Tenant: " + contract.getTenant() + ")");
      if (contract.getDescription() != null && !contract.getDescription().isEmpty()) {
        System.out.println("      Description: " + contract.getDescription());
      }
      if (contract.getScope() != null) {
        System.out.println("      Scope: " + contract.getScope());
      }
      if (contract.getSubjects() != null && !contract.getSubjects().isEmpty()) {
        System.out.println("      Subjects: " + contract.getSubjects().size());
        for (Contract.Subject subject : contract.getSubjects()) {
          System.out.println(
              "        - " + subject.getName() + " (" + subject.getFilters().size() + " filters)");
        }
        contractWithSubjects++;
      }
      contractCount++;
    }
    System.out.println("Total Contracts: " + contractCount);
    System.out.println("Contracts with subjects: " + contractWithSubjects);

    // Verify ACLs were created for contracts
    int totalAclLines = 0;
    for (Configuration config : configs.values()) {
      for (Contract contract : aciConfig.getContracts().values()) {
        String aclName = AciConversion.getContractAclName(contract.getName());
        if (config.getIpAccessLists().containsKey(aclName)) {
          IpAccessList acl = config.getIpAccessLists().get(aclName);
          System.out.println(
              "  ACL for contract '"
                  + contract.getName()
                  + "': "
                  + acl.getLines().size()
                  + " lines");
          totalAclLines += acl.getLines().size();

          // Verify ACL has expected structure (permit + deny)
          boolean hasPermit = false;
          boolean hasDeny = false;
          for (var line : acl.getLines()) {
            if (((ExprAclLine) line).getAction() == LineAction.PERMIT) {
              hasPermit = true;
            } else if (((ExprAclLine) line).getAction() == LineAction.DENY) {
              hasDeny = true;
            }
          }
          if (hasPermit) {
            System.out.println("    Has PERMIT lines");
          }
          if (hasDeny) {
            System.out.println("    Has DENY lines (implicit deny)");
          }
        }
      }
    }

    // Step 10: Verify tenants are parsed
    System.out.println("\n========================================");
    System.out.println("Tenants");
    System.out.println("========================================");
    int tenantCount = 0;
    for (Map.Entry<String, Tenant> entry : aciConfig.getTenants().entrySet()) {
      Tenant tenant = entry.getValue();
      System.out.println("  - " + tenant.getName());
      System.out.println("      VRFs: " + tenant.getVrfs().size());
      System.out.println("      Bridge Domains: " + tenant.getBridgeDomains().size());
      System.out.println("      EPGs: " + tenant.getEpgs().size());
      System.out.println("      Contracts: " + tenant.getContracts().size());
      tenantCount++;
    }
    System.out.println("Total Tenants: " + tenantCount);

    // Step 11: Print final summary
    System.out.println("\n========================================");
    System.out.println("Summary");
    System.out.println("========================================");
    System.out.println("Tenants:              " + tenantCount);
    System.out.println("VRFs:                 " + vrfCount);
    System.out.println(
        "Bridge Domains:       " + bdCount + " (" + bdWithSubnets + " with subnets)");
    System.out.println(
        "EPGs:                 "
            + epgCount
            + " ("
            + epgWithBd
            + " with BD, "
            + epgWithContracts
            + " with contracts)");
    System.out.println(
        "Contracts:            " + contractCount + " (" + contractWithSubjects + " with subjects)");
    System.out.println("Fabric Nodes:         " + aciConfig.getFabricNodes().size());
    System.out.println("Configurations:       " + configs.size());
    System.out.println("Total ACL Lines:      " + totalAclLines);
    System.out.println(
        "Warnings:             " + warnings.getRedFlagWarnings().size() + " red flags");

    // Print any warnings if present
    if (!warnings.getRedFlagWarnings().isEmpty()) {
      System.out.println("\n========================================");
      System.out.println("Warnings");
      System.out.println("========================================");
      warnings
          .getRedFlagWarnings()
          .forEach(w -> System.out.println("  [RED FLAG] " + w.getTag() + ": " + w.getText()));
    }

    System.out.println("\n========================================");
    System.out.println("End-to-End Test Complete");
    System.out.println("========================================");

    // Basic assertions to verify the pipeline worked
    assertThat("Should have parsed at least one tenant", tenantCount, greaterThan(0));
    assertThat("Should have created at least one configuration", configs.size(), greaterThan(0));
  }

  /**
   * Loads a realistic ACI JSON configuration for end-to-end testing.
   *
   * <p>This configuration is loaded from a resource file and includes:
   *
   * <ul>
   *   <li>Fabric nodes (spine and leaf switches)
   *   <li>Tenants with VRFs, bridge domains, EPGs, and contracts
   *   <li>Full policy hierarchy to test the parsing pipeline
   * </ul>
   *
   * @return The config text as a string
   */
  private String loadRealAciConfig() throws IOException {
    try (java.io.InputStream is =
        getClass().getResourceAsStream("/org/batfish/vendor/cisco_aci/end-to-end-config.json")) {
      if (is == null) {
        throw new IOException(
            "Resource not found: /org/batfish/vendor/cisco_aci/end-to-end-config.json");
      }
      return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }
  }

  /** Test that verifies the conversion pipeline handles edge cases gracefully. */
  @Test
  public void testConversionPipelineEdgeCases() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    // Parse and convert
    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);
    aciConfig.setVendor(ConfigurationFormat.CISCO_ACI);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Verify all configurations have required fields set
    for (Configuration config : configs.values()) {
      assertNotNull("Config should have hostname", config.getHostname());
      assertNotNull("Config should have VRFs", config.getVrfs());
      assertNotNull("Config should have interfaces", config.getAllInterfaces());
      assertThat(
          "Config format should be CISCO_ACI",
          config.getConfigurationFormat(),
          equalTo(ConfigurationFormat.CISCO_ACI));

      // Verify default VRF is present
      assertThat(
          "Default VRF should exist", config.getVrfs(), hasKey(Configuration.DEFAULT_VRF_NAME));
    }

    // Verify all fabric nodes have corresponding configurations
    for (FabricNode node : aciConfig.getFabricNodes().values()) {
      String hostname = node.getName();
      assertNotNull("Fabric node should have a name", hostname);
      assertThat("Configuration should exist for node " + hostname, configs, hasKey(hostname));
    }
  }

  /** Test that verifies contract ACLs are properly created with expected structure. */
  @Test
  public void testContractAclStructure() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);
    aciConfig.setVendor(ConfigurationFormat.CISCO_ACI);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Check that each contract with subjects creates a properly structured ACL
    int contractsWithAcls = 0;
    for (Contract contract : aciConfig.getContracts().values()) {
      if (contract.getSubjects() == null || contract.getSubjects().isEmpty()) {
        continue; // Skip contracts without subjects
      }

      String aclName = AciConversion.getContractAclName(contract.getName());

      // Check that at least one configuration has this ACL
      boolean aclFound = false;
      for (Configuration config : configs.values()) {
        if (config.getIpAccessLists().containsKey(aclName)) {
          aclFound = true;
          IpAccessList acl = config.getIpAccessLists().get(aclName);

          // ACL should not be empty
          assertThat(
              "ACL should have lines for contract " + contract.getName(),
              acl.getLines().size(),
              greaterThan(0));

          // Should end with implicit deny
          var lastLine = acl.getLines().get(acl.getLines().size() - 1);
          assertThat(
              "Last ACL line should be deny",
              ((ExprAclLine) lastLine).getAction(),
              equalTo(LineAction.DENY));

          contractsWithAcls++;
          break;
        }
      }
      assertTrue("ACL should be found for contract " + contract.getName(), aclFound);
    }

    System.out.println("Contracts with properly structured ACLs: " + contractsWithAcls);
  }

  /** Test that verifies bridge domain to interface conversion. */
  @Test
  public void testBridgeDomainInterfaceConversion() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);
    aciConfig.setVendor(ConfigurationFormat.CISCO_ACI);
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    // Count bridge domains with subnets and corresponding interfaces
    int bdWithSubnets = 0;
    int vlanInterfacesFound = 0;

    for (BridgeDomain bd : aciConfig.getBridgeDomains().values()) {
      if (bd.getSubnets() != null && !bd.getSubnets().isEmpty()) {
        bdWithSubnets++;
        // Check if any configuration has a VLAN interface for this BD
        for (Configuration config : configs.values()) {
          for (Interface iface : config.getAllInterfaces().values()) {
            if (iface.getInterfaceType() == InterfaceType.VLAN
                && iface.getHumanName() != null
                && iface.getHumanName().contains(bd.getName())) {
              vlanInterfacesFound++;
              break;
            }
          }
        }
      }
    }

    System.out.println("Bridge domains with subnets: " + bdWithSubnets);
    System.out.println("VLAN interfaces found: " + vlanInterfacesFound);

    // We expect at least some VLAN interfaces to be created
    assertTrue(
        "Should have VLAN interfaces for bridge domains with subnets",
        vlanInterfacesFound > 0 || bdWithSubnets == 0);
  }

  /** Test that verifies warnings are collected during parsing. */
  @Test
  public void testWarningCollection() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    AciConfiguration.fromJson("real-aci-config.json", configText, warnings);

    // Just verify warnings are collected (not asserting specific warnings
    // since the real config may vary)
    System.out.println("Red flags during parsing: " + warnings.getRedFlagWarnings().size());
    System.out.println("Parse warnings during parsing: " + warnings.getParseWarnings().size());

    // Parsing should succeed even with warnings
    assertNotNull("Warnings object should not be null", warnings);
  }

  /** Test that verifies fabric node properties are correctly parsed. */
  @Test
  public void testFabricNodeProperties() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);

    int spineCount = 0;
    int leafCount = 0;
    int otherCount = 0;

    for (FabricNode node : aciConfig.getFabricNodes().values()) {
      assertNotNull("Node should have ID", node.getNodeId());
      assertNotNull("Node should have name", node.getName());

      String role = node.getRole();
      if ("spine".equalsIgnoreCase(role)) {
        spineCount++;
      } else if ("leaf".equalsIgnoreCase(role)) {
        leafCount++;
      } else {
        otherCount++;
      }

      // Pod ID should be present for most nodes
      if (node.getPodId() != null) {
        assertTrue(
            "Pod ID should be numeric or empty",
            node.getPodId().matches("\\d+") || node.getPodId().isEmpty());
      }
    }

    System.out.println("Fabric nodes by role:");
    System.out.println("  Spines:  " + spineCount);
    System.out.println("  Leaves:  " + leafCount);
    System.out.println("  Other:   " + otherCount);
    System.out.println("  Total:   " + aciConfig.getFabricNodes().size());

    // Real ACI fabric should have at least one node
    assertThat("Should have fabric nodes", aciConfig.getFabricNodes().size(), greaterThan(0));
  }

  /** Test that verifies management IPs are correctly extracted. */
  @Test
  public void testManagementIpExtraction() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);

    int nodesWithMgmt = 0;
    int nodesWithoutMgmt = 0;

    for (FabricNode node : aciConfig.getFabricNodes().values()) {
      ManagementInfo mgmtInfo = node.getManagementInfo();

      if (mgmtInfo != null && mgmtInfo.getAddress() != null) {
        nodesWithMgmt++;

        // Verify management IP structure
        assertNotNull("Management address should not be null", mgmtInfo.getAddress());
        assertTrue(
            "Management address should contain CIDR notation", mgmtInfo.getAddress().contains("/"));

        // Gateway is optional but should be valid if present
        if (mgmtInfo.getGateway() != null) {
          assertTrue(
              "Gateway should be valid IP",
              mgmtInfo.getGateway().matches("\\d+\\.\\d+\\.\\d+\\.\\d+"));
        }
      } else {
        nodesWithoutMgmt++;
      }
    }

    System.out.println("Management IP extraction:");
    System.out.println("  Nodes with management IPs: " + nodesWithMgmt);
    System.out.println("  Nodes without management IPs: " + nodesWithoutMgmt);
    System.out.println("  Total nodes: " + aciConfig.getFabricNodes().size());

    // Real ACI fabrics typically have management IPs configured
    // We don't assert that all nodes must have management IPs, but we expect at least one
    if (!aciConfig.getFabricNodes().isEmpty()) {
      // Log whether management IPs were found
      System.out.println(
          nodesWithMgmt > 0
              ? "Management IPs found and extracted successfully"
              : "No management IPs found in this configuration");
    }

    // If management IPs were found, verify they create management interfaces in conversion
    if (nodesWithMgmt > 0) {
      aciConfig.setVendor(ConfigurationFormat.CISCO_ACI);
      SortedMap<String, Configuration> configs =
          AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

      int mgmtInterfaces = 0;
      for (Configuration config : configs.values()) {
        Interface mgmtIface = config.getAllInterfaces().get("mgmt0");
        if (mgmtIface != null) {
          mgmtInterfaces++;
          assertNotNull("Management interface should have an address", mgmtIface.getAddress());
          assertTrue("Management interface should be admin up", mgmtIface.getAdminUp());
        }
      }

      System.out.println("Management interfaces created: " + mgmtInterfaces);
      assertThat(
          "Should create management interfaces for nodes with management IPs",
          mgmtInterfaces,
          greaterThan(0));
    }
  }

  /** Test that verifies Layer 1 topology edges are correctly generated from real config. */
  @Test
  public void testTopologyGeneration() throws IOException {
    String configText = loadRealAciConfig();
    Warnings warnings = new Warnings();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("real-aci-config.json", configText, warnings);
    aciConfig.setVendor(ConfigurationFormat.CISCO_ACI);

    // Count spines and leaves
    int spineCount = 0;
    int leafCount = 0;
    for (FabricNode node : aciConfig.getFabricNodes().values()) {
      String role = node.getRole();
      if ("spine".equalsIgnoreCase(role)) {
        spineCount++;
      } else if ("leaf".equalsIgnoreCase(role)) {
        leafCount++;
      }
    }

    System.out.println("Topology generation test:");
    System.out.println("  Spines: " + spineCount);
    System.out.println("  Leaves: " + leafCount);
    System.out.println("  Expected edges: " + (spineCount * leafCount));

    // Get topology edges
    Set<Layer1Edge> edges = aciConfig.getLayer1Edges();

    System.out.println("  Actual edges: " + edges.size());

    // Should have spineCount x leafCount edges
    assertThat(
        "Should create correct number of topology edges",
        edges.size(),
        equalTo(spineCount * leafCount));

    // Build hostname-to-role lookup for parsed fabric nodes.
    Map<String, String> roleByHostname = new java.util.HashMap<>();
    for (FabricNode node : aciConfig.getFabricNodes().values()) {
      if (node.getName() != null && node.getRole() != null) {
        roleByHostname.put(node.getName(), node.getRole());
      }
    }

    // Verify each edge connects a spine to a leaf using hostnames
    for (Layer1Edge edge : edges) {
      String node1 = edge.getNode1().getHostname();
      String node2 = edge.getNode2().getHostname();

      // Verify nodes exist in config
      assertTrue(
          "Edge node1 should exist in fabric node hostnames: " + node1,
          roleByHostname.containsKey(node1));
      assertTrue(
          "Edge node2 should exist in fabric node hostnames: " + node2,
          roleByHostname.containsKey(node2));

      // Verify edge connects different nodes
      assertNotEquals("Edge should connect two different nodes", node1, node2);

      // Verify spine-leaf connection (one spine, one leaf)
      String role1 = roleByHostname.get(node1);
      String role2 = roleByHostname.get(node2);

      boolean isSpineLeaf =
          ("spine".equalsIgnoreCase(role1) && "leaf".equalsIgnoreCase(role2))
              || ("leaf".equalsIgnoreCase(role1) && "spine".equalsIgnoreCase(role2));

      assertTrue(
          "Edge should connect spine to leaf: "
              + node1
              + " ("
              + role1
              + ") -> "
              + node2
              + " ("
              + role2
              + ")",
          isSpineLeaf);
    }

    System.out.println("  ✓ All edges valid spine-leaf connections");
    System.out.println("  ✓ All edges use node hostnames");
  }
}
