package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI fvCtx (VRF Context) object.
 *
 * <p>VRF contexts (also called private networks or contexts) define Layer 3 forwarding domains.
 * Multiple bridge domains can be associated with a single VRF for inter-VLAN routing.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciVrf implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciVrfAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciVrfAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciVrfAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI VRF Context (fvCtx). */

  /** Attributes of an ACI VRF Context (fvCtx). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciVrfAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("bdEnforcedEnable")
    private @Nullable String _bdEnforcedEnable;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("ipDataPlaneLearning")
    private @Nullable String _ipDataPlaneLearning;

    @JsonProperty("knwMcastAct")
    private @Nullable String _knownMcastAction;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("ownerKey")
    private @Nullable String _ownerKey;

    @JsonProperty("ownerTag")
    private @Nullable String _ownerTag;

    @JsonProperty("pcEnfDir")
    private @Nullable String _policyEnforcementDirection;

    @JsonProperty("pcEnfPref")
    private @Nullable String _policyEnforcementPreference;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    @JsonProperty("vrfIndex")
    private @Nullable String _vrfIndex;

    public @Nullable String getAnnotation() {
      return _annotation;
    }

    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    public @Nullable String getBdEnforcedEnable() {
      return _bdEnforcedEnable;
    }

    public void setBdEnforcedEnable(@Nullable String bdEnforcedEnable) {
      _bdEnforcedEnable = bdEnforcedEnable;
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

    public @Nullable String getIpDataPlaneLearning() {
      return _ipDataPlaneLearning;
    }

    public void setIpDataPlaneLearning(@Nullable String ipDataPlaneLearning) {
      _ipDataPlaneLearning = ipDataPlaneLearning;
    }

    public @Nullable String getKnownMcastAction() {
      return _knownMcastAction;
    }

    public void setKnownMcastAction(@Nullable String knownMcastAction) {
      _knownMcastAction = knownMcastAction;
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

    public @Nullable String getPolicyEnforcementDirection() {
      return _policyEnforcementDirection;
    }

    public void setPolicyEnforcementDirection(@Nullable String policyEnforcementDirection) {
      _policyEnforcementDirection = policyEnforcementDirection;
    }

    public @Nullable String getPolicyEnforcementPreference() {
      return _policyEnforcementPreference;
    }

    public void setPolicyEnforcementPreference(@Nullable String policyEnforcementPreference) {
      _policyEnforcementPreference = policyEnforcementPreference;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }

    public @Nullable String getVrfIndex() {
      return _vrfIndex;
    }

    public void setVrfIndex(@Nullable String vrfIndex) {
      _vrfIndex = vrfIndex;
    }
  }
}
