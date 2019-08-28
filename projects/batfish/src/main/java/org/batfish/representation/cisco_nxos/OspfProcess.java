package org.batfish.representation.cisco_nxos;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
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

  /** Return all redistribution policies. */
  public final @Nonnull List<RedistributionPolicy> getRedistributionPolicies() {
    return ImmutableList.copyOf(_redistributionPolicies.values());
  }

  /** Return all redistribution policies for the given protocol. */
  public final @Nonnull List<RedistributionPolicy> getRedistributionPolicies(
      NxosRoutingProtocol protocol) {
    return _redistributionPolicies.values().stream()
        .filter(rp -> rp.getInstance().getProtocol() == protocol)
        .collect(ImmutableList.toImmutableList());
  }

  /** Return the redistribution policy for the given instance, if one has been configured. */
  public final @Nullable RedistributionPolicy getRedistributionPolicy(
      RoutingProtocolInstance protocol) {
    return _redistributionPolicies.get(protocol);
  }

  /** Set the redistribution policy for the given instance. */
  public final void setRedistributionPolicy(RoutingProtocolInstance instance, String routeMap) {
    _redistributionPolicies.put(instance, new RedistributionPolicy(instance, routeMap));
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
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
  private final Map<RoutingProtocolInstance, RedistributionPolicy> _redistributionPolicies;
  private @Nullable Ip _routerId;
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
    _redistributionPolicies = new HashMap<>();
    _summaryAddresses = new HashMap<>();
    _timersLsaArrivalMbps = DEFAULT_TIMERS_LSA_ARRIVAL_MS;
    _timersLsaGroupPacingS = DEFAULT_TIMERS_LSA_GROUP_PACING_S;
    _timersLsaHoldIntervalMs = DEFAULT_TIMERS_THROTTLE_LSA_HOLD_INTERVAL_MS;
    _timersLsaMaxIntervalMs = DEFAULT_TIMERS_THROTTLE_LSA_MAX_INTERVAL_MS;
    _timersLsaStartIntervalMs = DEFAULT_TIMERS_THROTTLE_LSA_START_INTERVAL_MS;
  }
}
