package org.batfish.datamodel;

import javax.annotation.Nonnull;

/** Uniquely identifies an IPSec peer in the network */
public class IpsecPeerConfigId {

  @Nonnull private String _ipsecPeerConfigName;

  @Nonnull private String _hostName;

  public IpsecPeerConfigId(@Nonnull String ipsecPeerConfigName, @Nonnull String hostName) {
    _ipsecPeerConfigName = ipsecPeerConfigName;
    _hostName = hostName;
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
