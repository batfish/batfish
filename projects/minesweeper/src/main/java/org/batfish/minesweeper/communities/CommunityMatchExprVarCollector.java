package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SiteOfOriginExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.communities.VpnDistinguisherExtendedCommunities;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.IntMatchExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;

/** Collect all community literals and regexes in a {@link CommunityMatchExpr}. */
@ParametersAreNonnullByDefault
public class CommunityMatchExprVarCollector
    implements CommunityMatchExprVisitor<Set<CommunityVar>, Configuration> {

  @Override
  public Set<CommunityVar> visitAllExtendedCommunities(
      AllExtendedCommunities allExtendedCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitAllLargeCommunities(
      AllLargeCommunities allLargeCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitAllStandardCommunities(
      AllStandardCommunities allStandardCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitCommunityAcl(CommunityAcl communityAcl, Configuration arg) {
    return visitAll(
        communityAcl.getLines().stream()
            .map(CommunityAclLine::getCommunityMatchExpr)
            .collect(ImmutableList.toImmutableList()),
        arg);
  }

  @Override
  public Set<CommunityVar> visitCommunityIn(CommunityIn communityIn, Configuration arg) {
    return communityIn.getCommunitySetExpr().accept(new CommunitySetExprVarCollector(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunityIs(CommunityIs communityIs, Configuration arg) {
    return ImmutableSet.of(CommunityVar.from(communityIs.getCommunity()));
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchAll(
      CommunityMatchAll communityMatchAll, Configuration arg) {
    return visitAll(communityMatchAll.getExprs(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchAny(
      CommunityMatchAny communityMatchAny, Configuration arg) {
    return visitAll(communityMatchAny.getExprs(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, Configuration arg) {
    String name = communityMatchExprReference.getName();
    CommunityMatchExpr matchExpr = arg.getCommunityMatchExprs().get(name);
    if (matchExpr == null) {
      throw new BatfishException("Cannot find community match expression: " + name);
    }
    return matchExpr.accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitCommunityMatchRegex(
      CommunityMatchRegex communityMatchRegex, Configuration arg) {
    return ImmutableSet.of(CommunityVar.from(communityMatchRegex.getRegex()));
  }

  @Override
  public Set<CommunityVar> visitCommunityNot(CommunityNot communityNot, Configuration arg) {
    return communityNot.getExpr().accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      Configuration arg) {
    // This is not supported, but rather than throw we do nothing. If we end up needing to model
    // this structure, the later code will crash instead.
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      Configuration arg) {
    // This is not supported, but rather than throw we do nothing. If we end up needing to model
    // this structure, the later code will crash instead.
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      Configuration arg) {
    // This is not supported, but rather than throw we do nothing. If we end up needing to model
    // this structure, the later code will crash instead.
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch,
      Configuration arg) {
    // This is not supported, but rather than throw we do nothing. If we end up needing to model
    // this structure, the later code will crash instead.
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch, Configuration arg) {
    // If the given integer match expression is not currently supported we return an empty set
    // instead of throwing an exception.  If we end up needing to model this structure, the later
    // code will crash instead.
    return standardCommunityHighMatchToRegex(standardCommunityHighMatch)
        .map(ImmutableSet::of)
        .orElse(ImmutableSet.of());
  }

  @Override
  public Set<CommunityVar> visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, Configuration arg) {
    // If the given integer match expression is not currently supported we return an empty set
    // instead of throwing an exception.  If we end up needing to model this structure, the later
    // code will crash instead.
    return standardCommunityLowMatchToRegex(standardCommunityLowMatch)
        .map(ImmutableSet::of)
        .orElse(ImmutableSet.of());
  }

  @Override
  public Set<CommunityVar> visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  private Set<CommunityVar> visitAll(Collection<CommunityMatchExpr> exprs, Configuration arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Converts a {@link StandardCommunityHighMatch} to a {@link CommunityVar} representing it as a
   * community regex. We currently only handle a subset of {@link StandardCommunityHighMatch}
   * expressions, so we use the {@link Optional} type to indicate whether the given one is supported
   * or not.
   */
  public static Optional<CommunityVar> standardCommunityHighMatchToRegex(
      StandardCommunityHighMatch match) {
    Optional<Integer> val = intMatchEqualityExprToInt(match.getExpr());
    return val.map(i -> CommunityVar.from("^" + i + ":"));
  }

  /**
   * Converts a {@link StandardCommunityLowMatch} to a {@link CommunityVar} representing it as a
   * community regex. We currently only handle a subset of {@link StandardCommunityLowMatch}
   * expressions, so we use the {@link Optional} type to indicate whether the given one is supported
   * or not.
   */
  public static Optional<CommunityVar> standardCommunityLowMatchToRegex(
      StandardCommunityLowMatch match) {
    Optional<Integer> val = intMatchEqualityExprToInt(match.getExpr());
    return val.map(i -> CommunityVar.from(":" + i + "$"));
  }

  /**
   * The only integer match expressions currently supported are equality comparisons with an integer
   * constant; this method returns that constant.
   */
  private static Optional<Integer> intMatchEqualityExprToInt(IntMatchExpr intMatchExpr) {
    if (intMatchExpr instanceof IntComparison) {
      IntComparison intComp = (IntComparison) intMatchExpr;
      IntExpr expr = intComp.getExpr();
      if (intComp.getComparator() == IntComparator.EQ && expr instanceof LiteralInt) {
        return Optional.of(((LiteralInt) expr).getValue());
      }
    }
    return Optional.empty();
  }
}
