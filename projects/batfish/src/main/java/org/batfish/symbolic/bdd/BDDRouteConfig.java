package org.batfish.symbolic.bdd;

public class BDDRouteConfig {

  private boolean _keepLp;

  private boolean _keepAd;

  private boolean _keepMetric;

  private boolean _keepMed;

  private boolean _keepOspfMetric;

  private boolean _keepHistory;

  private boolean _keepCommunities;

  private boolean _keepRouters;

  public BDDRouteConfig(boolean abstraction) {
    if (abstraction) {
      _keepAd = false;
      _keepOspfMetric = false;
      _keepMed = false;
      _keepLp = false;
      _keepHistory = true;
      _keepCommunities = true;
      _keepMetric = false;
      _keepRouters = true;
    } else {
      _keepAd = true;
      _keepCommunities = true;
      _keepHistory = true;
      _keepLp = true;
      _keepMed = true;
      _keepMetric = true;
      _keepOspfMetric = true;
      _keepRouters = false;
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

  public boolean getKeepHistory() {
    return _keepHistory;
  }

  public boolean getKeepCommunities() {
    return _keepCommunities;
  }

  public boolean getKeepRouters() {
    return _keepRouters;
  }
}
