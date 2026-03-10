package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class AuthenticationKeyChain implements Serializable {

  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_KEYS = "keys";
  private static final String PROP_NAME = "name";
  private static final String PROP_TOLERANCE = "tolerance";

  private String _description;

  private Map<String, AuthenticationKey> _keys;

  private final String _name;

  private int _tolerance;

  @JsonCreator
  public AuthenticationKeyChain(@JsonProperty(PROP_NAME) String name) {
    _keys = new TreeMap<>();
    _name = name;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_KEYS)
  public Map<String, AuthenticationKey> getKeys() {
    return _keys;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_TOLERANCE)
  public int getTolerance() {
    return _tolerance;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(PROP_KEYS)
  public void setKeys(Map<String, AuthenticationKey> keys) {
    _keys = keys;
  }

  @JsonProperty(PROP_TOLERANCE)
  public void setTolerance(int tolerance) {
    _tolerance = tolerance;
  }
}
