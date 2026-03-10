package org.batfish.representation.cisco;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

@ParametersAreNonnullByDefault
public final class RouteMapMatchIpAccessListLine extends RouteMapMatchLine {

  private final @Nonnull Set<String> _listNames;

  public RouteMapMatchIpAccessListLine(Set<String> listNames) {
    _listNames = listNames;
  }

  public @Nonnull Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public @Nonnull BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    List<BooleanExpr> disjuncts = Lists.newLinkedList();
    for (String listName : _listNames) {
      /*
       * TODO: it's weird to have ACLs modeled as route filter lists. It seems like they should at least have some
       * kind of auto-generated name to imply that they came from ACLs.
       */
      RouteFilterList routeFilterList = c.getRouteFilterLists().get(listName);
      if (routeFilterList != null) {
        disjuncts.add(
            new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(listName)));
      }
    }
    if (disjuncts.isEmpty()) {
      // None of the referenced ACLs exists, so this entire match line is ignored.
      return BooleanExprs.TRUE;
    } else if (disjuncts.size() == 1) {
      return disjuncts.get(0);
    }
    return new Disjunction(disjuncts);
  }

  @Override
  public <T> T accept(RouteMapMatchLineVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpAccessListLine(this);
  }
}
