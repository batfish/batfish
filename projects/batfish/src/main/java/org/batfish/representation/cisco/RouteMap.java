package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.TreeMap;

public class RouteMap implements Serializable {

  private static final long serialVersionUID = 1L;

  private NavigableMap<Integer, RouteMapClause> _clauses;

  private final String _name;

  public RouteMap(String name) {
    _name = name;
    _clauses = new TreeMap<>();
  }

  public NavigableMap<Integer, RouteMapClause> getClauses() {
    return _clauses;
  }

  public String getName() {
    return _name;
  }
}
