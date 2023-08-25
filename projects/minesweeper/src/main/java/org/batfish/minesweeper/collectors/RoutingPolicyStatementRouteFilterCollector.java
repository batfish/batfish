package org.batfish.minesweeper.collectors;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.RoutePolicyStatementMatchCollector;

/** Collect all prefix-list names in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutingPolicyStatementRouteFilterCollector
    extends RoutePolicyStatementMatchCollector<String> {
  public RoutingPolicyStatementRouteFilterCollector() {
    super(new RouteFilterBooleanExprCollector());
  }
}
