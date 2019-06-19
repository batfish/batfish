package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents properties of a peering session between two {@link BgpPeerConfig}s. */
@ParametersAreNonnullByDefault
public final class BgpSessionProperties {

  private static final String PROP_ADDITIONAL_PATHS = "additionalPaths";
  private static final String PROP_ADVERTISE_EXTERNAL = "advertiseExternal";
  private static final String PROP_ADVERTISE_INACTIVE = "advertiseInactive";
  private static final String PROP_HEAD_IP = "headIp";
  private static final String PROP_SESSION_TYPE = "sessionType";
  private static final String PROP_TAIL_IP = "tailIp";

  @JsonCreator
  private static @Nonnull BgpSessionProperties create(
      @JsonProperty(PROP_ADDITIONAL_PATHS) boolean additionalPaths,
      @JsonProperty(PROP_ADVERTISE_EXTERNAL) boolean advertiseExternal,
      @JsonProperty(PROP_ADVERTISE_INACTIVE) boolean advertiseInactive,
      @JsonProperty(PROP_HEAD_IP) @Nullable Ip headIp,
      @JsonProperty(PROP_SESSION_TYPE) @Nullable SessionType sessionType,
      @JsonProperty(PROP_TAIL_IP) @Nullable Ip tailIp) {
    checkArgument(headIp != null, "Missing %s", PROP_HEAD_IP);
    checkArgument(tailIp != null, "Missing %s", PROP_TAIL_IP);
    return new BgpSessionProperties(
        additionalPaths,
        advertiseExternal,
        advertiseInactive,
        tailIp,
        headIp,
        firstNonNull(sessionType, SessionType.UNSET));
  }

  public static final class Builder {

    private boolean _additionalPaths;
    private boolean _advertiseExternal;
    private boolean _advertiseInactive;
    private @Nullable Ip _tailIp;
    private @Nullable Ip _headIp;
    private @Nonnull SessionType _sessionType;

    private Builder() {
      _sessionType = SessionType.UNSET;
    }

    public @Nonnull BgpSessionProperties build() {
      checkArgument(_headIp != null, "Missing headIp");
      checkArgument(_tailIp != null, "Missing tailIp");
      return new BgpSessionProperties(
          _additionalPaths, _advertiseExternal, _advertiseInactive, _tailIp, _headIp, _sessionType);
    }

    public @Nonnull Builder setAdditionalPaths(boolean additionalPaths) {
      _additionalPaths = additionalPaths;
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
  private final boolean _advertiseExternal;
  private final boolean _advertiseInactive;
  @Nonnull private final Ip _tailIp;
  @Nonnull private final Ip _headIp;
  private final SessionType _sessionType;

  private BgpSessionProperties(
      boolean additionalPaths,
      boolean advertiseExternal,
      boolean advertiseInactive,
      @Nonnull Ip tailIp,
      @Nonnull Ip headIp,
      SessionType sessionType) {
    _additionalPaths = additionalPaths;
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
   * Create a set of new parameters based on session initiator and listener. <b>Note</b> that some
   * parameters (such as {@link #isEbgp()} will be determined based on the configuration of the
   * initiator only.
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
    return new BgpSessionProperties(
        !SessionType.isEbgp(sessionType)
            && listener.getAdditionalPathsReceive()
            && initiator.getAdditionalPathsSend()
            && initiator.getAdditionalPathsSelectAll(),
        !SessionType.isEbgp(sessionType) && initiator.getAdvertiseExternal(),
        SessionType.isEbgp(sessionType) && initiator.getAdvertiseInactive(),
        reverseDirection ? listenerIp : initiatorIp,
        reverseDirection ? initiatorIp : listenerIp,
        sessionType);
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
    return _additionalPaths == that._additionalPaths
        && _advertiseExternal == that._advertiseExternal
        && _advertiseInactive == that._advertiseInactive
        && _sessionType == that._sessionType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _additionalPaths, _advertiseExternal, _advertiseInactive, _sessionType.ordinal());
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
