package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;

/** A generic OSPF route */
@ParametersAreNonnullByDefault
public abstract class OspfRoute extends AbstractRoute {

  /** Indicate that this route has no defined area (e.g., external routes) */
  public static final long NO_AREA = 0xFFFFFFFFL;

  protected static final String PROP_AREA = "area";

  // Stored as unsigned 32-bit int for memory savings. Use getArea() to read.
  private final int _area;
  // Stored as unsigned 32-bit int for memory savings. Use getMetric() to read.
  private final int _metric;

  protected OspfRoute(
      Prefix network,
      NextHop nextHop,
      long admin,
      long metric,
      long area,
      long tag,
      boolean nonRouting,
      boolean nonForwarding) {
    super(network, admin, tag, nonRouting, nonForwarding);
    checkArgument(
        nonRouting || !(nextHop instanceof NextHopIp),
        "OSPF routes cannot only have next-hop IP unless they are non-routing.");
    checkArgument(
        !(nextHop instanceof NextHopInterface) || ((NextHopInterface) nextHop).getIp() != null,
        "OSPF routes with next-hop interface must have next-hop IP.");
    _metric = (int) metric;
    _nextHop = nextHop;
    _area = (int) area;
  }

  /** The route's area number */
  @JsonProperty(PROP_AREA)
  public long getArea() {
    return Integer.toUnsignedLong(_area);
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  public final @Nonnull long getMetric() {
    return Integer.toUnsignedLong(_metric);
  }

  @Override
  public abstract @Nonnull RoutingProtocol getProtocol();
}
