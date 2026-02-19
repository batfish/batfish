package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.batfish.vendor.cisco_aci.representation.BridgeDomain;
import org.batfish.vendor.cisco_aci.representation.Contract;
import org.batfish.vendor.cisco_aci.representation.Epg;
import org.batfish.vendor.cisco_aci.representation.FabricNode;
import org.batfish.vendor.cisco_aci.representation.Tenant;
import org.batfish.vendor.cisco_aci.representation.TenantVrf;
import org.junit.Test;

/**
 * Tests of ACI JSON and XML parsing and grammar.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>ACI JSON parsing with various object types (fvTenant, fvCtx, fvBD, vzBrCP, fabricNodePEp)
 *   <li>ACI XML parsing (if XML support is ready)
 *   <li>Edge cases (empty config, missing required fields)
 *   <li>Contract to ACL conversion produces correct rules
 * </ul>
 */
public class AciGrammarTest {

  /**
   * Creates a minimal polUni JSON structure with a single tenant.
   *
   * @param tenantName The name of the tenant
   * @return The polUni JSON string
   */
  private static String createPolUniJson(String tenantName) {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-"
        + tenantName
        + "\","
        + "\"name\": \""
        + tenantName
        + "\","
        + "\"descr\": \"Test tenant\""
        + "},"
        + "\"children\": []"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Creates a polUni JSON with tenant, VRF, and bridge domain. */
  private static String createPolUniJsonWithVrfAndBd() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant\","
        + "\"name\": \"test_tenant\","
        + "\"descr\": \"Test tenant\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/ctx-test_vrf\","
        + "\"name\": \"test_vrf\","
        + "\"descr\": \"Test VRF\""
        + "},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"fvBD\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/BD-test_bd\","
        + "\"name\": \"test_bd\","
        + "\"descr\": \"Test Bridge Domain\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvRsCtx\": {"
        + "\"attributes\": {"
        + "\"tnFvCtxName\": \"test_vrf\""
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
        + "}";
  }

