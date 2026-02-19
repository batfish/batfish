package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/** Represents the fabricProtPol element containing fabric protection policies. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricProtPol implements Serializable {
  private AciFabricProtPolAttributes _attributes;
  private List<FabricProtPolChild> _children;

  public @Nullable AciFabricProtPolAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciFabricProtPolAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<FabricProtPolChild> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<FabricProtPolChild> children) {
    _children = children;
  }

  /** Attributes of fabricProtPol. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricProtPolAttributes implements Serializable {
    private @Nullable String _annotation;
    private @Nullable String _distinguishedName;
    private @Nullable String _name;
    private @Nullable String _nameAlias;
    private @Nullable String _ownerKey;
    private @Nullable String _ownerTag;
    private @Nullable String _userDomain;

    @JsonProperty("annotation")
    public @Nullable String getAnnotation() {
      return _annotation;
    }

    @JsonProperty("annotation")
    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    @JsonProperty("dn")
    public @Nullable String getDistinguishedName() {
      return _distinguishedName;
    }

    @JsonProperty("dn")
    public void setDistinguishedName(@Nullable String distinguishedName) {
      _distinguishedName = distinguishedName;
    }

    @JsonProperty("name")
    public @Nullable String getName() {
      return _name;
    }

    @JsonProperty("name")
    public void setName(@Nullable String name) {
      _name = name;
    }

    @JsonProperty("nameAlias")
    public @Nullable String getNameAlias() {
      return _nameAlias;
    }

    @JsonProperty("nameAlias")
    public void setNameAlias(@Nullable String nameAlias) {
      _nameAlias = nameAlias;
    }

    @JsonProperty("ownerKey")
    public @Nullable String getOwnerKey() {
      return _ownerKey;
    }

    @JsonProperty("ownerKey")
    public void setOwnerKey(@Nullable String ownerKey) {
      _ownerKey = ownerKey;
    }

    @JsonProperty("ownerTag")
    public @Nullable String getOwnerTag() {
      return _ownerTag;
    }

    @JsonProperty("ownerTag")
    public void setOwnerTag(@Nullable String ownerTag) {
      _ownerTag = ownerTag;
    }

    @JsonProperty("userdom")
    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    @JsonProperty("userdom")
    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }

  /** Child elements of fabricProtPol. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FabricProtPolChild implements Serializable {
    private @Nullable AciFabricExplicitGEp _fabricExplicitGEp;

    @JsonProperty("fabricExplicitGEp")
    public @Nullable AciFabricExplicitGEp getFabricExplicitGEp() {
      return _fabricExplicitGEp;
    }

    @JsonProperty("fabricExplicitGEp")
    public void setFabricExplicitGEp(@Nullable AciFabricExplicitGEp fabricExplicitGEp) {
      _fabricExplicitGEp = fabricExplicitGEp;
    }
  }
}
