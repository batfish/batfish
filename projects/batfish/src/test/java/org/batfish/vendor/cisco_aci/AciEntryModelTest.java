package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.apic.AciEntry;
import org.junit.Test;

/**
 * Tests for {@link AciEntry} model class, covering JSON deserialization, getters/setters, and edge
 * cases.
 */
public final class AciEntryModelTest {

  private static final ObjectMapper MAPPER = BatfishObjectMapper.ignoreUnknownMapper();

  /** Test deserialization with all fields populated. */
  @Test
  public void testDeserialize_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"annotation\": \"test_annotation\","
            + "  \"descr\": \"Filter entry description\","
            + "  \"dn\": \"uni/tn-[tenant1]/filter-[filter1]/entry-[entry1]\","
            + "  \"name\": \"entry1\","
            + "  \"nameAlias\": \"Entry 1\","
            + "  \"etherT\": \"ip\","
            + "  \"prot\": \"tcp\","
            + "  \"dPort\": \"80\","
            + "  \"sPort\": \"1024\","
            + "  \"dFromPort\": \"8000\","
            + "  \"dToPort\": \"9000\","
            + "  \"sFromPort\": \"3000\","
            + "  \"sToPort\": \"4000\","
            + "  \"icmpv4T\": \"echo\","
            + "  \"icmpv4C\": \"0\","
            + "  \"icmpv6T\": \"echo-request\","
            + "  \"icmpv6C\": \"0\","
            + "  \"arpOpc\": \"request\","
            + "  \"applyToFrag\": \"yes\","
            + "  \"stateful\": \"yes\","
            + "  \"tcpRules\": \"established\","
            + "  \"matchDscp\": \"32\","
            + "  \"srcAddr\": \"10.0.0.0/24\","
            + "  \"dstAddr\": \"192.168.1.0/24\","
            + "  \"ownerKey\": \"key1\","
            + "  \"ownerTag\": \"tag1\","
            + "  \"userdom\": \"user1:domain1\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    AciEntry.AciEntryAttributes attrs = entry.getAttributes();

