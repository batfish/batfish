package org.batfish.datamodel.routing_policy.communities;

/**
 * A visitor of {@link CommunitySetMatchExpr} that takes 1 generic argument and returns a generic
 * value.
 */
public interface CommunitySetMatchExprVisitor<T, U> {

  T visitCommunitySetAcl(CommunitySetAcl communitySetAcl, U arg);

  T visitCommunitySetMatchAll(CommunitySetMatchAll communitySetMatchAll, U arg);

  T visitCommunitySetMatchAny(CommunitySetMatchAny communitySetMatchAny, U arg);

  T visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, U arg);

  T visitCommunitySetMatchRegex(CommunitySetMatchRegex communitySetMatchRegex, U arg);

  T visitCommunitySetNot(CommunitySetNot communitySetNot, U arg);

  T visitHasCommunity(HasCommunity hasCommunity, U arg);

  T visitHasSize(HasSize hasSize, U arg);
}
