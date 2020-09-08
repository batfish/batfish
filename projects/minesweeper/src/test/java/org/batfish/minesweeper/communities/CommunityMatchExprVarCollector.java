package org.batfish.minesweeper.communities;

import java.util.Set;
import org.batfish.datamodel.routing_policy.communities.AllExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.AllLargeCommunities;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityContext;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorHighMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorLowMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityLocalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SiteOfOriginExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.communities.VpnDistinguisherExtendedCommunities;
import org.batfish.minesweeper.CommunityVar;

public class CommunityMatchExprVarCollector
    implements CommunityMatchExprVisitor<Set<CommunityVar>, CommunityContext> {
  @Override
  public Set<CommunityVar> visitAllExtendedCommunities(
      AllExtendedCommunities allExtendedCommunities, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitAllLargeCommunities(
      AllLargeCommunities allLargeCommunities, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitAllStandardCommunities(
      AllStandardCommunities allStandardCommunities, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityAcl(CommunityAcl communityAcl, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityIn(CommunityIn communityIn, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityIs(CommunityIs communityIs, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchAll(
      CommunityMatchAll communityMatchAll, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchAny(
      CommunityMatchAny communityMatchAny, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchRegex(
      CommunityMatchRegex communityMatchRegex, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityNot(CommunityNot communityNot, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch,
      CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, CommunityContext arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities,
      CommunityContext arg) {
    return null;
  }
}
