package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;
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
  @Override
  public BDD visitAllExtendedCommunities(AllExtendedCommunities allExtendedCommunities, Arg arg) {
    throw new UnsupportedOperationException("Match on all extended communities");
  }

  @Override
  public BDD visitAllLargeCommunities(AllLargeCommunities allLargeCommunities, Arg arg) {
    throw new UnsupportedOperationException("Match on all large communities");
  }

  @Override
  public BDD visitAllStandardCommunities(AllStandardCommunities allStandardCommunities, Arg arg) {
    throw new UnsupportedOperationException("Match on all standard communities");
  }

  @Override
  public BDD visitCommunityAcl(CommunityAcl communityAcl, Arg arg) {
    List<CommunityAclLine> lines = new ArrayList<>(communityAcl.getLines());
    Collections.reverse(lines);
    BDD acc = BDDRoute.factory.zero();
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
        communityIs.accept(
            new CommunityMatchExprVarCollector(), arg.getTransferBDD().getConfiguration()),
        arg);
  }

  @Override
  public BDD visitCommunityMatchAll(CommunityMatchAll communityMatchAll, Arg arg) {
    return communityMatchAll.getExprs().stream()
        .map(expr -> expr.accept(this, arg))
        .reduce(BDDRoute.factory.one(), BDD::and);
  }

  @Override
  public BDD visitCommunityMatchAny(CommunityMatchAny communityMatchAny, Arg arg) {
    return BDDRoute.factory.orAll(
        communityMatchAny.getExprs().stream()
            .map(expr -> expr.accept(this, arg))
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, Arg arg) {
    String name = communityMatchExprReference.getName();
    CommunityMatchExpr expr =
        arg.getTransferBDD().getConfiguration().getCommunityMatchExprs().get(name);
    if (expr == null) {
      throw new BatfishException("Cannot find community match expression: " + name);
    }
    return expr.accept(this, arg);
  }

  @Override
  public BDD visitCommunityMatchRegex(CommunityMatchRegex communityMatchRegex, Arg arg) {
    return CommunitySetMatchExprToBDD.communityVarsToBDD(
        communityMatchRegex.accept(
            new CommunityMatchExprVarCollector(), arg.getTransferBDD().getConfiguration()),
        arg);
  }

  @Override
  public BDD visitCommunityNot(CommunityNot communityNot, Arg arg) {
    BDD toBeNegated = communityNot.getExpr().accept(this, arg);
    // to negate a predicate on a single community, we diff it from a predicate representing
    // any community. simply negating toBeNegated is not sufficient because it would allow a model
    // where all atomic predicates are false, which doesn't correspond to any concrete communities.
    return arg.getBDDRoute().anyCommunity().diffWith(toBeNegated);
  }

  @Override
  public BDD visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }

  @Override
  public BDD visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }

  @Override
  public BDD visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }

  @Override
  public BDD visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch, Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }

  @Override
  public BDD visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }

  @Override
  public BDD visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }

  @Override
  public BDD visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch, Arg arg) {
    return CommunitySetMatchExprToBDD.communityVarsToBDD(
        standardCommunityHighMatch.accept(
            new CommunityMatchExprVarCollector(), arg.getTransferBDD().getConfiguration()),
        arg);
  }

  @Override
  public BDD visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, Arg arg) {
    return CommunitySetMatchExprToBDD.communityVarsToBDD(
        standardCommunityLowMatch.accept(
            new CommunityMatchExprVarCollector(), arg.getTransferBDD().getConfiguration()),
        arg);
  }

  @Override
  public BDD visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, Arg arg) {
    throw new UnsupportedOperationException(
        "Currently not supporting matches on extended communities");
  }
}
