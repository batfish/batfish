package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
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
      ExtendedCommunity routeTarget,
      String importRouteTarget) {
    super(vni, vrf, rd, routeTarget, importRouteTarget);
  }

  @JsonCreator
  private static Layer2VniConfig create(
      @JsonProperty(PROP_VNI) @Nullable Integer vni,
      @JsonProperty(PROP_VRF) @Nullable String vrf,
      @JsonProperty(PROP_ROUTE_DISTINGUISHER) @Nullable RouteDistinguisher rd,
      @JsonProperty(PROP_ROUTE_TARGET) @Nullable ExtendedCommunity routeTarget,
      @JsonProperty(PROP_IMPORT_ROUTE_TARGET) @Nullable String importRouteTarget) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    checkArgument(rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(routeTarget != null, "Missing %s", PROP_ROUTE_TARGET);
    checkArgument(importRouteTarget != null, "Missing %s", PROP_IMPORT_ROUTE_TARGET);
    return new Layer2VniConfig(vni, vrf, rd, routeTarget, importRouteTarget);
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
        && _routeTarget.equals(vniConfig._routeTarget)
        && _importRouteTarget.equals(vniConfig._importRouteTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _vrf, _rd, _routeTarget, _importRouteTarget);
  }

  @Override
  public int compareTo(Layer2VniConfig o) {
    return Comparator.comparing(Layer2VniConfig::getVni)
        .thenComparing(Layer2VniConfig::getVrf)
        .thenComparing(Layer2VniConfig::getRouteDistinguisher)
        .thenComparing(Layer2VniConfig::getRouteTarget)
        .thenComparing(Layer2VniConfig::getImportRouteTarget)
        .compare(this, o);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_VNI, _vni)
        .add(PROP_VRF, _vrf)
        .add(PROP_ROUTE_DISTINGUISHER, _rd)
        .add(PROP_ROUTE_TARGET, _routeTarget)
        .add(PROP_IMPORT_ROUTE_TARGET, _importRouteTarget)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    protected @Nullable Integer _vni;
    protected @Nullable String _vrf;
    protected @Nullable RouteDistinguisher _rd;
    protected @Nullable ExtendedCommunity _routeTarget;
    protected @Nullable String _importRouteTarget;

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

    public Builder setRouteTarget(ExtendedCommunity routeTarget) {
      _routeTarget = routeTarget;
      return this;
    }

    public Builder setImportRouteTarget(String importRouteTarget) {
      _importRouteTarget = importRouteTarget;
      return this;
    }

    public Layer2VniConfig build() {
      checkArgument(_vni != null, "Missing %s", PROP_VNI);
      checkArgument(_vrf != null, "Missing %s", PROP_VRF);
      checkArgument(_rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      checkArgument(_routeTarget != null, "Missing %s", PROP_ROUTE_TARGET);
      return new Layer2VniConfig(
          _vni,
          _vrf,
          _rd,
          _routeTarget,
          firstNonNull(_importRouteTarget, importRtPatternForAnyAs(_vni)));
    }
  }
}
