package org.batfish.representation.host;

import java.io.Serializable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.StaticRoute;

public class HostStaticRoute implements Serializable {

  public static final int DEFAULT_ADMINISTRATIVE_COST = 1;

  private int _administrativeCost;

  private String _nextHopInterface;

  private Ip _nextHopIp;

  private Prefix _prefix;

  private Integer _tag;

  public HostStaticRoute() {
    _administrativeCost = DEFAULT_ADMINISTRATIVE_COST;
  }

  public int getAdministrativeCost() {
    return _administrativeCost;
  }

  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public Integer getTag() {
    return _tag;
  }

  public void setAdministrativeCost(int administrativeCost) {
    _administrativeCost = administrativeCost;
  }

  public void setNextHopInterface(String nextHopInterface) {
    _nextHopInterface = nextHopInterface;
  }

  public void setNextHopIp(Ip nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  public void setPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  public void setTag(Integer tag) {
    _tag = tag;
  }

  public StaticRoute toStaticRoute() {
    long tag = _tag == null ? Route.UNSET_ROUTE_TAG : _tag;
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(_prefix)
            .setNextHopIp(_nextHopIp)
            .setNextHopInterface(_nextHopInterface)
            .setAdministrativeCost(_administrativeCost)
            .setTag(tag)
            .build();
    return sr;
  }
}
