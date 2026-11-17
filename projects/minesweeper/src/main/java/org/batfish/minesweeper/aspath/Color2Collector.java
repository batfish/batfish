package org.batfish.minesweeper.aspath;

import static org.batfish.minesweeper.aspath.Tag2Collector.literalValue;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.MatchColor2;
import org.batfish.datamodel.routing_policy.statement.SetColor2;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collects the literal {@code color2} values (see {@link
 * org.batfish.datamodel.BgpRoute#getColor2()}) that a {@link
 * org.batfish.datamodel.routing_policy.RoutingPolicy} matches or sets.
 */
@ParametersAreNonnullByDefault
public class Color2Collector extends RoutingPolicyCollector<Long> {

  public static Color2Collector instance() {
    return INSTANCE;
  }

  @Override
  public Set<Long> visitMatchColor2(
      MatchColor2 matchColor2, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(matchColor2.getColor2());
  }

  @Override
  public Set<Long> visitSetColor2(SetColor2 setColor2, Tuple<Set<String>, Configuration> arg) {
    return literalValue(setColor2.getColor2());
  }

  private static final Color2Collector INSTANCE = new Color2Collector();
}
