package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.AciBgpContextPolicy;
import org.junit.Test;

/**
 * Tests for {@link AciBgpContextPolicy} model class, covering JSON deserialization,
 * getters/setters, and edge cases.
 */
public final class AciBgpContextPolicyModelTest {

  private static final ObjectMapper MAPPER = BatfishObjectMapper.ignoreUnknownMapper();

  /** Test deserialization with all fields populated. */
  @Test
  public void testDeserialize_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"annotation\": \"test_annotation\","
            + "  \"descr\": \"BGP context policy description\","
            + "  \"dn\": \"sys/ctx-[tenant1]/bgpCtxPol-[bgp1]\","
            + "  \"grCtrl\": \"l3\","
            + "  \"holdIntvl\": \"180\","
            + "  \"kaIntvl\": \"60\","
            + "  \"maxAsLimit\": \"50\","
            + "  \"name\": \"bgp1\","
            + "  \"nameAlias\": \"bgp_policy_1\","
            + "  \"ownerKey\": \"key123\","
            + "  \"ownerTag\": \"tag456\","
            + "  \"staleIntvl\": \"300\","
            + "  \"userdom\": \"user1:domain1\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs = policy.getAttributes();

    assertEquals("test_annotation", attrs.getAnnotation());
    assertEquals("BGP context policy description", attrs.getDescription());
    assertEquals("sys/ctx-[tenant1]/bgpCtxPol-[bgp1]", attrs.getDistinguishedName());
    assertEquals("l3", attrs.getGracefulRestartControl());
    assertEquals("180", attrs.getHoldInterval());
    assertEquals("60", attrs.getKeepaliveInterval());
    assertEquals("50", attrs.getMaxAsLimit());
    assertEquals("bgp1", attrs.getName());
    assertEquals("bgp_policy_1", attrs.getNameAlias());
    assertEquals("key123", attrs.getOwnerKey());
    assertEquals("tag456", attrs.getOwnerTag());
    assertEquals("300", attrs.getStaleInterval());
    assertEquals("user1:domain1", attrs.getUserDomain());
  }

  /** Test deserialization with null/empty attributes. */
  @Test
  public void testDeserialize_emptyAttributes() throws IOException {
    String json = "{\"attributes\": {}}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertThat(policy.getAttributes().getName(), nullValue());
    assertThat(policy.getAttributes().getDescription(), nullValue());
  }

  /** Test deserialization with null attributes. */
  @Test
  public void testDeserialize_nullAttributes() throws IOException {
    String json = "{}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertThat(policy.getAttributes(), nullValue());
  }

  /** Test getter and setter for annotation field. */
  @Test
  public void testGetterSetter_annotation() {
    AciBgpContextPolicy policy = new AciBgpContextPolicy();
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setAnnotation("test_annotation");
    assertEquals("test_annotation", attrs.getAnnotation());

    attrs.setAnnotation(null);
    assertThat(attrs.getAnnotation(), nullValue());

    attrs.setAnnotation("");
    assertEquals("", attrs.getAnnotation());

    policy.setAttributes(attrs);
    assertEquals(attrs, policy.getAttributes());
  }

  /** Test getter and setter for description field. */
  @Test
  public void testGetterSetter_description() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setDescription("Test description");
    assertEquals("Test description", attrs.getDescription());

    attrs.setDescription(null);
    assertThat(attrs.getDescription(), nullValue());

    attrs.setDescription("");
    assertEquals("", attrs.getDescription());
  }

  /** Test getter and setter for distinguishedName field. */
  @Test
  public void testGetterSetter_distinguishedName() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setDistinguishedName("sys/ctx-[tenant]/bgpCtxPol-[policy]");
    assertEquals("sys/ctx-[tenant]/bgpCtxPol-[policy]", attrs.getDistinguishedName());

    attrs.setDistinguishedName(null);
    assertThat(attrs.getDistinguishedName(), nullValue());

    attrs.setDistinguishedName("");
    assertEquals("", attrs.getDistinguishedName());
  }

  /** Test getter and setter for gracefulRestartControl field. */
  @Test
  public void testGetterSetter_gracefulRestartControl() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setGracefulRestartControl("l3");
    assertEquals("l3", attrs.getGracefulRestartControl());

    attrs.setGracefulRestartControl("l2");
    assertEquals("l2", attrs.getGracefulRestartControl());

    attrs.setGracefulRestartControl(null);
    assertThat(attrs.getGracefulRestartControl(), nullValue());

    attrs.setGracefulRestartControl("");
    assertEquals("", attrs.getGracefulRestartControl());
  }

  /** Test getter and setter for holdInterval field. */
  @Test
  public void testGetterSetter_holdInterval() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setHoldInterval("180");
    assertEquals("180", attrs.getHoldInterval());

    attrs.setHoldInterval("240");
    assertEquals("240", attrs.getHoldInterval());

    attrs.setHoldInterval(null);
    assertThat(attrs.getHoldInterval(), nullValue());

    attrs.setHoldInterval("");
    assertEquals("", attrs.getHoldInterval());
  }

  /** Test getter and setter for keepaliveInterval field. */
  @Test
  public void testGetterSetter_keepaliveInterval() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setKeepaliveInterval("60");
    assertEquals("60", attrs.getKeepaliveInterval());

    attrs.setKeepaliveInterval("80");
    assertEquals("80", attrs.getKeepaliveInterval());

    attrs.setKeepaliveInterval(null);
    assertThat(attrs.getKeepaliveInterval(), nullValue());

    attrs.setKeepaliveInterval("");
    assertEquals("", attrs.getKeepaliveInterval());
  }

  /** Test getter and setter for maxAsLimit field. */
  @Test
  public void testGetterSetter_maxAsLimit() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setMaxAsLimit("50");
    assertEquals("50", attrs.getMaxAsLimit());

    attrs.setMaxAsLimit("100");
    assertEquals("100", attrs.getMaxAsLimit());

    attrs.setMaxAsLimit(null);
    assertThat(attrs.getMaxAsLimit(), nullValue());

    attrs.setMaxAsLimit("");
    assertEquals("", attrs.getMaxAsLimit());
  }

  /** Test getter and setter for name field. */
  @Test
  public void testGetterSetter_name() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setName("bgp_policy1");
    assertEquals("bgp_policy1", attrs.getName());

    attrs.setName("bgp_policy2");
    assertEquals("bgp_policy2", attrs.getName());

    attrs.setName(null);
    assertThat(attrs.getName(), nullValue());

    attrs.setName("");
    assertEquals("", attrs.getName());
  }

  /** Test getter and setter for nameAlias field. */
  @Test
  public void testGetterSetter_nameAlias() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setNameAlias("BGP Policy 1");
    assertEquals("BGP Policy 1", attrs.getNameAlias());

    attrs.setNameAlias(null);
    assertThat(attrs.getNameAlias(), nullValue());

    attrs.setNameAlias("");
    assertEquals("", attrs.getNameAlias());
  }

  /** Test getter and setter for ownerKey field. */
  @Test
  public void testGetterSetter_ownerKey() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setOwnerKey("key123");
    assertEquals("key123", attrs.getOwnerKey());

    attrs.setOwnerKey(null);
    assertThat(attrs.getOwnerKey(), nullValue());

    attrs.setOwnerKey("");
    assertEquals("", attrs.getOwnerKey());
  }

  /** Test getter and setter for ownerTag field. */
  @Test
  public void testGetterSetter_ownerTag() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setOwnerTag("tag456");
    assertEquals("tag456", attrs.getOwnerTag());

    attrs.setOwnerTag(null);
    assertThat(attrs.getOwnerTag(), nullValue());

    attrs.setOwnerTag("");
    assertEquals("", attrs.getOwnerTag());
  }

  /** Test getter and setter for staleInterval field. */
  @Test
  public void testGetterSetter_staleInterval() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setStaleInterval("300");
    assertEquals("300", attrs.getStaleInterval());

    attrs.setStaleInterval("600");
    assertEquals("600", attrs.getStaleInterval());

    attrs.setStaleInterval(null);
    assertThat(attrs.getStaleInterval(), nullValue());

    attrs.setStaleInterval("");
    assertEquals("", attrs.getStaleInterval());
  }

  /** Test getter and setter for userDomain field. */
  @Test
  public void testGetterSetter_userDomain() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setUserDomain("user1:domain1");
    assertEquals("user1:domain1", attrs.getUserDomain());

    attrs.setUserDomain("user2:domain2");
    assertEquals("user2:domain2", attrs.getUserDomain());

    attrs.setUserDomain(null);
    assertThat(attrs.getUserDomain(), nullValue());

    attrs.setUserDomain("");
    assertEquals("", attrs.getUserDomain());
  }

  /** Test getter and setter for attributes field. */
  @Test
  public void testGetterSetter_attributes() {
    AciBgpContextPolicy policy = new AciBgpContextPolicy();
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setName("test_policy");
    policy.setAttributes(attrs);
    assertEquals(attrs, policy.getAttributes());
    assertEquals("test_policy", policy.getAttributes().getName());

    policy.setAttributes(null);
    assertThat(policy.getAttributes(), nullValue());
  }

  /** Test getter and setter for children field. */
  @Test
  public void testGetterSetter_children() {
    AciBgpContextPolicy policy = new AciBgpContextPolicy();

    policy.setChildren(null);
    assertThat(policy.getChildren(), nullValue());

    // Note: Testing with actual List content would require creating actual child objects
    // which may not exist in the current codebase
  }

  /** Test deserialization with BGP timer fields. */
  @Test
  public void testDeserialize_timerFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"timer_policy\","
            + "  \"holdIntvl\": \"180\","
            + "  \"kaIntvl\": \"60\","
            + "  \"staleIntvl\": \"300\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("180", policy.getAttributes().getHoldInterval());
    assertEquals("60", policy.getAttributes().getKeepaliveInterval());
    assertEquals("300", policy.getAttributes().getStaleInterval());
  }

  /** Test deserialization with graceful restart fields. */
  @Test
  public void testDeserialize_gracefulRestartFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"gr_policy\","
            + "  \"grCtrl\": \"l3\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("l3", policy.getAttributes().getGracefulRestartControl());
  }

  /** Test deserialization with AS limit field. */
  @Test
  public void testDeserialize_maxAsLimitField() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"as_limit_policy\","
            + "  \"maxAsLimit\": \"50\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("50", policy.getAttributes().getMaxAsLimit());
  }

  /** Test deserialization with owner fields. */
  @Test
  public void testDeserialize_ownerFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"owned_policy\","
            + "  \"ownerKey\": \"owner1\","
            + "  \"ownerTag\": \"tag1\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("owner1", policy.getAttributes().getOwnerKey());
    assertEquals("tag1", policy.getAttributes().getOwnerTag());
  }

  /** Test deserialization with distinguished name. */
  @Test
  public void testDeserialize_distinguishedName() throws IOException {
    String json =
        "{" + "\"attributes\": {" + "  \"dn\": \"sys/ctx-[tenant1]/bgpCtxPol-[bgp1]\"" + "}" + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals(
        "sys/ctx-[tenant1]/bgpCtxPol-[bgp1]", policy.getAttributes().getDistinguishedName());
  }

  /** Test deserialization with name and alias. */
  @Test
  public void testDeserialize_nameAndAlias() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bgp1\","
            + "  \"nameAlias\": \"BGP Policy 1\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("bgp1", policy.getAttributes().getName());
    assertEquals("BGP Policy 1", policy.getAttributes().getNameAlias());
  }

  /** Test deserialization with user domain. */
  @Test
  public void testDeserialize_userDomain() throws IOException {
    String json = "{" + "\"attributes\": {" + "  \"userdom\": \"user1:domain1\"" + "}" + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("user1:domain1", policy.getAttributes().getUserDomain());
  }

  /** Test serialization and deserialization round-trip. */
  @Test
  public void testSerializationRoundTrip() throws IOException {
    AciBgpContextPolicy original = new AciBgpContextPolicy();
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setName("test_policy");
    attrs.setDescription("Test BGP context policy");
    attrs.setHoldInterval("180");
    attrs.setKeepaliveInterval("60");
    attrs.setGracefulRestartControl("l3");
    attrs.setMaxAsLimit("50");
    original.setAttributes(attrs);

    String serialized = MAPPER.writeValueAsString(original);
    AciBgpContextPolicy deserialized = MAPPER.readValue(serialized, AciBgpContextPolicy.class);

    assertEquals(original.getAttributes().getName(), deserialized.getAttributes().getName());
    assertEquals(
        original.getAttributes().getDescription(), deserialized.getAttributes().getDescription());
    assertEquals(
        original.getAttributes().getHoldInterval(), deserialized.getAttributes().getHoldInterval());
    assertEquals(
        original.getAttributes().getKeepaliveInterval(),
        deserialized.getAttributes().getKeepaliveInterval());
    assertEquals(
        original.getAttributes().getGracefulRestartControl(),
        deserialized.getAttributes().getGracefulRestartControl());
    assertEquals(
        original.getAttributes().getMaxAsLimit(), deserialized.getAttributes().getMaxAsLimit());
  }

  /** Test edge case: zero values for numeric string fields. */
  @Test
  public void testEdgeCase_zeroValues() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"holdIntvl\": \"0\","
            + "  \"kaIntvl\": \"0\","
            + "  \"maxAsLimit\": \"0\","
            + "  \"staleIntvl\": \"0\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("0", policy.getAttributes().getHoldInterval());
    assertEquals("0", policy.getAttributes().getKeepaliveInterval());
    assertEquals("0", policy.getAttributes().getMaxAsLimit());
    assertEquals("0", policy.getAttributes().getStaleInterval());
  }

  /** Test edge case: special characters in string fields. */
  @Test
  public void testEdgeCase_specialCharacters() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"policy-with-dashes\","
            + "  \"descr\": \"Description with special chars: <>&\\\"'\","
            + "  \"userdom\": \"user:domain:subdomain\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("policy-with-dashes", policy.getAttributes().getName());
    assertEquals("Description with special chars: <>&\"'", policy.getAttributes().getDescription());
    assertEquals("user:domain:subdomain", policy.getAttributes().getUserDomain());
  }

  /** Test edge case: very long string values. */
  @Test
  public void testEdgeCase_longStrings() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    String longString =
        "a".repeat(1000); // Create a very long string (though likely shorter than real limits)

    attrs.setDescription(longString);
    assertEquals(longString, attrs.getDescription());

    attrs.setDistinguishedName(longString);
    assertEquals(longString, attrs.getDistinguishedName());
  }

  /** Test edge case: setting all fields to null. */
  @Test
  public void testEdgeCase_allNulls() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setAnnotation(null);
    attrs.setDescription(null);
    attrs.setDistinguishedName(null);
    attrs.setGracefulRestartControl(null);
    attrs.setHoldInterval(null);
    attrs.setKeepaliveInterval(null);
    attrs.setMaxAsLimit(null);
    attrs.setName(null);
    attrs.setNameAlias(null);
    attrs.setOwnerKey(null);
    attrs.setOwnerTag(null);
    attrs.setStaleInterval(null);
    attrs.setUserDomain(null);

    assertThat(attrs.getAnnotation(), nullValue());
    assertThat(attrs.getDescription(), nullValue());
    assertThat(attrs.getDistinguishedName(), nullValue());
    assertThat(attrs.getGracefulRestartControl(), nullValue());
    assertThat(attrs.getHoldInterval(), nullValue());
    assertThat(attrs.getKeepaliveInterval(), nullValue());
    assertThat(attrs.getMaxAsLimit(), nullValue());
    assertThat(attrs.getName(), nullValue());
    assertThat(attrs.getNameAlias(), nullValue());
    assertThat(attrs.getOwnerKey(), nullValue());
    assertThat(attrs.getOwnerTag(), nullValue());
    assertThat(attrs.getStaleInterval(), nullValue());
    assertThat(attrs.getUserDomain(), nullValue());
  }

  /** Test edge case: setting all fields to empty strings. */
  @Test
  public void testEdgeCase_allEmptyStrings() {
    AciBgpContextPolicy.AciBgpContextPolicyAttributes attrs =
        new AciBgpContextPolicy.AciBgpContextPolicyAttributes();

    attrs.setAnnotation("");
    attrs.setDescription("");
    attrs.setDistinguishedName("");
    attrs.setGracefulRestartControl("");
    attrs.setHoldInterval("");
    attrs.setKeepaliveInterval("");
    attrs.setMaxAsLimit("");
    attrs.setName("");
    attrs.setNameAlias("");
    attrs.setOwnerKey("");
    attrs.setOwnerTag("");
    attrs.setStaleInterval("");
    attrs.setUserDomain("");

    assertEquals("", attrs.getAnnotation());
    assertEquals("", attrs.getDescription());
    assertEquals("", attrs.getDistinguishedName());
    assertEquals("", attrs.getGracefulRestartControl());
    assertEquals("", attrs.getHoldInterval());
    assertEquals("", attrs.getKeepaliveInterval());
    assertEquals("", attrs.getMaxAsLimit());
    assertEquals("", attrs.getName());
    assertEquals("", attrs.getNameAlias());
    assertEquals("", attrs.getOwnerKey());
    assertEquals("", attrs.getOwnerTag());
    assertEquals("", attrs.getStaleInterval());
    assertEquals("", attrs.getUserDomain());
  }

  /** Test that unknown properties are ignored during deserialization. */
  @Test
  public void testDeserialize_UnknownPropertiesIgnored() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"policy1\","
            + "  \"unknownField\": \"should be ignored\","
            + "  \"anotherUnknown\": 12345"
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertEquals("policy1", policy.getAttributes().getName());
  }
}
