package org.batfish.minesweeper.env;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.minesweeper.aspath.BooleanExprMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all source VRFs (see {@link MatchSourceVrf}) in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprSourceVrfCollector extends BooleanExprMatchCollector<String> {
  @Override
  public Set<String> visitMatchSourceVrf(
      MatchSourceVrf matchSourceVrf, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(matchSourceVrf.getSourceVrf());
  }
}
