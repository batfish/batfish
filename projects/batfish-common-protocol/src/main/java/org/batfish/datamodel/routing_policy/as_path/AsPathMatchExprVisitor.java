package org.batfish.datamodel.routing_policy.as_path;

/**
 * A visitor of {@link AsPathMatchExpr} that takes a single generic argument and returns a generic
 * argument.
 */
public interface AsPathMatchExprVisitor<T, U> {
  T visitAsPathMatchAny(AsPathMatchAny asPathMatchAny, U arg);

  T visitAsPathMatchExprReference(AsPathMatchExprReference asPathMatchExprReference, U arg);

  T visitAsPathMatchRegex(AsPathMatchRegex asPathMatchRegex, U arg);

  T visitAsSetsMatchingRanges(AsSetsMatchingRanges asSetsMatchingRanges, U arg);

  T visitHasAsPathLength(HasAsPathLength hasAsPathLength, U arg);
}
