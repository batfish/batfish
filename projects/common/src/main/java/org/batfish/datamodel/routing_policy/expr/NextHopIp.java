package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/** Expression that extracts a route's next hop IP given a routing policy environment. */
public final class NextHopIp extends IpExpr {

  private static final NextHopIp INSTANCE = new NextHopIp();

  private NextHopIp() {}

  public static NextHopIp instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof NextHopIp);
  }

  @Override
  public Ip evaluate(Environment env) {
    return env.getOriginalRoute().getNextHopIp();
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
