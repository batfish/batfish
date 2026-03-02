package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI l3extOut (Layer 3 External Network / L3Out) object.
 *
 * <p>L3Outs provide external connectivity for ACI tenants, allowing communication with networks
 * outside the ACI fabric. They contain logical node profiles (l3extLNodeP), external EPGs
 * (l3extInstP), and can be associated with VRF contexts and external domains.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciL3Out implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciL3OutAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciL3OutAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciL3OutAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI L3Out (l3extOut). */

  /** Attributes of an ACI L3Out (l3extOut). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciL3OutAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("enforceRtctrl")
    private @Nullable String _enforceRouteControl;

    @JsonProperty("hostBasedRoute")
    private @Nullable String _hostBasedRoute;

    @JsonProperty("matchT")
    private @Nullable String _matchType;

    @JsonProperty("maxEcmp")
    private @Nullable String _maxEcmp;

    @JsonProperty("mplsEnabled")
    private @Nullable String _mplsEnabled;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("ownerKey")
    private @Nullable String _ownerKey;

    @JsonProperty("ownerTag")
    private @Nullable String _ownerTag;

    @JsonProperty("targetDscp")
    private @Nullable String _targetDscp;

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

    public @Nullable String getEnforceRouteControl() {
      return _enforceRouteControl;
    }

    public void setEnforceRouteControl(@Nullable String enforceRouteControl) {
      _enforceRouteControl = enforceRouteControl;
    }

    public @Nullable String getHostBasedRoute() {
      return _hostBasedRoute;
    }

    public void setHostBasedRoute(@Nullable String hostBasedRoute) {
      _hostBasedRoute = hostBasedRoute;
    }

    public @Nullable String getMatchType() {
      return _matchType;
    }

    public void setMatchType(@Nullable String matchType) {
      _matchType = matchType;
    }

    public @Nullable String getMaxEcmp() {
      return _maxEcmp;
    }

    public void setMaxEcmp(@Nullable String maxEcmp) {
      _maxEcmp = maxEcmp;
    }

    public @Nullable String getMplsEnabled() {
      return _mplsEnabled;
    }

    public void setMplsEnabled(@Nullable String mplsEnabled) {
      _mplsEnabled = mplsEnabled;
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

    public @Nullable String getTargetDscp() {
      return _targetDscp;
    }

    public void setTargetDscp(@Nullable String targetDscp) {
      _targetDscp = targetDscp;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }
}
