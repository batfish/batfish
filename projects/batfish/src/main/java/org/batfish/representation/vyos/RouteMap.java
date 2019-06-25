package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class RouteMap implements Serializable {

  private final String _name;

  private final Map<Integer, RouteMapRule> _rules;

  public RouteMap(String name) {
    _name = name;
    _rules = new TreeMap<>();
  }

  public String getName() {
    return _name;
  }

  public Map<Integer, RouteMapRule> getRules() {
    return _rules;
  }
}
