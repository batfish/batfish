package org.batfish.datamodel.routing_policy.communities;

/**
 * A visitor of {@link CommunityMatchExpr} that takes 1 generic argument and returns a generic
 * value.
 */
public interface CommunityMatchExprVisitor<T, U> {

  T visitAllExtendedCommunities(AllExtendedCommunities allExtendedCommunities, U arg);

  T visitAllLargeCommunities(AllLargeCommunities allLargeCommunities, U arg);

  T visitAllStandardCommunities(AllStandardCommunities allStandardCommunities, U arg);

  T visitCommunityAcl(CommunityAcl communityAcl, U arg);

  T visitCommunityIn(CommunityIn communityIn, U arg);

  T visitCommunityIs(CommunityIs communityIs, U arg);

  T visitCommunityMatchAll(CommunityMatchAll communityMatchAll, U arg);

  T visitCommunityMatchAny(CommunityMatchAny communityMatchAny, U arg);

  T visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, U arg);

  T visitCommunityMatchRegex(CommunityMatchRegex communityMatchRegex, U arg);

  T visitCommunityNot(CommunityNot communityNot, U arg);

  T visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      U arg);

  T visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      U arg);

  T visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch, U arg);

  T visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch, U arg);

  T visitOpaqueExtendedCommunities(OpaqueExtendedCommunities opaqueExtendedCommunities, U arg);

  T visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, U arg);

  T visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, U arg);

  T visitStandardCommunityHighMatch(StandardCommunityHighMatch standardCommunityHighMatch, U arg);

  T visitStandardCommunityLowMatch(StandardCommunityLowMatch standardCommunityLowMatch, U arg);

  T visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, U arg);
}
