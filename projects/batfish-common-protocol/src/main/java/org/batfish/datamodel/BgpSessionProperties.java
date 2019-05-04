package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents properties of a peering session between two {@link BgpPeerConfig}s. */
@ParametersAreNonnullByDefault
public final class BgpSessionProperties implements Serializable {

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

  private static final long serialVersionUID = 1L;

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
  public boolean getAdvertiseExternal() {
    return _advertiseExternal;
  }

  /**
   * When this is true, add best BGP path independently of whether it is preempted by an IGP route.
   * Only applicable to eBGP sessions.
   */
  public boolean getAdvertiseInactive() {
    return _advertiseInactive;
  }

  /** When this is true, advertise all paths from the multipath RIB */
  public boolean getAdditionalPaths() {
    return _additionalPaths;
  }

  /** Whether this session is eBGP. */
  public boolean isEbgp() {
    return SessionType.isEbgp(_sessionType);
  }

  /** IP of local peer for this session */
  @Nonnull
  public Ip getTailIp() {
    return _tailIp;
  }

  /** IP of remote peer for this session */
  @Nonnull
  public Ip getHeadIp() {
    return _headIp;
  }

  /** Return this session's {@link SessionType}. */
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
        SessionType.isEbgp(sessionType) && initiator.getAdvertiseInactive(),
        !SessionType.isEbgp(sessionType) && initiator.getAdvertiseExternal(),
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
