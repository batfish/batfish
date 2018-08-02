package org.batfish.symbolic;

import static org.batfish.symbolic.bdd.CommunityVarConverter.toCommunityVar;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
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
import org.batfish.symbolic.bdd.CommunityVarConverter;

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
    communityList
        .getLines()
        .stream()
        .map(CommunityListLine::getMatchCondition)
        .forEach(expr -> expr.accept(this));
  }

  @Override
  public void visitEmptyCommunitySetExpr(EmptyCommunitySetExpr emptyCommunitySetExpr) {}

  @Override
  public void visitLiteralCommunity(LiteralCommunity literalCommunity) {
    _builder.add(toCommunityVar(literalCommunity));
  }

  @Override
  public void visitLiteralCommunityConjunction(
      LiteralCommunityConjunction literalCommunityConjunction) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet) {
    literalCommunitySet
        .getCommunities()
        .stream()
        .map(CommunityVarConverter::toCommunityVar)
        .forEach(_builder::add);
  }

  @Override
  public void visitNamedCommunitySet(NamedCommunitySet namedCommunitySet) {
    visitCommunityList(_configuration.getCommunityLists().get(namedCommunitySet.getName()));
  }

  @Override
  public void visitRegexCommunitySet(RegexCommunitySet regexCommunitySet) {
    _builder.add(toCommunityVar(regexCommunitySet));
  }
}
