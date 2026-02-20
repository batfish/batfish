package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.batfish.vendor.cisco_aci.representation.apic.AciEndpointGroup;
import org.junit.Test;

/**
 * Tests for {@link AciEndpointGroup} model deserialization and field access.
 *
 * <p>This test class verifies that Endpoint Group JSON configuration can be properly deserialized
 * into the corresponding representation class, including all attribute fields.
 */
public class AciEndpointGroupModelTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Test deserialization of AciEndpointGroup with basic fields. */
  @Test
  public void testDeserializeAciEndpointGroup_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web-epg\","
            + "  \"descr\": \"Web Endpoint Group\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("web-epg"));
    assertThat(epg.getAttributes().getDescription(), equalTo("Web Endpoint Group"));
  }

  /** Test deserialization of AciEndpointGroup with annotation and distinguished name. */
  @Test
  public void testDeserializeAciEndpointGroup_annotationAndDn() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"epg1\","
            + "  \"annotation\": \"production\","
            + "  \"dn\": \"uni/tn-tenant1/ap-application1/epg-epg1\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("epg1"));
    assertThat(epg.getAttributes().getAnnotation(), equalTo("production"));
    assertThat(
        epg.getAttributes().getDistinguishedName(),
        equalTo("uni/tn-tenant1/ap-application1/epg-epg1"));
  }

  /** Test deserialization of AciEndpointGroup with exception tag. */
  @Test
  public void testDeserializeAciEndpointGroup_exceptionTag() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"exception_epg\","
            + "  \"exceptionTag\": \"exception1\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getExceptionTag(), equalTo("exception1"));
  }

  /** Test deserialization of AciEndpointGroup with flood on encapsulation. */
  @Test
  public void testDeserializeAciEndpointGroup_floodOnEncap() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"flood_epg\","
            + "  \"floodOnEncap\": \"enabled\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getFloodOnEncap(), equalTo("enabled"));
  }

  /** Test deserialization of AciEndpointGroup with forwarding control. */
  @Test
  public void testDeserializeAciEndpointGroup_forwardingControl() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"fwd_epg\","
            + "  \"fwdCtrl\": \"enabled\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getForwardingControl(), equalTo("enabled"));
  }

  /** Test deserialization of AciEndpointGroup with multicast source. */
  @Test
  public void testDeserializeAciEndpointGroup_hasMcastSource() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"mcast_epg\","
            + "  \"hasMcastSource\": \"yes\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getHasMcastSource(), equalTo("yes"));
  }

  /** Test deserialization of AciEndpointGroup with attribute-based EPG flag. */
  @Test
  public void testDeserializeAciEndpointGroup_isAttributeBasedEpg() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"attr_epg\","
            + "  \"isAttrBasedEPg\": \"yes\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getIsAttributeBasedEpg(), equalTo("yes"));
  }

  /** Test deserialization of AciEndpointGroup with match type. */
  @Test
  public void testDeserializeAciEndpointGroup_matchType() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"match_epg\","
            + "  \"matchT\": \"atleastOne\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getMatchType(), equalTo("atleastOne"));
  }

  /** Test deserialization of AciEndpointGroup with name alias. */
  @Test
  public void testDeserializeAciEndpointGroup_nameAlias() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"aliased_epg\","
            + "  \"nameAlias\": \"web_endpoint_group\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getNameAlias(), equalTo("web_endpoint_group"));
  }

  /** Test deserialization of AciEndpointGroup with policy enforcement preference. */
  @Test
  public void testDeserializeAciEndpointGroup_policyEnforcementPreference() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"policy_epg\","
            + "  \"pcEnfPref\": \"enforced\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test deserialization of AciEndpointGroup with preferred group member. */
  @Test
  public void testDeserializeAciEndpointGroup_preferredGroupMember() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"group_epg\","
            + "  \"prefGrMemb\": \"enabled\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getPreferredGroupMember(), equalTo("enabled"));
  }

  /** Test deserialization of AciEndpointGroup with priority. */
  @Test
  public void testDeserializeAciEndpointGroup_priority() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"priority_epg\","
            + "  \"prio\": \"1\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getPriority(), equalTo("1"));
  }

  /** Test deserialization of AciEndpointGroup with shutdown flag. */
  @Test
  public void testDeserializeAciEndpointGroup_shutdown() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"shutdown_epg\","
            + "  \"shutdown\": \"yes\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getShutdown(), equalTo("yes"));
  }

  /** Test deserialization of AciEndpointGroup with user domain. */
  @Test
  public void testDeserializeAciEndpointGroup_userDomain() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"userdom_epg\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getUserDomain(), equalTo("all"));
  }

  /** Test deserialization of AciEndpointGroup with all fields. */
  @Test
  public void testDeserializeAciEndpointGroup_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_epg\","
            + "  \"descr\": \"Full Endpoint Group\","
            + "  \"annotation\": \"production\","
            + "  \"dn\": \"uni/tn-tenant1/ap-app1/epg-full_epg\","
            + "  \"exceptionTag\": \"exception1\","
            + "  \"floodOnEncap\": \"enabled\","
            + "  \"fwdCtrl\": \"enabled\","
            + "  \"hasMcastSource\": \"yes\","
            + "  \"isAttrBasedEPg\": \"no\","
            + "  \"matchT\": \"atleastOne\","
            + "  \"nameAlias\": \"full_endpoint_group\","
            + "  \"pcEnfPref\": \"enforced\","
            + "  \"prefGrMemb\": \"enabled\","
            + "  \"prio\": \"1\","
            + "  \"shutdown\": \"no\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("full_epg"));
    assertThat(epg.getAttributes().getDescription(), equalTo("Full Endpoint Group"));
    assertThat(epg.getAttributes().getAnnotation(), equalTo("production"));
    assertThat(
        epg.getAttributes().getDistinguishedName(), equalTo("uni/tn-tenant1/ap-app1/epg-full_epg"));
    assertThat(epg.getAttributes().getExceptionTag(), equalTo("exception1"));
    assertThat(epg.getAttributes().getFloodOnEncap(), equalTo("enabled"));
    assertThat(epg.getAttributes().getForwardingControl(), equalTo("enabled"));
    assertThat(epg.getAttributes().getHasMcastSource(), equalTo("yes"));
    assertThat(epg.getAttributes().getIsAttributeBasedEpg(), equalTo("no"));
    assertThat(epg.getAttributes().getMatchType(), equalTo("atleastOne"));
    assertThat(epg.getAttributes().getNameAlias(), equalTo("full_endpoint_group"));
    assertThat(epg.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
    assertThat(epg.getAttributes().getPreferredGroupMember(), equalTo("enabled"));
    assertThat(epg.getAttributes().getPriority(), equalTo("1"));
    assertThat(epg.getAttributes().getShutdown(), equalTo("no"));
    assertThat(epg.getAttributes().getUserDomain(), equalTo("all"));
  }

  /** Test deserialization of AciEndpointGroup with children. */
  @Test
  public void testDeserializeAciEndpointGroup_withChildren() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"epg_with_children\""
            + "},"
            + "\"children\": ["
            + "  {\"child1\": \"value1\"},"
            + "  {\"child2\": \"value2\"}"
            + "]"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getChildren(), notNullValue());
    assertThat(epg.getChildren(), hasSize(2));
  }

  /** Test deserialization of AciEndpointGroup with null attributes. */
  @Test
  public void testDeserializeAciEndpointGroup_nullAttributes() throws IOException {
    String json = "{}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), nullValue());
  }

  /** Test deserialization of AciEndpointGroup with empty attribute values. */
  @Test
  public void testDeserializeAciEndpointGroup_emptyAttributeValues() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"\","
            + "  \"descr\": \"\","
            + "  \"annotation\": \"\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo(""));
    assertThat(epg.getAttributes().getDescription(), equalTo(""));
    assertThat(epg.getAttributes().getAnnotation(), equalTo(""));
  }

  /** Test direct instantiation of AciEndpointGroup. */
  @Test
  public void testDirectInstantiation() {
    AciEndpointGroup epg = new AciEndpointGroup();
    assertThat(epg.getAttributes(), nullValue());
    assertThat(epg.getChildren(), nullValue());
  }

  /** Test setter and getter for attributes field. */
  @Test
  public void testSetGetAttributes() {
    AciEndpointGroup epg = new AciEndpointGroup();
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();

    epg.setAttributes(attributes);
    assertThat(epg.getAttributes(), equalTo(attributes));
  }

  /** Test setter and getter for children field. */
  @Test
  public void testSetGetChildren() {
    AciEndpointGroup epg = new AciEndpointGroup();
    List<Object> children = new ArrayList<>();
    children.add("child1");
    children.add("child2");

    epg.setChildren(children);
    assertThat(epg.getChildren(), equalTo(children));
    assertThat(epg.getChildren(), hasSize(2));
  }

  /** Test setting null attributes. */
  @Test
  public void testSetNullAttributes() {
    AciEndpointGroup epg = new AciEndpointGroup();
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();

    epg.setAttributes(attributes);
    assertThat(epg.getAttributes(), notNullValue());

    epg.setAttributes(null);
    assertThat(epg.getAttributes(), nullValue());
  }

  /** Test setting null children. */
  @Test
  public void testSetNullChildren() {
    AciEndpointGroup epg = new AciEndpointGroup();
    List<Object> children = new ArrayList<>();

    epg.setChildren(children);
    assertThat(epg.getChildren(), notNullValue());

    epg.setChildren(null);
    assertThat(epg.getChildren(), nullValue());
  }

  /** Test direct instantiation of AciEndpointGroupAttributes. */
  @Test
  public void testDirectInstantiationAttributes() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    assertThat(attributes.getAnnotation(), nullValue());
    assertThat(attributes.getDescription(), nullValue());
    assertThat(attributes.getDistinguishedName(), nullValue());
    assertThat(attributes.getExceptionTag(), nullValue());
    assertThat(attributes.getFloodOnEncap(), nullValue());
    assertThat(attributes.getForwardingControl(), nullValue());
    assertThat(attributes.getHasMcastSource(), nullValue());
    assertThat(attributes.getIsAttributeBasedEpg(), nullValue());
    assertThat(attributes.getMatchType(), nullValue());
    assertThat(attributes.getName(), nullValue());
    assertThat(attributes.getNameAlias(), nullValue());
    assertThat(attributes.getPolicyEnforcementPreference(), nullValue());
    assertThat(attributes.getPreferredGroupMember(), nullValue());
    assertThat(attributes.getPriority(), nullValue());
    assertThat(attributes.getShutdown(), nullValue());
    assertThat(attributes.getUserDomain(), nullValue());
  }

  /** Test setter and getter for annotation field. */
  @Test
  public void testSetGetAnnotation() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setAnnotation("test_annotation");
    assertThat(attributes.getAnnotation(), equalTo("test_annotation"));
  }

  /** Test setter and getter for description field. */
  @Test
  public void testSetGetDescription() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setDescription("test description");
    assertThat(attributes.getDescription(), equalTo("test description"));
  }

  /** Test setter and getter for distinguishedName field. */
  @Test
  public void testSetGetDistinguishedName() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setDistinguishedName("uni/tn-tenant1/ap-app1/epg-epg1");
    assertThat(attributes.getDistinguishedName(), equalTo("uni/tn-tenant1/ap-app1/epg-epg1"));
  }

  /** Test setter and getter for exceptionTag field. */
  @Test
  public void testSetGetExceptionTag() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setExceptionTag("exception1");
    assertThat(attributes.getExceptionTag(), equalTo("exception1"));
  }

  /** Test setter and getter for floodOnEncap field. */
  @Test
  public void testSetGetFloodOnEncap() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setFloodOnEncap("enabled");
    assertThat(attributes.getFloodOnEncap(), equalTo("enabled"));
  }

  /** Test setter and getter for forwardingControl field. */
  @Test
  public void testSetGetForwardingControl() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setForwardingControl("enabled");
    assertThat(attributes.getForwardingControl(), equalTo("enabled"));
  }

  /** Test setter and getter for hasMcastSource field. */
  @Test
  public void testSetGetHasMcastSource() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setHasMcastSource("yes");
    assertThat(attributes.getHasMcastSource(), equalTo("yes"));
  }

  /** Test setter and getter for isAttributeBasedEpg field. */
  @Test
  public void testSetGetIsAttributeBasedEpg() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setIsAttributeBasedEpg("yes");
    assertThat(attributes.getIsAttributeBasedEpg(), equalTo("yes"));
  }

  /** Test setter and getter for matchType field. */
  @Test
  public void testSetGetMatchType() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setMatchType("atleastOne");
    assertThat(attributes.getMatchType(), equalTo("atleastOne"));
  }

  /** Test setter and getter for name field. */
  @Test
  public void testSetGetName() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setName("test_epg");
    assertThat(attributes.getName(), equalTo("test_epg"));
  }

  /** Test setter and getter for nameAlias field. */
  @Test
  public void testSetGetNameAlias() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setNameAlias("test_alias");
    assertThat(attributes.getNameAlias(), equalTo("test_alias"));
  }

  /** Test setter and getter for policyEnforcementPreference field. */
  @Test
  public void testSetGetPolicyEnforcementPreference() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setPolicyEnforcementPreference("enforced");
    assertThat(attributes.getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test setter and getter for preferredGroupMember field. */
  @Test
  public void testSetGetPreferredGroupMember() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setPreferredGroupMember("enabled");
    assertThat(attributes.getPreferredGroupMember(), equalTo("enabled"));
  }

  /** Test setter and getter for priority field. */
  @Test
  public void testSetGetPriority() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setPriority("1");
    assertThat(attributes.getPriority(), equalTo("1"));
  }

  /** Test setter and getter for shutdown field. */
  @Test
  public void testSetGetShutdown() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setShutdown("yes");
    assertThat(attributes.getShutdown(), equalTo("yes"));
  }

  /** Test setter and getter for userDomain field. */
  @Test
  public void testSetGetUserDomain() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setUserDomain("all");
    assertThat(attributes.getUserDomain(), equalTo("all"));
  }

  /** Test setting null values for all attribute fields. */
  @Test
  public void testSetNullAttributeValues() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();

    attributes.setAnnotation(null);
    attributes.setDescription(null);
    attributes.setDistinguishedName(null);
    attributes.setExceptionTag(null);
    attributes.setFloodOnEncap(null);
    attributes.setForwardingControl(null);
    attributes.setHasMcastSource(null);
    attributes.setIsAttributeBasedEpg(null);
    attributes.setMatchType(null);
    attributes.setName(null);
    attributes.setNameAlias(null);
    attributes.setPolicyEnforcementPreference(null);
    attributes.setPreferredGroupMember(null);
    attributes.setPriority(null);
    attributes.setShutdown(null);
    attributes.setUserDomain(null);

    assertThat(attributes.getAnnotation(), nullValue());
    assertThat(attributes.getDescription(), nullValue());
    assertThat(attributes.getDistinguishedName(), nullValue());
    assertThat(attributes.getExceptionTag(), nullValue());
    assertThat(attributes.getFloodOnEncap(), nullValue());
    assertThat(attributes.getForwardingControl(), nullValue());
    assertThat(attributes.getHasMcastSource(), nullValue());
    assertThat(attributes.getIsAttributeBasedEpg(), nullValue());
    assertThat(attributes.getMatchType(), nullValue());
    assertThat(attributes.getName(), nullValue());
    assertThat(attributes.getNameAlias(), nullValue());
    assertThat(attributes.getPolicyEnforcementPreference(), nullValue());
    assertThat(attributes.getPreferredGroupMember(), nullValue());
    assertThat(attributes.getPriority(), nullValue());
    assertThat(attributes.getShutdown(), nullValue());
    assertThat(attributes.getUserDomain(), nullValue());
  }

  /** Test setting empty string values for all string attributes. */
  @Test
  public void testSetEmptyStringAttributeValues() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();

    attributes.setAnnotation("");
    attributes.setDescription("");
    attributes.setDistinguishedName("");
    attributes.setExceptionTag("");
    attributes.setFloodOnEncap("");
    attributes.setForwardingControl("");
    attributes.setHasMcastSource("");
    attributes.setIsAttributeBasedEpg("");
    attributes.setMatchType("");
    attributes.setName("");
    attributes.setNameAlias("");
    attributes.setPolicyEnforcementPreference("");
    attributes.setPreferredGroupMember("");
    attributes.setPriority("");
    attributes.setShutdown("");
    attributes.setUserDomain("");

    assertThat(attributes.getAnnotation(), equalTo(""));
    assertThat(attributes.getDescription(), equalTo(""));
    assertThat(attributes.getDistinguishedName(), equalTo(""));
    assertThat(attributes.getExceptionTag(), equalTo(""));
    assertThat(attributes.getFloodOnEncap(), equalTo(""));
    assertThat(attributes.getForwardingControl(), equalTo(""));
    assertThat(attributes.getHasMcastSource(), equalTo(""));
    assertThat(attributes.getIsAttributeBasedEpg(), equalTo(""));
    assertThat(attributes.getMatchType(), equalTo(""));
    assertThat(attributes.getName(), equalTo(""));
    assertThat(attributes.getNameAlias(), equalTo(""));
    assertThat(attributes.getPolicyEnforcementPreference(), equalTo(""));
    assertThat(attributes.getPreferredGroupMember(), equalTo(""));
    assertThat(attributes.getPriority(), equalTo(""));
    assertThat(attributes.getShutdown(), equalTo(""));
    assertThat(attributes.getUserDomain(), equalTo(""));
  }

  /** Test multiple updates to the same attribute field. */
  @Test
  public void testMultipleUpdatesToAttributeField() {
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();

    attributes.setName("epg1");
    assertThat(attributes.getName(), equalTo("epg1"));

    attributes.setName("epg2");
    assertThat(attributes.getName(), equalTo("epg2"));

    attributes.setName("epg3");
    assertThat(attributes.getName(), equalTo("epg3"));
  }

  /** Test serializability of AciEndpointGroup. */
  @Test
  public void testSerializable() throws IOException {
    AciEndpointGroup epg = new AciEndpointGroup();
    AciEndpointGroup.AciEndpointGroupAttributes attributes =
        new AciEndpointGroup.AciEndpointGroupAttributes();
    attributes.setName("test_epg");
    attributes.setDescription("Test Endpoint Group");
    epg.setAttributes(attributes);

    // Serialize and deserialize
    String json = MAPPER.writeValueAsString(epg);
    AciEndpointGroup deserialized = MAPPER.readValue(json, AciEndpointGroup.class);

    assertThat(deserialized.getAttributes(), notNullValue());
    assertThat(deserialized.getAttributes().getName(), equalTo("test_epg"));
    assertThat(deserialized.getAttributes().getDescription(), equalTo("Test Endpoint Group"));
  }
}
