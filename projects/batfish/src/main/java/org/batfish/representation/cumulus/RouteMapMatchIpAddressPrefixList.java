package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

/**
 * A {@link RouteMapMatch} that matches routes based on whether the route's network is matched by
 * named Ip prefix lists.
 */
public final class RouteMapMatchIpAddressPrefixList implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchIpAddressPrefixList(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  @Nonnull
  @Override
  public BooleanExpr toBooleanExpr(
      Configuration c, CumulusConcatenatedConfiguration vc, Warnings w) {
    return new Disjunction(
        _names.stream()
            .filter(vc.getIpPrefixLists()::containsKey)
            .map(
                name -> new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(name)))
            .collect(ImmutableList.toImmutableList()));
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }
}
