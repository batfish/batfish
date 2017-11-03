package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum EquivalenceType {
  NODE("node"),
  INTERFACE("interface"),
  POLICY("policy");

  private static final Map<String, EquivalenceType> _map = buildMap();

  private static Map<String, EquivalenceType> buildMap() {
    Map<String, EquivalenceType> map = new HashMap<>();
    for (EquivalenceType value : EquivalenceType.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return Collections.unmodifiableMap(map);
  }

  @JsonCreator
  public static EquivalenceType fromName(String name) {
    EquivalenceType instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + EquivalenceType.class.getSimpleName() + " with name: \"" + name + "\"");
    }
    return instance;
  }

  private final String _name;

  EquivalenceType(String name) {
    _name = name;
  }

  @JsonValue
  public String diffTypeName() {
    return _name;
  }
}
