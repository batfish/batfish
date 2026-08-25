package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Palo Alto IKE gateway, configured under {@code network ike gateway}. */
public final class IkeGateway implements Serializable {

  /** IKE version, from {@code protocol version}. */
  public enum Version {
    IKEV1,
    IKEV2,
    IKEV2_PREFERRED
  }

  /** How the peer address is specified. PAN-OS documents IP, FQDN and Dynamic. */
  public enum PeerAddressType {
    DYNAMIC,
    FQDN,
    IP
  }

  /** How the gateway authenticates its peer. */
  public enum AuthenticationType {
    CERTIFICATE,
    PRE_SHARED_KEY
  }

  private final @Nonnull String _name;

  private @Nullable AuthenticationType _authenticationType;

  private @Nullable String _ikeV1CryptoProfile;

  private @Nullable String _ikeV2CryptoProfile;

  private @Nullable Version _version;

  private @Nullable String _keyHash;

  private @Nullable InterfaceAddress _localAddress;

  private @Nullable String _localInterface;

  private @Nullable InterfaceAddress _localFloatingIp;

  private @Nullable InterfaceAddress _peerAddress;

  private @Nullable PeerAddressType _peerAddressType;

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

  /**
   * The credential as it appears in the config: the encrypted pre-shared key, or the certificate
   * configuration for a certificate-authenticated gateway. PAN-OS never exports a plaintext
   * pre-shared key, so this is opaque and cannot be compared against a peer's.
   */
  public @Nullable String getKeyHash() {
    return _keyHash;
  }

  public void setKeyHash(@Nullable String keyHash) {
    _keyHash = keyHash;
  }

  public @Nullable String getIkeV1CryptoProfile() {
    return _ikeV1CryptoProfile;
  }

  public void setIkeV1CryptoProfile(@Nullable String ikeV1CryptoProfile) {
    _ikeV1CryptoProfile = ikeV1CryptoProfile;
  }

  public @Nullable String getIkeV2CryptoProfile() {
    return _ikeV2CryptoProfile;
  }

  public void setIkeV2CryptoProfile(@Nullable String ikeV2CryptoProfile) {
    _ikeV2CryptoProfile = ikeV2CryptoProfile;
  }

  /** Value of {@code protocol version}; null when the config does not say. */
  public @Nullable Version getVersion() {
    return _version;
  }

  public void setVersion(@Nullable Version version) {
    _version = version;
  }

  /**
   * The crypto profiles that apply, in preference order. PAN-OS allows a different profile per IKE
   * version, so {@code protocol version} selects which is in effect. In {@code ikev2-preferred}
   * mode both are genuinely in play, so both are returned with IKEv2 first. Empty when the selected
   * version has no profile configured.
   */
  public @Nonnull List<String> getEffectiveIkeCryptoProfiles() {
    if (_version == Version.IKEV1) {
      return _ikeV1CryptoProfile == null
          ? ImmutableList.of()
          : ImmutableList.of(_ikeV1CryptoProfile);
    }
    if (_version == Version.IKEV2) {
      return _ikeV2CryptoProfile == null
          ? ImmutableList.of()
          : ImmutableList.of(_ikeV2CryptoProfile);
    }
    // IKEV2_PREFERRED, or no version configured: IKEv2 is tried first, with IKEv1 as fallback.
    // The same profile is commonly configured for both versions, so dedupe: a repeat would
    // otherwise produce a duplicate entry in the policy's proposal list.
    return Stream.of(_ikeV2CryptoProfile, _ikeV1CryptoProfile)
        .filter(Objects::nonNull)
        .distinct()
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * True when the config does not say which IKE version applies and the gateway configures a
   * profile for both, so the choice of preference order is an assumption rather than the config.
   */
  public boolean hasAmbiguousIkeVersion() {
    return _version == null && _ikeV1CryptoProfile != null && _ikeV2CryptoProfile != null;
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

  /**
   * Address from {@code local-address floating-ip}, used by HA active/active pairs. Batfish does
   * not model floating addresses, so this is recorded only to report it accurately.
   */
  public @Nullable InterfaceAddress getLocalFloatingIp() {
    return _localFloatingIp;
  }

  public void setLocalFloatingIp(@Nullable InterfaceAddress localFloatingIp) {
    _localFloatingIp = localFloatingIp;
  }

  /** Interface named by {@code local-address interface}. */
  public @Nullable String getLocalInterface() {
    return _localInterface;
  }

  public void setLocalInterface(@Nullable String localInterface) {
    _localInterface = localInterface;
  }

  public @Nullable PeerAddressType getPeerAddressType() {
    return _peerAddressType;
  }

  public void setPeerAddressType(@Nullable PeerAddressType peerAddressType) {
    _peerAddressType = peerAddressType;
  }

  public @Nullable InterfaceAddress getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(@Nullable InterfaceAddress peerAddress) {
    _peerAddress = peerAddress;
  }
}
