package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/** Represents a fabric explicit group endpoint (fabricExplicitGEp). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricExplicitGEp implements Serializable {
  private AciFabricExplicitGEpAttributes _attributes;
  private List<FabricExplicitGEpChild> _children;

  public @Nullable AciFabricExplicitGEpAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciFabricExplicitGEpAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<FabricExplicitGEpChild> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<FabricExplicitGEpChild> children) {
    _children = children;
  }

  /** Attributes of fabric explicit group endpoint. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricExplicitGEpAttributes implements Serializable {
    private @Nullable String _annotation;
    private @Nullable String _description;
    private @Nullable String _distinguishedName;
    private @Nullable String _id;
    private @Nullable String _name;
    private @Nullable String _nameAlias;
    private @Nullable String _userDomain;

    @JsonProperty("annotation")
    public @Nullable String getAnnotation() {
      return _annotation;
    }

    @JsonProperty("annotation")
    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    @JsonProperty("descr")
    public @Nullable String getDescription() {
      return _description;
    }

    @JsonProperty("descr")
    public void setDescription(@Nullable String description) {
      _description = description;
    }

    @JsonProperty("dn")
    public @Nullable String getDistinguishedName() {
      return _distinguishedName;
    }

    @JsonProperty("dn")
    public void setDistinguishedName(@Nullable String distinguishedName) {
      _distinguishedName = distinguishedName;
    }

    @JsonProperty("id")
    public @Nullable String getId() {
      return _id;
    }

    @JsonProperty("id")
    public void setId(@Nullable String id) {
      _id = id;
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

    @JsonProperty("userdom")
    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    @JsonProperty("userdom")
    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }

  /** Child elements of fabric explicit group endpoint. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FabricExplicitGEpChild implements Serializable {
    private @Nullable AciFabricNodePEp _fabricNodePEp;
    private @Nullable AciFabricNodeIdentP _fabricNodeIdentP;

    @JsonProperty("fabricNodePEp")
    public @Nullable AciFabricNodePEp getFabricNodePEp() {
      return _fabricNodePEp;
    }

    @JsonProperty("fabricNodePEp")
    public void setFabricNodePEp(@Nullable AciFabricNodePEp fabricNodePEp) {
      _fabricNodePEp = fabricNodePEp;
    }

    @JsonProperty("fabricNodeIdentP")
    public @Nullable AciFabricNodeIdentP getFabricNodeIdentP() {
      return _fabricNodeIdentP;
    }

    @JsonProperty("fabricNodeIdentP")
    public void setFabricNodeIdentP(@Nullable AciFabricNodeIdentP fabricNodeIdentP) {
      _fabricNodeIdentP = fabricNodeIdentP;
    }
  }
}
