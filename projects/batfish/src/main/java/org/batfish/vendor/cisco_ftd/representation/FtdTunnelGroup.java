package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents a Cisco FTD tunnel group. */
public class FtdTunnelGroup implements Serializable {

  public enum Type {
    IPSEC_L2L,
    REMOTE_ACCESS
  }

  private final String _name;
  private @Nullable Type _type;
  private @Nullable String _ikev2Policy;
  private @Nullable String _presharedKey;
  private @Nullable String _presharedKeyStandby;

  public FtdTunnelGroup(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public Type getType() {
    return _type;
  }

  public void setType(Type type) {
    _type = type;
  }

  public String getIkev2Policy() {
    return _ikev2Policy;
  }

  public void setIkev2Policy(String ikev2Policy) {
    _ikev2Policy = ikev2Policy;
  }

  public String getPresharedKey() {
    return _presharedKey;
  }

  public void setPresharedKey(String presharedKey) {
    _presharedKey = presharedKey;
  }

  public String getPresharedKeyStandby() {
    return _presharedKeyStandby;
  }

  public void setPresharedKeyStandby(String presharedKeyStandby) {
    _presharedKeyStandby = presharedKeyStandby;
  }

  @Override
  public String toString() {
    return String.format(
        "FtdTunnelGroup[name=%s, type=%s, ikev2Policy=%s]", _name, _type, _ikev2Policy);
  }
}
