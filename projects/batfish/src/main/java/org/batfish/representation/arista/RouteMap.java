package org.batfish.representation.arista;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

public class RouteMap implements Serializable {

  private @Nonnull NavigableMap<Integer, RouteMapClause> _clauses;

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
