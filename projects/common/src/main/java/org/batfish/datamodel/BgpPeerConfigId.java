package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Uniquely identifies a {@link BgpPeerConfig} in a network. */
public final class BgpPeerConfigId implements Comparable<BgpPeerConfigId> {

  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_VRF_NAME = "vrf";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_TYPE = "type";
  private static final String PROP_DYNAMIC = "dynamic";

  private final @Nonnull String _hostname;
  private final @Nonnull String _vrfName;
  private final @Nullable String _peerInterface;
  private final @Nullable Prefix _remotePeerPrefix;
  private final @Nonnull BgpPeerConfigType _type;

  /** Create a new ID. */
  public BgpPeerConfigId(
      @Nonnull String hostname,
      @Nonnull String vrfName,
      @Nonnull Prefix remotePeerPrefix,
      boolean dynamic) {
    _hostname = hostname;
    _vrfName = vrfName;
    _peerInterface = null;
    _remotePeerPrefix = remotePeerPrefix;
    _type = dynamic ? BgpPeerConfigType.DYNAMIC : BgpPeerConfigType.ACTIVE;
  }

  /** Create a new ID for a BGP unnumbered peer. */
  public BgpPeerConfigId(
      @Nonnull String hostname, @Nonnull String vrfName, @Nonnull String peerInterface) {
    _hostname = hostname;
    _vrfName = vrfName;
    _peerInterface = peerInterface;
    _remotePeerPrefix = null;
    _type = BgpPeerConfigType.UNNUMBERED;
  }

  @JsonCreator
  private static BgpPeerConfigId create(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_VRF_NAME) @Nullable String vrfName,
      @JsonProperty(PROP_INTERFACE) @Nullable String peerInterface,
      @JsonProperty(PROP_PREFIX) @Nullable Prefix remotePeerPrefix,
      @JsonProperty(PROP_TYPE) @Nullable BgpPeerConfigType type,
      @JsonProperty(PROP_DYNAMIC) boolean dynamic) {
    checkArgument(hostname != null, "%s must be provided", PROP_HOSTNAME);
    checkArgument(vrfName != null, "%s must be provided", PROP_VRF_NAME);
    if (type != BgpPeerConfigType.UNNUMBERED) {
      // Includes case where type is null (for backwards compatibility)
      checkArgument(
          remotePeerPrefix != null, "%s must be provided for non-unnumbered peers", PROP_PREFIX);
      checkArgument(
          peerInterface == null, "%s must be null for non-unnumbered peers", PROP_INTERFACE);
      return new BgpPeerConfigId(
          hostname, vrfName, remotePeerPrefix, dynamic || type == BgpPeerConfigType.DYNAMIC);
    }
    // BGP unnumbered peer
    checkArgument(remotePeerPrefix == null, "%s must be null for unnumbered peers", PROP_PREFIX);
    checkArgument(
        peerInterface != null, "%s must not be null for unnumbered peers", PROP_INTERFACE);
    return new BgpPeerConfigId(hostname, vrfName, peerInterface);
  }

  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_VRF_NAME)
  public @Nonnull String getVrfName() {
    return _vrfName;
  }

  /**
   * The interface of this peer ID, or {@code null} iff it does not represent a {@link
   * BgpUnnumberedPeerConfig}. Exactly one of {@link #getPeerInterface()} and {@link
   * #getRemotePeerPrefix()} is nonnull.
   */
  @JsonProperty(PROP_INTERFACE)
  public @Nullable String getPeerInterface() {
    return _peerInterface;
  }

  /**
   * The remote prefix of this peer ID, or {@code null} iff it represents a {@link
   * BgpUnnumberedPeerConfig}. Exactly one of {@link #getPeerInterface()} and {@link
   * #getRemotePeerPrefix()} is nonnull.
   */
  @JsonProperty(PROP_PREFIX)
  public @Nullable Prefix getRemotePeerPrefix() {
    return _remotePeerPrefix;
  }

  /** The {@link BgpPeerConfigType} of this peer ID */
  @JsonProperty(PROP_TYPE)
  public @Nonnull BgpPeerConfigType getType() {
    return _type;
  }

  @Override
  public int compareTo(@Nonnull BgpPeerConfigId o) {
    return Comparator.comparing(BgpPeerConfigId::getHostname)
        .thenComparing(BgpPeerConfigId::getVrfName)
        .thenComparing(BgpPeerConfigId::getType)
        .thenComparing(
            BgpPeerConfigId::getRemotePeerPrefix, Comparator.nullsLast(Prefix::compareTo))
        .thenComparing(
            BgpPeerConfigId::getPeerInterface, Comparator.nullsLast(Comparator.naturalOrder()))
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpPeerConfigId)) {
      return false;
    }
    BgpPeerConfigId other = (BgpPeerConfigId) o;
    return (_hashCode == other._hashCode || _hashCode == 0 || other._hashCode == 0)
        && _type == other._type
        && _hostname.equals(other._hostname)
        && _vrfName.equals(other._vrfName)
        && Objects.equals(_peerInterface, other._peerInterface)
        && Objects.equals(_remotePeerPrefix, other._remotePeerPrefix);
  }

  @Override
  public int hashCode() {
    int hashCode = _hashCode;
    if (hashCode == 0) {
      hashCode = _hostname.hashCode();
      hashCode = 31 * hashCode + _vrfName.hashCode();
      hashCode = 31 * hashCode + Objects.hashCode(_peerInterface);
      hashCode = 31 * hashCode + Objects.hashCode(_remotePeerPrefix);
      hashCode = 31 * hashCode + _type.ordinal();
      _hashCode = hashCode;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_HOSTNAME, _hostname)
        .add(PROP_INTERFACE, _peerInterface)
        .add(PROP_PREFIX, _remotePeerPrefix)
        .toString();
  }

  /** Types of BGP peers */
  public enum BgpPeerConfigType {
    /** Type for {@link BgpActivePeerConfig}s */
    ACTIVE,
    /** Type for {@link BgpPassivePeerConfig}s */
    DYNAMIC,
    /** Type for {@link BgpUnnumberedPeerConfig}s */
    UNNUMBERED
  }

  private transient int _hashCode;
}
