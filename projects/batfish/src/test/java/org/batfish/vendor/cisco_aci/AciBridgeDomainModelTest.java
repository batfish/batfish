package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.apic.AciBridgeDomain;
import org.junit.Test;

/**
 * Tests for {@link AciBridgeDomain} model class, covering JSON deserialization, getters/setters,
 * and edge cases.
 */
public final class AciBridgeDomainModelTest {

  private static final ObjectMapper MAPPER = BatfishObjectMapper.ignoreUnknownMapper();

  /** Test deserialization with all fields populated. */
  @Test
  public void testDeserialize_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"annotation\": \"test_annotation\","
            + "  \"arpFlood\": \"yes\","
            + "  \"descr\": \"Bridge domain description\","
            + "  \"dn\": \"uni/tn-[tenant1]/BD-[bd1]\","
            + "  \"epClear\": \"yes\","
            + "  \"epMoveDetectMode\": \"global\","
            + "  \"hostBasedRouting\": \"enabled\","
            + "  \"ipLearning\": \"enabled\","
            + "  \"llAddr\": \"fe80::/10\","
            + "  \"mac\": \"00:22:BD:F8:19:FF\","
            + "  \"mcastAllow\": \"yes\","
            + "  \"multiDstPktAct\": \"bd-flood\","
            + "  \"name\": \"bd1\","
            + "  \"nameAlias\": \"Bridge Domain 1\","
            + "  \"type\": \"regular\","
            + "  \"unicastRoute\": \"yes\","
            + "  \"unkMacUcastAct\": \"proxy\","
            + "  \"unkMcastAct\": \"flood\","
            + "  \"userdom\": \"user1:domain1\","
            + "  \"v6unkMcastAct\": \"flood\","
            + "  \"vmac\": \"00:22:BD:F8:19:FE\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    AciBridgeDomain.AciBridgeDomainAttributes attrs = bd.getAttributes();

