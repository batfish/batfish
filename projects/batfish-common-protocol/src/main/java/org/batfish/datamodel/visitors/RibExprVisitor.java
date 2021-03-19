package org.batfish.datamodel.visitors;

import org.batfish.datamodel.routing_policy.expr.MainRib;
import org.batfish.datamodel.routing_policy.expr.RibExpr;

/**
 * Visitor of {@link RibExpr} that takes a single generic argument and returns a generic argument.
 */
public interface RibExprVisitor<T, U> {

  T visitMainRib(MainRib mainRib, U arg);
}
