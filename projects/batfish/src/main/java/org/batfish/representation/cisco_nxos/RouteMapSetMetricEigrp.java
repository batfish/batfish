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

  /** Bandwidth in Kb/s */
  public long getBandwidth() {
    return _bandwidth;
  }

  /** Delay in tens of microseconds */
  public long getDelayTensOfMicroseconds() {
    return _delay;
  }

  /** Reliability from 0 to 255 (100 percent reliable) */
  public int getReliability() {
    return _reliability;
  }

  /** Load from 1 to 255 (100 percent loaded) */
  public int getLoad() {
    return _load;
  }

  /** MTU of the path from 1 to 16777215 */
  public long getMtu() {
    return _mtu;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetMetricEigrp(this);
  }
}
