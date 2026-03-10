package org.batfish.minesweeper;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchInterface;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all next-hop interface names (see {@link MatchInterface}) in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class NextHopInterfaceCollector extends RoutingPolicyCollector<String> {
  @Override
  public Set<String> visitMatchInterface(
      MatchInterface matchInterface, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.copyOf(matchInterface.getInterfaces());
  }
}
