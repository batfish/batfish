package org.batfish.datamodel.visitors;

import org.batfish.datamodel.routing_policy.expr.MainRibRoutes;

/**
 * Visitor of {@link org.batfish.datamodel.routing_policy.expr.RoutesExpr} that takes a single
 * generic argument and returns a generic argument.
 */
public interface RoutesExprVisitor<T, U> {

  T visitMainRibRoutes(MainRibRoutes mainRibRoutes, U arg);
}
