package org.batfish.vendor.cisco_aci.representation;

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
  private AciPolUniInternalAttributes attributes;
  private List<PolUniChild> children;

  public @Nullable AciPolUniInternalAttributes getAttributes() {
    return attributes;
  }

  public void setAttributes(@Nullable AciPolUniInternalAttributes attributes) {
    this.attributes = attributes;
  }

  public @Nullable List<PolUniChild> getChildren() {
    return children;
  }

  public void setChildren(@Nullable List<PolUniChild> children) {
    this.children = children;
  }

  /** Attributes of the polUni root element. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciPolUniInternalAttributes implements Serializable {
    @JsonProperty("annotation")
    private @Nullable String annotation;

    @JsonProperty("dn")
    private @Nullable String distinguishedName;

    @JsonProperty("name")
    private @Nullable String name;

    @JsonProperty("nameAlias")
    private @Nullable String nameAlias;

    @JsonProperty("userdom")
    private @Nullable String userDomain;

    public @Nullable String getName() {
      return name;
    }

    public void setName(@Nullable String name) {
      this.name = name;
    }
  }

  /** Child elements at the polUni level. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PolUniChild implements Serializable {
    private @Nullable AciTenant fvTenant;
    private @Nullable AciFabricInst fabricInst;
    private @Nullable AciCtrlrInst ctrlrInst;

    @JsonProperty("fvTenant")
    public @Nullable AciTenant getFvTenant() {
      return fvTenant;
    }

    @JsonProperty("fvTenant")
    public void setFvTenant(@Nullable AciTenant fvTenant) {
      this.fvTenant = fvTenant;
    }

    @JsonProperty("fabricInst")
    public @Nullable AciFabricInst getFabricInst() {
      return fabricInst;
    }

    @JsonProperty("fabricInst")
    public void setFabricInst(@Nullable AciFabricInst fabricInst) {
      this.fabricInst = fabricInst;
    }

    @JsonProperty("ctrlrInst")
    public @Nullable AciCtrlrInst getCtrlrInst() {
      return ctrlrInst;
    }

    @JsonProperty("ctrlrInst")
    public void setCtrlrInst(@Nullable AciCtrlrInst ctrlrInst) {
      this.ctrlrInst = ctrlrInst;
    }
  }
}
