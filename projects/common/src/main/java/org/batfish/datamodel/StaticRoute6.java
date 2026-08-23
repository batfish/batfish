package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IPv6 static route. */
@ParametersAreNonnullByDefault
public final class StaticRoute6 extends AbstractRoute6
    implements Comparable<StaticRoute6> {

  public static final long DEFAULT_ADMINISTRATIVE_COST = 1L;
  public static final long DEFAULT_METRIC = 0L;

  @JsonCreator
  private static StaticRoute6 create(
      @JsonProperty(PROP_NETWORK)
          @Nullable Prefix6 network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE)
          @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP)
          @Nullable Ip6 nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST)
          @Nullable Long administrativeCost,
      @JsonProperty(PROP_METRIC)
          @Nullable Long metric,
      @JsonProperty(PROP_TAG)
          @Nullable Long tag) {
    return new StaticRoute6(
        requireNonNull(network),
        nextHopInterface == null
            ? Route.UNSET_NEXT_HOP_INTERFACE
            : nextHopInterface,
        nextHopIp,
        administrativeCost == null
            ? DEFAULT_ADMINISTRATIVE_COST
            : administrativeCost,
        metric == null
            ? DEFAULT_METRIC
            : metric,
        tag == null
            ? Route.UNSET_ROUTE_TAG
            : tag);
  }

  private StaticRoute6(
      Prefix6 network,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp,
      long administrativeCost,
      long metric,
      long tag) {
    super(
        network,
        administrativeCost,
        tag,
        false,
        false,
        nextHopInterface,
        nextHopIp);

    checkArgument(
        metric >= 0 && metric <= 0xFFFFFFFFL,
        "Invalid IPv6 static-route metric %s",
        metric);

    _metric = metric;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int compareTo(StaticRoute6 rhs) {
    return COMPARATOR.compare(this, rhs);
  }

  @Override
  @JsonProperty(PROP_METRIC)
  public long getMetric() {
    return _metric;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.STATIC;
  }

  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setNextHopInterface(
            getNextHopInterface())
        .setNextHopIp(getNextHopIp())
        .setAdministrativeCost(
            getAdministrativeCost())
        .setMetric(getMetric())
        .setTag(getTag());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StaticRoute6)) {
      return false;
    }

    StaticRoute6 rhs =
        (StaticRoute6) o;

    return getNetwork().equals(
            rhs.getNetwork())
        && getNextHopInterface().equals(
            rhs.getNextHopInterface())
        && Objects.equals(
            getNextHopIp(),
            rhs.getNextHopIp())
        && getAdministrativeCost()
            == rhs.getAdministrativeCost()
        && getMetric() == rhs.getMetric()
        && getTag() == rhs.getTag();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getNetwork(),
        getNextHopInterface(),
        getNextHopIp(),
        getAdministrativeCost(),
        getMetric(),
        getTag());
  }

  public static final class Builder {

    private long _administrativeCost =
        DEFAULT_ADMINISTRATIVE_COST;
    private long _metric = DEFAULT_METRIC;
    private @Nullable Prefix6 _network;
    private @Nonnull String _nextHopInterface =
        Route.UNSET_NEXT_HOP_INTERFACE;
    private @Nullable Ip6 _nextHopIp;
    private long _tag = Route.UNSET_ROUTE_TAG;

    private Builder() {}

    public StaticRoute6 build() {
      checkArgument(
          _network != null,
          "IPv6 static route missing network");

      checkArgument(
          _nextHopIp != null
              || !Route.UNSET_NEXT_HOP_INTERFACE
                  .equals(_nextHopInterface),
          "IPv6 static route missing next hop");

      return new StaticRoute6(
          _network,
          _nextHopInterface,
          _nextHopIp,
          _administrativeCost,
          _metric,
          _tag);
    }

    public Builder setAdministrativeCost(
        long administrativeCost) {
      _administrativeCost =
          administrativeCost;
      return this;
    }

    public Builder setMetric(long metric) {
      _metric = metric;
      return this;
    }

    public Builder setNetwork(
        Prefix6 network) {
      _network = network;
      return this;
    }

    public Builder setNextHopInterface(
        String nextHopInterface) {
      _nextHopInterface =
          nextHopInterface;
      return this;
    }

    public Builder setNextHopIp(
        @Nullable Ip6 nextHopIp) {
      _nextHopIp = nextHopIp;
      return this;
    }

    public Builder setTag(long tag) {
      _tag = tag;
      return this;
    }
  }

  private static final Comparator<StaticRoute6>
      COMPARATOR =
          Comparator
              .comparing(
                  StaticRoute6::getNetwork)
              .thenComparing(
                  StaticRoute6::
                      getNextHopInterface)
              .thenComparing(
                  StaticRoute6::getNextHopIp,
                  Comparator.nullsFirst(
                      Comparator.naturalOrder()))
              .thenComparingLong(
                  StaticRoute6::
                      getAdministrativeCost)
              .thenComparingLong(
                  StaticRoute6::getMetric)
              .thenComparingLong(
                  StaticRoute6::getTag);

  private final long _metric;
}
