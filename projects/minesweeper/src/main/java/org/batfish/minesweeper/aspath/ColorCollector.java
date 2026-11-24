package org.batfish.minesweeper.aspath;

import static org.batfish.minesweeper.aspath.Tag2Collector.literalValue;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.statement.SetColor;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collects the literal {@code color} values (see {@link org.batfish.datamodel.BgpRoute#getColor()})
 * that a {@link org.batfish.datamodel.routing_policy.RoutingPolicy} matches or sets.
 */
@ParametersAreNonnullByDefault
public class ColorCollector extends RoutingPolicyCollector<Long> {

  public static ColorCollector instance() {
    return INSTANCE;
  }

  @Override
  public Set<Long> visitMatchColor(MatchColor matchColor, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(matchColor.getColor());
  }

  @Override
  public Set<Long> visitSetColor(SetColor setColor, Tuple<Set<String>, Configuration> arg) {
    return literalValue(setColor.getColor());
  }

  private static final ColorCollector INSTANCE = new ColorCollector();
}
