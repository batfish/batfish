package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.EvpnAddressFamily;

/**
 * Represents properties of a peering session between two {@link BgpPeerConfig}s (usually associated
 * with a {@link org.batfish.datamodel.bgp.BgpTopology.EdgeId bgp edge}).
 *
 * <p>Intended use:
 *
 * <ul>
 *   <li>For this session to be created, two configurations must be deemed compatible.
 *   <li>Some properties of the session are directional (such as head/tail IPs) and therefore must
 *       be reciprocal if the edge is reversed. Others are negotiated by devices and therefore must
 *       be equal in both directions. See {@link #from(BgpPeerConfig, BgpPeerConfig, boolean)} for
 *       more details.
 * </ul>
 */
@ParametersAreNonnullByDefault
public final class BgpSessionProperties {

  private static final String PROP_ADDITIONAL_PATHS = "additionalPaths";
  private static final String PROP_ADDRESS_FAMILIES = "addressFamilies";
  private static final String PROP_ADVERTISE_EXTERNAL = "advertiseExternal";
  private static final String PROP_ADVERTISE_INACTIVE = "advertiseInactive";
  private static final String PROP_HEAD_IP = "headIp";
  private static final String PROP_SESSION_TYPE = "sessionType";
  private static final String PROP_TAIL_IP = "tailIp";

  @JsonCreator
  private static @Nonnull BgpSessionProperties create(
      @JsonProperty(PROP_ADDITIONAL_PATHS) boolean additionalPaths,
      @JsonProperty(PROP_ADDRESS_FAMILIES) @Nullable Collection<Type> addressFamilies,
      @JsonProperty(PROP_ADVERTISE_EXTERNAL) boolean advertiseExternal,
      @JsonProperty(PROP_ADVERTISE_INACTIVE) boolean advertiseInactive,
      @JsonProperty(PROP_HEAD_IP) @Nullable Ip headIp,
      @JsonProperty(PROP_SESSION_TYPE) @Nullable SessionType sessionType,
      @JsonProperty(PROP_TAIL_IP) @Nullable Ip tailIp) {
    checkArgument(headIp != null, "Missing %s", PROP_HEAD_IP);
    checkArgument(tailIp != null, "Missing %s", PROP_TAIL_IP);
    return new BgpSessionProperties(
        additionalPaths,
        firstNonNull(addressFamilies, ImmutableSet.of()),
        advertiseExternal,
        advertiseInactive,
        tailIp,
        headIp,
        firstNonNull(sessionType, SessionType.UNSET));
  }

  public static final class Builder {

    private boolean _additionalPaths;
    @Nullable private Collection<EvpnAddressFamily.Type> _addressFamilies;
    private boolean _advertiseExternal;
    private boolean _advertiseInactive;
    private @Nullable Ip _tailIp;
    private @Nullable Ip _headIp;
    private @Nonnull SessionType _sessionType;

    private Builder() {
      _sessionType = SessionType.UNSET;
    }

    public @Nonnull BgpSessionProperties build() {
      checkArgument(_headIp != null, "Missing %s", PROP_HEAD_IP);
      checkArgument(_tailIp != null, "Missing %s", PROP_TAIL_IP);
      return new BgpSessionProperties(
          _additionalPaths,
          firstNonNull(_addressFamilies, ImmutableSet.of()),
          _advertiseExternal,
          _advertiseInactive,
          _tailIp,
          _headIp,
          _sessionType);
    }

    public @Nonnull Builder setAdditionalPaths(boolean additionalPaths) {
      _additionalPaths = additionalPaths;
      return this;
    }

    @Nonnull
    public Builder setAddressFamilies(Collection<Type> addressFamilies) {
      _addressFamilies = addressFamilies;
      return this;
    }

    public @Nonnull Builder setAdvertiseExternal(boolean advertiseExternal) {
      _advertiseExternal = advertiseExternal;
      return this;
    }

    public @Nonnull Builder setAdvertiseInactive(boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
      return this;
    }

    public @Nonnull Builder setTailIp(Ip tailIp) {
      _tailIp = tailIp;
      return this;
    }

    public @Nonnull Builder setHeadIp(Ip headIp) {
      _headIp = headIp;
      return this;
    }

