package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nullable;

/** L2VPN EVPN settings for BGP */
public class BgpL2vpnEvpnAddressFamily implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _advertiseAllVni;
  private boolean _advertiseDefaultGw;
  private @Nullable BgpL2VpnEvpnIpv4Unicast _advertiseIpv4Unicast;

  public boolean getAdvertiseAllVni() {
    return _advertiseAllVni;
  }

  public boolean getAdvertiseDefaultGw() {
    return _advertiseDefaultGw;
  }

  public BgpL2VpnEvpnIpv4Unicast getAdvertiseIpv4Unicast() {
    return _advertiseIpv4Unicast;
  }

  public void setAdvertiseAllVni(boolean advertiseAllVni) {
    _advertiseAllVni = advertiseAllVni;
  }

  public void setAdvertiseDefaultGw(boolean advertiseDefaultGw) {
    _advertiseDefaultGw = advertiseDefaultGw;
  }

  public void setAdvertiseIpv4Unicast(@Nullable BgpL2VpnEvpnIpv4Unicast advertiseIpv4Unicast) {
    _advertiseIpv4Unicast = advertiseIpv4Unicast;
  }
}
