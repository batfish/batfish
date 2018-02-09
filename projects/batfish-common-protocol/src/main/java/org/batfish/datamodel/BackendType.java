package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum BackendType {
  NOD("NoD"),
  DELTANET("deltanet"),
  DELTANET_BDD("deltanet-bdd");

  private static final Map<String, BackendType> _map = buildMap();

  private static Map<String, BackendType> buildMap() {
    ImmutableMap.Builder<String, BackendType> map = ImmutableMap.builder();
    for (BackendType value : BackendType.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  @JsonCreator
  public static BackendType fromName(String name) {
    BackendType instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + BackendType.class.getSimpleName() + " with name: \"" + name + "\"");
    }
    return instance;
  }

  private final String _name;

  BackendType(String name) {
    _name = name;
  }

  @JsonValue
  public String backendTypeName() {
    return _name;
  }
}
