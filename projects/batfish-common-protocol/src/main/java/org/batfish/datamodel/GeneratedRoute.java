package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
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

/**
 * A generated/aggregate IPV4 route.
 *
 * <p>Implements {@link Comparable}, but {@link #compareTo(GeneratedRoute)} <em>should not</em> be
 * used for determining route preference in RIBs.
 */
@ParametersAreNonnullByDefault
public final class GeneratedRoute extends AbstractRoute implements Comparable<GeneratedRoute> {
  /** A {@link GeneratedRoute} builder */
  public static class Builder extends AbstractRouteBuilder<Builder, GeneratedRoute> {

    @Nullable private AsPath _asPath;
    @Nullable private String _attributePolicy;
    @Nullable private Set<Community> _communities;
    private boolean _discard;
    @Nullable private String _generationPolicy;
    @Nullable private String _nextHopInterface;

    private Builder() {}

    @Nonnull
    @Override
    public GeneratedRoute build() {
      return new GeneratedRoute(
          getNetwork(),
          getAdmin(),
          getNextHopIp(),
          firstNonNull(_asPath, AsPath.empty()),
          _attributePolicy,
          ImmutableSortedSet.copyOf(firstNonNull(_communities, ImmutableSet.of())),
          _discard,
          _generationPolicy,
          getMetric(),
          firstNonNull(_nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAsPath(@Nullable AsPath asPath) {
      _asPath = asPath;
      return this;
    }

    public Builder setAttributePolicy(@Nullable String attributePolicy) {
      _attributePolicy = attributePolicy;
      return this;
    }

    public Builder setCommunities(@Nullable Set<Community> communities) {
      _communities = communities;
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

    public Builder setNextHopInterface(@Nullable String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final String PROP_AS_PATH = "asPath";
  private static final String PROP_ATTRIBUTE_POLICY = "attributePolicy";
  private static final String PROP_ATTRIBUTE_POLICY_SOURCES = "attributePolicySources";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_DISCARD = "discard";
  private static final String PROP_GENERATION_POLICY = "generationPolicy";
  private static final String PROP_GENERATION_POLICY_SOURCES = "generationPolicySources";
  private static final String PROP_METRIC = "metric";

  @Nonnull private final AsPath _asPath;
  @Nullable private final String _attributePolicy;
  @Nonnull private final SortedSet<Community> _communities;
  private final boolean _discard;
  @Nullable private final String _generationPolicy;
  private final long _metric;
  @Nonnull private final String _nextHopInterface;
  @Nonnull private final Ip _nextHopIp;
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
      @Nullable @JsonProperty(PROP_METRIC) Long metric,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "GeneratedRoute missing %s", PROP_NETWORK);
    checkArgument(metric != null, "GeneratedRoute missing %s", PROP_METRIC);
    return new GeneratedRoute(
        network,
        administrativeCost,
        firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP),
        firstNonNull(asPath, AsPath.empty()),
        attributePolicy,
        firstNonNull(communities, ImmutableSortedSet.of()),
        discard,
        generationPolicy,
        metric,
        firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
        tag,
        false,
        false);
  }

  private GeneratedRoute(
      Prefix network,
      int administrativeCost,
      Ip nextHopIp,
      AsPath asPath,
      @Nullable String attributePolicy,
      @Nullable SortedSet<Community> communities,
      boolean discard,
      @Nullable String generationPolicy,
      long metric,
      String nextHopInterface,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, administrativeCost, tag, nonRouting, nonForwarding);
    _asPath = asPath;
    _attributePolicy = attributePolicy;
    _attributePolicySources = ImmutableSortedSet.of();
    _communities = firstNonNull(communities, ImmutableSortedSet.of());
    _discard = discard;
    _generationPolicy = generationPolicy;
    _generationPolicySources = ImmutableSortedSet.of();
    _metric = metric;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
  }

  /** A BGP AS-path attribute to associate with this generated route */
  @JsonProperty(PROP_AS_PATH)
  public AsPath getAsPath() {
    return _asPath;
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

  /** The communities attached to this route */
  @Nonnull
  @JsonProperty(PROP_COMMUNITIES)
  public SortedSet<Community> getCommunities() {
    return _communities;
  }

  /** Whether this route is route is meant to discard all matching packets */
  @JsonProperty(PROP_DISCARD)
  public boolean getDiscard() {
    return _discard;
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

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public Long getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
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
    return RoutingProtocol.AGGREGATE;
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
          .thenComparing(GeneratedRoute::getNextHopIp)
          .thenComparing(GeneratedRoute::getNextHopInterface)
          .thenComparing(GeneratedRoute::getMetric)
          .thenComparing(GeneratedRoute::getAdministrativeCost)
          .thenComparing(GeneratedRoute::getTag)
          .thenComparing(GeneratedRoute::getNonRouting)
          .thenComparing(GeneratedRoute::getNonForwarding)
          .thenComparing(GeneratedRoute::getAsPath)
          .thenComparing(
              GeneratedRoute::getAttributePolicy, Comparator.nullsLast(String::compareTo))
          .thenComparing(
              GeneratedRoute::getCommunities, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(GeneratedRoute::getDiscard)
          .thenComparing(
              GeneratedRoute::getGenerationPolicy, Comparator.nullsLast(String::compareTo));

  @Override
  public Builder toBuilder() {
    return new Builder()
        // General route properties
        .setNetwork(getNetwork())
        .setAdmin(getAdministrativeCost())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setMetric(firstNonNull(getMetric(), 0L))
        .setNextHopInterface(getNextHopInterface())
        .setNextHopIp(getNextHopIp())
        .setTag(getTag())
        // GeneratedRoute properties
        .setAsPath(getAsPath())
        .setAttributePolicy(getAttributePolicy())
        .setCommunities(getCommunities())
        .setDiscard(getDiscard())
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
        && _nextHopInterface.equals(that._nextHopInterface)
        && _nextHopIp.equals(that._nextHopIp);
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
              _nextHopInterface,
              _nextHopIp);
      _hashCode = h;
    }
    return h;
  }
}
