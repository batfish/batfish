package org.batfish.symbolic;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunityHalvesExpr;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.EmptyCommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunityConjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.visitors.VoidCommunitySetExprVisitor;

public class CommunityVarCollector implements VoidCommunitySetExprVisitor {

  public static @Nonnull Set<CommunityVar> collectCommunityVars(
      @Nonnull Configuration configuration, @Nonnull CommunitySetExpr communitySetExpr) {
    CommunityVarCollector visitor = new CommunityVarCollector(configuration);
    communitySetExpr.accept(visitor);
    return visitor._builder.build();
  }

  private final ImmutableSet.Builder<CommunityVar> _builder;

  private final Configuration _configuration;

  private CommunityVarCollector(@Nonnull Configuration configuration) {
    _builder = ImmutableSet.builder();
    _configuration = configuration;
  }

  @Override
  public void visitCommunityHalvesExpr(CommunityHalvesExpr communityHalvesExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitCommunityList(CommunityList communityList) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitEmptyCommunitySetExpr(EmptyCommunitySetExpr emptyCommunitySetExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitLiteralCommunity(LiteralCommunity literalCommunity) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitLiteralCommunityConjunction(
      LiteralCommunityConjunction literalCommunityConjunction) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNamedCommunitySet(NamedCommunitySet namedCommunitySet) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitRegexCommunitySet(RegexCommunitySet regexCommunitySet) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
