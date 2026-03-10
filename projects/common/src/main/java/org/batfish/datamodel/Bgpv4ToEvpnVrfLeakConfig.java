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
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** VRF leaking config that leaks routes from a BGPv4 RIB into an EVPN RIB */
public final class Bgpv4ToEvpnVrfLeakConfig implements Serializable {

  /** Additional route-targets to attach to a leaked route, on top of any set by policy. */
  @JsonIgnore
  public @Nonnull Set<ExtendedCommunity> getAttachRouteTargets() {
    return _attachRouteTargets;
  }

  @JsonProperty(PROP_ATTACH_ROUTE_TARGETS)
  private @Nonnull SortedSet<ExtendedCommunity> getAttachRouteTargetsSorted() {
    return ImmutableSortedSet.copyOf(Comparator.naturalOrder(), _attachRouteTargets);
  }

  /** Name of the source VRF from which to copy routes. The source VRF must have a BGP RIB. */
  @JsonProperty(PROP_IMPORT_FROM_VRF)
  public @Nonnull String getImportFromVrf() {
    return _importFromVrf;
  }

  /** Route distinguisher of the source VRF from which to copy routes. */
  @JsonProperty(PROP_SRC_VRF_ROUTE_DISTINGUISHER)
  public @Nonnull RouteDistinguisher getSrcVrfRouteDistinguisher() {
    return _srcVrfRouteDistinguisher;
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Bgpv4ToEvpnVrfLeakConfig)) {
      return false;
    }
    Bgpv4ToEvpnVrfLeakConfig that = (Bgpv4ToEvpnVrfLeakConfig) o;
    return _attachRouteTargets.equals(that._attachRouteTargets)
        && _importFromVrf.equals(that._importFromVrf)
        && _srcVrfRouteDistinguisher.equals(that._srcVrfRouteDistinguisher);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_attachRouteTargets, _importFromVrf, _srcVrfRouteDistinguisher);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Bgpv4ToEvpnVrfLeakConfig.class)
        .omitNullValues()
        .add(PROP_IMPORT_FROM_VRF, _importFromVrf)
        .add(PROP_SRC_VRF_ROUTE_DISTINGUISHER, _srcVrfRouteDistinguisher)
        .add(PROP_ATTACH_ROUTE_TARGETS, _attachRouteTargets)
        .toString();
  }

  private Bgpv4ToEvpnVrfLeakConfig(
      String importFromVrf,
      RouteDistinguisher srcVrfRouteDistinguisher,
      Set<ExtendedCommunity> attachRouteTargets) {
    _attachRouteTargets = attachRouteTargets;
    _importFromVrf = importFromVrf;
    _srcVrfRouteDistinguisher = srcVrfRouteDistinguisher;
  }

  @JsonCreator
  private static Bgpv4ToEvpnVrfLeakConfig create(
      @JsonProperty(PROP_ATTACH_ROUTE_TARGETS) @Nullable
          Iterable<ExtendedCommunity> attachRouteTargets,
      @JsonProperty(PROP_IMPORT_FROM_VRF) @Nullable String importFromVrf,
      @JsonProperty(PROP_SRC_VRF_ROUTE_DISTINGUISHER) @Nullable
          RouteDistinguisher srcVrfRouteDistinguisher) {
    return builder()
        .setAttachRouteTargets(firstNonNull(attachRouteTargets, ImmutableSet.of()))
        .setImportFromVrf(importFromVrf)
        .setSrcVrfRouteDistinguisher(srcVrfRouteDistinguisher)
        .build();
  }

  private static final String PROP_ATTACH_ROUTE_TARGETS = "attachRouteTargets";
  private static final String PROP_IMPORT_FROM_VRF = "importFromVrf";
  private static final String PROP_SRC_VRF_ROUTE_DISTINGUISHER = "srcVrfRouteDistinguisher";

  private final @Nonnull Set<ExtendedCommunity> _attachRouteTargets;
  private final @Nonnull String _importFromVrf;
  private final @Nonnull RouteDistinguisher _srcVrfRouteDistinguisher;

  public static final class Builder {
    public @Nonnull Bgpv4ToEvpnVrfLeakConfig build() {
      checkArgument(_importFromVrf != null, "Missing %s", PROP_IMPORT_FROM_VRF);
      checkArgument(
          _srcVrfRouteDistinguisher != null, "Missing %s", PROP_SRC_VRF_ROUTE_DISTINGUISHER);
      return new Bgpv4ToEvpnVrfLeakConfig(
          _importFromVrf, _srcVrfRouteDistinguisher, _attachRouteTargets);
    }

    public @Nonnull Builder setAttachRouteTargets(Iterable<ExtendedCommunity> attachRouteTargets) {
      _attachRouteTargets = ImmutableSet.copyOf(attachRouteTargets);
      return this;
    }

    public @Nonnull Builder setAttachRouteTargets(ExtendedCommunity... attachRouteTargets) {
      return setAttachRouteTargets(Arrays.asList(attachRouteTargets));
    }

    public @Nonnull Builder setImportFromVrf(@Nullable String importFromVrf) {
      _importFromVrf = importFromVrf;
      return this;
    }

    public @Nonnull Builder setSrcVrfRouteDistinguisher(@Nullable RouteDistinguisher rd) {
      _srcVrfRouteDistinguisher = rd;
      return this;
    }

    private @Nonnull Set<ExtendedCommunity> _attachRouteTargets;
    private @Nullable String _importFromVrf;
    private @Nullable RouteDistinguisher _srcVrfRouteDistinguisher;

    private Builder() {
      _attachRouteTargets = ImmutableSet.of();
    }
  }
}
