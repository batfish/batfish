package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
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
public final class StaticRoute extends AbstractRoute implements Comparable<StaticRoute> {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (56 bytes data, would be 56 MiB total ignoring overhead).
  private static final LoadingCache<StaticRoute, StaticRoute> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 20).build(x -> x);

  static final long DEFAULT_STATIC_ROUTE_METRIC = 0L;
  private static final String PROP_NEXT_VRF = "nextVrf";
  private static final String PROP_TAG2 = "tag2";
  private static final String PROP_TRACK = "track";

  // Stored as unsigned 32-bit int for memory savings. Use getMetric() to read.
  private final int _metric;
  private final boolean _recursive;
  private final @Nullable String _track;
  @VisibleForTesting final @Nullable UncommonAttributes _uncommonAttributes;

  private transient int _hashCode;

  /**
   * Holds the properties of static routes that most static routes do not have. A route with none of
   * them has no {@link UncommonAttributes} (null), so it pays only for the reference.
   *
   * <p>Interned for memory overhead.
   */
  static final class UncommonAttributes implements Serializable {
    /** Keys of the attributes stored in {@link #_attributes}; each getter knows its value type. */
    private enum Key {
      TAG2
    }

    /** Returns the attributes with the given values, or {@code null} if none of them is set. */
    static @Nullable UncommonAttributes create(long tag2) {
      ImmutableMap.Builder<Key, Object> attributes = ImmutableMap.builder();
      if (tag2 != 0) {
        attributes.put(Key.TAG2, tag2);
      }
      ImmutableMap<Key, Object> map = attributes.buildOrThrow();
      // Intern.
      return map.isEmpty() ? null : UNCOMMON_ATTRIBUTE_CACHE.get(new UncommonAttributes(map));
    }

    private UncommonAttributes(ImmutableMap<Key, Object> attributes) {
      _attributes = attributes;
    }

    /** Only the attributes that are set; never empty. */
    private final @Nonnull ImmutableMap<Key, Object> _attributes;

    /** A second route tag, local to the device (Junos {@code tag2}). 0 if unset. */
    long getTag2() {
      Long tag2 = (Long) _attributes.get(Key.TAG2);
      return tag2 == null ? 0L : tag2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof UncommonAttributes)) {
        return false;
      }
      return _attributes.equals(((UncommonAttributes) o)._attributes);
    }

    @Override
    public int hashCode() {
      return _attributes.hashCode();
    }

    /** Re-intern after deserialization. */
    @Serial
    private Object readResolve() throws ObjectStreamException {
      return UNCOMMON_ATTRIBUTE_CACHE.get(this);
    }
  }

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size; few routes have uncommon attributes.
  private static final LoadingCache<UncommonAttributes, UncommonAttributes>
      UNCOMMON_ATTRIBUTE_CACHE =
          Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(a -> a);

  @JsonCreator
  private static StaticRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_VRF) @Nullable String nextVrf,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) long administrativeCost,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_TAG2) @Nullable Long tag2,
      @JsonProperty(PROP_TRACK) @Nullable String track) {
    return CACHE.get(
        new StaticRoute(
            requireNonNull(network),
            nextVrf != null
                ? NextHopVrf.of(nextVrf)
                : NextHop.legacyConverter(nextHopInterface, nextHopIp),
            administrativeCost,
            metric,
            tag,
            UncommonAttributes.create(tag2 != null ? tag2 : 0L),
            false,
            false,
            true,
            track));
  }

  private StaticRoute(
      @Nonnull Prefix network,
      @Nonnull NextHop nextHop,
      long administrativeCost,
      long metric,
      long tag,
      @Nullable UncommonAttributes uncommonAttributes,
      boolean nonForwarding,
      boolean nonRouting,
      boolean recursive,
      @Nullable String track) {
    super(network, administrativeCost, tag, nonRouting, nonForwarding);
    _metric = (int) metric;
    _nextHop = nextHop;
    _recursive = recursive;
    _track = track;
    _uncommonAttributes = uncommonAttributes;
  }

  @Override
  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  public long getMetric() {
    return Integer.toUnsignedLong(_metric);
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

  /** A second route tag, local to the device (Junos {@code tag2}). */
  @JsonIgnore
  public long getTag2() {
    return _uncommonAttributes == null ? 0L : _uncommonAttributes.getTag2();
  }

  /** Jackson use only: omit tag2 when it is 0. */
  @JsonProperty(PROP_TAG2)
  private @Nullable Long getJsonTag2() {
    long tag2 = getTag2();
    return tag2 == 0 ? null : tag2;
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
    private long _tag2;
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
      return CACHE.get(
          new StaticRoute(
              getNetwork(),
              _nextHop,
              getAdmin(),
              getMetric(),
              getTag(),
              UncommonAttributes.create(_tag2),
              getNonForwarding(),
              getNonRouting(),
              _recursive,
              _track));
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    public @Nonnull Builder setAdministrativeCost(long administrativeCost) {
      // Call method on parent builder. Keep backwards-compatible API.
      setAdmin(administrativeCost);
      return this;
    }

    public @Nonnull Builder setRecursive(boolean recursive) {
      _recursive = recursive;
      return this;
    }

    public long getTag2() {
      return _tag2;
    }

    public @Nonnull Builder setTag2(long tag2) {
      _tag2 = tag2;
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
          .thenComparing(StaticRoute::getNextHop)
          .thenComparing(StaticRoute::getMetric)
          .thenComparing(StaticRoute::getAdministrativeCost)
          .thenComparing(StaticRoute::getTag)
          .thenComparing(StaticRoute::getTag2)
          .thenComparing(StaticRoute::getNonRouting)
          .thenComparing(StaticRoute::getNonForwarding)
          .thenComparing(StaticRoute::getRecursive)
          .thenComparing(StaticRoute::getTrack, nullsFirst(String::compareTo));

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setNextHop(_nextHop)
        .setMetric(getMetric())
        .setTag(getTag())
        .setTag2(getTag2())
        .setAdmin(getAdministrativeCost())
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setRecursive(getRecursive())
        .setTrack(_track);
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
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
        && getAdministrativeCost() == rhs.getAdministrativeCost()
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting()
        && getMetric() == rhs.getMetric()
        && _nextHop.equals(rhs._nextHop)
        && getTag() == rhs.getTag()
        && Objects.equals(_uncommonAttributes, rhs._uncommonAttributes)
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
              getAdministrativeCost(),
              getNonForwarding(),
              getNonRouting(),
              getMetric(),
              _nextHop,
              getTag(),
              _uncommonAttributes,
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
        .add(PROP_ADMINISTRATIVE_COST, getAdministrativeCost())
        .add(PROP_METRIC, getMetric())
        .add("recursive", _recursive)
        .add(PROP_TAG, getTag())
        .add(PROP_TAG2, getTag2())
        .add(PROP_TRACK, _track)
        .toString();
  }
}
