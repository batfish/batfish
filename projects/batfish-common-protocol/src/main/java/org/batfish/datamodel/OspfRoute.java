package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;

/** A generic OSPF route */
@ParametersAreNonnullByDefault
public abstract class OspfRoute extends AbstractRoute {

  /** Indicate that this route has no defined area (e.g., external routes) */
  public static final long NO_AREA = -1L;

  protected static final String PROP_AREA = "area";

  protected final long _area;
  protected final long _metric;

  protected OspfRoute(
      Prefix network,
      NextHop nextHop,
      int admin,
      long metric,
      long area,
      long tag,
      boolean nonRouting,
      boolean nonForwarding) {
    super(network, admin, tag, nonRouting, nonForwarding);
    _metric = metric;
    _nextHop = nextHop;
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

  @Override
  @Nonnull
  public abstract RoutingProtocol getProtocol();
}
