package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.expr.MatchTag2;
import org.batfish.datamodel.routing_policy.statement.SetTag2;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collects the literal {@code tag2} values (see {@link org.batfish.datamodel.BgpRoute#getTag2()})
 * that a {@link org.batfish.datamodel.routing_policy.RoutingPolicy} matches for equality or sets.
 */
@ParametersAreNonnullByDefault
public class Tag2Collector extends RoutingPolicyCollector<Long> {

  public static Tag2Collector instance() {
    return INSTANCE;
  }

  @Override
  public Set<Long> visitMatchTag2(MatchTag2 matchTag2, Tuple<Set<String>, Configuration> arg) {
    return matchTag2.getCmp() == IntComparator.EQ
        ? literalValue(matchTag2.getTag2())
        : ImmutableSet.of();
  }

  @Override
  public Set<Long> visitSetTag2(SetTag2 setTag2, Tuple<Set<String>, Configuration> arg) {
    return literalValue(setTag2.getTag2());
  }

  /** The value of {@code expr} if it is a {@link LiteralLong}, or nothing. */
  static Set<Long> literalValue(LongExpr expr) {
    return expr instanceof LiteralLong l ? ImmutableSet.of(l.getValue()) : ImmutableSet.of();
  }

  private static final Tag2Collector INSTANCE = new Tag2Collector();
}
