package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
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

  public static final class Builder {
    private int _adminCost;
    private @Nonnull Map<Long, Ospfv3Area> _areas;
    private boolean _defaultInformationOriginate;
    private boolean _defaultInformationOriginateAlways;
    private long _defaultInformationMetric;
    private @Nullable String _processId;
    private double _referenceBandwidth;
    private boolean _redistributeConnected;
    private @Nullable RouteMap6
        _redistributeConnectedRouteMap;
    private boolean _redistributeStatic;
    private @Nullable RouteMap6
        _redistributeStaticRouteMap;
    private long _redistributionMetric;
    private @Nullable Ip _routerId;
    private @Nullable Vrf _vrf;

    private Builder() {
      _adminCost =
          DEFAULT_ADMIN_COST;
      _areas =
          ImmutableMap.of();
      _defaultInformationMetric =
          DEFAULT_INFORMATION_METRIC;
      _referenceBandwidth =
          DEFAULT_REFERENCE_BANDWIDTH;
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
          "Invalid admin cost %s",
          _adminCost);

      checkArgument(
          _referenceBandwidth > 0,
          "Invalid reference bandwidth %s",
          _referenceBandwidth);

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
              _referenceBandwidth,
              _redistributeConnected,
              _redistributeStatic,
              _redistributeConnectedRouteMap,
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

    public Builder setAdminCost(
        int adminCost) {
      _adminCost = adminCost;
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

    public Builder setRedistributeConnected(
        boolean redistributeConnected) {
      _redistributeConnected =
          redistributeConnected;
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
  private static final String PROP_REDISTRIBUTE_CONNECTED =
      "redistributeConnected";
  private static final String
      PROP_REDISTRIBUTE_CONNECTED_ROUTE_MAP =
          "redistributeConnectedRouteMap";
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
      @JsonProperty(PROP_REFERENCE_BANDWIDTH)
          @Nullable Double referenceBandwidth,
      @JsonProperty(PROP_REDISTRIBUTE_CONNECTED)
          @Nullable Boolean redistributeConnected,
      @JsonProperty(PROP_REDISTRIBUTE_STATIC)
          @Nullable Boolean redistributeStatic,
      @JsonProperty(PROP_REDISTRIBUTE_CONNECTED_ROUTE_MAP)
          @Nullable RouteMap6 redistributeConnectedRouteMap,
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

    return new Ospfv3Process(
        processId,
        routerId,
        firstNonNull(
            areas,
            ImmutableMap.of()),
        firstNonNull(
            adminCost,
            DEFAULT_ADMIN_COST),
        firstNonNull(
            referenceBandwidth,
            DEFAULT_REFERENCE_BANDWIDTH),
        firstNonNull(
            redistributeConnected,
            false),
        firstNonNull(
            redistributeStatic,
            false),
        redistributeConnectedRouteMap,
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
      double referenceBandwidth,
      boolean redistributeConnected,
      boolean redistributeStatic,
      @Nullable RouteMap6 redistributeConnectedRouteMap,
      @Nullable RouteMap6 redistributeStaticRouteMap,
      long redistributionMetric,
      boolean defaultInformationOriginate,
      boolean defaultInformationOriginateAlways,
      long defaultInformationMetric) {

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
    _referenceBandwidth =
        referenceBandwidth;
    _redistributeConnected =
        redistributeConnected;
    _redistributeConnectedRouteMap =
        redistributeConnectedRouteMap;
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

  @JsonProperty(PROP_ADMIN_COST)
  public int getAdminCost() {
    return _adminCost;
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

  @JsonProperty(PROP_REDISTRIBUTE_CONNECTED)
  public boolean getRedistributeConnected() {
    return _redistributeConnected;
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
  private final @Nonnull String _processId;
  private final @Nonnull Ip _routerId;
  private final @Nonnull SortedMap<Long, Ospfv3Area>
      _areas;
  private final double _referenceBandwidth;
  private final boolean _redistributeConnected;
  private final @Nullable RouteMap6
      _redistributeConnectedRouteMap;
  private final boolean _redistributeStatic;
  private final @Nullable RouteMap6
      _redistributeStaticRouteMap;
  private final long _redistributionMetric;
  private final boolean _defaultInformationOriginate;
  private final boolean
      _defaultInformationOriginateAlways;
  private final long _defaultInformationMetric;
}
