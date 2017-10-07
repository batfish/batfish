package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum DiffType {
  INCREASED("increased"),
  REDUCED("reduced"),
  ANY("any");

  private static final Map<String, DiffType> _map = buildMap();

  private static Map<String, DiffType> buildMap() {
    Map<String, DiffType> map = new HashMap<>();
    for (DiffType value : DiffType.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return Collections.unmodifiableMap(map);
  }

  @JsonCreator
  public static DiffType fromName(String name) {
    DiffType instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + DiffType.class.getSimpleName() + " with name: \"" + name + "\"");
    }
    return instance;
  }

  private final String _name;

  DiffType(String name) {
    _name = name;
  }

  @JsonValue
  public String diffTypeName() {
    return _name;
  }
}

