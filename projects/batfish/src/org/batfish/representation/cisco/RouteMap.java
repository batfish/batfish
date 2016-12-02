package org.batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public class RouteMap extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private Map<Integer, RouteMapClause> _clauses;

   public RouteMap(String name) {
      super(name);
      _clauses = new TreeMap<>();
   }

   public Map<Integer, RouteMapClause> getClauses() {
      return _clauses;
   }

}
