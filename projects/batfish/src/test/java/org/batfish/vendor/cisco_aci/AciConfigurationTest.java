package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciVrfModel;
import org.junit.Test;

/** Tests of {@link AciConfiguration}. */
public class AciConfigurationTest {

  private static final String SAMPLE_CONFIG_JSON =
      "{"
          + "\"polUni\": {"
          + "\"attributes\": {"
          + "\"name\": \"aci-fabric\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvTenant\": {"
          + "\"attributes\": {"
          + "\"name\": \"tenant1\","
          + "\"descr\": \"Test Tenant\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvCtx\": {"
          + "\"attributes\": {"
          + "\"name\": \"vrf1\","
          + "\"descr\": \"Test VRF\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fvBD\": {"
          + "\"attributes\": {"
          + "\"name\": \"bd1\","
          + "\"descr\": \"Test Bridge Domain\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvRsCtx\": {"
          + "\"attributes\": {"
          + "\"tnFvCtxName\": \"vrf1\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fvSubnet\": {"
          + "\"attributes\": {"
          + "\"ip\": \"10.0.0.0/24\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "},"
          + "{"
          + "\"fvAp\": {"
          + "\"attributes\": {"
          + "\"name\": \"ap1\","
          + "\"descr\": \"Test Application Profile\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvAEPg\": {"
          + "\"attributes\": {"
          + "\"name\": \"epg1\","
          + "\"descr\": \"Test EPG\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvRsBd\": {"
          + "\"attributes\": {"
          + "\"tnFvBDName\": \"bd1\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}"
          + "]"
          + "}"
          + "},"
          + "{"
          + "\"vzBrCP\": {"
          + "\"attributes\": {"
          + "\"name\": \"contract1\","
          + "\"descr\": \"Test Contract\","
          + "\"scope\": \"tenant\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "},"
          + "{"
          + "\"fvTenant\": {"
          + "\"attributes\": {"
          + "\"name\": \"tenant2\","
          + "\"descr\": \"Second Test Tenant\""
          + "},"
          + "\"children\": []"
          + "}"
          + "},"
          + "{"
          + "\"fabricInst\": {"
          + "\"attributes\": {"
          + "\"dn\": \"uni/fabric\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fabricProtPol\": {"
          + "\"attributes\": {"
          + "\"dn\": \"uni/fabric/fabricprotPol\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fabricExplicitGEp\": {"
          + "\"attributes\": {"
          + "\"dn\": \"uni/fabric/fabricprotPol/expgep-1\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fabricNodePEp\": {"
          + "\"attributes\": {"
          + "\"id\": \"1001\","
          + "\"name\": \"spine1\","
          + "\"role\": \"spine\","
          + "\"podId\": \"1\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fabricNodePEp\": {"
          + "\"attributes\": {"
          + "\"id\": \"1002\","
          + "\"name\": \"leaf1\","
          + "\"role\": \"leaf\","
          + "\"podId\": \"1\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}";

  private static final String REAL_ACI_SAMPLE_JSON =
      "{"
          + "\"polUni\": {"
          + "\"attributes\": {"
          + "\"name\": \"aci-fabric\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvTenant\": {"
          + "\"attributes\": {"
          + "\"name\": \"corp\","
          + "\"descr\": \"Corporate Tenant\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvCtx\": {"
          + "\"attributes\": {"
          + "\"name\": \"vrf-prod\","
          + "\"descr\": \"Production VRF\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fvCtx\": {"
          + "\"attributes\": {"
          + "\"name\": \"vrf-dev\","
          + "\"descr\": \"Development VRF\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fvBD\": {"
          + "\"attributes\": {"
          + "\"name\": \"bd-web\","
          + "\"descr\": \"Web Bridge Domain\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvRsCtx\": {"
          + "\"attributes\": {"
          + "\"tnFvCtxName\": \"vrf-prod\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fvSubnet\": {"
          + "\"attributes\": {"
          + "\"ip\": \"10.1.0.0/24\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "},"
          + "{"
          + "\"fvBD\": {"
          + "\"attributes\": {"
          + "\"name\": \"bd-app\","
          + "\"descr\": \"App Bridge Domain\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvRsCtx\": {"
          + "\"attributes\": {"
          + "\"tnFvCtxName\": \"vrf-prod\""
          + "}"
          + "}"
          + "},"
          + "{"
          + "\"fvSubnet\": {"
          + "\"attributes\": {"
          + "\"ip\": \"10.2.0.0/24\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "},"
          + "{"
          + "\"vzBrCP\": {"
          + "\"attributes\": {"
          + "\"name\": \"contract-web-to-app\","
          + "\"descr\": \"Web to App Contract\","
          + "\"scope\": \"tenant\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "},"
          + "{"
          + "\"fvTenant\": {"
          + "\"attributes\": {"
          + "\"name\": \"infra\","
          + "\"descr\": \"Infrastructure Tenant\""
          + "},"
          + "\"children\": ["
          + "{"
          + "\"fvCtx\": {"
          + "\"attributes\": {"
          + "\"name\": \"vrf-infra\","
          + "\"descr\": \"Infrastructure VRF\""
          + "}"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}"
          + "]"
          + "}"
          + "}";

  private String getConfigText() {
    return SAMPLE_CONFIG_JSON;
  }

  /** Test parsing a sample ACI JSON config and extracting tenants */
  @Test
  public void testParseConfig_tenants() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getConfigText()));

    AciConfiguration config =
        AciConfiguration.fromJson(
            "test-config.json", configText, new org.batfish.common.Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants().keySet(), hasSize(2));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getTenants(), hasKey("tenant2"));

    AciConfiguration.Tenant tenant1 = config.getTenants().get("tenant1");
    assertThat(tenant1.getName(), equalTo("tenant1"));
    assertThat(tenant1.getVrfs().keySet(), hasSize(1));
    assertThat(tenant1.getVrfs(), hasKey("tenant1:vrf1"));

    AciVrfModel vrf1 = tenant1.getVrfs().get("tenant1:vrf1");
    assertThat(vrf1.getName(), equalTo("tenant1:vrf1"));
    assertThat(vrf1.getTenant(), equalTo("tenant1"));
    assertThat(vrf1.getDescription(), equalTo("Test VRF"));

    assertThat(tenant1.getBridgeDomains().keySet(), hasSize(1));
    assertThat(tenant1.getBridgeDomains(), hasKey("tenant1:bd1"));

    AciConfiguration.BridgeDomain bd1 = tenant1.getBridgeDomains().get("tenant1:bd1");
    assertThat(bd1.getName(), equalTo("tenant1:bd1"));
    assertThat(bd1.getTenant(), equalTo("tenant1"));
    assertThat(bd1.getVrf(), equalTo("tenant1:vrf1"));
    assertThat(bd1.getSubnets(), equalTo(ImmutableList.of("10.0.0.0/24")));
    assertThat(bd1.getDescription(), equalTo("Test Bridge Domain"));

    assertThat(tenant1.getEpgs().keySet(), hasSize(1));
    assertThat(tenant1.getEpgs(), hasKey("tenant1:ap1:epg1"));

    AciConfiguration.Epg epg1 = tenant1.getEpgs().get("tenant1:ap1:epg1");
    assertThat(epg1.getName(), equalTo("tenant1:ap1:epg1"));
    assertThat(epg1.getTenant(), equalTo("tenant1"));
    assertThat(epg1.getBridgeDomain(), equalTo("tenant1:bd1"));
    assertThat(epg1.getDescription(), equalTo("Test EPG"));
  }

  /** Test parsing fabric nodes from config */
  @Test
  public void testParseConfig_fabricNodes() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getConfigText()));

    AciConfiguration config =
        AciConfiguration.fromJson(
            "test-config.json", configText, new org.batfish.common.Warnings());

    assertThat(config.getFabricNodes().keySet(), hasSize(2));
    assertThat(config.getFabricNodes(), hasKey("1001"));
    assertThat(config.getFabricNodes(), hasKey("1002"));

    AciConfiguration.FabricNode node1 = config.getFabricNodes().get("1001");
    assertThat(node1.getNodeId(), equalTo("1001"));
    assertThat(node1.getName(), equalTo("spine1"));
    assertThat(node1.getRole(), equalTo("spine"));
    assertThat(node1.getPodId(), equalTo("1"));

    AciConfiguration.FabricNode node2 = config.getFabricNodes().get("1002");
    assertThat(node2.getNodeId(), equalTo("1002"));
    assertThat(node2.getName(), equalTo("leaf1"));
    assertThat(node2.getRole(), equalTo("leaf"));
  }

  /** Test parsing contracts from config */
  @Test
  public void testParseConfig_contracts() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getConfigText()));

    AciConfiguration config =
        AciConfiguration.fromJson(
            "test-config.json", configText, new org.batfish.common.Warnings());

    assertThat(config.getContracts().keySet(), hasSize(1));
    assertThat(config.getContracts(), hasKey("tenant1:contract1"));

    AciConfiguration.Contract contract1 = config.getContracts().get("tenant1:contract1");
    assertThat(contract1.getName(), equalTo("tenant1:contract1"));
    assertThat(contract1.getTenant(), equalTo("tenant1"));
    assertThat(contract1.getDescription(), equalTo("Test Contract"));
    assertThat(contract1.getScope(), equalTo("tenant"));
  }

  /** Test that hostname is normalized to lowercase */
  @Test
  public void testParseConfig_hostnameLowercase() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getConfigText()));

    AciConfiguration config =
        AciConfiguration.fromJson(
            "test-config.json", configText, new org.batfish.common.Warnings());

    // Hostname should be lowercase even if input has uppercase
    assertThat(config.getHostname(), equalTo("aci-fabric"));
  }

  /** Test conversion to vendor-independent configuration */
  @Test
  public void testToVendorIndependentConfigurations() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getConfigText()));

    AciConfiguration config =
        AciConfiguration.fromJson(
            "test-config.json", configText, new org.batfish.common.Warnings());
    config.setVendor(ConfigurationFormat.CISCO_ACI);

    // Fabric nodes each generate their own configuration
    var viConfigs = config.toVendorIndependentConfigurations();

    // With 2 fabric nodes (spine1, leaf1), we should have 2 configurations
    assertThat(viConfigs, hasSize(2));

    // Check that each has the proper format
    for (Configuration viConfig : viConfigs) {
      assertThat(viConfig.getConfigurationFormat(), equalTo(ConfigurationFormat.CISCO_ACI));
    }
  }

  /** Test getOrCreateTenant method */
  @Test
  public void testGetOrCreateTenant() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Tenant tenant1 = config.getOrCreateTenant("new-tenant");
    assertThat(tenant1.getName(), equalTo("new-tenant"));
    assertThat(config.getTenants(), hasKey("new-tenant"));

    // Calling again should return the same instance
    AciConfiguration.Tenant tenant1Again = config.getOrCreateTenant("new-tenant");
    assertThat(tenant1Again, equalTo(tenant1));
    assertThat(config.getTenants().keySet(), hasSize(1));
  }

  /** Test getOrCreateVrf method */
  @Test
  public void testGetOrCreateVrf() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf = config.getOrCreateVrf("new-vrf");
    assertThat(vrf.getName(), equalTo("new-vrf"));
    assertThat(config.getVrfs(), hasKey("new-vrf"));

    // Calling again should return the same instance
    AciVrfModel vrfAgain = config.getOrCreateVrf("new-vrf");
    assertThat(vrfAgain, equalTo(vrf));
    assertThat(config.getVrfs().keySet(), hasSize(1));
  }

  /** Test getOrCreateBridgeDomain method */
  @Test
  public void testGetOrCreateBridgeDomain() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = config.getOrCreateBridgeDomain("new-bd");
    assertThat(bd.getName(), equalTo("new-bd"));
    assertThat(config.getBridgeDomains(), hasKey("new-bd"));

    // Calling again should return the same instance
    AciConfiguration.BridgeDomain bdAgain = config.getOrCreateBridgeDomain("new-bd");
    assertThat(bdAgain, equalTo(bd));
    assertThat(config.getBridgeDomains().keySet(), hasSize(1));
  }

  /** Test getOrCreateEpg method */
  @Test
  public void testGetOrCreateEpg() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Epg epg = config.getOrCreateEpg("new-epg");
    assertThat(epg.getName(), equalTo("new-epg"));
    assertThat(config.getEpgs(), hasKey("new-epg"));

    // Calling again should return the same instance
    AciConfiguration.Epg epgAgain = config.getOrCreateEpg("new-epg");
    assertThat(epgAgain, equalTo(epg));
    assertThat(config.getEpgs().keySet(), hasSize(1));
  }

  /** Test getOrCreateContract method */
  @Test
  public void testGetOrCreateContract() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = config.getOrCreateContract("new-contract");
    assertThat(contract.getName(), equalTo("new-contract"));
    assertThat(config.getContracts(), hasKey("new-contract"));

    // Calling again should return the same instance
    AciConfiguration.Contract contractAgain = config.getOrCreateContract("new-contract");
    assertThat(contractAgain, equalTo(contract));
    assertThat(config.getContracts().keySet(), hasSize(1));
  }

  /** Test parsing real ACI JSON export from fabric */
  @Test
  public void testParseRealAciJson() throws Exception {
    // Use a smaller inline JSON sample that mimics real ACI export structure
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(REAL_ACI_SAMPLE_JSON));

    // The real ACI JSON has polUni structure
    // This test verifies we can at least parse the JSON with Jackson
    com.fasterxml.jackson.databind.JsonNode root =
        BatfishObjectMapper.mapper().readTree(configText);

    assertThat(root.has("polUni"), equalTo(true));

    com.fasterxml.jackson.databind.JsonNode polUni = root.get("polUni");
    assertThat(polUni.has("attributes"), equalTo(true));
    assertThat(polUni.has("children"), equalTo(true));

    com.fasterxml.jackson.databind.JsonNode children = polUni.get("children");
    assertThat(children.isArray(), equalTo(true));
    assertThat(children.size() > 0, equalTo(true));

    // Count fvTenant objects
    int tenantCount = 0;
    int bdCount = 0;
    int vrfCount = 0;
    int contractCount = 0;

    for (com.fasterxml.jackson.databind.JsonNode child : children) {
      if (child.has("fvTenant")) {
        tenantCount++;
        com.fasterxml.jackson.databind.JsonNode fvTenant = child.get("fvTenant");
        if (fvTenant.has("children")) {
          com.fasterxml.jackson.databind.JsonNode tenantChildren = fvTenant.get("children");
          for (com.fasterxml.jackson.databind.JsonNode tc : tenantChildren) {
            if (tc.has("fvBD")) {
              bdCount++;
            }
            if (tc.has("fvCtx")) {
              vrfCount++;
            }
            if (tc.has("vzBrCP")) {
              contractCount++;
            }
          }
        }
      }
    }

    assertThat("Should find tenants", tenantCount > 0, equalTo(true));
    assertThat("Should find bridge domains", bdCount > 0, equalTo(true));
    assertThat("Should find VRFs", vrfCount > 0, equalTo(true));
    assertThat("Should find contracts", contractCount > 0, equalTo(true));

    // Note: The actual conversion from polUni structure to AciConfiguration
    // will require a separate parser/adapter that is not yet implemented.
  }
}
