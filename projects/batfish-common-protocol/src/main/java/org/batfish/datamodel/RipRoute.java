package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class RipRoute extends AbstractRoute {

  private static final long serialVersionUID = 1L;

  /** Maximum allowable route metric in RIP */
  public static final long MAX_ROUTE_METRIC = 16;

  protected final long _metric;
  @Nonnull protected final Ip _nextHopIp;

  @JsonCreator
  protected RipRoute(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric) {
    super(network, admin, false, false);
    checkArgument(
        metric >= 0 && metric <= MAX_ROUTE_METRIC,
        "Invalid RIP route metric %d. Must be between 0 and %d",
        metric,
        MAX_ROUTE_METRIC);
    _metric = metric;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public final Long getMetric() {
    return _metric;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Override
  public final Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nullable
  @Override
  public String getNextVrf() {
    return null;
  }
}
