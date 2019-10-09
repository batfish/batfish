package org.batfish.datamodel.routing_policy.communities;

/** A visitor of {@link CommunityExpr} that takes 1 generic argument and returns a generic value. */
public interface CommunityExprVisitor<T, U> {

  T visitStandardCommunityHighLowExprs(
      StandardCommunityHighLowExprs standardCommunityHighLowExprs, U arg);
}
