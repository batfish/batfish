package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing the root ACI polUni (Policy Universe) object.
 *
 * <p>The polUni is the top-level container in the ACI object model. It contains all ACI
 * configuration objects as children. The JSON structure is:
 *
 * <pre>{@code
 * {
 *   "polUni": {
 *     "attributes": {"dn": "uni", ...},
 *     "children": [
 *       {"fvTenant": {"attributes": {...}, "children": [...]}},
 *       {"fabricNodePEp": {"attributes": {...}, "children": [...]}},
 *       ...
 *     ]
 *   }
 * }
 * }</pre>
 *
 * @see AciChild
 * @see AciAttributes
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AciPolUni implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<AciChild> _children;

  /**
   * Returns the attributes of the polUni object. Typically contains the distinguished name ("dn")
   * with value "uni".
   *
   * @return the attributes, or null if not present
   */
  public @Nullable AciAttributes getAttributes() {
    return _attributes;
  }

  /**
   * Sets the attributes of the polUni object.
   *
   * @param attributes the attributes to set
   */
  public void setAttributes(@Nullable AciAttributes attributes) {
    _attributes = attributes;
  }

  /**
   * Returns the list of child objects contained in the polUni. These children represent all
   * top-level ACI configuration objects such as tenants (fvTenant), fabric nodes (fabricNodePEp),
   * and other policy containers.
   *
   * @return the list of children, or null if not present
   */
  public @Nullable List<AciChild> getChildren() {
    return _children;
  }

  /**
   * Sets the list of child objects for the polUni.
   *
   * @param children the children to set
   */
  public void setChildren(@Nullable List<AciChild> children) {
    _children = children;
  }
}
