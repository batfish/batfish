package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
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
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community literals and regexes in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutePolicyStatementVarCollector
    implements StatementVisitor<Set<CommunityVar>, Tuple<Set<String>, Configuration>> {

  @Override
  public Set<CommunityVar> visitCallStatement(
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
  public Set<CommunityVar> visitComment(Comment comment, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitIf(If if1, Tuple<Set<String>, Configuration> arg) {
    ImmutableSet.Builder<CommunityVar> builder = ImmutableSet.builder();
    return builder
        .addAll(if1.getGuard().accept(new BooleanExprVarCollector(), arg))
        .addAll(visitAll(if1.getTrueStatements(), arg))
        .addAll(visitAll(if1.getFalseStatements(), arg))
        .build();
  }

  @Override
  public Set<CommunityVar> visitPrependAsPath(
      PrependAsPath prependAsPath, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitReplaceAsesInAsSequence(
      ReplaceAsesInAsSequence replaceAsesInAsPathSequence) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetOriginatorIp(
      SetOriginatorIp setOriginatorIp, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitExcludeAsPath(
      ExcludeAsPath excludeAsPath, Tuple<Set<String>, Configuration> arg) {
    // if/when TransferBDD gets updated to support AS-path excluding, this will have to be updated
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitRemoveTunnelEncapsulationAttribute(
      RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return setCommunities
        .getCommunitySetExpr()
        .accept(new CommunitySetExprVarCollector(), arg.getSecond());
  }

  @Override
  public Set<CommunityVar> visitSetDefaultPolicy(
      SetDefaultPolicy setDefaultPolicy, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetEigrpMetric(
      SetEigrpMetric setEigrpMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetIsisLevel(
      SetIsisLevel setIsisLevel, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetIsisMetricType(
      SetIsisMetricType setIsisMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetLocalPreference(
      SetLocalPreference setLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetMetric(
      SetMetric setMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetNextHop(
      SetNextHop setNextHop, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetOrigin(
      SetOrigin setOrigin, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetOspfMetricType(
      SetOspfMetricType setOspfMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetTag(SetTag setTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetDefaultTag(
      SetDefaultTag setDefaultTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelAttribute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetVarMetricType(
      SetVarMetricType setVarMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitSetWeight(
      SetWeight setWeight, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitStaticStatement(
      StaticStatement staticStatement, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<CommunityVar> visitTraceableStatement(
      TraceableStatement traceableStatement, Tuple<Set<String>, Configuration> arg) {
    return visitAll(traceableStatement.getInnerStatements(), arg);
  }

  public Set<CommunityVar> visitAll(
      List<Statement> statements, Tuple<Set<String>, Configuration> arg) {
    return statements.stream()
        .flatMap(stmt -> stmt.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
