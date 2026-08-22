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
import org.batfish.datamodel.Vrf;

/** An OSPFv3 routing process. */
@ParametersAreNonnullByDefault
public final class Ospfv3Process implements Serializable {

  public static final int DEFAULT_ADMIN_COST = 110;

  /** AOS-CX OSPFv3 default reference bandwidth: 100000 Mbps. */
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100_000_000_000D;

  /** AOS-CX default metric for redistributed OSPFv3 routes. */
  public static final long DEFAULT_REDISTRIBUTION_METRIC = 25L;

  public static final class Builder {
    private int _adminCost;
    private @Nonnull Map<Long, Ospfv3Area> _areas;
    private @Nullable String _processId;
    private double _referenceBandwidth;
    private boolean _redistributeConnected;
    private long _redistributionMetric;
    private @Nullable Ip _routerId;
    private @Nullable Vrf _vrf;

    private Builder() {
      _adminCost = DEFAULT_ADMIN_COST;
      _areas = ImmutableMap.of();
      _referenceBandwidth = DEFAULT_REFERENCE_BANDWIDTH;
      _redistributionMetric = DEFAULT_REDISTRIBUTION_METRIC;
    }

    public Ospfv3Process build() {
      checkArgument(_processId != null, "Missing processId");
      checkArgument(_routerId != null, "Missing routerId");
      checkArgument(_adminCost >= 0, "Invalid admin cost %s", _adminCost);
      checkArgument(
          _referenceBandwidth > 0,
          "Invalid reference bandwidth %s",
          _referenceBandwidth);
      checkArgument(
          _redistributionMetric >= 0,
          "Invalid redistribution metric %s",
          _redistributionMetric);

      Ospfv3Process process =
          new Ospfv3Process(
              _processId,
              _routerId,
              _areas,
              _adminCost,
              _referenceBandwidth,
              _redistributeConnected,
              _redistributionMetric);

      if (_vrf != null) {
        _vrf.addOspfv3Process(process);
      }
      return process;
    }

    public Builder setAdminCost(int adminCost) {
      _adminCost = adminCost;
      return this;
    }

    public Builder setProcessId(String processId) {
      _processId = processId;
      return this;
    }

    public Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public Builder setAreas(Map<Long, Ospfv3Area> areas) {
      _areas = areas;
      return this;
    }

    public Builder setReferenceBandwidth(double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
      return this;
    }

    public Builder setRedistributeConnected(
        boolean redistributeConnected) {
      _redistributeConnected = redistributeConnected;
      return this;
    }

    public Builder setRedistributionMetric(long redistributionMetric) {
      _redistributionMetric = redistributionMetric;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }
  }

  private static final String PROP_ADMIN_COST = "adminCost";
  private static final String PROP_AREAS = "areas";
  private static final String PROP_PROCESS_ID = "processId";
  private static final String PROP_REFERENCE_BANDWIDTH =
      "referenceBandwidth";
  private static final String PROP_REDISTRIBUTE_CONNECTED =
      "redistributeConnected";
  private static final String PROP_REDISTRIBUTION_METRIC =
      "redistributionMetric";
  private static final String PROP_ROUTER_ID = "routerId";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static Ospfv3Process create(
      @JsonProperty(PROP_PROCESS_ID) @Nullable String processId,
      @JsonProperty(PROP_ROUTER_ID) @Nullable Ip routerId,
      @JsonProperty(PROP_AREAS)
          @Nullable Map<Long, Ospfv3Area> areas,
      @JsonProperty(PROP_ADMIN_COST)
          @Nullable Integer adminCost,
      @JsonProperty(PROP_REFERENCE_BANDWIDTH)
          @Nullable Double referenceBandwidth,
      @JsonProperty(PROP_REDISTRIBUTE_CONNECTED)
          @Nullable Boolean redistributeConnected,
      @JsonProperty(PROP_REDISTRIBUTION_METRIC)
          @Nullable Long redistributionMetric) {
    checkArgument(processId != null, "Missing %s", PROP_PROCESS_ID);
    checkArgument(routerId != null, "Missing %s", PROP_ROUTER_ID);

    return new Ospfv3Process(
        processId,
        routerId,
        firstNonNull(areas, ImmutableMap.of()),
        firstNonNull(adminCost, DEFAULT_ADMIN_COST),
        firstNonNull(
            referenceBandwidth,
            DEFAULT_REFERENCE_BANDWIDTH),
        firstNonNull(redistributeConnected, false),
        firstNonNull(
            redistributionMetric,
            DEFAULT_REDISTRIBUTION_METRIC));
  }

  private Ospfv3Process(
      String processId,
      Ip routerId,
      Map<Long, Ospfv3Area> areas,
      int adminCost,
      double referenceBandwidth,
      boolean redistributeConnected,
      long redistributionMetric) {
    _processId = processId;
    _routerId = routerId;
    _areas = ImmutableSortedMap.copyOf(areas);
    _adminCost = adminCost;
    _referenceBandwidth = referenceBandwidth;
    _redistributeConnected = redistributeConnected;
    _redistributionMetric = redistributionMetric;
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
  public @Nonnull SortedMap<Long, Ospfv3Area> getAreas() {
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

  @JsonProperty(PROP_REDISTRIBUTION_METRIC)
  public long getRedistributionMetric() {
    return _redistributionMetric;
  }

  private final int _adminCost;
  private final @Nonnull String _processId;
  private final @Nonnull Ip _routerId;
  private final @Nonnull SortedMap<Long, Ospfv3Area> _areas;
  private final double _referenceBandwidth;
  private final boolean _redistributeConnected;
  private final long _redistributionMetric;
}
