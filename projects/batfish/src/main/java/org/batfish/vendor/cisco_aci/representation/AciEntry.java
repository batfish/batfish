package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI vzEntry (Filter Entry) object.
 *
 * <p>Filter entries define specific traffic matching criteria within a filter. They specify Layer 2
 * to Layer 4 match conditions including Ethernet type, IP protocol, TCP/UDP ports, ICMP types, and
 * source/destination addresses. Multiple entries within a filter are ORed together.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciEntry implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciEntryAttributes _attributes;

  public @Nullable AciEntryAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciEntryAttributes attributes) {
    _attributes = attributes;
  }

  /** Attributes of an ACI Filter Entry (vzEntry). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciEntryAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    // Ethernet type: ip, arp, trill, macsec, fcoe, etc.
    @JsonProperty("etherT")
    private @Nullable String _etherType;

    // IP protocol: tcp, udp, icmp, igmp, ip, etc.
    @JsonProperty("prot")
    private @Nullable String _protocol;

    // Destination port (single value or range)
    @JsonProperty("dPort")
    private @Nullable String _destinationPort;

    // Source port (single value or range)
    @JsonProperty("sPort")
    private @Nullable String _sourcePort;

    // Destination port range start
    @JsonProperty("dFromPort")
    private @Nullable String _destinationFromPort;

    // Destination port range end
    @JsonProperty("dToPort")
    private @Nullable String _destinationToPort;

    // Source port range start
    @JsonProperty("sFromPort")
    private @Nullable String _sourceFromPort;

    // Source port range end
    @JsonProperty("sToPort")
    private @Nullable String _sourceToPort;

    // ICMPv4 type (e.g., "echo", "echo-reply", "unreachable")
    @JsonProperty("icmpv4T")
    private @Nullable String _icmpv4Type;

    // ICMPv4 code
    @JsonProperty("icmpv4C")
    private @Nullable String _icmpv4Code;

    // ICMPv6 type
    @JsonProperty("icmpv6T")
    private @Nullable String _icmpv6Type;

    // ICMPv6 code
    @JsonProperty("icmpv6C")
    private @Nullable String _icmpv6Code;

    // ARP opcode (request, reply)
    @JsonProperty("arpOpc")
    private @Nullable String _arpOpcode;

    // Apply to fragments only
    @JsonProperty("applyToFrag")
    private @Nullable String _applyToFragments;

    // Stateful inspection
    @JsonProperty("stateful")
    private @Nullable String _stateful;

    // TCP rules (established, etc.)
    @JsonProperty("tcpRules")
    private @Nullable String _tcpRules;

    // Match only DSCP value
    @JsonProperty("matchDscp")
    private @Nullable String _matchDscp;

    // Source IP address (prefix format)
    @JsonProperty("srcAddr")
    private @Nullable String _sourceAddress;

    // Destination IP address (prefix format)
    @JsonProperty("dstAddr")
    private @Nullable String _destinationAddress;

    @JsonProperty("ownerKey")
    private @Nullable String _ownerKey;

    @JsonProperty("ownerTag")
    private @Nullable String _ownerTag;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    public @Nullable String getAnnotation() {
      return _annotation;
    }

    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(@Nullable String description) {
      _description = description;
    }

    public @Nullable String getDistinguishedName() {
      return _distinguishedName;
    }

    public void setDistinguishedName(@Nullable String distinguishedName) {
      _distinguishedName = distinguishedName;
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(@Nullable String name) {
      _name = name;
    }

    public @Nullable String getNameAlias() {
      return _nameAlias;
    }

    public void setNameAlias(@Nullable String nameAlias) {
      _nameAlias = nameAlias;
    }

    public @Nullable String getEtherType() {
      return _etherType;
    }

    public void setEtherType(@Nullable String etherType) {
      _etherType = etherType;
    }

    public @Nullable String getProtocol() {
      return _protocol;
    }

    public void setProtocol(@Nullable String protocol) {
      _protocol = protocol;
    }

    public @Nullable String getDestinationPort() {
      return _destinationPort;
    }

    public void setDestinationPort(@Nullable String destinationPort) {
      _destinationPort = destinationPort;
    }

    public @Nullable String getSourcePort() {
      return _sourcePort;
    }

    public void setSourcePort(@Nullable String sourcePort) {
      _sourcePort = sourcePort;
    }

    public @Nullable String getDestinationFromPort() {
      return _destinationFromPort;
    }

    public void setDestinationFromPort(@Nullable String destinationFromPort) {
      _destinationFromPort = destinationFromPort;
    }

    public @Nullable String getDestinationToPort() {
      return _destinationToPort;
    }

    public void setDestinationToPort(@Nullable String destinationToPort) {
      _destinationToPort = destinationToPort;
    }

    public @Nullable String getSourceFromPort() {
      return _sourceFromPort;
    }

    public void setSourceFromPort(@Nullable String sourceFromPort) {
      _sourceFromPort = sourceFromPort;
    }

    public @Nullable String getSourceToPort() {
      return _sourceToPort;
    }

    public void setSourceToPort(@Nullable String sourceToPort) {
      _sourceToPort = sourceToPort;
    }

    public @Nullable String getIcmpv4Type() {
      return _icmpv4Type;
    }

    public void setIcmpv4Type(@Nullable String icmpv4Type) {
      _icmpv4Type = icmpv4Type;
    }

    public @Nullable String getIcmpv4Code() {
      return _icmpv4Code;
    }

    public void setIcmpv4Code(@Nullable String icmpv4Code) {
      _icmpv4Code = icmpv4Code;
    }

    public @Nullable String getIcmpv6Type() {
      return _icmpv6Type;
    }

    public void setIcmpv6Type(@Nullable String icmpv6Type) {
      _icmpv6Type = icmpv6Type;
    }

    public @Nullable String getIcmpv6Code() {
      return _icmpv6Code;
    }

    public void setIcmpv6Code(@Nullable String icmpv6Code) {
      _icmpv6Code = icmpv6Code;
    }

    public @Nullable String getArpOpcode() {
      return _arpOpcode;
    }

    public void setArpOpcode(@Nullable String arpOpcode) {
      _arpOpcode = arpOpcode;
    }

    public @Nullable String getApplyToFragments() {
      return _applyToFragments;
    }

    public void setApplyToFragments(@Nullable String applyToFragments) {
      _applyToFragments = applyToFragments;
    }

    public @Nullable String getStateful() {
      return _stateful;
    }

    public void setStateful(@Nullable String stateful) {
      _stateful = stateful;
    }

    public @Nullable String getTcpRules() {
      return _tcpRules;
    }

    public void setTcpRules(@Nullable String tcpRules) {
      _tcpRules = tcpRules;
    }

    public @Nullable String getMatchDscp() {
      return _matchDscp;
    }

    public void setMatchDscp(@Nullable String matchDscp) {
      _matchDscp = matchDscp;
    }

    public @Nullable String getSourceAddress() {
      return _sourceAddress;
    }

    public void setSourceAddress(@Nullable String sourceAddress) {
      _sourceAddress = sourceAddress;
    }

    public @Nullable String getDestinationAddress() {
      return _destinationAddress;
    }

    public void setDestinationAddress(@Nullable String destinationAddress) {
      _destinationAddress = destinationAddress;
    }

    public @Nullable String getOwnerKey() {
      return _ownerKey;
    }

    public void setOwnerKey(@Nullable String ownerKey) {
      _ownerKey = ownerKey;
    }

    public @Nullable String getOwnerTag() {
      return _ownerTag;
    }

    public void setOwnerTag(@Nullable String ownerTag) {
      _ownerTag = ownerTag;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }
}
