package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.minesweeper.bdd.BDDRoute;

/** A set of constraints on a BGP route announcement. */
@ParametersAreNonnullByDefault
public class BgpRouteConstraints {

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_COMPLEMENT_PREFIX = "complementPrefix";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MED = "med";
  private static final String PROP_TAG = "tag";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_AS_PATH = "asPath";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";
  private static final String PROP_PROTOCOL = "protocol";

  // the announcement's prefix must be within this space
  @Nonnull private final PrefixSpace _prefix;
  // if this flag is set, then the prefix must be outside of the above space
  private final boolean _complementPrefix;
  // the announcement's local preference must be within this range
  @Nonnull private final LongSpace _localPreference;
  // the announcement's MED must be within this range
  @Nonnull private final LongSpace _med;
  // the announcement's tag must be within this range
  @Nonnull private final LongSpace _tag;
  // the announcement's communities must satisfy these constraints
  @Nonnull private final RegexConstraints _communities;
  // the announcement's AS path must satisfy these constraints
  @Nonnull private final RegexConstraints _asPath;
  // the announcement's next-hop IP must be within this prefix;
  // an empty value means that any next-hop IP is ok, including
  // an unset one
  @Nonnull private final Optional<Prefix> _nextHopIp;
  // the announcement's protocol must be a member of this set
  @Nonnull private final Set<RoutingProtocol> _protocol;

  private static final LongSpace THIRTY_TWO_BIT_RANGE =
      LongSpace.builder().including(Range.closed(0L, 4294967295L)).build();

  @JsonCreator
  private BgpRouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX) PrefixSpace prefix,
      @JsonProperty(PROP_COMPLEMENT_PREFIX) boolean complementPrefix,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) LongSpace.Builder localPreference,
      @Nullable @JsonProperty(PROP_MED) LongSpace.Builder med,
      @Nullable @JsonProperty(PROP_TAG) LongSpace.Builder tag,
      @Nullable @JsonProperty(PROP_COMMUNITIES) RegexConstraints communities,
      @Nullable @JsonProperty(PROP_AS_PATH) RegexConstraints asPath,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Prefix nextHopIp,
      @Nullable @JsonProperty(PROP_PROTOCOL) Set<RoutingProtocol> protocol) {
    this(
        prefix,
        complementPrefix,
        processBuilder(localPreference),
        processBuilder(med),
        processBuilder(tag),
        communities,
        asPath,
        nextHopIp,
        protocol);
  }

  private BgpRouteConstraints(
      @Nullable PrefixSpace prefix,
      boolean complementPrefix,
      @Nullable LongSpace localPreference,
      @Nullable LongSpace med,
      @Nullable LongSpace tag,
      @Nullable RegexConstraints communities,
      @Nullable RegexConstraints asPath,
      @Nullable Prefix nextHopIp,
      @Nullable Set<RoutingProtocol> protocol) {
    _prefix = firstNonNull(prefix, new PrefixSpace());
    _complementPrefix = complementPrefix;
    _localPreference = firstNonNull(localPreference, LongSpace.EMPTY);
    _med = firstNonNull(med, LongSpace.EMPTY);
    _tag = firstNonNull(tag, LongSpace.EMPTY);
    _communities = firstNonNull(communities, new RegexConstraints());
    _asPath = firstNonNull(asPath, new RegexConstraints());
    _nextHopIp = Optional.ofNullable(nextHopIp);
    _protocol = firstNonNull(protocol, ImmutableSet.of());
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
    LongSpace tag = constraints.getTag();
    Set<RoutingProtocol> protocol = constraints.getProtocol();

    checkArgument(is32BitRange(localPref), "Invalid value for local preference: %s", localPref);
    checkArgument(is32BitRange(med), "Invalid value for MED: %s", med);
    checkArgument(is32BitRange(tag), "Invalid value for tag: %s", tag);

    checkArgument(isBgpProtocol(protocol), "Invalid value for protocol: %s", protocol);
  }

  /** Check that the given long space only contains 32-bit integers. */
  @VisibleForTesting
  static boolean is32BitRange(@Nonnull LongSpace longSpace) {
    return THIRTY_TWO_BIT_RANGE.contains(longSpace);
  }

  @VisibleForTesting
  static boolean isBgpProtocol(Set<RoutingProtocol> protocol) {
    return BDDRoute.ALL_BGP_PROTOCOLS.containsAll(protocol);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private PrefixSpace _prefix;
    private boolean _complementPrefix = false;
    private LongSpace _localPreference;
    private LongSpace _med;
    private LongSpace _tag;
    private RegexConstraints _communities;
    private RegexConstraints _asPath;
    private Prefix _nextHopIp;
    private Set<RoutingProtocol> _protocol;

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

    public Builder setTag(LongSpace tag) {
      _tag = tag;
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

    public Builder setNextHopIp(Prefix nextHopIp) {
      _nextHopIp = nextHopIp;
      return this;
    }

    public Builder setProtocol(Set<RoutingProtocol> protocol) {
      _protocol = protocol;
      return this;
    }

    public BgpRouteConstraints build() {
      return new BgpRouteConstraints(
          _prefix,
          _complementPrefix,
          _localPreference,
          _med,
          _tag,
          _communities,
          _asPath,
          _nextHopIp,
          _protocol);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      return false;
    }
    BgpRouteConstraints other = (BgpRouteConstraints) o;
    return Objects.equals(_prefix, other._prefix)
        && _complementPrefix == other._complementPrefix
        && Objects.equals(_localPreference, other._localPreference)
        && Objects.equals(_med, other._med)
        && Objects.equals(_tag, other._tag)
        && Objects.equals(_communities, other._communities)
        && Objects.equals(_asPath, other._asPath)
        && Objects.equals(_nextHopIp, other._nextHopIp)
        && Objects.equals(_protocol, other._protocol);
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

  @JsonProperty(PROP_TAG)
  @Nonnull
  public LongSpace getTag() {
    return _tag;
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

  @JsonProperty(PROP_NEXT_HOP_IP)
  @Nonnull
  public Optional<Prefix> getNextHopIp() {
    return _nextHopIp;
  }

  @JsonProperty(PROP_PROTOCOL)
  @Nonnull
  public Set<RoutingProtocol> getProtocol() {
    return _protocol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _prefix,
        _complementPrefix,
        _localPreference,
        _med,
        _tag,
        _communities,
        _asPath,
        _nextHopIp,
        _protocol);
  }
}
