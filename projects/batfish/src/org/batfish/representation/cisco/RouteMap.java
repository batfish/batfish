package org.batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ReferenceCountedStructure;

public class RouteMap extends ReferenceCountedStructure {

   private static final long serialVersionUID = 1L;

   private Map<Integer, RouteMapClause> _clauses;
   private boolean _ignore;
   private String _mapName;

   public RouteMap(String name) {
      _mapName = name;
      _clauses = new TreeMap<Integer, RouteMapClause>();
      _ignore = false;
   }

   public Map<Integer, RouteMapClause> getClauses() {
      return _clauses;
   }

   public boolean getIgnore() {
      return _ignore;
   }

   public String getMapName() {
      return _mapName;
   }

   public void setIgnore(boolean b) {
      _ignore = b;
   }

}
