package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public abstract class OspfProcess implements Serializable {

  public static final int DEFAULT_AUTO_COST_REFERENCE_BANDWIDTH = 40_000; // Mbps
  public static final int DEFAULT_TIMERS_LSA_ARRIVAL = 1000; // ms

  // https://www.cisco.com/c/m/en_us/techdoc/dc/reference/cli/nxos/commands/ospf/timers-lsa-group-pacing-ospf.html
  public static final int DEFAULT_TIMERS_LSA_GROUP_PACING = 240; // s
  public static final int DEFAULT_TIMERS_THROTTLE_LSA_START_INTERVAL = 0; // ms
  public static final int DEFAULT_TIMERS_THROTTLE_LSA_HOLD_INTERVAL = 5000; // ms
  public static final int DEFAULT_TIMERS_THROTTLE_LSA_MAX_INTERVAL = 5000; // ms

  public @Nonnull Map<Long, OspfArea> getAreas() {
    return _areas;
  }

  public int getAutoCostReferenceBandwidth() {
    return _autoCostReferenceBandwidth;
  }

  public void setAutoCostReferenceBandwidth(int autoCostReferenceBandwidth) {
    _autoCostReferenceBandwidth = autoCostReferenceBandwidth;
  }

  public boolean getBfd() {
    return _bfd;
  }

  public void setBfd(boolean bfd) {
    _bfd = bfd;
  }

  public @Nullable OspfDefaultOriginate getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable OspfDefaultOriginate defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  public @Nullable OspfMaxMetricRouterLsa getMaxMetricRouterLsa() {
    return _maxMetricRouterLsa;
  }

  public void setMaxMetricRouterLsa(@Nullable OspfMaxMetricRouterLsa maxMetricRouterLsa) {
    _maxMetricRouterLsa = maxMetricRouterLsa;
  }

  public @Nonnull Map<IpWildcard, Long> getNetworks() {
    return _networks;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public void setPassiveInterfaceDefault(boolean passiveInterfaceDefault) {
    _passiveInterfaceDefault = passiveInterfaceDefault;
  }

  public @Nullable String getRedistributeDirectRouteMap() {
    return _redistributeDirectRouteMap;
  }

  public void setRedistributeDirectRouteMap(@Nullable String redistributeDirectRouteMap) {
    _redistributeDirectRouteMap = redistributeDirectRouteMap;
  }

  public @Nullable String getRedistributeStaticRouteMap() {
    return _redistributeStaticRouteMap;
  }

  public void setRedistributeStaticRouteMap(@Nullable String redistributeStaticRouteMap) {
    _redistributeStaticRouteMap = redistributeStaticRouteMap;
  }

  public @Nonnull Map<Prefix, OspfSummaryAddress> getSummaryAddresses() {
    return _summaryAddresses;
  }

  public int getTimersLsaArrival() {
    return _timersLsaArrival;
  }

  public void setTimersLsaArrival(int timersLsaArrival) {
    _timersLsaArrival = timersLsaArrival;
  }

  public int getTimersLsaGroupPacing() {
    return _timersLsaGroupPacing;
  }

  public void setTimersLsaGroupPacing(int timersLsaGroupPacing) {
    _timersLsaGroupPacing = timersLsaGroupPacing;
  }

  public int getTimersLsaHoldInterval() {
    return _timersLsaHoldInterval;
  }

  public void setTimersLsaHoldInterval(int timersLsaHoldInterval) {
    _timersLsaHoldInterval = timersLsaHoldInterval;
  }

  public int getTimersLsaMaxInterval() {
    return _timersLsaMaxInterval;
  }

  public void setTimersLsaMaxInterval(int timersLsaMaxInterval) {
    _timersLsaMaxInterval = timersLsaMaxInterval;
  }

  public int getTimersLsaStartInterval() {
    return _timersLsaStartInterval;
  }

  public void setTimersLsaStartInterval(int timersLsaStartInterval) {
    _timersLsaStartInterval = timersLsaStartInterval;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull Map<Long, OspfArea> _areas;
  private int _autoCostReferenceBandwidth;
  private boolean _bfd;
  private @Nullable OspfDefaultOriginate _defaultOriginate;
  private @Nullable OspfMaxMetricRouterLsa _maxMetricRouterLsa;
  private final @Nonnull Map<IpWildcard, Long> _networks;
  private boolean _passiveInterfaceDefault;
  private @Nullable String _redistributeDirectRouteMap;
  private @Nullable String _redistributeStaticRouteMap;
  private final @Nonnull Map<Prefix, OspfSummaryAddress> _summaryAddresses;
  private int _timersLsaArrival;
  private int _timersLsaGroupPacing;
  private int _timersLsaHoldInterval;
  private int _timersLsaMaxInterval;
  private int _timersLsaStartInterval;

  protected OspfProcess() {
    _areas = new HashMap<>();
    _autoCostReferenceBandwidth = DEFAULT_AUTO_COST_REFERENCE_BANDWIDTH;
    _networks = new HashMap<>();
    _summaryAddresses = new HashMap<>();
    _timersLsaArrival = DEFAULT_TIMERS_LSA_ARRIVAL;
    _timersLsaGroupPacing = DEFAULT_TIMERS_LSA_GROUP_PACING;
    _timersLsaHoldInterval = DEFAULT_TIMERS_THROTTLE_LSA_HOLD_INTERVAL;
    _timersLsaMaxInterval = DEFAULT_TIMERS_THROTTLE_LSA_MAX_INTERVAL;
    _timersLsaStartInterval = DEFAULT_TIMERS_THROTTLE_LSA_START_INTERVAL;
  }
}
