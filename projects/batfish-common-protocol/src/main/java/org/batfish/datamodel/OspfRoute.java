package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;

public abstract class OspfRoute extends AbstractRoute {

  /** */
  private static final long serialVersionUID = 1L;

  protected final int _admin;

  protected final int _metric;

  protected final Ip _nextHopIp;

  @JsonCreator
  public OspfRoute(
      @JsonProperty(NETWORK_VAR) Prefix network,
      @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
      @JsonProperty(ADMINISTRATIVE_COST_VAR) int admin,
      @JsonProperty(METRIC_VAR) int metric) {
    super(network);
    _admin = admin;
    _metric = metric;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
  }

  @JsonIgnore(false)
  @JsonProperty(ADMINISTRATIVE_COST_VAR)
  @Override
  public final int getAdministrativeCost() {
    return _admin;
  }

  @JsonIgnore(false)
  @JsonProperty(METRIC_VAR)
  @Override
  public final Integer getMetric() {
    return _metric;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(NEXT_HOP_IP_VAR)
  @Override
  public final Ip getNextHopIp() {
    return _nextHopIp;
  }
}
