package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** VRF leaking config that leaks routes between BGP RIBs */
@ParametersAreNonnullByDefault
public final class BgpVrfLeakConfig implements Serializable {

  /** Administrative distance to apply to leaked routes. */
  @JsonProperty(PROP_ADMIN)
  public int getAdmin() {
    return _admin;
  }

  /** Additional route-targets to attach to a leaked route, on top of any set by policy. */
  @JsonIgnore
  public @Nonnull Set<ExtendedCommunity> getAttachRouteTargets() {
    return _attachRouteTargets;
  }

  @JsonProperty(PROP_ATTACH_ROUTE_TARGETS)
  private @Nonnull SortedSet<ExtendedCommunity> getAttachRouteTargetsSorted() {
    return ImmutableSortedSet.copyOf(Comparator.naturalOrder(), _attachRouteTargets);
  }

  /**
   * Name of the import policy to apply to imported routes when leaking. If {@code null} no policy
   * is applied, all routes are allowed.
   */
  @JsonProperty(PROP_IMPORT_POLICY)
  public @Nullable String getImportPolicy() {
    return _importPolicy;
  }

  /** Name of the source VRF from which to copy routes. The source VRF must have a BGP RIB. */
  @JsonProperty(PROP_IMPORT_FROM_VRF)
  public @Nonnull String getImportFromVrf() {
    return _importFromVrf;
  }

  /** Weight to apply to leaked routes. */
  @JsonProperty(PROP_WEIGHT)
  public int getWeight() {
    return _weight;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpVrfLeakConfig)) {
      return false;
    }
    BgpVrfLeakConfig that = (BgpVrfLeakConfig) o;
    return _admin == that._admin
        && _attachRouteTargets.equals(that._attachRouteTargets)
        && _importFromVrf.equals(that._importFromVrf)
        && Objects.equals(_importPolicy, that._importPolicy)
        && _weight == that._weight;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_admin, _attachRouteTargets, _importPolicy, _importFromVrf, _weight);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(BgpVrfLeakConfig.class)
        .omitNullValues()
        .add("importPolicy", _importPolicy)
        .add("importFromVrf", _importFromVrf)
        .add("admin", _admin)
        .add("attachRouteTargets", _attachRouteTargets)
        .add("weight", _weight)
        .toString();
  }

  private BgpVrfLeakConfig(
      @Nullable String importPolicy,
      String importFromVrf,
      int admin,
      Set<ExtendedCommunity> attachRouteTargets,
      int weight) {
    _admin = admin;
    _attachRouteTargets = attachRouteTargets;
    _importPolicy = importPolicy;
    _importFromVrf = importFromVrf;
    _weight = weight;
  }

  private static final String PROP_ADMIN = "admin";
  private static final String PROP_ATTACH_ROUTE_TARGETS = "attachRouteTargets";
  private static final String PROP_IMPORT_FROM_VRF = "importFromVrf";
  private static final String PROP_IMPORT_POLICY = "importPolicy";
  private static final String PROP_WEIGHT = "weight";

  @JsonCreator
  private static BgpVrfLeakConfig create(
      @JsonProperty(PROP_IMPORT_FROM_VRF) @Nullable String importFromVrf,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy,
      @JsonProperty(PROP_ADMIN) int admin,
      @JsonProperty(PROP_ATTACH_ROUTE_TARGETS) @Nullable
          Iterable<ExtendedCommunity> attachRouteTargets,
      @JsonProperty(PROP_WEIGHT) int weight) {
    return builder()
        .setImportFromVrf(importFromVrf)
        .setImportPolicy(importPolicy)
        .setAdmin(admin)
        .setAttachRouteTargets(firstNonNull(attachRouteTargets, ImmutableSet.of()))
        .setWeight(weight)
        .build();
  }

  private final int _admin;
  private final @Nonnull Set<ExtendedCommunity> _attachRouteTargets;
  private final @Nullable String _importPolicy;
  private final @Nonnull String _importFromVrf;
  private final int _weight;

  @ParametersAreNonnullByDefault
  public static class Builder {

    public BgpVrfLeakConfig build() {
      checkArgument(_importFromVrf != null, "Missing %s", PROP_IMPORT_FROM_VRF);
      checkArgument(_admin != null, "Missing %s", PROP_ADMIN);
      checkArgument(_weight != null, "Missing %s", PROP_WEIGHT);
      return new BgpVrfLeakConfig(
          _importPolicy,
          _importFromVrf,
          _admin,
          firstNonNull(_attachRouteTargets, ImmutableSet.of()),
          _weight);
    }

    public Builder setAdmin(@Nullable Integer admin) {
      _admin = admin;
      return this;
    }

    public @Nonnull Builder setAttachRouteTargets(Iterable<ExtendedCommunity> attachRouteTargets) {
      _attachRouteTargets = ImmutableSet.copyOf(attachRouteTargets);
      return this;
    }

    public @Nonnull Builder setAttachRouteTargets(ExtendedCommunity... attachRouteTargets) {
      return setAttachRouteTargets(Arrays.asList(attachRouteTargets));
    }

    public Builder setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return this;
    }

    public Builder setImportFromVrf(@Nullable String importFromVrf) {
      _importFromVrf = importFromVrf;
      return this;
    }

    public Builder setWeight(@Nullable Integer weight) {
      _weight = weight;
      return this;
    }

    private @Nullable Integer _admin;
    private @Nonnull Set<ExtendedCommunity> _attachRouteTargets;
    private @Nullable String _importPolicy;
    private @Nullable String _importFromVrf;
    private @Nullable Integer _weight;

    private Builder() {
      _attachRouteTargets = ImmutableSet.of();
    }
  }
}
