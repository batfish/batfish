package org.batfish.datamodel.visitors;

import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;

/**
 * Visitor of {@link org.batfish.datamodel.routing_policy.expr.PrefixSpaceExpr} that takes a single
 * generic argument and returns a generic argument.
 */
public interface PrefixSpaceExprVisitor<T, U> {

  T visitExplicitPrefixSet(ExplicitPrefixSet explicitPrefixSet, U arg);
}
