package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents the fabricNodeIdentP element containing fabric node identification. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricNodeIdentP implements Serializable {
  private AciFabricNodeIdentPAttributes _attributes;

  public @Nullable AciFabricNodeIdentPAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciFabricNodeIdentPAttributes attributes) {
    _attributes = attributes;
  }

  /** Attributes of fabric node identification. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricNodeIdentPAttributes implements Serializable {
    private @Nullable String _annotation;
    private @Nullable String _description;
    private @Nullable String _distinguishedName;
    private @Nullable String _id;
    private @Nullable String _name;
    private @Nullable String _nodeId;
    private @Nullable String _podId;
    private @Nullable String _role;
    private @Nullable String _serial;
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

    @JsonProperty("serial")
    public @Nullable String getSerial() {
      return _serial;
    }

    @JsonProperty("serial")
    public void setSerial(@Nullable String serial) {
      _serial = serial;
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
}
