package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Internal representation of polUni with typed children.
 *
 * <p>This class is used during deserialization of APIC exports before conversion into the main ACI
 * data model.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AciPolUniDeserializer.class)
public class AciPolUniInternal implements Serializable {
  private AciPolUniInternalAttributes _attributes;
  private List<PolUniChild> _children;

  public @Nullable AciPolUniInternalAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciPolUniInternalAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<PolUniChild> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<PolUniChild> children) {
    _children = children;
  }

  /** Attributes of the polUni root element. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciPolUniInternalAttributes implements Serializable {
    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    public @Nullable String getName() {
      return _name;
    }

    public void setName(@Nullable String name) {
      _name = name;
    }
  }

  /** Child elements at the polUni level. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PolUniChild implements Serializable {
    private @Nullable AciTenant _fvTenant;
    private @Nullable AciFabricInst _fabricInst;
    private @Nullable AciCtrlrInst _ctrlrInst;

    @JsonProperty("fvTenant")
    public @Nullable AciTenant getFvTenant() {
      return _fvTenant;
    }

    @JsonProperty("fvTenant")
    public void setFvTenant(@Nullable AciTenant fvTenant) {
      _fvTenant = fvTenant;
    }

    @JsonProperty("fabricInst")
    public @Nullable AciFabricInst getFabricInst() {
      return _fabricInst;
    }

    @JsonProperty("fabricInst")
    public void setFabricInst(@Nullable AciFabricInst fabricInst) {
      _fabricInst = fabricInst;
    }

    @JsonProperty("ctrlrInst")
    public @Nullable AciCtrlrInst getCtrlrInst() {
      return _ctrlrInst;
    }

    @JsonProperty("ctrlrInst")
    public void setCtrlrInst(@Nullable AciCtrlrInst ctrlrInst) {
      _ctrlrInst = ctrlrInst;
    }
  }
}
