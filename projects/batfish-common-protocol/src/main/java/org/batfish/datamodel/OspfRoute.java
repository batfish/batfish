package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A generic OSPF route */
@ParametersAreNonnullByDefault
public abstract class OspfRoute extends AbstractRoute {

  private static final long serialVersionUID = 1L;
  /** Indicate that this route has no defined area (e.g., external routes) */
  public static final long NO_AREA = -1L;

  protected static final String PROP_AREA = "area";

  protected final long _area;
  protected final long _metric;
  @Nonnull protected final Ip _nextHopIp;

  protected OspfRoute(
      Prefix network,
      @Nullable Ip nextHopIp,
      int admin,
      long metric,
      long area,
      boolean nonRouting,
      boolean nonForwarding) {
    super(network, admin, nonRouting, nonForwarding);
    _metric = metric;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _area = area;
  }

  /** The route's area number */
  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  @Override
  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  public final Long getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Nonnull
  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  public final Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  @Nonnull
  public abstract RoutingProtocol getProtocol();

  @Override
  public int getTag() {
    return NO_TAG;
  }
}
