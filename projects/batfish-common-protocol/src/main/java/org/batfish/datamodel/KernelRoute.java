package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Non-routing virtual route used for advertisement.
 *
 * <p>Implements {@link Comparable}, but {@link #compareTo(KernelRoute)} <em>should not</em> be used
 * for determining route preference in RIBs.
 */
@ParametersAreNonnullByDefault
public final class KernelRoute extends AbstractRoute implements Comparable<KernelRoute> {

  // The comparator has no impact on route preference in RIBs and should not be used as such
  private static final Comparator<KernelRoute> COMPARATOR =
      Comparator.comparing(KernelRoute::getNetwork)
          .thenComparing(KernelRoute::getNextHopIp)
          .thenComparing(KernelRoute::getNextHopInterface)
          .thenComparing(KernelRoute::getMetric)
          .thenComparing(KernelRoute::getAdministrativeCost)
          .thenComparing(KernelRoute::getTag)
          .thenComparing(KernelRoute::getNonRouting)
          .thenComparing(KernelRoute::getNonForwarding);

  /** Builder for {@link KernelRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, KernelRoute> {
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
  private static KernelRoute create(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int adminCost) {
    checkArgument(network != null, "Cannot create kernel route: missing %s", PROP_NETWORK);
    return new KernelRoute(network);
  }

  public KernelRoute(Prefix network) {
    super(network, 0, false, true);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof KernelRoute)) {
      return false;
    }
    KernelRoute rhs = (KernelRoute) o;
    return _network.equals(rhs._network);
  }

  @Override
  public int hashCode() {
    return _network.hashCode();
  }

  @Override
  public int compareTo(KernelRoute o) {
    // The comparator has no impact on route preference in RIBs and should not be used as such
    return COMPARATOR.compare(this, o);
  }

  @Override
  public Long getMetric() {
    return 0L;
  }

  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Nonnull
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
  public Builder toBuilder() {
    return builder().setNetwork(getNetwork());
  }
}
