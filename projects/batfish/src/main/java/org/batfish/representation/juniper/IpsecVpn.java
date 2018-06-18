package org.batfish.representation.juniper;

import org.batfish.common.util.ComparableStructure;

public final class IpsecVpn extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private Interface _bindInterface;

  private String _gateway;

  private String _ipsecPolicy;

  public IpsecVpn(String name) {
    super(name);
  }

  public Interface getBindInterface() {
    return _bindInterface;
  }

  public String getGateway() {
    return _gateway;
  }

  public String getIpsecPolicy() {
    return _ipsecPolicy;
  }

  public void setBindInterface(Interface iface) {
    _bindInterface = iface;
  }

  public void setGateway(String gateway) {
    _gateway = gateway;
  }

  public void setIpsecPolicy(String name) {
    _ipsecPolicy = name;
  }
}
