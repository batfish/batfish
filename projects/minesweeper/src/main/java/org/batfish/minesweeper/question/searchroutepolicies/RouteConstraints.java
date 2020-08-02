package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.PrefixSpace;

/** A set of constraints on a route announcement. */
@ParametersAreNonnullByDefault
public class RouteConstraints {

  private static final String PROP_PREFIX_SPACE = "prefixSpace";
  private static final String PROP_COMPLEMENT_PREFIX_SPACE = "complementPrefixSpace";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MULTI_EXIT_DISCRIMINATOR = "multiExitDiscriminator";
  private static final String PROP_COMMUNITY_REGEXES = "communityRegexes";
  private static final String PROP_COMPLEMENT_COMMUNITIES = "complementCommunities";

  // the announcement's prefix must be within this space
  @Nonnull private final PrefixSpace _prefixSpace;
  // if this flag is set, then the prefix must be outside of the above space
  private final boolean _complementPrefixSpace;
  // the announcement's local preference must be within this range
  @Nonnull private final IntegerSpace _localPref;
  // the announcement's MED must be within this range
  @Nonnull private final IntegerSpace _med;
  // the announcement must be tagged with at least one community matching a regex in this set
  @Nonnull private final Set<String> _communityRegexes;
  // if this flag is set, the announcement must not be tagged with any of
  // community matching a regex in the above set
  private final boolean _complementCommunities;

  @JsonCreator
  private RouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX_SPACE) PrefixSpace prefixSpace,
      @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE) boolean complementPrefixSpace,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) IntegerSpace localPref,
      @Nullable @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR) IntegerSpace med,
      @Nullable @JsonProperty(PROP_COMMUNITY_REGEXES) Set<String> communityRegexes,
      @JsonProperty(PROP_COMPLEMENT_COMMUNITIES) boolean complementCommunities) {
    _prefixSpace = firstNonNull(prefixSpace, new PrefixSpace());
    _complementPrefixSpace = complementPrefixSpace;
    _localPref = firstNonNull(localPref, IntegerSpace.EMPTY);
    _med = firstNonNull(med, IntegerSpace.EMPTY);
    _communityRegexes = firstNonNull(communityRegexes, ImmutableSet.of());
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
    private Set<String> _communityRegexes;
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

    public Builder setCommunityRegexes(Set<String> communityRegexes) {
      _communityRegexes = communityRegexes;
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
          _communityRegexes,
          _complementCommunities);
    }
  }

  @JsonProperty(PROP_PREFIX_SPACE)
  @Nonnull
  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE)
  public boolean getComplementPrefixSpace() {
    return _complementPrefixSpace;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  @Nonnull
  public IntegerSpace getLocalPref() {
    return _localPref;
  }

  @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR)
  @Nonnull
  public IntegerSpace getMed() {
    return _med;
  }

  @JsonProperty(PROP_COMMUNITY_REGEXES)
  @Nonnull
  public Set<String> getCommunityRegexes() {
    return _communityRegexes;
  }

  @JsonProperty(PROP_COMPLEMENT_COMMUNITIES)
  public boolean getComplementCommunities() {
    return _complementCommunities;
  }
}
