package org.batfish.symbolic.bdd;

public class BDDNetConfig {

  private boolean _keepLp;

  private boolean _keepAd;

  private boolean _keepMetric;

  private boolean _keepMed;

  private boolean _keepOspfMetric;

  private boolean _keepProtocol;

  private boolean _keepCommunities;

  private boolean _keepRouters;

  private boolean _keepRRClient;

  private boolean _keepNextHopIp;

  public BDDNetConfig(boolean abstraction) {
    if (abstraction) {
      _keepAd = false;
      _keepOspfMetric = false;
      _keepMed = false;
      _keepLp = false;
      _keepProtocol = true;
      _keepCommunities = true;
      _keepMetric = false;
      _keepRouters = true;
      _keepRRClient = true;
      _keepNextHopIp = false;
    } else {
      _keepAd = true;
      _keepCommunities = true;
      _keepProtocol = true;
      _keepLp = true;
      _keepMed = true;
      _keepMetric = true;
      _keepOspfMetric = true;
      _keepRouters = false;
      _keepRRClient = true;
      _keepNextHopIp = true;
    }
  }

  public boolean getKeepLp() {
    return _keepLp;
  }

  public boolean getKeepAd() {
    return _keepAd;
  }

  public boolean getKeepMetric() {
    return _keepMetric;
  }

  public boolean getKeepMed() {
    return _keepMed;
  }

  public boolean getKeepOspfMetric() {
    return _keepOspfMetric;
  }

  public boolean getKeepProtocol() {
    return _keepProtocol;
  }

  public boolean getKeepCommunities() {
    return _keepCommunities;
  }

  public boolean getKeepRouters() {
    return _keepRouters;
  }

  public boolean getKeepRRClient() {
    return _keepRRClient;
  }

  public boolean getKeepNextHopIp() {
    return _keepNextHopIp;
  }
}
