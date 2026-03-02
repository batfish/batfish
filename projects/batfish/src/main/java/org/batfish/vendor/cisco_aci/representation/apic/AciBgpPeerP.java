package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI bgpPeerP (BGP Peer Prefix Policy) object.
 *
 * <p>BGP Peer Prefix Policies define prefix limits for BGP peers to prevent route table exhaustion
 * attacks. They specify the maximum number of prefixes accepted from a peer, threshold values, and
 * actions to take when limits are exceeded.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciBgpPeerP implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciBgpPeerPAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciBgpPeerPAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciBgpPeerPAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI BGP Peer Prefix Policy (bgpPeerPfxPol). */

  /** Attributes of an ACI BGP Peer Prefix Policy (bgpPeerPfxPol). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciBgpPeerPAttributes implements Serializable {

    @JsonProperty("action")
    private @Nullable String _action;

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("maxPfx")
    private @Nullable String _maxPrefixes;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("ownerKey")
    private @Nullable String _ownerKey;

    @JsonProperty("ownerTag")
    private @Nullable String _ownerTag;

    @JsonProperty("restartTime")
    private @Nullable String _restartTime;

    @JsonProperty("thresh")
    private @Nullable String _threshold;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    public @Nullable String getAction() {
      return _action;
    }

    public void setAction(@Nullable String action) {
      _action = action;
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

    public @Nullable String getMaxPrefixes() {
      return _maxPrefixes;
    }

    public void setMaxPrefixes(@Nullable String maxPrefixes) {
      _maxPrefixes = maxPrefixes;
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

    public @Nullable String getRestartTime() {
      return _restartTime;
    }

    public void setRestartTime(@Nullable String restartTime) {
      _restartTime = restartTime;
    }

    public @Nullable String getThreshold() {
      return _threshold;
    }

    public void setThreshold(@Nullable String threshold) {
      _threshold = threshold;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }
}
