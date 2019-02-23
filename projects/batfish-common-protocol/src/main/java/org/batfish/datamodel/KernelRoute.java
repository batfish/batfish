package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class KernelRoute extends AbstractRoute {

  /** Builder for {@link KernelRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, KernelRoute> {
    private Builder() {}

    @Nonnull
    @Override
    public KernelRoute build() {
      return new KernelRoute(getNetwork());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }
  }

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull KernelRoute create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer adminCost,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface) {
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    return new KernelRoute(network);
  }

  public KernelRoute(Prefix network) {
    super(network, 0, true, true);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KernelRoute)) {
      return false;
    }
    return _network.equals(((KernelRoute) o)._network);
  }

  @Override
  public Long getMetric() {
    return 0L;
  }

  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Override
  public Ip getNextHopIp() {
    return Route.UNSET_ROUTE_NEXT_HOP_IP;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.KERNEL;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @Override
  public int hashCode() {
    return _network.hashCode();
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    return 0;
  }

  @Override
  public AbstractRouteBuilder<?, ?> toBuilder() {
    return builder().setNetwork(_network);
  }
}
