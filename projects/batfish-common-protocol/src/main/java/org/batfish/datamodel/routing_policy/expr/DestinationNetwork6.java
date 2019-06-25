package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;

public class DestinationNetwork6 extends Prefix6Expr {

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

  @Nullable
  @Override
  public Prefix6 evaluate(Environment env) {
    if (env.getOriginalRoute6() != null) {
      return env.getOriginalRoute6().getNetwork();
    } else if (env.getOriginalRoute() == null) {
      throw new BatfishException("No IPV4 nor IPV6 route passed as input");
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
