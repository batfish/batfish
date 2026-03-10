package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serial;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;

/** Implements BGP next-hop unchanged semantics */
public class UnchangedNextHop extends NextHopExpr {

  private static final UnchangedNextHop INSTANCE = new UnchangedNextHop();

  public static UnchangedNextHop getInstance() {
    return INSTANCE;
  }

  private UnchangedNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof UnchangedNextHop;
  }

  @Override
  public @Nullable NextHop evaluate(Environment env) {
    // applies only to BGP-to-BGP transformations
    if (!(env.getOriginalRoute() instanceof BgpRoute<?, ?>)) {
      return null;
    }
    // No operation for IBGP
    BgpSessionProperties sessionProperties = env.getBgpSessionProperties();
    if (sessionProperties == null || !sessionProperties.isEbgp()) {
      return null;
    }
    // Preserve original NHIP if present
    Ip originalRouteNextHop = env.getOriginalRoute().getNextHopIp();
    if (!originalRouteNextHop.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
      return NextHopIp.of(originalRouteNextHop);
    }

    return null; // no-op, will be changed elsewhere in the pipeline
  }

  @Override
  public int hashCode() {
    return 0x4b4bebc0; // randomly generated
  }

  @JsonCreator
  private static UnchangedNextHop jsonCreator() {
    return INSTANCE;
  }

  @Serial
  private Object readResolve() {
    return INSTANCE;
  }
}
