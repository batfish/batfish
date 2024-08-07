package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
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
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.CommunityVar.Type;
import org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD.Arg;
import org.batfish.minesweeper.communities.CommunityMatchExprVarCollector;

/**
 * Create a BDD from a {@link CommunityMatchExpr} expression, such that the models of the BDD
 * represent all and only communities that match the expression. This BDD is used as part of
 * symbolic route analysis of the {@link
 * org.batfish.datamodel.routing_policy.communities.MatchCommunities} expression.
 */
@ParametersAreNonnullByDefault
public class CommunityMatchExprToBDD implements CommunityMatchExprVisitor<BDD, Arg> {

  private final Predicate<CommunityVar> _isExtendedCommunityLiteral =
      c -> c.getType() == Type.EXACT && c.getLiteralValue() instanceof ExtendedCommunity;

  @Override
  public BDD visitAllExtendedCommunities(AllExtendedCommunities allExtendedCommunities, Arg arg) {
    // we currently only support extended community literals (as opposed to also regexes)
    return matchingCommunityVarsToBDD(_isExtendedCommunityLiteral, arg);
  }

  @Override
  public BDD visitAllLargeCommunities(AllLargeCommunities allLargeCommunities, Arg arg) {
    // we currently only support large community literals (as opposed to also regexes)
    return matchingCommunityVarsToBDD(
        c -> c.getType() == Type.EXACT && c.getLiteralValue() instanceof LargeCommunity, arg);
  }

  @Override
  public BDD visitAllStandardCommunities(AllStandardCommunities allStandardCommunities, Arg arg) {
    return CommunitySetMatchExprToBDD.communityVarsToBDD(
        ImmutableSet.of(CommunityVar.ALL_STANDARD_COMMUNITIES), arg);
  }

  @Override
  public BDD visitCommunityAcl(CommunityAcl communityAcl, Arg arg) {
    List<CommunityAclLine> lines = new ArrayList<>(communityAcl.getLines());
    Collections.reverse(lines);
    BDD acc = arg.getTransferBDD().getFactory().zero();
    for (CommunityAclLine line : lines) {
      boolean action = (line.getAction() == LineAction.PERMIT);
      BDD lineBDD = line.getCommunityMatchExpr().accept(this, arg);
      acc = lineBDD.ite(arg.getTransferBDD().mkBDD(action), acc);
    }
    return acc;
  }

  @Override
  public BDD visitCommunityIn(CommunityIn communityIn, Arg arg) {
    return communityIn.getCommunitySetExpr().accept(new CommunitySetExprToBDD(), arg);
  }

  @Override
  public BDD visitCommunityIs(CommunityIs communityIs, Arg arg) {

    return CommunitySetMatchExprToBDD.communityVarsToBDD(
        communityIs.accept(new CommunityMatchExprVarCollector(), arg.getConfiguration()), arg);
  }

  @Override
  public BDD visitCommunityMatchAll(CommunityMatchAll communityMatchAll, Arg arg) {
    return communityMatchAll.getExprs().stream()
        .map(expr -> expr.accept(this, arg))
        .reduce(arg.getTransferBDD().getFactory().one(), BDD::and);
  }

  @Override
  public BDD visitCommunityMatchAny(CommunityMatchAny communityMatchAny, Arg arg) {
    return arg.getTransferBDD()
        .getFactory()
        .orAll(
            communityMatchAny.getExprs().stream()
                .map(expr -> expr.accept(this, arg))
                .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, Arg arg) {
    String name = communityMatchExprReference.getName();
    CommunityMatchExpr expr = arg.getCommunityMatchExpr(name);
    return expr.accept(this, arg);
  }

  @Override
  public BDD visitCommunityMatchRegex(CommunityMatchRegex communityMatchRegex, Arg arg) {
    return CommunitySetMatchExprToBDD.communityVarsToBDD(
        communityMatchRegex.accept(new CommunityMatchExprVarCollector(), arg.getConfiguration()),
        arg);
  }

  @Override
  public BDD visitCommunityNot(CommunityNot communityNot, Arg arg) {
    BDD toBeNegated = communityNot.getExpr().accept(this, arg);
    return toBeNegated.not();
  }

  @Override
  public BDD visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      Arg arg) {
    throw new UnsupportedOperationException(
        extendedCommunityGlobalAdministratorHighMatch.toString());
  }

