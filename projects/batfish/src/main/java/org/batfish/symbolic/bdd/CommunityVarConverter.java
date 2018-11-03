package org.batfish.symbolic.bdd;

import javax.annotation.Nonnull;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunityHalvesExpr;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.EmptyCommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunityConjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.visitors.CommunitySetExprVisitor;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;

/**
 * Visitor for converting a non-recursive {@link CommunitySetExpr} to a {@link CommunityVar} for
 * symbolic analysis.
 */
public final class CommunityVarConverter implements CommunitySetExprVisitor<CommunityVar> {

  private static final CommunityVarConverter INSTANCE = new CommunityVarConverter();

  public static @Nonnull CommunityVar toCommunityVar(@Nonnull CommunitySetExpr matchCondition) {
    return matchCondition.accept(INSTANCE);
  }

  public static @Nonnull CommunityVar toCommunityVar(long community) {
    return new CommunityVar(Type.EXACT, CommonUtil.longToCommunity(community), community);
  }

  @Override
  public CommunityVar visitCommunityHalvesExpr(CommunityHalvesExpr communityHalvesExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public CommunityVar visitCommunityList(CommunityList communityList) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public CommunityVar visitEmptyCommunitySetExpr(EmptyCommunitySetExpr emptyCommunitySetExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public CommunityVar visitLiteralCommunity(LiteralCommunity literalCommunity) {
    return toCommunityVar(literalCommunity.getCommunity());
  }

  @Override
  public CommunityVar visitLiteralCommunityConjunction(
      LiteralCommunityConjunction literalCommunityConjunction) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public CommunityVar visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public CommunityVar visitNamedCommunitySet(NamedCommunitySet namedCommunitySet) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public CommunityVar visitRegexCommunitySet(RegexCommunitySet regexCommunitySet) {
    return new CommunityVar(Type.REGEX, regexCommunitySet.getRegex(), null);
  }
}
