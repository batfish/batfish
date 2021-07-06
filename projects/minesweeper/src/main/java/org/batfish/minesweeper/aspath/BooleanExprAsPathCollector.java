package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.*;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs.StaticBooleanExpr;
import org.batfish.minesweeper.SymbolicAsPathRegex;

/** Collect all AS-path regexes in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprAsPathCollector
    implements BooleanExprVisitor<Set<SymbolicAsPathRegex>, Configuration> {
  @Override
  public Set<SymbolicAsPathRegex> visitBooleanExprs(
      StaticBooleanExpr staticBooleanExpr, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitCallExpr(CallExpr callExpr, Configuration arg) {
    /* we already visit all route policies in a configuration (see Graph::findAsPathRegexes), so no
    need to recurse to the callee policy */
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitConjunction(Conjunction conjunction, Configuration arg) {
    return visitAll(conjunction.getConjuncts(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitConjunctionChain(
      ConjunctionChain conjunctionChain, Configuration arg) {
    return visitAll(conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitDisjunction(Disjunction disjunction, Configuration arg) {
    return visitAll(disjunction.getDisjuncts(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitFirstMatchChain(
      FirstMatchChain firstMatchChain, Configuration arg) {
    return visitAll(firstMatchChain.getSubroutines(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitRibIntersectsPrefixSpace(
      RibIntersectsPrefixSpace ribIntersectsPrefixSpace, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitHasRoute(HasRoute hasRoute, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitHasRoute6(HasRoute6 hasRoute6, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchAsPath(MatchAsPath matchAsPath, Configuration arg) {
    AsPathMatchExpr matchExpr = matchAsPath.getAsPathMatchExpr();
    return matchExpr.accept(new AsPathMatchExprAsPathCollector(), arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Configuration arg) {
    AsPathSetExpr expr = legacyMatchAsPath.getExpr();
    if (expr instanceof ExplicitAsPathSet) {
      ExplicitAsPathSet explicitAsPathSet = (ExplicitAsPathSet) expr;
      return explicitAsPathSet.getElems().stream()
          .map(AsPathSetElem::regex)
          .map(SymbolicAsPathRegex::new)
          .collect(ImmutableSet.toImmutableSet());
    } else if (expr instanceof NamedAsPathSet) {
      NamedAsPathSet namedSet = (NamedAsPathSet) expr;
      // collect up all regexes in the AsPathAccessList of the specified name
      AsPathAccessList list = arg.getAsPathAccessLists().get(namedSet.getName());
      return list.getLines().stream()
          .map(AsPathAccessListLine::getRegex)
          .map(SymbolicAsPathRegex::new)
          .collect(ImmutableSet.toImmutableSet());
    } else {
      return ImmutableSet.of();
    }
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchColor(MatchColor matchColor, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchCommunities(
      MatchCommunities matchCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchIp6AccessList(
      MatchIp6AccessList matchIp6AccessList, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchIpv4(MatchIpv4 matchIpv4, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchIpv6(MatchIpv6 matchIpv6, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLocalPreference(
      MatchLocalPreference matchLocalPreference, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchMetric(MatchMetric matchMetric, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchPrefix6Set(
      MatchPrefix6Set matchPrefix6Set, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchPrefixSet(
      MatchPrefixSet matchPrefixSet, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchProcessAsn(
      MatchProcessAsn matchProcessAsn, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchProtocol(
      MatchProtocol matchProtocol, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchRouteType(
      MatchRouteType matchRouteType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchSourceVrf(
      MatchSourceVrf matchSourceVrf, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchTag(MatchTag matchTag, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitNeighborIsAsPath(
      NeighborIsAsPath neighborIsAsPath, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitNot(Not not, Configuration arg) {
    return not.getExpr().accept(this, arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitOriginatesFromAsPath(
      OriginatesFromAsPath originatesFromAsPath, Configuration arg) {
    // if/when OriginatesFromAsPath is supported by TransferBDD this will need to be updated
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitPassesThroughAsPath(
      PassesThroughAsPath passesThroughAsPath, Configuration arg) {
    // if/when OriginatesFromAsPath is supported by TransferBDD this will need to be updated
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitRouteIsClassful(
      RouteIsClassful routeIsClassful, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Configuration arg) {
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

  private Set<SymbolicAsPathRegex> visitAll(List<BooleanExpr> exprs, Configuration arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
