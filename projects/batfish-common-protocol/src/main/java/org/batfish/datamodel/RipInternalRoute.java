package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RipInternalRoute extends RipRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  public RipInternalRoute(
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric) {
    super(network, nextHopIp, admin, metric);
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
        && _nextHopIp.equals(other._nextHopIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _admin, _metric, _nextHopIp);
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

  @Override
  public int getTag() {
    return NO_TAG;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return new Builder()
        .setNetwork(getNetwork())
        .setNextHopIp(getNextHopIp())
        .setAdmin(getAdministrativeCost())
        .setMetric(getMetric());
  }

  /** A {@link RipInternalRoute} builder */
  public static class Builder extends AbstractRouteBuilder<Builder, RipInternalRoute> {

    private Builder() {}

    @Nonnull
    @Override
    public RipInternalRoute build() {
      return new RipInternalRoute(getNetwork(), getNextHopIp(), getAdmin(), getMetric());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }
  }
}
