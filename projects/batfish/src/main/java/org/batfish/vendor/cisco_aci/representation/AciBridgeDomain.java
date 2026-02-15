package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI fvBD (Bridge Domain) object.
 *
 * <p>Bridge domains (BDs) are Layer 2 forwarding domains within ACI. They contain subnets, endpoint
 * groups, and can be associated with VRF contexts for Layer 3 forwarding.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciBridgeDomain implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciBridgeDomainAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciBridgeDomainAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciBridgeDomainAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI Bridge Domain (fvBD). */

  /** Attributes of an ACI Bridge Domain (fvBD). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciBridgeDomainAttributes implements Serializable {

    @JsonProperty("arpFlood")
    private @Nullable String _arpFlood;

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("epClear")
    private @Nullable String _epClear;

    @JsonProperty("epMoveDetectMode")
    private @Nullable String _epMoveDetectMode;

    @JsonProperty("hostBasedRouting")
    private @Nullable String _hostBasedRouting;

    @JsonProperty("ipLearning")
    private @Nullable String _ipLearning;

    @JsonProperty("llAddr")
    private @Nullable String _linkLocalAddr;

    @JsonProperty("mac")
    private @Nullable String _macAddress;

    @JsonProperty("mcastAllow")
    private @Nullable String _multicastAllow;

    @JsonProperty("multiDstPktAct")
    private @Nullable String _multiDestPacketAction;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("type")
    private @Nullable String _type;

    @JsonProperty("unicastRoute")
    private @Nullable String _unicastRoute;

    @JsonProperty("unkMacUcastAct")
    private @Nullable String _unknownMacUcastAction;

    @JsonProperty("unkMcastAct")
    private @Nullable String _unknownMcastAction;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    @JsonProperty("v6unkMcastAct")
    private @Nullable String _v6UnknownMcastAction;

    @JsonProperty("vmac")
    private @Nullable String _virtualMac;

    public @Nullable String getArpFlood() {
      return _arpFlood;
    }

    public void setArpFlood(@Nullable String arpFlood) {
      _arpFlood = arpFlood;
    }

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

    public @Nullable String getEpClear() {
      return _epClear;
    }

    public void setEpClear(@Nullable String epClear) {
      _epClear = epClear;
    }

    public @Nullable String getEpMoveDetectMode() {
      return _epMoveDetectMode;
    }

    public void setEpMoveDetectMode(@Nullable String epMoveDetectMode) {
      _epMoveDetectMode = epMoveDetectMode;
    }

    public @Nullable String getHostBasedRouting() {
      return _hostBasedRouting;
    }

    public void setHostBasedRouting(@Nullable String hostBasedRouting) {
      _hostBasedRouting = hostBasedRouting;
    }

    public @Nullable String getIpLearning() {
      return _ipLearning;
    }

    public void setIpLearning(@Nullable String ipLearning) {
      _ipLearning = ipLearning;
    }

    public @Nullable String getLinkLocalAddr() {
      return _linkLocalAddr;
    }

    public void setLinkLocalAddr(@Nullable String linkLocalAddr) {
      _linkLocalAddr = linkLocalAddr;
    }

    public @Nullable String getMacAddress() {
      return _macAddress;
    }

    public void setMacAddress(@Nullable String macAddress) {
      _macAddress = macAddress;
    }

    public @Nullable String getMulticastAllow() {
      return _multicastAllow;
    }

    public void setMulticastAllow(@Nullable String multicastAllow) {
      _multicastAllow = multicastAllow;
    }

    public @Nullable String getMultiDestPacketAction() {
      return _multiDestPacketAction;
    }

    public void setMultiDestPacketAction(@Nullable String multiDestPacketAction) {
      _multiDestPacketAction = multiDestPacketAction;
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

    public @Nullable String getType() {
      return _type;
    }

    public void setType(@Nullable String type) {
      _type = type;
    }

    public @Nullable String getUnicastRoute() {
      return _unicastRoute;
    }

    public void setUnicastRoute(@Nullable String unicastRoute) {
      _unicastRoute = unicastRoute;
    }

    public @Nullable String getUnknownMacUcastAction() {
      return _unknownMacUcastAction;
    }

    public void setUnknownMacUcastAction(@Nullable String unknownMacUcastAction) {
      _unknownMacUcastAction = unknownMacUcastAction;
    }

    public @Nullable String getUnknownMcastAction() {
      return _unknownMcastAction;
    }

    public void setUnknownMcastAction(@Nullable String unknownMcastAction) {
      _unknownMcastAction = unknownMcastAction;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }

    public @Nullable String getV6UnknownMcastAction() {
      return _v6UnknownMcastAction;
    }

    public void setV6UnknownMcastAction(@Nullable String v6UnknownMcastAction) {
      _v6UnknownMcastAction = v6UnknownMcastAction;
    }

    public @Nullable String getVirtualMac() {
      return _virtualMac;
    }

    public void setVirtualMac(@Nullable String virtualMac) {
      _virtualMac = virtualMac;
    }
  }
}
