package org.batfish.datamodel.routing_policy.expr;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
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
    // applies only to BGP to BGP transformations
    if (!(environment.getOriginalRoute() instanceof BgpRoute<?, ?>)) {
      return null;
    }
    Prefix peerPrefix = environment.getPeerPrefix();
    if (peerPrefix == null) {
      return null;
    }
    // TODO: extend to dynamic and unnumbered neighbors
    BgpActivePeerConfig activeNeighbor =
        environment.getBgpProcess().getActiveNeighbors().get(peerPrefix);
    if (activeNeighbor == null || Objects.isNull(activeNeighbor.getLocalAs())) {
      return null;
    }
    if (!activeNeighbor.getRemoteAsns().equals(LongSpace.of(activeNeighbor.getLocalAs()))) {
      // if eBGP preserve original NHIP if present
      return firstNonNull(
          environment.getOriginalRoute().getNextHopIp(), activeNeighbor.getLocalIp());
    }
    // no operation for iBGP
    return null;
  }

  @Override
  public int hashCode() {
    return 0x4b4bebc0; // randomly generated
  }
}
