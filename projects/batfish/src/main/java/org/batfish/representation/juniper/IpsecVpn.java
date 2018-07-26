package org.batfish.representation.juniper;

import java.io.Serializable;

public final class IpsecVpn implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Interface _bindInterface;

  private String _gateway;

  private String _ipsecPolicy;

  private final String _name;

  public IpsecVpn(String name) {
    _name = name;
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

  public String getName() {
    return _name;
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
