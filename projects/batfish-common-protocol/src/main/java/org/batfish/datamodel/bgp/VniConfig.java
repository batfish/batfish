package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/**
 * Configuration for how to advertise a specific VNI (i.e., which route distinguisher and route
 * targets to use).
 */
@ParametersAreNonnullByDefault
public abstract class VniConfig implements Serializable {

  public static final String PROP_VNI = "vni";
  public static final String PROP_VRF = "vrf";
  public static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";
  public static final String PROP_ROUTE_TARGET = "routeTarget";
  public static final String PROP_IMPORT_ROUTE_TARGET = "importRouteTarget";

  protected final int _vni;
  protected final @Nonnull String _vrf;
  protected final @Nonnull RouteDistinguisher _rd;
  protected final @Nonnull ExtendedCommunity _routeTarget;
  protected final @Nonnull String _importRouteTarget;

  protected VniConfig(
      int vni,
      String vrf,
      RouteDistinguisher rd,
      ExtendedCommunity routeTarget,
      String importRouteTarget) {
    _vni = vni;
    _vrf = vrf;
    _rd = rd;
    _routeTarget = routeTarget;
    _importRouteTarget = importRouteTarget;
  }

  /** Return an import route target pattern equivalent to "*:VNI" */
  public static @Nonnull String importRtPatternForAnyAs(int vni) {
    checkArgument(vni > 0 && vni < 1 << 24, "VNI value %d is not in the valid range 1-16777215");
    return String.format("^\\d+:%d$", vni);
  }

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }

  /** The VRF to which this VNI belongs */
  @JsonProperty(PROP_VRF)
  public @Nonnull String getVrf() {
    return _vrf;
  }

  /** {@link RouteDistinguisher} to use when advertising this VNI */
  @JsonProperty(PROP_ROUTE_DISTINGUISHER)
  public @Nonnull RouteDistinguisher getRouteDistinguisher() {
    return _rd;
  }

  /** Route target to use when advertising this VNI (i.e., the export route target) */
  @JsonProperty(PROP_ROUTE_TARGET)
  public @Nonnull ExtendedCommunity getRouteTarget() {
    return _routeTarget;
  }

  /** The import route target pattern. Can be compiled into a {@link Pattern} */
  public @Nonnull String getImportRouteTarget() {
    return _importRouteTarget;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(@Nullable Object obj);
}
