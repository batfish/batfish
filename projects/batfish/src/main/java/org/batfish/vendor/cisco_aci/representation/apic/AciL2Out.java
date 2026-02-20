package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI l2extOut (L2 External) object.
 *
 * <p>L2 external connectivity defines connections to external networks through Layer 2, using
 * bridge domains rather than routed interfaces. This is often used for "L2 L3Out" scenarios where
 * traffic is bridged rather than routed.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciL2Out implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciL2OutAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciL2OutAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciL2OutAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI L2Out (l2extOut). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciL2OutAttributes implements Serializable {

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
