package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing a dynamic ACI child object.
 *
 * <p>In the ACI JSON structure, children are represented as objects with a single key that
 * indicates the ACI class name (e.g., "fvTenant", "fvCtx", "fabricNodePEp", "vzBrCP"). The value of
 * that key is an object containing "attributes" and optionally "children".
 *
 * <p>Examples:
 *
 * <pre>{@code
 * {"fvTenant": {"attributes": {...}, "children": [...]}}
 * {"fabricNodePEp": {"attributes": {...}}}
 * {"vzBrCP": {"attributes": {...}, "children": [...]}}
 * }</pre>
 *
 * <p>This class uses {@link JsonAnySetter} to dynamically handle any ACI object type.
 *
 * @see AciPolUni
 * @see AciAttributes
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AciChild implements Serializable {

  /** The ACI class name for this child (e.g., "fvTenant", "fabricNodePEp", "vzBrCP"). */
  @JsonProperty("className")
  private @Nullable String _className;

  /** The attributes of this ACI object. */
  @JsonProperty("attributes")
  private @Nullable AciAttributes _attributes;

  /** Nested children of this ACI object, if any. */
  @JsonProperty("children")
  private @Nullable List<AciChild> _children;

  /**
   * Returns the ACI class name for this child object. This represents the type of ACI object (e.g.,
   * "fvTenant" for tenant, "fvCtx" for VRF context, "fabricNodePEp" for fabric node).
   *
   * @return the ACI class name, or null if not set
   */
  public @Nullable String getClassName() {
    return _className;
  }

  /**
   * Sets the ACI class name for this child object.
   *
   * @param className the class name to set
   */
  public void setClassName(@Nullable String className) {
    _className = className;
  }

  /**
   * Returns the attributes of this ACI object. The attributes contain the object's properties such
   * as name, distinguished name (dn), description, and various configuration flags.
   *
   * @return the attributes, or null if not present
   */
  @JsonProperty("attributes")
  public @Nullable AciAttributes getAttributes() {
    return _attributes;
  }

  /**
   * Sets the attributes for this ACI object.
   *
   * @param attributes the attributes to set
   */
  public void setAttributes(@Nullable AciAttributes attributes) {
    _attributes = attributes;
  }

  /**
   * Returns the list of nested children for this ACI object. Many ACI objects contain nested
   * objects (e.g., a tenant contains application profiles, bridge domains, VRFs, etc.).
   *
   * @return the list of children, or null if this object has no children
   */
  @JsonProperty("children")
  public @Nullable List<AciChild> getChildren() {
    return _children;
  }

  /**
   * Sets the list of nested children for this ACI object.
   *
   * @param children the children to set
   */
  public void setChildren(@Nullable List<AciChild> children) {
    _children = children;
  }

  /**
   * Dynamic setter called by Jackson for any property in the JSON object. This handles the variable
   * ACI class name (e.g., "fvTenant", "fabricNodePEp") as the single key in the child object.
   *
   * <p>The value is expected to be a Map-like object (handled by Jackson) containing the
   * "attributes" and optionally "children" keys. This method is called once per child object.
   *
   * @param key the ACI class name (e.g., "fvTenant", "fabricNodePEp")
   * @param value the object value containing attributes and children
   */
  @JsonAnySetter
  public void setAnyProperty(String key, Object value) {
    _className = key;
    if (value instanceof AciChild) {
      AciChild child = (AciChild) value;
      _attributes = child._attributes;
      _children = child._children;
    }
    // Jackson will also handle setting attributes and children via @JsonProperty
  }
}
