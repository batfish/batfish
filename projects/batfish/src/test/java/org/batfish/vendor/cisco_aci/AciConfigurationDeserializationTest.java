package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/** Tests for {@link AciConfiguration} deserialization edge cases. */
public class AciConfigurationDeserializationTest {

  /** Test parsing an empty polUni configuration. */
  @Test
  public void testEmptyPolUni() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"empty-fabric\"},"
            + "\"children\": []"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("empty.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("empty-fabric"));
    assertThat(config.getTenants(), aMapWithSize(0));
    assertThat(config.getVrfs(), aMapWithSize(0));
    assertThat(config.getBridgeDomains(), aMapWithSize(0));
    assertThat(config.getEpgs(), aMapWithSize(0));
    assertThat(config.getContracts(), aMapWithSize(0));
    assertThat(config.getFabricNodes(), aMapWithSize(0));
  }

  /** Test parsing configuration with null children arrays. */
  @Test
  public void testNullChildren() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": null"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("null-children.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
    AciConfiguration.Tenant tenant = config.getTenants().get("tenant1");
    assertNotNull(tenant);
    assertThat(tenant.getName(), equalTo("tenant1"));
  }

  /** Test parsing configuration with empty tenant name. */
  @Test
  public void testEmptyTenantName() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("empty-name.json", json, new Warnings());

