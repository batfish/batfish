package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** An entry of a {@link RouteMap}. */
public final class RouteMapEntry implements Serializable {

  private @Nonnull LineAction _action;
  private @Nullable RouteMapMatchAsPath _matchAsPath;
  private @Nullable RouteMapMatchCommunity _matchCommunity;
  private @Nullable RouteMapMatchInterface _matchInterface;
  private @Nullable RouteMapMatchIpAddress _matchIpAddress;
  private @Nullable RouteMapMatchIpAddressPrefixList _matchIpAddressPrefixList;
  private @Nullable RouteMapMatchMetric _matchMetric;
  private @Nullable RouteMapMatchTag _matchTag;
  private final int _sequence;
  private @Nullable RouteMapSetAsPathPrepend _setAsPathPrepend;
  private @Nullable RouteMapSetCommunity _setCommunity;
  private @Nullable RouteMapSetIpNextHop _setIpNextHop;
  private @Nullable RouteMapSetLocalPreference _setLocalPreference;
  private @Nullable RouteMapSetMetric _setMetric;
  private @Nullable RouteMapSetMetricType _setMetricType;
  private @Nullable RouteMapSetOrigin _setOrigin;
  private @Nullable RouteMapSetTag _setTag;

  public RouteMapEntry(int sequence) {
    _sequence = sequence;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nullable RouteMapMatchAsPath getMatchAsPath() {
    return _matchAsPath;
  }

  public @Nullable RouteMapMatchCommunity getMatchCommunity() {
    return _matchCommunity;
  }

  public @Nonnull Stream<RouteMapMatch> getMatches() {
    return Stream.of(
            _matchAsPath,
            _matchCommunity,
            _matchInterface,
            _matchIpAddress,
            _matchIpAddressPrefixList,
            _matchMetric,
            _matchTag)
        .filter(Objects::nonNull);
  }

  public @Nullable RouteMapMatchInterface getMatchInterface() {
    return _matchInterface;
  }

  public @Nullable RouteMapMatchIpAddress getMatchIpAddress() {
    return _matchIpAddress;
  }

  public @Nullable RouteMapMatchIpAddressPrefixList getMatchIpAddressPrefixList() {
    return _matchIpAddressPrefixList;
  }

  public @Nullable RouteMapMatchMetric getMatchMetric() {
    return _matchMetric;
  }

  public @Nullable RouteMapMatchTag getMatchTag() {
    return _matchTag;
  }

  public int getSequence() {
    return _sequence;
  }

  public @Nullable RouteMapSetAsPathPrepend getSetAsPathPrepend() {
    return _setAsPathPrepend;
  }

  public @Nullable RouteMapSetCommunity getSetCommunity() {
    return _setCommunity;
  }

  public @Nullable RouteMapSetIpNextHop getSetIpNextHop() {
    return _setIpNextHop;
  }

  public @Nullable RouteMapSetLocalPreference getSetLocalPreference() {
    return _setLocalPreference;
  }

  public @Nullable RouteMapSetMetric getSetMetric() {
    return _setMetric;
  }

  public @Nullable RouteMapSetMetricType getSetMetricType() {
    return _setMetricType;
  }

  public @Nullable RouteMapSetOrigin getSetOrigin() {
    return _setOrigin;
  }

  public @Nonnull Stream<RouteMapSet> getSets() {
    return Stream.of(
            _setAsPathPrepend,
            _setCommunity,
            _setIpNextHop,
            _setLocalPreference,
            _setMetric,
            _setMetricType,
            _setOrigin,
            _setTag)
        .filter(Objects::nonNull);
  }

  public @Nullable RouteMapSetTag getSetTag() {
    return _setTag;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setMatchAsPath(@Nullable RouteMapMatchAsPath matchAsPath) {
    _matchAsPath = matchAsPath;
  }

  public void setMatchCommunity(@Nullable RouteMapMatchCommunity matchCommunity) {
    _matchCommunity = matchCommunity;
  }

  public void setMatchInterface(@Nullable RouteMapMatchInterface matchInterface) {
    _matchInterface = matchInterface;
  }

  public void setMatchIpAddress(@Nullable RouteMapMatchIpAddress matchIpAddress) {
    _matchIpAddress = matchIpAddress;
  }

  public void setMatchIpAddressPrefixList(
      @Nullable RouteMapMatchIpAddressPrefixList matchIpAddressPrefixList) {
    _matchIpAddressPrefixList = matchIpAddressPrefixList;
  }

  public void setMatchMetric(@Nullable RouteMapMatchMetric matchMetric) {
    _matchMetric = matchMetric;
  }

  public void setMatchTag(@Nullable RouteMapMatchTag matchTag) {
    _matchTag = matchTag;
  }

  public void setSetAsPathPrepend(@Nullable RouteMapSetAsPathPrepend setAsPathPrepend) {
    _setAsPathPrepend = setAsPathPrepend;
  }

  public void setSetCommunity(@Nullable RouteMapSetCommunity setCommunity) {
    _setCommunity = setCommunity;
  }

  public void setSetIpNextHop(@Nullable RouteMapSetIpNextHop setIpNextHop) {
    _setIpNextHop = setIpNextHop;
  }

  public void setSetLocalPreference(@Nullable RouteMapSetLocalPreference setLocalPreference) {
    _setLocalPreference = setLocalPreference;
  }

  public void setSetMetric(@Nullable RouteMapSetMetric setMetric) {
    _setMetric = setMetric;
  }

  public void setSetMetricType(@Nullable RouteMapSetMetricType setMetricType) {
    _setMetricType = setMetricType;
  }

  public void setSetOrigin(@Nullable RouteMapSetOrigin setOrigin) {
    _setOrigin = setOrigin;
  }

  public void setSetTag(@Nullable RouteMapSetTag setTag) {
    _setTag = setTag;
  }
}
