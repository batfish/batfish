package org.batfish.minesweeper.communities;

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
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.StatementVisitor;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.CommunityVar;

/** Collect all community literals and regexes in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutePolicyStatementVarCollector
    implements StatementVisitor<Set<CommunityVar>, Configuration> {
  @Override
  public Set<CommunityVar> visitBufferedStatement(
      BufferedStatement bufferedStatement, Configuration arg) {
    return bufferedStatement.getStatement().accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitCallStatement(CallStatement callStatement, Configuration arg) {
    // no need to check the callee here because we already execute this visitor on every statement
    // of every route policy (see Graph::findAllCommunities)
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitComment(Comment comment, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitIf(If if1, Configuration arg) {
    ImmutableSet.Builder<CommunityVar> builder = ImmutableSet.builder();
    return builder
        .addAll(if1.getGuard().accept(new BooleanExprVarCollector(), arg))
        .addAll(visitAll(if1.getTrueStatements(), arg))
        .addAll(visitAll(if1.getFalseStatements(), arg))
        .build();
  }

  @Override
  public Set<CommunityVar> visitPrependAsPath(PrependAsPath prependAsPath, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitExcludeAsPath(ExcludeAsPath excludeAsPath, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetCommunities(SetCommunities setCommunities, Configuration arg) {
    return setCommunities.getCommunitySetExpr().accept(new CommunitySetExprVarCollector(), arg);
  }

  @Override
  public Set<CommunityVar> visitSetDefaultPolicy(
      SetDefaultPolicy setDefaultPolicy, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetEigrpMetric(SetEigrpMetric setEigrpMetric, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetIsisLevel(SetIsisLevel setIsisLevel, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetIsisMetricType(
      SetIsisMetricType setIsisMetricType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetLocalPreference(
      SetLocalPreference setLocalPreference, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetMetric(SetMetric setMetric, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetNextHop(SetNextHop setNextHop, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetOrigin(SetOrigin setOrigin, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetOspfMetricType(
      SetOspfMetricType setOspfMetricType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetTag(SetTag setTag, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetDefaultTag(SetDefaultTag setDefaultTag, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetVarMetricType(
      SetVarMetricType setVarMetricType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetWeight(SetWeight setWeight, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitStaticStatement(
      StaticStatement staticStatement, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitTraceableStatement(
      TraceableStatement traceableStatement, Configuration arg) {
    return visitAll(traceableStatement.getInnerStatements(), arg);
  }

  public Set<CommunityVar> visitAll(List<Statement> statements, Configuration arg) {
    return statements.stream()
        .flatMap(stmt -> stmt.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
