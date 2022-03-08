package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link Statement} that prepends AS numbers to AS paths. */
@ParametersAreNonnullByDefault
public final class PsThenAsPathExpandAsList extends PsThenAsPathExpand {

  public PsThenAsPathExpandAsList(List<Long> asList) {
    assert !asList.isEmpty();
    _asList = ImmutableList.copyOf(asList);
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    List<AsExpr> asList =
        _asList.stream().map(ExplicitAs::new).collect(ImmutableList.toImmutableList());
    statements.add(new PrependAsPath(new LiteralAsList(asList)));
  }

  public @Nonnull List<Long> getAsList() {
    return _asList;
  }

  private final @Nonnull List<Long> _asList;
}
