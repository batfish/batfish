package org.batfish.datamodel.routing_policy.communities;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** A visitor for evaluating a {@link CommunityMatchExpr} under a {@link CommunityContext}. */
public final class CommunityMatchExprEvaluator implements CommunityMatchExprVisitor<Boolean> {

  public CommunityMatchExprEvaluator(Community community, CommunityContext ctx) {
    _community = community;
    _ctx = ctx;
  }

  @Override
  public Boolean visitAllExtendedCommunities(AllExtendedCommunities allExtendedCommunities) {
    return _community instanceof ExtendedCommunity;
  }

  @Override
  public Boolean visitAllLargeCommunities(AllLargeCommunities allLargeCommunities) {
    return _community instanceof LargeCommunity;
  }

  @Override
  public Boolean visitAllStandardCommunities(AllStandardCommunities allStandardCommunities) {
    return _community instanceof StandardCommunity;
  }

  @Override
  public Boolean visitCommunityAcl(CommunityAcl communityAcl) {
    for (CommunityAclLine line : communityAcl.getLines()) {
      if (line.getCommunityMatchExpr().accept(this)) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public Boolean visitCommunityIs(CommunityIs communityIs) {
    return _community.equals(communityIs.getCommunity());
  }

  @Override
  public Boolean visitCommunityMatchAll(CommunityMatchAll communityMatchAll) {
    return communityMatchAll.getExprs().stream().allMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitCommunityMatchAny(CommunityMatchAny communityMatchAny) {
    return communityMatchAny.getExprs().stream().anyMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitCommunityMatchExprReference(
      CommunityMatchExprReference communityMatchExprReference) {
    CommunityMatchExpr expr =
        _ctx.getCommunityMatchExprs().get(communityMatchExprReference.getName());
    if (expr == null) {
      return false;
    }
    return expr.accept(this);
  }

  @Override
  public Boolean visitCommunityMatchRegex(CommunityMatchRegex communityMatchRegex) {
    return Pattern.compile(communityMatchRegex.getRegex())
        .matcher(
            communityMatchRegex.getCommunityRendering().accept(new CommunityRenderer(_community)))
        .find();
  }

  @Override
  public Boolean visitCommunityNot(CommunityNot communityNot) {
    return !communityNot.getExpr().accept(this);
  }

  @Override
  public Boolean visitRouteTargetExtendedCommunities(
      RouteTargetExtendedCommunities routeTargetExtendedCommunities) {
    if (!(_community instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) _community).isRouteTarget();
  }

  @Override
  public Boolean visitSiteOfOriginExtendedCommunities(
      SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities) {
    if (!(_community instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) _community).isRouteOrigin();
  }

  @Override
  public Boolean visitVpnDistinguisherExtendedCommunities(
      VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities) {
    if (!(_community instanceof ExtendedCommunity)) {
      return false;
    }
    return ((ExtendedCommunity) _community).isVpnDistinguisher();
  }

  private final @Nonnull Community _community;
  private final @Nonnull CommunityContext _ctx;
}
