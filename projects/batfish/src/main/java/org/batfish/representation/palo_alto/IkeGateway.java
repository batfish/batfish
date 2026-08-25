package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Palo Alto IKE gateway, configured under {@code network ike gateway}. */
public final class IkeGateway implements Serializable {

  /** How the gateway authenticates its peer. */
  public enum AuthenticationType {
    CERTIFICATE,
    PRE_SHARED_KEY
  }

  private final @Nonnull String _name;

  private @Nullable AuthenticationType _authenticationType;

  private @Nullable String _ikeCryptoProfile;

  private @Nullable InterfaceAddress _localAddress;

  private @Nullable String _localInterface;

  private @Nullable InterfaceAddress _peerAddress;

  public IkeGateway(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Null when the config does not say; PAN-OS defaults to pre-shared key. */
  public @Nullable AuthenticationType getAuthenticationType() {
    return _authenticationType;
  }

  public void setAuthenticationType(@Nullable AuthenticationType authenticationType) {
    _authenticationType = authenticationType;
  }

  public @Nullable String getIkeCryptoProfile() {
    return _ikeCryptoProfile;
  }

  public void setIkeCryptoProfile(@Nullable String ikeCryptoProfile) {
    _ikeCryptoProfile = ikeCryptoProfile;
  }

  /**
   * Explicit {@code local-address ip}, if configured. May be an address-object reference. When
   * null, the local IP is taken from {@link #getLocalInterface()}.
   */
  public @Nullable InterfaceAddress getLocalAddress() {
    return _localAddress;
  }

  public void setLocalAddress(@Nullable InterfaceAddress localAddress) {
    _localAddress = localAddress;
  }

  /** Interface named by {@code local-address interface}. */
  public @Nullable String getLocalInterface() {
    return _localInterface;
  }

  public void setLocalInterface(@Nullable String localInterface) {
    _localInterface = localInterface;
  }

  public @Nullable InterfaceAddress getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(@Nullable InterfaceAddress peerAddress) {
    _peerAddress = peerAddress;
  }
}
