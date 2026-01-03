package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpRoute;
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
    return obj instanceof AutoAs;
  }

  @Override
  public long evaluate(Environment environment) {
    Direction direction = environment.getDirection();
    if (direction == Direction.IN) {
      AsPath asPath;
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
      // really should not receive empty as-path in route from EBGP neighbor
      List<AsSet> asSets = asPath.getAsSets();
      if (asSets.isEmpty()) {
        // Default to remote AS. TODO Is this the correct behavior?
        Optional<Long> remoteAs = environment.getRemoteAs();
        checkState(remoteAs.isPresent(), "Expected BGP session properties");
        return remoteAs.get();
      }
      SortedSet<Long> firstAsSetAsns = asSets.get(0).getAsns();
      // TODO: see if clients of AsExpr should really be provided the entire AsSet instead of a
      // single AS
      assert firstAsSetAsns.size() == 1;
      // for now, arbitrarily use lowest AS in set
      return firstAsSetAsns.first();
    }
    assert direction == Direction.OUT;
    Optional<Long> localAs = environment.getLocalAs();
    checkState(localAs.isPresent(), "Expected BGP session properties");
    return localAs.get();
  }

  @Override
  public int hashCode() {
    return 0xb21f9d07; // randomly generated
  }
}
