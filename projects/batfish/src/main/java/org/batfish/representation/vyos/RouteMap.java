package org.batfish.representation.vyos;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class RouteMap extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final Map<Integer, RouteMapRule> _rules;

  public RouteMap(String name) {
    super(name);
    _rules = new TreeMap<>();
  }

  public Map<Integer, RouteMapRule> getRules() {
    return _rules;
  }
}
