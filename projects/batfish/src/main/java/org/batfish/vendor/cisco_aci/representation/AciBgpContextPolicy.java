package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI bgpCtxPol (BGP Context Policy) object.
 *
 * <p>BGP Context Policies define BGP protocol settings that apply to a VRF context, including
 * timers, graceful restart parameters, and AS path limits. These settings are inherited by BGP
 * peerings within the context.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciBgpContextPolicy implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciBgpContextPolicyAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciBgpContextPolicyAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciBgpContextPolicyAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI BGP Context Policy (bgpCtxPol). */

  /** Attributes of an ACI BGP Context Policy (bgpCtxPol). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciBgpContextPolicyAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("grCtrl")
    private @Nullable String _gracefulRestartControl;

    @JsonProperty("holdIntvl")
    private @Nullable String _holdInterval;

    @JsonProperty("kaIntvl")
    private @Nullable String _keepaliveInterval;

    @JsonProperty("maxAsLimit")
    private @Nullable String _maxAsLimit;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("ownerKey")
    private @Nullable String _ownerKey;

    @JsonProperty("ownerTag")
    private @Nullable String _ownerTag;

    @JsonProperty("staleIntvl")
    private @Nullable String _staleInterval;

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

    public @Nullable String getGracefulRestartControl() {
      return _gracefulRestartControl;
    }

    public void setGracefulRestartControl(@Nullable String gracefulRestartControl) {
      _gracefulRestartControl = gracefulRestartControl;
    }

    public @Nullable String getHoldInterval() {
      return _holdInterval;
    }

    public void setHoldInterval(@Nullable String holdInterval) {
      _holdInterval = holdInterval;
    }

    public @Nullable String getKeepaliveInterval() {
      return _keepaliveInterval;
    }

    public void setKeepaliveInterval(@Nullable String keepaliveInterval) {
      _keepaliveInterval = keepaliveInterval;
    }

    public @Nullable String getMaxAsLimit() {
      return _maxAsLimit;
    }

    public void setMaxAsLimit(@Nullable String maxAsLimit) {
      _maxAsLimit = maxAsLimit;
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

    public @Nullable String getStaleInterval() {
      return _staleInterval;
    }

    public void setStaleInterval(@Nullable String staleInterval) {
      _staleInterval = staleInterval;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }
}
