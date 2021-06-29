package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Clause of set as-path prepend in route map. */
// TODO rename RouteMapSetPrependAsPath
@ParametersAreNonnullByDefault
public class RouteMapSetPrependAsPath implements RouteMapSet {

  @Nonnull private final List<Long> _asns;

  public RouteMapSetPrependAsPath(List<Long> asns) {
    _asns = asns;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(
      Configuration c, CumulusConcatenatedConfiguration vc, Warnings w) {
    List<AsExpr> asExprs =
        _asns.stream().map(ExplicitAs::new).collect(ImmutableList.toImmutableList());
    return Stream.of(new PrependAsPath(new LiteralAsList(asExprs)));
  }

  @Nonnull
  public List<Long> getAsns() {
    return _asns;
  }
}
