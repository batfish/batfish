package org.batfish.datamodel.questions.ainterpreter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum DomainType {
  REACHABILITY("reachability"),
  EXACT("exact");

  private static final Map<String, DomainType> _map = buildMap();

  private static Map<String, DomainType> buildMap() {
    ImmutableMap.Builder<String, DomainType> map = ImmutableMap.builder();
    for (DomainType value : DomainType.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  @JsonCreator
  public static DomainType fromName(String name) {
    DomainType instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + DomainType.class.getSimpleName() + " with name: \"" + name + "\"");
    }
    return instance;
  }

  private final String _name;

  DomainType(String name) {
    _name = name;
  }

  @JsonValue
  public String diffTypeName() {
    return _name;
  }
}
