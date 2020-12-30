package org.batfish.datamodel.eigrp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents an EIGRP process on a router */
@ParametersAreNonnullByDefault
public final class EigrpProcess implements Serializable {
  private static final String PROP_ASN = "asn";
  private static final String PROP_EXPORT_POLICY = "exportPolicy";
  private static final String PROP_METRIC_VERSION = "metricVersion";
  private static final String PROP_MODE = "eigrpMode";
  private static final String PROP_NEIGHBORS = "neighbors";
  private static final String PROP_ROUTER_ID = "routerId";
  private static final String PROP_INTERNAL_ADMIN_COST = "internalAdminCost";
  private static final String PROP_EXTERNAL_ADMIN_COST = "externalAdminCost";

  private final long _asn;
  @Nullable private final String _redistributionPolicy;
  @Nonnull private final EigrpMetricVersion _metricVersion;
  @Nonnull private final EigrpProcessMode _mode;
  @Nonnull private SortedMap<String, EigrpNeighborConfig> _neighbors;
  @Nonnull private final Ip _routerId;
  private final int _internalAdminCost;
  private final int _externalAdminCost;

  private EigrpProcess(
      long asn,
      @Nullable String exportPolicy,
      EigrpProcessMode mode,
      EigrpMetricVersion metricVersion,
      Ip routerId,
      Map<String, EigrpNeighborConfig> neighbors,
      int internalAdminCost,
      int externalAdminCost) {
    _asn = asn;
    _redistributionPolicy = exportPolicy;
    _mode = mode;
    _metricVersion = metricVersion;
    _neighbors = ImmutableSortedMap.copyOf(neighbors);
    _routerId = routerId;
    _internalAdminCost = internalAdminCost;
    _externalAdminCost = externalAdminCost;
  }

