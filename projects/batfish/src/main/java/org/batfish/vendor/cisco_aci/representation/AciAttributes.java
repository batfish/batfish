package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Data model class representing the attributes object in ACI configuration objects.
 *
 * <p>Every ACI object contains an "attributes" object with properties specific to that object type.
 * Common attributes include:
 *
 * <ul>
 *   <li>dn (distinguished name) - the unique identifier for the object
 *   <li>name - the object name
 *   <li>descr or description - a description of the object
 *   <li>annotation - user annotations
 *   <li>nameAlias - an alternative name
 *   <li>status - object status (e.g., "created", "modified")
 *   <li>Various type-specific attributes
 * </ul>
 *
 * <p>Since different ACI object types have different attributes, this class uses {@link
 * JsonAnySetter} to capture all properties dynamically while also providing typed getters for
 * common attributes.
 *
 * <p>Example:
 *
 * <pre>{@code
 * "attributes": {
 *   "dn": "uni/tn-mgmt",
 *   "name": "mgmt",
 *   "descr": "Management Tenant",
 *   "annotation": "",
 *   "nameAlias": "",
 *   "userdom": "all"
 * }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AciAttributes implements Serializable {

  /** Map storing all attribute values keyed by attribute name. */
  private final Map<String, String> _attributes;

  /** Constructs a new empty AciAttributes object. */
  public AciAttributes() {
    _attributes = new LinkedHashMap<>();
  }

  /**
   * Dynamic setter called by Jackson for any property in the attributes object. This allows
   * capturing any attribute regardless of its name.
   *
   * @param key the attribute name
   * @param value the attribute value
   */
  @JsonAnySetter
  public void setAnyAttribute(String key, String value) {
    _attributes.put(key, value);
  }

  /**
   * Returns all attributes as a map. This is used by Jackson for serialization and allows access to
   * any attribute value.
   *
   * @return a map of all attribute names to values
   */
  @JsonAnyGetter
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Returns the value of a specific attribute by name.
   *
   * @param name the attribute name
   * @return the attribute value, or null if not present
   */
  public @Nullable String get(String name) {
    return _attributes.get(name);
  }

  /**
   * Sets an attribute value.
   *
   * @param name the attribute name
   * @param value the attribute value
   */
  public void put(String name, String value) {
    _attributes.put(name, value);
  }

  /**
   * Returns the distinguished name (dn) of the ACI object. The DN is the unique identifier for the
   * object in the ACI management information tree.
   *
   * @return the distinguished name, or null if not present
   */
  @JsonProperty("dn")
  public @Nullable String getDistinguishedName() {
    return _attributes.get("dn");
  }

  /**
   * Returns the name of the ACI object.
   *
   * @return the object name, or null if not present
   */
  @JsonProperty("name")
  public @Nullable String getName() {
    return _attributes.get("name");
  }

  /**
   * Returns the description of the ACI object. In ACI, the description attribute is named "descr".
   *
   * @return the description, or null if not present
   */
  @JsonProperty("descr")
  public @Nullable String getDescription() {
    return _attributes.get("descr");
  }

  /**
   * Returns the annotation of the ACI object.
   *
   * @return the annotation, or null if not present
   */
  @JsonProperty("annotation")
  public @Nullable String getAnnotation() {
    return _attributes.get("annotation");
  }

  /**
   * Returns the name alias of the ACI object.
   *
   * @return the name alias, or null if not present
   */
  @JsonProperty("nameAlias")
  public @Nullable String getNameAlias() {
    return _attributes.get("nameAlias");
  }

  /**
   * Returns the status of the ACI object.
   *
   * @return the status, or null if not present
   */
  @JsonProperty("status")
  public @Nullable String getStatus() {
    return _attributes.get("status");
  }

  /**
   * Returns the user domain of the ACI object.
   *
   * @return the user domain, or null if not present
   */
  @JsonProperty("userdom")
  public @Nullable String getUserDomain() {
    return _attributes.get("userdom");
  }

  /**
   * Returns the owner key of the ACI object.
   *
   * @return the owner key, or null if not present
   */
  @JsonProperty("ownerKey")
  public @Nullable String getOwnerKey() {
    return _attributes.get("ownerKey");
  }

  /**
   * Returns the owner tag of the ACI object.
   *
   * @return the owner tag, or null if not present
   */
  @JsonProperty("ownerTag")
  public @Nullable String getOwnerTag() {
    return _attributes.get("ownerTag");
  }

  /**
   * Checks if an attribute with the given name exists.
   *
   * @param name the attribute name
   * @return true if the attribute exists, false otherwise
   */
  public boolean hasAttribute(String name) {
    return _attributes.containsKey(name);
  }

  /**
   * Returns the number of attributes in this object.
   *
   * @return the attribute count
   */
  public int size() {
    return _attributes.size();
  }
}
