package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.common.util.ReferenceCountedStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;

public class Tunnel extends ReferenceCountedStructure {

  public enum TunnelMode {
    GRE,
    IPSEC
  }

  private static final long serialVersionUID = 1L;

  Ip _destination;

  String _ipsecProfileName;

  TunnelMode _mode;

  IpProtocol _protocol;

  @Nullable Ip _sourceAddress;

  @Nullable String _sourceInterfaceName;

  public Ip getDestination() {
    return _destination;
  }

  public String getIpsecProfileName() {
    return _ipsecProfileName;
  }

  public TunnelMode getMode() {
    return _mode;
  }

  @Nullable
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @Nullable
  public String getSourceInterfaceName() {
    return _sourceInterfaceName;
  }

  public void setDestination(Ip destination) {
    _destination = destination;
  }

  public void setIpsecProfileName(String name) {
    _ipsecProfileName = name;
  }

  public void setMode(TunnelMode mode) {
    _mode = mode;
  }

  public void setProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  public void setSourceAddress(Ip source) {
    _sourceAddress = source;
  }

  public void setSourceInterfaceName(String sourceInterfaceName) {
    _sourceInterfaceName = sourceInterfaceName;
  }
}
