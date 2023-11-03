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
import org.batfish.datamodel.route.nh.NextHopInterface;

/**
 * A local route. Local routes are more specific versions of interface (i.e., connected) routes.
 * They are {@link Prefix#MAX_PREFIX_LENGTH} routes to the interface's IP address.
 */
@ParametersAreNonnullByDefault
public final class LocalRoute extends AbstractRoute {

  private static final String PROP_SOURCE_PREFIX_LENGTH = "sourcePrefixLength";

  private final int _sourcePrefixLength;

  public LocalRoute(ConcreteInterfaceAddress interfaceAddress, String nextHopInterface) {
    this(
        interfaceAddress.getIp().toPrefix(),
        NextHopInterface.of(nextHopInterface),
        interfaceAddress.getNetworkBits(),
        0,
        Route.UNSET_ROUTE_TAG);
  }

  @VisibleForTesting
  LocalRoute(Prefix network, String nextHopInterface, int sourcePrefixLength, int admin, long tag) {
    this(network, NextHopInterface.of(nextHopInterface), sourcePrefixLength, admin, tag);
  }

  private LocalRoute(Prefix network, NextHop nextHop, int sourcePrefixLength, int admin, long tag) {
    super(network, admin, tag, false, false);
    _nextHop = nextHop;
    _sourcePrefixLength = sourcePrefixLength;
  }

  @JsonCreator
  @SuppressWarnings("unused")
  private static LocalRoute create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_SOURCE_PREFIX_LENGTH) int sourcePrefixLength,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "LocalRoute missing %s", PROP_NETWORK);
    checkArgument(nextHopInterface != null, "LocalRoute missing %s", PROP_NEXT_HOP_INTERFACE);
    return new LocalRoute(
        network, NextHopInterface.of(nextHopInterface), sourcePrefixLength, admin, tag);
  }

  @Override
  public long getMetric() {
    return 0L;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.LOCAL;
  }

  @JsonProperty(PROP_SOURCE_PREFIX_LENGTH)
  public int getSourcePrefixLength() {
    return _sourcePrefixLength;
  }

  /** Builder for {@link org.batfish.datamodel.LocalRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, LocalRoute> {

    private @Nullable Integer _sourcePrefixLength;

    @Override
    public @Nonnull LocalRoute build() {
      checkArgument(getNetwork() != null, "LocalRoute missing %s", PROP_NETWORK);
      checkArgument(
          _sourcePrefixLength != null, "LocalRoute missing %s", PROP_SOURCE_PREFIX_LENGTH);
      checkArgument(_nextHop != null, "LocalRoute missing next hop");
      return new LocalRoute(getNetwork(), _nextHop, _sourcePrefixLength, getAdmin(), getTag());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    public Builder setSourcePrefixLength(int prefixLength) {
      _sourcePrefixLength = prefixLength;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public AbstractRouteBuilder<Builder, LocalRoute> toBuilder() {
    return builder()
        .setNetwork(_network)
        .setAdmin(_admin)
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setNextHop(_nextHop)
        .setSourcePrefixLength(_sourcePrefixLength)
        .setTag(_tag);
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof LocalRoute)) {
      return false;
    }
    LocalRoute rhs = (LocalRoute) o;
    return _network.equals(rhs._network)
        && _admin == rhs._admin
        && getNonRouting() == rhs.getNonRouting()
        && getNonForwarding() == rhs.getNonForwarding()
        && _nextHop.equals(rhs._nextHop)
        && _sourcePrefixLength == rhs._sourcePrefixLength
        && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network, _admin, getNonRouting(), getNonForwarding(), _nextHop, _sourcePrefixLength, _tag);
  }
}
