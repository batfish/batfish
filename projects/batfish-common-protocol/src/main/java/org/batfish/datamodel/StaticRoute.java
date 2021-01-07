package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;

/**
 * A static route.
 *
 * <p>Implements {@link Comparable}, but {@link #compareTo(StaticRoute)} <em>should not</em> be used
 * for determining route preference in RIBs.
 */
@ParametersAreNonnullByDefault
public class StaticRoute extends AbstractRoute implements Comparable<StaticRoute> {

  static final long DEFAULT_STATIC_ROUTE_METRIC = 0L;
  private static final String PROP_NEXT_VRF = "nextVrf";

  private final long _metric;
  @Nullable private final String _nextVrf;

  private transient int _hashCode;

  @JsonCreator
  private static StaticRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_VRF) String nextVrf,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int administrativeCost,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_TAG) long tag) {
    return new StaticRoute(
        requireNonNull(network),
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        nextVrf,
        administrativeCost,
        metric,
        tag,
        false,
        false);
  }

  private StaticRoute(
      @Nonnull Prefix network,
      @Nonnull NextHop nextHop,
      @Nullable String nextVrf,
      int administrativeCost,
      long metric,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, administrativeCost, tag, nonRouting, nonForwarding);
    _metric = metric;
    _nextHop = nextHop;
    _nextVrf = nextVrf;
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  public Long getMetric() {
    return _metric;
  }

  @Nullable
  @JsonProperty(PROP_NEXT_VRF)
  public String getNextVrf() {
    return _nextVrf;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.STATIC;
  }

  public static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  public static Builder testBuilder() {
    return builder().setNextHop(NextHopDiscard.instance()).setAdmin(1);
  }

  @Override
  public int compareTo(StaticRoute o) {
    // The comparator has no impact on route preference in RIBs and should not be used as such
    return COMPARATOR.compare(this, o);
  }

  /** Builder for {@link StaticRoute} */
  @ParametersAreNonnullByDefault
  public static final class Builder extends AbstractRouteBuilder<Builder, StaticRoute> {

    @Nullable private String _nextVrf;

    private Builder() {
      // Tmp hack until parent builder is fixed and doesn't default to primitives
      setAdmin(Route.UNSET_ROUTE_ADMIN);
    }

    @Nonnull
    @Override
    public StaticRoute build() {
      checkArgument(
          getAdmin() != Route.UNSET_ROUTE_ADMIN,
          "Static route cannot have unset %s",
          PROP_ADMINISTRATIVE_COST);
      if (_nextVrf == null) {
        // Allow null next hop if next vrf is set
        checkArgument(_nextHop != null, "Missing next hop");
      }
      return new StaticRoute(
          getNetwork(),
          // Only the case when next vrf is set
          _nextHop == null ? NextHopDiscard.instance() : _nextHop,
          _nextVrf,
          getAdmin(),
          getMetric(),
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }

    @Nonnull
    public Builder setAdministrativeCost(int administrativeCost) {
      // Call method on parent builder. Keep backwards-compatible API.
      setAdmin(administrativeCost);
      return this;
    }

    @Nonnull
    public Builder setNextVrf(@Nullable String nextVrf) {
      _nextVrf = nextVrf;
      return this;
    }
  }

  /////// Keep COMPARATOR, #toBuilder, #equals, and #hashCode in sync ////////

  // The comparator has no impact on route preference in RIBs and should not be used as such
  private static final Comparator<StaticRoute> COMPARATOR =
      comparing(StaticRoute::getNetwork)
          .thenComparing(StaticRoute::getNextHopIp)
          .thenComparing(StaticRoute::getNextHopInterface)
          .thenComparing(StaticRoute::getNextVrf, nullsFirst(String::compareTo))
          .thenComparing(StaticRoute::getMetric)
          .thenComparing(StaticRoute::getAdministrativeCost)
          .thenComparing(StaticRoute::getTag)
          .thenComparing(StaticRoute::getNonRouting)
          .thenComparing(StaticRoute::getNonForwarding);

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setNextHop(_nextHop)
        .setNextVrf(_nextVrf)
        .setMetric(_metric)
        .setTag(_tag)
        .setAdmin(getAdministrativeCost())
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute rhs = (StaticRoute) o;
    return (_hashCode == rhs._hashCode || _hashCode == 0 || rhs._hashCode == 0)
        && _network.equals(rhs._network)
        && _admin == rhs._admin
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting()
        && _metric == rhs._metric
        && _nextHop.equals(rhs._nextHop)
        && Objects.equals(_nextVrf, rhs._nextVrf)
        && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h =
          Objects.hash(
              _network,
              _admin,
              getNonForwarding(),
              getNonRouting(),
              _metric,
              _nextHop,
              _nextVrf,
              _tag);
      _hashCode = h;
    }
    return h;
  }
}
