package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI l3extInstP (Layer 3 External Instance Profile / External
 * EPG) object.
 *
 * <p>External EPGs define the external networks that can communicate with the ACI fabric through an
 * L3Out. They contain subnet definitions (l3extSubnet) and contract providers/consumers that define
 * communication policies.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciL3ExternalEpg implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciL3ExternalEpgAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciL3ExternalEpgAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciL3ExternalEpgAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI External EPG (l3extInstP). */

  /** Attributes of an ACI External EPG (l3extInstP). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciL3ExternalEpgAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("exceptionTag")
    private @Nullable String _exceptionTag;

    @JsonProperty("floodOnEncap")
    private @Nullable String _floodOnEncap;

    @JsonProperty("matchT")
    private @Nullable String _matchType;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("pcEnfPref")
    private @Nullable String _policyEnforcementPreference;

    @JsonProperty("prefGrMemb")
    private @Nullable String _preferredGroupMember;

    @JsonProperty("prio")
    private @Nullable String _priority;

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

    public @Nullable String getExceptionTag() {
      return _exceptionTag;
    }

    public void setExceptionTag(@Nullable String exceptionTag) {
      _exceptionTag = exceptionTag;
    }

    public @Nullable String getFloodOnEncap() {
      return _floodOnEncap;
    }

    public void setFloodOnEncap(@Nullable String floodOnEncap) {
      _floodOnEncap = floodOnEncap;
    }

    public @Nullable String getMatchType() {
      return _matchType;
    }

    public void setMatchType(@Nullable String matchType) {
      _matchType = matchType;
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

    public @Nullable String getPolicyEnforcementPreference() {
      return _policyEnforcementPreference;
    }

    public void setPolicyEnforcementPreference(@Nullable String policyEnforcementPreference) {
      _policyEnforcementPreference = policyEnforcementPreference;
    }

    public @Nullable String getPreferredGroupMember() {
      return _preferredGroupMember;
    }

    public void setPreferredGroupMember(@Nullable String preferredGroupMember) {
      _preferredGroupMember = preferredGroupMember;
    }

    public @Nullable String getPriority() {
      return _priority;
    }

    public void setPriority(@Nullable String priority) {
      _priority = priority;
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
