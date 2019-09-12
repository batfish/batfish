package org.batfish.datamodel.routing_policy.communities;

/** A visitor of {@link CommunitySetExpr}. */
public interface CommunitySetExprVisitor<T> {

  T visitCommunitySetDifference(CommunitySetDifference communitySetDifference);

  T visitCommunitySetExprReference(CommunitySetExprReference communitySetExprReference);

  T visitCommunitySetReference(CommunitySetReference communitySetReference);

  T visitCommunitySetUnion(CommunitySetUnion communitySetUnion);

  T visitInputCommunities(InputCommunities inputCommunities);

  T visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet);
}
