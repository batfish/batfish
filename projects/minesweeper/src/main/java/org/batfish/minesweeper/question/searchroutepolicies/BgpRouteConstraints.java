package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.PrefixSpace;

/** A set of constraints on a BGP route announcement. */
@ParametersAreNonnullByDefault
public class BgpRouteConstraints {

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_COMPLEMENT_PREFIX = "complementPrefix";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MED = "med";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_COMPLEMENT_COMMUNITIES = "complementCommunities";
  private static final String PROP_AS_PATH = "asPath";

  // the announcement's prefix must be within this space
  @Nonnull private final PrefixSpace _prefix;
  // if this flag is set, then the prefix must be outside of the above space
  private final boolean _complementPrefix;
  // the announcement's local preference must be within this range
  @Nonnull private final LongSpace _localPreference;
  // the announcement's MED must be within this range
  @Nonnull private final LongSpace _med;
  // the announcement must be tagged with at least one community matching a regex in this set
  @Nonnull private final Set<String> _communities;
  // if this flag is set, the announcement must not be tagged with any of
  // community matching a regex in the above set
  private final boolean _complementCommunities;
  // the announcement's AS path must match at least one regex in this set
  @Nonnull private final Set<String> _asPath;

  private static final LongSpace THIRTY_TWO_BIT_RANGE =
      LongSpace.builder().including(Range.closed(0L, 4294967295L)).build();

  @JsonCreator
  private BgpRouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX) PrefixSpace prefix,
      @JsonProperty(PROP_COMPLEMENT_PREFIX) boolean complementPrefix,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) LongSpace.Builder localPreference,
      @Nullable @JsonProperty(PROP_MED) LongSpace.Builder med,
      @Nullable @JsonProperty(PROP_COMMUNITIES) Set<String> communities,
      @JsonProperty(PROP_COMPLEMENT_COMMUNITIES) boolean complementCommunities,
      @Nullable @JsonProperty(PROP_AS_PATH) Set<String> asPath) {
    this(
        prefix,
        complementPrefix,
        processBuilder(localPreference),
        processBuilder(med),
        communities,
        complementCommunities,
        asPath);
  }

  private BgpRouteConstraints(
      @Nullable PrefixSpace prefix,
      boolean complementPrefix,
      @Nullable LongSpace localPreference,
      @Nullable LongSpace med,
      @Nullable Set<String> communities,
      boolean complementCommunities,
      @Nullable Set<String> asPath) {
    _prefix = firstNonNull(prefix, new PrefixSpace());
    _complementPrefix = complementPrefix;
    _localPreference = firstNonNull(localPreference, LongSpace.EMPTY);
    _med = firstNonNull(med, LongSpace.EMPTY);
    _communities = firstNonNull(communities, ImmutableSet.of());
    _complementCommunities = complementCommunities;
    _asPath = firstNonNull(asPath, ImmutableSet.of());
    validate(this);
  }

  @VisibleForTesting
  @Nullable
  static LongSpace processBuilder(@Nullable LongSpace.Builder builder) {
    if (builder == null) {
      return null;
    }
    if (builder.hasExclusionsOnly()) {
      return builder.including(THIRTY_TWO_BIT_RANGE).build();
    }
    return builder.build();
  }

  /** Check that the constraints contain valid values. */
  private static void validate(@Nonnull BgpRouteConstraints constraints) {
    LongSpace localPref = constraints.getLocalPreference();
    LongSpace med = constraints.getMed();

    checkArgument(is32BitRange(localPref), "Invalid value for local preference: %s", localPref);
    checkArgument(is32BitRange(med), "Invalid value for MED: %s", med);
  }

  /** Check that the given long space only contains 32-bit integers. */
  @VisibleForTesting
  static boolean is32BitRange(@Nonnull LongSpace longSpace) {
    return THIRTY_TWO_BIT_RANGE.contains(longSpace);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private PrefixSpace _prefix;
    private boolean _complementPrefix = false;
    private LongSpace _localPreference;
    private LongSpace _med;
    private Set<String> _communities;
    private boolean _complementCommunities = false;
    private Set<String> _asPath;

    private Builder() {}

    public Builder setPrefix(PrefixSpace prefix) {
      _prefix = prefix;
      return this;
    }

    public Builder setComplementPrefix(boolean complementPrefix) {
      _complementPrefix = complementPrefix;
      return this;
    }

    public Builder setLocalPreference(LongSpace localPreference) {
      _localPreference = localPreference;
      return this;
    }

    public Builder setMed(LongSpace med) {
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

    public Builder setAsPath(Set<String> asPath) {
      _asPath = asPath;
      return this;
    }

    public BgpRouteConstraints build() {
      return new BgpRouteConstraints(
          _prefix,
          _complementPrefix,
          _localPreference,
          _med,
          _communities,
          _complementCommunities,
          _asPath);
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
  public LongSpace getLocalPreference() {
    return _localPreference;
  }

  @JsonProperty(PROP_MED)
  @Nonnull
  public LongSpace getMed() {
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

  @JsonProperty(PROP_AS_PATH)
  @Nonnull
  public Set<String> getAsPath() {
    return _asPath;
  }
}
