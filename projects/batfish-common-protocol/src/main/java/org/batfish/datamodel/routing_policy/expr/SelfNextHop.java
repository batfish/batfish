package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/** Implements BGP next-hop-self semantics */
public class SelfNextHop extends NextHopExpr {

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
    // BgpSessionProperties are for session directed toward the node with the policy being executed
    BgpSessionProperties sessionProperties = environment.getBgpSessionProperties();
    return sessionProperties == null ? null : sessionProperties.getHeadIp();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