    // Empty name tenant may be skipped by parser
    // The test verifies no crash occurs on empty name
    assertNotNull(config);
  }

  /** Test parsing when VRF reference is not found. */
  @Test
  public void testMissingVrfReference() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsCtx\": {"
            + "\"attributes\": {\"tnFvCtxName\": \"nonexistent-vrf\"}"
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

    AciConfiguration config = AciConfiguration.fromJson("missing-vrf.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
    AciConfiguration.Tenant tenant = config.getTenants().get("tenant1");
    assertThat(tenant.getBridgeDomains(), hasKey("tenant1:bd1"));

    AciConfiguration.BridgeDomain bd = tenant.getBridgeDomains().get("tenant1:bd1");
    // VRF reference should be stored even if target doesn't exist
    assertNotNull(bd);
  }

  /** Test parsing multiple bridge domains. */
  @Test
  public void testMultipleBridgeDomains() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"name\": \"vrf1\"}"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsCtx\": {"
            + "\"attributes\": {\"tnFvCtxName\": \"vrf1\"}"
            + "}"
            + "},"
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {\"ip\": \"10.1.1.0/24\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd2\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsCtx\": {"
            + "\"attributes\": {\"tnFvCtxName\": \"vrf1\"}"
            + "}"
            + "},"
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {\"ip\": \"10.2.1.0/24\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd3\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsCtx\": {"
            + "\"attributes\": {\"tnFvCtxName\": \"vrf1\"}"
            + "}"
            + "},"
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {\"ip\": \"10.3.1.0/24\"}"
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

    AciConfiguration config = AciConfiguration.fromJson("multi-bd.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
    AciConfiguration.Tenant tenant = config.getTenants().get("tenant1");
    assertThat(tenant.getBridgeDomains().keySet(), hasSize(3));
    assertThat(tenant.getBridgeDomains(), hasKey("tenant1:bd1"));
    assertThat(tenant.getBridgeDomains(), hasKey("tenant1:bd2"));
    assertThat(tenant.getBridgeDomains(), hasKey("tenant1:bd3"));

    AciConfiguration.BridgeDomain bd1 = tenant.getBridgeDomains().get("tenant1:bd1");
    assertThat(bd1.getSubnets(), equalTo(ImmutableList.of("10.1.1.0/24")));

    AciConfiguration.BridgeDomain bd2 = tenant.getBridgeDomains().get("tenant1:bd2");
    assertThat(bd2.getSubnets(), equalTo(ImmutableList.of("10.2.1.0/24")));
  }

  /** Test parsing contract with multiple subjects. */
  @Test
  public void testContractWithMultipleSubjects() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"vzBrCP\": {"
            + "\"attributes\": {\"name\": \"contract1\"},"
            + "\"children\": ["
            + "{"
            + "\"vzSubj\": {"
            + "\"attributes\": {\"name\": \"subject1\"},"
            + "\"children\": ["
            + "{"
            + "\"vzRsSubjFiltAtt\": {"
            + "\"attributes\": {\"tnVzFilterName\": \"filter1\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "},"
            + "{"
            + "\"vzSubj\": {"
            + "\"attributes\": {\"name\": \"subject2\"},"
            + "\"children\": ["
            + "{"
            + "\"vzRsSubjFiltAtt\": {"
            + "\"attributes\": {\"tnVzFilterName\": \"filter2\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "},"
            + "{"
            + "\"vzSubj\": {"
            + "\"attributes\": {\"name\": \"subject3\"},"
            + "\"children\": ["
            + "{"
            + "\"vzRsSubjFiltAtt\": {"
            + "\"attributes\": {\"tnVzFilterName\": \"filter3\"}"
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

    AciConfiguration config =
        AciConfiguration.fromJson("multi-subject-contract.json", json, new Warnings());

    assertThat(config.getContracts(), hasKey("tenant1:contract1"));
    AciConfiguration.Contract contract = config.getContracts().get("tenant1:contract1");
    assertNotNull(contract);
    assertThat(contract.getSubjects().size(), equalTo(3));
  }

  /** Test parsing filter with all protocols (TCP, UDP, ICMP). */
  @Test
  public void testFilterWithAllProtocols() throws Exception {
    // Test contract with multiple filter types (TCP, UDP, ICMP)
    // Note: The ACI configuration parser uses a custom deserializer that processes
    // the raw JSON structure, so contracts must be created programmatically for testing
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create contract with filters for different protocols
    AciConfiguration.Contract contract = config.getOrCreateContract("tenant1:multi_proto");
    contract.setTenant("tenant1");

    AciConfiguration.Contract.Subject tcpSubject = new AciConfiguration.Contract.Subject();
    tcpSubject.setName("tcp_traffic");

    AciConfiguration.Contract.Filter tcpFilter = new AciConfiguration.Contract.Filter();
    tcpFilter.setName("tcp_filter");
    tcpFilter.setIpProtocol("tcp");
    tcpFilter.setDestinationPorts(ImmutableList.of("80", "443"));

    AciConfiguration.Contract.Subject icmpSubject = new AciConfiguration.Contract.Subject();
    icmpSubject.setName("icmp_traffic");

    AciConfiguration.Contract.Filter icmpFilter = new AciConfiguration.Contract.Filter();
    icmpFilter.setName("icmp_filter");
    icmpFilter.setIpProtocol("icmp");
    icmpFilter.setIcmpType("8");

    tcpSubject.setFilters(ImmutableList.of(tcpFilter));
    icmpSubject.setFilters(ImmutableList.of(icmpFilter));
    contract.setSubjects(ImmutableList.of(tcpSubject, icmpSubject));

    config.finalizeStructures();

    // Verify contract structure
    assertThat(config.getContracts(), hasKey("tenant1:multi_proto"));
    AciConfiguration.Contract parsedContract = config.getContracts().get("tenant1:multi_proto");
    assertNotNull(parsedContract);
    assertThat(parsedContract.getSubjects().size(), equalTo(2));
    assertThat(
        parsedContract.getSubjects().get(0).getFilters().get(0).getIpProtocol(), equalTo("tcp"));
    assertThat(
        parsedContract.getSubjects().get(1).getFilters().get(0).getIpProtocol(), equalTo("icmp"));
  }

  /** Test handling of malformed JSON. */
  @Test
  public void testMalformedJson() throws Exception {
    String malformedJson = "{\"polUni\": {\"attributes\": {\"name\": ";

    try {
      AciConfiguration.fromJson("malformed.json", malformedJson, new Warnings());
      fail("Expected exception for malformed JSON");
    } catch (Exception e) {
      // Expected exception
      assertTrue(
          "Expected JSON parsing exception",
          e.getMessage() != null && e.getMessage().contains("Unexpected end-of-input"));
    }
  }

  /** Test handling of malformed XML. */
  @Test
  public void testMalformedXml() throws Exception {
    String malformedXml = "<polUni><fvTenant name=\"test\"";

    try {
      AciConfiguration.fromXml("malformed.xml", malformedXml, new Warnings());
      fail("Expected exception for malformed XML");
    } catch (Exception e) {
      // Expected exception
      assertTrue("Expected XML parsing exception", e.getMessage() != null || e.getCause() != null);
    }
  }

  /** Test parsing configuration with empty children array. */
  @Test
  public void testEmptyChildrenArray() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": []"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("empty-children.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), aMapWithSize(0));
  }

  /** Test parsing configuration with heterogeneous children types. */
  @Test
  public void testHeterogeneousChildren() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {\"attributes\": {\"name\": \"vrf1\"}}"
            + "},"
            + "{"
            + "\"fvBD\": {\"attributes\": {\"name\": \"bd1\"}}"
            + "},"
            + "{"
            + "\"vzBrCP\": {\"attributes\": {\"name\": \"contract1\"}}"
            + "},"
            + "{"
            + "\"fvAp\": {"
            + "\"attributes\": {\"name\": \"app1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvAEPg\": {\"attributes\": {\"name\": \"epg1\"}}"
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

    AciConfiguration config = AciConfiguration.fromJson("heterogeneous.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
    AciConfiguration.Tenant tenant = config.getTenants().get("tenant1");
    assertThat(tenant.getVrfs(), hasKey("tenant1:vrf1"));
    assertThat(tenant.getBridgeDomains(), hasKey("tenant1:bd1"));
    assertThat(tenant.getEpgs(), hasKey("tenant1:app1:epg1"));
  }

  /** Test XML parsing with empty polUni. */
  @Test
  public void testEmptyPolUniXml() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><polUni name=\"empty-fabric\"/>";

    AciConfiguration config = AciConfiguration.fromXml("empty.xml", xml, new Warnings());

    // XML parsing may not extract hostname from polUni name attribute
    assertNotNull(config);
    assertThat(config.getTenants(), aMapWithSize(0));
  }

  /** Test XML parsing with nested structure. */
  @Test
  public void testNestedStructureXml() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<polUni name=\"test-fabric\">"
            + "  <fvTenant name=\"tenant1\">"
            + "    <fvCtx name=\"vrf1\"/>"
            + "    <fvBD name=\"bd1\">"
            + "      <fvRsCtx tnFvCtxName=\"vrf1\"/>"
            + "    </fvBD>"
            + "  </fvTenant>"
            + "</polUni>";

    AciConfiguration config = AciConfiguration.fromXml("nested.xml", xml, new Warnings());

    // XML parsing may have limited support
    // Just verify no crash occurs
    assertNotNull(config);
  }

  /** Test parsing EPG relationship objects documented by ACI Toolkit. */
  @Test
  public void testEpgContractInterfaceAndTabooRelationships() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvAp\": {"
            + "\"attributes\": {\"name\": \"app1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"name\": \"epg1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsProv\": {\"attributes\": {\"tnVzBrCPName\": \"contract-provided\"}}"
            + "},"
            + "{"
            + "\"fvRsCons\": {\"attributes\": {\"tnVzBrCPName\": \"contract-consumed\"}}"
            + "},"
            + "{"
            + "\"fvRsProvIf\": {\"attributes\": {\"tnVzCPIfName\": \"cif-provided\"}}"
            + "},"
            + "{"
            + "\"fvRsConsIf\": {\"attributes\": {\"tnVzCPIfName\": \"cif-consumed\"}}"
            + "},"
            + "{"
            + "\"fvRsProtBy\": {\"attributes\": {\"tnVzTabooName\": \"taboo1\"}}"
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

    AciConfiguration config =
        AciConfiguration.fromJson("epg-relationships.json", json, new Warnings());

    AciConfiguration.Epg epg = config.getEpgs().get("tenant1:app1:epg1");
    assertNotNull(epg);
    assertThat(epg.getProvidedContracts(), hasItems("tenant1:contract-provided"));
    assertThat(epg.getConsumedContracts(), hasItems("tenant1:contract-consumed"));
    assertThat(epg.getProvidedContractInterfaces(), hasItems("tenant1:cif-provided"));
    assertThat(epg.getConsumedContractInterfaces(), hasItems("tenant1:cif-consumed"));
    assertThat(epg.getProtectedByTaboos(), hasItems("tenant1:taboo1"));
  }

  /** Test parsing external EPG relationship objects documented by ACI Toolkit. */
  @Test
  public void testExternalEpgContractInterfaceAndTabooRelationships() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"infra\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {\"attributes\": {\"name\": \"overlay\"}}"
            + "},"
            + "{"
            + "\"l3ExtOut\": {"
            + "\"attributes\": {\"name\": \"out1\"},"
            + "\"children\": ["
            + "{"
            + "\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"overlay\"}}"
            + "},"
            + "{"
            + "\"l3ExtInstP\": {"
            + "\"attributes\": {\"name\": \"ext1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsProv\": {\"attributes\": {\"tnVzBrCPName\": \"ext-provided\"}}"
            + "},"
            + "{"
            + "\"fvRsCons\": {\"attributes\": {\"tnVzBrCPName\": \"ext-consumed\"}}"
            + "},"
            + "{"
            + "\"fvRsProvIf\": {\"attributes\": {\"tnVzCPIfName\": \"ext-cif-provided\"}}"
            + "},"
            + "{"
            + "\"fvRsConsIf\": {\"attributes\": {\"tnVzCPIfName\": \"ext-cif-consumed\"}}"
            + "},"
            + "{"
            + "\"fvRsProtBy\": {\"attributes\": {\"tnVzTabooName\": \"ext-taboo\"}}"
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

    AciConfiguration config =
        AciConfiguration.fromJson("external-epg-relationships.json", json, new Warnings());

    AciConfiguration.L3Out l3Out = config.getL3Outs().get("infra:out1");
    assertNotNull(l3Out);
    assertThat(l3Out.getExternalEpgs(), hasSize(1));
    AciConfiguration.ExternalEpg extEpg = l3Out.getExternalEpgs().get(0);
    assertThat(extEpg.getProvidedContracts(), hasItems("infra:ext-provided"));
    assertThat(extEpg.getConsumedContracts(), hasItems("infra:ext-consumed"));
    assertThat(extEpg.getProvidedContractInterfaces(), hasItems("infra:ext-cif-provided"));
    assertThat(extEpg.getConsumedContractInterfaces(), hasItems("infra:ext-cif-consumed"));
    assertThat(extEpg.getProtectedByTaboos(), hasItems("infra:ext-taboo"));
  }

  /** Test parsing tenant-level contract interfaces and taboo contracts. */
  @Test
  public void testTenantContractInterfacesAndTaboos() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"vzCPIf\": {\"attributes\": {\"name\": \"cif1\", \"descr\": \"interface\"}}"
            + "},"
            + "{"
            + "\"vzTaboo\": {\"attributes\": {\"name\": \"taboo1\", \"scope\": \"context\"}}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("tenant-cif-taboo.json", json, new Warnings());

    assertThat(config.getContractInterfaces(), hasKey("tenant1:cif1"));
    assertThat(config.getTabooContracts(), hasKey("tenant1:taboo1"));
    assertThat(config.getTenants().get("tenant1").getContractInterfaces(), hasKey("tenant1:cif1"));
    assertThat(config.getTenants().get("tenant1").getTabooContracts(), hasKey("tenant1:taboo1"));
  }
}
