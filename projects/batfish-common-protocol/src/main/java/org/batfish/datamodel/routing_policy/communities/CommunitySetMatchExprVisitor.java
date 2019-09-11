package org.batfish.datamodel.routing_policy.communities;

/** A visitor of {@link CommunitySetMatchExpr}. */
public interface CommunitySetMatchExprVisitor<T> {

  T visitCommunitySetAcl(CommunitySetAcl communitySetAcl);

  T visitCommunitySetMatchAll(CommunitySetMatchAll communitySetMatchAll);

  T visitCommunitySetMatchAny(CommunitySetMatchAny communitySetMatchAny);

  T visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference);

  T visitCommunitySetMatchRegex(CommunitySetMatchRegex communitySetMatchRegex);

  T visitCommunitySetNot(CommunitySetNot communitySetNot);

  T visitHasCommunity(HasCommunity hasCommunity);
}
