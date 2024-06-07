package org.batfish.minesweeper.env;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collect all source VRFs (see {@link MatchSourceVrf}) in a {@link
 * org.batfish.datamodel.routing_policy.RoutingPolicy}.
 */
@ParametersAreNonnullByDefault
public class SourceVrfCollector extends RoutingPolicyCollector<String> {
  @Override
  public Set<String> visitMatchSourceVrf(
      MatchSourceVrf matchSourceVrf, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(matchSourceVrf.getSourceVrf());
  }
}
