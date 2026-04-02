package org.batfish.datamodel.routing_policy.expr;

import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nullable;
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
  public @Nullable Ip evaluate(Environment env) {
    return env.getOriginalRoute().getNextHopIp();
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  /** Deserialize to singleton instance. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}
