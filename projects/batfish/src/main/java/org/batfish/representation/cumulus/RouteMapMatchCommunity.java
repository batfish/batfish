package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

/** A {@link RouteMapMatch} that matches routes based on the route's community attribute. */
public final class RouteMapMatchCommunity implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchCommunity(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    // TODO
    throw new BatfishException("to be implemented");
  }
}
