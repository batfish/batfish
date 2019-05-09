package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
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
public final class Layer3VniConfig implements Serializable, Comparable<Layer3VniConfig> {
  private static final long serialVersionUID = 1L;
  private static final String PROP_VNI = "vni";
  private static final String PROP_VRF = "vrf";
  private static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";
  private static final String PROP_ROUTE_TARGET = "routeTarget";
  private static final String PROP_ADVERTISE_V4_UNICAST = "advertiseV4UnicastRoutes";

  private final int _vni;
  @Nonnull private final String _vrf;
  @Nonnull private final RouteDistinguisher _rd;
  @Nonnull private final ExtendedCommunity _routeTarget;
  private final boolean _advertisev4Unicast;

  public Layer3VniConfig(
      int vni,
      String vrf,
      RouteDistinguisher rd,
      ExtendedCommunity routeTarget,
      boolean advertisev4Unicast) {
    _vni = vni;
    _vrf = vrf;
    _rd = rd;
    _routeTarget = routeTarget;
    _advertisev4Unicast = advertisev4Unicast;
  }

  @JsonCreator
  private static Layer3VniConfig create(
      @Nullable @JsonProperty(PROP_VNI) Integer vni,
      @Nullable @JsonProperty(PROP_VRF) String vrf,
      @Nullable @JsonProperty(PROP_ROUTE_DISTINGUISHER) RouteDistinguisher rd,
      @Nullable @JsonProperty(PROP_ROUTE_TARGET) ExtendedCommunity routeTarget,
      @Nullable @JsonProperty(PROP_ADVERTISE_V4_UNICAST) Boolean advertisev4Unicast) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    checkArgument(rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(routeTarget != null, "Missing %s", PROP_ROUTE_TARGET);
    return new Layer3VniConfig(
        vni, vrf, rd, routeTarget, firstNonNull(advertisev4Unicast, Boolean.FALSE));
  }

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

  /**
   * Whether to advertise IPV4 unicast routes from {@link #getVrf()} as EVPN routes with the {@link
   * #getRouteDistinguisher()}
   */
  @JsonProperty(PROP_ADVERTISE_V4_UNICAST)
  public boolean getAdvertisev4Unicast() {
    return _advertisev4Unicast;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Layer3VniConfig)) {
      return false;
    }
    Layer3VniConfig vniConfig = (Layer3VniConfig) o;
    return _vni == vniConfig._vni
        && Objects.equals(_vrf, vniConfig._vrf)
        && Objects.equals(_rd, vniConfig._rd)
        && Objects.equals(_routeTarget, vniConfig._routeTarget)
        && _advertisev4Unicast == vniConfig._advertisev4Unicast;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _vrf, _rd, _routeTarget);
  }

  @Override
  public int compareTo(Layer3VniConfig o) {
    return Comparator.comparing(Layer3VniConfig::getVrf)
        .thenComparing(Layer3VniConfig::getRouteDistinguisher)
        .thenComparing(Layer3VniConfig::getRouteTarget)
        .thenComparing(Layer3VniConfig::getAdvertisev4Unicast)
        .compare(this, o);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_VNI, _vni)
        .add(PROP_VRF, _vrf)
        .add(PROP_ROUTE_DISTINGUISHER, _rd)
        .add(PROP_ROUTE_TARGET, _routeTarget)
        .add(PROP_ADVERTISE_V4_UNICAST, _advertisev4Unicast)
        .toString();
  }
}
