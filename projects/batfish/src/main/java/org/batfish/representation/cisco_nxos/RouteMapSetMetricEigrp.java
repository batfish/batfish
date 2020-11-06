package org.batfish.representation.cisco_nxos;

/** A {@link RouteMapSet} that sets the EIGRP metric for a route. */
public final class RouteMapSetMetricEigrp implements RouteMapSet {

  private final long _bandwidth;
  private final long _delay;
  private final int _reliability;
  private final int _load;
  private final long _mtu;

  public RouteMapSetMetricEigrp(long bandwidth, long delay, int reliability, int load, long mtu) {
    _bandwidth = bandwidth;
    _delay = delay;
    _reliability = reliability;
    _load = load;
    _mtu = mtu;
  }

  public long getBandwidth() {
    return _bandwidth;
  }

  public long getDelay() {
    return _delay;
  }

  public int getReliability() {
    return _reliability;
  }

  public int getLoad() {
    return _load;
  }

  public long getMtu() {
    return _mtu;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetMetricEigrp(this);
  }
}
