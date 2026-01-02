package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * A next hop expression indicating that the route should drop traffic destined to its network, but
 * otherwise leave route parameters unchanged.
 */
public class DiscardNextHop extends NextHopExpr {

  public static final DiscardNextHop INSTANCE = new DiscardNextHop();

  private DiscardNextHop() {}

  @Override
  public @Nonnull NextHop evaluate(Environment env) {
    return NextHopDiscard.instance();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof DiscardNextHop;
  }

  @Override
  public int hashCode() {
    return DiscardNextHop.class.getCanonicalName().hashCode();
  }

  @JsonCreator
  private static DiscardNextHop jsonCreator() {
    return INSTANCE;
  }

  @Serial
  private Object readResolve() {
    return INSTANCE;
  }
}
