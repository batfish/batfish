package org.batfish.question.ipsecvpnstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import org.batfish.datamodel.IpsecVpn;

public class IpsecVpnEndpoint implements Comparable<IpsecVpnEndpoint> {

  private static final String PROP_HOSTNAME = "hostname";

  private static final String PROP_VPN_NAME = "vpnName";

  private String _hostname;

  private String _vpnName;

  @JsonCreator
  public IpsecVpnEndpoint(
      @JsonProperty(PROP_HOSTNAME) String hostname, @JsonProperty(PROP_VPN_NAME) String vpnName) {
    _hostname = hostname;
    _vpnName = vpnName;
  }

  public IpsecVpnEndpoint(IpsecVpn vpn) {
    this(vpn.getOwner().getHostname(), vpn.getName());
  }

  @Override
  public int compareTo(IpsecVpnEndpoint o) {
    return Comparator.comparing(IpsecVpnEndpoint::getHostname)
        .thenComparing(IpsecVpnEndpoint::getVpnName)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IpsecVpnEndpoint)) {
      return false;
    }
    return Objects.equals(_hostname, ((IpsecVpnEndpoint) o)._hostname)
        && Objects.equals(_vpnName, ((IpsecVpnEndpoint) o)._vpnName);
  }

  @JsonProperty(PROP_HOSTNAME)
  public String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_VPN_NAME)
  public String getVpnName() {
    return _vpnName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vpnName);
  }

  @Override
  public String toString() {
    return String.format("hostname: %s vpnname: %s", _hostname, _vpnName);
  }
}
