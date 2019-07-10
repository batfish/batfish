package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/**
 * Configuration for how to advertise a specific VNI (i.e., which route distinguisher and route
 * targets to use).
 */
@ParametersAreNonnullByDefault
public abstract class VniConfig implements Serializable {

  public static final String PROP_VNI = "vni";
  public static final String PROP_VRF = "vrf";
  public static final String PROP_ADVERTISED_SOURCE_ADDRESS = "advertisedSourceAddress";
  public static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";
  public static final String PROP_ROUTE_TARGET = "routeTarget";
  public static final String PROP_IMPORT_ROUTE_TARGET = "importRouteTarget";

  protected final int _vni;
  @Nonnull protected final String _vrf;
  @Nullable protected final Ip _advertisedSourceAddress;
  @Nonnull protected final RouteDistinguisher _rd;
  @Nonnull protected final ExtendedCommunity _routeTarget;
  @Nonnull protected final String _importRouteTarget;

  protected VniConfig(
      int vni,
      String vrf,
      RouteDistinguisher rd,
      @Nullable Ip advertisedSourceAddress,
      ExtendedCommunity routeTarget,
      String importRouteTarget) {
    _vni = vni;
    _vrf = vrf;
    _rd = rd;
    _advertisedSourceAddress = advertisedSourceAddress;
    _routeTarget = routeTarget;
    _importRouteTarget = importRouteTarget;
  }

  /** Return an import route target pattern equivalent to "*:VNI" */
  @Nonnull
  public static String importRtPatternForAnyAs(int vni) {
    checkArgument(vni > 0 && vni < 1 << 24, "VNI value %d is not in the valid range 1-16777215");
    return String.format("^\\d+:%d$", vni);
  }

  @JsonProperty(PROP_VNI)
  public final int getVni() {
    return _vni;
  }

  /** The VRF to which this VNI belongs */
  @Nonnull
  @JsonProperty(PROP_VRF)
  public final String getVrf() {
    return _vrf;
  }

  /**
   * Overrides which IP address to advertise for this VNI. If {@code null}, the {@link
   * org.batfish.datamodel.VniSettings#getSourceAddress()} should be used by default
   */
  @Nullable
  @JsonProperty(PROP_ADVERTISED_SOURCE_ADDRESS)
  public final Ip getAdvertisedSourceAddress() {
    return _advertisedSourceAddress;
  }

  /** {@link RouteDistinguisher} to use when advertising this VNI */
  @Nonnull
  @JsonProperty(PROP_ROUTE_DISTINGUISHER)
  public final RouteDistinguisher getRouteDistinguisher() {
    return _rd;
  }

  /** Route target to use when advertising this VNI (i.e., the export route target) */
  @Nonnull
  @JsonProperty(PROP_ROUTE_TARGET)
  public final ExtendedCommunity getRouteTarget() {
    return _routeTarget;
  }

  /** The import route target pattern. Can be compiled into a {@link Pattern} */
  @Nonnull
  @JsonProperty(PROP_IMPORT_ROUTE_TARGET)
  public final String getImportRouteTarget() {
    return _importRouteTarget;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(@Nullable Object obj);
}
