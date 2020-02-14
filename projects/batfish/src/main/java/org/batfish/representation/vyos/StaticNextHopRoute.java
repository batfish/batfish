package org.batfish.representation.vyos;

import java.io.Serializable;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.Prefix;

public class StaticNextHopRoute implements Serializable {

  private final int _distance;

  private final Ip _nextHopIp;

  private final Prefix _prefix;

  public StaticNextHopRoute(Prefix prefix, Ip nextHopIp, int distance) {
    _prefix = prefix;
    _nextHopIp = nextHopIp;
    _distance = distance;
  }

  public int getDistance() {
    return _distance;
  }

  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
