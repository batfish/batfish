package org.batfish.datamodel;

public class RipNeighbor {

  private Interface _iface;
  private final Ip _localIp;
  private final Ip _remoteIp;
  private Configuration _owner;
  private transient RipNeighbor _remoteRipNeighbor;
  private String _vrf;

  public RipNeighbor(Ip localIp, Ip remoteIp) {
    _localIp = localIp;
    _remoteIp = remoteIp;
  }

  public Interface getIface() {
    return _iface;
  }

  public Ip getLocalIp() {
    return _localIp;
  }

  public Configuration getOwner() {
    return _owner;
  }

  public Ip getRemoteIp() {
    return _remoteIp;
  }

  public RipNeighbor getRemoteRipNeighbor() {
    return _remoteRipNeighbor;
  }

  public String getVrf() {
    return _vrf;
  }

  public void setIface(Interface iface) {
    _iface = iface;
  }

  public void setInterface(Interface iface) {
    _iface = iface;
  }

  public void setOwner(Configuration owner) {
    _owner = owner;
  }

  public void setRemoteRipNeighbor(RipNeighbor remoteRipNeighbor) {
    _remoteRipNeighbor = remoteRipNeighbor;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
