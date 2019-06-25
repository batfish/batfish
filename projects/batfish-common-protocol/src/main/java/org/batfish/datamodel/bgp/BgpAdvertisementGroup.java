package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/**
 * A group of BGP advertisements to be injected into the network for a given snapshot. For each
 * session identified by {@link #getRxPeer}, {@link #getTxAs}, and {@link #getTxPeer}: one
 * advertisement is injected for each {@link Prefix} in {@link #getPrefixes} with the attributes
 * specified by the other properties of this class.
 */
@ParametersAreNonnullByDefault
public final class BgpAdvertisementGroup implements Serializable {

  public static final class Builder {

    private AsPath _asPath;

    private String _description;

    private Set<ExtendedCommunity> _extendedCommunities;

    private long _localPreference;

    private long _med;

    private Ip _originator;

    private OriginType _originType;

    private Set<Prefix> _prefixes;

    private Ip _rxPeer;

    private Set<Long> _standardCommunities;

    private Long _txAs;

    private Ip _txPeer;

    private Builder() {
      _description = "";
      _extendedCommunities = ImmutableSet.of();
      _localPreference = DEFAULT_LOCAL_PREFERENCE;
      _med = DEFAULT_MED;
      _originType = DEFAULT_ORIGIN_TYPE;
      _prefixes = ImmutableSet.of();
      _standardCommunities = ImmutableSet.of();
    }

    public @Nonnull BgpAdvertisementGroup build() {
      checkArgument(_asPath != null, "Missing %s", PROP_AS_PATH);
      checkArgument(!_prefixes.isEmpty(), "%s must be nonempty", PROP_PREFIXES);
      checkArgument(_rxPeer != null, "Missing %s", PROP_RX_PEER);
      checkArgument(_txPeer != null, "Missing %s", PROP_TX_PEER);
      return new BgpAdvertisementGroup(
          _asPath,
          _description,
          _extendedCommunities,
          _localPreference,
          _med,
          firstNonNull(_originator, _txPeer),
          _originType,
          _prefixes,
          _rxPeer,
          _standardCommunities,
          _txAs,
          _txPeer);
    }

    public @Nonnull Builder setAsPath(@Nullable AsPath asPath) {
      _asPath = asPath;
      return this;
    }

    public @Nonnull Builder setDescription(String description) {
      _description = description;
      return this;
    }

    public @Nonnull Builder setExtendedCommunities(Set<ExtendedCommunity> extendedCommunities) {
      _extendedCommunities = ImmutableSet.copyOf(extendedCommunities);
      return this;
    }

    public @Nonnull Builder setLocalPreference(long localPreference) {
      checkArgument(
          localPreference >= 0 && localPreference <= MAX_LOCAL_PREFERENCE,
          "%s must be betwee 0 and %s",
          PROP_LOCAL_PREFERENCE);
      _localPreference = localPreference;
      return this;
    }

    public @Nonnull Builder setMed(long med) {
      checkArgument(med >= 0 && med <= MAX_MED, "%s must be between 0 and %s", PROP_MED, MAX_MED);
      _med = med;
      return this;
    }

    public @Nonnull Builder setOriginator(@Nullable Ip originator) {
      _originator = originator;
      return this;
    }

    public @Nonnull Builder setOriginType(OriginType originType) {
      _originType = originType;
      return this;
    }

    public @Nonnull Builder setPrefixes(Set<Prefix> prefixes) {
      _prefixes = ImmutableSet.copyOf(prefixes);
      return this;
    }

    public @Nonnull Builder setRxPeer(Ip rxPeer) {
      _rxPeer = rxPeer;
      return this;
    }

    public @Nonnull Builder setStandardCommunities(Set<Long> standardCommunities) {
      _standardCommunities = ImmutableSet.copyOf(standardCommunities);
      return this;
    }

    public @Nonnull Builder setTxAs(@Nullable Long txAs) {
      checkArgument(
          txAs == null || (txAs > 0 && txAs <= MAX_AS_NUMBER),
          "%s must be either null or a number between 1 and %s",
          PROP_TX_AS,
          MAX_AS_NUMBER);
      _txAs = txAs;
      return this;
    }

    public @Nonnull Builder setTxPeer(Ip txPeer) {
      _txPeer = txPeer;
      return this;
    }
  }

  private static final long DEFAULT_LOCAL_PREFERENCE = 100L;

  private static final long DEFAULT_MED = 0L;

  private static final OriginType DEFAULT_ORIGIN_TYPE = OriginType.INCOMPLETE;

