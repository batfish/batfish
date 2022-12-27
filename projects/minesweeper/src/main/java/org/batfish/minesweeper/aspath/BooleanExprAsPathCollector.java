package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
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
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RibIntersectsPrefixSpace;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all AS-path regexes in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprAsPathCollector
    implements BooleanExprVisitor<Set<SymbolicAsPathRegex>, Tuple<Set<String>, Configuration>> {

  @Override
  public Set<SymbolicAsPathRegex> visitMatchClusterListLength(
      MatchClusterListLength matchClusterListLength, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitBooleanExprs(
      StaticBooleanExpr staticBooleanExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitCallExpr(
      CallExpr callExpr, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callExpr.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and recurse.
    Set<String> newSeen = new HashSet<>(arg.getFirst());
    newSeen.add(callExpr.getCalledPolicyName());

    return new RoutePolicyStatementAsPathCollector()
        .visitAll(
            arg.getSecond()
                .getRoutingPolicies()
                .get(callExpr.getCalledPolicyName())
                .getStatements(),
            new Tuple<>(newSeen, arg.getSecond()));
  }

  @Override
  public Set<SymbolicAsPathRegex> visitConjunction(
      Conjunction conjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(conjunction.getConjuncts(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitConjunctionChain(
      ConjunctionChain conjunctionChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitDisjunction(
      Disjunction disjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(disjunction.getDisjuncts(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitFirstMatchChain(
      FirstMatchChain firstMatchChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(firstMatchChain.getSubroutines(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitRibIntersectsPrefixSpace(
      RibIntersectsPrefixSpace ribIntersectsPrefixSpace, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitHasRoute(
      HasRoute hasRoute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchAsPath(
      MatchAsPath matchAsPath, Tuple<Set<String>, Configuration> arg) {
    AsPathMatchExpr matchExpr = matchAsPath.getAsPathMatchExpr();
    return matchExpr.accept(new AsPathMatchExprAsPathCollector(), arg.getSecond());
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchBgpSessionType(
      MatchBgpSessionType matchBgpSessionType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Tuple<Set<String>, Configuration> arg) {
    AsPathSetExpr expr = legacyMatchAsPath.getExpr();
    if (expr instanceof NamedAsPathSet) {
      NamedAsPathSet namedSet = (NamedAsPathSet) expr;
      AsPathAccessList list = arg.getSecond().getAsPathAccessLists().get(namedSet.getName());
      // conversion to VI should guarantee list is not null
      assert list != null;
      return list.getLines().stream()
          .map(AsPathAccessListLine::getRegex)
          .map(SymbolicAsPathRegex::new)
          .collect(ImmutableSet.toImmutableSet());
    } else {
      return ImmutableSet.of();
    }
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchColor(
      MatchColor matchColor, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchCommunities(
      MatchCommunities matchCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchInterface(
      MatchInterface matchInterface, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchIpv4(
      MatchIpv4 matchIpv4, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLocalPreference(
      MatchLocalPreference matchLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchMetric(
      MatchMetric matchMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchPrefixSet(
      MatchPrefixSet matchPrefixSet, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchProcessAsn(
      MatchProcessAsn matchProcessAsn, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchProtocol(
      MatchProtocol matchProtocol, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchRouteType(
      MatchRouteType matchRouteType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchSourceProtocol(
      MatchSourceProtocol matchSourceProtocol, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchSourceVrf(
      MatchSourceVrf matchSourceVrf, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchTag(
      MatchTag matchTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitNot(Not not, Tuple<Set<String>, Configuration> arg) {
    return not.getExpr().accept(this, arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitRouteIsClassful(
      RouteIsClassful routeIsClassful, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<SymbolicAsPathRegex>builder()
        .addAll(withEnvironmentExpr.getExpr().accept(this, arg))
        .addAll(
            new RoutePolicyStatementAsPathCollector()
                .visitAll(withEnvironmentExpr.getPreStatements(), arg))
        .addAll(
            new RoutePolicyStatementAsPathCollector()
                .visitAll(withEnvironmentExpr.getPostStatements(), arg))
        .addAll(
            new RoutePolicyStatementAsPathCollector()
                .visitAll(withEnvironmentExpr.getPostTrueStatements(), arg))
        .build();
  }

  private Set<SymbolicAsPathRegex> visitAll(
      List<BooleanExpr> exprs, Tuple<Set<String>, Configuration> arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
