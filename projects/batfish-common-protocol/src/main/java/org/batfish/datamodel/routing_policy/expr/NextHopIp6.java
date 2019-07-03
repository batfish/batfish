package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.Environment;

public class NextHopIp6 extends Ip6Expr {

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public Ip6 evaluate(Environment env) {
    return env.getOriginalRoute6().getNextHopIp();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
