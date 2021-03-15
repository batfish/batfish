package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** A route containining BGP properites, but not necessarily created by a BGP process. */
@ParametersAreNonnullByDefault
public abstract class BgpAttributesRoute<
        B extends BgpAttributesRoute.Builder<B, R>, R extends BgpAttributesRoute<B, R>>
    extends AbstractRoute {

  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, R>, R extends BgpAttributesRoute<B, R>>
      extends AbstractRouteBuilder<B, R> {
    @Nonnull protected AsPath _asPath;
    // Invariant: either immutable or a local copy shielded from external mutations.
    @Nonnull protected Set<Community> _communities;
    protected long _localPreference;
    @Nullable protected OriginType _originType;
    protected int _weight;

    protected Builder() {
      _asPath = AsPath.empty();
      _communities = ImmutableSet.of();
    }

    /**
     * Returns a completely new builder of type {@link B} which has all the fields unset.
     *
     * @return A completely new builder of type {@link B}.
     */
    /* This is needed in cases where we need to create a new builder having type same as any of the
    subclasses of BgpRoute's builder but we are not sure of the exact type of the concrete child
    class.
    For example while evaluating a routing policy and executing its statements we need
    to create a completely new builder which should be of the same type as environment's output
    route builder but we are not sure of the concrete type and only know that it extends the
    abstract BgpRoute's builder. */
    @Nonnull
    public abstract B newBuilder();

    @Nonnull
    @Override
    public abstract R build();

    @Override
    @Nonnull
    protected abstract B getThis();

    @Nonnull
    public AsPath getAsPath() {
      return _asPath;
    }

    @Nonnull
    public Set<Community> getCommunities() {
      return _communities instanceof ImmutableSet
          ? _communities
          : Collections.unmodifiableSet(_communities);
    }

    public long getLocalPreference() {
      return _localPreference;
    }

    @Nullable
    public OriginType getOriginType() {
      return _originType;
    }

    public int getWeight() {
      return _weight;
    }

    public B setAsPath(AsPath asPath) {
      _asPath = asPath;
      return getThis();
    }

    /** Overwrite communities */
    public B setCommunities(CommunitySet communities) {
      _communities = communities.getCommunities();
      return getThis();
    }

    /** Overwrite communities */
    public B setCommunities(Collection<? extends Community> communities) {
      if (communities instanceof ImmutableSet) {
        @SuppressWarnings("unchecked") // cannot be mutated, cast to superclass is safe.
        ImmutableSet<Community> immutableComm = (ImmutableSet<Community>) communities;
        _communities = immutableComm;
      } else {
        _communities = new HashSet<>(communities);
      }
      return getThis();
    }

    /** Add communities */
    public B addCommunities(Collection<? extends Community> communities) {
      if (_communities instanceof ImmutableSet) {
        _communities = new HashSet<>(_communities);
      }
      _communities.addAll(communities);
      return getThis();
    }

    /** Add a single community */
    public B addCommunity(Community community) {
      if (_communities instanceof ImmutableSet) {
        _communities = new HashSet<>(_communities);
      }
      _communities.add(community);
      return getThis();
    }

    /** Add communities */
    public B removeCommunities(Set<Community> communities) {
      if (_communities instanceof ImmutableSet) {
        _communities = new HashSet<>(_communities);
      }
      _communities.removeAll(communities);
      return getThis();
    }

    public B setLocalPreference(long localPreference) {
      _localPreference = localPreference;
      return getThis();
    }

    public B setOriginType(OriginType originType) {
      _originType = originType;
      return getThis();
    }

    public B setWeight(int weight) {
      _weight = weight;
      return getThis();
    }
  }

  /** Local-preference has a maximum value of u32 max. */
  public static final long MAX_LOCAL_PREFERENCE = (1L << 32) - 1;
  /** Default local preference for a BGP route if one is not set explicitly */
  public static final long DEFAULT_LOCAL_PREFERENCE = 100L;

  public static final String PROP_AS_PATH = "asPath";
  public static final String PROP_COMMUNITIES = "communities";
  public static final String PROP_LOCAL_PREFERENCE = "localPreference";
  static final String PROP_ORIGIN_TYPE = "originType";
  static final String PROP_WEIGHT = "weight";

  @Nonnull protected final AsPath _asPath;
  @Nonnull protected final CommunitySet _communities;
  protected final long _localPreference;
  protected final long _med;
  @Nonnull protected final OriginType _originType;
  /* NOTE: Cisco-only attribute */
  protected final int _weight;

  protected BgpAttributesRoute(
      @Nullable Prefix network,
      @Nonnull NextHop nextHop,
      int admin,
      @Nullable AsPath asPath,
      @Nullable Set<Community> communities,
      long localPreference,
      long med,
      OriginType originType,
      long tag,
      int weight,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, admin, tag, nonRouting, nonForwarding);
    _asPath = firstNonNull(asPath, AsPath.empty());
    _communities = communities == null ? CommunitySet.empty() : CommunitySet.of(communities);
    _localPreference = localPreference;
    _med = med;
    _nextHop = nextHop;
    _originType = originType;
    _weight = weight;
  }

  @Nonnull
  @JsonProperty(PROP_AS_PATH)
  public AsPath getAsPath() {
    return _asPath;
  }

  /** Return the set of all community attributes */
  public @Nonnull CommunitySet getCommunities() {
    return _communities;
  }

  /** Return only standard community attributes */
  @Nonnull
  @JsonIgnore
  public Set<StandardCommunity> getStandardCommunities() {
    return _communities.getStandardCommunities();
  }

  /** Return only extended community attributes */
  @Nonnull
  @JsonIgnore
  public Set<ExtendedCommunity> getExtendedCommunities() {
    return _communities.getExtendedCommunities();
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public long getLocalPreference() {
    return _localPreference;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public Long getMetric() {
    return _med;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @JsonProperty(PROP_WEIGHT)
  public int getWeight() {
    return _weight;
  }

  @Override
  public abstract B toBuilder();

  @JsonProperty(PROP_COMMUNITIES)
  private @Nonnull CommunitySet getJsonCommunities() {
    return _communities;
  }
}
