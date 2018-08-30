package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum EdgeType {
  BGP("bgp"),
  EIGRP("eigrp"),
  ISIS("isis"),
  LAYER1("layer1"),
  LAYER2("layer2"),
  LAYER3("layer3"),
  OSPF("ospf"),
  RIP("rip");

  private static final Map<String, EdgeType> _map = buildMap();

  private static Map<String, EdgeType> buildMap() {
    ImmutableMap.Builder<String, EdgeType> map = ImmutableMap.builder();
    for (EdgeType value : EdgeType.values()) {
      String name = value._name;
      map.put(name, value);
    }
    return map.build();
  }

  @JsonCreator
  public static EdgeType fromName(String name) {
    EdgeType instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + EdgeType.class.getSimpleName() + " with name: '" + name + "'");
    }
    return instance;
  }

  private final String _name;

  EdgeType(String name) {
    _name = name;
  }

  @JsonValue
  public String edgeTypeName() {
    return _name;
  }
}
