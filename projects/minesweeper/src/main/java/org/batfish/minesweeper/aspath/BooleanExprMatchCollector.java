package org.batfish.minesweeper.aspath;

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
import org.batfish.minesweeper.utils.Tuple;

/**
 * An abstract base class for visitors that collect up a specific set of items in the {@link
 * BooleanExpr}s of a route policy. We keep track of the set of policies that have already been
 * visited, to prevent cycles when traversing called policies recursively. Also see {@link
 * RoutePolicyStatementMatchCollector}.
 */
@ParametersAreNonnullByDefault
public abstract class BooleanExprMatchCollector<T>
    implements BooleanExprVisitor<Set<T>, Tuple<Set<String>, Configuration>> {

  @Override
  public Set<T> visitMatchClusterListLength(
      MatchClusterListLength matchClusterListLength, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitBooleanExprs(
      StaticBooleanExpr staticBooleanExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitCallExpr(CallExpr callExpr, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callExpr.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and recurse.
    arg.getFirst().add(callExpr.getCalledPolicyName());

    return new RoutePolicyStatementMatchCollector<>(this)
        .visitAll(
            arg.getSecond()
                .getRoutingPolicies()
                .get(callExpr.getCalledPolicyName())
                .getStatements(),
            arg);
  }

  @Override
  public Set<T> visitConjunction(Conjunction conjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(conjunction.getConjuncts(), arg);
  }

  @Override
  public Set<T> visitConjunctionChain(
      ConjunctionChain conjunctionChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public Set<T> visitDisjunction(Disjunction disjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(disjunction.getDisjuncts(), arg);
  }

  @Override
  public Set<T> visitFirstMatchChain(
      FirstMatchChain firstMatchChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(firstMatchChain.getSubroutines(), arg);
  }

  @Override
  public Set<T> visitTrackSucceeded(
      TrackSucceeded trackSucceeded, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitHasRoute(HasRoute hasRoute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchAsPath(MatchAsPath matchAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchBgpSessionType(
      MatchBgpSessionType matchBgpSessionType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchColor(MatchColor matchColor, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchCommunities(
      MatchCommunities matchCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchInterface(
      MatchInterface matchInterface, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchIpv4(MatchIpv4 matchIpv4, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchLocalPreference(
      MatchLocalPreference matchLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchMetric(MatchMetric matchMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchPrefixSet(
      MatchPrefixSet matchPrefixSet, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchProcessAsn(
      MatchProcessAsn matchProcessAsn, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchProtocol(
      MatchProtocol matchProtocol, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchRouteType(
      MatchRouteType matchRouteType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchSourceProtocol(
      MatchSourceProtocol matchSourceProtocol, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchSourceVrf(
      MatchSourceVrf matchSourceVrf, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchTag(MatchTag matchTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitNot(Not not, Tuple<Set<String>, Configuration> arg) {
    return not.getExpr().accept(this, arg);
  }

  @Override
  public Set<T> visitRouteIsClassful(
      RouteIsClassful routeIsClassful, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<T>builder()
        .addAll(withEnvironmentExpr.getExpr().accept(this, arg))
        .addAll(
            new RoutePolicyStatementMatchCollector<>(this)
                .visitAll(withEnvironmentExpr.getPreStatements(), arg))
        .addAll(
            new RoutePolicyStatementMatchCollector<>(this)
                .visitAll(withEnvironmentExpr.getPostStatements(), arg))
        .addAll(
            new RoutePolicyStatementMatchCollector<>(this)
                .visitAll(withEnvironmentExpr.getPostTrueStatements(), arg))
        .build();
  }

  private Set<T> visitAll(List<BooleanExpr> exprs, Tuple<Set<String>, Configuration> arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