    assertEquals("test_annotation", attrs.getAnnotation());
    assertEquals("Filter entry description", attrs.getDescription());
    assertEquals("uni/tn-[tenant1]/filter-[filter1]/entry-[entry1]", attrs.getDistinguishedName());
    assertEquals("entry1", attrs.getName());
    assertEquals("Entry 1", attrs.getNameAlias());
    assertEquals("ip", attrs.getEtherType());
    assertEquals("tcp", attrs.getProtocol());
    assertEquals("80", attrs.getDestinationPort());
    assertEquals("1024", attrs.getSourcePort());
    assertEquals("8000", attrs.getDestinationFromPort());
    assertEquals("9000", attrs.getDestinationToPort());
    assertEquals("3000", attrs.getSourceFromPort());
    assertEquals("4000", attrs.getSourceToPort());
    assertEquals("echo", attrs.getIcmpv4Type());
    assertEquals("0", attrs.getIcmpv4Code());
    assertEquals("echo-request", attrs.getIcmpv6Type());
    assertEquals("0", attrs.getIcmpv6Code());
    assertEquals("request", attrs.getArpOpcode());
    assertEquals("yes", attrs.getApplyToFragments());
    assertEquals("yes", attrs.getStateful());
    assertEquals("established", attrs.getTcpRules());
    assertEquals("32", attrs.getMatchDscp());
    assertEquals("10.0.0.0/24", attrs.getSourceAddress());
    assertEquals("192.168.1.0/24", attrs.getDestinationAddress());
    assertEquals("key1", attrs.getOwnerKey());
    assertEquals("tag1", attrs.getOwnerTag());
    assertEquals("user1:domain1", attrs.getUserDomain());
  }

  /** Test deserialization with null/empty attributes. */
  @Test
  public void testDeserialize_emptyAttributes() throws IOException {
    String json = "{\"attributes\": {}}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    // When attributes is present but empty, Jackson creates an empty object
    assertThat(entry.getAttributes().getName(), nullValue());
    assertThat(entry.getAttributes().getEtherType(), nullValue());
    assertThat(entry.getAttributes().getProtocol(), nullValue());
  }

  /** Test deserialization with null attributes. */
  @Test
  public void testDeserialize_nullAttributes() throws IOException {
    String json = "{}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), nullValue());
  }

  /** Test deserialization with TCP/UDP port fields. */
  @Test
  public void testDeserialize_portFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"prot\": \"tcp\","
            + "  \"dPort\": \"443\","
            + "  \"sPort\": \"8080\","
            + "  \"dFromPort\": \"8000\","
            + "  \"dToPort\": \"9000\","
            + "  \"sFromPort\": \"3000\","
            + "  \"sToPort\": \"4000\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("tcp", entry.getAttributes().getProtocol());
    assertEquals("443", entry.getAttributes().getDestinationPort());
    assertEquals("8080", entry.getAttributes().getSourcePort());
    assertEquals("8000", entry.getAttributes().getDestinationFromPort());
    assertEquals("9000", entry.getAttributes().getDestinationToPort());
    assertEquals("3000", entry.getAttributes().getSourceFromPort());
    assertEquals("4000", entry.getAttributes().getSourceToPort());
  }

  /** Test deserialization with ICMPv4 fields. */
  @Test
  public void testDeserialize_icmpv4Fields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"prot\": \"icmp\","
            + "  \"icmpv4T\": \"echo\","
            + "  \"icmpv4C\": \"0\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("icmp", entry.getAttributes().getProtocol());
    assertEquals("echo", entry.getAttributes().getIcmpv4Type());
    assertEquals("0", entry.getAttributes().getIcmpv4Code());
  }

  /** Test deserialization with ICMPv6 fields. */
  @Test
  public void testDeserialize_icmpv6Fields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"prot\": \"ipv6-icmp\","
            + "  \"icmpv6T\": \"echo-request\","
            + "  \"icmpv6C\": \"0\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("ipv6-icmp", entry.getAttributes().getProtocol());
    assertEquals("echo-request", entry.getAttributes().getIcmpv6Type());
    assertEquals("0", entry.getAttributes().getIcmpv6Code());
  }

  /** Test deserialization with ARP fields. */
  @Test
  public void testDeserialize_arpFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"etherT\": \"arp\","
            + "  \"arpOpc\": \"request\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("arp", entry.getAttributes().getEtherType());
    assertEquals("request", entry.getAttributes().getArpOpcode());
  }

  /** Test deserialization with applyToFragments and stateful. */
  @Test
  public void testDeserialize_fragmentAndStatefulFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"applyToFrag\": \"yes\","
            + "  \"stateful\": \"yes\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("yes", entry.getAttributes().getApplyToFragments());
    assertEquals("yes", entry.getAttributes().getStateful());
  }

  /** Test deserialization with tcpRules and matchDscp. */
  @Test
  public void testDeserialize_tcpRulesAndDscpFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"tcpRules\": \"established\","
            + "  \"matchDscp\": \"32\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("established", entry.getAttributes().getTcpRules());
    assertEquals("32", entry.getAttributes().getMatchDscp());
  }

  /** Test deserialization with source and destination addresses. */
  @Test
  public void testDeserialize_addressFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"srcAddr\": \"10.0.0.0/24\","
            + "  \"dstAddr\": \"192.168.1.100/32\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("10.0.0.0/24", entry.getAttributes().getSourceAddress());
    assertEquals("192.168.1.100/32", entry.getAttributes().getDestinationAddress());
  }

  /** Test getter and setter for etherType field. */
  @Test
  public void testGetterSetter_etherType() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setEtherType("ip");
    assertEquals("ip", attrs.getEtherType());

    attrs.setEtherType("arp");
    assertEquals("arp", attrs.getEtherType());

    attrs.setEtherType("ipv6");
    assertEquals("ipv6", attrs.getEtherType());

    attrs.setEtherType(null);
    assertThat(attrs.getEtherType(), nullValue());

    attrs.setEtherType("");
    assertEquals("", attrs.getEtherType());
  }

  /** Test getter and setter for protocol field. */
  @Test
  public void testGetterSetter_protocol() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setProtocol("tcp");
    assertEquals("tcp", attrs.getProtocol());

    attrs.setProtocol("udp");
    assertEquals("udp", attrs.getProtocol());

    attrs.setProtocol("icmp");
    assertEquals("icmp", attrs.getProtocol());

    attrs.setProtocol(null);
    assertThat(attrs.getProtocol(), nullValue());

    attrs.setProtocol("");
    assertEquals("", attrs.getProtocol());
  }

  /** Test getter and setter for destinationPort field. */
  @Test
  public void testGetterSetter_destinationPort() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setDestinationPort("80");
    assertEquals("80", attrs.getDestinationPort());

    attrs.setDestinationPort("443");
    assertEquals("443", attrs.getDestinationPort());

    attrs.setDestinationPort(null);
    assertThat(attrs.getDestinationPort(), nullValue());

    attrs.setDestinationPort("");
    assertEquals("", attrs.getDestinationPort());
  }

  /** Test getter and setter for sourcePort field. */
  @Test
  public void testGetterSetter_sourcePort() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setSourcePort("1024");
    assertEquals("1024", attrs.getSourcePort());

    attrs.setSourcePort("8080");
    assertEquals("8080", attrs.getSourcePort());

    attrs.setSourcePort(null);
    assertThat(attrs.getSourcePort(), nullValue());

    attrs.setSourcePort("");
    assertEquals("", attrs.getSourcePort());
  }

  /** Test getter and setter for destinationFromPort field. */
  @Test
  public void testGetterSetter_destinationFromPort() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setDestinationFromPort("8000");
    assertEquals("8000", attrs.getDestinationFromPort());

    attrs.setDestinationFromPort("1000");
    assertEquals("1000", attrs.getDestinationFromPort());

    attrs.setDestinationFromPort(null);
    assertThat(attrs.getDestinationFromPort(), nullValue());

    attrs.setDestinationFromPort("");
    assertEquals("", attrs.getDestinationFromPort());
  }

  /** Test getter and setter for destinationToPort field. */
  @Test
  public void testGetterSetter_destinationToPort() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setDestinationToPort("9000");
    assertEquals("9000", attrs.getDestinationToPort());

    attrs.setDestinationToPort("2000");
    assertEquals("2000", attrs.getDestinationToPort());

    attrs.setDestinationToPort(null);
    assertThat(attrs.getDestinationToPort(), nullValue());

    attrs.setDestinationToPort("");
    assertEquals("", attrs.getDestinationToPort());
  }

  /** Test getter and setter for sourceFromPort field. */
  @Test
  public void testGetterSetter_sourceFromPort() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setSourceFromPort("3000");
    assertEquals("3000", attrs.getSourceFromPort());

    attrs.setSourceFromPort("5000");
    assertEquals("5000", attrs.getSourceFromPort());

    attrs.setSourceFromPort(null);
    assertThat(attrs.getSourceFromPort(), nullValue());

    attrs.setSourceFromPort("");
    assertEquals("", attrs.getSourceFromPort());
  }

  /** Test getter and setter for sourceToPort field. */
  @Test
  public void testGetterSetter_sourceToPort() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setSourceToPort("4000");
    assertEquals("4000", attrs.getSourceToPort());

    attrs.setSourceToPort("6000");
    assertEquals("6000", attrs.getSourceToPort());

    attrs.setSourceToPort(null);
    assertThat(attrs.getSourceToPort(), nullValue());

    attrs.setSourceToPort("");
    assertEquals("", attrs.getSourceToPort());
  }

  /** Test getter and setter for icmpv4Type field. */
  @Test
  public void testGetterSetter_icmpv4Type() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setIcmpv4Type("echo");
    assertEquals("echo", attrs.getIcmpv4Type());

    attrs.setIcmpv4Type("echo-reply");
    assertEquals("echo-reply", attrs.getIcmpv4Type());

    attrs.setIcmpv4Type("unreachable");
    assertEquals("unreachable", attrs.getIcmpv4Type());

    attrs.setIcmpv4Type(null);
    assertThat(attrs.getIcmpv4Type(), nullValue());

    attrs.setIcmpv4Type("");
    assertEquals("", attrs.getIcmpv4Type());
  }

  /** Test getter and setter for icmpv4Code field. */
  @Test
  public void testGetterSetter_icmpv4Code() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setIcmpv4Code("0");
    assertEquals("0", attrs.getIcmpv4Code());

    attrs.setIcmpv4Code("8");
    assertEquals("8", attrs.getIcmpv4Code());

    attrs.setIcmpv4Code(null);
    assertThat(attrs.getIcmpv4Code(), nullValue());

    attrs.setIcmpv4Code("");
    assertEquals("", attrs.getIcmpv4Code());
  }

  /** Test getter and setter for icmpv6Type field. */
  @Test
  public void testGetterSetter_icmpv6Type() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setIcmpv6Type("echo-request");
    assertEquals("echo-request", attrs.getIcmpv6Type());

    attrs.setIcmpv6Type("echo-reply");
    assertEquals("echo-reply", attrs.getIcmpv6Type());

    attrs.setIcmpv6Type("destination-unreachable");
    assertEquals("destination-unreachable", attrs.getIcmpv6Type());

    attrs.setIcmpv6Type(null);
    assertThat(attrs.getIcmpv6Type(), nullValue());

    attrs.setIcmpv6Type("");
    assertEquals("", attrs.getIcmpv6Type());
  }

  /** Test getter and setter for icmpv6Code field. */
  @Test
  public void testGetterSetter_icmpv6Code() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setIcmpv6Code("0");
    assertEquals("0", attrs.getIcmpv6Code());

    attrs.setIcmpv6Code("3");
    assertEquals("3", attrs.getIcmpv6Code());

    attrs.setIcmpv6Code(null);
    assertThat(attrs.getIcmpv6Code(), nullValue());

    attrs.setIcmpv6Code("");
    assertEquals("", attrs.getIcmpv6Code());
  }

  /** Test getter and setter for arpOpcode field. */
  @Test
  public void testGetterSetter_arpOpcode() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setArpOpcode("request");
    assertEquals("request", attrs.getArpOpcode());

    attrs.setArpOpcode("reply");
    assertEquals("reply", attrs.getArpOpcode());

    attrs.setArpOpcode(null);
    assertThat(attrs.getArpOpcode(), nullValue());

    attrs.setArpOpcode("");
    assertEquals("", attrs.getArpOpcode());
  }

  /** Test getter and setter for applyToFragments field. */
  @Test
  public void testGetterSetter_applyToFragments() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setApplyToFragments("yes");
    assertEquals("yes", attrs.getApplyToFragments());

    attrs.setApplyToFragments("no");
    assertEquals("no", attrs.getApplyToFragments());

    attrs.setApplyToFragments(null);
    assertThat(attrs.getApplyToFragments(), nullValue());

    attrs.setApplyToFragments("");
    assertEquals("", attrs.getApplyToFragments());
  }

  /** Test getter and setter for stateful field. */
  @Test
  public void testGetterSetter_stateful() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setStateful("yes");
    assertEquals("yes", attrs.getStateful());

    attrs.setStateful("no");
    assertEquals("no", attrs.getStateful());

    attrs.setStateful(null);
    assertThat(attrs.getStateful(), nullValue());

    attrs.setStateful("");
    assertEquals("", attrs.getStateful());
  }

  /** Test getter and setter for tcpRules field. */
  @Test
  public void testGetterSetter_tcpRules() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setTcpRules("established");
    assertEquals("established", attrs.getTcpRules());

    attrs.setTcpRules("un-established");
    assertEquals("un-established", attrs.getTcpRules());

    attrs.setTcpRules(null);
    assertThat(attrs.getTcpRules(), nullValue());

    attrs.setTcpRules("");
    assertEquals("", attrs.getTcpRules());
  }

  /** Test getter and setter for matchDscp field. */
  @Test
  public void testGetterSetter_matchDscp() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setMatchDscp("32");
    assertEquals("32", attrs.getMatchDscp());

    attrs.setMatchDscp("0");
    assertEquals("0", attrs.getMatchDscp());

    attrs.setMatchDscp("63");
    assertEquals("63", attrs.getMatchDscp());

    attrs.setMatchDscp(null);
    assertThat(attrs.getMatchDscp(), nullValue());

    attrs.setMatchDscp("");
    assertEquals("", attrs.getMatchDscp());
  }

  /** Test getter and setter for sourceAddress field. */
  @Test
  public void testGetterSetter_sourceAddress() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setSourceAddress("10.0.0.0/24");
    assertEquals("10.0.0.0/24", attrs.getSourceAddress());

    attrs.setSourceAddress("192.168.1.1/32");
    assertEquals("192.168.1.1/32", attrs.getSourceAddress());

    attrs.setSourceAddress("2001:db8::/32");
    assertEquals("2001:db8::/32", attrs.getSourceAddress());

    attrs.setSourceAddress(null);
    assertThat(attrs.getSourceAddress(), nullValue());

    attrs.setSourceAddress("");
    assertEquals("", attrs.getSourceAddress());
  }

  /** Test getter and setter for destinationAddress field. */
  @Test
  public void testGetterSetter_destinationAddress() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setDestinationAddress("192.168.1.0/24");
    assertEquals("192.168.1.0/24", attrs.getDestinationAddress());

    attrs.setDestinationAddress("10.0.0.1/32");
    assertEquals("10.0.0.1/32", attrs.getDestinationAddress());

    attrs.setDestinationAddress("2001:db8::1/128");
    assertEquals("2001:db8::1/128", attrs.getDestinationAddress());

    attrs.setDestinationAddress(null);
    assertThat(attrs.getDestinationAddress(), nullValue());

    attrs.setDestinationAddress("");
    assertEquals("", attrs.getDestinationAddress());
  }

  /** Test getter and setter for annotation field. */
  @Test
  public void testGetterSetter_annotation() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

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
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

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
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setDistinguishedName("uni/tn-[tenant]/filter-[filter1]/entry-[entry1]");
    assertEquals("uni/tn-[tenant]/filter-[filter1]/entry-[entry1]", attrs.getDistinguishedName());

    attrs.setDistinguishedName(null);
    assertThat(attrs.getDistinguishedName(), nullValue());

    attrs.setDistinguishedName("");
    assertEquals("", attrs.getDistinguishedName());
  }

  /** Test getter and setter for name field. */
  @Test
  public void testGetterSetter_name() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setName("entry1");
    assertEquals("entry1", attrs.getName());

    attrs.setName("allow-all");
    assertEquals("allow-all", attrs.getName());

    attrs.setName(null);
    assertThat(attrs.getName(), nullValue());

    attrs.setName("");
    assertEquals("", attrs.getName());
  }

  /** Test getter and setter for nameAlias field. */
  @Test
  public void testGetterSetter_nameAlias() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setNameAlias("Entry 1");
    assertEquals("Entry 1", attrs.getNameAlias());

    attrs.setNameAlias(null);
    assertThat(attrs.getNameAlias(), nullValue());

    attrs.setNameAlias("");
    assertEquals("", attrs.getNameAlias());
  }

  /** Test getter and setter for ownerKey field. */
  @Test
  public void testGetterSetter_ownerKey() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setOwnerKey("key1");
    assertEquals("key1", attrs.getOwnerKey());

    attrs.setOwnerKey(null);
    assertThat(attrs.getOwnerKey(), nullValue());

    attrs.setOwnerKey("");
    assertEquals("", attrs.getOwnerKey());
  }

  /** Test getter and setter for ownerTag field. */
  @Test
  public void testGetterSetter_ownerTag() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setOwnerTag("tag1");
    assertEquals("tag1", attrs.getOwnerTag());

    attrs.setOwnerTag(null);
    assertThat(attrs.getOwnerTag(), nullValue());

    attrs.setOwnerTag("");
    assertEquals("", attrs.getOwnerTag());
  }

  /** Test getter and setter for userDomain field. */
  @Test
  public void testGetterSetter_userDomain() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

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
    AciEntry entry = new AciEntry();
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setName("entry1");
    attrs.setProtocol("tcp");
    entry.setAttributes(attrs);
    assertEquals(attrs, entry.getAttributes());
    assertEquals("entry1", entry.getAttributes().getName());
    assertEquals("tcp", entry.getAttributes().getProtocol());

    entry.setAttributes(null);
    assertThat(entry.getAttributes(), nullValue());
  }

  /** Test serialization and deserialization round-trip. */
  @Test
  public void testSerializationRoundTrip() throws IOException {
    AciEntry original = new AciEntry();
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setName("entry1");
    attrs.setDescription("Test filter entry");
    attrs.setEtherType("ip");
    attrs.setProtocol("tcp");
    attrs.setDestinationPort("443");
    attrs.setSourcePort("8080");
    attrs.setApplyToFragments("yes");
    attrs.setStateful("yes");
    attrs.setSourceAddress("10.0.0.0/24");
    attrs.setDestinationAddress("192.168.1.0/24");
    original.setAttributes(attrs);

    String serialized = MAPPER.writeValueAsString(original);
    AciEntry deserialized = MAPPER.readValue(serialized, AciEntry.class);

    assertEquals(original.getAttributes().getName(), deserialized.getAttributes().getName());
    assertEquals(
        original.getAttributes().getDescription(), deserialized.getAttributes().getDescription());
    assertEquals(
        original.getAttributes().getEtherType(), deserialized.getAttributes().getEtherType());
    assertEquals(
        original.getAttributes().getProtocol(), deserialized.getAttributes().getProtocol());
    assertEquals(
        original.getAttributes().getDestinationPort(),
        deserialized.getAttributes().getDestinationPort());
    assertEquals(
        original.getAttributes().getSourcePort(), deserialized.getAttributes().getSourcePort());
    assertEquals(
        original.getAttributes().getApplyToFragments(),
        deserialized.getAttributes().getApplyToFragments());
    assertEquals(
        original.getAttributes().getStateful(), deserialized.getAttributes().getStateful());
    assertEquals(
        original.getAttributes().getSourceAddress(),
        deserialized.getAttributes().getSourceAddress());
    assertEquals(
        original.getAttributes().getDestinationAddress(),
        deserialized.getAttributes().getDestinationAddress());
  }

  /** Test edge case: setting all fields to null. */
  @Test
  public void testEdgeCase_allNulls() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setAnnotation(null);
    attrs.setDescription(null);
    attrs.setDistinguishedName(null);
    attrs.setName(null);
    attrs.setNameAlias(null);
    attrs.setEtherType(null);
    attrs.setProtocol(null);
    attrs.setDestinationPort(null);
    attrs.setSourcePort(null);
    attrs.setDestinationFromPort(null);
    attrs.setDestinationToPort(null);
    attrs.setSourceFromPort(null);
    attrs.setSourceToPort(null);
    attrs.setIcmpv4Type(null);
    attrs.setIcmpv4Code(null);
    attrs.setIcmpv6Type(null);
    attrs.setIcmpv6Code(null);
    attrs.setArpOpcode(null);
    attrs.setApplyToFragments(null);
    attrs.setStateful(null);
    attrs.setTcpRules(null);
    attrs.setMatchDscp(null);
    attrs.setSourceAddress(null);
    attrs.setDestinationAddress(null);
    attrs.setOwnerKey(null);
    attrs.setOwnerTag(null);
    attrs.setUserDomain(null);

    assertThat(attrs.getAnnotation(), nullValue());
    assertThat(attrs.getDescription(), nullValue());
    assertThat(attrs.getDistinguishedName(), nullValue());
    assertThat(attrs.getName(), nullValue());
    assertThat(attrs.getNameAlias(), nullValue());
    assertThat(attrs.getEtherType(), nullValue());
    assertThat(attrs.getProtocol(), nullValue());
    assertThat(attrs.getDestinationPort(), nullValue());
    assertThat(attrs.getSourcePort(), nullValue());
    assertThat(attrs.getDestinationFromPort(), nullValue());
    assertThat(attrs.getDestinationToPort(), nullValue());
    assertThat(attrs.getSourceFromPort(), nullValue());
    assertThat(attrs.getSourceToPort(), nullValue());
    assertThat(attrs.getIcmpv4Type(), nullValue());
    assertThat(attrs.getIcmpv4Code(), nullValue());
    assertThat(attrs.getIcmpv6Type(), nullValue());
    assertThat(attrs.getIcmpv6Code(), nullValue());
    assertThat(attrs.getArpOpcode(), nullValue());
    assertThat(attrs.getApplyToFragments(), nullValue());
    assertThat(attrs.getStateful(), nullValue());
    assertThat(attrs.getTcpRules(), nullValue());
    assertThat(attrs.getMatchDscp(), nullValue());
    assertThat(attrs.getSourceAddress(), nullValue());
    assertThat(attrs.getDestinationAddress(), nullValue());
    assertThat(attrs.getOwnerKey(), nullValue());
    assertThat(attrs.getOwnerTag(), nullValue());
    assertThat(attrs.getUserDomain(), nullValue());
  }

  /** Test edge case: setting all fields to empty strings. */
  @Test
  public void testEdgeCase_allEmptyStrings() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

    attrs.setAnnotation("");
    attrs.setDescription("");
    attrs.setDistinguishedName("");
    attrs.setName("");
    attrs.setNameAlias("");
    attrs.setEtherType("");
    attrs.setProtocol("");
    attrs.setDestinationPort("");
    attrs.setSourcePort("");
    attrs.setDestinationFromPort("");
    attrs.setDestinationToPort("");
    attrs.setSourceFromPort("");
    attrs.setSourceToPort("");
    attrs.setIcmpv4Type("");
    attrs.setIcmpv4Code("");
    attrs.setIcmpv6Type("");
    attrs.setIcmpv6Code("");
    attrs.setArpOpcode("");
    attrs.setApplyToFragments("");
    attrs.setStateful("");
    attrs.setTcpRules("");
    attrs.setMatchDscp("");
    attrs.setSourceAddress("");
    attrs.setDestinationAddress("");
    attrs.setOwnerKey("");
    attrs.setOwnerTag("");
    attrs.setUserDomain("");

    assertEquals("", attrs.getAnnotation());
    assertEquals("", attrs.getDescription());
    assertEquals("", attrs.getDistinguishedName());
    assertEquals("", attrs.getName());
    assertEquals("", attrs.getNameAlias());
    assertEquals("", attrs.getEtherType());
    assertEquals("", attrs.getProtocol());
    assertEquals("", attrs.getDestinationPort());
    assertEquals("", attrs.getSourcePort());
    assertEquals("", attrs.getDestinationFromPort());
    assertEquals("", attrs.getDestinationToPort());
    assertEquals("", attrs.getSourceFromPort());
    assertEquals("", attrs.getSourceToPort());
    assertEquals("", attrs.getIcmpv4Type());
    assertEquals("", attrs.getIcmpv4Code());
    assertEquals("", attrs.getIcmpv6Type());
    assertEquals("", attrs.getIcmpv6Code());
    assertEquals("", attrs.getArpOpcode());
    assertEquals("", attrs.getApplyToFragments());
    assertEquals("", attrs.getStateful());
    assertEquals("", attrs.getTcpRules());
    assertEquals("", attrs.getMatchDscp());
    assertEquals("", attrs.getSourceAddress());
    assertEquals("", attrs.getDestinationAddress());
    assertEquals("", attrs.getOwnerKey());
    assertEquals("", attrs.getOwnerTag());
    assertEquals("", attrs.getUserDomain());
  }

  /** Test edge case: special characters in string fields. */
  @Test
  public void testEdgeCase_specialCharacters() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry-with-dashes\","
            + "  \"descr\": \"Description with special chars: <>&\\\"'\","
            + "  \"userdom\": \"user:domain:subdomain\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("entry-with-dashes", entry.getAttributes().getName());
    assertEquals("Description with special chars: <>&\"'", entry.getAttributes().getDescription());
    assertEquals("user:domain:subdomain", entry.getAttributes().getUserDomain());
  }

  /** Test edge case: very long string values. */
  @Test
  public void testEdgeCase_longStrings() {
    AciEntry.AciEntryAttributes attrs = new AciEntry.AciEntryAttributes();

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
            + "  \"name\": \"entry1\","
            + "  \"unknownField\": \"should be ignored\","
            + "  \"anotherUnknown\": 12345"
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("entry1", entry.getAttributes().getName());
  }

  /** Test deserialization with yes/no boolean-like values. */
  @Test
  public void testDeserialize_yesNoValues() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"applyToFrag\": \"yes\","
            + "  \"stateful\": \"no\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("yes", entry.getAttributes().getApplyToFragments());
    assertEquals("no", entry.getAttributes().getStateful());
  }

  /** Test deserialization with various etherType values. */
  @Test
  public void testDeserialize_etherTypeValues() throws IOException {
    String json =
        "{" + "\"attributes\": {" + "  \"name\": \"entry1\"," + "  \"etherT\": \"ip\"" + "}" + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("ip", entry.getAttributes().getEtherType());
  }

  /** Test deserialization with various protocol values. */
  @Test
  public void testDeserialize_protocolValues() throws IOException {
    String json =
        "{" + "\"attributes\": {" + "  \"name\": \"entry1\"," + "  \"prot\": \"tcp\"" + "}" + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("tcp", entry.getAttributes().getProtocol());
  }

  /** Test deserialization with different ICMPv4 types. */
  @Test
  public void testDeserialize_icmpv4Types() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"icmpv4T\": \"unreachable\","
            + "  \"icmpv4C\": \"3\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("unreachable", entry.getAttributes().getIcmpv4Type());
    assertEquals("3", entry.getAttributes().getIcmpv4Code());
  }

  /** Test deserialization with IPv6 addresses. */
  @Test
  public void testDeserialize_ipv6Addresses() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"srcAddr\": \"2001:db8::/32\","
            + "  \"dstAddr\": \"2001:db8::1/128\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("2001:db8::/32", entry.getAttributes().getSourceAddress());
    assertEquals("2001:db8::1/128", entry.getAttributes().getDestinationAddress());
  }

  /** Test with minimal required fields. */
  @Test
  public void testDeserialize_minimalFields() throws IOException {
    String json = "{" + "\"attributes\": {" + "  \"name\": \"entry1\"" + "}" + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("entry1", entry.getAttributes().getName());
    assertThat(entry.getAttributes().getEtherType(), nullValue());
    assertThat(entry.getAttributes().getProtocol(), nullValue());
  }

  /** Test deserialization with port ranges. */
  @Test
  public void testDeserialize_portRanges() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"entry1\","
            + "  \"dFromPort\": \"8000\","
            + "  \"dToPort\": \"8080\","
            + "  \"sFromPort\": \"5000\","
            + "  \"sToPort\": \"5100\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertEquals("8000", entry.getAttributes().getDestinationFromPort());
    assertEquals("8080", entry.getAttributes().getDestinationToPort());
    assertEquals("5000", entry.getAttributes().getSourceFromPort());
    assertEquals("5100", entry.getAttributes().getSourceToPort());
  }
}
