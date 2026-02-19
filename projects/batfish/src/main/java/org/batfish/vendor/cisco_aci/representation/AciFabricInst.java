package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/** Represents the fabricInst element containing fabric-wide configuration. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricInst implements Serializable {
  private AciFabricInstAttributes _attributes;
  private List<FabricInstChild> _children;

  public @Nullable AciFabricInstAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciFabricInstAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<FabricInstChild> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<FabricInstChild> children) {
    _children = children;
  }

  /** Attributes of fabricInst. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricInstAttributes implements Serializable {
    private @Nullable String _annotation;

    private @Nullable String _distinguishedName;

    private @Nullable String _name;

    private @Nullable String _nameAlias;

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
  }

  /** Child elements of fabricInst. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FabricInstChild implements Serializable {
    private @Nullable AciFabricProtPol _fabricProtPol;
    private @Nullable AciFabricNodeIdentPol _fabricNodeIdentPol;

    @JsonProperty("fabricProtPol")
    public @Nullable AciFabricProtPol getFabricProtPol() {
      return _fabricProtPol;
    }

    @JsonProperty("fabricProtPol")
    public void setFabricProtPol(@Nullable AciFabricProtPol fabricProtPol) {
      _fabricProtPol = fabricProtPol;
    }

    @JsonProperty("fabricNodeIdentPol")
    public @Nullable AciFabricNodeIdentPol getFabricNodeIdentPol() {
      return _fabricNodeIdentPol;
    }

    @JsonProperty("fabricNodeIdentPol")
    public void setFabricNodeIdentPol(@Nullable AciFabricNodeIdentPol fabricNodeIdentPol) {
      _fabricNodeIdentPol = fabricNodeIdentPol;
    }
  }
}