  @JsonCreator
  private static EigrpProcess jsonCreator(
      @Nullable @JsonProperty(PROP_ASN) Long asn,
      @Nullable @JsonProperty(PROP_EXPORT_POLICY) String exportPolicy,
      @Nullable @JsonProperty(PROP_METRIC_VERSION) EigrpMetricVersion metricVersion,
      @Nullable @JsonProperty(PROP_MODE) EigrpProcessMode mode,
      @Nullable @JsonProperty(PROP_NEIGHBORS) Map<String, EigrpNeighborConfig> neighbors,
      @Nullable @JsonProperty(PROP_ROUTER_ID) Ip routerId,
      @Nullable @JsonProperty(PROP_INTERNAL_ADMIN_COST) Integer internalAdminCost,
      @Nullable @JsonProperty(PROP_EXTERNAL_ADMIN_COST) Integer externalAdminCost) {
    checkArgument(asn != null, "Missing %s", PROP_ASN);
    checkArgument(metricVersion != null, "Missing %s", PROP_METRIC_VERSION);
    checkArgument(mode != null, "Missing %s", PROP_MODE);
    checkArgument(routerId != null, "Missing %s", PROP_ROUTER_ID);
    checkArgument(internalAdminCost != null, "Missing %s", PROP_INTERNAL_ADMIN_COST);
    checkArgument(externalAdminCost != null, "Missing %s", PROP_EXTERNAL_ADMIN_COST);
    return new EigrpProcess(
        asn,
        exportPolicy,
        mode,
        metricVersion,
        routerId,
        firstNonNull(neighbors, ImmutableMap.of()),
        internalAdminCost,
        externalAdminCost);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** @return The AS number for this process */
  @JsonProperty(PROP_ASN)
  public long getAsn() {
    return _asn;
  }

  /**
   * @return The routing policy applied to routes in the main RIB to determine which ones are
   *     exported into EIGRP and how
   */
  @Nullable
  @JsonProperty(PROP_EXPORT_POLICY)
  public String getRedistributionPolicy() {
    return _redistributionPolicy;
  }

  /** @return All EIGRP neighbors in this process */
  @Nonnull
  @JsonProperty(PROP_NEIGHBORS)
  public SortedMap<String, EigrpNeighborConfig> getNeighbors() {
    return _neighbors;
  }

  /** @return The router-id of this EIGRP process */
  @Nonnull
  @JsonProperty(PROP_ROUTER_ID)
  public Ip getRouterId() {
    return _routerId;
  }

  /** @return The {@link EigrpMetricVersion} used by this process */
  @Nonnull
  @JsonProperty(PROP_METRIC_VERSION)
  public EigrpMetricVersion getMetricVersion() {
    return _metricVersion;
  }

  /** @return The EIGRP mode for this process */
  @Nonnull
  @JsonProperty(PROP_MODE)
  public EigrpProcessMode getMode() {
    return _mode;
  }

  public int getInternalAdminCost() {
    return _internalAdminCost;
  }

  public int getExternalAdminCost() {
    return _externalAdminCost;
  }

  /** Add an {@link EigrpNeighborConfig} to this EIGRP process */
  public void addNeighbor(EigrpNeighborConfig neighborConfig) {
    _neighbors =
        ImmutableSortedMap.<String, EigrpNeighborConfig>naturalOrder()
            .putAll(_neighbors)
            .put(neighborConfig.getInterfaceName(), neighborConfig)
            .build();
  }

  /** Add a {@link Collection} of {@link EigrpNeighborConfig}s to this EIGRP process */
  public void addNeighbors(Collection<EigrpNeighborConfig> neighborConfigs) {
    _neighbors =
        ImmutableSortedMap.<String, EigrpNeighborConfig>naturalOrder()
            .putAll(_neighbors)
            .putAll(
                neighborConfigs.stream()
                    .collect(
                        ImmutableMap.toImmutableMap(
                            EigrpNeighborConfig::getInterfaceName, Function.identity())))
            .build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpProcess)) {
      return false;
    }
    EigrpProcess that = (EigrpProcess) o;
    return _asn == that._asn
        && Objects.equals(_redistributionPolicy, that._redistributionPolicy)
        && _mode == that._mode
        && _neighbors == that._neighbors
        && _routerId.equals(that._routerId)
        && _internalAdminCost == that._internalAdminCost
        && _externalAdminCost == that._externalAdminCost;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _asn,
        _redistributionPolicy,
        _mode,
        _routerId,
        _neighbors,
        _internalAdminCost,
        _externalAdminCost);
  }

  public static class Builder {
    @Nullable private Long _asn;
    @Nullable private String _exportPolicy;
    @Nullable private EigrpMetricVersion _metricVersion;
    @Nullable private EigrpProcessMode _mode;
    @Nullable private Map<String, EigrpNeighborConfig> _neighbors;
    @Nullable private Ip _routerId;
    // The defaults are consistent across cisco variants
    private int _internalAdminCost = 90;
    private int _externalAdminCost = 170;

    private Builder() {}

    @Nonnull
    public EigrpProcess build() {
      checkArgument(_asn != null, "Missing %s", PROP_ASN);
      checkArgument(_metricVersion != null, "Missing %s", PROP_METRIC_VERSION);
      checkArgument(_mode != null, "Missing %s", PROP_MODE);
      checkArgument(_routerId != null, "Missing %s", PROP_ROUTER_ID);
      return new EigrpProcess(
          _asn,
          _exportPolicy,
          _mode,
          _metricVersion,
          _routerId,
          firstNonNull(_neighbors, ImmutableMap.of()),
          _internalAdminCost,
          _externalAdminCost);
    }

    @Nonnull
    public Builder setAsNumber(long asn) {
      _asn = asn;
      return this;
    }

    @Nonnull
    public Builder setRedistributionPolicy(@Nullable String exportPolicy) {
      _exportPolicy = exportPolicy;
      return this;
    }

    public Builder setNeighbors(@Nonnull Map<String, EigrpNeighborConfig> neighbors) {
      _neighbors = neighbors;
      return this;
    }

    @Nonnull
    public Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    @Nonnull
    public Builder setMetricVersion(EigrpMetricVersion version) {
      _metricVersion = version;
      return this;
    }

    @Nonnull
    public Builder setMode(EigrpProcessMode mode) {
      _mode = mode;
      return this;
    }

    public Builder setInternalAdminCost(int internalAdminCost) {
      _internalAdminCost = internalAdminCost;
      return this;
    }

    public Builder setExternalAdminCost(int externalAdminCost) {
      _externalAdminCost = externalAdminCost;
      return this;
    }
  }
}
