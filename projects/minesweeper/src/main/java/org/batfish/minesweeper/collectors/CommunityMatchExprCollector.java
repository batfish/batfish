package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.AllExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.AllLargeCommunities;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorHighMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorLowMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityLocalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.OpaqueExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SiteOfOriginExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.communities.VpnDistinguisherExtendedCommunities;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collect all community-list names in a {@link CommunityMatchExpr}. The visitor takes as argument a
 * set of seen community names (used to break out of potential infinite loops in a malformed
 * snapshot) and a Batfish {@link Configuration} used to dereference some community expressions.
 */
@ParametersAreNonnullByDefault
public class CommunityMatchExprCollector
    implements CommunityMatchExprVisitor<Set<String>, Tuple<Set<String>, Configuration>> {

  private static final Logger LOGGER = LogManager.getLogger(CommunityMatchExprCollector.class);

  @Override
  public Set<String> visitAllExtendedCommunities(
      AllExtendedCommunities allExtendedCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitAllLargeCommunities(
      AllLargeCommunities allLargeCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitAllStandardCommunities(
      AllStandardCommunities allStandardCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitCommunityAcl(
      CommunityAcl communityAcl, Tuple<Set<String>, Configuration> arg) {
    return visitAll(
        communityAcl.getLines().stream()
            .map(CommunityAclLine::getCommunityMatchExpr)
            .collect(ImmutableList.toImmutableList()),
        arg);
  }

  @Override
  public Set<String> visitCommunityIn(
      CommunityIn communityIn, Tuple<Set<String>, Configuration> arg) {
    return communityIn.getCommunitySetExpr().accept(new CommunitySetExprCollector(), arg);
  }

  @Override
  public Set<String> visitCommunityIs(
      CommunityIs communityIs, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitCommunityMatchAll(
      CommunityMatchAll communityMatchAll, Tuple<Set<String>, Configuration> arg) {
    return visitAll(communityMatchAll.getExprs(), arg);
  }

  @Override
  public Set<String> visitCommunityMatchAny(
      CommunityMatchAny communityMatchAny, Tuple<Set<String>, Configuration> arg) {
    return visitAll(communityMatchAny.getExprs(), arg);
  }

  @Override
  public Set<String> visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference,
      Tuple<Set<String>, Configuration> arg) {
    String name = communityMatchExprReference.getName();
    // In case we have already dereferenced this community in this visit, issue a warning and return
    // an empty set.
    if (arg.getFirst().contains(name)) {
      LOGGER.warn("Cycle detected in communities - this denotes a malformed snapshot.");
      return ImmutableSet.of();
    }

    CommunityMatchExpr matchExpr = arg.getSecond().getCommunityMatchExprs().get(name);
    if (matchExpr == null) {
      throw new BatfishException("Cannot find community match expression: " + name);
    }

    // Add this name to set of seen references.
    Set<String> newSeen = new HashSet<>(arg.getFirst());
    newSeen.add(name);
    return ImmutableSet.<String>builder()
        .add(name)
        .addAll(matchExpr.accept(this, new Tuple<>(newSeen, arg.getSecond())))
        .build();
  }

  @Override
  public Set<String> visitCommunityMatchRegex(
      CommunityMatchRegex communityMatchRegex, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitCommunityNot(
      CommunityNot communityNot, Tuple<Set<String>, Configuration> arg) {
    return communityNot.getExpr().accept(this, arg);
  }

  @Override
  public Set<String> visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitOpaqueExtendedCommunities(
      OpaqueExtendedCommunities opaqueExtendedCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  private Set<String> visitAll(
      Collection<CommunityMatchExpr> exprs, Tuple<Set<String>, Configuration> arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
