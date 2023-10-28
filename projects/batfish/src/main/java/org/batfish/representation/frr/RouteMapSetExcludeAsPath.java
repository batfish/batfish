package org.batfish.representation.frr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Clause of set as-path exclude in route map {@see <a
 * href="https://docs.frrouting.org/en/latest/routemap.html#clicmd-set-as-path-exclude-AS-NUMBER...">docs.frrouting.org/a>}.
 */
public final class RouteMapSetExcludeAsPath implements RouteMapSet {
  /** List of as numbers to be excluded from an as-path */
  private final @Nonnull List<Long> _asns;

  public RouteMapSetExcludeAsPath(List<Long> asns) {
    _asns = asns;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(Configuration c, FrrConfiguration vc, Warnings w) {
    List<AsExpr> asExprs =
        _asns.stream().map(ExplicitAs::new).collect(ImmutableList.toImmutableList());
    return Stream.of(new ExcludeAsPath(new LiteralAsList(asExprs)));
  }

  public @Nonnull List<Long> getAsns() {
    return _asns;
  }
}
