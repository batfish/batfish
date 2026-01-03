package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.bgp.BgpTopologyUtils.computeAsPair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.BgpTopologyUtils.AsPair;
import org.batfish.datamodel.bgp.BgpTopologyUtils.ConfedSessionType;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;

/**
 * Represents properties of a peering session between two {@link BgpPeerConfig}s (usually associated
 * with a {@link org.batfish.datamodel.bgp.BgpTopology.EdgeId bgp edge}).
 *
 * <p>Intended use:
 *
 * <ul>
 *   <li>For this session to be created, two configurations must be deemed compatible.
 *   <li>Some properties of the session are directional (such as local/remote IPs) and therefore
 *       must be reciprocal if the edge is reversed. Others are negotiated by devices and therefore
 *       must be equal in both directions. See {@link #from(BgpPeerConfig, Ip, BgpPeerConfig,
 *       boolean, long, long, ConfedSessionType)} for more details.
 * </ul>
 */
@ParametersAreNonnullByDefault
public final class BgpSessionProperties {

  private static final String PROP_ADDITIONAL_PATHS = "additionalPaths";
  private static final String PROP_ADDRESS_FAMILIES = "addressFamilies";
  private static final String PROP_ADVERTISE_EXTERNAL = "advertiseExternal";
  private static final String PROP_ADVERTISE_INACTIVE = "advertiseInactive";
  private static final String PROP_HEAD_AS = "headAs";
  private static final String PROP_HEAD_IP = "headIp";
  private static final String PROP_ROUTE_EXCHANGE = "routeExchange";
  private static final String PROP_SESSION_TYPE = "sessionType";
  private static final String PROP_TAIL_AS = "tailAs";
  private static final String PROP_TAIL_IP = "tailIp";
  private static final String PROP_CONFEDERATION_TYPE = "confederationType";

  private static final String PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT = "replaceNonLocalAsesOnExport";

  @JsonCreator
  private static @Nonnull BgpSessionProperties create(
      @JsonProperty(PROP_ADDRESS_FAMILIES) @Nullable Collection<Type> addressFamilies,
      @JsonProperty(PROP_HEAD_AS) @Nullable Long remoteAs,
      @JsonProperty(PROP_HEAD_IP) @Nullable Ip remoteIp,
      @JsonProperty(PROP_ROUTE_EXCHANGE) @Nullable
          Map<AddressFamily.Type, RouteExchange> routeExchange,
      @JsonProperty(PROP_SESSION_TYPE) @Nullable SessionType sessionType,
      @JsonProperty(PROP_TAIL_AS) @Nullable Long localAs,
      @JsonProperty(PROP_TAIL_IP) @Nullable Ip localIp,
      @JsonProperty(PROP_CONFEDERATION_TYPE) @Nullable ConfedSessionType type,
      @JsonProperty(PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT) @Nullable
          Boolean replaceNonLocalAsesOnExport) {
    checkArgument(localAs != null, "Missing %s", PROP_TAIL_AS);
    checkArgument(remoteAs != null, "Missing %s", PROP_HEAD_AS);
    checkArgument(remoteIp != null, "Missing %s", PROP_HEAD_IP);
    checkArgument(localIp != null, "Missing %s", PROP_TAIL_IP);
    checkArgument(type != null, "Missing %s", PROP_CONFEDERATION_TYPE);
    return new BgpSessionProperties(
        firstNonNull(addressFamilies, ImmutableSet.of()),
        firstNonNull(routeExchange, ImmutableMap.of()),
        localAs,
        remoteAs,
        localIp,
        remoteIp,
        firstNonNull(sessionType, SessionType.UNSET),
        type,
        firstNonNull(replaceNonLocalAsesOnExport, Boolean.FALSE));
  }

  public static final class Builder {

    private @Nullable Collection<EvpnAddressFamily.Type> _addressFamilies;
    private @Nullable Long _localAs;
    private @Nullable Long _remoteAs;
    private @Nullable Ip _localIp;
    private @Nullable Ip _remoteIp;
    private @Nonnull Map<AddressFamily.Type, RouteExchange> _routeExchangeSettings;
    private @Nonnull SessionType _sessionType;
    private @Nonnull ConfedSessionType _confedSessionType = ConfedSessionType.NO_CONFED;

    private boolean _replaceNonLocalAsesOnExport;

