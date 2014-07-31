package batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Route_map_tailContext;
import batfish.representation.LineAction;

public class RouteMapClause implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;
   private transient Route_map_tailContext _context;
   private boolean _ignore;
   private String _mapName;
   private List<RouteMapMatchLine> _matchList;
   private int _seqNum;
   private List<RouteMapSetLine> _setList;

   public RouteMapClause(LineAction action, String name, int num) {
      _action = action;
      _mapName = name;
      _seqNum = num;
      _matchList = new ArrayList<RouteMapMatchLine>();
      _setList = new ArrayList<RouteMapSetLine>();
   }

   public void addMatchLine(RouteMapMatchLine line) {
      _matchList.add(line);
   }

   public void addSetLine(RouteMapSetLine line) {
      _setList.add(line);
   }

   public LineAction getAction() {
      return _action;
   }

   public Route_map_tailContext getContext() {
      return _context;
   }

   public boolean getIgnore() {
      return _ignore;
   }

   public String getMapName() {
      return _mapName;
   }

   public List<RouteMapMatchLine> getMatchList() {
      return _matchList;
   }

   public int getSeqNum() {
      return _seqNum;
   }

   public List<RouteMapSetLine> getSetList() {
      return _setList;
   }

   public void setContext(Route_map_tailContext ctx) {
      _context = ctx;
   }

   public void setIgnore(boolean b) {
      _ignore = b;
   }

}
