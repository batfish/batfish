package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nullable;

/** Physical layer 1 interface configuration in ACI. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciL1PhysIf implements Serializable {
  private AciL1PhysIfAttributes _attributes;

  public @Nullable AciL1PhysIfAttributes getAttributes() {
    return _attributes;
  }

  @JsonProperty("attributes")
  public void setAttributes(@Nullable AciL1PhysIfAttributes attributes) {
    _attributes = attributes;
  }

  /** Attributes of a physical layer 1 interface. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciL1PhysIfAttributes implements Serializable {
    private @Nullable String _annotation;
    private @Nullable String _description;
    private @Nullable String _distinguishedName;
    private @Nullable String _id;

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
  }
}
