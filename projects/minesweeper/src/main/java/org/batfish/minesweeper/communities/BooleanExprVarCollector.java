package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprVisitor;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs.StaticBooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchInterface;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchLocalPreference;
import org.batfish.datamodel.routing_policy.expr.MatchLocalRouteSourcePrefixLength;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProcessAsn;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchRouteType;
import org.batfish.datamodel.routing_policy.expr.MatchSourceProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RibIntersectsPrefixSpace;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.minesweeper.CommunityVar;

/** Collect all community literals and regexes in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprVarCollector
    implements BooleanExprVisitor<Set<CommunityVar>, Configuration> {
  @Override
  public Set<CommunityVar> visitBooleanExprs(
      StaticBooleanExpr staticBooleanExpr, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitCallExpr(CallExpr callExpr, Configuration arg) {
    /* we already visit all route policies in a configuration (see Graph::findAllCommunities), so no
    need to recurse to the callee policy */
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitConjunction(Conjunction conjunction, Configuration arg) {
    return visitAll(conjunction.getConjuncts(), arg);
  }

  @Override
  public Set<CommunityVar> visitConjunctionChain(
      ConjunctionChain conjunctionChain, Configuration arg) {
    return visitAll(conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public Set<CommunityVar> visitDisjunction(Disjunction disjunction, Configuration arg) {
    return visitAll(disjunction.getDisjuncts(), arg);
  }

  @Override
  public Set<CommunityVar> visitFirstMatchChain(
      FirstMatchChain firstMatchChain, Configuration arg) {
    return visitAll(firstMatchChain.getSubroutines(), arg);
  }

  @Override
  public Set<CommunityVar> visitRibIntersectsPrefixSpace(
      RibIntersectsPrefixSpace ribIntersectsPrefixSpace, Configuration arg) {
    return ribIntersectsPrefixSpace.getRibExpr().accept(RibExprVarCollector.instance(), arg);
  }

  @Override
  public Set<CommunityVar> visitHasRoute(HasRoute hasRoute, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchAsPath(MatchAsPath matchAsPath, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchBgpSessionType(
      MatchBgpSessionType matchBgpSessionType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchColor(MatchColor matchColor, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchCommunities(
      MatchCommunities matchCommunities, Configuration arg) {
    return ImmutableSet.<CommunityVar>builder()
        .addAll(
            matchCommunities.getCommunitySetExpr().accept(new CommunitySetExprVarCollector(), arg))
        .addAll(
            matchCommunities
                .getCommunitySetMatchExpr()
                .accept(new CommunitySetMatchExprVarCollector(), arg))
        .build();
  }

  @Override
  public Set<CommunityVar> visitMatchInterface(MatchInterface matchInterface, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchIpv4(MatchIpv4 matchIpv4, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchLocalPreference(
      MatchLocalPreference matchLocalPreference, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchMetric(MatchMetric matchMetric, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchPrefixSet(MatchPrefixSet matchPrefixSet, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchProcessAsn(
      MatchProcessAsn matchProcessAsn, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchProtocol(MatchProtocol matchProtocol, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchRouteType(MatchRouteType matchRouteType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchSourceProtocol(
      MatchSourceProtocol matchSourceProtocol, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchSourceVrf(MatchSourceVrf matchSourceVrf, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchTag(MatchTag matchTag, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitNot(Not not, Configuration arg) {
    return not.getExpr().accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitRouteIsClassful(
      RouteIsClassful routeIsClassful, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Configuration arg) {
    return ImmutableSet.<CommunityVar>builder()
        .addAll(withEnvironmentExpr.getExpr().accept(this, arg))
        .addAll(
            new RoutePolicyStatementVarCollector()
                .visitAll(withEnvironmentExpr.getPreStatements(), arg))
        .addAll(
            new RoutePolicyStatementVarCollector()
                .visitAll(withEnvironmentExpr.getPostStatements(), arg))
        .addAll(
            new RoutePolicyStatementVarCollector()
                .visitAll(withEnvironmentExpr.getPostTrueStatements(), arg))
        .build();
  }

  private Set<CommunityVar> visitAll(List<BooleanExpr> exprs, Configuration arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
