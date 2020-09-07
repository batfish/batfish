package org.batfish.minesweeper;

import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.AllExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.AllLargeCommunities;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
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

public class CommunityMatchExprVarCollector
    implements CommunityMatchExprVisitor<Set<CommunityVar>, Configuration> {
  @Override
  public Set<CommunityVar> visitAllExtendedCommunities(
      AllExtendedCommunities allExtendedCommunities, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitAllLargeCommunities(
      AllLargeCommunities allLargeCommunities, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitAllStandardCommunities(
      AllStandardCommunities allStandardCommunities, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityAcl(CommunityAcl communityAcl, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityIn(CommunityIn communityIn, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityIs(CommunityIs communityIs, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchAll(
      CommunityMatchAll communityMatchAll, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchAny(
      CommunityMatchAny communityMatchAny, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchRegex(
      CommunityMatchRegex communityMatchRegex, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunityNot(CommunityNot communityNot, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch,
      Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, Configuration arg) {
    return null;
  }
}
