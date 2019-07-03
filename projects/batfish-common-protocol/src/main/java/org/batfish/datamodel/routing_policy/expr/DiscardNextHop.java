package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * A next hop expression indicating that the route should drop traffic destined to its network, but
 * otherwise leave route parameters unchanged.
 */
public class DiscardNextHop extends NextHopExpr {

  public static final DiscardNextHop INSTANCE = new DiscardNextHop();

  private DiscardNextHop() {}

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof DiscardNextHop;
  }

  @Override
  public boolean getDiscard() {
    return true;
  }

  @Override
  public Ip getNextHopIp(Environment environment) {
    return null;
  }

  @Override
  public int hashCode() {
    return DiscardNextHop.class.getCanonicalName().hashCode();
  }
}
