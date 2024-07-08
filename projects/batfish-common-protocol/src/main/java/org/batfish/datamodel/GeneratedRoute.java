package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.lexicographical;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static org.batfish.datamodel.BgpRoute.PROP_AS_PATH;
import static org.batfish.datamodel.BgpRoute.PROP_COMMUNITIES;
import static org.batfish.datamodel.BgpRoute.PROP_LOCAL_PREFERENCE;
import static org.batfish.datamodel.BgpRoute.PROP_ORIGIN_TYPE;
import static org.batfish.datamodel.BgpRoute.PROP_WEIGHT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/**
 * A generated/aggregate IPV4 route.
 *
 * <p>Implements {@link Comparable}, but {@link #compareTo(GeneratedRoute)} <em>should not</em> be
 * used for determining route preference in RIBs.
 */
@ParametersAreNonnullByDefault
public final class GeneratedRoute extends AbstractRoute
    implements Comparable<GeneratedRoute>,
        HasReadableAsPath,
        HasReadableCommunities,
        HasReadableLocalPreference,
        HasReadableOriginType,
        HasReadableWeight {
  /** A {@link GeneratedRoute} builder */
  public static class Builder extends AbstractRouteBuilder<Builder, GeneratedRoute>
      implements HasWritableAsPath<Builder, GeneratedRoute>,
          HasWritableCommunities<Builder, GeneratedRoute>,
          HasWritableLocalPreference<Builder, GeneratedRoute>,
          HasWritableOriginType<Builder, GeneratedRoute>,
          HasWritableWeight<Builder, GeneratedRoute> {

    private @Nonnull AsPath _asPath;
    private @Nullable String _attributePolicy;
    private @Nonnull CommunitySet _communities;
    private boolean _discard;
    private @Nullable String _generationPolicy;
    private long _localPreference;
    private @Nonnull OriginType _originType;
    private int _weight;

    private Builder() {
      _asPath = AsPath.empty();
      _communities = CommunitySet.empty();
      _localPreference = BgpRoute.DEFAULT_LOCAL_PREFERENCE;
      _originType = OriginType.INCOMPLETE;
    }

    @Override
    public @Nonnull GeneratedRoute build() {
      Prefix network = getNetwork();
      checkArgument(network != null, "Missing %s", PROP_NETWORK);
      return new GeneratedRoute(
          network,
          getAdmin(),
          firstNonNull(_nextHop, NextHopDiscard.instance()),
          firstNonNull(_asPath, AsPath.empty()),
          _attributePolicy,
          _communities,
          _discard,
          _localPreference,
          _generationPolicy,
          getMetric(),
          _originType,
          getTag(),
          _weight,
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull AsPath getAsPath() {
      return _asPath;
    }

    @Override
    public @Nonnull Builder setAsPath(AsPath asPath) {
      _asPath = asPath;
      return this;
    }

    public @Nonnull Builder setAttributePolicy(@Nullable String attributePolicy) {
      _attributePolicy = attributePolicy;
      return this;
    }

    @Override
    public @Nonnull CommunitySet getCommunities() {
      return _communities;
    }

    @Override
    public @Nonnull Set<Community> getCommunitiesAsSet() {
      return _communities.getCommunities();
    }

    /** Overwrite communities */
    @Override
    public Builder setCommunities(CommunitySet communities) {
      _communities = communities;
      return this;
    }

    public @Nonnull Builder setDiscard(boolean discard) {
      _discard = discard;
      return this;
    }

    public @Nonnull Builder setGenerationPolicy(@Nullable String generationPolicy) {
      _generationPolicy = generationPolicy;
      return this;
    }

    @Override
    public long getLocalPreference() {
      return _localPreference;
    }

    @Override
    public @Nonnull Builder setLocalPreference(long localPreference) {
      _localPreference = localPreference;
      return this;
    }

    @Override
    public @Nonnull OriginType getOriginType() {
      return _originType;
    }

    @Override
    public @Nonnull Builder setOriginType(OriginType originType) {
      _originType = originType;
      return this;
    }

    @Override
    public int getWeight() {
      return _weight;
    }

    @Override
    public @Nonnull Builder setWeight(int weight) {
      _weight = weight;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final String PROP_ATTRIBUTE_POLICY = "attributePolicy";
  private static final String PROP_ATTRIBUTE_POLICY_SOURCES = "attributePolicySources";
  private static final String PROP_DISCARD = "discard";
  private static final String PROP_GENERATION_POLICY = "generationPolicy";
  private static final String PROP_GENERATION_POLICY_SOURCES = "generationPolicySources";

  private final @Nonnull AsPath _asPath;
  private final @Nullable String _attributePolicy;
  private final @Nonnull CommunitySet _communities;
  private final boolean _discard;
  private final @Nullable String _generationPolicy;
  private final long _localPreference;
  private final long _metric;
  private final @Nonnull OriginType _originType;
  private final int _weight;

  // Non-final fields, not properties of the route. Should not impact equality or hashcode.
  private SortedSet<String> _attributePolicySources;
  private SortedSet<String> _generationPolicySources;
  // Cache the hashcode
  private transient int _hashCode = 0;

  @JsonCreator
  private static GeneratedRoute jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int administrativeCost,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_AS_PATH) @Nullable AsPath asPath,
      @JsonProperty(PROP_ATTRIBUTE_POLICY) @Nullable String attributePolicy,
      @JsonProperty(PROP_COMMUNITIES) @Nullable CommunitySet communities,
      @JsonProperty(PROP_DISCARD) boolean discard,
      @JsonProperty(PROP_GENERATION_POLICY) @Nullable String generationPolicy,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_ORIGIN_TYPE) @Nullable OriginType originType,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(network != null, "GeneratedRoute missing %s", PROP_NETWORK);
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    return new GeneratedRoute(
        network,
        administrativeCost,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        firstNonNull(asPath, AsPath.empty()),
        attributePolicy,
        firstNonNull(communities, CommunitySet.empty()),
        discard,
        localPreference,
        generationPolicy,
        metric,
        originType,
        tag,
        weight,
        false,
        false);
  }

  private GeneratedRoute(
      Prefix network,
      int administrativeCost,
      NextHop nextHop,
      AsPath asPath,
      @Nullable String attributePolicy,
      CommunitySet communities,
      boolean discard,
      long localPreference,
      @Nullable String generationPolicy,
      long metric,
      OriginType originType,
      long tag,
      int weight,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, administrativeCost, tag, nonRouting, nonForwarding);
    _asPath = asPath;
    _attributePolicy = attributePolicy;
    _attributePolicySources = ImmutableSortedSet.of();
    _communities = communities;
    _discard = discard;
    _generationPolicy = generationPolicy;
    _generationPolicySources = ImmutableSortedSet.of();
    _metric = metric;
    _localPreference = localPreference;
    _nextHop = nextHop;
    _originType = originType;
    _weight = weight;
  }

  @JsonProperty(PROP_AS_PATH)
  @Override
  public @Nonnull AsPath getAsPath() {
    return _asPath;
  }

  /** The name of the policy that sets attributes of this route */
  @JsonProperty(PROP_ATTRIBUTE_POLICY)
  public @Nullable String getAttributePolicy() {
    return _attributePolicy;
  }

  @JsonProperty(PROP_ATTRIBUTE_POLICY_SOURCES)
  public SortedSet<String> getAttributePolicySources() {
    return _attributePolicySources;
  }

  /** Return the set of all community attributes */
  @Override
  public @Nonnull CommunitySet getCommunities() {
    return _communities;
  }

  /** Return the set of all community attributes */
  @Override
  public @Nonnull Set<Community> getCommunitiesAsSet() {
    return _communities.getCommunities();
  }

  /** Whether this route is meant to discard all matching packets */
  @JsonProperty(PROP_DISCARD)
  public boolean getDiscard() {
    return _discard;
  }

  /** The name of the policy that will generate this route if another route matches it */
  @JsonProperty(PROP_GENERATION_POLICY)
  public @Nullable String getGenerationPolicy() {
    return _generationPolicy;
  }

  @JsonProperty(PROP_GENERATION_POLICY_SOURCES)
  public SortedSet<String> getGenerationPolicySources() {
    return _generationPolicySources;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  @Override
  public long getLocalPreference() {
    return _localPreference;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public long getMetric() {
    return _metric;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  @Override
  public @Nonnull OriginType getOriginType() {
    return _originType;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.AGGREGATE;
  }

  @JsonProperty(PROP_WEIGHT)
  @Override
  public int getWeight() {
    return _weight;
  }

  @Override
  public int compareTo(GeneratedRoute rhs) {
    // The comparator has no impact on route preference in RIBs and should not be used as such
    return COMPARATOR.compare(this, rhs);
  }

  @JsonProperty(PROP_ATTRIBUTE_POLICY_SOURCES)
  public void setAttributePolicySources(SortedSet<String> attributePolicySources) {
    _attributePolicySources = attributePolicySources;
  }

  @JsonProperty(PROP_GENERATION_POLICY_SOURCES)
  public void setGenerationPolicySources(SortedSet<String> generationPolicySources) {
    _generationPolicySources = generationPolicySources;
  }

  /////// Keep COMPARATOR, #toBuilder, #equals, and #hashCode in sync ////////

  // The comparator has no impact on route preference in RIBs and should not be used as such
  private static final Comparator<GeneratedRoute> COMPARATOR =
      Comparator.comparing(GeneratedRoute::getNetwork)
          .thenComparing(GeneratedRoute::getNextHopIp, nullsLast(naturalOrder()))
          .thenComparing(GeneratedRoute::getNextHopInterface, nullsLast(naturalOrder()))
          .thenComparing(GeneratedRoute::getMetric)
          .thenComparing(GeneratedRoute::getAdministrativeCost)
          .thenComparing(GeneratedRoute::getTag)
          .thenComparing(GeneratedRoute::getNonRouting)
          .thenComparing(GeneratedRoute::getNonForwarding)
          .thenComparing(GeneratedRoute::getAsPath)
          .thenComparing(GeneratedRoute::getLocalPreference)
          .thenComparing(gr -> gr.getOriginType().getPreference())
          .thenComparing(GeneratedRoute::getWeight)
          .thenComparing(GeneratedRoute::getAttributePolicy, nullsLast(String::compareTo))
          .thenComparing(
              gr -> gr.getCommunities().getCommunities(), lexicographical(Ordering.natural()))
          .thenComparing(GeneratedRoute::getDiscard)
          .thenComparing(GeneratedRoute::getGenerationPolicy, nullsLast(String::compareTo));

  @Override
  public @Nonnull Builder toBuilder() {
    return new Builder()
        // General route properties
        .setNetwork(getNetwork())
        .setAdmin(getAdministrativeCost())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setMetric(firstNonNull(getMetric(), 0L))
        .setNextHop(getNextHop())
        .setTag(getTag())
        // BGP route properties
        .setAsPath(getAsPath())
        .setCommunities(getCommunities())
        .setLocalPreference(_localPreference)
        .setOriginType(_originType)
        .setWeight(_weight)
        // GeneratedRoute properties
        .setAttributePolicy(getAttributePolicy())
        .setDiscard(_discard)
        .setGenerationPolicy(getGenerationPolicy());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GeneratedRoute)) {
      return false;
    }
    GeneratedRoute that = (GeneratedRoute) o;
    return (_hashCode == that._hashCode || _hashCode == 0 || that._hashCode == 0)
        && _network.equals(that._network)
        && _admin == that._admin
        && getNonRouting() == that.getNonRouting()
        && getNonForwarding() == that.getNonForwarding()
        && _discard == that._discard
        && _metric == that._metric
        && _tag == that._tag
        && _asPath.equals(that._asPath)
        && Objects.equals(_attributePolicy, that._attributePolicy)
        && _communities.equals(that._communities)
        && Objects.equals(_generationPolicy, that._generationPolicy)
        && _nextHop.equals(that._nextHop)
        && _localPreference == that._localPreference
        && _originType == that._originType
        && _weight == that._weight;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h =
          Objects.hash(
              _network,
              _admin,
              getNonRouting(),
              getNonForwarding(),
              _tag,
              _asPath,
              _attributePolicy,
              _communities,
              _discard,
              _generationPolicy,
              _metric,
              _nextHop,
              _localPreference,
              _originType.ordinal(),
              _weight);
      _hashCode = h;
    }
    return h;
  }
}