    private Builder() {
      _routeExchangeSettings = new EnumMap<>(AddressFamily.Type.class);
      _sessionType = SessionType.UNSET;
    }

    public @Nonnull BgpSessionProperties build() {
      checkArgument(_remoteIp != null, "Missing %s", PROP_HEAD_IP);
      checkArgument(_localIp != null, "Missing %s", PROP_TAIL_IP);
      checkArgument(_localAs != null, "Missing %s", PROP_TAIL_AS);
      checkArgument(_remoteAs != null, "Missing %s", PROP_HEAD_AS);
      return new BgpSessionProperties(
          firstNonNull(_addressFamilies, ImmutableSet.of()),
          _routeExchangeSettings,
          _localAs,
          _remoteAs,
          _localIp,
          _remoteIp,
          _sessionType,
          _confedSessionType,
          _replaceNonLocalAsesOnExport);
    }

    public @Nonnull Builder setAddressFamilies(Collection<Type> addressFamilies) {
      _addressFamilies = addressFamilies;
      return this;
    }

    public Builder setLocalAs(long localAs) {
      _localAs = localAs;
      return this;
    }

    public Builder setRemoteAs(long remoteAs) {
      _remoteAs = remoteAs;
      return this;
    }

    public @Nonnull Builder setLocalIp(Ip localIp) {
      _localIp = localIp;
      return this;
    }

    public @Nonnull Builder setRemoteIp(Ip remoteIp) {
      _remoteIp = remoteIp;
      return this;
    }

    public Builder setRouteExchangeSettings(Map<Type, RouteExchange> routeExchangeSettings) {
      _routeExchangeSettings = routeExchangeSettings;
      return this;
    }

    public @Nonnull Builder setSessionType(SessionType sessionType) {
      _sessionType = sessionType;
      return this;
    }

    public Builder setConfedSessionType(ConfedSessionType confedSessionType) {
      _confedSessionType = confedSessionType;
      return this;
    }

