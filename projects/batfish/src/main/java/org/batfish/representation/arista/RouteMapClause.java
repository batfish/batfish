package org.batfish.representation.arista;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

public class RouteMapClause implements Serializable {

  private @Nonnull LineAction _action;

  private RouteMapContinue _continueLine;

  private boolean _ignore;

  private String _mapName;

  private List<RouteMapMatchLine> _matchList;

  private int _seqNum;

  private List<RouteMapSetLine> _setList;

  public RouteMapClause(@Nonnull LineAction action, String name, int num) {
    _action = action;
    _mapName = name;
    _seqNum = num;
    _matchList = new ArrayList<>();
    _setList = new ArrayList<>();
  }

  public void addMatchLine(RouteMapMatchLine line) {
    _matchList.add(line);
  }

  public void addSetLine(RouteMapSetLine line) {
    _setList.add(line);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public RouteMapContinue getContinueLine() {
    return _continueLine;
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

  public void setAction(@Nonnull LineAction action) {
    _action = action;
  }

  public void setContinueLine(RouteMapContinue continueLine) {
    _continueLine = continueLine;
  }

  public void setIgnore(boolean b) {
    _ignore = b;
  }
}
