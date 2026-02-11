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
import org.batfish.vendor.cisco_aci.representation.AciApplicationProfile;
import org.junit.Test;

/**
 * Tests for {@link AciApplicationProfile} model deserialization and field access.
 *
 * <p>This test class verifies that Application Profile JSON configuration can be properly
 * deserialized into the corresponding representation class, including all attribute fields.
 */
public class AciApplicationProfileModelTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Test deserialization of AciApplicationProfile with basic fields. */
  @Test
  public void testDeserializeAciApplicationProfile_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web-app\","
            + "  \"descr\": \"Web Application Profile\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("web-app"));
    assertThat(profile.getAttributes().getDescription(), equalTo("Web Application Profile"));
  }

  /** Test deserialization of AciApplicationProfile with annotation and distinguished name. */
  @Test
  public void testDeserializeAciApplicationProfile_annotationAndDn() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"app1\","
            + "  \"annotation\": \"production\","
            + "  \"dn\": \"uni/tn-tenant1/ap-app1\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("app1"));
    assertThat(profile.getAttributes().getAnnotation(), equalTo("production"));
    assertThat(profile.getAttributes().getDistinguishedName(), equalTo("uni/tn-tenant1/ap-app1"));
  }

  /** Test deserialization of AciApplicationProfile with owner fields. */
  @Test
  public void testDeserializeAciApplicationProfile_owner() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"owned_app\","
            + "  \"ownerKey\": \"key123\","
            + "  \"ownerTag\": \"tag456\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("owned_app"));
    assertThat(profile.getAttributes().getOwnerKey(), equalTo("key123"));
    assertThat(profile.getAttributes().getOwnerTag(), equalTo("tag456"));
    assertThat(profile.getAttributes().getUserDomain(), equalTo("all"));
  }

  /** Test deserialization of AciApplicationProfile with exception tag. */
  @Test
  public void testDeserializeAciApplicationProfile_exceptionTag() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"exception_app\","
            + "  \"exceptionTag\": \"exception1\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getExceptionTag(), equalTo("exception1"));
  }

  /** Test deserialization of AciApplicationProfile with flood on encapsulation. */
  @Test
  public void testDeserializeAciApplicationProfile_floodOnEncap() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"flood_app\","
            + "  \"floodOnEncap\": \"enabled\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getFloodOnEncap(), equalTo("enabled"));
  }

  /** Test deserialization of AciApplicationProfile with forwarding control. */
  @Test
  public void testDeserializeAciApplicationProfile_forwardingControl() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"fwd_app\","
            + "  \"fwdCtrl\": \"enabled\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getForwardingControl(), equalTo("enabled"));
  }

  /** Test deserialization of AciApplicationProfile with multicast source. */
  @Test
  public void testDeserializeAciApplicationProfile_hasMulticastSource() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"mcast_app\","
            + "  \"hasMcastSource\": \"yes\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getHasMulticastSource(), equalTo("yes"));
  }

  /** Test deserialization of AciApplicationProfile with attribute-based EPG flag. */
  @Test
  public void testDeserializeAciApplicationProfile_isAttributeBasedEpg() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"attr_epg_app\","
            + "  \"isAttrBasedEPg\": \"yes\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getIsAttributeBasedEpg(), equalTo("yes"));
  }

  /** Test deserialization of AciApplicationProfile with match type. */
  @Test
  public void testDeserializeAciApplicationProfile_matchType() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"match_app\","
            + "  \"matchT\": \"atleastOne\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getMatchType(), equalTo("atleastOne"));
  }

  /** Test deserialization of AciApplicationProfile with name alias. */
  @Test
  public void testDeserializeAciApplicationProfile_nameAlias() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"aliased_app\","
            + "  \"nameAlias\": \"web_profile\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getNameAlias(), equalTo("web_profile"));
  }

  /** Test deserialization of AciApplicationProfile with policy enforcement preference. */
  @Test
  public void testDeserializeAciApplicationProfile_policyEnforcementPreference()
      throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"policy_app\","
            + "  \"pcEnfPref\": \"enforced\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test deserialization of AciApplicationProfile with preferred group member. */
  @Test
  public void testDeserializeAciApplicationProfile_preferredGroupMember() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"group_app\","
            + "  \"prefGrMemb\": \"enabled\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getPreferredGroupMember(), equalTo("enabled"));
  }

  /** Test deserialization of AciApplicationProfile with priority. */
  @Test
  public void testDeserializeAciApplicationProfile_priority() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"priority_app\","
            + "  \"prio\": \"1\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getPriority(), equalTo("1"));
  }

  /** Test deserialization of AciApplicationProfile with shutdown flag. */
  @Test
  public void testDeserializeAciApplicationProfile_shutdown() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"shutdown_app\","
            + "  \"shutdown\": \"yes\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getShutdown(), equalTo("yes"));
  }

  /** Test deserialization of AciApplicationProfile with all fields. */
  @Test
  public void testDeserializeAciApplicationProfile_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_app\","
            + "  \"descr\": \"Full Application Profile\","
            + "  \"annotation\": \"production\","
            + "  \"dn\": \"uni/tn-tenant1/ap-full_app\","
            + "  \"exceptionTag\": \"exception1\","
            + "  \"floodOnEncap\": \"enabled\","
            + "  \"fwdCtrl\": \"enabled\","
            + "  \"hasMcastSource\": \"yes\","
            + "  \"isAttrBasedEPg\": \"no\","
            + "  \"matchT\": \"atleastOne\","
            + "  \"nameAlias\": \"full_profile\","
            + "  \"ownerKey\": \"key123\","
            + "  \"ownerTag\": \"tag456\","
            + "  \"pcEnfPref\": \"enforced\","
            + "  \"prefGrMemb\": \"enabled\","
            + "  \"prio\": \"1\","
            + "  \"shutdown\": \"no\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("full_app"));
    assertThat(profile.getAttributes().getDescription(), equalTo("Full Application Profile"));
    assertThat(profile.getAttributes().getAnnotation(), equalTo("production"));
    assertThat(
        profile.getAttributes().getDistinguishedName(), equalTo("uni/tn-tenant1/ap-full_app"));
    assertThat(profile.getAttributes().getExceptionTag(), equalTo("exception1"));
    assertThat(profile.getAttributes().getFloodOnEncap(), equalTo("enabled"));
    assertThat(profile.getAttributes().getForwardingControl(), equalTo("enabled"));
    assertThat(profile.getAttributes().getHasMulticastSource(), equalTo("yes"));
    assertThat(profile.getAttributes().getIsAttributeBasedEpg(), equalTo("no"));
    assertThat(profile.getAttributes().getMatchType(), equalTo("atleastOne"));
    assertThat(profile.getAttributes().getNameAlias(), equalTo("full_profile"));
    assertThat(profile.getAttributes().getOwnerKey(), equalTo("key123"));
    assertThat(profile.getAttributes().getOwnerTag(), equalTo("tag456"));
    assertThat(profile.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
    assertThat(profile.getAttributes().getPreferredGroupMember(), equalTo("enabled"));
    assertThat(profile.getAttributes().getPriority(), equalTo("1"));
    assertThat(profile.getAttributes().getShutdown(), equalTo("no"));
    assertThat(profile.getAttributes().getUserDomain(), equalTo("all"));
  }

  /** Test deserialization of AciApplicationProfile with children. */
  @Test
  public void testDeserializeAciApplicationProfile_withChildren() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"app_with_children\""
            + "},"
            + "\"children\": ["
            + "  {\"child1\": \"value1\"},"
            + "  {\"child2\": \"value2\"}"
            + "]"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getChildren(), notNullValue());
    assertThat(profile.getChildren(), hasSize(2));
  }

  /** Test deserialization of AciApplicationProfile with null attributes. */
  @Test
  public void testDeserializeAciApplicationProfile_nullAttributes() throws IOException {
    String json = "{}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), nullValue());
  }

  /** Test deserialization of AciApplicationProfile with empty attribute values. */
  @Test
  public void testDeserializeAciApplicationProfile_emptyAttributeValues() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"\","
            + "  \"descr\": \"\","
            + "  \"annotation\": \"\""
            + "}"
            + "}";

    AciApplicationProfile profile = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo(""));
    assertThat(profile.getAttributes().getDescription(), equalTo(""));
    assertThat(profile.getAttributes().getAnnotation(), equalTo(""));
  }

  /** Test direct instantiation of AciApplicationProfile. */
  @Test
  public void testDirectInstantiation() {
    AciApplicationProfile profile = new AciApplicationProfile();
    assertThat(profile.getAttributes(), nullValue());
    assertThat(profile.getChildren(), nullValue());
  }

  /** Test setter and getter for attributes field. */
  @Test
  public void testSetGetAttributes() {
    AciApplicationProfile profile = new AciApplicationProfile();
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();

    profile.setAttributes(attributes);
    assertThat(profile.getAttributes(), equalTo(attributes));
  }

  /** Test setter and getter for children field. */
  @Test
  public void testSetGetChildren() {
    AciApplicationProfile profile = new AciApplicationProfile();
    List<Object> children = new ArrayList<>();
    children.add("child1");
    children.add("child2");

    profile.setChildren(children);
    assertThat(profile.getChildren(), equalTo(children));
    assertThat(profile.getChildren(), hasSize(2));
  }

  /** Test setting null attributes. */
  @Test
  public void testSetNullAttributes() {
    AciApplicationProfile profile = new AciApplicationProfile();
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();

    profile.setAttributes(attributes);
    assertThat(profile.getAttributes(), notNullValue());

    profile.setAttributes(null);
    assertThat(profile.getAttributes(), nullValue());
  }

  /** Test setting null children. */
  @Test
  public void testSetNullChildren() {
    AciApplicationProfile profile = new AciApplicationProfile();
    List<Object> children = new ArrayList<>();

    profile.setChildren(children);
    assertThat(profile.getChildren(), notNullValue());

    profile.setChildren(null);
    assertThat(profile.getChildren(), nullValue());
  }

  /** Test direct instantiation of AciApplicationProfileAttributes. */
  @Test
  public void testDirectInstantiationAttributes() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    assertThat(attributes.getAnnotation(), nullValue());
    assertThat(attributes.getDescription(), nullValue());
    assertThat(attributes.getDistinguishedName(), nullValue());
    assertThat(attributes.getExceptionTag(), nullValue());
    assertThat(attributes.getFloodOnEncap(), nullValue());
    assertThat(attributes.getForwardingControl(), nullValue());
    assertThat(attributes.getHasMulticastSource(), nullValue());
    assertThat(attributes.getIsAttributeBasedEpg(), nullValue());
    assertThat(attributes.getMatchType(), nullValue());
    assertThat(attributes.getName(), nullValue());
    assertThat(attributes.getNameAlias(), nullValue());
    assertThat(attributes.getOwnerKey(), nullValue());
    assertThat(attributes.getOwnerTag(), nullValue());
    assertThat(attributes.getPolicyEnforcementPreference(), nullValue());
    assertThat(attributes.getPreferredGroupMember(), nullValue());
    assertThat(attributes.getPriority(), nullValue());
    assertThat(attributes.getShutdown(), nullValue());
    assertThat(attributes.getUserDomain(), nullValue());
  }

  /** Test setter and getter for annotation field. */
  @Test
  public void testSetGetAnnotation() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setAnnotation("test_annotation");
    assertThat(attributes.getAnnotation(), equalTo("test_annotation"));
  }

  /** Test setter and getter for description field. */
  @Test
  public void testSetGetDescription() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setDescription("test description");
    assertThat(attributes.getDescription(), equalTo("test description"));
  }

  /** Test setter and getter for distinguishedName field. */
  @Test
  public void testSetGetDistinguishedName() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setDistinguishedName("uni/tn-tenant1/ap-app1");
    assertThat(attributes.getDistinguishedName(), equalTo("uni/tn-tenant1/ap-app1"));
  }

  /** Test setter and getter for exceptionTag field. */
  @Test
  public void testSetGetExceptionTag() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setExceptionTag("exception1");
    assertThat(attributes.getExceptionTag(), equalTo("exception1"));
  }

  /** Test setter and getter for floodOnEncap field. */
  @Test
  public void testSetGetFloodOnEncap() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setFloodOnEncap("enabled");
    assertThat(attributes.getFloodOnEncap(), equalTo("enabled"));
  }

  /** Test setter and getter for forwardingControl field. */
  @Test
  public void testSetGetForwardingControl() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setForwardingControl("enabled");
    assertThat(attributes.getForwardingControl(), equalTo("enabled"));
  }

  /** Test setter and getter for hasMulticastSource field. */
  @Test
  public void testSetGetHasMulticastSource() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setHasMulticastSource("yes");
    assertThat(attributes.getHasMulticastSource(), equalTo("yes"));
  }

  /** Test setter and getter for isAttributeBasedEpg field. */
  @Test
  public void testSetGetIsAttributeBasedEpg() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setIsAttributeBasedEpg("yes");
    assertThat(attributes.getIsAttributeBasedEpg(), equalTo("yes"));
  }

  /** Test setter and getter for matchType field. */
  @Test
  public void testSetGetMatchType() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setMatchType("atleastOne");
    assertThat(attributes.getMatchType(), equalTo("atleastOne"));
  }

  /** Test setter and getter for name field. */
  @Test
  public void testSetGetName() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setName("test_app");
    assertThat(attributes.getName(), equalTo("test_app"));
  }

  /** Test setter and getter for nameAlias field. */
  @Test
  public void testSetGetNameAlias() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setNameAlias("test_alias");
    assertThat(attributes.getNameAlias(), equalTo("test_alias"));
  }

  /** Test setter and getter for ownerKey field. */
  @Test
  public void testSetGetOwnerKey() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setOwnerKey("key123");
    assertThat(attributes.getOwnerKey(), equalTo("key123"));
  }

  /** Test setter and getter for ownerTag field. */
  @Test
  public void testSetGetOwnerTag() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setOwnerTag("tag456");
    assertThat(attributes.getOwnerTag(), equalTo("tag456"));
  }

  /** Test setter and getter for policyEnforcementPreference field. */
  @Test
  public void testSetGetPolicyEnforcementPreference() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setPolicyEnforcementPreference("enforced");
    assertThat(attributes.getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test setter and getter for preferredGroupMember field. */
  @Test
  public void testSetGetPreferredGroupMember() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setPreferredGroupMember("enabled");
    assertThat(attributes.getPreferredGroupMember(), equalTo("enabled"));
  }

  /** Test setter and getter for priority field. */
  @Test
  public void testSetGetPriority() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setPriority("1");
    assertThat(attributes.getPriority(), equalTo("1"));
  }

  /** Test setter and getter for shutdown field. */
  @Test
  public void testSetGetShutdown() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setShutdown("yes");
    assertThat(attributes.getShutdown(), equalTo("yes"));
  }

  /** Test setter and getter for userDomain field. */
  @Test
  public void testSetGetUserDomain() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setUserDomain("all");
    assertThat(attributes.getUserDomain(), equalTo("all"));
  }

  /** Test setting null values for all attribute fields. */
  @Test
  public void testSetNullAttributeValues() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();

    attributes.setAnnotation(null);
    attributes.setDescription(null);
    attributes.setDistinguishedName(null);
    attributes.setExceptionTag(null);
    attributes.setFloodOnEncap(null);
    attributes.setForwardingControl(null);
    attributes.setHasMulticastSource(null);
    attributes.setIsAttributeBasedEpg(null);
    attributes.setMatchType(null);
    attributes.setName(null);
    attributes.setNameAlias(null);
    attributes.setOwnerKey(null);
    attributes.setOwnerTag(null);
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
    assertThat(attributes.getHasMulticastSource(), nullValue());
    assertThat(attributes.getIsAttributeBasedEpg(), nullValue());
    assertThat(attributes.getMatchType(), nullValue());
    assertThat(attributes.getName(), nullValue());
    assertThat(attributes.getNameAlias(), nullValue());
    assertThat(attributes.getOwnerKey(), nullValue());
    assertThat(attributes.getOwnerTag(), nullValue());
    assertThat(attributes.getPolicyEnforcementPreference(), nullValue());
    assertThat(attributes.getPreferredGroupMember(), nullValue());
    assertThat(attributes.getPriority(), nullValue());
    assertThat(attributes.getShutdown(), nullValue());
    assertThat(attributes.getUserDomain(), nullValue());
  }

  /** Test setting empty string values for all string attributes. */
  @Test
  public void testSetEmptyStringAttributeValues() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();

    attributes.setAnnotation("");
    attributes.setDescription("");
    attributes.setDistinguishedName("");
    attributes.setExceptionTag("");
    attributes.setFloodOnEncap("");
    attributes.setForwardingControl("");
    attributes.setHasMulticastSource("");
    attributes.setIsAttributeBasedEpg("");
    attributes.setMatchType("");
    attributes.setName("");
    attributes.setNameAlias("");
    attributes.setOwnerKey("");
    attributes.setOwnerTag("");
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
    assertThat(attributes.getHasMulticastSource(), equalTo(""));
    assertThat(attributes.getIsAttributeBasedEpg(), equalTo(""));
    assertThat(attributes.getMatchType(), equalTo(""));
    assertThat(attributes.getName(), equalTo(""));
    assertThat(attributes.getNameAlias(), equalTo(""));
    assertThat(attributes.getOwnerKey(), equalTo(""));
    assertThat(attributes.getOwnerTag(), equalTo(""));
    assertThat(attributes.getPolicyEnforcementPreference(), equalTo(""));
    assertThat(attributes.getPreferredGroupMember(), equalTo(""));
    assertThat(attributes.getPriority(), equalTo(""));
    assertThat(attributes.getShutdown(), equalTo(""));
    assertThat(attributes.getUserDomain(), equalTo(""));
  }

  /** Test multiple updates to the same attribute field. */
  @Test
  public void testMultipleUpdatesToAttributeField() {
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();

    attributes.setName("app1");
    assertThat(attributes.getName(), equalTo("app1"));

    attributes.setName("app2");
    assertThat(attributes.getName(), equalTo("app2"));

    attributes.setName("app3");
    assertThat(attributes.getName(), equalTo("app3"));
  }

  /** Test serializability of AciApplicationProfile. */
  @Test
  public void testSerializable() throws IOException {
    AciApplicationProfile profile = new AciApplicationProfile();
    AciApplicationProfile.AciApplicationProfileAttributes attributes =
        new AciApplicationProfile.AciApplicationProfileAttributes();
    attributes.setName("test_app");
    attributes.setDescription("Test Application Profile");
    profile.setAttributes(attributes);

    // Serialize and deserialize
    String json = MAPPER.writeValueAsString(profile);
    AciApplicationProfile deserialized = MAPPER.readValue(json, AciApplicationProfile.class);

    assertThat(deserialized.getAttributes(), notNullValue());
    assertThat(deserialized.getAttributes().getName(), equalTo("test_app"));
    assertThat(deserialized.getAttributes().getDescription(), equalTo("Test Application Profile"));
  }
}
