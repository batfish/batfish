package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;

@ParametersAreNonnullByDefault
public class RipInternalRoute extends RipRoute {

  @JsonCreator
  private static RipInternalRoute create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable int admin,
      @JsonProperty(PROP_METRIC) @Nullable long metric,
      @JsonProperty(PROP_TAG) @Nullable long tag) {
    checkArgument(network != null);
    checkArgument(nextHopIp != null);
    return new RipInternalRoute(network, NextHopIp.of(nextHopIp), admin, metric, tag);
  }

  private RipInternalRoute(Prefix network, NextHop nextHop, int admin, long metric, long tag) {
    super(network, nextHop, admin, metric, tag);
  }

  @VisibleForTesting
  RipInternalRoute(Prefix network, Ip nextHopIp, int admin, long metric, long tag) {
    super(network, NextHopIp.of(nextHopIp), admin, metric, tag);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.RIP;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** A {@link RipInternalRoute} builder */
  public static class Builder extends AbstractRouteBuilder<Builder, RipInternalRoute> {

    private Builder() {}

    @Override
    public @Nonnull RipInternalRoute build() {
      return new RipInternalRoute(getNetwork(), _nextHop, getAdmin(), getMetric(), getTag());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return new Builder()
        .setAdmin(getAdministrativeCost())
        .setMetric(getMetric())
        .setNetwork(getNetwork())
        .setNextHop(getNextHop())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setTag(getTag());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RipRoute)) {
      return false;
    }
    RipRoute other = (RipRoute) o;
    return _network.equals(other._network)
        && _admin == other._admin
        && _metric == other._metric
        && _nextHop.equals(other._nextHop)
        && getNonForwarding() == other.getNonForwarding()
        && getNonRouting() == other.getNonRouting()
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network, _admin, _metric, _nextHop, getNonForwarding(), getNonRouting(), _tag);
  }
}
