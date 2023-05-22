package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExprVisitor;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
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
 * Collect a set of items in a route-policy {@link Statement} that only appear as part of boolean
 * expressions. A {@link BooleanExprVisitor} is provided in order to find these items. We keep track
 * of the set of policies that have already been visited, to prevent cycles when traversing called
 * policies recursively.
 */
@ParametersAreNonnullByDefault
public class RoutePolicyStatementMatchCollector<T>
    implements StatementVisitor<Set<T>, Tuple<Set<String>, Configuration>> {

  private final BooleanExprVisitor<Set<T>, Tuple<Set<String>, Configuration>> _booleanExprVisitor;

  public RoutePolicyStatementMatchCollector(
      BooleanExprVisitor<Set<T>, Tuple<Set<String>, Configuration>> booleanExprVisitor) {
    _booleanExprVisitor = booleanExprVisitor;
  }

  @Override
  public Set<T> visitBufferedStatement(
      BufferedStatement bufferedStatement, Tuple<Set<String>, Configuration> arg) {
    return bufferedStatement.getStatement().accept(this, arg);
  }

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
        .addAll(if1.getGuard().accept(_booleanExprVisitor, arg))
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
}
