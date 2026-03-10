package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

public class RouteMapClause implements Serializable {

  private @Nonnull LineAction _action;

  private RouteMapContinue _continueLine;

  private boolean _ignore;

  private String _mapName;

  private List<RouteMapMatchLine> _matchList;

  private int _seqNum;

  private List<RouteMapSetLine> _setList;

  private @Nullable RouteMapSetCommunity _setCommunity;

  private @Nullable RouteMapSetCommunityNone _setCommunityNone;

  private @Nullable RouteMapSetCommunityList _setCommunityList;

  private @Nullable RouteMapSetCommunityDelete _setCommunityDelete;

  private @Nullable RouteMapSetCommunityListDelete _setCommunityListDelete;

  private @Nullable RouteMapMatchCommunity _matchCommunity;

  public RouteMapClause(@Nonnull LineAction action, String name, int num) {
    _action = action;
    _mapName = name;
    _seqNum = num;
    _matchList = new ArrayList<>();
    _setList = new ArrayList<>();
  }

  public @Nullable RouteMapSetCommunity getSetCommunity() {
    return _setCommunity;
  }

  public void setSetCommunity(@Nullable RouteMapSetCommunity setCommunity) {
    _setCommunity = setCommunity;
  }

  public @Nullable RouteMapSetCommunityNone getSetCommunityNone() {
    return _setCommunityNone;
  }

  public void setSetCommunityNone(@Nullable RouteMapSetCommunityNone setCommunityNone) {
    _setCommunityNone = setCommunityNone;
  }

  public @Nullable RouteMapSetCommunityList getSetCommunityList() {
    return _setCommunityList;
  }

  public void setSetCommunityList(@Nullable RouteMapSetCommunityList setCommunityList) {
    _setCommunityList = setCommunityList;
  }

  public @Nullable RouteMapSetCommunityDelete getSetCommunityDelete() {
    return _setCommunityDelete;
  }

  public void setSetCommunityDelete(@Nullable RouteMapSetCommunityDelete setCommunityDelete) {
    _setCommunityDelete = setCommunityDelete;
  }

  public @Nullable RouteMapSetCommunityListDelete getSetCommunityListDelete() {
    return _setCommunityListDelete;
  }

  public void setSetCommunityListDelete(
      @Nullable RouteMapSetCommunityListDelete setCommunityListDelete) {
    _setCommunityListDelete = setCommunityListDelete;
  }

  public @Nullable RouteMapMatchCommunity getMatchCommunity() {
    return _matchCommunity;
  }

  public void setMatchCommunity(@Nullable RouteMapMatchCommunity matchCommunity) {
    _matchCommunity = matchCommunity;
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

  /**
   * Return {@code true} iff set commmunity additive or set community community-list additive is
   * set.
   */
  public boolean getAdditive() {
    return (_setCommunity != null && _setCommunity.getAdditive())
        || (_setCommunityList != null && _setCommunityList.getAdditive());
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
    return Stream.concat(_matchList.stream(), Stream.of(_matchCommunity).filter(Objects::nonNull))
        .collect(ImmutableList.toImmutableList());
  }

  public int getSeqNum() {
    return _seqNum;
  }

  public List<RouteMapSetLine> getSetList() {
    return Stream.concat(
            _setList.stream(),
            Stream.of(
                    _setCommunityDelete,
                    _setCommunityListDelete,
                    _setCommunity,
                    _setCommunityList,
                    _setCommunityNone)
                .filter(Objects::nonNull))
        .collect(ImmutableList.toImmutableList());
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

  /** Clear set community lines not ending in additive. */
  public void clearNonAdditiveSetCommunity() {
    if (_setCommunity != null && !_setCommunity.getAdditive()) {
      _setCommunity = null;
    }
    if (_setCommunityList != null && !_setCommunityList.getAdditive()) {
      _setCommunityList = null;
    }
    _setCommunityNone = null;
  }

  /** Clear set community lines not ending in delete */
  public void clearNonDeleteSetCommunity() {
    _setCommunity = null;
    _setCommunityList = null;
    _setCommunityNone = null;
  }

  /** Clear all set community lines of all types. */
  public void clearSetCommunity() {
    _setCommunityList = null;
    _setCommunity = null;
    _setCommunityDelete = null;
    _setCommunityListDelete = null;
    _setCommunityNone = null;
  }
}
