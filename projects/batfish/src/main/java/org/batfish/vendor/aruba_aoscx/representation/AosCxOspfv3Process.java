package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix6;

/** Vendor-specific representation of an Aruba AOS-CX OSPFv3 process. */
public final class AosCxOspfv3Process
    implements Serializable {

  /** AOS-CX default administrative distance for OSPFv3. */
  public static final int DEFAULT_ADMIN_DISTANCE =
      110;

  /** AOS-CX default reference bandwidth: 100000 Mbps. */
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100_000_000_000D;

  /** AOS-CX default metric for redistributed OSPFv3 routes. */
  public static final long DEFAULT_REDISTRIBUTION_METRIC =
      25L;

  /** AOS-CX default metric for default-information origination. */
  public static final long DEFAULT_INFORMATION_METRIC =
      1L;

  /** AOS-CX default number of OSPFv3 ECMP paths. */
  public static final int DEFAULT_MAXIMUM_PATHS =
      4;

  /** AOS-CX default graceful-restart restart interval in seconds. */
  public static final int
      DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS =
          120;

  /** Default duration for max-metric router-lsa on-startup. */
  public static final int
      DEFAULT_MAX_METRIC_ROUTER_LSA_ON_STARTUP_SECONDS =
          600;

  public AosCxOspfv3Process(int processId) {
    _processId = processId;
  }

  public int getProcessId() {
    return _processId;
  }

  public boolean
      getActiveBackboneStubDefaultRoute() {

    return _activeBackboneStubDefaultRoute;
  }

  public void setActiveBackboneStubDefaultRoute(
      boolean activeBackboneStubDefaultRoute) {

    _activeBackboneStubDefaultRoute =
        activeBackboneStubDefaultRoute;
  }

  public boolean getBfdAllInterfaces() {
    return _bfdAllInterfaces;
  }

  public void setBfdAllInterfaces(
      boolean enabled) {

    _bfdAllInterfaces =
        enabled;
  }

  public int getGracefulRestartIntervalSeconds() {
    return _gracefulRestartIntervalSeconds;
  }

  public void setGracefulRestartIntervalSeconds(
      int seconds) {

    _gracefulRestartIntervalSeconds =
        seconds;
  }

  public void resetGracefulRestartInterval() {

    _gracefulRestartIntervalSeconds =
        DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS;
  }

  public boolean getGracefulRestartHelper() {

    return _gracefulRestartHelper;
  }

  public boolean
      getGracefulRestartHelperStrictLsaCheck() {

    return _gracefulRestartHelperStrictLsaCheck;
  }

  public void setGracefulRestartHelper(
      boolean helper,
      boolean strictLsaCheck) {

    _gracefulRestartHelper =
        helper;

    _gracefulRestartHelperStrictLsaCheck =
        helper
            && strictLsaCheck;
  }

  public boolean
      getGracefulRestartIgnoreLostInterface() {

    return _gracefulRestartIgnoreLostInterface;
  }

  public void
      setGracefulRestartIgnoreLostInterface(
          boolean ignoreLostInterface) {

    _gracefulRestartIgnoreLostInterface =
        ignoreLostInterface;
  }

  public int getMaximumPaths() {
    return _maximumPaths;
  }

  public void setMaximumPaths(
      int maximumPaths) {
    _maximumPaths = maximumPaths;
  }

  public void resetMaximumPaths() {
    _maximumPaths =
        DEFAULT_MAXIMUM_PATHS;
  }

  public boolean getMaxMetricRouterLsa() {
    return _maxMetricRouterLsa;
  }

  public void setMaxMetricRouterLsa(
      boolean maxMetricRouterLsa) {

    _maxMetricRouterLsa =
        maxMetricRouterLsa;
  }

  public @Nullable Integer
      getMaxMetricRouterLsaOnStartupSeconds() {

    return _maxMetricRouterLsaOnStartupSeconds;
  }

  public void setMaxMetricRouterLsaOnStartupSeconds(
      int seconds) {

    _maxMetricRouterLsaOnStartupSeconds =
        seconds;
  }

  public void clearMaxMetricRouterLsaOnStartup() {
    _maxMetricRouterLsaOnStartupSeconds =
        null;
  }

  public @Nullable String getDistributeListIn() {
    return _distributeListIn;
  }

  public void setDistributeListIn(String name) {
    _distributeListIn = name;
  }

  public void clearDistributeListIn() {
    _distributeListIn = null;
  }

  public @Nullable String getDistributeListOut() {
    return _distributeListOut;
  }

  public void setDistributeListOut(String name) {
    _distributeListOut = name;
  }

  public void clearDistributeListOut() {
    _distributeListOut = null;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public int getIntraAreaDistance() {
    return _intraAreaDistance;
  }

  public int getInterAreaDistance() {
    return _interAreaDistance;
  }

  public int getExternalDistance() {
    return _externalDistance;
  }

  public void setDistance(int distance) {
    _intraAreaDistance = distance;
    _interAreaDistance = distance;
    _externalDistance = distance;
  }

  public void setIntraAreaDistance(int distance) {
    _intraAreaDistance = distance;
  }

  public void setInterAreaDistance(int distance) {
    _interAreaDistance = distance;
  }

  public void setExternalDistance(int distance) {
    _externalDistance = distance;
  }

  public void resetDistance() {
    setDistance(DEFAULT_ADMIN_DISTANCE);
  }

  public void resetIntraAreaDistance() {
    _intraAreaDistance =
        DEFAULT_ADMIN_DISTANCE;
  }

  public void resetInterAreaDistance() {
    _interAreaDistance =
        DEFAULT_ADMIN_DISTANCE;
  }

  public void resetExternalDistance() {
    _externalDistance =
        DEFAULT_ADMIN_DISTANCE;
  }

  public boolean getRedistributeConnected() {
    return _redistributeConnected;
  }

  public void setRedistributeConnected(
      boolean redistributeConnected) {
    setRedistributeConnected(
        redistributeConnected,
        null);
  }

  public void setRedistributeConnected(
      boolean redistributeConnected,
      @Nullable String routeMap) {
    _redistributeConnected =
        redistributeConnected;
    _redistributeConnectedRouteMap =
        redistributeConnected
            ? routeMap
            : null;
  }

  public @Nullable String
      getRedistributeConnectedRouteMap() {
    return _redistributeConnectedRouteMap;
  }

  public boolean getRedistributeLocalLoopback() {
    return _redistributeLocalLoopback;
  }

  public void setRedistributeLocalLoopback(
      boolean redistributeLocalLoopback) {

    setRedistributeLocalLoopback(
        redistributeLocalLoopback,
        null);
  }

  public void setRedistributeLocalLoopback(
      boolean redistributeLocalLoopback,
      @Nullable String routeMap) {

    _redistributeLocalLoopback =
        redistributeLocalLoopback;

    _redistributeLocalLoopbackRouteMap =
        redistributeLocalLoopback
            ? routeMap
            : null;
  }

  public @Nullable String
      getRedistributeLocalLoopbackRouteMap() {

    return _redistributeLocalLoopbackRouteMap;
  }

  public @Nonnull Set<Integer>
      getRedistributeOspfProcesses() {

    return _redistributeOspfProcesses;
  }

  public @Nonnull Map<Integer, String>
      getRedistributeOspfRouteMaps() {

    return _redistributeOspfRouteMaps;
  }

  public void setRedistributeOspf(
      int sourceProcessId,
      @Nullable String routeMap) {

    _redistributeOspfProcesses.add(
        sourceProcessId);

    if (routeMap == null) {
      _redistributeOspfRouteMaps.remove(
          sourceProcessId);
    } else {
      _redistributeOspfRouteMaps.put(
          sourceProcessId,
          routeMap);
    }
  }

  public void removeRedistributeOspf(
      int sourceProcessId) {

    _redistributeOspfProcesses.remove(
        sourceProcessId);

    _redistributeOspfRouteMaps.remove(
        sourceProcessId);
  }

  public boolean getRedistributeStatic() {
    return _redistributeStatic;
  }

  public void setRedistributeStatic(
      boolean redistributeStatic) {
    setRedistributeStatic(
        redistributeStatic,
        null);
  }

  public void setRedistributeStatic(
      boolean redistributeStatic,
      @Nullable String routeMap) {
    _redistributeStatic =
        redistributeStatic;
    _redistributeStaticRouteMap =
        redistributeStatic
            ? routeMap
            : null;
  }

  public @Nullable String
      getRedistributeStaticRouteMap() {
    return _redistributeStaticRouteMap;
  }

  public long getRedistributionMetric() {
    return _redistributionMetric;
  }

  public void setRedistributionMetric(
      long redistributionMetric) {
    _redistributionMetric =
        redistributionMetric;
  }

  public void resetRedistributionMetric() {
    _redistributionMetric =
        DEFAULT_REDISTRIBUTION_METRIC;
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public boolean getDefaultInformationOriginateAlways() {
    return _defaultInformationOriginateAlways;
  }

  public long getDefaultInformationMetric() {
    return _defaultInformationMetric;
  }

  public void setDefaultInformationOriginate(
      boolean always,
      long metric) {
    _defaultInformationOriginate = true;
    _defaultInformationOriginateAlways = always;
    _defaultInformationMetric = metric;
  }

  public void disableDefaultInformationOriginate() {
    _defaultInformationOriginate = false;
    _defaultInformationOriginateAlways = false;
    _defaultInformationMetric =
        DEFAULT_INFORMATION_METRIC;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public void setPassiveInterfaceDefault(
      boolean passiveInterfaceDefault) {
    _passiveInterfaceDefault =
        passiveInterfaceDefault;
  }

  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public void setReferenceBandwidthMbps(
      long bandwidthMbps) {
    _referenceBandwidth =
        bandwidthMbps * 1_000_000D;
  }

  public void resetReferenceBandwidth() {
    _referenceBandwidth =
        DEFAULT_REFERENCE_BANDWIDTH;
  }

  public @Nonnull Set<String> getAreas() {
    return _areas;
  }

  public void addArea(String area) {
    _areas.add(area);
  }

  /** Change an existing area to normal area type. */
  public void setNormalArea(String area) {
    _areas.add(area);
    _stubAreas.remove(area);
    _nssaAreas.remove(area);
  }

  /**
   * Return stub areas keyed by configured area ID.
   *
   * <p>The Boolean value is true for {@code no-summary}.
   */
  public @Nonnull Map<String, Boolean>
      getStubAreas() {
    return _stubAreas;
  }

  public void setStubArea(
      String area,
      boolean suppressInterArea) {
    _areas.add(area);
    _nssaAreas.remove(area);
    _stubAreas.put(
        area,
        suppressInterArea);
  }

  /** Clear stub type while retaining the area declaration. */
  public void clearStubArea(String area) {
    _areas.add(area);
    _stubAreas.remove(area);
  }

  /** Retain stub status while clearing no-summary. */
  public void clearStubNoSummary(String area) {
    _areas.add(area);

    if (_stubAreas.containsKey(area)) {
      _stubAreas.put(area, false);
    }
  }

  /**
   * Return NSSA areas keyed by configured area ID.
   *
   * <p>The Boolean value is true for {@code no-summary}.
   */
  public @Nonnull Map<String, Boolean>
      getNssaAreas() {
    return _nssaAreas;
  }

  public void setNssaArea(
      String area,
      boolean suppressInterArea) {
    _areas.add(area);
    _stubAreas.remove(area);
    _nssaAreas.put(
        area,
        suppressInterArea);
  }

  /**
   * Clear NSSA type, changing the area back to normal.
   */
  public void clearNssaArea(String area) {
    _areas.add(area);
    _nssaAreas.remove(area);
  }

  /**
   * Retain NSSA status while clearing no-summary.
   */
  public void clearNssaNoSummary(String area) {
    _areas.add(area);

    if (_nssaAreas.containsKey(area)) {
      _nssaAreas.put(area, false);
    }
  }

  public @Nonnull Map<String, Set<Ip>>
      getVirtualLinks() {

    return _virtualLinks;
  }

  public void setVirtualLink(
      String transitArea,
      Ip peerRouterId) {

    _areas.add(transitArea);

    _virtualLinks
        .computeIfAbsent(
            transitArea,
            ignored -> new HashSet<>())
        .add(peerRouterId);
  }

  public @Nullable AosCxOspfv3Authentication
      getVirtualLinkAuthentication(
          String transitArea,
          Ip peerRouterId) {

    Map<Ip, AosCxOspfv3Authentication>
        areaAuthentications =
            _virtualLinkAuthentications.get(
                transitArea);

    return areaAuthentications == null
        ? null
        : areaAuthentications.get(
            peerRouterId);
  }

  public void setVirtualLinkAuthentication(
      String transitArea,
      Ip peerRouterId,
      AosCxOspfv3Authentication authentication) {

    setVirtualLink(
        transitArea,
        peerRouterId);

    _virtualLinkAuthentications
        .computeIfAbsent(
            transitArea,
            ignored -> new HashMap<>())
        .put(
            peerRouterId,
            authentication);
  }

  public void clearVirtualLinkAuthentication(
      String transitArea,
      Ip peerRouterId) {

    Map<Ip, AosCxOspfv3Authentication>
        areaAuthentications =
            _virtualLinkAuthentications.get(
                transitArea);

    if (areaAuthentications == null) {
      return;
    }

    areaAuthentications.remove(
        peerRouterId);

    if (areaAuthentications.isEmpty()) {
      _virtualLinkAuthentications.remove(
          transitArea);
    }
  }

  public @Nullable AosCxOspfv3Encryption
      getVirtualLinkEncryption(
          String transitArea,
          Ip peerRouterId) {

    Map<Ip, AosCxOspfv3Encryption>
        areaEncryptions =
            _virtualLinkEncryptions.get(
                transitArea);

    return areaEncryptions == null
        ? null
        : areaEncryptions.get(
            peerRouterId);
  }

  public void setVirtualLinkEncryption(
      String transitArea,
      Ip peerRouterId,
      AosCxOspfv3Encryption encryption) {

    setVirtualLink(
        transitArea,
        peerRouterId);

    _virtualLinkEncryptions
        .computeIfAbsent(
            transitArea,
            ignored -> new HashMap<>())
        .put(
            peerRouterId,
            encryption);
  }

  public void clearVirtualLinkEncryption(
      String transitArea,
      Ip peerRouterId) {

    Map<Ip, AosCxOspfv3Encryption>
        areaEncryptions =
            _virtualLinkEncryptions.get(
                transitArea);

    if (areaEncryptions == null) {
      return;
    }

    areaEncryptions.remove(
        peerRouterId);

    if (areaEncryptions.isEmpty()) {
      _virtualLinkEncryptions.remove(
          transitArea);
    }
  }

  public void removeVirtualLink(
      String transitArea,
      Ip peerRouterId) {

    Set<Ip> peers =
        _virtualLinks.get(transitArea);

    if (peers != null) {

      peers.remove(peerRouterId);

      if (peers.isEmpty()) {
        _virtualLinks.remove(
            transitArea);
      }
    }

    clearVirtualLinkAuthentication(
        transitArea,
        peerRouterId);

    clearVirtualLinkEncryption(
        transitArea,
        peerRouterId);
  }

  public @Nonnull
      Map<String, AosCxOspfv3Authentication>
      getAreaAuthentications() {

    return _areaAuthentications;
  }

  public void setAreaAuthentication(
      String area,
      AosCxOspfv3Authentication authentication) {

    _areas.add(area);

    _areaAuthentications.put(
        area,
        authentication);
  }

  public void clearAreaAuthentication(
      String area) {

    _areas.add(area);

    _areaAuthentications.remove(
        area);
  }

  public @Nonnull
      Map<String, AosCxOspfv3Encryption>
      getAreaEncryptions() {

    return _areaEncryptions;
  }

  public void setAreaEncryption(
      String area,
      AosCxOspfv3Encryption encryption) {

    _areas.add(area);

    _areaEncryptions.put(
        area,
        encryption);
  }

  public void clearAreaEncryption(
      String area) {

    _areas.add(area);

    _areaEncryptions.remove(
        area);
  }

  public @Nonnull Map<String, Long>
      getAreaDefaultMetrics() {
    return _areaDefaultMetrics;
  }

  public void setAreaDefaultMetric(
      String area,
      long metric) {
    _areas.add(area);
    _areaDefaultMetrics.put(area, metric);
  }

  public void clearAreaDefaultMetric(
      String area) {
    _areas.add(area);
    _areaDefaultMetrics.remove(area);
  }

  /**
   * Inter-area aggregation ranges keyed first by configured area ID,
   * then by IPv6 prefix. The Boolean value is true when the range
   * should be advertised.
   */
  public @Nonnull Map<String, Map<Prefix6, Boolean>>
      getInterAreaRanges() {
    return _interAreaRanges;
  }

  public void setInterAreaRange(
      String area,
      Prefix6 prefix,
      boolean advertise) {

    _areas.add(area);

    _interAreaRanges
        .computeIfAbsent(
            area,
            ignored ->
                new HashMap<>())
        .put(
            prefix,
            advertise);
  }

  public void removeInterAreaRange(
      String area,
      Prefix6 prefix) {

    Map<Prefix6, Boolean> ranges =
        _interAreaRanges.get(area);

    if (ranges == null) {
      return;
    }

    ranges.remove(prefix);

    if (ranges.isEmpty()) {
      _interAreaRanges.remove(area);
    }
  }

  /**
   * Implements:
   *
   * <pre>
   * no area ... range ... type inter-area no-advertise
   * </pre>
   *
   * by retaining an existing range and restoring advertisement.
   */
  public void enableInterAreaRangeAdvertisement(
      String area,
      Prefix6 prefix) {

    Map<Prefix6, Boolean> ranges =
        _interAreaRanges.get(area);

    if (ranges != null
        && ranges.containsKey(prefix)) {
      ranges.put(
          prefix,
          true);
    }
  }

  /**
   * NSSA Type-7 aggregation ranges keyed first by configured area ID,
   * then by IPv6 prefix. The Boolean value is true when the translated
   * aggregate should be advertised.
   */
  public @Nonnull Map<String, Map<Prefix6, Boolean>>
      getNssaRanges() {
    return _nssaRanges;
  }

  public void setNssaRange(
      String area,
      Prefix6 prefix,
      boolean advertise) {

    _areas.add(area);

    _nssaRanges
        .computeIfAbsent(
            area,
            ignored ->
                new HashMap<>())
        .put(
            prefix,
            advertise);
  }

  public void removeNssaRange(
      String area,
      Prefix6 prefix) {

    Map<Prefix6, Boolean> ranges =
        _nssaRanges.get(area);

    if (ranges == null) {
      return;
    }

    ranges.remove(prefix);

    if (ranges.isEmpty()) {
      _nssaRanges.remove(area);
    }
  }

  /**
   * Implements:
   *
   * <pre>
   * no area ... range ... type nssa no-advertise
   * </pre>
   *
   * by retaining an existing range and restoring advertisement.
   */
  public void enableNssaRangeAdvertisement(
      String area,
      Prefix6 prefix) {

    Map<Prefix6, Boolean> ranges =
        _nssaRanges.get(area);

    if (ranges != null
        && ranges.containsKey(prefix)) {

      ranges.put(
          prefix,
          true);
    }
  }

  public @Nonnull
      Map<Prefix6, AosCxOspfv3ExternalSummary>
      getExternalSummaries() {

    return _externalSummaries;
  }

  public void setExternalSummary(
      Prefix6 prefix,
      boolean advertise,
      @Nullable Long tag) {

    _externalSummaries.put(
        prefix,
        new AosCxOspfv3ExternalSummary(
            prefix,
            advertise,
            tag));
  }

  public void removeExternalSummary(
      Prefix6 prefix) {

    _externalSummaries.remove(prefix);
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  private final int _processId;

  private @Nullable String _distributeListIn;
  private @Nullable String _distributeListOut;

  private boolean
      _activeBackboneStubDefaultRoute =
          true;

  private int _gracefulRestartIntervalSeconds =
      DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS;

  private boolean _gracefulRestartHelper;

  private boolean
      _gracefulRestartHelperStrictLsaCheck;

  private boolean
      _gracefulRestartIgnoreLostInterface;

  private int _maximumPaths =
      DEFAULT_MAXIMUM_PATHS;

  private boolean _maxMetricRouterLsa;

  private @Nullable Integer
      _maxMetricRouterLsaOnStartupSeconds;

  private boolean _enabled = true;
  private int _intraAreaDistance =
      DEFAULT_ADMIN_DISTANCE;
  private int _interAreaDistance =
      DEFAULT_ADMIN_DISTANCE;
  private int _externalDistance =
      DEFAULT_ADMIN_DISTANCE;

  private boolean _defaultInformationOriginate;
  private boolean _defaultInformationOriginateAlways;
  private long _defaultInformationMetric =
      DEFAULT_INFORMATION_METRIC;

  private boolean _redistributeConnected;
  private @Nullable String
      _redistributeConnectedRouteMap;
  private boolean _redistributeLocalLoopback;
  private @Nullable String
      _redistributeLocalLoopbackRouteMap;

  private final @Nonnull Set<Integer>
      _redistributeOspfProcesses =
          new HashSet<>();

  private final @Nonnull Map<Integer, String>
      _redistributeOspfRouteMaps =
          new HashMap<>();

  private boolean _redistributeStatic;
  private @Nullable String
      _redistributeStaticRouteMap;
  private long _redistributionMetric =
      DEFAULT_REDISTRIBUTION_METRIC;

  private boolean _bfdAllInterfaces;

  private boolean _passiveInterfaceDefault;

  private double _referenceBandwidth =
      DEFAULT_REFERENCE_BANDWIDTH;

  private final @Nonnull Set<String> _areas =
      new HashSet<>();

  private final @Nonnull Map<String, Boolean>
      _stubAreas = new HashMap<>();

  private final @Nonnull Map<String, Boolean>
      _nssaAreas = new HashMap<>();

  private final @Nonnull
      Map<String, AosCxOspfv3Authentication>
          _areaAuthentications =
              new HashMap<>();

  private final @Nonnull
      Map<String, AosCxOspfv3Encryption>
          _areaEncryptions =
              new HashMap<>();

  private final @Nonnull Map<String, Long>
      _areaDefaultMetrics = new HashMap<>();

  private final @Nonnull Map<String, Set<Ip>>
      _virtualLinks = new HashMap<>();

  private final @Nonnull
      Map<String, Map<Ip, AosCxOspfv3Authentication>>
          _virtualLinkAuthentications =
              new HashMap<>();

  private final @Nonnull
      Map<String, Map<Ip, AosCxOspfv3Encryption>>
          _virtualLinkEncryptions =
              new HashMap<>();

  private final @Nonnull
      Map<String, Map<Prefix6, Boolean>>
          _interAreaRanges =
              new HashMap<>();

  private final @Nonnull
      Map<String, Map<Prefix6, Boolean>>
          _nssaRanges =
              new HashMap<>();

  private final @Nonnull
      Map<Prefix6, AosCxOspfv3ExternalSummary>
          _externalSummaries =
              new HashMap<>();

  private @Nullable Ip _routerId;
}
