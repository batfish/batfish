package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
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

/** Collect all community-list names in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutingPolicyStatementCommunityCollector
    implements StatementVisitor<Set<String>, Tuple<Set<String>, Configuration>> {
  @Override
  public Set<String> visitBufferedStatement(
      BufferedStatement bufferedStatement, Tuple<Set<String>, Configuration> arg) {
    return bufferedStatement.getStatement().accept(this, arg);
  }

  @Override
  public Set<String> visitCallStatement(
      CallStatement callStatement, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callStatement.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and continue.
    arg.getFirst().add(callStatement.getCalledPolicyName());

    return visitAll(
        arg.getSecond()
            .getRoutingPolicies()
            .get(callStatement.getCalledPolicyName())
            .getStatements(),
        arg);
  }

  @Override
  public Set<String> visitComment(Comment comment, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitIf(If if1, Tuple<Set<String>, Configuration> arg) {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    return builder
        .addAll(if1.getGuard().accept(new CommunityBooleanExprCollector(), arg))
        .addAll(visitAll(if1.getTrueStatements(), arg))
        .addAll(visitAll(if1.getFalseStatements(), arg))
        .build();
  }

  @Override
  public Set<String> visitPrependAsPath(
      PrependAsPath prependAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitReplaceAsesInAsSequence(
      ReplaceAsesInAsSequence replaceAsesInAsPathSequence) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitExcludeAsPath(
      ExcludeAsPath excludeAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitRemoveTunnelEncapsulationAttribute(
      RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return setCommunities.getCommunitySetExpr().accept(new CommunitySetExprCollector(), arg);
  }

  @Override
  public Set<String> visitSetDefaultPolicy(
      SetDefaultPolicy setDefaultPolicy, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetEigrpMetric(
      SetEigrpMetric setEigrpMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetIsisLevel(
      SetIsisLevel setIsisLevel, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetIsisMetricType(
      SetIsisMetricType setIsisMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetLocalPreference(
      SetLocalPreference setLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetMetric(SetMetric setMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetNextHop(SetNextHop setNextHop, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetOrigin(SetOrigin setOrigin, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetOspfMetricType(
      SetOspfMetricType setOspfMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetTag(SetTag setTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetDefaultTag(
      SetDefaultTag setDefaultTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelAttribute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetVarMetricType(
      SetVarMetricType setVarMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitSetWeight(SetWeight setWeight, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitStaticStatement(
      StaticStatement staticStatement, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitTraceableStatement(
      TraceableStatement traceableStatement, Tuple<Set<String>, Configuration> arg) {
    return visitAll(traceableStatement.getInnerStatements(), arg);
  }

  public Set<String> visitAll(List<Statement> statements, Tuple<Set<String>, Configuration> arg) {
    return statements.stream()
        .flatMap(stmt -> stmt.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
