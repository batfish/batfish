package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** A Layer 3 {@link VniConfig}. */
@ParametersAreNonnullByDefault
public final class Layer3VniConfig extends VniConfig
    implements Serializable, Comparable<Layer3VniConfig> {

  private static final String PROP_ADVERTISE_V4_UNICAST = "advertiseV4Unicast";

  private final boolean _advertiseV4Unicast;

  private Layer3VniConfig(
      int vni,
      String vrf,
      RouteDistinguisher rd,
      ExtendedCommunity routeTarget,
      String importRouteTarget,
      boolean advertiseV4Unicast) {
    super(vni, vrf, rd, routeTarget, importRouteTarget);
    _advertiseV4Unicast = advertiseV4Unicast;
  }

  @JsonCreator
  private static Layer3VniConfig create(
      @JsonProperty(PROP_VNI) @Nullable Integer vni,
      @JsonProperty(PROP_VRF) @Nullable String vrf,
      @JsonProperty(PROP_ROUTE_DISTINGUISHER) @Nullable RouteDistinguisher rd,
      @JsonProperty(PROP_ROUTE_TARGET) @Nullable ExtendedCommunity routeTarget,
      @JsonProperty(PROP_IMPORT_ROUTE_TARGET) @Nullable String importRouteTarget,
      @JsonProperty(PROP_ADVERTISE_V4_UNICAST) @Nullable Boolean advertiseV4Unicast) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    checkArgument(rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(routeTarget != null, "Missing %s", PROP_ROUTE_TARGET);
    checkArgument(importRouteTarget != null, "Missing %s", PROP_ROUTE_TARGET);
    return new Builder()
        .setVni(vni)
        .setVrf(vrf)
        .setRouteDistinguisher(rd)
        .setRouteTarget(routeTarget)
        .setImportRouteTarget(importRouteTarget)
        .setAdvertiseV4Unicast(firstNonNull(advertiseV4Unicast, Boolean.FALSE))
        .build();
  }

  /**
   * Whether to advertise IPV4 unicast routes from {@link #getVrf()} as EVPN routes with the {@link
   * #getRouteDistinguisher()}
   */
  @JsonProperty(PROP_ADVERTISE_V4_UNICAST)
  public boolean getAdvertiseV4Unicast() {
    return _advertiseV4Unicast;
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
        && Objects.equals(_importRouteTarget, vniConfig._importRouteTarget)
        && _advertiseV4Unicast == vniConfig._advertiseV4Unicast;
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
        .thenComparing(Layer3VniConfig::getAdvertiseV4Unicast)
        .compare(this, o);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_VNI, _vni)
        .add(PROP_VRF, _vrf)
        .add(PROP_ROUTE_DISTINGUISHER, _rd)
        .add(PROP_ROUTE_TARGET, _routeTarget)
        .add(PROP_ADVERTISE_V4_UNICAST, _advertiseV4Unicast)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private @Nullable Integer _vni;
    private @Nullable String _vrf;
    private @Nullable RouteDistinguisher _rd;
    private @Nullable ExtendedCommunity _routeTarget;
    private @Nullable String _importRouteTarget;
    private boolean _advertiseV4Unicast;

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

    public Builder setAdvertiseV4Unicast(boolean advertisev4Unicast) {
      _advertiseV4Unicast = advertisev4Unicast;
      return this;
    }

    public Layer3VniConfig build() {
      checkArgument(_vni != null, "Missing %s", PROP_VNI);
      checkArgument(_vrf != null, "Missing %s", PROP_VRF);
      checkArgument(_rd != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      checkArgument(_routeTarget != null, "Missing %s", PROP_ROUTE_TARGET);
      String importRt = firstNonNull(_importRouteTarget, importRtPatternForAnyAs(_vni));
      // check pattern for validity
      try {
        Pattern.compile(importRt);
      } catch (PatternSyntaxException e) {
        throw new IllegalArgumentException(
            String.format("Invalid patthern %s for %s", importRt, PROP_IMPORT_ROUTE_TARGET));
      }
      return new Layer3VniConfig(_vni, _vrf, _rd, _routeTarget, importRt, _advertiseV4Unicast);
    }
  }
}
