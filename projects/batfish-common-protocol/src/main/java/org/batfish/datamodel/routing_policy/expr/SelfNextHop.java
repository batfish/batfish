package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nullable;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

/** Implements BGP next-hop-self semantics */
public class SelfNextHop extends NextHopExpr {

  private static final long serialVersionUID = 1L;

  private static final SelfNextHop _instance = new SelfNextHop();

  public static SelfNextHop getInstance() {
    return _instance;
  }

  private SelfNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof NextHopExpr;
  }

  @Override
  @Nullable
  public Ip getNextHopIp(Environment environment) {
    Prefix peerPrefix = environment.getPeerPrefix();
    if (peerPrefix == null) {
      return null;
    }
    BgpPeerConfig neighbor = environment.getBgpProcess().getActiveNeighbors().get(peerPrefix);
    if (neighbor == null) {
      neighbor = environment.getBgpProcess().getPassiveNeighbors().get(peerPrefix);
    }
    if (neighbor == null) {
      return null;
    }
    return neighbor.getLocalIp();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
