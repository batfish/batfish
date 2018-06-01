package org.batfish.representation.cisco;

import java.util.NavigableMap;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class RouteMap extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private NavigableMap<Integer, RouteMapClause> _clauses;

  public RouteMap(String name) {
    super(name);
    _clauses = new TreeMap<>();
  }

  public NavigableMap<Integer, RouteMapClause> getClauses() {
    return _clauses;
  }
}
