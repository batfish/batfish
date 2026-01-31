package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** An entry of a {@link RouteMap}. */
public final class RouteMapEntry implements Serializable {

  private @Nonnull LineAction _action;
  private @Nullable Integer _continue;
  private @Nullable RouteMapMatchAsNumber _matchAsNumber;
  private @Nullable RouteMapMatchAsPath _matchAsPath;
  private @Nullable RouteMapMatchCommunity _matchCommunity;
  private @Nullable RouteMapMatchInterface _matchInterface;
  private @Nullable RouteMapMatchIpAddress _matchIpAddress;
  private @Nullable RouteMapMatchIpAddressPrefixList _matchIpAddressPrefixList;
  private @Nullable RouteMapMatchIpMulticast _matchIpMulticast;
  private @Nullable RouteMapMatchIpv6Address _matchIpv6Address;
  private @Nullable RouteMapMatchIpv6AddressPrefixList _matchIpv6AddressPrefixList;
  private @Nullable RouteMapMatchMetric _matchMetric;
  private @Nullable RouteMapMatchRouteType _matchRouteType;
  private @Nullable RouteMapMatchSourceProtocol _matchSourceProtocol;
  private @Nullable RouteMapMatchTag _matchTag;
  private @Nullable RouteMapMatchVlan _matchVlan;
  private final int _sequence;
  private @Nullable RouteMapSetAsPathPrepend _setAsPathPrepend;
  private @Nullable RouteMapSetCommListDelete _setCommListDelete;
  private @Nullable RouteMapSetCommunity _setCommunity;
  private @Nullable RouteMapSetIpNextHop _setIpNextHop;
  private @Nullable RouteMapSetLocalPreference _setLocalPreference;
  private @Nullable RouteMapSetMetric _setMetric;
  private @Nullable RouteMapSetMetricEigrp _setMetricEigrp;
  private @Nullable RouteMapSetMetricType _setMetricType;
  private @Nullable RouteMapSetOrigin _setOrigin;
  private @Nullable RouteMapSetTag _setTag;
  private @Nullable RouteMapSetWeight _setWeight;

  public RouteMapEntry(int sequence) {
    _sequence = sequence;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nullable Integer getContinue() {
    return _continue;
  }

  public @Nullable RouteMapMatchAsNumber getMatchAsNumber() {
    return _matchAsNumber;
  }

  public @Nullable RouteMapMatchAsPath getMatchAsPath() {
    return _matchAsPath;
  }

  public @Nullable RouteMapMatchCommunity getMatchCommunity() {
    return _matchCommunity;
  }

  public @Nonnull Stream<RouteMapMatch> getMatches() {
    return Stream.of(
            _matchAsNumber,
            _matchAsPath,
            _matchCommunity,
            _matchInterface,
            _matchIpAddress,
            _matchIpAddressPrefixList,
            _matchIpMulticast,
            _matchIpv6Address,
            _matchIpv6AddressPrefixList,
            _matchMetric,
            _matchRouteType,
            _matchSourceProtocol,
            _matchTag,
            _matchVlan)
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

  public @Nullable RouteMapMatchIpMulticast getMatchIpMulticast() {
    return _matchIpMulticast;
  }

  public @Nullable RouteMapMatchIpv6Address getMatchIpv6Address() {
    return _matchIpv6Address;
  }

  public @Nullable RouteMapMatchIpv6AddressPrefixList getMatchIpv6AddressPrefixList() {
    return _matchIpv6AddressPrefixList;
  }

  public @Nullable RouteMapMatchMetric getMatchMetric() {
    return _matchMetric;
  }

  public @Nullable RouteMapMatchRouteType getMatchRouteType() {
    return _matchRouteType;
  }

  public @Nullable RouteMapMatchSourceProtocol getMatchSourceProtocol() {
    return _matchSourceProtocol;
  }

  public @Nullable RouteMapMatchTag getMatchTag() {
    return _matchTag;
  }

  public @Nullable RouteMapMatchVlan getMatchVlan() {
    return _matchVlan;
  }

  public int getSequence() {
    return _sequence;
  }

  public @Nullable RouteMapSetAsPathPrepend getSetAsPathPrepend() {
    return _setAsPathPrepend;
  }

  public @Nullable RouteMapSetCommListDelete getSetCommListDelete() {
    return _setCommListDelete;
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

  public @Nullable RouteMapSetMetricEigrp getSetMetricEigrp() {
    return _setMetricEigrp;
  }

  public @Nullable RouteMapSetMetricType getSetMetricType() {
    return _setMetricType;
  }

  public @Nullable RouteMapSetOrigin getSetOrigin() {
    return _setOrigin;
  }

  public @Nonnull Stream<RouteMapSet> getSets() {
    // set comm-list delete before set community:
    // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus7000/sw/unicast/command/reference/n7k_unicast_cmds/l3_cmds_s.html#set_comm-list_delete
    return Stream.of(
            _setAsPathPrepend,
            _setCommListDelete,
            _setCommunity,
            _setIpNextHop,
            _setLocalPreference,
            _setMetric,
            _setMetricEigrp,
            _setMetricType,
            _setOrigin,
            _setTag,
            _setWeight)
        .filter(Objects::nonNull);
  }

  public @Nullable RouteMapSetTag getSetTag() {
    return _setTag;
  }

  public @Nullable RouteMapSetWeight getSetWeight() {
    return _setWeight;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setContinue(@Nullable Integer continueNumber) {
    _continue = continueNumber;
  }

  public void setMatchAsNumber(@Nullable RouteMapMatchAsNumber matchAsNumber) {
    _matchAsNumber = matchAsNumber;
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

  public void setMatchIpMulticast(@Nullable RouteMapMatchIpMulticast matchIpMulticast) {
    _matchIpMulticast = matchIpMulticast;
  }

  public void setMatchIpv6Address(@Nullable RouteMapMatchIpv6Address matchIpv6Address) {
    _matchIpv6Address = matchIpv6Address;
  }

  public void setMatchIpv6AddressPrefixList(
      @Nullable RouteMapMatchIpv6AddressPrefixList matchIpv6AddressPrefixList) {
    _matchIpv6AddressPrefixList = matchIpv6AddressPrefixList;
  }

  public void setMatchMetric(@Nullable RouteMapMatchMetric matchMetric) {
    _matchMetric = matchMetric;
  }

  public void setMatchRouteType(@Nullable RouteMapMatchRouteType matchRouteType) {
    _matchRouteType = matchRouteType;
  }

  public void setMatchSourceProtocol(@Nullable RouteMapMatchSourceProtocol matchSourceProtocol) {
    _matchSourceProtocol = matchSourceProtocol;
  }

  public void setMatchTag(@Nullable RouteMapMatchTag matchTag) {
    _matchTag = matchTag;
  }

  public void setMatchVlan(@Nullable RouteMapMatchVlan matchVlan) {
    _matchVlan = matchVlan;
  }

  public void setSetAsPathPrepend(@Nullable RouteMapSetAsPathPrepend setAsPathPrepend) {
    _setAsPathPrepend = setAsPathPrepend;
  }

  public void setSetCommListDelete(@Nullable RouteMapSetCommListDelete setCommListDelete) {
    _setCommListDelete = setCommListDelete;
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

  public void setSetMetricEigrp(@Nullable RouteMapSetMetricEigrp setMetricEigrp) {
    _setMetricEigrp = setMetricEigrp;
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

  public void setSetWeight(RouteMapSetWeight routeMapSetWeight) {
    _setWeight = routeMapSetWeight;
  }
}
