package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI bgpRtSummPol (BGP Route Summarization Policy) object.
 *
 * <p>BGP Route Summarization Policies control how BGP routes are summarized and advertised. They
 * define address family settings, route control parameters, and summarization behavior for BGP
 * peerings in an ACI fabric.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciBgpRouteTargetProfile implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciBgpRouteTargetProfileAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciBgpRouteTargetProfileAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciBgpRouteTargetProfileAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI BGP Route Summarization Policy (bgpRtSummPol). */

  /** Attributes of an ACI BGP Route Summarization Policy (bgpRtSummPol). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciBgpRouteTargetProfileAttributes implements Serializable {

    @JsonProperty("addrTCtrl")
    private @Nullable String _addressTypeControl;

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("attrmap")
    private @Nullable String _attributeMap;

    @JsonProperty("ctrl")
    private @Nullable String _control;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("ownerKey")
    private @Nullable String _ownerKey;

    @JsonProperty("ownerTag")
    private @Nullable String _ownerTag;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    public @Nullable String getAddressTypeControl() {
      return _addressTypeControl;
    }

    public void setAddressTypeControl(@Nullable String addressTypeControl) {
      _addressTypeControl = addressTypeControl;
    }

    public @Nullable String getAnnotation() {
      return _annotation;
    }

    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    public @Nullable String getAttributeMap() {
      return _attributeMap;
    }

    public void setAttributeMap(@Nullable String attributeMap) {
      _attributeMap = attributeMap;
    }

    public @Nullable String getControl() {
      return _control;
    }

    public void setControl(@Nullable String control) {
      _control = control;
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