    public @Nonnull Builder setReplaceNonLocalAsesOnExport(boolean replaceNonLocalAsesOnExport) {
      _replaceNonLocalAsesOnExport = replaceNonLocalAsesOnExport;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull Set<AddressFamily.Type> _addressFamilies;
  private final long _localAs;
  private final long _remoteAs;
  private final @Nonnull Ip _localIp;
  private final @Nonnull Ip _remoteIp;
  private final SessionType _sessionType;
  private final @Nonnull Map<Type, RouteExchange> _routeExchangeSettings;
  private final @Nonnull ConfedSessionType _confedSessionType;

  private final boolean _replaceNonLocalAsesOnExport;

  private BgpSessionProperties(
      Collection<Type> addressFamilies,
      Map<Type, RouteExchange> routeExchangeSettings,
      long localAs,
      long remoteAs,
      Ip localIp,
      Ip remoteIp,
      SessionType sessionType,
      ConfedSessionType confedType,
      boolean replaceNonLocalAsesOnExport) {
    _addressFamilies = Sets.immutableEnumSet(addressFamilies);
    _routeExchangeSettings = ImmutableMap.copyOf(routeExchangeSettings);
    _localAs = localAs;
    _remoteAs = remoteAs;
    _localIp = localIp;
    _remoteIp = remoteIp;
    _sessionType = sessionType;
    _confedSessionType = confedType;
    _replaceNonLocalAsesOnExport = replaceNonLocalAsesOnExport;
  }

  /**
   * When this is set for Ipv4 unicast, add best eBGP path independently of whether it is preempted
   * by an iBGP or IGP route. Only applicable to iBGP sessions.
   */
  @JsonIgnore
  public boolean getAdvertiseExternal() {
    return Optional.ofNullable(_routeExchangeSettings.get(Type.IPV4_UNICAST))
        .map(RouteExchange::getAdvertiseExternal)
        .orElse(Boolean.FALSE);
  }

  /**
   * When this is true for Ipv4 unicast, add best BGP path independently of whether it is preempted
   * by an IGP route. Only applicable to eBGP sessions.
   */
  @JsonIgnore
  public boolean getAdvertiseInactive() {
    return Optional.ofNullable(_routeExchangeSettings.get(Type.IPV4_UNICAST))
        .map(RouteExchange::getAdvertiseInactive)
        .orElse(Boolean.FALSE);
  }

  /** When this is true for Ipv4 unicast, advertise all paths from the multipath RIB */
  @JsonIgnore
  public boolean getAdditionalPaths() {
    return Optional.ofNullable(_routeExchangeSettings.get(Type.IPV4_UNICAST))
        .map(RouteExchange::getAdditionalPaths)
        .orElse(Boolean.FALSE);
  }

  /**
   * When true, replace every AS-path element with the singleton element of the local AS as the last
   * step post-export. Only applicable to eBGP sessions.
   */
  @JsonProperty(PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT)
  public boolean getReplaceNonLocalAsesOnExport() {
    return _replaceNonLocalAsesOnExport;
  }

  /** Whether this session is eBGP. */
  @JsonIgnore
  public boolean isEbgp() {
    return SessionType.isEbgp(_sessionType);
  }

  @JsonProperty(PROP_TAIL_AS)
  public long getLocalAs() {
    return _localAs;
  }

  @JsonProperty(PROP_HEAD_AS)
  public long getRemoteAs() {
    return _remoteAs;
  }

  /** IP of local peer for this session */
  @JsonProperty(PROP_TAIL_IP)
  public @Nonnull Ip getLocalIp() {
    return _localIp;
  }

  /** IP of remote peer for this session */
  @JsonProperty(PROP_HEAD_IP)
  public @Nonnull Ip getRemoteIp() {
    return _remoteIp;
  }

  /** Return this session's {@link SessionType}. */
  @JsonProperty(PROP_SESSION_TYPE)
  public SessionType getSessionType() {
    return _sessionType;
  }

  @JsonProperty(PROP_CONFEDERATION_TYPE)
  public @Nonnull ConfedSessionType getConfedSessionType() {
    return _confedSessionType;
  }

  /** Returns true if this session is IBGP or eBGP within a confederation */
  private boolean isIbgpOrWithinConfed() {
    return !isEbgp() || _confedSessionType == ConfedSessionType.WITHIN_CONFED;
  }

  /**
   * Whether or not this session type requires propagating unchanged MED values (i.e., IBGP or eBGP
   * within a confederation)
   */
  public boolean advertiseUnchangedMed() {
    return isIbgpOrWithinConfed();
  }

  /**
   * Whether or not this session type requires propagating unchanged local preference values (i.e.,
   * IBGP or eBGP within a confederation)
   */
  public boolean advertiseUnchangedLocalPref() {
    return isIbgpOrWithinConfed();
  }

  /**
   * Return the set of address family types for which the NLRIs (i.e., routes) can be exchanged over
   * this session
   */
  @JsonProperty(PROP_ADDRESS_FAMILIES)
  public @Nonnull Set<Type> getAddressFamilies() {
    return _addressFamilies;
  }

  /** Get the route exchange settings, keyed by address family type */
  @JsonProperty(PROP_ROUTE_EXCHANGE)
  public @Nonnull Map<Type, RouteExchange> getRouteExchangeSettings() {
    return _routeExchangeSettings;
  }

  /**
   * Create a set of new parameters based on session initiator and listener. This assumes that
   * provided {@link BgpPeerConfig configs} are compatible.
   *
   * <p>For session parameters that are directional (e.g., IPs used), complicated inference is made.
   *
   * <p><b>Note</b> that some parameters (such as {@link #isEbgp()} will be determined based on the
   * configuration of the initiator only.
   *
   * @param reverseDirection Whether to create the session properties for reverse direction
   *     (listener to initiator) rather than forwards direction (initiator to listener)
   */
  public static BgpSessionProperties from(
      BgpPeerConfig initiator,
      Ip initiatorIp,
      BgpPeerConfig listener,
      boolean reverseDirection,
      long initiatorLocalAs,
      long listenerLocalAs,
      ConfedSessionType confedSessionType) {
    Ip listenerIp = listener.getLocalIp();
    if (listenerIp == null || Ip.AUTO.equals(listenerIp)) {
      // Determine listener's IP from initiator.
      // Listener must be active or dynamic, because unnumbered peers always have localIP defined.
      // Therefore initiator must be active since unnumbered peers only peer with each other.
      assert initiator instanceof BgpActivePeerConfig;
      listenerIp = ((BgpActivePeerConfig) initiator).getPeerAddress();
    }
    assert listenerIp != null;

    SessionType sessionType = getSessionType(initiator);

    EnumSet<Type> addressFamilyIntersection = getAddressFamilyIntersection(initiator, listener);
    BgpPeerConfig directionalSender = reverseDirection ? listener : initiator;
    BgpPeerConfig directionalReceiver = reverseDirection ? initiator : listener;
    return new BgpSessionProperties(
        addressFamilyIntersection,
        addressFamilyIntersection.contains(Type.IPV4_UNICAST)
            ? ImmutableMap.of(
                Type.IPV4_UNICAST,
                new RouteExchange(
                    computeAdditionalPaths(directionalSender, directionalReceiver, sessionType),
                    !SessionType.isEbgp(sessionType)
                        && directionalSender
                            .getIpv4UnicastAddressFamily()
                            .getAddressFamilyCapabilities()
                            .getAdvertiseExternal(),
                    SessionType.isEbgp(sessionType)
                        && directionalSender
                            .getIpv4UnicastAddressFamily()
                            .getAddressFamilyCapabilities()
                            .getAdvertiseInactive()))
            : ImmutableMap.of(),
        reverseDirection ? listenerLocalAs : initiatorLocalAs,
        reverseDirection ? initiatorLocalAs : listenerLocalAs,
        reverseDirection ? listenerIp : initiatorIp,
        reverseDirection ? initiatorIp : listenerIp,
        sessionType,
        confedSessionType,
        directionalSender.getReplaceNonLocalAsesOnExport());
  }

  /** For test use only. */
  @VisibleForTesting
  public static BgpSessionProperties from(
      BgpPeerConfig initiator, BgpPeerConfig listener, boolean reverseDirection) {

    // Both local ASNs must be nonnull for BgpPeerConfig#hasCompatibleRemoteAsns to have passed.
    long initiatorLocalAs = checkNotNull(initiator.getLocalAs());
    Ip initiatorLocalIp = checkNotNull(initiator.getLocalIp());
    long listenerLocalAs = checkNotNull(listener.getLocalAs());
    AsPair asPair =
        checkNotNull(
            computeAsPair(
                initiatorLocalAs,
                initiator.getConfederationAsn(),
                initiator.getRemoteAsns(),
                listenerLocalAs,
                listener.getConfederationAsn(),
                listener.getRemoteAsns()));
    return from(
        initiator,
        initiatorLocalIp,
        listener,
        reverseDirection,
        asPair.getLocalAs(),
        asPair.getRemoteAs(),
        asPair.getConfedSessionType());
  }

  /** Computes whether two peers have compatible configuration to enable add-path */
  private static boolean computeAdditionalPaths(
      BgpPeerConfig sender, BgpPeerConfig receiver, SessionType sessionType) {
    // TODO: support address families other than IPv4 unicast
    Ipv4UnicastAddressFamily senderAF = sender.getIpv4UnicastAddressFamily();
    Ipv4UnicastAddressFamily receiverAF = receiver.getIpv4UnicastAddressFamily();
    if (senderAF == null || receiverAF == null) {
      return false;
    }
    AddressFamilyCapabilities senderCapabilities = senderAF.getAddressFamilyCapabilities();
    return !SessionType.isEbgp(sessionType)
        && receiverAF.getAddressFamilyCapabilities().getAdditionalPathsReceive()
        && senderCapabilities.getAdditionalPathsSend()
        && senderCapabilities.getAdditionalPathsSelectAll();
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
    LongSpace remoteAsns = initiator.getRemoteAsns();
    if (initiator.getLocalAs() == null || remoteAsns.isEmpty()) {
      return SessionType.UNSET;
    }
    if (initiator.getConfederationAsn() != null) {
      // eBGP vs iBGP cases:
      // 1. iBGP within confederation: Remote ASNs contains exactly the local (sub-confed) AS
      // 2. iBGP across confederation boundary: Remote ASNs contains exactly the confederation AS
      // 3. (either within or across confederation) eBGP: Remote ASNs contains anything else
      // TODO: What if remote ASNs contains (local or confed AS) and also something else?
      if (remoteAsns.equals(LongSpace.of(initiator.getLocalAs()))) {
        return getSessionType(initiator, initiator.getLocalAs());
      } else if (remoteAsns.equals(LongSpace.of(initiator.getConfederationAsn()))) {
        return getSessionType(initiator, initiator.getConfederationAsn());
      }
    }
    return getSessionType(initiator, initiator.getLocalAs());
  }

  /**
   * Determine what type of session {@code initiator} is configured to establish under an assumption
   * about which local AS it will use for peerings.
   *
   * @param initiator the {@link BgpPeerConfig} representing the connection initiator.
   * @param initiatorAs the local AS the initiator is assumed to use
   * @return a {@link SessionType} the initiator is configured to establish.
   */
  private static @Nonnull SessionType getSessionType(BgpPeerConfig initiator, long initiatorAs) {
    assert initiator.getLocalAs() != null;
    assert Objects.equals(initiatorAs, initiator.getLocalAs())
        || Objects.equals(initiatorAs, initiator.getConfederationAsn());
    if (initiator instanceof BgpUnnumberedPeerConfig) {
      return initiator.getRemoteAsns().equals(LongSpace.of(initiatorAs))
          ? SessionType.IBGP_UNNUMBERED
          : SessionType.EBGP_UNNUMBERED;
    }
    return initiator.getRemoteAsns().equals(LongSpace.of(initiatorAs))
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
    return _remoteAs == that._remoteAs
        && _localAs == that._localAs
        && _remoteIp.equals(that._remoteIp)
        && _localIp.equals(that._localIp)
        && _sessionType == that._sessionType
        && _routeExchangeSettings.equals(that._routeExchangeSettings)
        && _addressFamilies.equals(that._addressFamilies)
        && _confedSessionType == that._confedSessionType
        && _replaceNonLocalAsesOnExport == that._replaceNonLocalAsesOnExport;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _addressFamilies,
        _routeExchangeSettings,
        _remoteAs,
        _localAs,
        _remoteIp,
        _localIp,
        _sessionType.ordinal(),
        _confedSessionType.ordinal(),
        _replaceNonLocalAsesOnExport);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("addressFamilies", _addressFamilies)
        .add("routeExchangeSettings", _routeExchangeSettings)
        .add("localAs", _localAs)
        .add("remoteAs", _remoteAs)
        .add("localIp", _localIp)
        .add("remoteIp", _remoteIp)
        .add("sessionType", _sessionType)
        .add("confedSessionType", _confedSessionType)
        .add("replaceNonLocalAsesOnExport", _replaceNonLocalAsesOnExport)
        .toString();
  }

