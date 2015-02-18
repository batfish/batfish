package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.batfish.representation.LineAction;

public class RouteMapClause implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;
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

   public void setIgnore(boolean b) {
      _ignore = b;
   }

}
