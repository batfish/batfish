package org.batfish.minesweeper.env;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.minesweeper.aspath.BooleanExprMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all tracks (see {@link TrackSucceeded}) in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprTrackCollector extends BooleanExprMatchCollector<String> {
  @Override
  public Set<String> visitTrackSucceeded(
      TrackSucceeded trackSucceeded, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(trackSucceeded.getTrackName());
  }
}
