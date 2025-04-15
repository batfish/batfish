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
import org.batfish.datamodel.routing_policy.expr.MatchClusterListLength;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchInterface;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchLocalPreference;
import org.batfish.datamodel.routing_policy.expr.MatchLocalRouteSourcePrefixLength;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;
import org.batfish.datamodel.routing_policy.expr.MatchPeerAddress;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProcessAsn;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchRouteType;
import org.batfish.datamodel.routing_policy.expr.MatchSourceProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community literals and regexes in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprVarCollector
    implements BooleanExprVisitor<Set<CommunityVar>, Tuple<Set<String>, Configuration>> {

  @Override
  public Set<CommunityVar> visitMatchClusterListLength(
      MatchClusterListLength matchClusterListLength, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitBooleanExprs(
      StaticBooleanExpr staticBooleanExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitCallExpr(CallExpr callExpr, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callExpr.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and recurse.
    arg.getFirst().add(callExpr.getCalledPolicyName());

    return new RoutePolicyStatementVarCollector()
        .visitAll(
            arg.getSecond()
                .getRoutingPolicies()
                .get(callExpr.getCalledPolicyName())
                .getStatements(),
            arg);
  }

  @Override
  public Set<CommunityVar> visitConjunction(
      Conjunction conjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(conjunction.getConjuncts(), arg);
  }

  @Override
  public Set<CommunityVar> visitConjunctionChain(
      ConjunctionChain conjunctionChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public Set<CommunityVar> visitDisjunction(
      Disjunction disjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(disjunction.getDisjuncts(), arg);
  }

  @Override
  public Set<CommunityVar> visitFirstMatchChain(
      FirstMatchChain firstMatchChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(firstMatchChain.getSubroutines(), arg);
  }

  @Override
  public Set<CommunityVar> visitTrackSucceeded(
      TrackSucceeded trackSucceeded, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitHasRoute(HasRoute hasRoute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchAsPath(
      MatchAsPath matchAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchBgpSessionType(
      MatchBgpSessionType matchBgpSessionType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchColor(
      MatchColor matchColor, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchCommunities(
      MatchCommunities matchCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<CommunityVar>builder()
        .addAll(
            matchCommunities
                .getCommunitySetExpr()
                .accept(new CommunitySetExprVarCollector(), arg.getSecond()))
        .addAll(
            matchCommunities
                .getCommunitySetMatchExpr()
                .accept(new CommunitySetMatchExprVarCollector(), arg.getSecond()))
        .build();
  }

  @Override
  public Set<CommunityVar> visitMatchInterface(
      MatchInterface matchInterface, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchIpv4(
      MatchIpv4 matchIpv4, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchLocalPreference(
      MatchLocalPreference matchLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchMetric(
      MatchMetric matchMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchPeerAddress(
      MatchPeerAddress matchPeerAddress, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchPrefixSet(
      MatchPrefixSet matchPrefixSet, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchProcessAsn(
      MatchProcessAsn matchProcessAsn, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchProtocol(
      MatchProtocol matchProtocol, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchRouteType(
      MatchRouteType matchRouteType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchSourceProtocol(
      MatchSourceProtocol matchSourceProtocol, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchSourceVrf(
      MatchSourceVrf matchSourceVrf, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitMatchTag(MatchTag matchTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitNot(Not not, Tuple<Set<String>, Configuration> arg) {
    return not.getExpr().accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitRouteIsClassful(
      RouteIsClassful routeIsClassful, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Tuple<Set<String>, Configuration> arg) {
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

  private Set<CommunityVar> visitAll(
      List<BooleanExpr> exprs, Tuple<Set<String>, Configuration> arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