  /** Different types of BGP sessions */
  public enum SessionType {
    // these should all be upper case for the parse function below to work
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

    public static SessionType parse(String sessionType) {
      return Enum.valueOf(SessionType.class, sessionType.toUpperCase());
    }
  }

  public static final class RouteExchange {
    private final boolean _additionalPaths;
    private final boolean _advertiseExternal;
    private final boolean _advertiseInactive;

    @JsonCreator
    @VisibleForTesting
    RouteExchange(
        @JsonProperty(PROP_ADDITIONAL_PATHS) boolean additionalPaths,
        @JsonProperty(PROP_ADVERTISE_EXTERNAL) boolean advertiseExternal,
        @JsonProperty(PROP_ADVERTISE_INACTIVE) boolean advertiseInactive) {
      _additionalPaths = additionalPaths;
      _advertiseExternal = advertiseExternal;
      _advertiseInactive = advertiseInactive;
    }

    /**
     * When this is set, add best eBGP path independently of whether it is preempted by an iBGP or
     * IGP route. Only applicable to iBGP sessions.
     */
    @JsonProperty(PROP_ADVERTISE_EXTERNAL)
    public boolean getAdvertiseExternal() {
      return _advertiseExternal;
    }

    /**
     * When this is true, add best BGP path independently of whether it is preempted by an IGP
     * route. Only applicable to eBGP sessions.
     */
    @JsonProperty(BgpSessionProperties.PROP_ADVERTISE_INACTIVE)
    public boolean getAdvertiseInactive() {
      return _advertiseInactive;
    }

    /** When this is true, advertise all paths from the multipath RIB */
    @JsonProperty(BgpSessionProperties.PROP_ADDITIONAL_PATHS)
    public boolean getAdditionalPaths() {
      return _additionalPaths;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof RouteExchange)) {
        return false;
      }
      RouteExchange that = (RouteExchange) o;
      return _additionalPaths == that._additionalPaths
          && _advertiseExternal == that._advertiseExternal
          && _advertiseInactive == that._advertiseInactive;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_additionalPaths, _advertiseExternal, _advertiseInactive);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("additionalPaths", _additionalPaths)
          .add("advertiseExternal", _advertiseExternal)
          .add("advertiseInactive", _advertiseInactive)
          .toString();
    }
  }
}
