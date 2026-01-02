package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.LegacyNextHops;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopVrf;

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
  private static final String PROP_TRACK = "track";

  private final long _metric;
  private final boolean _recursive;
  private final @Nullable String _track;

  private transient int _hashCode;

  @JsonCreator
  private static StaticRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_VRF) @Nullable String nextVrf,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int administrativeCost,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_TRACK) @Nullable String track) {
    return new StaticRoute(
        requireNonNull(network),
        nextVrf != null
            ? NextHopVrf.of(nextVrf)
            : NextHop.legacyConverter(nextHopInterface, nextHopIp),
        administrativeCost,
        metric,
        tag,
        false,
        false,
        true,
        track);
  }

  private StaticRoute(
      @Nonnull Prefix network,
      @Nonnull NextHop nextHop,
      int administrativeCost,
      long metric,
      long tag,
      boolean nonForwarding,
      boolean nonRouting,
      boolean recursive,
      @Nullable String track) {
    super(network, administrativeCost, tag, nonRouting, nonForwarding);
    _metric = metric;
    _nextHop = nextHop;
    _recursive = recursive;
    _track = track;
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  public long getMetric() {
    return _metric;
  }

  /** Jackson use only */
  @JsonProperty(PROP_NEXT_VRF)
  private @Nullable String getNextVrf() {
    return LegacyNextHops.getNextVrf(_nextHop).orElse(null);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.STATIC;
  }

  @JsonIgnore
  public boolean getRecursive() {
    return _recursive;
  }

  @JsonProperty(PROP_TRACK)
  public @Nullable String getTrack() {
    return _track;
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

    private boolean _recursive;
    private @Nullable String _track;

    private Builder() {
      // Tmp hack until parent builder is fixed and doesn't default to primitives
      setAdmin(Route.UNSET_ROUTE_ADMIN);
      // Default to true to match behavior predating this flag
      _recursive = true;
    }

    @Override
    public @Nonnull StaticRoute build() {
      checkArgument(
          getAdmin() != Route.UNSET_ROUTE_ADMIN,
          "Static route cannot have unset %s",
          PROP_ADMINISTRATIVE_COST);
      checkArgument(_nextHop != null, "Static route missing a next hop");
      return new StaticRoute(
          getNetwork(),
          _nextHop,
          getAdmin(),
          getMetric(),
          getTag(),
          getNonForwarding(),
          getNonRouting(),
          _recursive,
          _track);
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    public @Nonnull Builder setAdministrativeCost(int administrativeCost) {
      // Call method on parent builder. Keep backwards-compatible API.
      setAdmin(administrativeCost);
      return this;
    }

    public @Nonnull Builder setRecursive(boolean recursive) {
      _recursive = recursive;
      return this;
    }

    public @Nonnull Builder setTrack(@Nullable String track) {
      _track = track;
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
          .thenComparing(StaticRoute::getNonForwarding)
          .thenComparing(StaticRoute::getRecursive)
          .thenComparing(StaticRoute::getTrack, nullsFirst(String::compareTo));

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setNextHop(_nextHop)
        .setMetric(_metric)
        .setTag(_tag)
        .setAdmin(getAdministrativeCost())
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setRecursive(getRecursive())
        .setTrack(_track);
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
        && _tag == rhs._tag
        && _recursive == rhs._recursive
        && Objects.equals(_track, rhs._track);
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
              _tag,
              _recursive,
              _track);
      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add(PROP_NETWORK, _network)
        .add("nextHop", _nextHop)
        .add(PROP_ADMINISTRATIVE_COST, _admin)
        .add(PROP_METRIC, _metric)
        .add("recursive", _recursive)
        .add(PROP_TAG, _tag)
        .add(PROP_TRACK, _track)
        .toString();
  }
}
