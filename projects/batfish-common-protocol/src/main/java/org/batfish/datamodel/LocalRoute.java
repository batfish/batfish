package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A local route. Local routes are more specific versions of interface (i.e., connected) routes.
 * They are {@link Prefix#MAX_PREFIX_LENGTH} routes to the interface's IP address.
 */
@ParametersAreNonnullByDefault
public final class LocalRoute extends AbstractRoute {

  private static final long serialVersionUID = 1L;
  private static final String PROP_SOURCE_PREFIX_LENGTH = "sourcePrefixLength";

  private final String _nextHopInterface;
  private final int _sourcePrefixLength;

  public LocalRoute(InterfaceAddress interfaceAddress, String nextHopInterface) {
    this(
        Prefix.create(interfaceAddress.getIp(), Prefix.MAX_PREFIX_LENGTH),
        nextHopInterface,
        interfaceAddress.getNetworkBits(),
        0);
  }

  @VisibleForTesting
  LocalRoute(Prefix network, String nextHopInterface, int sourcePrefixLength, int admin) {
    super(network, admin, false, false);
    _nextHopInterface = nextHopInterface;
    _sourcePrefixLength = sourcePrefixLength;
  }

  @JsonCreator
  private static LocalRoute create(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_SOURCE_PREFIX_LENGTH) int sourcePrefixLength,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin) {
    checkArgument(network != null, "LocalRoute missing %s", PROP_NETWORK);
    return new LocalRoute(
        network,
        firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
        sourcePrefixLength,
        admin);
  }

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
        && _nextHopInterface.equals(rhs._nextHopInterface)
        && _sourcePrefixLength == rhs._sourcePrefixLength;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network,
        _admin,
        getNonRouting(),
        getNonForwarding(),
        _nextHopInterface,
        _sourcePrefixLength);
  }

  @Override
  public Long getMetric() {
    return 0L;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nonnull
  @Override
  public Ip getNextHopIp() {
    return Route.UNSET_ROUTE_NEXT_HOP_IP;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.LOCAL;
  }

  @JsonProperty(PROP_SOURCE_PREFIX_LENGTH)
  public int getSourcePrefixLength() {
    return _sourcePrefixLength;
  }

  @Override
  public int getTag() {
    return Route.UNSET_ROUTE_TAG;
  }

  @Override
  public AbstractRouteBuilder<Builder, LocalRoute> toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAdmin(getAdministrativeCost())
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setNextHopInterface(_nextHopInterface)
        .setSourcePrefixLength(_sourcePrefixLength);
  }

  /** Builder for {@link org.batfish.datamodel.LocalRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, LocalRoute> {

    @Nullable private String _nextHopInterface;
    @Nullable private Integer _sourcePrefixLength;

    @Nonnull
    @Override
    public LocalRoute build() {
      checkArgument(getNetwork() != null, "LocalRoute missing %s", PROP_NETWORK);
      checkArgument(
          _sourcePrefixLength != null, "LocalRoute missing %s", PROP_SOURCE_PREFIX_LENGTH);
      return new LocalRoute(
          getNetwork(),
          firstNonNull(_nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
          _sourcePrefixLength,
          getAdmin());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setNextHopInterface(@Nullable String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
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
}
