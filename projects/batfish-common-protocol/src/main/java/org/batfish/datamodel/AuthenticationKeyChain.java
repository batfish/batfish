package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class AuthenticationKeyChain extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private static final String PROP_DESCRIPTION = "description";

  private static final String PROP_KEYS = "keys";

  private static final String PROP_TOLERANCE = "tolerance";

  private String _description;

  private Map<String, AuthenticationKey> _keys;

  private int _tolerance;

  public AuthenticationKeyChain(String name) {
    super(name);
    _description = "";
    _keys = new TreeMap<>();
    _tolerance = 0;
  }

  @JsonCreator
  public AuthenticationKeyChain(@JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_KEYS) Map<String, AuthenticationKey> keys,
      @JsonProperty(PROP_TOLERANCE) int tolerance) {
    super(name);
    _description = description;
    _keys = keys;
    _tolerance = tolerance;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_KEYS)
  public Map<String, AuthenticationKey> getKeys() {
    return _keys;
  }

  @JsonProperty(PROP_TOLERANCE)
  public int getTolerance() {
    return _tolerance;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    this._description = description;
  }

  @JsonProperty(PROP_KEYS)
  public void setKeys(Map<String, AuthenticationKey> keys) {
    this._keys = keys;
  }

  @JsonProperty(PROP_TOLERANCE)
  public void setTolerance(int tolerance) {
    this._tolerance = tolerance;
  }

}
