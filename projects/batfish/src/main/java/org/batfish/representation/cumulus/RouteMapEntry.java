package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;

/** Entry in a route-map supplying match conditions and transformations for a route */
@ParametersAreNonnullByDefault
public final class RouteMapEntry implements Serializable {

  private final @Nonnull LineAction _action;
  private @Nullable RouteMapCall _call;
  private @Nullable RouteMapContinue _continue;
  private @Nullable RouteMapMatchAsPath _matchAsPath;
  private @Nullable RouteMapMatchCommunity _matchCommunity;
  private @Nullable RouteMapMatchInterface _matchInterface;
  private @Nullable RouteMapMatchIpAddressPrefixLen _matchIpAddressPrefixLen;
  private @Nullable RouteMapMatchIpAddressPrefixList _matchIpAddressPrefixList;
  private @Nullable RouteMapMatchSourceProtocol _matchSourceProtocol;
  private @Nullable RouteMapMatchTag _matchTag;
  private final int _number;
  private @Nullable String _description;

  private @Nullable RouteMapSetPrependAsPath _setAsPath;
  private @Nullable RouteMapSetExcludeAsPath _setExcludeAsPath;
  private @Nullable RouteMapSetMetric _setMetric;
  private @Nullable RouteMapSetMetricType _setMetricType;
  private @Nullable RouteMapSetIpNextHopLiteral _setIpNextHop;
  private @Nullable RouteMapSetCommListDelete _setCommListDelete;
  private @Nullable RouteMapSetCommunity _setCommunity;
  private @Nullable RouteMapSetLocalPreference _setLocalPreference;
  private @Nullable RouteMapSetTag _setTag;
  private @Nullable RouteMapSetWeight _setWeight;

  public RouteMapEntry(int number, LineAction action) {
    _number = number;
    _action = action;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  @Nullable
  public RouteMapCall getCall() {
    return _call;
  }

  public void setCall(@Nullable RouteMapCall call) {
    _call = call;
  }

  /** Return stream of match statements for this entry. */
  public @Nonnull Stream<RouteMapMatch> getMatches() {
    return Stream.of(
            _matchAsPath,
            _matchInterface,
            _matchCommunity,
            _matchIpAddressPrefixLen,
            _matchIpAddressPrefixList,
            _matchSourceProtocol,
            _matchTag)
        .filter(Objects::nonNull);
  }

  public @Nullable RouteMapMatchAsPath getMatchAsPath() {
    return _matchAsPath;
  }

  public @Nullable RouteMapMatchInterface getMatchInterface() {
    return _matchInterface;
  }

  public @Nullable RouteMapMatchCommunity getMatchCommunity() {
    return _matchCommunity;
  }

  public @Nullable RouteMapMatchIpAddressPrefixLen getMatchIpAddressPrefixLen() {
    return _matchIpAddressPrefixLen;
  }

  public void setMatchIpAddressPrefixLen(
      @Nullable RouteMapMatchIpAddressPrefixLen matchIpAddressPrefixLen) {
    _matchIpAddressPrefixLen = matchIpAddressPrefixLen;
  }

  public @Nullable RouteMapMatchIpAddressPrefixList getMatchIpAddressPrefixList() {
    return _matchIpAddressPrefixList;
  }

  public @Nullable RouteMapMatchSourceProtocol getMatchSourceProtocol() {
    return _matchSourceProtocol;
  }

  public void setMatchSourceProtocol(@Nullable RouteMapMatchSourceProtocol matchSourceProtocol) {
    _matchSourceProtocol = matchSourceProtocol;
  }

  @Nullable
  public RouteMapMatchTag getMatchTag() {
    return _matchTag;
  }

  public void setMatchTag(@Nullable RouteMapMatchTag matchTag) {
    _matchTag = matchTag;
  }

  public int getNumber() {
    return _number;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  /** Return stream of set statements for this entry. */
  public @Nonnull Stream<RouteMapSet> getSets() {
    return Stream.of(
            _setAsPath,
            _setExcludeAsPath,
            _setCommListDelete,
            _setMetric,
            _setMetricType,
            _setIpNextHop,
            _setCommunity,
            _setLocalPreference,
            _setTag,
            _setWeight)
        .filter(Objects::nonNull);
  }

  public @Nullable RouteMapSetPrependAsPath getSetAsPath() {
    return _setAsPath;
  }

  public @Nullable RouteMapSetExcludeAsPath getSetExcludeAsPath() {
    return _setExcludeAsPath;
  }

  public @Nullable RouteMapSetMetric getSetMetric() {
    return _setMetric;
  }

  public @Nullable RouteMapSetMetricType getSetMetricType() {
    return _setMetricType;
  }

  public @Nullable RouteMapSetIpNextHopLiteral getSetIpNextHop() {
    return _setIpNextHop;
  }

  public @Nullable RouteMapSetCommListDelete getSetCommListDelete() {
    return _setCommListDelete;
  }

  public @Nullable RouteMapSetCommunity getSetCommunity() {
    return _setCommunity;
  }

  public @Nullable RouteMapSetWeight getSetWeight() {
    return _setWeight;
  }

  public void setMatchAsPath(@Nullable RouteMapMatchAsPath matchAsPath) {
    _matchAsPath = matchAsPath;
  }

  public void setMatchInterface(@Nullable RouteMapMatchInterface matchInterface) {
    _matchInterface = matchInterface;
  }

  public void setMatchCommunity(@Nullable RouteMapMatchCommunity matchCommunity) {
    _matchCommunity = matchCommunity;
  }

  public void setMatchIpAddressPrefixList(
      @Nullable RouteMapMatchIpAddressPrefixList matchIpAddressPrefixList) {
    _matchIpAddressPrefixList = matchIpAddressPrefixList;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  // TODO: check usage
  public void setSetAsPath(@Nullable RouteMapSetPrependAsPath setAsPath) {
    _setAsPath = setAsPath;
  }

  public void setSetExcludeAsPath(@Nullable RouteMapSetExcludeAsPath setExcludeAsPath) {
    _setExcludeAsPath = setExcludeAsPath;
  }

  public void setSetIpNextHop(@Nullable RouteMapSetIpNextHopLiteral setIpNextHop) {
    _setIpNextHop = setIpNextHop;
  }

  public void setSetMetric(@Nullable RouteMapSetMetric setMetric) {
    _setMetric = setMetric;
  }

  public void setSetMetricType(RouteMapSetMetricType setMetricType) {
    _setMetricType = setMetricType;
  }

  public void setSetWeight(@Nullable RouteMapSetWeight setWeight) {
    _setWeight = setWeight;
  }

  public void setSetCommListDelete(@Nullable RouteMapSetCommListDelete setCommListDelete) {
    _setCommListDelete = setCommListDelete;
  }

  public void setSetCommunity(@Nullable RouteMapSetCommunity setCommunity) {
    _setCommunity = setCommunity;
  }

  @Nullable
  public RouteMapSetLocalPreference getSetLocalPreference() {
    return _setLocalPreference;
  }

  public void setSetLocalPreference(@Nullable RouteMapSetLocalPreference setLocalPreference) {
    _setLocalPreference = setLocalPreference;
  }

  @Nullable
  public RouteMapSetTag getSetTag() {
    return _setTag;
  }

  public void setSetTag(@Nullable RouteMapSetTag setTag) {
    _setTag = setTag;
  }

  @Nullable
  public RouteMapContinue getContinue() {
    return _continue;
  }

  public void setContinue(@Nullable RouteMapContinue aContinue) {
    _continue = aContinue;
  }
}
