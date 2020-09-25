package org.batfish.minesweeper;

import static org.batfish.minesweeper.bdd.CommunityVarConverter.toCommunityVar;

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
import org.batfish.minesweeper.bdd.CommunityVarConverter;

/** Collect up all community literals and regexes in a {@link CommunitySetExpr}. */
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
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void visitCommunityList(CommunityList communityList) {
    /*
     * The following implementation should be considered deprecated, but exists to recreate old behavior for existing tests.
     * The old behavior only supported regexes as match conditions. For relevant tests, those regexes were actually created
     * from IOS standard community-lists, i.e. literal communities. So the temporary implementation below expects all match
     * conditions here to be literal communities, which it then converts to regexes as expected by the old implementation.
     * Actual regexes are unmodified.
     */
    communityList.getLines().stream()
        .map(CommunityListLine::getMatchCondition)
        .map(CommunitySetExprToRegex::convert)
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
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet) {
    literalCommunitySet.getCommunities().stream()
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
