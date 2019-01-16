package org.batfish.datamodel;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Uniquely identifies an IPSec peer in the network */
public class IpsecPeerConfigId {

  @Nonnull private String _ipsecPeerConfigName;

  @Nonnull private String _hostName;

  public IpsecPeerConfigId(@Nonnull String ipsecPeerConfigName, @Nonnull String hostName) {
    _ipsecPeerConfigName = ipsecPeerConfigName;
    _hostName = hostName;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpsecPeerConfigId)) {
      return false;
    }
    IpsecPeerConfigId other = (IpsecPeerConfigId) o;
    return _ipsecPeerConfigName.equals(other._ipsecPeerConfigName)
        && _hostName.equals(other._hostName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ipsecPeerConfigName, _hostName);
  }

  @Nonnull
  public String getIpsecPeerConfigName() {
    return _ipsecPeerConfigName;
  }

  @Nonnull
  public String getHostName() {
    return _hostName;
  }
}
