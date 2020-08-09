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

/** A set of constraints on a BGP route announcement. */
@ParametersAreNonnullByDefault
public class BgpRouteConstraints {

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_COMPLEMENT_PREFIX = "complementPrefix";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MULTI_EXIT_DISCRIMINATOR = "multiExitDiscriminator";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_COMPLEMENT_COMMUNITIES = "complementCommunities";

  // the announcement's prefix must be within this space
  @Nonnull private final PrefixSpace _prefix;
  // if this flag is set, then the prefix must be outside of the above space
  private final boolean _complementPrefix;
  // the announcement's local preference must be within this range
  @Nonnull private final IntegerSpace _localPref;
  // the announcement's MED must be within this range
  @Nonnull private final IntegerSpace _med;
  // the announcement must be tagged with at least one community matching a regex in this set
  @Nonnull private final Set<String> _communities;
  // if this flag is set, the announcement must not be tagged with any of
  // community matching a regex in the above set
  private final boolean _complementCommunities;

  @JsonCreator
  private BgpRouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX) PrefixSpace prefix,
      @JsonProperty(PROP_COMPLEMENT_PREFIX) boolean complementPrefix,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) IntegerSpace localPref,
      @Nullable @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR) IntegerSpace med,
      @Nullable @JsonProperty(PROP_COMMUNITIES) Set<String> communities,
      @JsonProperty(PROP_COMPLEMENT_COMMUNITIES) boolean complementCommunities) {
    _prefix = firstNonNull(prefix, new PrefixSpace());
    _complementPrefix = complementPrefix;
    _localPref = firstNonNull(localPref, IntegerSpace.EMPTY);
    _med = firstNonNull(med, IntegerSpace.EMPTY);
    _communities = firstNonNull(communities, ImmutableSet.of());
    _complementCommunities = complementCommunities;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private PrefixSpace _prefix;
    private boolean _complementPrefix = false;
    private IntegerSpace _localPref;
    private IntegerSpace _med;
    private Set<String> _communities;
    private boolean _complementCommunities = false;

    private Builder() {}

    public Builder setPrefix(PrefixSpace prefix) {
      _prefix = prefix;
      return this;
    }

    public Builder setComplementPrefix(boolean complementPrefix) {
      _complementPrefix = complementPrefix;
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

    public Builder setCommunities(Set<String> communities) {
      _communities = communities;
      return this;
    }

    public Builder setComplementCommunities(boolean complementCommunities) {
      _complementCommunities = complementCommunities;
      return this;
    }

    public BgpRouteConstraints build() {
      return new BgpRouteConstraints(
          _prefix, _complementPrefix, _localPref, _med, _communities, _complementCommunities);
    }
  }

  @JsonProperty(PROP_PREFIX)
  @Nonnull
  public PrefixSpace getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_COMPLEMENT_PREFIX)
  public boolean getComplementPrefix() {
    return _complementPrefix;
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

  @JsonProperty(PROP_COMMUNITIES)
  @Nonnull
  public Set<String> getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_COMPLEMENT_COMMUNITIES)
  public boolean getComplementCommunities() {
    return _complementCommunities;
  }
}
