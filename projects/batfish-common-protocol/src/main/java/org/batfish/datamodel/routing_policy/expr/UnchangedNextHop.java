package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.Nullable;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/** Implements BGP next-hop unchanged semantics */
public class UnchangedNextHop extends NextHopExpr {

  private static final UnchangedNextHop _instance = new UnchangedNextHop();

  public static UnchangedNextHop getInstance() {
    return _instance;
  }

  private UnchangedNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof UnchangedNextHop;
  }

  @Override
  @Nullable
  public Ip getNextHopIp(Environment environment) {
    // applies only to BGP-to-BGP transformations
    if (!(environment.getOriginalRoute() instanceof BgpRoute<?, ?>)) {
      return null;
    }
    // No operation for IBGP
    BgpSessionProperties sessionProperties = environment.getBgpSessionProperties();
    if (sessionProperties == null || !sessionProperties.isEbgp()) {
      return null;
    }
    // Preserve original NHIP if present
    Ip originalRouteNextHop = environment.getOriginalRoute().getNextHopIp();
    return firstNonNull(originalRouteNextHop, sessionProperties.getHeadIp());
  }

  @Override
  public int hashCode() {
    return 0x4b4bebc0; // randomly generated
  }
}
