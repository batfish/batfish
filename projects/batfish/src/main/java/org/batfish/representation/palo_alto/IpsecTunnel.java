package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Palo Alto IPsec tunnel, configured under {@code network tunnel ipsec}. */
public final class IpsecTunnel implements Serializable {

  private final @Nonnull String _name;

  private boolean _disabled;

  private @Nullable String _ikeGateway;

  private @Nullable String _ipsecCryptoProfile;

  private @Nullable String _tunnelInterface;

  public IpsecTunnel(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public boolean getDisabled() {
    return _disabled;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }

  /** Name of the {@code auto-key ike-gateway} this tunnel binds to. */
  public @Nullable String getIkeGateway() {
    return _ikeGateway;
  }

  public void setIkeGateway(@Nullable String ikeGateway) {
    _ikeGateway = ikeGateway;
  }

  public @Nullable String getIpsecCryptoProfile() {
    return _ipsecCryptoProfile;
  }

  public void setIpsecCryptoProfile(@Nullable String ipsecCryptoProfile) {
    _ipsecCryptoProfile = ipsecCryptoProfile;
  }

  /** Logical tunnel interface (e.g. {@code tunnel.1}) this tunnel is bound to. */
  public @Nullable String getTunnelInterface() {
    return _tunnelInterface;
  }

  public void setTunnelInterface(@Nullable String tunnelInterface) {
    _tunnelInterface = tunnelInterface;
  }
}
