package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/** Represents a fabric node endpoint (fabricNodePEp). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricNodePEp implements Serializable {
  private AciFabricNodePEpAttributes _attributes;
  private List<FabricNodePEpChild> _children;

  public @Nullable AciFabricNodePEpAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciFabricNodePEpAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<FabricNodePEpChild> getChildren() {
    return _children;
  }

  @JsonProperty("children")
  public void setChildren(@Nullable List<FabricNodePEpChild> children) {
    _children = children;
  }

  /** Attributes of a fabric node. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricNodePEpAttributes implements Serializable {
    private @Nullable String _annotation;
    private @Nullable String _description;
    private @Nullable String _distinguishedName;
    private @Nullable String _id;
    private @Nullable String _name;
    private @Nullable String _nameAlias;
    private @Nullable String _nodeId;
    private @Nullable String _podId;
    private @Nullable String _role;
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

    @JsonProperty("nodeId")
    public @Nullable String getNodeId() {
      return _nodeId;
    }

    @JsonProperty("nodeId")
    public void setNodeId(@Nullable String nodeId) {
      _nodeId = nodeId;
    }

    @JsonProperty("podId")
    public @Nullable String getPodId() {
      return _podId;
    }

    @JsonProperty("podId")
    public void setPodId(@Nullable String podId) {
      _podId = podId;
    }

    @JsonProperty("role")
    public @Nullable String getRole() {
      return _role;
    }

    @JsonProperty("role")
    public void setRole(@Nullable String role) {
      _role = role;
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

  /** Child elements of a fabric node. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FabricNodePEpChild implements Serializable {
    private AciFabricInterface _fabricInterface;
    private AciL1PhysIf _l1PhysIf;

    @JsonProperty("fabricInterface")
    public @Nullable AciFabricInterface getFabricInterface() {
      return _fabricInterface;
    }

    @JsonProperty("fabricInterface")
    public void setFabricInterface(@Nullable AciFabricInterface fabricInterface) {
      _fabricInterface = fabricInterface;
    }

    @JsonProperty("l1PhysIf")
    public @Nullable AciL1PhysIf getL1PhysIf() {
      return _l1PhysIf;
    }

    @JsonProperty("l1PhysIf")
    public void setL1PhysIf(@Nullable AciL1PhysIf l1PhysIf) {
      _l1PhysIf = l1PhysIf;
    }
  }
}
