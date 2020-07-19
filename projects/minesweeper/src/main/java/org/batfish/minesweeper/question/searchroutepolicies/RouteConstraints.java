package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** A set of constraints on a route announcement. */
@ParametersAreNonnullByDefault
public class RouteConstraints {

  private static final String PROP_PREFIX_SPACE = "prefixSpace";
  private static final String PROP_COMPLEMENT_PREFIX_SPACE = "complementPrefixSpace";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MULTI_EXIT_DISCRIMINATOR = "multiExitDiscriminator";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_COMPLEMENT_COMMUNITIES = "complementCommunities";

  // the announcement's prefix must be within this space
  @Nonnull private PrefixSpace _prefixSpace;
  // if this flag is set, then the prefix must be outside of the above space
  private boolean _complementPrefixSpace;
  // the announcement's local prefix must be within this range
  @Nonnull private IntegerSpace _localPref;
  // the announcement's MED must be within this range
  @Nonnull private IntegerSpace _med;
  // the announcement must be tagged with at least one of these communities
  @Nonnull private CommunitySet _communities;
  // if this flag is set, the announcement must not be tagged with any of
  // the above communities
  boolean _complementCommunities;

  @JsonCreator
  private RouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX_SPACE) PrefixSpace prefixSpace,
      @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE) boolean complementPrefixSpace,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) IntegerSpace localPref,
      @Nullable @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR) IntegerSpace med,
      @Nullable @JsonProperty(PROP_COMMUNITIES) CommunitySet communities,
      @JsonProperty(PROP_COMPLEMENT_COMMUNITIES) boolean complementCommunities) {
    _prefixSpace = firstNonNull(prefixSpace, new PrefixSpace());
    _complementPrefixSpace = complementPrefixSpace;
    _localPref = firstNonNull(localPref, IntegerSpace.EMPTY);
    _med = firstNonNull(med, IntegerSpace.EMPTY);
    _communities = firstNonNull(communities, CommunitySet.empty());
    _complementCommunities = complementCommunities;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private PrefixSpace _prefixSpace;
    private boolean _complementPrefixSpace = false;
    private IntegerSpace _localPref;
    private IntegerSpace _med;
    private CommunitySet _communities;
    private boolean _complementCommunities = false;

    private Builder() {}

    public Builder setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
      return this;
    }

    public Builder setComplementPrefixSpace(boolean complementPrefixSpace) {
      _complementPrefixSpace = complementPrefixSpace;
      return this;
    }

    public Builder setLocalPref(IntegerSpace localPref) {
      _localPref = localPref;
      return this;
    }

    public Builder setMed(IntegerSpace med) {
      _med = med;
      return this;
    }

    public Builder setCommunities(CommunitySet communities) {
      _communities = communities;
      return this;
    }

    public Builder setComplementCommunities(boolean complementCommunities) {
      _complementCommunities = complementCommunities;
      return this;
    }

    public RouteConstraints build() {
      return new RouteConstraints(
          _prefixSpace,
          _complementPrefixSpace,
          _localPref,
          _med,
          _communities,
          _complementCommunities);
    }
  }

  @Nonnull
  @JsonProperty(PROP_PREFIX_SPACE)
  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE)
  public boolean getComplementPrefixSpace() {
    return _complementPrefixSpace;
  }

  @Nonnull
  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public IntegerSpace getLocalPref() {
    return _localPref;
  }

  @Nonnull
  @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR)
  public IntegerSpace getMed() {
    return _med;
  }

  @Nonnull
  @JsonProperty(PROP_COMMUNITIES)
  public CommunitySet getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_COMPLEMENT_COMMUNITIES)
  public boolean getComplementCommunities() {
    return _complementCommunities;
  }
}
