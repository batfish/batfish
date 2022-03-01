package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LastAs;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MultipliedAs;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * A {@link Statement} that prepends one or more copies of the most recent AS of the input as-path
 * if non-empty.
 */
@ParametersAreNonnullByDefault
public final class PsThenAsPathExpandLastAs extends PsThenAsPathExpand {

  public PsThenAsPathExpandLastAs(int count) {
    _count = count;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    statements.add(
        new If(
            MatchAsPath.of(
                InputAsPath.instance(),
                HasAsPathLength.of(new IntComparison(IntComparator.GT, new LiteralInt(0)))),
            ImmutableList.of(
                new PrependAsPath(new MultipliedAs(LastAs.instance(), new LiteralInt(_count))))));
  }

  public int getCount() {
    return _count;
  }

  private final int _count;
}
