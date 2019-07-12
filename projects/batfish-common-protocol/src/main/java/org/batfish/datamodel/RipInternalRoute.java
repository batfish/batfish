package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RipInternalRoute extends RipRoute {

  @JsonCreator
  public RipInternalRoute(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_TAG) long tag) {
    super(network, nextHopIp, admin, metric, tag);
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
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

    @Nonnull
    @Override
    public RipInternalRoute build() {
      return new RipInternalRoute(getNetwork(), getNextHopIp(), getAdmin(), getMetric(), getTag());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
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
        .setNextHopIp(getNextHopIp())
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
        && _nextHopIp.equals(other._nextHopIp)
        && getNonForwarding() == other.getNonForwarding()
        && getNonRouting() == other.getNonRouting()
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network, _admin, _metric, _nextHopIp, getNonForwarding(), getNonRouting(), _tag);
  }
}
