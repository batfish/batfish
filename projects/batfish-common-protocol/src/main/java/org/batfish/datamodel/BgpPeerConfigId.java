package org.batfish.datamodel;

import java.util.Objects;
import javax.annotation.Nonnull;

/** Uniquely identifies a {@link BgpPeerConfig} in a network. */
public final class BgpPeerConfigId {
  private final String _hostname;
  private final String _vrfName;
  private final Prefix _remotePeerPrefix;

  /** Create a new ID. */
  public BgpPeerConfigId(
      @Nonnull String hostname, @Nonnull String vrfName, @Nonnull Prefix remotePeerPrefix) {
    _hostname = hostname;
    _vrfName = vrfName;
    _remotePeerPrefix = remotePeerPrefix;
  }

  public String getHostname() {
    return _hostname;
  }

  public String getVrfName() {
    return _vrfName;
  }

  public Prefix getRemotePeerPrefix() {
    return _remotePeerPrefix;
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
    return Objects.equals(_hostname, other._hostname)
        && Objects.equals(_vrfName, other._vrfName)
        && Objects.equals(_remotePeerPrefix, other._remotePeerPrefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vrfName, _remotePeerPrefix);
  }
}
