package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.Vrf;

/** An OSPFv3 routing process. */
@ParametersAreNonnullByDefault
public final class Ospfv3Process
    implements Serializable {

  public static final int DEFAULT_ADMIN_COST =
      110;

  /** AOS-CX OSPFv3 default reference bandwidth: 100000 Mbps. */
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100_000_000_000D;

  /** AOS-CX default metric for redistributed OSPFv3 routes. */
  public static final long DEFAULT_REDISTRIBUTION_METRIC =
      25L;

  /** AOS-CX default metric for default-information origination. */
  public static final long DEFAULT_INFORMATION_METRIC =
      1L;

  /** Largest usable OSPF metric; LSInfinity itself is not usable. */
  public static final long MAX_METRIC =
      0xFFFFFEL;

  /** AOS-CX default number of OSPFv3 ECMP paths. */
  public static final int DEFAULT_MAXIMUM_PATHS =
      4;

  /** AOS-CX default graceful-restart restart interval in seconds. */
  public static final int
      DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS =
          120;

  /** Default initial SPF throttle delay in milliseconds. */
  public static final int
      DEFAULT_SPF_THROTTLE_START_TIME_MS =
          200;

  /** Default SPF throttle hold time in milliseconds. */
  public static final int
      DEFAULT_SPF_THROTTLE_HOLD_TIME_MS =
          1000;

  /** Default SPF throttle maximum wait in milliseconds. */
  public static final int
      DEFAULT_SPF_THROTTLE_MAX_WAIT_TIME_MS =
          5000;

  /** Default initial LSA generation delay in milliseconds. */
  public static final int
      DEFAULT_LSA_THROTTLE_START_TIME_MS =
          5000;

  /** Default LSA regeneration hold time in milliseconds. */
  public static final int
      DEFAULT_LSA_THROTTLE_HOLD_TIME_MS =
          0;

  /** Default LSA regeneration maximum wait in milliseconds. */
  public static final int
      DEFAULT_LSA_THROTTLE_MAX_WAIT_TIME_MS =
          0;

  /** Default minimum arrival delay for the same LSA in milliseconds. */
  public static final int
      DEFAULT_LSA_ARRIVAL_TIME_MS =
          1000;

  public static final class Builder {
    private int _adminCost;
    private int _interAreaAdminCost;
    private int _externalAdminCost;
    private boolean _enabled;
    private boolean
        _activeBackboneStubDefaultRoute;
    private int
        _gracefulRestartIntervalSeconds;
    private boolean
        _gracefulRestartHelper;
    private boolean
        _gracefulRestartHelperStrictLsaCheck;
    private boolean
        _gracefulRestartIgnoreLostInterface;
    private int
        _spfThrottleStartTimeMs;
    private int
        _spfThrottleHoldTimeMs;
    private int
        _spfThrottleMaxWaitTimeMs;
    private int
        _lsaThrottleStartTimeMs;
    private int
        _lsaThrottleHoldTimeMs;
    private int
        _lsaThrottleMaxWaitTimeMs;
    private int
        _lsaArrivalTimeMs;
    private @Nullable PrefixList6
        _inboundDistributeList;
    private @Nullable PrefixList6
        _outboundDistributeList;
    private int _maximumPaths;
    private boolean _maxMetricRouterLsa;
    private @Nullable Integer
        _maxMetricRouterLsaOnStartupSeconds;
    private @Nonnull
        Set<Ospfv3ExternalSummary> _externalSummaries;
    private @Nonnull
        Set<Ospfv3VirtualLink> _virtualLinks;
    private @Nonnull Map<Long, Ospfv3Area> _areas;
    private boolean _defaultInformationOriginate;
    private boolean _defaultInformationOriginateAlways;
    private long _defaultInformationMetric;
    private @Nullable String _processId;
    private double _referenceBandwidth;
    private boolean
        _redistributeActiveRoutesOnly;
    private boolean _redistributeConnected;
    private @Nullable RouteMap6
        _redistributeConnectedRouteMap;
    private boolean _redistributeLocalLoopback;
    private @Nullable RouteMap6
        _redistributeLocalLoopbackRouteMap;
    private @Nonnull Set<String>
        _redistributeOspfProcesses;
    private @Nonnull Map<String, RouteMap6>
        _redistributeOspfRouteMaps;
    private boolean _redistributeStatic;
    private @Nullable RouteMap6
        _redistributeStaticRouteMap;
    private long _redistributionMetric;
    private @Nullable Ip _routerId;
    private @Nullable Vrf _vrf;

    private Builder() {
      _adminCost =
          DEFAULT_ADMIN_COST;
      _interAreaAdminCost =
          DEFAULT_ADMIN_COST;
      _externalAdminCost =
          DEFAULT_ADMIN_COST;
      _enabled = true;
      _activeBackboneStubDefaultRoute =
          true;
      _gracefulRestartIntervalSeconds =
          DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS;
      _spfThrottleStartTimeMs =
          DEFAULT_SPF_THROTTLE_START_TIME_MS;
      _spfThrottleHoldTimeMs =
          DEFAULT_SPF_THROTTLE_HOLD_TIME_MS;
      _spfThrottleMaxWaitTimeMs =
          DEFAULT_SPF_THROTTLE_MAX_WAIT_TIME_MS;
      _lsaThrottleStartTimeMs =
          DEFAULT_LSA_THROTTLE_START_TIME_MS;
      _lsaThrottleHoldTimeMs =
          DEFAULT_LSA_THROTTLE_HOLD_TIME_MS;
      _lsaThrottleMaxWaitTimeMs =
          DEFAULT_LSA_THROTTLE_MAX_WAIT_TIME_MS;
      _lsaArrivalTimeMs =
          DEFAULT_LSA_ARRIVAL_TIME_MS;
      _maximumPaths =
          DEFAULT_MAXIMUM_PATHS;
      _externalSummaries =
          ImmutableSet.of();
      _virtualLinks =
          ImmutableSet.of();
      _areas =
          ImmutableMap.of();
      _defaultInformationMetric =
          DEFAULT_INFORMATION_METRIC;
      _referenceBandwidth =
          DEFAULT_REFERENCE_BANDWIDTH;
      _redistributeOspfProcesses =
          ImmutableSet.of();
      _redistributeOspfRouteMaps =
          ImmutableMap.of();
      _redistributionMetric =
          DEFAULT_REDISTRIBUTION_METRIC;
    }

    public Ospfv3Process build() {
      checkArgument(
          _processId != null,
          "Missing processId");

      checkArgument(
          _routerId != null,
          "Missing routerId");

      checkArgument(
          _adminCost >= 0,
          "Invalid intra-area admin cost %s",
          _adminCost);

      checkArgument(
          _interAreaAdminCost >= 0,
          "Invalid inter-area admin cost %s",
          _interAreaAdminCost);

      checkArgument(
          _externalAdminCost >= 0,
          "Invalid external admin cost %s",
          _externalAdminCost);

      checkArgument(
          _referenceBandwidth > 0,
          "Invalid reference bandwidth %s",
          _referenceBandwidth);

      checkArgument(
          _maximumPaths >= 1
              && _maximumPaths <= 32,
          "Invalid OSPFv3 maximum paths %s",
          _maximumPaths);

      checkArgument(
          _gracefulRestartIntervalSeconds >= 5
              && _gracefulRestartIntervalSeconds <= 1800,
          "Invalid OSPFv3 graceful-restart interval %s",
          _gracefulRestartIntervalSeconds);

      checkArgument(
          !_gracefulRestartHelperStrictLsaCheck
              || _gracefulRestartHelper,
          "OSPFv3 strict-lsa-check requires graceful-restart helper mode");

      checkArgument(
          _spfThrottleStartTimeMs >= 1
              && _spfThrottleStartTimeMs <= 600000
              && _spfThrottleHoldTimeMs >= 1
              && _spfThrottleHoldTimeMs <= 600000
              && _spfThrottleMaxWaitTimeMs >= 1
              && _spfThrottleMaxWaitTimeMs <= 600000,
          "Invalid OSPFv3 SPF throttle timers %s/%s/%s",
          _spfThrottleStartTimeMs,
          _spfThrottleHoldTimeMs,
          _spfThrottleMaxWaitTimeMs);

      checkArgument(
          _lsaThrottleStartTimeMs >= 0
              && _lsaThrottleStartTimeMs <= 600000
              && _lsaThrottleHoldTimeMs >= 0
              && _lsaThrottleHoldTimeMs <= 600000
              && _lsaThrottleMaxWaitTimeMs >= 0
              && _lsaThrottleMaxWaitTimeMs <= 600000,
          "Invalid OSPFv3 LSA throttle timers %s/%s/%s",
          _lsaThrottleStartTimeMs,
          _lsaThrottleHoldTimeMs,
          _lsaThrottleMaxWaitTimeMs);

      checkArgument(
          _lsaArrivalTimeMs >= 0
              && _lsaArrivalTimeMs <= 600000,
          "Invalid OSPFv3 LSA arrival timer %s",
          _lsaArrivalTimeMs);

      checkArgument(
          _redistributionMetric >= 0
              && _redistributionMetric <= MAX_METRIC,
          "Invalid redistribution metric %s",
          _redistributionMetric);

      checkArgument(
          _defaultInformationMetric >= 1
              && _defaultInformationMetric <= MAX_METRIC,
          "Invalid default-information metric %s",
          _defaultInformationMetric);

      Ospfv3Process process =
          new Ospfv3Process(
              _processId,
              _routerId,
              _areas,
              _adminCost,
              _interAreaAdminCost,
              _externalAdminCost,
              _enabled,
              _activeBackboneStubDefaultRoute,
              _gracefulRestartIntervalSeconds,
              _gracefulRestartHelper,
              _gracefulRestartHelperStrictLsaCheck,
              _gracefulRestartIgnoreLostInterface,
              _spfThrottleStartTimeMs,
              _spfThrottleHoldTimeMs,
              _spfThrottleMaxWaitTimeMs,
              _lsaThrottleStartTimeMs,
              _lsaThrottleHoldTimeMs,
              _lsaThrottleMaxWaitTimeMs,
              _lsaArrivalTimeMs,
              _inboundDistributeList,
              _outboundDistributeList,
              _maximumPaths,
              _maxMetricRouterLsa,
              _maxMetricRouterLsaOnStartupSeconds,
              _externalSummaries,
              _virtualLinks,
              _referenceBandwidth,
              _redistributeActiveRoutesOnly,
              _redistributeConnected,
              _redistributeLocalLoopback,
              _redistributeOspfProcesses,
              _redistributeOspfRouteMaps,
              _redistributeStatic,
              _redistributeConnectedRouteMap,
              _redistributeLocalLoopbackRouteMap,
              _redistributeStaticRouteMap,
              _redistributionMetric,
              _defaultInformationOriginate
                  || _defaultInformationOriginateAlways,
              _defaultInformationOriginateAlways,
              _defaultInformationMetric);

      if (_vrf != null) {
        _vrf.addOspfv3Process(process);
      }

      return process;
    }

    /**
     * Set one administrative distance for all OSPFv3 route types.
     */
    public Builder setAdminCost(
        int adminCost) {
      _adminCost = adminCost;
      _interAreaAdminCost = adminCost;
      _externalAdminCost = adminCost;
      return this;
    }

    public Builder setIntraAreaAdminCost(
        int adminCost) {
      _adminCost = adminCost;
      return this;
    }

    public Builder setInterAreaAdminCost(
        int adminCost) {
      _interAreaAdminCost = adminCost;
      return this;
    }

    public Builder setExternalAdminCost(
        int adminCost) {
      _externalAdminCost = adminCost;
      return this;
    }

    public Builder setEnabled(boolean enabled) {
      _enabled = enabled;
      return this;
    }

    public Builder setActiveBackboneStubDefaultRoute(
        boolean activeBackboneStubDefaultRoute) {

      _activeBackboneStubDefaultRoute =
          activeBackboneStubDefaultRoute;

      return this;
    }

    public Builder setGracefulRestartIntervalSeconds(
        int seconds) {

      _gracefulRestartIntervalSeconds =
          seconds;

      return this;
    }

    public Builder setGracefulRestartHelper(
        boolean helper) {

      _gracefulRestartHelper =
          helper;

      if (!helper) {
        _gracefulRestartHelperStrictLsaCheck =
            false;
      }

      return this;
    }

    public Builder
        setGracefulRestartHelperStrictLsaCheck(
            boolean strictLsaCheck) {

      _gracefulRestartHelperStrictLsaCheck =
          strictLsaCheck;

      return this;
    }

    public Builder
        setGracefulRestartIgnoreLostInterface(
            boolean ignoreLostInterface) {

      _gracefulRestartIgnoreLostInterface =
          ignoreLostInterface;

      return this;
    }

    public Builder setSpfThrottleTimers(
        int startTimeMs,
        int holdTimeMs,
        int maxWaitTimeMs) {

      _spfThrottleStartTimeMs =
          startTimeMs;

      _spfThrottleHoldTimeMs =
          holdTimeMs;

      _spfThrottleMaxWaitTimeMs =
          maxWaitTimeMs;

      return this;
    }

    public Builder setLsaThrottleTimers(
        int startTimeMs,
        int holdTimeMs,
        int maxWaitTimeMs) {

      _lsaThrottleStartTimeMs =
          startTimeMs;

      _lsaThrottleHoldTimeMs =
          holdTimeMs;

      _lsaThrottleMaxWaitTimeMs =
          maxWaitTimeMs;

      return this;
    }

    public Builder setLsaArrivalTimeMs(
        int delayMs) {

      _lsaArrivalTimeMs =
          delayMs;

      return this;
    }

    public Builder setInboundDistributeList(
        @Nullable PrefixList6 prefixList) {
      _inboundDistributeList =
          prefixList;
      return this;
    }

    public Builder setOutboundDistributeList(
        @Nullable PrefixList6 prefixList) {
      _outboundDistributeList =
          prefixList;
      return this;
    }

    public Builder setMaximumPaths(
        int maximumPaths) {
      _maximumPaths =
          maximumPaths;
      return this;
    }

    public Builder setMaxMetricRouterLsa(
        boolean maxMetricRouterLsa) {

      _maxMetricRouterLsa =
          maxMetricRouterLsa;

      return this;
    }

    public Builder
        setMaxMetricRouterLsaOnStartupSeconds(
            @Nullable Integer seconds) {

      _maxMetricRouterLsaOnStartupSeconds =
          seconds;

      return this;
    }

    public Builder setExternalSummaries(
        Set<Ospfv3ExternalSummary> externalSummaries) {

      _externalSummaries =
          externalSummaries;

      return this;
    }

    public Builder setVirtualLinks(
        Set<Ospfv3VirtualLink> virtualLinks) {

      _virtualLinks =
          virtualLinks;

      return this;
    }

    public Builder setProcessId(
        String processId) {
      _processId = processId;
      return this;
    }

    public Builder setRouterId(
        Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public Builder setAreas(
        Map<Long, Ospfv3Area> areas) {
      _areas = areas;
      return this;
    }

    public Builder setReferenceBandwidth(
        double referenceBandwidth) {
      _referenceBandwidth =
          referenceBandwidth;
      return this;
    }

    public Builder setRedistributeActiveRoutesOnly(
        boolean redistributeActiveRoutesOnly) {

      _redistributeActiveRoutesOnly =
          redistributeActiveRoutesOnly;

      return this;
    }

    public Builder setRedistributeConnected(
        boolean redistributeConnected) {
      _redistributeConnected =
          redistributeConnected;
      return this;
    }

    public Builder setRedistributeLocalLoopback(
        boolean redistributeLocalLoopback) {

      _redistributeLocalLoopback =
          redistributeLocalLoopback;

      return this;
    }

    public Builder setRedistributeOspfProcesses(
        Set<String> processIds) {

      _redistributeOspfProcesses =
          processIds;

      return this;
    }

    public Builder setRedistributeOspfRouteMaps(
        Map<String, RouteMap6> routeMaps) {

      _redistributeOspfRouteMaps =
          routeMaps;

      return this;
    }

    public Builder setRedistributeStatic(
        boolean redistributeStatic) {
      _redistributeStatic =
          redistributeStatic;
      return this;
    }

    public Builder setRedistributeConnectedRouteMap(
        @Nullable RouteMap6 routeMap) {
      _redistributeConnectedRouteMap =
          routeMap;
      return this;
    }

    public Builder setRedistributeLocalLoopbackRouteMap(
        @Nullable RouteMap6 routeMap) {

      _redistributeLocalLoopbackRouteMap =
          routeMap;

      return this;
    }

    public Builder setRedistributeStaticRouteMap(
        @Nullable RouteMap6 routeMap) {
      _redistributeStaticRouteMap =
          routeMap;
      return this;
    }

    public Builder setRedistributionMetric(
        long redistributionMetric) {
      _redistributionMetric =
          redistributionMetric;
      return this;
    }

    public Builder setDefaultInformationOriginate(
        boolean defaultInformationOriginate) {
      _defaultInformationOriginate =
          defaultInformationOriginate;
      return this;
    }

    public Builder setDefaultInformationOriginateAlways(
        boolean defaultInformationOriginateAlways) {
      _defaultInformationOriginateAlways =
          defaultInformationOriginateAlways;

      if (defaultInformationOriginateAlways) {
        _defaultInformationOriginate = true;
      }

      return this;
    }

    public Builder setDefaultInformationMetric(
        long defaultInformationMetric) {
      _defaultInformationMetric =
          defaultInformationMetric;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }
  }

  private static final String PROP_ADMIN_COST =
      "adminCost";
  private static final String PROP_INTER_AREA_ADMIN_COST =
      "interAreaAdminCost";
  private static final String PROP_EXTERNAL_ADMIN_COST =
      "externalAdminCost";
  private static final String PROP_ENABLED =
      "enabled";
  private static final String
      PROP_ACTIVE_BACKBONE_STUB_DEFAULT_ROUTE =
          "activeBackboneStubDefaultRoute";
  private static final String
      PROP_GRACEFUL_RESTART_INTERVAL_SECONDS =
          "gracefulRestartIntervalSeconds";
  private static final String
      PROP_GRACEFUL_RESTART_HELPER =
          "gracefulRestartHelper";
  private static final String
      PROP_GRACEFUL_RESTART_HELPER_STRICT_LSA_CHECK =
          "gracefulRestartHelperStrictLsaCheck";
  private static final String
      PROP_GRACEFUL_RESTART_IGNORE_LOST_INTERFACE =
          "gracefulRestartIgnoreLostInterface";
  private static final String
      PROP_SPF_THROTTLE_START_TIME_MS =
          "spfThrottleStartTimeMs";
  private static final String
      PROP_SPF_THROTTLE_HOLD_TIME_MS =
          "spfThrottleHoldTimeMs";
  private static final String
      PROP_SPF_THROTTLE_MAX_WAIT_TIME_MS =
          "spfThrottleMaxWaitTimeMs";
  private static final String
      PROP_LSA_THROTTLE_START_TIME_MS =
          "lsaThrottleStartTimeMs";
  private static final String
      PROP_LSA_THROTTLE_HOLD_TIME_MS =
          "lsaThrottleHoldTimeMs";
  private static final String
      PROP_LSA_THROTTLE_MAX_WAIT_TIME_MS =
          "lsaThrottleMaxWaitTimeMs";
  private static final String
      PROP_LSA_ARRIVAL_TIME_MS =
          "lsaArrivalTimeMs";
  private static final String
      PROP_INBOUND_DISTRIBUTE_LIST =
          "inboundDistributeList";
  private static final String
      PROP_OUTBOUND_DISTRIBUTE_LIST =
          "outboundDistributeList";
  private static final String PROP_MAXIMUM_PATHS =
      "maximumPaths";
  private static final String PROP_MAX_METRIC_ROUTER_LSA =
      "maxMetricRouterLsa";
  private static final String
      PROP_MAX_METRIC_ROUTER_LSA_ON_STARTUP_SECONDS =
          "maxMetricRouterLsaOnStartupSeconds";
  private static final String PROP_EXTERNAL_SUMMARIES =
      "externalSummaries";
  private static final String PROP_VIRTUAL_LINKS =
      "virtualLinks";
  private static final String PROP_AREAS =
      "areas";
  private static final String
      PROP_DEFAULT_INFORMATION_METRIC =
          "defaultInformationMetric";
  private static final String
      PROP_DEFAULT_INFORMATION_ORIGINATE =
          "defaultInformationOriginate";
  private static final String
      PROP_DEFAULT_INFORMATION_ORIGINATE_ALWAYS =
          "defaultInformationOriginateAlways";
  private static final String PROP_PROCESS_ID =
      "processId";
  private static final String PROP_REFERENCE_BANDWIDTH =
      "referenceBandwidth";
  private static final String
      PROP_REDISTRIBUTE_ACTIVE_ROUTES_ONLY =
          "redistributeActiveRoutesOnly";
  private static final String PROP_REDISTRIBUTE_CONNECTED =
      "redistributeConnected";
  private static final String
      PROP_REDISTRIBUTE_CONNECTED_ROUTE_MAP =
          "redistributeConnectedRouteMap";
  private static final String
      PROP_REDISTRIBUTE_LOCAL_LOOPBACK =
          "redistributeLocalLoopback";
  private static final String
      PROP_REDISTRIBUTE_LOCAL_LOOPBACK_ROUTE_MAP =
          "redistributeLocalLoopbackRouteMap";
  private static final String
      PROP_REDISTRIBUTE_OSPF_PROCESSES =
          "redistributeOspfProcesses";
  private static final String
      PROP_REDISTRIBUTE_OSPF_ROUTE_MAPS =
          "redistributeOspfRouteMaps";
  private static final String PROP_REDISTRIBUTE_STATIC =
      "redistributeStatic";
  private static final String
      PROP_REDISTRIBUTE_STATIC_ROUTE_MAP =
          "redistributeStaticRouteMap";
  private static final String PROP_REDISTRIBUTION_METRIC =
      "redistributionMetric";
  private static final String PROP_ROUTER_ID =
      "routerId";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static Ospfv3Process create(
      @JsonProperty(PROP_PROCESS_ID)
          @Nullable String processId,
      @JsonProperty(PROP_ROUTER_ID)
          @Nullable Ip routerId,
      @JsonProperty(PROP_AREAS)
          @Nullable Map<Long, Ospfv3Area> areas,
      @JsonProperty(PROP_ADMIN_COST)
          @Nullable Integer adminCost,
      @JsonProperty(PROP_INTER_AREA_ADMIN_COST)
          @Nullable Integer interAreaAdminCost,
      @JsonProperty(PROP_EXTERNAL_ADMIN_COST)
          @Nullable Integer externalAdminCost,
      @JsonProperty(PROP_ENABLED)
          @Nullable Boolean enabled,
      @JsonProperty(
              PROP_ACTIVE_BACKBONE_STUB_DEFAULT_ROUTE)
          @Nullable Boolean activeBackboneStubDefaultRoute,
      @JsonProperty(
              PROP_GRACEFUL_RESTART_INTERVAL_SECONDS)
          @Nullable Integer gracefulRestartIntervalSeconds,
      @JsonProperty(PROP_GRACEFUL_RESTART_HELPER)
          @Nullable Boolean gracefulRestartHelper,
      @JsonProperty(
              PROP_GRACEFUL_RESTART_HELPER_STRICT_LSA_CHECK)
          @Nullable Boolean gracefulRestartHelperStrictLsaCheck,
      @JsonProperty(
              PROP_GRACEFUL_RESTART_IGNORE_LOST_INTERFACE)
          @Nullable Boolean gracefulRestartIgnoreLostInterface,
      @JsonProperty(PROP_SPF_THROTTLE_START_TIME_MS)
          @Nullable Integer spfThrottleStartTimeMs,
      @JsonProperty(PROP_SPF_THROTTLE_HOLD_TIME_MS)
          @Nullable Integer spfThrottleHoldTimeMs,
      @JsonProperty(PROP_SPF_THROTTLE_MAX_WAIT_TIME_MS)
          @Nullable Integer spfThrottleMaxWaitTimeMs,
      @JsonProperty(PROP_LSA_THROTTLE_START_TIME_MS)
          @Nullable Integer lsaThrottleStartTimeMs,
      @JsonProperty(PROP_LSA_THROTTLE_HOLD_TIME_MS)
          @Nullable Integer lsaThrottleHoldTimeMs,
      @JsonProperty(PROP_LSA_THROTTLE_MAX_WAIT_TIME_MS)
          @Nullable Integer lsaThrottleMaxWaitTimeMs,
      @JsonProperty(PROP_LSA_ARRIVAL_TIME_MS)
          @Nullable Integer lsaArrivalTimeMs,
      @JsonProperty(PROP_INBOUND_DISTRIBUTE_LIST)
          @Nullable PrefixList6 inboundDistributeList,
      @JsonProperty(PROP_OUTBOUND_DISTRIBUTE_LIST)
          @Nullable PrefixList6 outboundDistributeList,
      @JsonProperty(PROP_MAXIMUM_PATHS)
          @Nullable Integer maximumPaths,
      @JsonProperty(PROP_MAX_METRIC_ROUTER_LSA)
          @Nullable Boolean maxMetricRouterLsa,
      @JsonProperty(
              PROP_MAX_METRIC_ROUTER_LSA_ON_STARTUP_SECONDS)
          @Nullable Integer maxMetricRouterLsaOnStartupSeconds,
      @JsonProperty(PROP_EXTERNAL_SUMMARIES)
          @Nullable Set<Ospfv3ExternalSummary> externalSummaries,
      @JsonProperty(PROP_VIRTUAL_LINKS)
          @Nullable Set<Ospfv3VirtualLink> virtualLinks,
      @JsonProperty(PROP_REFERENCE_BANDWIDTH)
          @Nullable Double referenceBandwidth,
      @JsonProperty(PROP_REDISTRIBUTE_ACTIVE_ROUTES_ONLY)
          @Nullable Boolean redistributeActiveRoutesOnly,
      @JsonProperty(PROP_REDISTRIBUTE_CONNECTED)
          @Nullable Boolean redistributeConnected,
      @JsonProperty(PROP_REDISTRIBUTE_LOCAL_LOOPBACK)
          @Nullable Boolean redistributeLocalLoopback,
      @JsonProperty(PROP_REDISTRIBUTE_OSPF_PROCESSES)
          @Nullable Set<String> redistributeOspfProcesses,
      @JsonProperty(PROP_REDISTRIBUTE_OSPF_ROUTE_MAPS)
          @Nullable Map<String, RouteMap6> redistributeOspfRouteMaps,
      @JsonProperty(PROP_REDISTRIBUTE_STATIC)
          @Nullable Boolean redistributeStatic,
      @JsonProperty(PROP_REDISTRIBUTE_CONNECTED_ROUTE_MAP)
          @Nullable RouteMap6 redistributeConnectedRouteMap,
      @JsonProperty(PROP_REDISTRIBUTE_LOCAL_LOOPBACK_ROUTE_MAP)
          @Nullable RouteMap6 redistributeLocalLoopbackRouteMap,
      @JsonProperty(PROP_REDISTRIBUTE_STATIC_ROUTE_MAP)
          @Nullable RouteMap6 redistributeStaticRouteMap,
      @JsonProperty(PROP_REDISTRIBUTION_METRIC)
          @Nullable Long redistributionMetric,
      @JsonProperty(PROP_DEFAULT_INFORMATION_ORIGINATE)
          @Nullable Boolean defaultInformationOriginate,
      @JsonProperty(PROP_DEFAULT_INFORMATION_ORIGINATE_ALWAYS)
          @Nullable Boolean defaultInformationOriginateAlways,
      @JsonProperty(PROP_DEFAULT_INFORMATION_METRIC)
          @Nullable Long defaultInformationMetric) {

    checkArgument(
        processId != null,
        "Missing %s",
        PROP_PROCESS_ID);

    checkArgument(
        routerId != null,
        "Missing %s",
        PROP_ROUTER_ID);

    boolean always =
        firstNonNull(
            defaultInformationOriginateAlways,
            false);

    int effectiveIntraAreaAdminCost =
        firstNonNull(
            adminCost,
            DEFAULT_ADMIN_COST);

    return new Ospfv3Process(
        processId,
        routerId,
        firstNonNull(
            areas,
            ImmutableMap.of()),
        effectiveIntraAreaAdminCost,
        firstNonNull(
            interAreaAdminCost,
            effectiveIntraAreaAdminCost),
        firstNonNull(
            externalAdminCost,
            effectiveIntraAreaAdminCost),
        firstNonNull(
            enabled,
            true),
        firstNonNull(
            activeBackboneStubDefaultRoute,
            true),
        firstNonNull(
            gracefulRestartIntervalSeconds,
            DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS),
        firstNonNull(
            gracefulRestartHelper,
            false),
        firstNonNull(
            gracefulRestartHelperStrictLsaCheck,
            false),
        firstNonNull(
            gracefulRestartIgnoreLostInterface,
            false),
        firstNonNull(
            spfThrottleStartTimeMs,
            DEFAULT_SPF_THROTTLE_START_TIME_MS),
        firstNonNull(
            spfThrottleHoldTimeMs,
            DEFAULT_SPF_THROTTLE_HOLD_TIME_MS),
        firstNonNull(
            spfThrottleMaxWaitTimeMs,
            DEFAULT_SPF_THROTTLE_MAX_WAIT_TIME_MS),
        firstNonNull(
            lsaThrottleStartTimeMs,
            DEFAULT_LSA_THROTTLE_START_TIME_MS),
        firstNonNull(
            lsaThrottleHoldTimeMs,
            DEFAULT_LSA_THROTTLE_HOLD_TIME_MS),
        firstNonNull(
            lsaThrottleMaxWaitTimeMs,
            DEFAULT_LSA_THROTTLE_MAX_WAIT_TIME_MS),
        firstNonNull(
            lsaArrivalTimeMs,
            DEFAULT_LSA_ARRIVAL_TIME_MS),
        inboundDistributeList,
        outboundDistributeList,
        firstNonNull(
            maximumPaths,
            DEFAULT_MAXIMUM_PATHS),
        firstNonNull(
            maxMetricRouterLsa,
            false),
        maxMetricRouterLsaOnStartupSeconds,
        firstNonNull(
            externalSummaries,
            ImmutableSet.of()),
        firstNonNull(
            virtualLinks,
            ImmutableSet.of()),
        firstNonNull(
            referenceBandwidth,
            DEFAULT_REFERENCE_BANDWIDTH),
        firstNonNull(
            redistributeActiveRoutesOnly,
            false),
        firstNonNull(
            redistributeConnected,
            false),
        firstNonNull(
            redistributeLocalLoopback,
            false),
        firstNonNull(
            redistributeOspfProcesses,
            ImmutableSet.of()),
        firstNonNull(
            redistributeOspfRouteMaps,
            ImmutableMap.of()),
        firstNonNull(
            redistributeStatic,
            false),
        redistributeConnectedRouteMap,
        redistributeLocalLoopbackRouteMap,
        redistributeStaticRouteMap,
        firstNonNull(
            redistributionMetric,
            DEFAULT_REDISTRIBUTION_METRIC),
        firstNonNull(
                defaultInformationOriginate,
                false)
            || always,
        always,
        firstNonNull(
            defaultInformationMetric,
            DEFAULT_INFORMATION_METRIC));
  }

  private Ospfv3Process(
      String processId,
      Ip routerId,
      Map<Long, Ospfv3Area> areas,
      int adminCost,
      int interAreaAdminCost,
      int externalAdminCost,
      boolean enabled,
      boolean activeBackboneStubDefaultRoute,
      int gracefulRestartIntervalSeconds,
      boolean gracefulRestartHelper,
      boolean gracefulRestartHelperStrictLsaCheck,
      boolean gracefulRestartIgnoreLostInterface,
      int spfThrottleStartTimeMs,
      int spfThrottleHoldTimeMs,
      int spfThrottleMaxWaitTimeMs,
      int lsaThrottleStartTimeMs,
      int lsaThrottleHoldTimeMs,
      int lsaThrottleMaxWaitTimeMs,
      int lsaArrivalTimeMs,
      @Nullable PrefixList6 inboundDistributeList,
      @Nullable PrefixList6 outboundDistributeList,
      int maximumPaths,
      boolean maxMetricRouterLsa,
      @Nullable Integer maxMetricRouterLsaOnStartupSeconds,
      Set<Ospfv3ExternalSummary> externalSummaries,
      Set<Ospfv3VirtualLink> virtualLinks,
      double referenceBandwidth,
      boolean redistributeActiveRoutesOnly,
      boolean redistributeConnected,
      boolean redistributeLocalLoopback,
      Set<String> redistributeOspfProcesses,
      Map<String, RouteMap6> redistributeOspfRouteMaps,
      boolean redistributeStatic,
      @Nullable RouteMap6 redistributeConnectedRouteMap,
      @Nullable RouteMap6 redistributeLocalLoopbackRouteMap,
      @Nullable RouteMap6 redistributeStaticRouteMap,
      long redistributionMetric,
      boolean defaultInformationOriginate,
      boolean defaultInformationOriginateAlways,
      long defaultInformationMetric) {

    checkArgument(
        maximumPaths >= 1
            && maximumPaths <= 32,
        "Invalid OSPFv3 maximum paths %s",
        maximumPaths);

    checkArgument(
        gracefulRestartIntervalSeconds >= 5
            && gracefulRestartIntervalSeconds <= 1800,
        "Invalid OSPFv3 graceful-restart interval %s",
        gracefulRestartIntervalSeconds);

    checkArgument(
        !gracefulRestartHelperStrictLsaCheck
            || gracefulRestartHelper,
        "OSPFv3 strict-lsa-check requires graceful-restart helper mode");

    checkArgument(
        spfThrottleStartTimeMs >= 1
            && spfThrottleStartTimeMs <= 600000
            && spfThrottleHoldTimeMs >= 1
            && spfThrottleHoldTimeMs <= 600000
            && spfThrottleMaxWaitTimeMs >= 1
            && spfThrottleMaxWaitTimeMs <= 600000,
        "Invalid OSPFv3 SPF throttle timers %s/%s/%s",
        spfThrottleStartTimeMs,
        spfThrottleHoldTimeMs,
        spfThrottleMaxWaitTimeMs);

    checkArgument(
        lsaThrottleStartTimeMs >= 0
            && lsaThrottleStartTimeMs <= 600000
            && lsaThrottleHoldTimeMs >= 0
            && lsaThrottleHoldTimeMs <= 600000
            && lsaThrottleMaxWaitTimeMs >= 0
            && lsaThrottleMaxWaitTimeMs <= 600000,
        "Invalid OSPFv3 LSA throttle timers %s/%s/%s",
        lsaThrottleStartTimeMs,
        lsaThrottleHoldTimeMs,
        lsaThrottleMaxWaitTimeMs);

    checkArgument(
        lsaArrivalTimeMs >= 0
            && lsaArrivalTimeMs <= 600000,
        "Invalid OSPFv3 LSA arrival timer %s",
        lsaArrivalTimeMs);

    checkArgument(
        maxMetricRouterLsaOnStartupSeconds == null
            || (maxMetricRouterLsaOnStartupSeconds >= 5
                && maxMetricRouterLsaOnStartupSeconds <= 86400),
        "Invalid max-metric router-lsa on-startup interval %s",
        maxMetricRouterLsaOnStartupSeconds);

    checkArgument(
        redistributeOspfProcesses.containsAll(
            redistributeOspfRouteMaps.keySet()),
        "OSPFv3 redistribution route-map configured for disabled source process");

    checkArgument(
        redistributionMetric >= 0
            && redistributionMetric <= MAX_METRIC,
        "Invalid redistribution metric %s",
        redistributionMetric);

    checkArgument(
        defaultInformationMetric >= 1
            && defaultInformationMetric <= MAX_METRIC,
        "Invalid default-information metric %s",
        defaultInformationMetric);

    _processId = processId;
    _routerId = routerId;
    _areas =
        ImmutableSortedMap.copyOf(areas);
    _adminCost = adminCost;
    _interAreaAdminCost =
        interAreaAdminCost;
    _externalAdminCost =
        externalAdminCost;
    _enabled = enabled;
    _activeBackboneStubDefaultRoute =
        activeBackboneStubDefaultRoute;
    _gracefulRestartIntervalSeconds =
        gracefulRestartIntervalSeconds;
    _gracefulRestartHelper =
        gracefulRestartHelper;
    _gracefulRestartHelperStrictLsaCheck =
        gracefulRestartHelperStrictLsaCheck;
    _gracefulRestartIgnoreLostInterface =
        gracefulRestartIgnoreLostInterface;
    _spfThrottleStartTimeMs =
        spfThrottleStartTimeMs;
    _spfThrottleHoldTimeMs =
        spfThrottleHoldTimeMs;
    _spfThrottleMaxWaitTimeMs =
        spfThrottleMaxWaitTimeMs;
    _lsaThrottleStartTimeMs =
        lsaThrottleStartTimeMs;
    _lsaThrottleHoldTimeMs =
        lsaThrottleHoldTimeMs;
    _lsaThrottleMaxWaitTimeMs =
        lsaThrottleMaxWaitTimeMs;
    _lsaArrivalTimeMs =
        lsaArrivalTimeMs;
    _inboundDistributeList =
        inboundDistributeList;
    _outboundDistributeList =
        outboundDistributeList;
    _maximumPaths =
        maximumPaths;
    _maxMetricRouterLsa =
        maxMetricRouterLsa;
    _maxMetricRouterLsaOnStartupSeconds =
        maxMetricRouterLsaOnStartupSeconds;
    _externalSummaries =
        ImmutableSet.copyOf(
            externalSummaries);
    _virtualLinks =
        ImmutableSet.copyOf(
            virtualLinks);
    _referenceBandwidth =
        referenceBandwidth;
    _redistributeActiveRoutesOnly =
        redistributeActiveRoutesOnly;
    _redistributeConnected =
        redistributeConnected;
    _redistributeConnectedRouteMap =
        redistributeConnectedRouteMap;
    _redistributeLocalLoopback =
        redistributeLocalLoopback;
    _redistributeLocalLoopbackRouteMap =
        redistributeLocalLoopbackRouteMap;
    _redistributeOspfProcesses =
        ImmutableSet.copyOf(
            redistributeOspfProcesses);
    _redistributeOspfRouteMaps =
        ImmutableMap.copyOf(
            redistributeOspfRouteMaps);
    _redistributeStatic =
        redistributeStatic;
    _redistributeStaticRouteMap =
        redistributeStaticRouteMap;
    _redistributionMetric =
        redistributionMetric;
    _defaultInformationOriginate =
        defaultInformationOriginate
            || defaultInformationOriginateAlways;
    _defaultInformationOriginateAlways =
        defaultInformationOriginateAlways;
    _defaultInformationMetric =
        defaultInformationMetric;
  }

  /**
   * Legacy/common OSPF administrative cost accessor.
   *
   * <p>For OSPFv3 this is the intra-area administrative distance.
   */
  @JsonProperty(PROP_ADMIN_COST)
  public int getAdminCost() {
    return _adminCost;
  }

  /**
   * Return the OSPFv3 intra-area administrative distance.
   *
   * <p>This is a convenience alias for {@link #getAdminCost()}.
   * Keep it out of JSON so the serialized representation retains
   * the historical {@code adminCost} property without duplicating it
   * as {@code intraAreaAdminCost}.
   */
  @JsonIgnore
  public int getIntraAreaAdminCost() {
    return _adminCost;
  }

  @JsonProperty(PROP_INTER_AREA_ADMIN_COST)
  public int getInterAreaAdminCost() {
    return _interAreaAdminCost;
  }

  @JsonProperty(PROP_EXTERNAL_ADMIN_COST)
  public int getExternalAdminCost() {
    return _externalAdminCost;
  }

  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _enabled;
  }

  @JsonProperty(
      PROP_ACTIVE_BACKBONE_STUB_DEFAULT_ROUTE)
  public boolean
      getActiveBackboneStubDefaultRoute() {

    return _activeBackboneStubDefaultRoute;
  }

  /**
   * Graceful-restart configuration is retained for configuration fidelity.
   *
   * <p>The converged Batfish snapshot does not simulate a live control-plane
   * restart event or helper timer.
   */
  @JsonProperty(
      PROP_GRACEFUL_RESTART_INTERVAL_SECONDS)
  public int
      getGracefulRestartIntervalSeconds() {

    return _gracefulRestartIntervalSeconds;
  }

  @JsonProperty(PROP_GRACEFUL_RESTART_HELPER)
  public boolean getGracefulRestartHelper() {

    return _gracefulRestartHelper;
  }

  @JsonProperty(
      PROP_GRACEFUL_RESTART_HELPER_STRICT_LSA_CHECK)
  public boolean
      getGracefulRestartHelperStrictLsaCheck() {

    return _gracefulRestartHelperStrictLsaCheck;
  }

  @JsonProperty(
      PROP_GRACEFUL_RESTART_IGNORE_LOST_INTERFACE)
  public boolean
      getGracefulRestartIgnoreLostInterface() {

    return _gracefulRestartIgnoreLostInterface;
  }

  /**
   * OSPFv3 convergence timers are retained for configuration fidelity.
   *
   * <p>The converged dataplane does not simulate wall-clock SPF or LSA
   * scheduling.
   */
  @JsonProperty(PROP_SPF_THROTTLE_START_TIME_MS)
  public int getSpfThrottleStartTimeMs() {

    return _spfThrottleStartTimeMs;
  }

  @JsonProperty(PROP_SPF_THROTTLE_HOLD_TIME_MS)
  public int getSpfThrottleHoldTimeMs() {

    return _spfThrottleHoldTimeMs;
  }

  @JsonProperty(PROP_SPF_THROTTLE_MAX_WAIT_TIME_MS)
  public int getSpfThrottleMaxWaitTimeMs() {

    return _spfThrottleMaxWaitTimeMs;
  }

  @JsonProperty(PROP_LSA_THROTTLE_START_TIME_MS)
  public int getLsaThrottleStartTimeMs() {

    return _lsaThrottleStartTimeMs;
  }

  @JsonProperty(PROP_LSA_THROTTLE_HOLD_TIME_MS)
  public int getLsaThrottleHoldTimeMs() {

    return _lsaThrottleHoldTimeMs;
  }

  @JsonProperty(PROP_LSA_THROTTLE_MAX_WAIT_TIME_MS)
  public int getLsaThrottleMaxWaitTimeMs() {

    return _lsaThrottleMaxWaitTimeMs;
  }

  @JsonProperty(PROP_LSA_ARRIVAL_TIME_MS)
  public int getLsaArrivalTimeMs() {

    return _lsaArrivalTimeMs;
  }

  @JsonProperty(PROP_INBOUND_DISTRIBUTE_LIST)
  public @Nullable PrefixList6
      getInboundDistributeList() {
    return _inboundDistributeList;
  }

  @JsonProperty(PROP_OUTBOUND_DISTRIBUTE_LIST)
  public @Nullable PrefixList6
      getOutboundDistributeList() {
    return _outboundDistributeList;
  }

  @JsonProperty(PROP_MAXIMUM_PATHS)
  public int getMaximumPaths() {
    return _maximumPaths;
  }

  @JsonProperty(PROP_MAX_METRIC_ROUTER_LSA)
  public boolean getMaxMetricRouterLsa() {
    return _maxMetricRouterLsa;
  }

  /**
   * Startup-only max-metric configuration.
   *
   * <p>The VI dataplane represents a timeless converged snapshot, so this
   * value is retained for configuration fidelity but does not by itself
   * activate permanent stub-router behavior.
   */
  @JsonProperty(
      PROP_MAX_METRIC_ROUTER_LSA_ON_STARTUP_SECONDS)
  public @Nullable Integer
      getMaxMetricRouterLsaOnStartupSeconds() {

    return _maxMetricRouterLsaOnStartupSeconds;
  }

  @JsonProperty(PROP_EXTERNAL_SUMMARIES)
  public @Nonnull Set<Ospfv3ExternalSummary>
      getExternalSummaries() {

    return _externalSummaries;
  }

  @JsonProperty(PROP_VIRTUAL_LINKS)
  public @Nonnull Set<Ospfv3VirtualLink>
      getVirtualLinks() {

    return _virtualLinks;
  }

  @JsonProperty(PROP_PROCESS_ID)
  public @Nonnull String getProcessId() {
    return _processId;
  }

  @JsonProperty(PROP_ROUTER_ID)
  public @Nonnull Ip getRouterId() {
    return _routerId;
  }

  @JsonProperty(PROP_AREAS)
  public @Nonnull SortedMap<Long, Ospfv3Area>
      getAreas() {
    return _areas;
  }

  @JsonProperty(PROP_REFERENCE_BANDWIDTH)
  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  /**
   * When true, redistribution considers only routes that are currently
   * selected in the IPv6 forwarding/main RIB.
   */
  @JsonProperty(
      PROP_REDISTRIBUTE_ACTIVE_ROUTES_ONLY)
  public boolean
      getRedistributeActiveRoutesOnly() {

    return _redistributeActiveRoutesOnly;
  }

  @JsonProperty(PROP_REDISTRIBUTE_CONNECTED)
  public boolean getRedistributeConnected() {
    return _redistributeConnected;
  }

  @JsonProperty(PROP_REDISTRIBUTE_LOCAL_LOOPBACK)
  public boolean getRedistributeLocalLoopback() {
    return _redistributeLocalLoopback;
  }

  @JsonProperty(PROP_REDISTRIBUTE_OSPF_PROCESSES)
  public @Nonnull Set<String>
      getRedistributeOspfProcesses() {

    return _redistributeOspfProcesses;
  }

  @JsonProperty(PROP_REDISTRIBUTE_OSPF_ROUTE_MAPS)
  public @Nonnull Map<String, RouteMap6>
      getRedistributeOspfRouteMaps() {

    return _redistributeOspfRouteMaps;
  }

  @JsonProperty(PROP_REDISTRIBUTE_STATIC)
  public boolean getRedistributeStatic() {
    return _redistributeStatic;
  }

  @JsonProperty(PROP_REDISTRIBUTE_CONNECTED_ROUTE_MAP)
  public @Nullable RouteMap6
      getRedistributeConnectedRouteMap() {
    return _redistributeConnectedRouteMap;
  }

  @JsonProperty(PROP_REDISTRIBUTE_LOCAL_LOOPBACK_ROUTE_MAP)
  public @Nullable RouteMap6
      getRedistributeLocalLoopbackRouteMap() {

    return _redistributeLocalLoopbackRouteMap;
  }

  @JsonProperty(PROP_REDISTRIBUTE_STATIC_ROUTE_MAP)
  public @Nullable RouteMap6
      getRedistributeStaticRouteMap() {
    return _redistributeStaticRouteMap;
  }

  @JsonProperty(PROP_REDISTRIBUTION_METRIC)
  public long getRedistributionMetric() {
    return _redistributionMetric;
  }

  @JsonProperty(PROP_DEFAULT_INFORMATION_ORIGINATE)
  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  @JsonProperty(PROP_DEFAULT_INFORMATION_ORIGINATE_ALWAYS)
  public boolean getDefaultInformationOriginateAlways() {
    return _defaultInformationOriginateAlways;
  }

  @JsonProperty(PROP_DEFAULT_INFORMATION_METRIC)
  public long getDefaultInformationMetric() {
    return _defaultInformationMetric;
  }

  private final int _adminCost;
  private final int _interAreaAdminCost;
  private final int _externalAdminCost;
  private final boolean _enabled;
  private final boolean
      _activeBackboneStubDefaultRoute;
  private final int
      _gracefulRestartIntervalSeconds;
  private final boolean
      _gracefulRestartHelper;
  private final boolean
      _gracefulRestartHelperStrictLsaCheck;
  private final boolean
      _gracefulRestartIgnoreLostInterface;
  private final int
      _spfThrottleStartTimeMs;
  private final int
      _spfThrottleHoldTimeMs;
  private final int
      _spfThrottleMaxWaitTimeMs;
  private final int
      _lsaThrottleStartTimeMs;
  private final int
      _lsaThrottleHoldTimeMs;
  private final int
      _lsaThrottleMaxWaitTimeMs;
  private final int
      _lsaArrivalTimeMs;
  private final @Nullable PrefixList6
      _inboundDistributeList;
  private final @Nullable PrefixList6
      _outboundDistributeList;
  private final int _maximumPaths;
  private final boolean _maxMetricRouterLsa;
  private final @Nullable Integer
      _maxMetricRouterLsaOnStartupSeconds;
  private final @Nonnull
      Set<Ospfv3ExternalSummary> _externalSummaries;
  private final @Nonnull
      Set<Ospfv3VirtualLink> _virtualLinks;
  private final @Nonnull String _processId;
  private final @Nonnull Ip _routerId;
  private final @Nonnull SortedMap<Long, Ospfv3Area>
      _areas;
  private final double _referenceBandwidth;
  private final boolean
      _redistributeActiveRoutesOnly;
  private final boolean _redistributeConnected;
  private final @Nullable RouteMap6
      _redistributeConnectedRouteMap;
  private final boolean _redistributeLocalLoopback;
  private final @Nullable RouteMap6
      _redistributeLocalLoopbackRouteMap;
  private final @Nonnull Set<String>
      _redistributeOspfProcesses;
  private final @Nonnull Map<String, RouteMap6>
      _redistributeOspfRouteMaps;
  private final boolean _redistributeStatic;
  private final @Nullable RouteMap6
      _redistributeStaticRouteMap;
  private final long _redistributionMetric;
  private final boolean _defaultInformationOriginate;
  private final boolean
      _defaultInformationOriginateAlways;
  private final long _defaultInformationMetric;
}