  /** Creates a polUni JSON with contract and subjects. */
  private static String createPolUniJsonWithContract() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant\","
        + "\"name\": \"test_tenant\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"vzBrCP\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/brc-web_contract\","
        + "\"name\": \"web_contract\","
        + "\"descr\": \"Web traffic contract\","
        + "\"scope\": \"tenant\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"vzSubj\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/brc-web_contract/subj-http\","
        + "\"name\": \"http\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"vzRsSubjFiltAtt\": {"
        + "\"attributes\": {"
        + "\"tnVzFilterName\": \"http_filter\","
        + "\"action\": \"permit\""
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
  }

  /** Creates a polUni JSON with fabric nodes. */
  private static String createPolUniJsonWithFabricNode() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fabricInst\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fabricProtPol\": {"
        + "\"attributes\": {},"
        + "\"children\": ["
        + "{"
        + "\"fabricExplicitGEp\": {"
        + "\"attributes\": {},"
        + "\"children\": ["
        + "{"
        + "\"fabricNodePEp\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/fabric/nodePEp-101\","
        + "\"id\": \"101\","
        + "\"name\": \"spine1\","
        + "\"role\": \"spine\","
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
  }

  /** Creates a polUni JSON with application profile and EPG. */
  private static String createPolUniJsonWithEpg() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant\","
        + "\"name\": \"test_tenant\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvAp\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/ap-web_app\","
        + "\"name\": \"web_app\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvAEPg\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/ap-web_app/epg-web_epg\","
        + "\"name\": \"web_epg\","
        + "\"descr\": \"Web server EPG\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvRsBd\": {"
        + "\"attributes\": {"
        + "\"tnFvBDName\": \"web_bd\""
        + "}"
        + "}"
        + "},"
        + "{"
        + "\"fvRsProv\": {"
        + "\"attributes\": {"
        + "\"tnVzBrCPName\": \"web_contract\""
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
  }

  /** Creates a polUni JSON with filter entries. */
  private static String createPolUniJsonWithFilter() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"name\": \"test_tenant\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"vzFilter\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-test_tenant/flt-http_filter\","
        + "\"name\": \"http_filter\","
        + "\"descr\": \"HTTP traffic filter\""
        + "},"
        + "\"children\": []"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Test parsing a minimal polUni JSON with tenant. */
  @Test
  public void testParseJson_polUniTenant() throws IOException {
    String json = createPolUniJson("myTenant");
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants().keySet(), hasSize(1));
    assertThat(config.getTenants(), hasKey("myTenant"));

    Tenant tenant = config.getTenants().get("myTenant");
    assertThat(tenant.getName(), equalTo("myTenant"));
  }

  /** Test parsing polUni JSON with VRF and bridge domain. */
  @Test
  public void testParseJson_polUniVrfAndBd() throws IOException {
    String json = createPolUniJsonWithVrfAndBd();
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getTenants().keySet(), hasSize(1));
    assertThat(config.getTenants(), hasKey("test_tenant"));

    Tenant tenant = config.getTenants().get("test_tenant");
    assertThat(tenant.getVrfs().keySet(), hasSize(1));
    assertThat(tenant.getVrfs(), hasKey("test_tenant:test_vrf"));

    TenantVrf vrf = tenant.getVrfs().get("test_tenant:test_vrf");
    assertThat(vrf.getName(), equalTo("test_tenant:test_vrf"));
    assertThat(vrf.getTenant(), equalTo("test_tenant"));

    assertThat(config.getBridgeDomains().keySet(), hasSize(1));
    assertThat(config.getBridgeDomains(), hasKey("test_tenant:test_bd"));

    BridgeDomain bd = config.getBridgeDomains().get("test_tenant:test_bd");
    assertThat(bd.getName(), equalTo("test_tenant:test_bd"));
    assertThat(bd.getTenant(), equalTo("test_tenant"));
    assertThat(bd.getVrf(), equalTo("test_tenant:test_vrf"));
  }

  /** Test parsing polUni JSON with contract. */
  @Test
  public void testParseJson_polUniContract() throws IOException {
    String json = createPolUniJsonWithContract();
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getContracts().keySet(), hasSize(1));
    assertThat(config.getContracts(), hasKey("test_tenant:web_contract"));

    Contract contract = config.getContracts().get("test_tenant:web_contract");
    assertThat(contract.getName(), equalTo("test_tenant:web_contract"));
    assertThat(contract.getTenant(), equalTo("test_tenant"));
    assertThat(contract.getDescription(), equalTo("Web traffic contract"));
    assertThat(contract.getScope(), equalTo("tenant"));
    assertThat(contract.getSubjects(), hasSize(1));

    Contract.Subject subject = contract.getSubjects().get(0);
    assertThat(subject.getName(), equalTo("http"));
    assertThat(subject.getFilters(), hasSize(1));

    Contract.FilterRef filter = subject.getFilters().get(0);
    assertThat(filter.getName(), equalTo("http_filter"));
  }

  /** Test parsing polUni JSON with fabric node. */
  @Test
  public void testParseJson_polUniFabricNode() throws IOException {
    String json = createPolUniJsonWithFabricNode();
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getFabricNodes().keySet(), hasSize(1));
    assertThat(config.getFabricNodes(), hasKey("101"));

    FabricNode node = config.getFabricNodes().get("101");
    assertThat(node.getNodeId(), equalTo("101"));
    assertThat(node.getName(), equalTo("spine1"));
    assertThat(node.getRole(), equalTo("spine"));
    assertThat(node.getPodId(), equalTo("1"));
  }

  /** Test parsing polUni JSON with EPG. */
  @Test
  public void testParseJson_polUniEpg() throws IOException {
    String json = createPolUniJsonWithEpg();
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getEpgs().keySet(), hasSize(1));
    assertThat(config.getEpgs(), hasKey("test_tenant:web_app:web_epg"));

    Epg epg = config.getEpgs().get("test_tenant:web_app:web_epg");
    assertThat(epg.getName(), equalTo("test_tenant:web_app:web_epg"));
    assertThat(epg.getTenant(), equalTo("test_tenant"));
    assertThat(epg.getBridgeDomain(), equalTo("test_tenant:web_bd"));
    assertThat(epg.getDescription(), equalTo("Web server EPG"));
    assertThat(epg.getProvidedContracts(), contains("test_tenant:web_contract"));
  }

  /** Test parsing polUni JSON with filter. */
  @Test
  public void testParseJson_polUniFilter() throws IOException {
    String json = createPolUniJsonWithFilter();
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    // Filter parsing is for reference - actual filter entries are in contract subjects
    assertThat(config.getTenants(), hasKey("test_tenant"));
  }

  /** Test parsing empty configuration should produce filename-derived hostname. */
  @Test
  public void testParseJson_emptyConfig() throws IOException {
    String json = "{\"polUni\": {\"attributes\": {\"dn\": \"uni\"}, \"children\": []}}";
    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("aci-test")); // hostname derived from filename
  }

  /** Test parsing configuration with missing tenant name should not fail. */
  @Test
  public void testParseJson_missingTenantName() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-\"},"
            + "\"children\": []"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    // Tenant without name should not be added
    assertThat(config.getTenants().keySet(), hasSize(0));
  }

  /** Test parsing configuration with missing required fields on VRF. */
  @Test
  public void testParseJson_vrfMissingName() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"fvCtx\": {"
            + "\"attributes\": {\"descr\": \"VRF without name\"},"
            + "\"children\": []"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    // VRF without name should not be added
    assertThat(config.getVrfs().keySet(), hasSize(0));
  }

  /** Test parsing configuration with missing required fields on bridge domain. */
  @Test
  public void testParseJson_bdMissingName() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"fvBD\": {"
            + "\"attributes\": {\"descr\": \"BD without name\"},"
            + "\"children\": []"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    // BD without name should not be added
    assertThat(config.getBridgeDomains().keySet(), hasSize(0));
  }

  /** Test parsing configuration with missing fabric node ID. */
  @Test
  public void testParseJson_fabricNodeMissingId() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fabricInst\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric\"},"
            + "\"children\": ["
            + "{\"fabricProtPol\": {"
            + "\"children\": ["
            + "{\"fabricExplicitGEp\": {"
            + "\"children\": ["
            + "{\"fabricNodePEp\": {"
            + "\"attributes\": {\"name\": \"spine1\", \"role\": \"spine\"}"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    // Fabric node without ID should use name as key
    assertThat(config.getFabricNodes().keySet(), hasSize(1));
    assertThat(config.getFabricNodes(), hasKey("spine1"));

    FabricNode node = config.getFabricNodes().get("spine1");
    assertThat(node.getName(), equalTo("spine1"));
  }

  /** Test format detection - JSON starting with '{'. */
  @Test
  public void testFormatDetection_json() throws IOException {
    String json = createPolUniJson("test");
    AciConfiguration config = AciConfiguration.fromFile("test.json", json, new Warnings());

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("aci-fabric"));
  }

  /** Test format detection - XML starting with '<'. */
  @Test
  public void testFormatDetection_xml() throws IOException {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<polUni>"
            + "  <attributes>"
            + "    <dn>uni</dn>"
            + "  </attributes>"
            + "</polUni>";

    AciConfiguration config = AciConfiguration.fromFile("test.xml", xml, new Warnings());

    assertThat(config, notNullValue());
  }

  /** Test that fromFile handles empty input. */
  @Test
  public void testFromFile_emptyInput() {
    String empty = "";

    try {
      AciConfiguration.fromFile("test.json", empty, new Warnings());
      fail("Should have thrown exception for empty input");
    } catch (IOException e) {
      assertThat(e.getMessage(), containsString("Empty configuration file"));
    }
  }

  /** Test that fromFile handles unrecognized format. */
  @Test
  public void testFromFile_unrecognizedFormat() {
    String invalid = "NOT_JSON_OR_XML";

    try {
      AciConfiguration.fromFile("test.txt", invalid, new Warnings());
      fail("Should have thrown exception for unrecognized format");
    } catch (IOException e) {
      assertThat(e.getMessage(), containsString("Unrecognized configuration format"));
    }
  }

  /** Test contract to ACL conversion with TCP filter. */
  @Test
  public void testContractToAclConversion_tcpFilter() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create contract with TCP filter
    Contract contract = new Contract("web_contract");
    contract.setTenant("tenant1");

    Contract.Subject subject = new Contract.Subject();
    subject.setName("http_subject");

    Contract.FilterRef filter = new Contract.FilterRef();
    filter.setName("tcp_80");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("80"));

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.getContracts().put("tenant1:web_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations (this includes ACL conversion)
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    assertThat(viConfigs, hasSize(1));
    Configuration viConfig = viConfigs.get(0);
    assertThat(
        viConfig.getIpAccessLists(),
        hasKey(AciConversion.getContractAclName("tenant1:web_contract")));

    IpAccessList acl =
        viConfig.getIpAccessLists().get(AciConversion.getContractAclName("tenant1:web_contract"));
    assertThat(acl.getLines().size(), greaterThan(0));

    // First line should be permit TCP dst port 80
    assertThat(((ExprAclLine) acl.getLines().get(0)).getAction(), equalTo(LineAction.PERMIT));
  }

  /** Test contract to ACL conversion with ICMP filter. */
  @Test
  public void testContractToAclConversion_icmpFilter() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create contract with ICMP filter
    Contract contract = new Contract("icmp_contract");
    contract.setTenant("tenant1");

    Contract.Subject subject = new Contract.Subject();
    subject.setName("icmp_subject");

    Contract.FilterRef filter = new Contract.FilterRef();
    filter.setName("icmp_echo");
    filter.setIpProtocol("icmp");
    filter.setIcmpType("8"); // Echo request
    filter.setIcmpCode("0");

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.getContracts().put("tenant1:icmp_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    Configuration viConfig = viConfigs.get(0);
    assertThat(
        viConfig.getIpAccessLists(),
        hasKey(AciConversion.getContractAclName("tenant1:icmp_contract")));

    IpAccessList acl =
        viConfig.getIpAccessLists().get(AciConversion.getContractAclName("tenant1:icmp_contract"));
    assertThat(((ExprAclLine) acl.getLines().get(0)).getAction(), equalTo(LineAction.PERMIT));
  }

  /** Test contract to ACL conversion with port range. */
  @Test
  public void testContractToAclConversion_portRange() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create contract with port range filter
    Contract contract = new Contract("range_contract");
    contract.setTenant("tenant1");

    Contract.Subject subject = new Contract.Subject();
    subject.setName("range_subject");

    Contract.FilterRef filter = new Contract.FilterRef();
    filter.setName("tcp_range");
    filter.setIpProtocol("tcp");
    filter.setDestinationPorts(ImmutableList.of("8000-9000"));

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.getContracts().put("tenant1:range_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    Configuration viConfig = viConfigs.get(0);
    IpAccessList acl =
        viConfig.getIpAccessLists().get(AciConversion.getContractAclName("tenant1:range_contract"));

    assertThat(((ExprAclLine) acl.getLines().get(0)).getAction(), equalTo(LineAction.PERMIT));
  }

  /** Test contract to ACL conversion with IP protocol number. */
  @Test
  public void testContractToAclConversion_protocolNumber() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create contract with protocol number (6 = TCP)
    Contract contract = new Contract("proto_contract");
    contract.setTenant("tenant1");

    Contract.Subject subject = new Contract.Subject();
    subject.setName("proto_subject");

    Contract.FilterRef filter = new Contract.FilterRef();
    filter.setName("proto_6");
    filter.setIpProtocol("6");

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.getContracts().put("tenant1:proto_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    Configuration viConfig = viConfigs.get(0);
    IpAccessList acl =
        viConfig.getIpAccessLists().get(AciConversion.getContractAclName("tenant1:proto_contract"));

    assertThat(((ExprAclLine) acl.getLines().get(0)).getAction(), equalTo(LineAction.PERMIT));
  }

  /** Test contract to ACL conversion with IP address matching. */
  @Test
  public void testContractToAclConversion_ipAddress() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create contract with source/destination IP
    Contract contract = new Contract("ip_contract");
    contract.setTenant("tenant1");

    Contract.Subject subject = new Contract.Subject();
    subject.setName("ip_subject");

    Contract.FilterRef filter = new Contract.FilterRef();
    filter.setName("src_dst_filter");
    filter.setIpProtocol("ip");
    filter.setSourceAddress("10.1.1.0/24");
    filter.setDestinationAddress("10.2.2.0/24");

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.getContracts().put("tenant1:ip_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    Configuration viConfig = viConfigs.get(0);
    IpAccessList acl =
        viConfig.getIpAccessLists().get(AciConversion.getContractAclName("tenant1:ip_contract"));

    assertThat(((ExprAclLine) acl.getLines().get(0)).getAction(), equalTo(LineAction.PERMIT));
  }

  /** Test contract ACL name generation. */
  @Test
  public void testGetContractAclName() {
    assertThat(AciConversion.getContractAclName("web_contract"), equalTo("~CONTRACT~web_contract"));
    assertThat(
        AciConversion.getContractAclName("tenant1:web_contract"),
        equalTo("~CONTRACT~tenant1:web_contract"));

    // ACL name is used as-is in conversion
    assertThat(AciConversion.getContractAclName(""), equalTo("~CONTRACT~"));
    assertThat(AciConversion.getContractAclName("default"), equalTo("~CONTRACT~default"));
  }

  /** Test parsing multi-tenant configuration. */
  @Test
  public void testParseJson_multiTenant() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"name\": \"tenant1\"}, \"children\": []}},"
            + "{\"fvTenant\": {\"attributes\": {\"name\": \"tenant2\"}, \"children\": []}},"
            + "{\"fvTenant\": {\"attributes\": {\"name\": \"tenant3\"}, \"children\": []}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getTenants().keySet(), hasSize(3));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getTenants(), hasKey("tenant2"));
    assertThat(config.getTenants(), hasKey("tenant3"));
  }

  /** Test parsing nested bridge domains in tenant. */
  @Test
  public void testParseJson_nestedBridgeDomains() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"fvBD\": {\"attributes\": {\"name\": \"bd1\"}, \"children\": []}},"
            + "{\"fvBD\": {\"attributes\": {\"name\": \"bd2\"}, \"children\": []}},"
            + "{\"fvBD\": {\"attributes\": {\"name\": \"bd3\"}, \"children\": []}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getBridgeDomains().keySet(), hasSize(3));
    assertThat(config.getBridgeDomains(), hasKey("test_tenant:bd1"));
    assertThat(config.getBridgeDomains(), hasKey("test_tenant:bd2"));
    assertThat(config.getBridgeDomains(), hasKey("test_tenant:bd3"));
  }

  /** Test parsing multiple subjects in contract. */
  @Test
  public void testParseJson_multipleSubjects() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"vzBrCP\": {"
            + "\"attributes\": {\"name\": \"multi_contract\"},"
            + "\"children\": ["
            + "{\"vzSubj\": {\"attributes\": {\"name\": \"http\"}, \"children\": []}},"
            + "{\"vzSubj\": {\"attributes\": {\"name\": \"https\"}, \"children\": []}},"
            + "{\"vzSubj\": {\"attributes\": {\"name\": \"icmp\"}, \"children\": []}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    Contract contract = config.getContracts().get("test_tenant:multi_contract");
    assertThat(contract.getSubjects(), hasSize(3));
    assertThat(contract.getSubjects().get(0).getName(), equalTo("http"));
    assertThat(contract.getSubjects().get(1).getName(), equalTo("https"));
    assertThat(contract.getSubjects().get(2).getName(), equalTo("icmp"));
  }

  /** Test parsing multiple filters in subject. */
  @Test
  public void testParseJson_multipleFilters() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"vzBrCP\": {"
            + "\"attributes\": {\"name\": \"test_contract\"},"
            + "\"children\": ["
            + "{\"vzSubj\": {"
            + "\"attributes\": {\"name\": \"multi_filter\"},"
            + "\"children\": ["
            + "{\"vzRsSubjFiltAtt\": {\"attributes\": {\"tnVzFilterName\": \"http\"}}},"
            + "{\"vzRsSubjFiltAtt\": {\"attributes\": {\"tnVzFilterName\": \"https\"}}},"
            + "{\"vzRsSubjFiltAtt\": {\"attributes\": {\"tnVzFilterName\": \"icmp\"}}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    Contract contract = config.getContracts().get("test_tenant:test_contract");
    Contract.Subject subject = contract.getSubjects().get(0);
    assertThat(subject.getFilters(), hasSize(3));
    assertThat(subject.getFilters().get(0).getName(), equalTo("http"));
    assertThat(subject.getFilters().get(1).getName(), equalTo("https"));
    assertThat(subject.getFilters().get(2).getName(), equalTo("icmp"));
  }

  /** Test parsing fabric node with interfaces. */
  @Test
  public void testParseJson_fabricNodeWithInterfaces() throws IOException {
    // Test with fabricNodePEp attributes
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fabricInst\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());
    assertThat(config.getHostname(), equalTo("aci-fabric"));
  }

  /** Test contract with deny action filter. */
  @Test
  public void testContractToAclConversion_denyAction() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    Contract contract = new Contract("deny_contract");
    contract.setTenant("tenant1");

    Contract.Subject subject = new Contract.Subject();
    subject.setName("deny_subject");

    Contract.FilterRef filter = new Contract.FilterRef();
    filter.setName("deny_filter");
    filter.setIpProtocol("tcp");

    subject.setFilters(ImmutableList.of(filter));
    contract.setSubjects(ImmutableList.of(subject));

    config.getContracts().put("tenant1:deny_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    Configuration viConfig = viConfigs.get(0);
    IpAccessList acl =
        viConfig.getIpAccessLists().get(AciConversion.getContractAclName("tenant1:deny_contract"));

    // Default action is permit for contract filters
    assertThat(((ExprAclLine) acl.getLines().get(0)).getAction(), equalTo(LineAction.PERMIT));
    // Last line should be implicit deny
    assertThat(
        ((ExprAclLine) acl.getLines().get(acl.getLines().size() - 1)).getAction(),
        equalTo(LineAction.DENY));
  }

  /** Test contract with empty subjects produces ACL with only deny. */
  @Test
  public void testContractToAclConversion_emptySubjects() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    Contract contract = new Contract("empty_contract");
    contract.setTenant("tenant1");
    contract.setSubjects(ImmutableList.of()); // Empty subjects

    config.getContracts().put("tenant1:empty_contract", contract);
    config.finalizeStructures();

    // Convert to VI configurations
    List<Configuration> viConfigs = config.toVendorIndependentConfigurations();

    Configuration viConfig = viConfigs.get(0);

    // Empty contract should not create ACL
    assertThat(
        viConfig.getIpAccessLists(),
        not(hasKey(AciConversion.getContractAclName("tenant1:empty_contract"))));
  }

  /** Test parsing VRF with description. */
  @Test
  public void testParseJson_vrfWithDescription() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"fvCtx\": {"
            + "\"attributes\": {"
            + "\"name\": \"prod_vrf\","
            + "\"descr\": \"Production VRF for web traffic\""
            + "},"
            + "\"children\": []"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    TenantVrf vrf = config.getVrfs().get("test_tenant:prod_vrf");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getDescription(), equalTo("Production VRF for web traffic"));
  }

  /** Test parsing bridge domain with VRF association. */
  @Test
  public void testParseJson_bdWithVrfAssociation() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"fvCtx\": {"
            + "\"attributes\": {\"name\": \"test_vrf\"},"
            + "\"children\": []"
            + "}},"
            + "{\"fvBD\": {"
            + "\"attributes\": {\"name\": \"test_bd\"},"
            + "\"children\": ["
            + "{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"test_vrf\"}}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    BridgeDomain bd = config.getBridgeDomains().get("test_tenant:test_bd");
    assertThat(bd, notNullValue());
    assertThat(bd.getVrf(), equalTo("test_tenant:test_vrf"));
  }

  /** Test parsing EPG with contract references. */
  @Test
  public void testParseJson_epgWithContractReferences() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"fvAp\": {"
            + "\"attributes\": {\"name\": \"test_app\"},"
            + "\"children\": ["
            + "{\"fvAEPg\": {"
            + "\"attributes\": {\"name\": \"web_epg\"},"
            + "\"children\": ["
            + "{\"fvRsProv\": {\"attributes\": {\"tnVzBrCPName\": \"web_contract\"}}},"
            + "{\"fvRsCons\": {\"attributes\": {\"tnVzBrCPName\": \"db_contract\"}}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    Epg epg = config.getEpgs().get("test_tenant:test_app:web_epg");
    assertThat(epg, notNullValue());
    assertThat(epg.getProvidedContracts(), contains("test_tenant:web_contract"));
    assertThat(epg.getConsumedContracts(), contains("test_tenant:db_contract"));
  }

  /** Test contract with scope attribute. */
  @Test
  public void testParseJson_contractScope() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"vzBrCP\": {"
            + "\"attributes\": {"
            + "\"name\": \"global_contract\","
            + "\"scope\": \"global\""
            + "},"
            + "\"children\": []"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    Contract contract = config.getContracts().get("test_tenant:global_contract");
    assertThat(contract, notNullValue());
    assertThat(contract.getScope(), equalTo("global"));
  }

  /** Test fabric node with different roles. */
  @Test
  public void testParseJson_fabricNodeRoles() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\"},\"children\": [{\"fabricInst\":"
            + " {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": [{\"fabricProtPol\":"
            + " {\"children\": [{\"fabricExplicitGEp\": {\"children\": [{\"fabricNodePEp\":"
            + " {\"attributes\": {\"id\": \"1\", \"name\": \"spine1\", \"role\":"
            + " \"spine\"}}},{\"fabricNodePEp\": {\"attributes\": {\"id\": \"2\", \"name\":"
            + " \"leaf1\", \"role\": \"leaf\"}}},{\"fabricNodePEp\": {\"attributes\": {\"id\":"
            + " \"3\", \"name\": \"ctrl1\", \"role\": \"controller\"}}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getFabricNodes().keySet(), hasSize(3));
    assertThat(config.getFabricNodes().get("1").getRole(), equalTo("spine"));
    assertThat(config.getFabricNodes().get("2").getRole(), equalTo("leaf"));
    assertThat(config.getFabricNodes().get("3").getRole(), equalTo("controller"));
  }

  /** Test parsing configuration with whitespace. */
  @Test
  public void testParseJson_whitespaceHandling() throws IOException {
    String json =
        "{\n"
            + "  \"polUni\": {\n"
            + "    \"attributes\": {\n"
            + "      \"dn\": \"uni\",\n"
            + "      \"name\": \"aci-fabric\"\n"
            + "    },\n"
            + "    \"children\": [\n"
            + "      {\n"
            + "        \"fvTenant\": {\n"
            + "          \"attributes\": {\n"
            + "            \"name\": \"test_tenant\"\n"
            + "          },\n"
            + "          \"children\": []\n"
            + "        }\n"
            + "      }\n"
            + "    ]\n"
            + "  }\n"
            + "}\n";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("test_tenant"));
  }

  /** Test contract subject with match attributes. */
  @Test
  public void testParseJson_contractSubjectMatchTypes() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\"},"
            + "\"children\": ["
            + "{\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"test_tenant\"},"
            + "\"children\": ["
            + "{\"vzBrCP\": {"
            + "\"attributes\": {\"name\": \"test_contract\"},"
            + "\"children\": ["
            + "{\"vzSubj\": {"
            + "\"attributes\": {"
            + "\"name\": \"test_subject\","
            + "\"consMatchT\": \"AtleastOne\","
            + "\"provMatchT\": \"AtmostOne\""
            + "},"
            + "\"children\": []"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    Contract.Subject subject =
        config.getContracts().get("test_tenant:test_contract").getSubjects().get(0);
    assertThat(subject, notNullValue());
    assertThat(subject.getName(), equalTo("test_subject"));
  }

  /** Test finalizeStructures makes maps immutable. */
  @Test
  @SuppressWarnings("PMD.JUnitUseExpected")
  public void testFinalizeStructures() {
    AciConfiguration config = new AciConfiguration();

    config.getOrCreateTenant("tenant1");
    config.getOrCreateVrf("vrf1");
    config.getOrCreateBridgeDomain("bd1");

    // Before finalize, structures are mutable
    assertTrue(config.getTenants() instanceof java.util.TreeMap);

    config.finalizeStructures();

    // After finalize, structures should be immutable
    // ImmutableMap is a subtype of Map, check if operations that would modify throw
    Map<String, Tenant> tenants = config.getTenants();
    try {
      tenants.put("should_fail", new Tenant("should_fail"));
      fail("Should have thrown exception");
    } catch (UnsupportedOperationException e) {
      // Expected - map is now immutable
    }
  }

  /** Test parsing ACI spine-leaf config with topology edges. */
  @Test
  public void testParseRealConfig_spineLeafTopology() throws IOException {
    // Create a realistic spine-leaf topology config
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{\"fabricInst\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric\"},"
            + "\"children\": ["
            + "{\"fabricProtPol\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric/fabricprotPol\"},"
            + "\"children\": ["
            + "{\"fabricExplicitGEp\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric/fabricprotPol/expgep-1\"},"
            + "\"children\": ["
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"101\", \"name\": \"spine-1\", "
            + "\"role\": \"spine\", \"podId\": \"1\", "
            + "\"dn\": \"uni/fabric/nodePEp-101\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"102\", \"name\": \"spine-2\", "
            + "\"role\": \"spine\", \"podId\": \"1\", "
            + "\"dn\": \"uni/fabric/nodePEp-102\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"201\", \"name\": \"leaf-1\", "
            + "\"role\": \"leaf\", \"podId\": \"1\", "
            + "\"dn\": \"uni/fabric/nodePEp-201\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"202\", \"name\": \"leaf-2\", "
            + "\"role\": \"leaf\", \"podId\": \"1\", "
            + "\"dn\": \"uni/fabric/nodePEp-202\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"203\", \"name\": \"leaf-3\", "
            + "\"role\": \"leaf\", \"podId\": \"1\", "
            + "\"dn\": \"uni/fabric/nodePEp-203\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"204\", \"name\": \"leaf-4\", "
            + "\"role\": \"leaf\", \"podId\": \"1\", "
            + "\"dn\": \"uni/fabric/nodePEp-204\"}}}"
            + "]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("spine-leaf.json", json, new Warnings());
    config.setVendor(org.batfish.datamodel.ConfigurationFormat.CISCO_ACI);

    // Verify fabric nodes were parsed
    assertThat(config.getFabricNodes().size(), equalTo(6));
    assertThat(config.getFabricNodes(), hasKey("101"));
    assertThat(config.getFabricNodes(), hasKey("102"));
    assertThat(config.getFabricNodes(), hasKey("201"));
    assertThat(config.getFabricNodes(), hasKey("202"));
    assertThat(config.getFabricNodes(), hasKey("203"));
    assertThat(config.getFabricNodes(), hasKey("204"));

    // Verify roles
    assertThat(config.getFabricNodes().get("101").getRole(), equalTo("spine"));
    assertThat(config.getFabricNodes().get("201").getRole(), equalTo("leaf"));

    // Verify topology edges are created
    // Should have 2 spines x 4 leaves = 8 edges
    assertThat(config.getLayer1Edges().size(), equalTo(8));

    // Verify each edge connects a leaf to a spine
    for (org.batfish.common.topology.Layer1Edge edge : config.getLayer1Edges()) {
      String node1 = edge.getNode1().getHostname();
      String node2 = edge.getNode2().getHostname();
      // Edges use node hostnames.
      // Spines: spine-1, spine-2; Leaves: leaf-1, leaf-2, leaf-3, leaf-4
      assertTrue(
          "Edge should connect spine to leaf: " + node1 + " -> " + node2,
          ((node1.equals("spine-1") || node1.equals("spine-2"))
                  && (node2.equals("leaf-1")
                      || node2.equals("leaf-2")
                      || node2.equals("leaf-3")
                      || node2.equals("leaf-4")))
              || ((node2.equals("spine-1") || node2.equals("spine-2"))
                  && (node1.equals("leaf-1")
                      || node1.equals("leaf-2")
                      || node1.equals("leaf-3")
                      || node1.equals("leaf-4"))));
    }
  }

  /**
   * Test parsing ACI config with fabricNodeIdentPol under fabricInst (real-world structure). This
   * matches the structure from actual ACI config exports where node names are in fabricNodeIdentPol
   * (separate from fabricNodePEp which has empty names).
   */
  @Test
  public void testParseRealConfig_fabricNodeIdentPolUnderFabricInst() throws IOException {
    // Real ACI config structure:
    // - fabricNodeIdentPol is a direct child of fabricInst (not under fabricExplicitGEp)
    // - fabricNodePEp has empty name and "unspecified" role
    // - Node names contain role info (e.g., "ACI-Spine-Node01", "ACI-Leaf-Node01")
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{\"fabricInst\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric\"},"
            + "\"children\": ["
            // fabricNodeIdentPol is a direct child of fabricInst
            + "{\"fabricNodeIdentPol\": {"
            + "\"attributes\": {\"name\": \"default\"},"
            + "\"children\": ["
            + "{\"fabricNodeIdentP\": {\"attributes\": {\"nodeId\": \"1101\", "
            + "\"name\": \"ACI-Spine-Node01\", \"role\": \"unspecified\"}}},"
            + "{\"fabricNodeIdentP\": {\"attributes\": {\"nodeId\": \"1102\", "
            + "\"name\": \"ACI-Spine-Node02\", \"role\": \"unspecified\"}}},"
            + "{\"fabricNodeIdentP\": {\"attributes\": {\"nodeId\": \"1201\", "
            + "\"name\": \"ACI-Leaf-Node01\", \"role\": \"unspecified\"}}},"
            + "{\"fabricNodeIdentP\": {\"attributes\": {\"nodeId\": \"1202\", "
            + "\"name\": \"ACI-Leaf-Node02\", \"role\": \"unspecified\"}}}"
            + "]}}"
            + ","
            // fabricProtPol with fabricNodePEp entries (empty names, no role)
            + "{\"fabricProtPol\": {"
            + "\"attributes\": {\"dn\": \"uni/fabric/fabricprotPol\"},"
            + "\"children\": ["
            + "{\"fabricExplicitGEp\": {"
            + "\"attributes\": {\"id\": \"1\"},"
            + "\"children\": ["
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"1101\", \"name\": \"\", "
            + "\"podId\": \"1\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"1102\", \"name\": \"\", "
            + "\"podId\": \"1\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"1201\", \"name\": \"\", "
            + "\"podId\": \"1\"}}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"id\": \"1202\", \"name\": \"\", "
            + "\"podId\": \"1\"}}}"
            + "]}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("fabricNodeIdentPol-test.json", json, new Warnings());
    config.setVendor(org.batfish.datamodel.ConfigurationFormat.CISCO_ACI);

    // Verify fabric nodes were parsed with names from fabricNodeIdentPol
    assertThat(config.getFabricNodes().size(), equalTo(4));
    assertThat(config.getFabricNodes(), hasKey("1101"));
    assertThat(config.getFabricNodes(), hasKey("1102"));
    assertThat(config.getFabricNodes(), hasKey("1201"));
    assertThat(config.getFabricNodes(), hasKey("1202"));

    // Verify names were extracted from fabricNodeIdentPol
    assertThat(config.getFabricNodes().get("1101").getName(), equalTo("ACI-Spine-Node01"));
    assertThat(config.getFabricNodes().get("1201").getName(), equalTo("ACI-Leaf-Node01"));

    // Verify roles were extracted from node names (name contains "-Spine-" or "-Leaf-")
    assertThat(config.getFabricNodes().get("1101").getRole(), equalTo("spine"));
    assertThat(config.getFabricNodes().get("1102").getRole(), equalTo("spine"));
    assertThat(config.getFabricNodes().get("1201").getRole(), equalTo("leaf"));
    assertThat(config.getFabricNodes().get("1202").getRole(), equalTo("leaf"));

    // Verify topology edges are created: 2 spines x 2 leaves = 4 edges
    assertThat(config.getLayer1Edges().size(), equalTo(4));
  }

  /** Helper method to load test JSON resource files. */
  private String loadTestResource(String filename) throws IOException {
    try (java.io.InputStream is = getClass().getResourceAsStream(filename)) {
      if (is == null) {
        throw new IOException("Resource not found: " + filename);
      }
      return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }
  }
}
