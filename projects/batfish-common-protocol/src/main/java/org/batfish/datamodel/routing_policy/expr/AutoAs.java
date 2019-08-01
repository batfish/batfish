package org.batfish.datamodel.routing_policy.expr;

import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;

/**
 * In inbound direction, represents the last AS of the route (usually the neighbor's AS). In
 * outbound direction, represents our local AS for the session.
 */
public final class AutoAs extends AsExpr {

  private static final AutoAs INSTANCE = new AutoAs();

  public static @Nonnull AutoAs instance() {
    return INSTANCE;
  }

  private AutoAs() {}

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
  public long evaluate(Environment environment) {
    BgpProcess proc = environment.getBgpProcess();
    Direction direction = environment.getDirection();
    long as;
    if (direction == Direction.IN) {
      AsPath asPath = null;
      if (environment.getUseOutputAttributes()
          && environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
        BgpRoute.Builder<?, ?> bgpRouteBuilder =
            (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
        asPath = bgpRouteBuilder.getAsPath();
      } else if (environment.getReadFromIntermediateBgpAttributes()) {
        asPath = environment.getIntermediateBgpAttributes().getAsPath();
      } else {
        // caller should guarantee this holds if this branch is reached
        assert environment.getOriginalRoute() instanceof BgpRoute;
        BgpRoute<?, ?> bgpRoute = (BgpRoute<?, ?>) environment.getOriginalRoute();
        asPath = bgpRoute.getAsPath();
      }
      // really should not receive empty as-path in route from neighbor
      List<AsSet> asSets = asPath.getAsSets();
      assert !asSets.isEmpty();
      SortedSet<Long> asesInSet = asSets.iterator().next().getAsns();
      // TODO: see if clients of AsExpr should really be provided the entire AsSet instead of a
      // single AS
      assert asesInSet.size() == 1;
      // for now, arbitrarily use lowest AS in set
      as = asSets.iterator().next().getAsns().first();
    } else {
      assert direction == Direction.OUT;
      if (proc == null) {
        throw new BatfishException("Expected BGP process");
      }
      Ip peerAddress = environment.getPeerAddress();
      if (peerAddress == null) {
        throw new BatfishException("Expected a peer address");
      }
      Prefix peerPrefix = Prefix.create(peerAddress, Prefix.MAX_PREFIX_LENGTH);
      // TODO: support passive, interface neighbors via session instead
      BgpActivePeerConfig neighbor = proc.getActiveNeighbors().get(peerPrefix);
      if (neighbor == null) {
        throw new BatfishException("Expected a peer with address: " + peerAddress);
      }
      as = neighbor.getLocalAs();
    }
    return as;
  }

  @Override
  public int hashCode() {
    return 0xb21f9d07; // randomly generated
  }
}
