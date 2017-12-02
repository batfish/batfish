package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class OspfRoute extends AbstractRoute {

  public static final long NO_AREA = -1L;

  protected static final String PROP_AREA = "area";

  /** */
  private static final long serialVersionUID = 1L;

  protected final int _admin;

  protected final long _area;

  protected final long _metric;

  protected final Ip _nextHopIp;

  @JsonCreator
  public OspfRoute(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_AREA) long area) {
    super(network);
    _admin = admin;
    _metric = metric;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _area = area;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_ADMINISTRATIVE_COST)
  @Override
  public final int getAdministrativeCost() {
    return _admin;
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public final Long getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Override
  public final Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  public abstract RoutingProtocol getProtocol();

  @Override
  public int getTag() {
    return NO_TAG;
  }
}
