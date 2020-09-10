package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
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
  private static final String PROP_AS_PATH = "asPath";

  // the announcement's prefix must be within this space
  @Nonnull private final PrefixSpace _prefix;
  // if this flag is set, then the prefix must be outside of the above space
  private final boolean _complementPrefix;
  // the announcement's local preference must be within this range
  @Nonnull private final LongSpace _localPreference;
  // the announcement's MED must be within this range
  @Nonnull private final LongSpace _med;
  // the announcement's communities must satisfy these constraints
  @Nonnull private final RegexConstraints _communities;
  // the announcement's AS path must satisfy these constraints
  @Nonnull private final RegexConstraints _asPath;

  private static final LongSpace THIRTY_TWO_BIT_RANGE =
      LongSpace.builder().including(Range.closed(0L, 4294967295L)).build();

  @JsonCreator
  private BgpRouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX) PrefixSpace prefix,
      @JsonProperty(PROP_COMPLEMENT_PREFIX) boolean complementPrefix,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) LongSpace.Builder localPreference,
      @Nullable @JsonProperty(PROP_MED) LongSpace.Builder med,
      @Nullable @JsonProperty(PROP_COMMUNITIES) RegexConstraints communities,
      @Nullable @JsonProperty(PROP_AS_PATH) RegexConstraints asPath) {
    this(
        prefix,
        complementPrefix,
        processBuilder(localPreference),
        processBuilder(med),
        communities,
        asPath);
  }

  private BgpRouteConstraints(
      @Nullable PrefixSpace prefix,
      boolean complementPrefix,
      @Nullable LongSpace localPreference,
      @Nullable LongSpace med,
      @Nullable RegexConstraints communities,
      @Nullable RegexConstraints asPath) {
    _prefix = firstNonNull(prefix, new PrefixSpace());
    _complementPrefix = complementPrefix;
    _localPreference = firstNonNull(localPreference, LongSpace.EMPTY);
    _med = firstNonNull(med, LongSpace.EMPTY);
    _communities = firstNonNull(communities, new RegexConstraints());
    _asPath = firstNonNull(asPath, new RegexConstraints());
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
    private RegexConstraints _communities;
    private RegexConstraints _asPath;

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

    public Builder setCommunities(RegexConstraints communities) {
      _communities = communities;
      return this;
    }

    public Builder setAsPath(RegexConstraints asPath) {
      _asPath = asPath;
      return this;
    }

    public BgpRouteConstraints build() {
      return new BgpRouteConstraints(
          _prefix, _complementPrefix, _localPreference, _med, _communities, _asPath);
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
  public RegexConstraints getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_AS_PATH)
  @Nonnull
  public RegexConstraints getAsPath() {
    return _asPath;
  }
}
