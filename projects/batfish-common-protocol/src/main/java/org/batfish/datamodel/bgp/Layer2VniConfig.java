package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/**
 * Configuration for how to advertise a specific VNI (i.e., which route distinguisher and route
 * targets to use).
 */
@ParametersAreNonnullByDefault
public class Layer2VniConfig implements Serializable, Comparable<Layer2VniConfig> {
  private static final long serialVersionUID = 1L;
  private static final String PROP_VNI = "vni";
  private static final String PROP_VRF = "vrf";
  private static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";
  private static final String PROP_ROUTE_TARGET = "routeTarget";

  private final int _vni;
  @Nonnull private final String _vrf;
  @Nonnull private final RouteDistinguisher _rd;
  @Nonnull private final ExtendedCommunity _routeTarget;

  public Layer2VniConfig(
      int vni, String vrf, RouteDistinguisher rd, ExtendedCommunity routeTarget) {
    _vni = vni;
    _vrf = vrf;
    _rd = rd;
    _routeTarget = routeTarget;
  }

  @JsonCreator
  private static Layer2VniConfig create(
      @Nullable @JsonProperty(PROP_VNI) Integer vni,
      @Nullable @JsonProperty(PROP_VRF) String vrf,
      @Nullable @JsonProperty(PROP_ROUTE_DISTINGUISHER) RouteDistinguisher rd,
      @Nullable @JsonProperty(PROP_ROUTE_TARGET) ExtendedCommunity routeTarget) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    checkArgument(rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(routeTarget != null, "Missing %s", PROP_ROUTE_TARGET);
    return new Layer2VniConfig(vni, vrf, rd, routeTarget);
  }

  /** The layer 2 VNI number */
  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }

  /** The VRF to which this VNI belongs */
  @Nonnull
  @JsonProperty(PROP_VRF)
  public String getVrf() {
    return _vrf;
  }

  /** {@link RouteDistinguisher} to use when advertising this VNI */
  @Nonnull
  @JsonProperty(PROP_ROUTE_DISTINGUISHER)
  public RouteDistinguisher getRouteDistinguisher() {
    return _rd;
  }

  /** Route target to use when advertising this VNI */
  @Nonnull
  @JsonProperty(PROP_ROUTE_TARGET)
  public ExtendedCommunity getRouteTarget() {
    return _routeTarget;
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
        && Objects.equals(_vrf, vniConfig._vrf)
        && Objects.equals(_rd, vniConfig._rd)
        && Objects.equals(_routeTarget, vniConfig._routeTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _rd, _routeTarget);
  }

  @Override
  public int compareTo(Layer2VniConfig o) {
    return Comparator.comparing(Layer2VniConfig::getVni)
        .thenComparing(Layer2VniConfig::getRouteDistinguisher)
        .thenComparing(Layer2VniConfig::getRouteTarget)
        .compare(this, o);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_VNI, _vni)
        .add(PROP_VRF, _vrf)
        .add(PROP_ROUTE_DISTINGUISHER, _rd)
        .add(PROP_ROUTE_TARGET, _routeTarget)
        .toString();
  }
}
