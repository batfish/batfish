package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import org.batfish.common.util.PatternProvider;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.IntMatchExprEvaluator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongMatchExprEvaluator;

/** A visitor for evaluating a {@link CommunityMatchExpr} under a {@link CommunityContext}. */
public final class CommunityMatchExprEvaluator
    implements CommunityMatchExprVisitor<Boolean, Community> {

  public CommunityMatchExprEvaluator(CommunityContext ctx) {
    _ctx = ctx;
  }

  @Override
  public @Nonnull Boolean visitAllExtendedCommunities(
      AllExtendedCommunities allExtendedCommunities, Community arg) {
    return arg instanceof ExtendedCommunity;
  }

  @Override
  public @Nonnull Boolean visitAllLargeCommunities(
      AllLargeCommunities allLargeCommunities, Community arg) {
    return arg instanceof LargeCommunity;
  }

  @Override
  public @Nonnull Boolean visitAllStandardCommunities(
      AllStandardCommunities allStandardCommunities, Community arg) {
    return arg instanceof StandardCommunity;
  }

  @Override
  public @Nonnull Boolean visitCommunityAcl(CommunityAcl communityAcl, Community arg) {
    for (CommunityAclLine line : communityAcl.getLines()) {
      if (line.getCommunityMatchExpr().accept(this, arg)) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitCommunityIn(CommunityIn communityIn, Community arg) {
    return communityIn
        .getCommunitySetExpr()
        .accept(CommunitySetExprEvaluator.instance(), _ctx)
        .getCommunities()
        .contains(arg);
  }

  @Override
  public @Nonnull Boolean visitCommunityIs(CommunityIs communityIs, Community arg) {
    return arg.equals(communityIs.getCommunity());
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchAll(
      CommunityMatchAll communityMatchAll, Community arg) {
    for (CommunityMatchExpr expr : communityMatchAll.getExprs()) {
      if (!expr.accept(this, arg)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchAny(
      CommunityMatchAny communityMatchAny, Community arg) {
    for (CommunityMatchExpr expr : communityMatchAny.getExprs()) {
      if (expr.accept(this, arg)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, Community arg) {
    CommunityMatchExpr expr =
        _ctx.getCommunityMatchExprs().get(communityMatchExprReference.getName());
    // conversion to VI should guarantee expr is not null
    assert expr != null;
    return expr.accept(this, arg);
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchRegex(
      CommunityMatchRegex communityMatchRegex, Community arg) {
    return PatternProvider.fromString(communityMatchRegex.getRegex())
        .matcher(
            communityMatchRegex
                .getCommunityRendering()
                .accept(CommunityToRegexInputString.instance(), arg))
        .find();
  }

  @Override
  public @Nonnull Boolean visitCommunityNot(CommunityNot communityNot, Community arg) {
    return !communityNot.getExpr().accept(this, arg);
  }

  @Override
  public Boolean visitExtendedCommunityGlobalAdministratorHighMatch(
      ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
      Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return extendedCommunityGlobalAdministratorHighMatch
        .getExpr()
        .accept(
            IntMatchExprEvaluator.instance(),
            new LiteralInt((int) ((((ExtendedCommunity) arg).getGlobalAdministrator()) >> 16)));
  }

  @Override
  public Boolean visitExtendedCommunityGlobalAdministratorLowMatch(
      ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
      Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return extendedCommunityGlobalAdministratorLowMatch
        .getExpr()
        .accept(
            IntMatchExprEvaluator.instance(),
            new LiteralInt((int) ((((ExtendedCommunity) arg).getGlobalAdministrator()) & 0xFFFF)));
  }

  @Override
  public Boolean visitExtendedCommunityGlobalAdministratorMatch(
      ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
      Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return extendedCommunityGlobalAdministratorMatch
        .getExpr()
        .accept(
            LongMatchExprEvaluator.instance(),
            new LiteralLong(((ExtendedCommunity) arg).getGlobalAdministrator()));
  }

  @Override
  public Boolean visitExtendedCommunityLocalAdministratorMatch(
      ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch,
      Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return extendedCommunityLocalAdministratorMatch
        .getExpr()
        .accept(
            IntMatchExprEvaluator.instance(),
            new LiteralInt((int) ((ExtendedCommunity) arg).getLocalAdministrator()));
  }

  @Override
  public @Nonnull Boolean visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities, Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) arg).isRouteTarget();
  }

  @Override
  public @Nonnull Boolean visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities, Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) arg).isRouteOrigin();
  }

  @Override
  public Boolean visitStandardCommunityHighMatch(
      StandardCommunityHighMatch standardCommunityHighMatch, Community arg) {
    if (!(arg instanceof StandardCommunity)) {
      return false;
    }
    return standardCommunityHighMatch
        .getExpr()
        .accept(IntMatchExprEvaluator.instance(), new LiteralInt(((StandardCommunity) arg).high()));
  }

  @Override
  public Boolean visitStandardCommunityLowMatch(
      StandardCommunityLowMatch standardCommunityLowMatch, Community arg) {
    if (!(arg instanceof StandardCommunity)) {
      return false;
    }
    return standardCommunityLowMatch
        .getExpr()
        .accept(IntMatchExprEvaluator.instance(), new LiteralInt(((StandardCommunity) arg).low()));
  }

  @Override
  public @Nonnull Boolean visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) arg).isVpnDistinguisher();
  }

  private final @Nonnull CommunityContext _ctx;
}
