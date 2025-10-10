package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serial;
import javax.annotation.Nullable;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;

/** Implements BGP next-hop-self semantics */
public class SelfNextHop extends NextHopExpr {

  private static final SelfNextHop INSTANCE = new SelfNextHop();

  public static SelfNextHop getInstance() {
    return INSTANCE;
  }

  private SelfNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof NextHopExpr;
  }

  @Override
  public @Nullable NextHop evaluate(Environment env) {
    // TODO: does this make sense in direction IN?
    // It seems weird. Such a route would never be resolvable to an interface, as the IP is
    // owned.
    return env.getLocalIp().map(NextHopIp::of).orElse(null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }

  @JsonCreator
  private static SelfNextHop jsonCreator() {
    return INSTANCE;
  }

  @Serial
  private Object readResolve() {
    return INSTANCE;
  }
}
