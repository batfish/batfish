package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A static route */
public class StaticRoute extends AbstractRoute {
  static final long DEFAULT_STATIC_ROUTE_METRIC = 0L;

  private static final String PROP_NEXT_HOP_INTERFACE = "nextHopInterface";

  private static final long serialVersionUID = 1L;

  private final int _administrativeCost;

  private final long _metric;

  @Nonnull private final String _nextHopInterface;

  @Nonnull private final Ip _nextHopIp;

  private final int _tag;

  @JsonCreator
  private static StaticRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int administrativeCost,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_TAG) int tag) {
    return new StaticRoute(
        requireNonNull(network),
        nextHopIp,
        nextHopInterface,
        administrativeCost,
        metric,
        tag,
        false,
        false);
  }

  private StaticRoute(
      @Nonnull Prefix network,
      @Nullable Ip nextHopIp,
      @Nullable String nextHopInterface,
      int administrativeCost,
      long metric,
      int tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network);
    setNonForwarding(nonForwarding);
    setNonRouting(nonRouting);
    checkArgument(
        administrativeCost >= 0, "Invalid admin distance for static route: %d", administrativeCost);
    _administrativeCost = administrativeCost;
    _metric = metric;
    _nextHopInterface = firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE);
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _tag = tag;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute rhs = (StaticRoute) o;
    return _administrativeCost == rhs._administrativeCost
        && _tag == rhs._tag
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting()
        && Objects.equals(_network, rhs._network)
        && Objects.equals(_nextHopIp, rhs._nextHopIp)
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface);
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_ADMINISTRATIVE_COST)
  public int getAdministrativeCost() {
    return _administrativeCost;
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  public Long getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Override
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.STATIC;
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_TAG)
  public int getTag() {
    return _tag;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _administrativeCost,
        getNonForwarding(),
        getNonRouting(),
        _metric,
        _network,
        _nextHopInterface,
        _nextHopIp,
        _tag);
  }

  @Override
  protected final String protocolRouteString() {
    return " tag:" + _tag;
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    return 0;
  }

  /** Builder for {@link StaticRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, StaticRoute> {

    private int _administrativeCost = Route.UNSET_ROUTE_ADMIN;
    private String _nextHopInterface = Route.UNSET_NEXT_HOP_INTERFACE;

    private Builder() {}

    @Override
    public StaticRoute build() {
      return new StaticRoute(
          getNetwork(),
          getNextHopIp(),
          _nextHopInterface,
          _administrativeCost,
          getMetric(),
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAdministrativeCost(int administrativeCost) {
      _administrativeCost = administrativeCost;
      return this;
    }

    public Builder setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return this;
    }
  }
}
