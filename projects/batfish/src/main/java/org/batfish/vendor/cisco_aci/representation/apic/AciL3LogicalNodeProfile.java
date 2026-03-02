package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI l3extLNodeP (Layer 3 External Logical Node Profile) object.
 *
 * <p>Logical Node Profiles define the nodes (leaf switches) that participate in an L3Out. They
 * contain node-specific configurations including router IDs, interface profiles, and BGP peer
 * settings for external connectivity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciL3LogicalNodeProfile implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciL3LogicalNodeProfileAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciL3LogicalNodeProfileAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciL3LogicalNodeProfileAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI L3 Logical Node Profile (l3extLNodeP). */

  /** Attributes of an ACI L3 Logical Node Profile (l3extLNodeP). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciL3LogicalNodeProfileAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("configIssues")
    private @Nullable String _configIssues;

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

    @JsonProperty("tag")
    private @Nullable String _tag;

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

    public @Nullable String getConfigIssues() {
      return _configIssues;
    }

    public void setConfigIssues(@Nullable String configIssues) {
      _configIssues = configIssues;
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

    public @Nullable String getTag() {
      return _tag;
    }

    public void setTag(@Nullable String tag) {
      _tag = tag;
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
