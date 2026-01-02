package org.batfish.datamodel.routing_policy.communities;

/**
 * A visitor of {@link CommunitySetExpr} that takes 1 generic argument and returns a generic value.
 */
public interface CommunitySetExprVisitor<T, U> {

  T visitCommunityExprsSet(CommunityExprsSet communityExprsSet, U arg);

  T visitCommunitySetDifference(CommunitySetDifference communitySetDifference, U arg);

  T visitCommunitySetExprReference(CommunitySetExprReference communitySetExprReference, U arg);

  T visitCommunitySetReference(CommunitySetReference communitySetReference, U arg);

  T visitCommunitySetUnion(CommunitySetUnion communitySetUnion, U arg);

  T visitInputCommunities(InputCommunities inputCommunities, U arg);

  T visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet, U arg);
}
