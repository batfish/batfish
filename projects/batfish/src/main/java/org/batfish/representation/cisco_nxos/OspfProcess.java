package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public abstract class OspfProcess implements Serializable {

  public static final int DEFAULT_AUTO_COST_REFERENCE_BANDWIDTH_MBPS = 40_000; // Mbps
  public static final int DEFAULT_TIMERS_LSA_ARRIVAL_MS = 1000; // ms

  // https://www.cisco.com/c/m/en_us/techdoc/dc/reference/cli/nxos/commands/ospf/timers-lsa-group-pacing-ospf.html
  public static final int DEFAULT_TIMERS_LSA_GROUP_PACING_S = 240; // s
  public static final int DEFAULT_TIMERS_THROTTLE_LSA_START_INTERVAL_MS = 0; // ms
  public static final int DEFAULT_TIMERS_THROTTLE_LSA_HOLD_INTERVAL_MS = 5000; // ms
  public static final int DEFAULT_TIMERS_THROTTLE_LSA_MAX_INTERVAL_MS = 5000; // ms

  public @Nonnull Map<Long, OspfArea> getAreas() {
    return _areas;
  }

  public int getAutoCostReferenceBandwidthMbps() {
    return _autoCostReferenceBandwidthMbps;
  }

  public void setAutoCostReferenceBandwidthMbps(int autoCostReferenceBandwidthMbps) {
    _autoCostReferenceBandwidthMbps = autoCostReferenceBandwidthMbps;
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
    return _timersLsaArrivalMbps;
  }

  public void setTimersLsaArrival(int timersLsaArrival) {
    _timersLsaArrivalMbps = timersLsaArrival;
  }

  public int getTimersLsaGroupPacing() {
    return _timersLsaGroupPacingS;
  }

  public void setTimersLsaGroupPacing(int timersLsaGroupPacing) {
    _timersLsaGroupPacingS = timersLsaGroupPacing;
  }

  public int getTimersLsaHoldInterval() {
    return _timersLsaHoldIntervalMs;
  }

  public void setTimersLsaHoldInterval(int timersLsaHoldInterval) {
    _timersLsaHoldIntervalMs = timersLsaHoldInterval;
  }

  public int getTimersLsaMaxInterval() {
    return _timersLsaMaxIntervalMs;
  }

  public void setTimersLsaMaxInterval(int timersLsaMaxInterval) {
    _timersLsaMaxIntervalMs = timersLsaMaxInterval;
  }

  public int getTimersLsaStartInterval() {
    return _timersLsaStartIntervalMs;
  }

  public void setTimersLsaStartInterval(int timersLsaStartInterval) {
    _timersLsaStartIntervalMs = timersLsaStartInterval;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull Map<Long, OspfArea> _areas;
  private int _autoCostReferenceBandwidthMbps;
  private boolean _bfd;
  private @Nullable OspfDefaultOriginate _defaultOriginate;
  private @Nullable OspfMaxMetricRouterLsa _maxMetricRouterLsa;
  private final @Nonnull Map<IpWildcard, Long> _networks;
  private boolean _passiveInterfaceDefault;
  private @Nullable String _redistributeDirectRouteMap;
  private @Nullable String _redistributeStaticRouteMap;
  private final @Nonnull Map<Prefix, OspfSummaryAddress> _summaryAddresses;
  private int _timersLsaArrivalMbps;
  private int _timersLsaGroupPacingS;
  private int _timersLsaHoldIntervalMs;
  private int _timersLsaMaxIntervalMs;
  private int _timersLsaStartIntervalMs;

  protected OspfProcess() {
    _areas = new HashMap<>();
    _autoCostReferenceBandwidthMbps = DEFAULT_AUTO_COST_REFERENCE_BANDWIDTH_MBPS;
    _networks = new HashMap<>();
    _summaryAddresses = new HashMap<>();
    _timersLsaArrivalMbps = DEFAULT_TIMERS_LSA_ARRIVAL_MS;
    _timersLsaGroupPacingS = DEFAULT_TIMERS_LSA_GROUP_PACING_S;
    _timersLsaHoldIntervalMs = DEFAULT_TIMERS_THROTTLE_LSA_HOLD_INTERVAL_MS;
    _timersLsaMaxIntervalMs = DEFAULT_TIMERS_THROTTLE_LSA_MAX_INTERVAL_MS;
    _timersLsaStartIntervalMs = DEFAULT_TIMERS_THROTTLE_LSA_START_INTERVAL_MS;
  }
}