    assertEquals("test_annotation", attrs.getAnnotation());
    assertEquals("yes", attrs.getArpFlood());
    assertEquals("Bridge domain description", attrs.getDescription());
    assertEquals("uni/tn-[tenant1]/BD-[bd1]", attrs.getDistinguishedName());
    assertEquals("yes", attrs.getEpClear());
    assertEquals("global", attrs.getEpMoveDetectMode());
    assertEquals("enabled", attrs.getHostBasedRouting());
    assertEquals("enabled", attrs.getIpLearning());
    assertEquals("fe80::/10", attrs.getLinkLocalAddr());
    assertEquals("00:22:BD:F8:19:FF", attrs.getMacAddress());
    assertEquals("yes", attrs.getMulticastAllow());
    assertEquals("bd-flood", attrs.getMultiDestPacketAction());
    assertEquals("bd1", attrs.getName());
    assertEquals("Bridge Domain 1", attrs.getNameAlias());
    assertEquals("regular", attrs.getType());
    assertEquals("yes", attrs.getUnicastRoute());
    assertEquals("proxy", attrs.getUnknownMacUcastAction());
    assertEquals("flood", attrs.getUnknownMcastAction());
    assertEquals("user1:domain1", attrs.getUserDomain());
    assertEquals("flood", attrs.getV6UnknownMcastAction());
    assertEquals("00:22:BD:F8:19:FE", attrs.getVirtualMac());
  }

  /** Test deserialization with null/empty attributes. */
  @Test
  public void testDeserialize_emptyAttributes() throws IOException {
    String json = "{\"attributes\": {}}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    // When attributes is present but empty, Jackson creates an empty object
    assertThat(bd.getAttributes().getName(), nullValue());
    assertThat(bd.getAttributes().getArpFlood(), nullValue());
    assertThat(bd.getAttributes().getDescription(), nullValue());
  }

  /** Test deserialization with null attributes. */
  @Test
  public void testDeserialize_nullAttributes() throws IOException {
    String json = "{}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), nullValue());
  }

  /** Test getter and setter for arpFlood field. */
  @Test
  public void testGetterSetter_arpFlood() {
    AciBridgeDomain bd = new AciBridgeDomain();
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setArpFlood("yes");
    assertEquals("yes", attrs.getArpFlood());

    attrs.setArpFlood("no");
    assertEquals("no", attrs.getArpFlood());

    attrs.setArpFlood(null);
    assertThat(attrs.getArpFlood(), nullValue());

    attrs.setArpFlood("");
    assertEquals("", attrs.getArpFlood());

    bd.setAttributes(attrs);
    assertEquals(attrs, bd.getAttributes());
  }

  /** Test getter and setter for annotation field. */
  @Test
  public void testGetterSetter_annotation() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setAnnotation("test_annotation");
    assertEquals("test_annotation", attrs.getAnnotation());

    attrs.setAnnotation(null);
    assertThat(attrs.getAnnotation(), nullValue());

    attrs.setAnnotation("");
    assertEquals("", attrs.getAnnotation());
  }

  /** Test getter and setter for description field. */
  @Test
  public void testGetterSetter_description() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

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
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setDistinguishedName("uni/tn-[tenant]/BD-[bd1]");
    assertEquals("uni/tn-[tenant]/BD-[bd1]", attrs.getDistinguishedName());

    attrs.setDistinguishedName(null);
    assertThat(attrs.getDistinguishedName(), nullValue());

    attrs.setDistinguishedName("");
    assertEquals("", attrs.getDistinguishedName());
  }

  /** Test getter and setter for epClear field. */
  @Test
  public void testGetterSetter_epClear() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setEpClear("yes");
    assertEquals("yes", attrs.getEpClear());

    attrs.setEpClear("no");
    assertEquals("no", attrs.getEpClear());

    attrs.setEpClear(null);
    assertThat(attrs.getEpClear(), nullValue());

    attrs.setEpClear("");
    assertEquals("", attrs.getEpClear());
  }

  /** Test getter and setter for epMoveDetectMode field. */
  @Test
  public void testGetterSetter_epMoveDetectMode() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setEpMoveDetectMode("global");
    assertEquals("global", attrs.getEpMoveDetectMode());

    attrs.setEpMoveDetectMode("local");
    assertEquals("local", attrs.getEpMoveDetectMode());

    attrs.setEpMoveDetectMode(null);
    assertThat(attrs.getEpMoveDetectMode(), nullValue());

    attrs.setEpMoveDetectMode("");
    assertEquals("", attrs.getEpMoveDetectMode());
  }

  /** Test getter and setter for hostBasedRouting field. */
  @Test
  public void testGetterSetter_hostBasedRouting() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setHostBasedRouting("enabled");
    assertEquals("enabled", attrs.getHostBasedRouting());

    attrs.setHostBasedRouting("disabled");
    assertEquals("disabled", attrs.getHostBasedRouting());

    attrs.setHostBasedRouting(null);
    assertThat(attrs.getHostBasedRouting(), nullValue());

    attrs.setHostBasedRouting("");
    assertEquals("", attrs.getHostBasedRouting());
  }

  /** Test getter and setter for ipLearning field. */
  @Test
  public void testGetterSetter_ipLearning() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setIpLearning("enabled");
    assertEquals("enabled", attrs.getIpLearning());

    attrs.setIpLearning("disabled");
    assertEquals("disabled", attrs.getIpLearning());

    attrs.setIpLearning(null);
    assertThat(attrs.getIpLearning(), nullValue());

    attrs.setIpLearning("");
    assertEquals("", attrs.getIpLearning());
  }

  /** Test getter and setter for linkLocalAddr field. */
  @Test
  public void testGetterSetter_linkLocalAddr() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setLinkLocalAddr("fe80::/10");
    assertEquals("fe80::/10", attrs.getLinkLocalAddr());

    attrs.setLinkLocalAddr("fe80::1");
    assertEquals("fe80::1", attrs.getLinkLocalAddr());

    attrs.setLinkLocalAddr(null);
    assertThat(attrs.getLinkLocalAddr(), nullValue());

    attrs.setLinkLocalAddr("");
    assertEquals("", attrs.getLinkLocalAddr());
  }

  /** Test getter and setter for macAddress field. */
  @Test
  public void testGetterSetter_macAddress() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setMacAddress("00:22:BD:F8:19:FF");
    assertEquals("00:22:BD:F8:19:FF", attrs.getMacAddress());

    attrs.setMacAddress("00:11:22:33:44:55");
    assertEquals("00:11:22:33:44:55", attrs.getMacAddress());

    attrs.setMacAddress(null);
    assertThat(attrs.getMacAddress(), nullValue());

    attrs.setMacAddress("");
    assertEquals("", attrs.getMacAddress());
  }

  /** Test getter and setter for multicastAllow field. */
  @Test
  public void testGetterSetter_multicastAllow() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setMulticastAllow("yes");
    assertEquals("yes", attrs.getMulticastAllow());

    attrs.setMulticastAllow("no");
    assertEquals("no", attrs.getMulticastAllow());

    attrs.setMulticastAllow(null);
    assertThat(attrs.getMulticastAllow(), nullValue());

    attrs.setMulticastAllow("");
    assertEquals("", attrs.getMulticastAllow());
  }

  /** Test getter and setter for multiDestPacketAction field. */
  @Test
  public void testGetterSetter_multiDestPacketAction() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setMultiDestPacketAction("bd-flood");
    assertEquals("bd-flood", attrs.getMultiDestPacketAction());

    attrs.setMultiDestPacketAction("drop");
    assertEquals("drop", attrs.getMultiDestPacketAction());

    attrs.setMultiDestPacketAction(null);
    assertThat(attrs.getMultiDestPacketAction(), nullValue());

    attrs.setMultiDestPacketAction("");
    assertEquals("", attrs.getMultiDestPacketAction());
  }

  /** Test getter and setter for name field. */
  @Test
  public void testGetterSetter_name() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setName("bd1");
    assertEquals("bd1", attrs.getName());

    attrs.setName("bd2");
    assertEquals("bd2", attrs.getName());

    attrs.setName(null);
    assertThat(attrs.getName(), nullValue());

    attrs.setName("");
    assertEquals("", attrs.getName());
  }

  /** Test getter and setter for nameAlias field. */
  @Test
  public void testGetterSetter_nameAlias() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setNameAlias("Bridge Domain 1");
    assertEquals("Bridge Domain 1", attrs.getNameAlias());

    attrs.setNameAlias(null);
    assertThat(attrs.getNameAlias(), nullValue());

    attrs.setNameAlias("");
    assertEquals("", attrs.getNameAlias());
  }

  /** Test getter and setter for type field. */
  @Test
  public void testGetterSetter_type() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setType("regular");
    assertEquals("regular", attrs.getType());

    attrs.setType("tw-way");
    assertEquals("tw-way", attrs.getType());

    attrs.setType(null);
    assertThat(attrs.getType(), nullValue());

    attrs.setType("");
    assertEquals("", attrs.getType());
  }

  /** Test getter and setter for unicastRoute field. */
  @Test
  public void testGetterSetter_unicastRoute() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setUnicastRoute("yes");
    assertEquals("yes", attrs.getUnicastRoute());

    attrs.setUnicastRoute("no");
    assertEquals("no", attrs.getUnicastRoute());

    attrs.setUnicastRoute(null);
    assertThat(attrs.getUnicastRoute(), nullValue());

    attrs.setUnicastRoute("");
    assertEquals("", attrs.getUnicastRoute());
  }

  /** Test getter and setter for unknownMacUcastAction field. */
  @Test
  public void testGetterSetter_unknownMacUcastAction() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setUnknownMacUcastAction("proxy");
    assertEquals("proxy", attrs.getUnknownMacUcastAction());

    attrs.setUnknownMacUcastAction("flood");
    assertEquals("flood", attrs.getUnknownMacUcastAction());

    attrs.setUnknownMacUcastAction(null);
    assertThat(attrs.getUnknownMacUcastAction(), nullValue());

    attrs.setUnknownMacUcastAction("");
    assertEquals("", attrs.getUnknownMacUcastAction());
  }

  /** Test getter and setter for unknownMcastAction field. */
  @Test
  public void testGetterSetter_unknownMcastAction() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setUnknownMcastAction("flood");
    assertEquals("flood", attrs.getUnknownMcastAction());

    attrs.setUnknownMcastAction("drop");
    assertEquals("drop", attrs.getUnknownMcastAction());

    attrs.setUnknownMcastAction(null);
    assertThat(attrs.getUnknownMcastAction(), nullValue());

    attrs.setUnknownMcastAction("");
    assertEquals("", attrs.getUnknownMcastAction());
  }

  /** Test getter and setter for userDomain field. */
  @Test
  public void testGetterSetter_userDomain() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setUserDomain("user1:domain1");
    assertEquals("user1:domain1", attrs.getUserDomain());

    attrs.setUserDomain("user2:domain2");
    assertEquals("user2:domain2", attrs.getUserDomain());

    attrs.setUserDomain(null);
    assertThat(attrs.getUserDomain(), nullValue());

    attrs.setUserDomain("");
    assertEquals("", attrs.getUserDomain());
  }

  /** Test getter and setter for v6UnknownMcastAction field. */
  @Test
  public void testGetterSetter_v6UnknownMcastAction() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setV6UnknownMcastAction("flood");
    assertEquals("flood", attrs.getV6UnknownMcastAction());

    attrs.setV6UnknownMcastAction("drop");
    assertEquals("drop", attrs.getV6UnknownMcastAction());

    attrs.setV6UnknownMcastAction(null);
    assertThat(attrs.getV6UnknownMcastAction(), nullValue());

    attrs.setV6UnknownMcastAction("");
    assertEquals("", attrs.getV6UnknownMcastAction());
  }

  /** Test getter and setter for virtualMac field. */
  @Test
  public void testGetterSetter_virtualMac() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setVirtualMac("00:22:BD:F8:19:FE");
    assertEquals("00:22:BD:F8:19:FE", attrs.getVirtualMac());

    attrs.setVirtualMac("00:11:22:33:44:66");
    assertEquals("00:11:22:33:44:66", attrs.getVirtualMac());

    attrs.setVirtualMac(null);
    assertThat(attrs.getVirtualMac(), nullValue());

    attrs.setVirtualMac("");
    assertEquals("", attrs.getVirtualMac());
  }

  /** Test getter and setter for attributes field. */
  @Test
  public void testGetterSetter_attributes() {
    AciBridgeDomain bd = new AciBridgeDomain();
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setName("bd1");
    bd.setAttributes(attrs);
    assertEquals(attrs, bd.getAttributes());
    assertEquals("bd1", bd.getAttributes().getName());

    bd.setAttributes(null);
    assertThat(bd.getAttributes(), nullValue());
  }

  /** Test getter and setter for children field. */
  @Test
  public void testGetterSetter_children() {
    AciBridgeDomain bd = new AciBridgeDomain();

    bd.setChildren(null);
    assertThat(bd.getChildren(), nullValue());

    // Note: Testing with actual List content would require creating actual child objects
  }

  /** Test deserialization with ARP flood control fields. */
  @Test
  public void testDeserialize_arpFloodFields() throws IOException {
    String json =
        "{" + "\"attributes\": {" + "  \"name\": \"bd1\"," + "  \"arpFlood\": \"yes\"" + "}" + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("yes", bd.getAttributes().getArpFlood());
  }

  /** Test deserialization with endpoint learning fields. */
  @Test
  public void testDeserialize_endpointLearningFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"epClear\": \"yes\","
            + "  \"epMoveDetectMode\": \"local\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("yes", bd.getAttributes().getEpClear());
    assertEquals("local", bd.getAttributes().getEpMoveDetectMode());
  }

  /** Test deserialization with routing fields. */
  @Test
  public void testDeserialize_routingFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"hostBasedRouting\": \"enabled\","
            + "  \"ipLearning\": \"enabled\","
            + "  \"unicastRoute\": \"yes\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("enabled", bd.getAttributes().getHostBasedRouting());
    assertEquals("enabled", bd.getAttributes().getIpLearning());
    assertEquals("yes", bd.getAttributes().getUnicastRoute());
  }

  /** Test deserialization with MAC address fields. */
  @Test
  public void testDeserialize_macAddressFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"mac\": \"00:22:BD:F8:19:FF\","
            + "  \"vmac\": \"00:22:BD:F8:19:FE\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("00:22:BD:F8:19:FF", bd.getAttributes().getMacAddress());
    assertEquals("00:22:BD:F8:19:FE", bd.getAttributes().getVirtualMac());
  }

  /** Test deserialization with multicast fields. */
  @Test
  public void testDeserialize_multicastFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"mcastAllow\": \"yes\","
            + "  \"multiDstPktAct\": \"bd-flood\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("yes", bd.getAttributes().getMulticastAllow());
    assertEquals("bd-flood", bd.getAttributes().getMultiDestPacketAction());
  }

  /** Test deserialization with unknown action fields. */
  @Test
  public void testDeserialize_unknownActionFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"unkMacUcastAct\": \"proxy\","
            + "  \"unkMcastAct\": \"flood\","
            + "  \"v6unkMcastAct\": \"drop\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("proxy", bd.getAttributes().getUnknownMacUcastAction());
    assertEquals("flood", bd.getAttributes().getUnknownMcastAction());
    assertEquals("drop", bd.getAttributes().getV6UnknownMcastAction());
  }

  /** Test deserialization with link local address. */
  @Test
  public void testDeserialize_linkLocalAddr() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"llAddr\": \"fe80::/10\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("fe80::/10", bd.getAttributes().getLinkLocalAddr());
  }

  /** Test deserialization with distinguished name. */
  @Test
  public void testDeserialize_distinguishedName() throws IOException {
    String json = "{" + "\"attributes\": {" + "  \"dn\": \"uni/tn-[tenant1]/BD-[bd1]\"" + "}" + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("uni/tn-[tenant1]/BD-[bd1]", bd.getAttributes().getDistinguishedName());
  }

  /** Test deserialization with name and alias. */
  @Test
  public void testDeserialize_nameAndAlias() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"nameAlias\": \"Bridge Domain 1\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("bd1", bd.getAttributes().getName());
    assertEquals("Bridge Domain 1", bd.getAttributes().getNameAlias());
  }

  /** Test deserialization with type field. */
  @Test
  public void testDeserialize_type() throws IOException {
    String json =
        "{" + "\"attributes\": {" + "  \"name\": \"bd1\"," + "  \"type\": \"regular\"" + "}" + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("regular", bd.getAttributes().getType());
  }

  /** Test deserialization with user domain. */
  @Test
  public void testDeserialize_userDomain() throws IOException {
    String json = "{" + "\"attributes\": {" + "  \"userdom\": \"user1:domain1\"" + "}" + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("user1:domain1", bd.getAttributes().getUserDomain());
  }

  /** Test serialization and deserialization round-trip. */
  @Test
  public void testSerializationRoundTrip() throws IOException {
    AciBridgeDomain original = new AciBridgeDomain();
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setName("bd1");
    attrs.setDescription("Test bridge domain");
    attrs.setArpFlood("yes");
    attrs.setUnicastRoute("yes");
    attrs.setMacAddress("00:22:BD:F8:19:FF");
    attrs.setMultiDestPacketAction("bd-flood");
    attrs.setEpMoveDetectMode("global");
    attrs.setIpLearning("enabled");
    original.setAttributes(attrs);

    String serialized = MAPPER.writeValueAsString(original);
    AciBridgeDomain deserialized = MAPPER.readValue(serialized, AciBridgeDomain.class);

    assertEquals(original.getAttributes().getName(), deserialized.getAttributes().getName());
    assertEquals(
        original.getAttributes().getDescription(), deserialized.getAttributes().getDescription());
    assertEquals(
        original.getAttributes().getArpFlood(), deserialized.getAttributes().getArpFlood());
    assertEquals(
        original.getAttributes().getUnicastRoute(), deserialized.getAttributes().getUnicastRoute());
    assertEquals(
        original.getAttributes().getMacAddress(), deserialized.getAttributes().getMacAddress());
    assertEquals(
        original.getAttributes().getMultiDestPacketAction(),
        deserialized.getAttributes().getMultiDestPacketAction());
    assertEquals(
        original.getAttributes().getEpMoveDetectMode(),
        deserialized.getAttributes().getEpMoveDetectMode());
    assertEquals(
        original.getAttributes().getIpLearning(), deserialized.getAttributes().getIpLearning());
  }

  /** Test edge case: setting all fields to null. */
  @Test
  public void testEdgeCase_allNulls() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setArpFlood(null);
    attrs.setAnnotation(null);
    attrs.setDescription(null);
    attrs.setDistinguishedName(null);
    attrs.setEpClear(null);
    attrs.setEpMoveDetectMode(null);
    attrs.setHostBasedRouting(null);
    attrs.setIpLearning(null);
    attrs.setLinkLocalAddr(null);
    attrs.setMacAddress(null);
    attrs.setMulticastAllow(null);
    attrs.setMultiDestPacketAction(null);
    attrs.setName(null);
    attrs.setNameAlias(null);
    attrs.setType(null);
    attrs.setUnicastRoute(null);
    attrs.setUnknownMacUcastAction(null);
    attrs.setUnknownMcastAction(null);
    attrs.setUserDomain(null);
    attrs.setV6UnknownMcastAction(null);
    attrs.setVirtualMac(null);

    assertThat(attrs.getArpFlood(), nullValue());
    assertThat(attrs.getAnnotation(), nullValue());
    assertThat(attrs.getDescription(), nullValue());
    assertThat(attrs.getDistinguishedName(), nullValue());
    assertThat(attrs.getEpClear(), nullValue());
    assertThat(attrs.getEpMoveDetectMode(), nullValue());
    assertThat(attrs.getHostBasedRouting(), nullValue());
    assertThat(attrs.getIpLearning(), nullValue());
    assertThat(attrs.getLinkLocalAddr(), nullValue());
    assertThat(attrs.getMacAddress(), nullValue());
    assertThat(attrs.getMulticastAllow(), nullValue());
    assertThat(attrs.getMultiDestPacketAction(), nullValue());
    assertThat(attrs.getName(), nullValue());
    assertThat(attrs.getNameAlias(), nullValue());
    assertThat(attrs.getType(), nullValue());
    assertThat(attrs.getUnicastRoute(), nullValue());
    assertThat(attrs.getUnknownMacUcastAction(), nullValue());
    assertThat(attrs.getUnknownMcastAction(), nullValue());
    assertThat(attrs.getUserDomain(), nullValue());
    assertThat(attrs.getV6UnknownMcastAction(), nullValue());
    assertThat(attrs.getVirtualMac(), nullValue());
  }

  /** Test edge case: setting all fields to empty strings. */
  @Test
  public void testEdgeCase_allEmptyStrings() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    attrs.setArpFlood("");
    attrs.setAnnotation("");
    attrs.setDescription("");
    attrs.setDistinguishedName("");
    attrs.setEpClear("");
    attrs.setEpMoveDetectMode("");
    attrs.setHostBasedRouting("");
    attrs.setIpLearning("");
    attrs.setLinkLocalAddr("");
    attrs.setMacAddress("");
    attrs.setMulticastAllow("");
    attrs.setMultiDestPacketAction("");
    attrs.setName("");
    attrs.setNameAlias("");
    attrs.setType("");
    attrs.setUnicastRoute("");
    attrs.setUnknownMacUcastAction("");
    attrs.setUnknownMcastAction("");
    attrs.setUserDomain("");
    attrs.setV6UnknownMcastAction("");
    attrs.setVirtualMac("");

    assertEquals("", attrs.getArpFlood());
    assertEquals("", attrs.getAnnotation());
    assertEquals("", attrs.getDescription());
    assertEquals("", attrs.getDistinguishedName());
    assertEquals("", attrs.getEpClear());
    assertEquals("", attrs.getEpMoveDetectMode());
    assertEquals("", attrs.getHostBasedRouting());
    assertEquals("", attrs.getIpLearning());
    assertEquals("", attrs.getLinkLocalAddr());
    assertEquals("", attrs.getMacAddress());
    assertEquals("", attrs.getMulticastAllow());
    assertEquals("", attrs.getMultiDestPacketAction());
    assertEquals("", attrs.getName());
    assertEquals("", attrs.getNameAlias());
    assertEquals("", attrs.getType());
    assertEquals("", attrs.getUnicastRoute());
    assertEquals("", attrs.getUnknownMacUcastAction());
    assertEquals("", attrs.getUnknownMcastAction());
    assertEquals("", attrs.getUserDomain());
    assertEquals("", attrs.getV6UnknownMcastAction());
    assertEquals("", attrs.getVirtualMac());
  }

  /** Test edge case: special characters in string fields. */
  @Test
  public void testEdgeCase_specialCharacters() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd-with-dashes\","
            + "  \"descr\": \"Description with special chars: <>&\\\"'\","
            + "  \"userdom\": \"user:domain:subdomain\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("bd-with-dashes", bd.getAttributes().getName());
    assertEquals("Description with special chars: <>&\"'", bd.getAttributes().getDescription());
    assertEquals("user:domain:subdomain", bd.getAttributes().getUserDomain());
  }

  /** Test edge case: very long string values. */
  @Test
  public void testEdgeCase_longStrings() {
    AciBridgeDomain.AciBridgeDomainAttributes attrs =
        new AciBridgeDomain.AciBridgeDomainAttributes();

    String longString = "a".repeat(1000);

    attrs.setDescription(longString);
    assertEquals(longString, attrs.getDescription());

    attrs.setDistinguishedName(longString);
    assertEquals(longString, attrs.getDistinguishedName());
  }

  /** Test that unknown properties are ignored during deserialization. */
  @Test
  public void testDeserialize_unknownPropertiesIgnored() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"unknownField\": \"should be ignored\","
            + "  \"anotherUnknown\": 12345"
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("bd1", bd.getAttributes().getName());
  }

  /** Test deserialization with yes/no boolean-like values. */
  @Test
  public void testDeserialize_yesNoValues() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"arpFlood\": \"yes\","
            + "  \"epClear\": \"no\","
            + "  \"mcastAllow\": \"yes\","
            + "  \"unicastRoute\": \"no\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("yes", bd.getAttributes().getArpFlood());
    assertEquals("no", bd.getAttributes().getEpClear());
    assertEquals("yes", bd.getAttributes().getMulticastAllow());
    assertEquals("no", bd.getAttributes().getUnicastRoute());
  }

  /** Test deserialization with different MAC address formats. */
  @Test
  public void testDeserialize_macAddressFormats() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"mac\": \"00:22:BD:F8:19:FF\","
            + "  \"vmac\": \"00-22-BD-F8-19-FE\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("00:22:BD:F8:19:FF", bd.getAttributes().getMacAddress());
    assertEquals("00-22-BD-F8-19-FE", bd.getAttributes().getVirtualMac());
  }

  /** Test deserialization with IPv6 link-local addresses. */
  @Test
  public void testDeserialize_ipv6LinkLocalAddresses() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"llAddr\": \"fe80::/10\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("fe80::/10", bd.getAttributes().getLinkLocalAddr());
  }

  /** Test with minimal required fields. */
  @Test
  public void testDeserialize_minimalFields() throws IOException {
    String json = "{" + "\"attributes\": {" + "  \"name\": \"bd1\"" + "}" + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertEquals("bd1", bd.getAttributes().getName());
    assertThat(bd.getAttributes().getArpFlood(), nullValue());
    assertThat(bd.getAttributes().getDescription(), nullValue());
  }
}
