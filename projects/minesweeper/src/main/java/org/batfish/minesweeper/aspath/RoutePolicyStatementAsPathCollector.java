package org.batfish.minesweeper.aspath;

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
import org.batfish.minesweeper.SymbolicAsPathRegex;

/** Collect all AS-path regexes in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutePolicyStatementAsPathCollector
    implements StatementVisitor<Set<SymbolicAsPathRegex>, Configuration> {
  @Override
  public Set<SymbolicAsPathRegex> visitBufferedStatement(
      BufferedStatement bufferedStatement, Configuration arg) {
    return bufferedStatement.getStatement().accept(this, arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitCallStatement(
      CallStatement callStatement, Configuration arg) {
    // no need to check the callee here because we already execute this visitor on every statement
    // of every route policy (see Graph::findAsPathRegexes)
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitComment(Comment comment, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitIf(If if1, Configuration arg) {
    ImmutableSet.Builder<SymbolicAsPathRegex> builder = ImmutableSet.builder();
    return builder
        .addAll(if1.getGuard().accept(new BooleanExprAsPathCollector(), arg))
        .addAll(visitAll(if1.getTrueStatements(), arg))
        .addAll(visitAll(if1.getFalseStatements(), arg))
        .build();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitPrependAsPath(
      PrependAsPath prependAsPath, Configuration arg) {
    // if/when we update TransferBDD to support AS-path prepending, we will need to update this as
    // well
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitExcludeAsPath(
      ExcludeAsPath excludeAsPath, Configuration arg) {
    // if/when we update TransferBDD to support AS-path prepending, we will need to update this as
    // well
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetCommunities(
      SetCommunities setCommunities, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetDefaultPolicy(
      SetDefaultPolicy setDefaultPolicy, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetEigrpMetric(
      SetEigrpMetric setEigrpMetric, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetIsisLevel(SetIsisLevel setIsisLevel, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetIsisMetricType(
      SetIsisMetricType setIsisMetricType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetLocalPreference(
      SetLocalPreference setLocalPreference, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetMetric(SetMetric setMetric, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetNextHop(SetNextHop setNextHop, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetOrigin(SetOrigin setOrigin, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetOspfMetricType(
      SetOspfMetricType setOspfMetricType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetTag(SetTag setTag, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetDefaultTag(
      SetDefaultTag setDefaultTag, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetVarMetricType(
      SetVarMetricType setVarMetricType, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetWeight(SetWeight setWeight, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitStaticStatement(
      StaticStatement staticStatement, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitTraceableStatement(
      TraceableStatement traceableStatement, Configuration arg) {
    return visitAll(traceableStatement.getInnerStatements(), arg);
  }

  public Set<SymbolicAsPathRegex> visitAll(List<Statement> statements, Configuration arg) {
    return statements.stream()
        .flatMap(stmt -> stmt.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