  private static final long MAX_AS_NUMBER = 0xFFFFFFFFL;

  private static final long MAX_LOCAL_PREFERENCE = 0xFFFFFFFFL;

  private static final long MAX_MED = 0xFFFFFFFFL;
  private static final String PROP_AS_PATH = "asPath";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_EXTENDED_COMMUNITIES = "extendedCommunities";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MED = "med";
  private static final String PROP_ORIGIN_TYPE = "originType";
  private static final String PROP_ORIGINATOR = "originator";
  private static final String PROP_PREFIXES = "prefixes";
  private static final String PROP_RX_PEER = "rxPeer";
  private static final String PROP_STANDARD_COMMUNITIES = "standardCommunities";
  private static final String PROP_TX_AS = "txAs";
  private static final String PROP_TX_PEER = "txPeer";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull BgpAdvertisementGroup create(
      @JsonProperty(PROP_AS_PATH) @Nullable AsPath asPath,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_EXTENDED_COMMUNITIES) @Nullable Set<ExtendedCommunity> extendedCommunities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) @Nullable Long localPreference,
      @JsonProperty(PROP_MED) @Nullable Long med,
      @JsonProperty(PROP_ORIGINATOR) @Nullable Ip originator,
      @JsonProperty(PROP_ORIGIN_TYPE) @Nullable OriginType originType,
      @JsonProperty(PROP_PREFIXES) @Nullable Set<Prefix> prefixes,
      @JsonProperty(PROP_RX_PEER) @Nullable Ip rxPeer,
      @JsonProperty(PROP_STANDARD_COMMUNITIES) @Nullable Set<Long> standardCommunities,
      @JsonProperty(PROP_TX_AS) @Nullable Long txAs,
      @JsonProperty(PROP_TX_PEER) @Nullable Ip txPeer) {
    checkArgument(asPath != null, "Missing %s", PROP_AS_PATH);
    checkArgument(
        localPreference == null
            || (localPreference >= 0 && localPreference <= MAX_LOCAL_PREFERENCE),
        "%s must be between 0 and %s",
        PROP_LOCAL_PREFERENCE,
        MAX_LOCAL_PREFERENCE);
    checkArgument(
        med == null || (med >= 0 && med <= MAX_MED),
        "%s must be between 0 and %s",
        PROP_MED,
        MAX_MED);
    checkArgument(prefixes != null && !prefixes.isEmpty(), "%s must be nonempty", PROP_PREFIXES);
    checkArgument(rxPeer != null, "Missing %s", PROP_RX_PEER);
    checkArgument(
        txAs == null || (txAs > 0 && txAs <= MAX_AS_NUMBER),
        "%s must be either null or a number between 1 and %s",
        PROP_TX_AS,
        MAX_AS_NUMBER);
    checkArgument(txPeer != null, "Missing %s", PROP_TX_PEER);
    return new BgpAdvertisementGroup(
        asPath,
        firstNonNull(description, ""),
        ImmutableSet.copyOf(firstNonNull(extendedCommunities, ImmutableSet.of())),
        firstNonNull(localPreference, DEFAULT_LOCAL_PREFERENCE),
        firstNonNull(med, DEFAULT_MED),
        firstNonNull(originator, txPeer),
        firstNonNull(originType, DEFAULT_ORIGIN_TYPE),
        ImmutableSet.copyOf(prefixes),
        rxPeer,
        ImmutableSet.copyOf(firstNonNull(standardCommunities, ImmutableSet.of())),
        txAs,
        txPeer);
  }

  private final AsPath _asPath;

  private final String _description;

  private final Set<ExtendedCommunity> _extendedCommunities;

  private final long _localPreference;

  private final long _med;

  private final Ip _originator;

  private final OriginType _originType;

  private final Set<Prefix> _prefixes;

  private final Ip _rxPeer;

  private final Set<Long> _standardCommunities;

  private final Long _txAs;

  private final Ip _txPeer;

  private BgpAdvertisementGroup(
      AsPath asPath,
      String description,
      Set<ExtendedCommunity> extendedCommunities,
      long localPreference,
      long med,
      Ip originator,
      OriginType originType,
      Set<Prefix> prefixes,
      Ip rxPeer,
      Set<Long> standardCommunities,
      @Nullable Long txAs,
      Ip txPeer) {
    _asPath = asPath;
    _description = description;
    _extendedCommunities = extendedCommunities;
    _localPreference = localPreference;
    _med = med;
    _originator = originator;
    _originType = originType;
    _prefixes = prefixes;
    _rxPeer = rxPeer;
    _standardCommunities = standardCommunities;
    _txAs = txAs;
    _txPeer = txPeer;
  }

  /** The AS path attribute of the BGP advertisements in this group. */
  @JsonProperty(PROP_AS_PATH)
  public @Nonnull AsPath getAsPath() {
    return _asPath;
  }

  /** A non-functional user-chosen descriptor for the BGP advertisements in this group. */
  @JsonProperty(PROP_DESCRIPTION)
  public @Nonnull String getDescription() {
    return _description;
  }

  /**
   * The set extended communities attribute of the BGP advertisements in this group. Defaults to
   * empty-set if not specified.
   */
  public @Nonnull Set<ExtendedCommunity> getExtendedCommunities() {
    return _extendedCommunities;
  }

  /**
   * The local-preference attribute of the BGP advertisements in this group. Defaults to 100L if not
   * specified at creation time.
   */
  public long getLocalPreference() {
    return _localPreference;
  }

  /**
   * The multi-exit-discriminator attribute of the BGP advertisements in this group. Defaults to 0
   * if not specified at creation time.
   */
  public long getMed() {
    return _med;
  }

  /**
   * The originator {@link Ip} address attribute of the BGP advertisements in this group. Defaults
   * to the IP address of the transmitting peer if not specified at creation time.
   */
  public @Nonnull Ip getOriginator() {
    return _originator;
  }

  /**
   * The origin attribute of the BGP advertisements in this group. Defaults to {@link
   * OriginType#INCOMPLETE} if not specified at creation time.
   */
  public @Nonnull OriginType getOriginType() {
    return _originType;
  }

  /**
   * The set of prefixes to be injected into the session identified by this group. One advertisement
   * will be injected per {@link Prefix} in the set.
   */
  public @Nonnull Set<Prefix> getPrefixes() {
    return _prefixes;
  }

  /**
   * The {@link Ip} address of the receiving peer of the eBGP session into which the advertisements
   * of this group will be injected.
   */
  public @Nonnull Ip getRxPeer() {
    return _rxPeer;
  }

  /**
   * The set of standard communitites in the communities attribute of the BGP advertisements in this
   * group. Defaults to empty-set if not specified.
   */
  public @Nonnull Set<Long> getStandardCommunities() {
    return _standardCommunities;
  }

  /**
   * The AS number of the transmitting peer. Only needs to be supplied if needed to uniquely
   * determine remote AS used by transmitting peer for a dynamic session configured on the receiving
   * peer that allows a range of AS numbers on the remote end.
   */
  public @Nullable Long getTxAs() {
    return _txAs;
  }

  /**
   * The {@link Ip} address of the transmitting peer of the eBGP session into which the
   * advertisements of this group will be injected.
   */
  public @Nonnull Ip getTxPeer() {
    return _txPeer;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BgpAdvertisementGroup)) {
      return false;
    }
    BgpAdvertisementGroup rhs = (BgpAdvertisementGroup) obj;
    return _asPath.equals(rhs._asPath)
        && _description.equals(rhs._description)
        && _extendedCommunities.equals(rhs._extendedCommunities)
        && _localPreference == rhs._localPreference
        && _med == rhs._med
        && _originator.equals(rhs._originator)
        && _originType == rhs._originType
        && _prefixes.equals(rhs._prefixes)
        && _rxPeer.equals(rhs._rxPeer)
        && _standardCommunities.equals(rhs._standardCommunities)
        && Objects.equals(_txAs, rhs._txAs)
        && _txPeer.equals(rhs._txPeer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _asPath,
        _description,
        _extendedCommunities,
        _localPreference,
        _med,
        _originator,
        _originType.ordinal(),
        _prefixes,
        _rxPeer,
        _standardCommunities,
        _txAs,
        _txPeer);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_AS_PATH, _asPath)
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_EXTENDED_COMMUNITIES, _extendedCommunities)
        .add(PROP_LOCAL_PREFERENCE, _localPreference)
        .add(PROP_MED, _med)
        .add(PROP_ORIGINATOR, _originator)
        .add(PROP_ORIGIN_TYPE, _originType)
        .add(PROP_PREFIXES, _prefixes)
        .add(PROP_RX_PEER, _rxPeer)
        .add(PROP_STANDARD_COMMUNITIES, _standardCommunities)
        .add(PROP_TX_AS, _txAs)
        .add(PROP_TX_PEER, _txPeer)
        .toString();
  }
}
