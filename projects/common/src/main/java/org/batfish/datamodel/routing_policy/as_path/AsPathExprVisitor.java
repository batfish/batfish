package org.batfish.datamodel.routing_policy.as_path;

/**
 * A visitor of {@link AsPathExpr} that takes a single generic argument and returns a generic
 * argument.
 */
public interface AsPathExprVisitor<T, U> {

  T visitAsPathExprReference(AsPathExprReference asPathExprReference, U arg);

  T visitDedupedAsPath(DedupedAsPath dedupedAsPath, U arg);

  T visitInputAsPath(InputAsPath inputAsPath, U arg);
}
