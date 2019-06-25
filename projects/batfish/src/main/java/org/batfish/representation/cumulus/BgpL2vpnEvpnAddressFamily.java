package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nullable;

/** L2VPN EVPN settings for BGP */
public class BgpL2vpnEvpnAddressFamily implements Serializable {

  private boolean _advertiseAllVni;
  private boolean _advertiseDefaultGw;
  private @Nullable BgpL2VpnEvpnIpv4Unicast _advertiseIpv4Unicast;

  /** Whether to generate type 3 VTEP reachability advertisements for all defined VNIs */
  public boolean getAdvertiseAllVni() {
    return _advertiseAllVni;
  }

  /**
   * Whether to generate a type 2 route for the IP address of the SVI associated with a defined VNI
   */
  public boolean getAdvertiseDefaultGw() {
    return _advertiseDefaultGw;
  }

  /** If not {@code null}, redistribute IPv4 routes into EVPN address family as type 5 routes */
  @Nullable
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