  @Override
  public BDD visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      Arg arg) {
    throw new UnsupportedOperationException(
        extendedCommunityGlobalAdministratorLowMatch.toString());
  }

  @Override
  public BDD visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      Arg arg) {
    throw new UnsupportedOperationException(extendedCommunityGlobalAdministratorMatch.toString());
  }

  @Override
  public BDD visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch, Arg arg) {
    throw new UnsupportedOperationException(extendedCommunityLocalAdministratorMatch.toString());
  }

  @Override
  public BDD visitOpaqueExtendedCommunities(
      OpaqueExtendedCommunities opaqueExtendedCommunities, Arg arg) {
    return matchingCommunityVarsToBDD(
        _isExtendedCommunityLiteral.and(
            c -> {
              assert c.getLiteralValue() != null;
              ExtendedCommunity ec = (ExtendedCommunity) c.getLiteralValue();
              return ec.isOpaque()
                  && ec.isTransitive() == opaqueExtendedCommunities.getIsTransitive()
                  && ec.getSubtype() == opaqueExtendedCommunities.getSubtype();
            }),
        arg);
  }

  @Override
  public BDD visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, Arg arg) {
    return matchingCommunityVarsToBDD(
        _isExtendedCommunityLiteral.and(
            c -> {
              assert c.getLiteralValue() != null;
              return ((ExtendedCommunity) c.getLiteralValue()).isRouteTarget();
            }),
        arg);
  }

  @Override
  public BDD visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, Arg arg) {
    return matchingCommunityVarsToBDD(
        _isExtendedCommunityLiteral.and(
            c -> {
              assert c.getLiteralValue() != null;
              return ((ExtendedCommunity) c.getLiteralValue()).isRouteOrigin();
            }),
        arg);
  }

  @Override
  public BDD visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch, Arg arg) {
    Optional<CommunityVar> optCVar =
        CommunityMatchExprVarCollector.standardCommunityHighMatchToRegex(
            standardCommunityHighMatch);
    // If the community variable does not exist it means we currently don't support this
    // StandardCommunityHighMatch, so we throw an exception
    return optCVar
        .map(cvar -> CommunitySetMatchExprToBDD.communityVarsToBDD(ImmutableSet.of(cvar), arg))
        .orElseThrow(
            () -> new UnsupportedOperationException(standardCommunityHighMatch.toString()));
  }

  @Override
  public BDD visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, Arg arg) {
    Optional<CommunityVar> optCVar =
        CommunityMatchExprVarCollector.standardCommunityLowMatchToRegex(standardCommunityLowMatch);
    // If the community variable does not exist it means we currently don't support this
    // StandardCommunityLowMatch, so we throw an exception
    return optCVar
        .map(cvar -> CommunitySetMatchExprToBDD.communityVarsToBDD(ImmutableSet.of(cvar), arg))
        .orElseThrow(() -> new UnsupportedOperationException(standardCommunityLowMatch.toString()));
  }

  @Override
  public BDD visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, Arg arg) {
    return matchingCommunityVarsToBDD(
        _isExtendedCommunityLiteral.and(
            c -> {
              assert c.getLiteralValue() != null;
              return ((ExtendedCommunity) c.getLiteralValue()).isVpnDistinguisher();
            }),
        arg);
  }

  // produce a BDD representing the set of community variables that satisfy the given predicate
  private static BDD matchingCommunityVarsToBDD(Predicate<CommunityVar> predicate, Arg arg) {
    Set<CommunityVar> cvars =
        arg.getTransferBDD().getCommunityAtomicPredicates().keySet().stream()
            .filter(predicate)
            .collect(ImmutableSet.toImmutableSet());
    return CommunitySetMatchExprToBDD.communityVarsToBDD(cvars, arg);
  }
}
