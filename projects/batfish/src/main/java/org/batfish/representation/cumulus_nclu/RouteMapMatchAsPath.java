package org.batfish.representation.cumulus_nclu;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;

/** A {@link RouteMapMatch} that matches routes based on the route's AS path. */
public final class RouteMapMatchAsPath implements RouteMapMatch {

  @Nonnull private final String _name;

  public RouteMapMatchAsPath(@Nonnull String name) {
    _name = name;
  }

  @Nonnull
  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    if (!c.getAsPathAccessLists().containsKey(_name)) {
      // Don't match anything. Rely on undefined references to surface this problem.
      return BooleanExprs.FALSE;
    }
    return new MatchAsPath(new NamedAsPathSet(_name));
  }

  public @Nonnull String getName() {
    return _name;
  }
}
