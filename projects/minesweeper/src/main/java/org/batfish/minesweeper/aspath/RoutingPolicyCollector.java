package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
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
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetDefaultTag;
import org.batfish.datamodel.routing_policy.statement.SetEigrpMetric;
import org.batfish.datamodel.routing_policy.statement.SetIsisLevel;
import org.batfish.datamodel.routing_policy.statement.SetIsisMetricType;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOriginatorIp;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.StatementVisitor;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collect a set of items in a {@link org.batfish.datamodel.routing_policy.RoutingPolicy} {@link
 * Statement}. This class automatically keeps track of the set of policies that have already been
 * visited, to prevent cycles when traversing called policies recursively.
 */
@ParametersAreNonnullByDefault
public class RoutingPolicyCollector<T>
    implements StatementVisitor<Set<T>, Tuple<Set<String>, Configuration>>,
        BooleanExprVisitor<Set<T>, Tuple<Set<String>, Configuration>> {

  @Override
  public Set<T> visitCallStatement(
      CallStatement callStatement, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callStatement.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and recurse.
    arg.getFirst().add(callStatement.getCalledPolicyName());

    return visitAll(
        arg.getSecond()
            .getRoutingPolicies()
            .get(callStatement.getCalledPolicyName())
            .getStatements(),
        arg);
  }

  @Override
  public Set<T> visitComment(Comment comment, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitIf(If if1, Tuple<Set<String>, Configuration> arg) {
    ImmutableSet.Builder<T> builder = ImmutableSet.builder();
    return builder
        .addAll(if1.getGuard().accept(this, arg))
        .addAll(visitAll(if1.getTrueStatements(), arg))
        .addAll(visitAll(if1.getFalseStatements(), arg))
        .build();
  }

  @Override
  public Set<T> visitPrependAsPath(
      PrependAsPath prependAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitReplaceAsesInAsSequence(ReplaceAsesInAsSequence replaceAsesInAsPathSequence) {
    // if/when we update TransferBDD to support AS-path replacing, we will need to update this as
    // well
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetOriginatorIp(
      SetOriginatorIp setOriginatorIp, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitExcludeAsPath(
      ExcludeAsPath excludeAsPath, Tuple<Set<String>, Configuration> arg) {
    // if/when TransferBDD gets updated to support AS-path excluding, this will have to be updated
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitRemoveTunnelEncapsulationAttribute(
      RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetDefaultPolicy(
      SetDefaultPolicy setDefaultPolicy, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetEigrpMetric(
      SetEigrpMetric setEigrpMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetIsisLevel(
      SetIsisLevel setIsisLevel, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetIsisMetricType(
      SetIsisMetricType setIsisMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetLocalPreference(
      SetLocalPreference setLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetMetric(SetMetric setMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetNextHop(SetNextHop setNextHop, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetOrigin(SetOrigin setOrigin, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetOspfMetricType(
      SetOspfMetricType setOspfMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetTag(SetTag setTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetDefaultTag(
      SetDefaultTag setDefaultTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelAttribute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetVarMetricType(
      SetVarMetricType setVarMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitSetWeight(SetWeight setWeight, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitStaticStatement(
      StaticStatement staticStatement, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitTraceableStatement(
      TraceableStatement traceableStatement, Tuple<Set<String>, Configuration> arg) {
    return visitAll(traceableStatement.getInnerStatements(), arg);
  }

  public Set<T> visitAll(List<Statement> statements, Tuple<Set<String>, Configuration> arg) {
    return statements.stream()
        .flatMap(stmt -> stmt.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

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

    RoutingPolicy routingPolicy =
        arg.getSecond().getRoutingPolicies().get(callExpr.getCalledPolicyName());
    if (routingPolicy == null) {
      return ImmutableSet.of();
    }
    return visitAll(routingPolicy.getStatements(), arg);
  }

  @Override
  public Set<T> visitConjunction(Conjunction conjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(this, conjunction.getConjuncts(), arg);
  }

  @Override
  public Set<T> visitConjunctionChain(
      ConjunctionChain conjunctionChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(this, conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public Set<T> visitDisjunction(Disjunction disjunction, Tuple<Set<String>, Configuration> arg) {
    return visitAll(this, disjunction.getDisjuncts(), arg);
  }

  @Override
  public Set<T> visitFirstMatchChain(
      FirstMatchChain firstMatchChain, Tuple<Set<String>, Configuration> arg) {
    return visitAll(this, firstMatchChain.getSubroutines(), arg);
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
  public Set<T> visitMatchOspfExternalType(
      org.batfish.datamodel.routing_policy.expr.MatchOspfExternalType matchOspfExternalType,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<T> visitMatchPeerAddress(
      MatchPeerAddress matchPeerAddress, Tuple<Set<String>, Configuration> arg) {
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
        .addAll(visitAll(withEnvironmentExpr.getPreStatements(), arg))
        .addAll(visitAll(withEnvironmentExpr.getPostStatements(), arg))
        .addAll(visitAll(withEnvironmentExpr.getPostTrueStatements(), arg))
        .build();
  }

  /**
   * A helper function to visit all elements of a list of boolean expressions.
   *
   * @param visitor the specific visitor to use
   * @param exprs the list of expressions
   * @param arg the argument that the visitor expects
   * @return a set containing the results of visiting each expression
   */
  public static <T> Set<T> visitAll(
      RoutingPolicyCollector<T> visitor,
      List<BooleanExpr> exprs,
      Tuple<Set<String>, Configuration> arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(visitor, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
