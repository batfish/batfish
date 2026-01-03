package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
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
      comparing(KernelRoute::getNetwork)
          .thenComparing(KernelRoute::getNextHopIp)
          .thenComparing(KernelRoute::getNextHopInterface)
          .thenComparing(KernelRoute::getMetric)
          .thenComparing(KernelRoute::getAdministrativeCost)
          .thenComparing(KernelRoute::getTag)
          .thenComparing(KernelRoute::getRequiredOwnedIp, nullsFirst(naturalOrder()))
          .thenComparing(KernelRoute::getNonRouting)
          .thenComparing(KernelRoute::getNonForwarding);

  /** Builder for {@link KernelRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, KernelRoute> {

    @Override
    public @Nonnull KernelRoute build() {
      return new KernelRoute(
          getNetwork(), getAdmin(), getTag(), _requiredOwnedIp, getNonForwarding());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    public @Nonnull Builder setRequiredOwnedIp(@Nullable Ip requiredOwnedIp) {
      _requiredOwnedIp = requiredOwnedIp;
      return this;
    }

    private Builder() {
      setNonForwarding(true);
    }

    private @Nullable Ip _requiredOwnedIp;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  @SuppressWarnings("unused")
  private static @Nonnull KernelRoute create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int adminCost,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_REQUIRED_OWNED_IP) @Nullable Ip requiredOwnedIp,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "Cannot create kernel route: missing %s", PROP_NETWORK);
    // Since nonForwarding is not jackson serialized, just use default value
    return new KernelRoute(network, adminCost, tag, requiredOwnedIp, true);
  }

  private KernelRoute(
      Prefix network, int admin, long tag, @Nullable Ip requiredOwnedIp, boolean nonForwarding) {
    super(network, admin, tag, false, nonForwarding);
    _requiredOwnedIp = requiredOwnedIp;
  }

  @Override
  public int compareTo(KernelRoute o) {
    // The comparator has no impact on route preference in RIBs and should not be used as such
    return COMPARATOR.compare(this, o);
  }

  @Override
  public long getMetric() {
    return 0L;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.KERNEL;
  }

  public @Nullable Ip getRequiredOwnedIp() {
    return _requiredOwnedIp;
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setNonForwarding(getNonForwarding())
        .setRequiredOwnedIp(getRequiredOwnedIp())
        .setTag(getTag());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof KernelRoute)) {
      return false;
    }
    KernelRoute rhs = (KernelRoute) o;
    return _network.equals(rhs._network)
        && _tag == rhs._tag
        && Objects.equals(_requiredOwnedIp, rhs._requiredOwnedIp)
        && getNonForwarding() == rhs.getNonForwarding();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _tag, _requiredOwnedIp, getNonForwarding());
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .omitNullValues()
        .add("_network", _network)
        .add("_tag", _tag)
        .add("_requiredOwnedIp", _requiredOwnedIp)
        .add("_nonForwarding", getNonForwarding())
        .toString();
  }

  private static final String PROP_REQUIRED_OWNED_IP = "requiredOwnedIp";

  private final @Nullable Ip _requiredOwnedIp;
}
