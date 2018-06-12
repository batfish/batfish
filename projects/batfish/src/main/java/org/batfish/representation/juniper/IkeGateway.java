package org.batfish.representation.juniper;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class IkeGateway extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _address;

  private Interface _externalInterface;

  private int _externalInterfaceLine;

  private String _ikePolicy;

  private Ip _localAddress;

  public IkeGateway(String name) {
    super(name);
  }

  public Ip getAddress() {
    return _address;
  }

  public Interface getExternalInterface() {
    return _externalInterface;
  }

  public int getExternalInterfaceLine() {
    return _externalInterfaceLine;
  }

  public String getIkePolicy() {
    return _ikePolicy;
  }

  public Ip getLocalAddress() {
    return _localAddress;
  }

  public void setAddress(Ip address) {
    _address = address;
  }

  public void setExternalInterface(Interface externalInterface) {
    _externalInterface = externalInterface;
  }

  public void setExternalInterfaceLine(int externalInterfaceLine) {
    _externalInterfaceLine = externalInterfaceLine;
  }

  public void setIkePolicy(String ikePolicy) {
    _ikePolicy = ikePolicy;
  }

  public void setLocalAddress(Ip localAddress) {
    _localAddress = localAddress;
  }
}
