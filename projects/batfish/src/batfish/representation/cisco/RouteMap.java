package batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;

import batfish.grammar.cisco.CiscoGrammar.Route_map_stanzaContext;

public class RouteMap {
   private Map<Integer, RouteMapClause> _clauses;
   private Route_map_stanzaContext _context;
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

   public Route_map_stanzaContext getContext() {
      return _context;
   }

   public boolean getIgnore() {
      return _ignore;
   }

   public String getMapName() {
      return _mapName;
   }

   public void setContext(Route_map_stanzaContext ctx) {
      _context = ctx;
   }

   public void setIgnore(boolean b) {
      _ignore = b;
   }

}
