package org.batfish.datamodel.routing_policy.communities;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

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
  public @Nonnull Boolean visitCommunityIs(CommunityIs communityIs, Community arg) {
    return arg.equals(communityIs.getCommunity());
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchAll(
      CommunityMatchAll communityMatchAll, Community arg) {
    return communityMatchAll.getExprs().stream().allMatch(expr -> expr.accept(this, arg));
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchAny(
      CommunityMatchAny communityMatchAny, Community arg) {
    return communityMatchAny.getExprs().stream().anyMatch(expr -> expr.accept(this, arg));
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference, Community arg) {
    CommunityMatchExpr expr =
        _ctx.getCommunityMatchExprs().get(communityMatchExprReference.getName());
    if (expr == null) {
      return false;
    }
    return expr.accept(this, arg);
  }

  @Override
  public @Nonnull Boolean visitCommunityMatchRegex(
      CommunityMatchRegex communityMatchRegex, Community arg) {
    return Pattern.compile(communityMatchRegex.getRegex())
        .matcher(
            communityMatchRegex.getCommunityRendering().accept(CommunityRenderer.instance(), arg))
        .find();
  }

  @Override
  public @Nonnull Boolean visitCommunityNot(CommunityNot communityNot, Community arg) {
    return !communityNot.getExpr().accept(this, arg);
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
  public @Nonnull Boolean visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities, Community arg) {
    if (!(arg instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) arg).isVpnDistinguisher();
  }

  private final @Nonnull CommunityContext _ctx;
}
