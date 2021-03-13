package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.lexicographical;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
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

/**
 * A generated/aggregate IPV4 route.
 *
 * <p>Implements {@link Comparable}, but {@link #compareTo(GeneratedRoute)} <em>should not</em> be
 * used for determining route preference in RIBs.
 */
@ParametersAreNonnullByDefault
public final class GeneratedRoute extends BgpRoute<GeneratedRoute.Builder, GeneratedRoute>
    implements Comparable<GeneratedRoute> {
  /** A {@link GeneratedRoute} builder */
  public static class Builder extends BgpRoute.Builder<Builder, GeneratedRoute> {

    @Override
    public Builder newBuilder() {
      return new Builder();
    }

    @Nullable private String _attributePolicy;
    private boolean _discard;
    @Nullable private String _generationPolicy;

    private Builder() {}

    @Nonnull
    @Override
    public GeneratedRoute build() {
      Prefix network = getNetwork();
      checkArgument(network != null, "Missing %s", PROP_NETWORK);
      return new GeneratedRoute(
          network,
          getAdmin(),
          firstNonNull(_nextHop, NextHopDiscard.instance()),
          firstNonNull(_asPath, AsPath.empty()),
          _attributePolicy,
          ImmutableSet.copyOf(firstNonNull(_communities, ImmutableSet.of())),
          _discard,
          _localPreference,
          _generationPolicy,
          getMetric(),
          firstNonNull(_originType, OriginType.INCOMPLETE),
          getTag(),
          _weight,
          getNonForwarding(),
          getNonRouting());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAttributePolicy(@Nullable String attributePolicy) {
      _attributePolicy = attributePolicy;
      return this;
    }

    public Builder setDiscard(boolean discard) {
      _discard = discard;
      return this;
    }

    public Builder setGenerationPolicy(@Nullable String generationPolicy) {
      _generationPolicy = generationPolicy;
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

  @Nullable private final String _attributePolicy;
  @Nullable private final String _generationPolicy;
  private final boolean _discard;
  // Non-final fields, not properties of the route. Should not impact equality or hashcode.
  private SortedSet<String> _attributePolicySources;
  private SortedSet<String> _generationPolicySources;
  // Cache the hashcode
  private transient int _hashCode = 0;

  @JsonCreator
  private static GeneratedRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int administrativeCost,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_ATTRIBUTE_POLICY) String attributePolicy,
      @Nullable @JsonProperty(PROP_COMMUNITIES) SortedSet<Community> communities,
      @JsonProperty(PROP_DISCARD) boolean discard,
      @Nullable @JsonProperty(PROP_GENERATION_POLICY) String generationPolicy,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long metric,
      @Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
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
        firstNonNull(communities, ImmutableSet.of()),
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
      Set<Community> communities,
      boolean discard,
      long localPreference,
      @Nullable String generationPolicy,
      long metric,
      OriginType originType,
      long tag,
      int weight,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        network,
        nextHop,
        administrativeCost,
        asPath,
        communities,
        localPreference,
        metric,
        Ip.ZERO,
        null,
        false,
        originType,
        RoutingProtocol.AGGREGATE,
        null,
        null,
        tag,
        weight,
        nonForwarding,
        nonRouting);
    _attributePolicy = attributePolicy;
    _attributePolicySources = ImmutableSortedSet.of();
    _discard = discard;
    _generationPolicy = generationPolicy;
    _generationPolicySources = ImmutableSortedSet.of();
    _nextHop = nextHop;
  }

  /** The name of the policy that sets attributes of this route */
  @Nullable
  @JsonProperty(PROP_ATTRIBUTE_POLICY)
  public String getAttributePolicy() {
    return _attributePolicy;
  }

  @JsonProperty(PROP_ATTRIBUTE_POLICY_SOURCES)
  public SortedSet<String> getAttributePolicySources() {
    return _attributePolicySources;
  }

  /** Whether this route is route is meant to discard all matching packets */
  @JsonProperty(PROP_DISCARD)
  public boolean getDiscard() {
    return _discard;
  }

  @JsonIgnore
  @Nonnull
  @Override
  public Set<Long> getClusterList() {
    return super.getClusterList();
  }

  @JsonIgnore
  @Nonnull
  @Override
  public Ip getOriginatorIp() {
    return super.getOriginatorIp();
  }

  @JsonIgnore
  @Nonnull
  @Override
  public RoutingProtocol getProtocol() {
    return super.getProtocol();
  }

  @JsonIgnore
  @Override
  public boolean getReceivedFromRouteReflectorClient() {
    return super.getReceivedFromRouteReflectorClient();
  }

  /** The name of the policy that will generate this route if another route matches it */
  @Nullable
  @JsonProperty(PROP_GENERATION_POLICY)
  public String getGenerationPolicy() {
    return _generationPolicy;
  }

  @JsonProperty(PROP_GENERATION_POLICY_SOURCES)
  public SortedSet<String> getGenerationPolicySources() {
    return _generationPolicySources;
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
  public Builder toBuilder() {
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
        && _med == that._med
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
              _med,
              _nextHop,
              _localPreference,
              _originType,
              _weight);
      _hashCode = h;
    }
    return h;
  }
}
