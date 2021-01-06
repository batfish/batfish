package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Route.UNSET_ROUTE_TAG;

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
      return new KernelRoute(getNetwork(), getTag());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  @SuppressWarnings("unused")
  private static KernelRoute create(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int adminCost,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "Cannot create kernel route: missing %s", PROP_NETWORK);
    return new KernelRoute(network, adminCost, tag);
  }

  public KernelRoute(Prefix network) {
    this(network, UNSET_ROUTE_TAG);
  }

  private KernelRoute(Prefix network, int admin, long tag) {
    super(network, admin, tag, false, true);
  }

  public KernelRoute(Prefix network, long tag) {
    this(network, 0, tag);
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

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.KERNEL;
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder().setNetwork(getNetwork()).setTag(getTag());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof KernelRoute)) {
      return false;
    }
    KernelRoute rhs = (KernelRoute) o;
    return _network.equals(rhs._network) && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    return _network.hashCode() * 31 + Long.hashCode(_tag);
  }
}
