package org.batfish.datamodel.routing_policy.communities;

/** A visitor of {@link CommunityMatchExpr}. */
public interface CommunityMatchExprVisitor<T> {

  T visitAllExtendedCommunities(AllExtendedCommunities allExtendedCommunities);

  T visitAllLargeCommunities(AllLargeCommunities allLargeCommunities);

  T visitAllStandardCommunities(AllStandardCommunities allStandardCommunities);

  T visitCommunityAcl(CommunityAcl communityAcl);

  T visitCommunityIs(CommunityIs communityIs);

  T visitCommunityMatchAll(CommunityMatchAll communityMatchAll);

  T visitCommunityMatchAny(CommunityMatchAny communityMatchAny);

  T visitCommunityMatchExprReference(CommunityMatchExprReference communityMatchExprReference);

  T visitCommunityMatchRegex(CommunityMatchRegex communityMatchRegex);

  T visitCommunityNot(CommunityNot communityNot);

  T visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities);

  T visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities);

  T visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities);
}
