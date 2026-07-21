package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** A Layer 2 {@link VniConfig} */
@ParametersAreNonnullByDefault
public class Layer2VniConfig extends VniConfig
    implements Serializable, Comparable<Layer2VniConfig> {

  private Layer2VniConfig(
      int vni,
      String vrf,
      RouteDistinguisher rd,
      SortedSet<ExtendedCommunity> routeTargets,
      SortedSet<String> importRouteTargets) {
    super(vni, vrf, rd, routeTargets, importRouteTargets);
  }

  @JsonCreator
  private static Layer2VniConfig create(
      @JsonProperty(PROP_VNI) @Nullable Integer vni,
      @JsonProperty(PROP_VRF) @Nullable String vrf,
      @JsonProperty(PROP_ROUTE_DISTINGUISHER) @Nullable RouteDistinguisher rd,
      @JsonProperty(PROP_ROUTE_TARGETS) @Nullable SortedSet<ExtendedCommunity> routeTargets,
      @JsonProperty(PROP_IMPORT_ROUTE_TARGETS) @Nullable SortedSet<String> importRouteTargets) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    checkArgument(rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(routeTargets != null, "Missing %s", PROP_ROUTE_TARGETS);
    checkArgument(importRouteTargets != null, "Missing %s", PROP_IMPORT_ROUTE_TARGETS);
    return new Builder()
        .setVni(vni)
        .setVrf(vrf)
        .setRouteDistinguisher(rd)
        .setRouteTargets(routeTargets)
        .setImportRouteTargets(importRouteTargets)
        .build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Layer2VniConfig)) {
      return false;
    }
    Layer2VniConfig vniConfig = (Layer2VniConfig) o;
    return _vni == vniConfig._vni
        && _vrf.equals(vniConfig._vrf)
        && _rd.equals(vniConfig._rd)
        && _routeTargets.equals(vniConfig._routeTargets)
        && _importRouteTargets.equals(vniConfig._importRouteTargets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _vrf, _rd, _routeTargets, _importRouteTargets);
  }

  @Override
  public int compareTo(Layer2VniConfig o) {
    return Comparator.comparing(Layer2VniConfig::getVni)
        .thenComparing(Layer2VniConfig::getVrf)
        .thenComparing(Layer2VniConfig::getRouteDistinguisher)
        .thenComparing(vc -> vc.getRouteTargets().toString())
        .thenComparing(vc -> vc.getImportRouteTargets().toString())
        .compare(this, o);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_VNI, _vni)
        .add(PROP_VRF, _vrf)
        .add(PROP_ROUTE_DISTINGUISHER, _rd)
        .add(PROP_ROUTE_TARGETS, _routeTargets)
        .add(PROP_IMPORT_ROUTE_TARGETS, _importRouteTargets)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    protected @Nullable Integer _vni;
    protected @Nullable String _vrf;
    protected @Nullable RouteDistinguisher _rd;
    protected @Nullable SortedSet<ExtendedCommunity> _routeTargets;
    protected @Nullable SortedSet<String> _importRouteTargets;

    private Builder() {}

    public Builder setVni(int vni) {
      _vni = vni;
      return this;
    }

    public Builder setVrf(String vrf) {
      _vrf = vrf;
      return this;
    }

    public Builder setRouteDistinguisher(RouteDistinguisher rd) {
      _rd = rd;
      return this;
    }

    /** Set a single export route target. Convenience for the common single-RT case. */
    public Builder setRouteTarget(ExtendedCommunity routeTarget) {
      _routeTargets = ImmutableSortedSet.of(routeTarget);
      return this;
    }

    public Builder setRouteTargets(SortedSet<ExtendedCommunity> routeTargets) {
      _routeTargets = ImmutableSortedSet.copyOf(routeTargets);
      return this;
    }

    /** Set a single import route target pattern. Convenience for the common single-RT case. */
    public Builder setImportRouteTarget(String importRouteTarget) {
      _importRouteTargets = ImmutableSortedSet.of(importRouteTarget);
      return this;
    }

    public Builder setImportRouteTargets(SortedSet<String> importRouteTargets) {
      _importRouteTargets = ImmutableSortedSet.copyOf(importRouteTargets);
      return this;
    }

    public Layer2VniConfig build() {
      checkArgument(_vni != null, "Missing %s", PROP_VNI);
      checkArgument(_vrf != null, "Missing %s", PROP_VRF);
      checkArgument(_rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      checkArgument(
          _routeTargets != null && !_routeTargets.isEmpty(), "Missing %s", PROP_ROUTE_TARGETS);
      SortedSet<String> importRts =
          _importRouteTargets == null || _importRouteTargets.isEmpty()
              ? ImmutableSortedSet.of(importRtPatternForAnyAs(_vni))
              : _importRouteTargets;
      return new Layer2VniConfig(_vni, _vrf, _rd, _routeTargets, importRts);
    }
  }
}
