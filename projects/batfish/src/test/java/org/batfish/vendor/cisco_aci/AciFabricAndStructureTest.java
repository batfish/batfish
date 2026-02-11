package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciAttributes;
import org.batfish.vendor.cisco_aci.representation.AciBgpRouteTargetProfile;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciFabricNode;
import org.batfish.vendor.cisco_aci.representation.AciPolUni;
import org.batfish.vendor.cisco_aci.representation.AciStructureType;
import org.batfish.vendor.cisco_aci.representation.AciStructureUsage;
import org.junit.Test;

/**
 * Tests for ACI fabric nodes, structure types/usage, and core configuration objects.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Fabric node deserialization
 *   <li>BGP route target profile deserialization
 *   <li>Dynamic attributes handling
 *   <li>PolUni root object deserialization
 *   <li>Structure type enum functionality
 *   <li>Structure usage enum functionality
 * </ul>
 */
public class AciFabricAndStructureTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Test deserialization of AciFabricNode with basic fields. */
  @Test
  public void testDeserializeAciFabricNode_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"leaf1\","
            + "  \"id\": \"101\","
            + "  \"podId\": \"1\","
            + "  \"role\": \"leaf\""
            + "}"
            + "}";

    AciFabricNode node = MAPPER.readValue(json, AciFabricNode.class);
    assertThat(node.getAttributes(), notNullValue());
    assertThat(node.getAttributes().getName(), equalTo("leaf1"));
    assertThat(node.getAttributes().getId(), equalTo("101"));
    assertThat(node.getAttributes().getPodId(), equalTo("1"));
    assertThat(node.getAttributes().getRole(), equalTo("leaf"));
  }

  /** Test deserialization of AciFabricNode with description. */
  @Test
  public void testDeserializeAciFabricNode_withDescription() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"spine1\","
            + "  \"descr\": \"Spine switch 1\","
            + "  \"id\": \"201\","
            + "  \"role\": \"spine\""
            + "}"
            + "}";

    AciFabricNode node = MAPPER.readValue(json, AciFabricNode.class);
    assertThat(node.getAttributes(), notNullValue());
    assertThat(node.getAttributes().getName(), equalTo("spine1"));
    assertThat(node.getAttributes().getDescription(), equalTo("Spine switch 1"));
    assertThat(node.getAttributes().getId(), equalTo("201"));
    assertThat(node.getAttributes().getRole(), equalTo("spine"));
  }

  /** Test deserialization of AciBgpRouteTargetProfile with control fields. */
  @Test
  public void testDeserializeAciBgpRouteTargetProfile_control() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"rt_profile\","
            + "  \"descr\": \"Route target profile\","
            + "  \"ctrl\": \"route-target\","
            + "  \"attrmap\": \"import\""
            + "}"
            + "}";

    AciBgpRouteTargetProfile profile = MAPPER.readValue(json, AciBgpRouteTargetProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("rt_profile"));
    assertThat(profile.getAttributes().getDescription(), equalTo("Route target profile"));
    assertThat(profile.getAttributes().getControl(), equalTo("route-target"));
    assertThat(profile.getAttributes().getAttributeMap(), equalTo("import"));
  }

  /** Test deserialization of AciBgpRouteTargetProfile with address type control. */
  @Test
  public void testDeserializeAciBgpRouteTargetProfile_addrCtrl() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"addr_ctrl_profile\","
            + "  \"addrTCtrl\": \"evpn\","
            + "  \"ctrl\": \"allow-self-as\""
            + "}"
            + "}";

    AciBgpRouteTargetProfile profile = MAPPER.readValue(json, AciBgpRouteTargetProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getAddressTypeControl(), equalTo("evpn"));
    assertThat(profile.getAttributes().getControl(), equalTo("allow-self-as"));
  }

  /** Test AciAttributes with common fields. */
  @Test
  public void testAciAttributes_commonFields() throws IOException {
    String json =
        "{"
            + "  \"dn\": \"uni/tn-test\","
            + "  \"name\": \"test\","
            + "  \"descr\": \"Test object\","
            + "  \"annotation\": \"test_annotation\","
            + "  \"nameAlias\": \"test_alias\","
            + "  \"status\": \"created\""
            + "}";

    AciAttributes attributes = MAPPER.readValue(json, AciAttributes.class);
    assertThat(attributes.getDistinguishedName(), equalTo("uni/tn-test"));
    assertThat(attributes.getName(), equalTo("test"));
    assertThat(attributes.getDescription(), equalTo("Test object"));
    assertThat(attributes.getAnnotation(), equalTo("test_annotation"));
    assertThat(attributes.getNameAlias(), equalTo("test_alias"));
    assertThat(attributes.getStatus(), equalTo("created"));
  }

  /** Test AciAttributes with owner fields. */
  @Test
  public void testAciAttributes_ownerFields() throws IOException {
    String json =
        "{"
            + "  \"name\": \"owned_object\","
            + "  \"ownerKey\": \"key123\","
            + "  \"ownerTag\": \"tag456\","
            + "  \"userdom\": \"all\""
            + "}";

    AciAttributes attributes = MAPPER.readValue(json, AciAttributes.class);
    assertThat(attributes.getName(), equalTo("owned_object"));
    assertThat(attributes.getOwnerKey(), equalTo("key123"));
    assertThat(attributes.getOwnerTag(), equalTo("tag456"));
    assertThat(attributes.getUserDomain(), equalTo("all"));
  }

  /** Test AciAttributes with custom fields. */
  @Test
  public void testAciAttributes_customFields() throws IOException {
    String json =
        "{"
            + "  \"name\": \"custom\","
            + "  \"customField1\": \"value1\","
            + "  \"customField2\": \"value2\""
            + "}";

    AciAttributes attributes = MAPPER.readValue(json, AciAttributes.class);
    assertThat(attributes.getName(), equalTo("custom"));
    assertThat(attributes.get("customField1"), equalTo("value1"));
    assertThat(attributes.get("customField2"), equalTo("value2"));
    assertThat(attributes.hasAttribute("customField1"), equalTo(true));
    assertThat(attributes.size(), equalTo(3));
  }

  /** Test AciAttributes put and get operations. */
  @Test
  public void testAciAttributes_putAndGet() {
    AciAttributes attributes = new AciAttributes();
    attributes.put("testKey", "testValue");
    assertThat(attributes.get("testKey"), equalTo("testValue"));
    assertThat(attributes.hasAttribute("testKey"), equalTo(true));
    assertThat(attributes.size(), equalTo(1));
  }

  /** Test AciPolUni with attributes. */
  @Test
  public void testDeserializeAciPolUni_withAttributes() throws IOException {
    String json =
        "{" + "  \"attributes\": {" + "    \"dn\": \"uni\"" + "  }," + "  \"children\": []" + "}";

    AciPolUni polUni = MAPPER.readValue(json, AciPolUni.class);
    assertThat(polUni.getAttributes(), notNullValue());
    assertThat(polUni.getAttributes().getDistinguishedName(), equalTo("uni"));
    assertThat(polUni.getChildren(), notNullValue());
  }

  /** Test AciPolUni with null children. */
  @Test
  public void testDeserializeAciPolUni_nullChildren() throws IOException {
    String json = "{" + "  \"attributes\": {" + "    \"dn\": \"uni\"" + "  }" + "}";

    AciPolUni polUni = MAPPER.readValue(json, AciPolUni.class);
    assertThat(polUni.getAttributes(), notNullValue());
    assertThat(polUni.getChildren(), nullValue());
  }

  /** Test AciStructureType enum values. */
  @Test
  public void testAciStructureType_enumValues() {
    assertThat(AciStructureType.BRIDGE_DOMAIN.getDescription(), equalTo("Bridge Domain"));
    assertThat(AciStructureType.CONTRACT.getDescription(), equalTo("Contract"));
    assertThat(AciStructureType.EPG.getDescription(), equalTo("Endpoint Group"));
    assertThat(AciStructureType.FABRIC_NODE.getDescription(), equalTo("Fabric Node"));
    assertThat(AciStructureType.INTERFACE.getDescription(), equalTo("Interface"));
    assertThat(AciStructureType.L3_OUT.getDescription(), equalTo("L3 External Network"));
    assertThat(AciStructureType.TENANT.getDescription(), equalTo("Tenant"));
    assertThat(AciStructureType.VRF.getDescription(), equalTo("VRF"));
  }

  /** Test AciStructureType abstract structures. */
  @Test
  public void testAciStructureType_abstractStructures() {
    assertThat(AciStructureType.CONTRACT_SUBJECT.getDescription(), equalTo("Contract Subject"));
    assertThat(AciStructureType.CONTRACT_FILTER.getDescription(), equalTo("Contract Filter"));
    assertThat(AciStructureType.ENDPOINT.getDescription(), equalTo("Endpoint"));
    assertThat(AciStructureType.L3_EXT_EPG.getDescription(), equalTo("L3 External EPG"));
    assertThat(AciStructureType.NODE_PATH.getDescription(), equalTo("Node Path"));
    assertThat(AciStructureType.PATH_ATTACHMENT.getDescription(), equalTo("Path Attachment"));
    assertThat(AciStructureType.SUBNET.getDescription(), equalTo("Subnet"));
  }

  /** Test AciStructureUsage enum values for tenant relationships. */
  @Test
  public void testAciStructureUsage_tenantRelationships() {
    assertThat(
        AciStructureUsage.TENANT_APPLICATION_PROFILE.getDescription(),
        equalTo("tenant application profile"));
    assertThat(
        AciStructureUsage.TENNT_BRIDGE_DOMAIN.getDescription(), equalTo("tenant bridge domain"));
    assertThat(AciStructureUsage.TENANT_CONTRACT.getDescription(), equalTo("tenant contract"));
    assertThat(AciStructureUsage.TENANT_FILTER.getDescription(), equalTo("tenant filter"));
    assertThat(AciStructureUsage.TENANT_L3OUT.getDescription(), equalTo("tenant l3out"));
    assertThat(AciStructureUsage.TENANT_VRF.getDescription(), equalTo("tenant vrf"));
    assertThat(AciStructureUsage.TENANT_SELF_REF.getDescription(), equalTo("tenant"));
  }

  /** Test AciStructureUsage enum values for EPG relationships. */
  @Test
  public void testAciStructureUsage_epgRelationships() {
    assertThat(
        AciStructureUsage.APPLICATION_PROFILE_EPG.getDescription(),
        equalTo("application profile endpoint group"));
    assertThat(
        AciStructureUsage.EPG_CONTRACT_CONSUMER.getDescription(),
        equalTo("endpoint group consumer contract"));
    assertThat(
        AciStructureUsage.EPG_CONTRACT_PROVIDER.getDescription(),
        equalTo("endpoint group provider contract"));
    assertThat(
        AciStructureUsage.EPG_DOMAIN_BINDING.getDescription(),
        equalTo("endpoint group domain binding"));
    assertThat(
        AciStructureUsage.EPG_PHYSICAL_DOMAIN.getDescription(),
        equalTo("endpoint group physical domain"));
    assertThat(AciStructureUsage.EPG_SELF_REF.getDescription(), equalTo("endpoint group"));
  }

  /** Test AciStructureUsage enum values for contract relationships. */
  @Test
  public void testAciStructureUsage_contractRelationships() {
    assertThat(AciStructureUsage.CONTRACT_PROVIDER.getDescription(), equalTo("contract provider"));
    assertThat(AciStructureUsage.CONTRACT_CONSUMER.getDescription(), equalTo("contract consumer"));
    assertThat(AciStructureUsage.CONTRACT_SUBJECT.getDescription(), equalTo("contract subject"));
    assertThat(
        AciStructureUsage.CONTRACT_SUBJECT_FILTER.getDescription(),
        equalTo("contract subject filter"));
    assertThat(AciStructureUsage.SUBJECT_FILTER.getDescription(), equalTo("subject filter"));
    assertThat(AciStructureUsage.SUBJECT_SELF_REF.getDescription(), equalTo("subject"));
  }

  /** Test AciStructureUsage enum values for VRF relationships. */
  @Test
  public void testAciStructureUsage_vrfRelationships() {
    assertThat(AciStructureUsage.VRF_BRIDGE_DOMAIN.getDescription(), equalTo("vrf bridge domain"));
    assertThat(AciStructureUsage.VRF_CONTRACT.getDescription(), equalTo("vrf contract"));
    assertThat(AciStructureUsage.VRF_L3OUT.getDescription(), equalTo("vrf l3out"));
    assertThat(AciStructureUsage.VRF_SELF_REF.getDescription(), equalTo("vrf"));
  }

  /** Test AciStructureUsage enum values for filter relationships. */
  @Test
  public void testAciStructureUsage_filterRelationships() {
    assertThat(AciStructureUsage.FILTER_SELF_REF.getDescription(), equalTo("filter"));
    assertThat(AciStructureUsage.FILTER_ENTRY.getDescription(), equalTo("filter entry"));
  }

  /** Test full configuration with fabric node. */
  @Test
  public void testFullConfiguration_fabricNode() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fabricNodePEp\": {"
            + "        \"attributes\": {"
            + "          \"id\": \"101\","
            + "          \"name\": \"leaf1\","
            + "          \"podId\": \"1\","
            + "          \"role\": \"leaf\""
            + "        }"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("fabric_node.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getFabricNodes(), notNullValue());
  }

  /** Test full configuration with BGP route target profile. */
  @Test
  public void testFullConfiguration_routeTargetProfile() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"},"
            + "        \"children\": ["
            + "          {"
            + "            \"bgpRtSummPol\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"rt_profile\","
            + "                \"ctrl\": \"route-target\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("rt_profile.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test AciAttributes with all fields null. */
  @Test
  public void testAciAttributes_empty() throws IOException {
    String json = "{}";

    AciAttributes attributes = MAPPER.readValue(json, AciAttributes.class);
    assertThat(attributes.getName(), nullValue());
    assertThat(attributes.getDistinguishedName(), nullValue());
    assertThat(attributes.size(), equalTo(0));
  }

  /** Test fabric node with all fields. */
  @Test
  public void testAciFabricNode_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"complete_node\","
            + "  \"descr\": \"Complete fabric node\","
            + "  \"id\": \"301\","
            + "  \"podId\": \"2\","
            + "  \"role\": \"leaf\","
            + "  \"nameAlias\": \"node_alias\","
            + "  \"annotation\": \"prod_node\""
            + "}"
            + "}";

    AciFabricNode node = MAPPER.readValue(json, AciFabricNode.class);
    assertThat(node.getAttributes(), notNullValue());
    assertThat(node.getAttributes().getName(), equalTo("complete_node"));
    assertThat(node.getAttributes().getDescription(), equalTo("Complete fabric node"));
    assertThat(node.getAttributes().getId(), equalTo("301"));
    assertThat(node.getAttributes().getPodId(), equalTo("2"));
    assertThat(node.getAttributes().getRole(), equalTo("leaf"));
    assertThat(node.getAttributes().getNameAlias(), equalTo("node_alias"));
    assertThat(node.getAttributes().getAnnotation(), equalTo("prod_node"));
  }

  /** Test BGP route target profile with all fields. */
  @Test
  public void testAciBgpRouteTargetProfile_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_rt_profile\","
            + "  \"descr\": \"Complete route target profile\","
            + "  \"addrTCtrl\": \"evpn\","
            + "  \"ctrl\": \"route-target\","
            + "  \"attrmap\": \"import-export\","
            + "  \"nameAlias\": \"rt_alias\""
            + "}"
            + "}";

    AciBgpRouteTargetProfile profile = MAPPER.readValue(json, AciBgpRouteTargetProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("full_rt_profile"));
    assertThat(profile.getAttributes().getDescription(), equalTo("Complete route target profile"));
    assertThat(profile.getAttributes().getAddressTypeControl(), equalTo("evpn"));
    assertThat(profile.getAttributes().getControl(), equalTo("route-target"));
    assertThat(profile.getAttributes().getAttributeMap(), equalTo("import-export"));
    assertThat(profile.getAttributes().getNameAlias(), equalTo("rt_alias"));
  }

  /** Test AciStructureUsage bridge domain relationships. */
  @Test
  public void testAciStructureUsage_bridgeDomainRelationships() {
    assertThat(
        AciStructureUsage.BRIDGE_DOMAIN_CONSUMER_CONTRACT.getDescription(),
        equalTo("bridge domain consumer contract"));
    assertThat(
        AciStructureUsage.BRIDGE_DOMAIN_CONTRACT.getDescription(),
        equalTo("bridge domain contract"));
    assertThat(
        AciStructureUsage.BRIDGE_DOMAIN_L3OUT.getDescription(), equalTo("bridge domain l3out"));
    assertThat(
        AciStructureUsage.BRIDGE_DOMAIN_PROVIDER_CONTRACT.getDescription(),
        equalTo("bridge domain provider contract"));
    assertThat(AciStructureUsage.BRIDGE_DOMAIN_SELF_REF.getDescription(), equalTo("bridge domain"));
    assertThat(
        AciStructureUsage.BRIDGE_DOMAIN_SUBNET.getDescription(), equalTo("bridge domain subnet"));
  }

  /** Test AciStructureUsage L3Out relationships. */
  @Test
  public void testAciStructureUsage_l3outRelationships() {
    assertThat(
        AciStructureUsage.L3OUT_EXTERNAL_EPG.getDescription(),
        equalTo("l3out external endpoint group"));
    assertThat(
        AciStructureUsage.L3OUT_NODE_PROFILE.getDescription(), equalTo("l3out node profile"));
    assertThat(AciStructureUsage.L3OUT_SELF_REF.getDescription(), equalTo("l3out"));
    assertThat(AciStructureUsage.L3OUT_VRF.getDescription(), equalTo("l3out vrf"));
  }

  /**
   * Test management IP parsing from mgmtMgmtP structure.
   *
   * <p>Verifies that out-of-band management IPs are correctly extracted from mgmtRsOoBStNode
   * objects and associated with fabric nodes.
   */
  @Test
  public void testManagementIpParsing() throws IOException {
    // Create a minimal ACI config with management IPs
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"mgmt\"},"
            + "        \"children\": ["
            + "          {"
            + "            \"mgmtMgmtP\": {"
            + "              \"attributes\": {\"name\": \"default\"},"
            + "              \"children\": ["
            + "                {"
            + "                  \"mgmtOoB\": {"
            + "                    \"attributes\": {\"name\": \"default\"},"
            + "                    \"children\": ["
            + "                      {"
            + "                        \"mgmtRsOoBStNode\": {"
            + "                          \"attributes\": {"
            + "                            \"addr\": \"10.35.1.52/24\","
            + "                            \"gw\": \"10.35.1.1\","
            + "                            \"tDn\": \"topology/pod-1/node-1208\""
            + "                          }"
            + "                        }"
            + "                      },"
            + "                      {"
            + "                        \"mgmtRsOoBStNode\": {"
            + "                          \"attributes\": {"
            + "                            \"addr\": \"10.35.1.48/24\","
            + "                            \"gw\": \"10.35.1.1\","
            + "                            \"tDn\": \"topology/pod-1/node-1204\""
            + "                          }"
            + "                        }"
            + "                      }"
            + "                    ]"
            + "                  }"
            + "                }"
            + "              ]"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    },"
            + "    {"
            + "      \"fabricInst\": {"
            + "        \"attributes\": {},"
            + "        \"children\": ["
            + "          {"
            + "            \"fabricProtPol\": {"
            + "              \"attributes\": {},"
            + "              \"children\": ["
            + "                {"
            + "                  \"fabricExplicitGEp\": {"
            + "                    \"attributes\": {},"
            + "                    \"children\": ["
            + "                      {"
            + "                        \"fabricNodeIdentP\": {"
            + "                          \"attributes\": {"
            + "                            \"id\": \"1208\","
            + "                            \"name\": \"leaf-1\""
            + "                          }"
            + "                        }"
            + "                      },"
            + "                      {"
            + "                        \"fabricNodePEp\": {"
            + "                          \"attributes\": {"
            + "                            \"id\": \"1208\","
            + "                            \"role\": \"leaf\","
            + "                            \"podId\": \"1\""
            + "                          }"
            + "                        }"
            + "                      },"
            + "                      {"
            + "                        \"fabricNodeIdentP\": {"
            + "                          \"attributes\": {"
            + "                            \"id\": \"1204\","
            + "                            \"name\": \"leaf-2\""
            + "                          }"
            + "                        }"
            + "                      },"
            + "                      {"
            + "                        \"fabricNodePEp\": {"
            + "                          \"attributes\": {"
            + "                            \"id\": \"1204\","
            + "                            \"role\": \"leaf\","
            + "                            \"podId\": \"1\""
            + "                          }"
            + "                        }"
            + "                      }"
            + "                    ]"
            + "                  }"
            + "                }"
            + "              ]"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    // Parse the configuration
    Warnings warnings = new Warnings();
    AciConfiguration aciConfig = AciConfiguration.fromJson("test-config.json", json, warnings);

    // Verify management IPs were extracted
    assertThat("Should have 2 fabric nodes", aciConfig.getFabricNodes().size(), equalTo(2));

    // Check node 1208
    AciConfiguration.FabricNode node1208 = aciConfig.getFabricNodes().get("1208");
    assertThat("Node 1208 should exist", node1208, notNullValue());
    assertThat(
        "Node 1208 should have management info", node1208.getManagementInfo(), notNullValue());
    assertThat(
        "Node 1208 management address",
        node1208.getManagementInfo().getAddress(),
        equalTo("10.35.1.52/24"));
    assertThat(
        "Node 1208 management gateway",
        node1208.getManagementInfo().getGateway(),
        equalTo("10.35.1.1"));

    // Check node 1204
    AciConfiguration.FabricNode node1204 = aciConfig.getFabricNodes().get("1204");
    assertThat("Node 1204 should exist", node1204, notNullValue());
    assertThat(
        "Node 1204 should have management info", node1204.getManagementInfo(), notNullValue());
    assertThat(
        "Node 1204 management address",
        node1204.getManagementInfo().getAddress(),
        equalTo("10.35.1.48/24"));
    assertThat(
        "Node 1204 management gateway",
        node1204.getManagementInfo().getGateway(),
        equalTo("10.35.1.1"));
  }
}
