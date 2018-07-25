package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.Ip;

public class IkeGateway implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _address;

  private Interface _externalInterface;

  private String _ikePolicy;

  private Ip _localAddress;

  private final String _name;

  public IkeGateway(String name) {
    _name = name;
  }

  public Ip getAddress() {
    return _address;
  }

  public Interface getExternalInterface() {
    return _externalInterface;
  }

  public String getIkePolicy() {
    return _ikePolicy;
  }

  public Ip getLocalAddress() {
    return _localAddress;
  }

  public String getName() {
    return _name;
  }

  public void setAddress(Ip address) {
    _address = address;
  }

  public void setExternalInterface(Interface externalInterface) {
    _externalInterface = externalInterface;
  }

  public void setIkePolicy(String ikePolicy) {
    _ikePolicy = ikePolicy;
  }

  public void setLocalAddress(Ip localAddress) {
    _localAddress = localAddress;
  }
}