    public @Nonnull Builder setSessionType(SessionType sessionType) {
      _sessionType = sessionType;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final boolean _additionalPaths;
  @Nonnull private final Set<EvpnAddressFamily.Type> _addressFamilies;
  private final boolean _advertiseExternal;
  private final boolean _advertiseInactive;
  @Nonnull private final Ip _tailIp;
  @Nonnull private final Ip _headIp;
  private final SessionType _sessionType;

  private BgpSessionProperties(
      boolean additionalPaths,
      Collection<AddressFamily.Type> addressFamilies,
      boolean advertiseExternal,
      boolean advertiseInactive,
      Ip tailIp,
      Ip headIp,
      SessionType sessionType) {
    _additionalPaths = additionalPaths;
    _addressFamilies = Sets.immutableEnumSet(addressFamilies);
    _advertiseExternal = advertiseExternal;
    _advertiseInactive = advertiseInactive;
    _tailIp = tailIp;
    _headIp = headIp;
    _sessionType = sessionType;
  }

  /**
   * When this is set, add best eBGP path independently of whether it is preempted by an iBGP or IGP
   * route. Only applicable to iBGP sessions.
   */
  @JsonProperty(PROP_ADVERTISE_EXTERNAL)
  public boolean getAdvertiseExternal() {
    return _advertiseExternal;
  }

  /**
   * When this is true, add best BGP path independently of whether it is preempted by an IGP route.
   * Only applicable to eBGP sessions.
   */
  @JsonProperty(PROP_ADVERTISE_INACTIVE)
  public boolean getAdvertiseInactive() {
    return _advertiseInactive;
  }

  /** When this is true, advertise all paths from the multipath RIB */
  @JsonProperty(PROP_ADDITIONAL_PATHS)
  public boolean getAdditionalPaths() {
    return _additionalPaths;
  }

  /** Whether this session is eBGP. */
  @JsonIgnore
  public boolean isEbgp() {
    return SessionType.isEbgp(_sessionType);
  }

  /** IP of local peer for this session */
  @JsonProperty(PROP_TAIL_IP)
  @Nonnull
  public Ip getTailIp() {
    return _tailIp;
  }

  /** IP of remote peer for this session */
  @JsonProperty(PROP_HEAD_IP)
  @Nonnull
  public Ip getHeadIp() {
    return _headIp;
  }

  /** Return this session's {@link SessionType}. */
  @JsonProperty(PROP_SESSION_TYPE)
  public SessionType getSessionType() {
    return _sessionType;
  }

  /**
   * Return the set of address family types for which the NLRIs (i.e., routes) can be exchanged over
   * this session
   */
  @Nonnull
  @JsonProperty(PROP_ADDRESS_FAMILIES)
  public Set<Type> getAddressFamilies() {
    return _addressFamilies;
  }

  /**
   * Create a set of new parameters based on session initiator and listener. This assumes that
   * provided {@link BgpPeerConfig configs} are compatible.
   *
   * <p>For session parameters that are directional (e.g., IPs used), the {@code initiator} config
   * will be used to fill in the {@code tail} values, unless {@code reverseDirection} is specified.
   * Other parameters are derived from both peer configurations, emulating session negotiation.
   *
   * <p><b>Note</b> that some parameters (such as {@link #isEbgp()} will be determined based on the
   * configuration of the initiator only.
   *
   * @param reverseDirection Whether to create the session properties for reverse direction
   *     (listener to initiator) rather than forwards direction (initiator to listener)
   */
  public static BgpSessionProperties from(
      BgpPeerConfig initiator, BgpPeerConfig listener, boolean reverseDirection) {
    Ip initiatorIp = initiator.getLocalIp();
    Ip listenerIp = listener.getLocalIp();
    if (listenerIp == null || listenerIp == Ip.AUTO) {
      // Determine listener's IP from initiator.
      // Listener must be active or dynamic, because unnumbered peers always have localIP defined.
      // Therefore initiator must be active since unnumbered peers only peer with each other.
      assert initiator instanceof BgpActivePeerConfig;
      listenerIp = ((BgpActivePeerConfig) initiator).getPeerAddress();
    }
    assert initiatorIp != null;
    assert listenerIp != null;

    SessionType sessionType = getSessionType(initiator);

    assert listener.getIpv4UnicastAddressFamily() != null;
    assert initiator.getIpv4UnicastAddressFamily() != null;
    return new BgpSessionProperties(
        !SessionType.isEbgp(sessionType)
            && listener
                .getIpv4UnicastAddressFamily()
                .getAddressFamilySettings()
                .getAdditionalPathsReceive()
            && initiator
                .getIpv4UnicastAddressFamily()
                .getAddressFamilySettings()
                .getAdditionalPathsSend()
            && initiator
                .getIpv4UnicastAddressFamily()
                .getAddressFamilySettings()
                .getAdditionalPathsSelectAll(),
        getAddressFamilyIntersection(initiator, listener),
        !SessionType.isEbgp(sessionType)
            && initiator
                .getIpv4UnicastAddressFamily()
                .getAddressFamilySettings()
                .getAdvertiseExternal(),
        SessionType.isEbgp(sessionType)
            && initiator
                .getIpv4UnicastAddressFamily()
                .getAddressFamilySettings()
                .getAdvertiseInactive(),
        reverseDirection ? listenerIp : initiatorIp,
        reverseDirection ? initiatorIp : listenerIp,
        sessionType);
  }

  @VisibleForTesting
  static EnumSet<Type> getAddressFamilyIntersection(BgpPeerConfig a, BgpPeerConfig b) {
    Set<Type> setA =
        a.getAllAddressFamilies().stream()
            .map(AddressFamily::getType)
            .collect(Sets.toImmutableEnumSet());
    Set<Type> setB =
        b.getAllAddressFamilies().stream()
            .map(AddressFamily::getType)
            .collect(Sets.toImmutableEnumSet());
    Collection<Type> intersection = Sets.intersection(setA, setB);
    // need to special case empty collection, otherwise exception
    return intersection.isEmpty() ? EnumSet.noneOf(Type.class) : EnumSet.copyOf(intersection);
  }

  /**
   * Determine what type of session {@code initiator} is configured to establish.
   *
   * @param initiator the {@link BgpPeerConfig} representing the connection initiator.
   * @return a {@link SessionType} the initiator is configured to establish.
   */
  public static @Nonnull SessionType getSessionType(BgpPeerConfig initiator) {
    if (initiator.getLocalAs() == null || initiator.getRemoteAsns().isEmpty()) {
      return SessionType.UNSET;
    }
    if (initiator instanceof BgpUnnumberedPeerConfig) {
      return initiator.getRemoteAsns().equals(LongSpace.of(initiator.getLocalAs()))
          ? SessionType.IBGP_UNNUMBERED
          : SessionType.EBGP_UNNUMBERED;
    }
    return initiator.getRemoteAsns().equals(LongSpace.of(initiator.getLocalAs()))
        ? SessionType.IBGP
        : initiator.getEbgpMultihop() ? SessionType.EBGP_MULTIHOP : SessionType.EBGP_SINGLEHOP;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpSessionProperties)) {
      return false;
    }
    BgpSessionProperties that = (BgpSessionProperties) o;
    return _headIp.equals(that._headIp)
        && _tailIp.equals(that._tailIp)
        && _sessionType == that._sessionType
        && _additionalPaths == that._additionalPaths
        && _addressFamilies.equals(that._addressFamilies)
        && _advertiseExternal == that._advertiseExternal
        && _advertiseInactive == that._advertiseInactive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _additionalPaths,
        _addressFamilies,
        _advertiseExternal,
        _advertiseInactive,
        _headIp,
        _tailIp,
        _sessionType.ordinal());
  }

  public enum SessionType {
    IBGP,
    EBGP_SINGLEHOP,
    EBGP_MULTIHOP,
    EBGP_UNNUMBERED,
    IBGP_UNNUMBERED,
    UNSET;

    public static boolean isEbgp(SessionType sessionType) {
      return sessionType == SessionType.EBGP_SINGLEHOP
          || sessionType == SessionType.EBGP_MULTIHOP
          || sessionType == EBGP_UNNUMBERED;
    }
  }
}
