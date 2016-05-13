package org.batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public class RouteMap extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private Map<Integer, RouteMapClause> _clauses;

   private boolean _ipv6;

   public RouteMap(String name) {
      super(name);
      _clauses = new TreeMap<Integer, RouteMapClause>();
   }

   public Map<Integer, RouteMapClause> getClauses() {
      return _clauses;
   }

   public boolean getIpv6() {
      return _ipv6;
   }

   public void setIpv6(boolean ipv6) {
      _ipv6 = ipv6;
   }

}
