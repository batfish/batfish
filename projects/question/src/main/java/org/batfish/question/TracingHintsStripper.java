package org.batfish.question;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.TraceElement;
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

public final class TracingHintsStripper implements StatementVisitor<Statement, Void> {

  // one instance of this class is enough
  public static final TracingHintsStripper TRACING_HINTS_STRIPPER = new TracingHintsStripper();

  static final String STRIP_TOKEN = "__stripped__tracing__";

  private TracingHintsStripper() {}

  @Override
  public Statement visitBufferedStatement(BufferedStatement bufferedStatement, Void arg) {
    return new BufferedStatement(bufferedStatement.getStatement().accept(this, arg));
  }

  @Override
  public Statement visitCallStatement(CallStatement callStatement, Void arg) {
    return callStatement;
  }

  @Override
  public Statement visitComment(Comment comment, Void arg) {
    return comment;
  }

  @Override
  public Statement visitIf(If if1, Void arg) {
    return new If(
        if1.getComment(),
        if1.getGuard(), // assumes that guards do have tracing hints
        if1.getTrueStatements().stream()
            .map(st -> st.accept(this, arg))
            .collect(ImmutableList.toImmutableList()),
        if1.getFalseStatements().stream()
            .map(st -> st.accept(this, arg))
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public Statement visitPrependAsPath(PrependAsPath prependAsPath, Void arg) {
    return prependAsPath;
  }

  @Override
  public Statement visitExcludeAsPath(ExcludeAsPath excludeAsPath, Void arg) {
    return excludeAsPath;
  }

  @Override
  public Statement visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Void arg) {
    return setAdministrativeCost;
  }

  @Override
  public Statement visitSetCommunities(SetCommunities setCommunities, Void arg) {
    return setCommunities;
  }

  @Override
  public Statement visitSetDefaultPolicy(SetDefaultPolicy setDefaultPolicy, Void arg) {
    return setDefaultPolicy;
  }

  @Override
  public Statement visitSetEigrpMetric(SetEigrpMetric setEigrpMetric, Void arg) {
    return setEigrpMetric;
  }

  @Override
  public Statement visitSetIsisLevel(SetIsisLevel setIsisLevel, Void arg) {
    return setIsisLevel;
  }

  @Override
  public Statement visitSetIsisMetricType(SetIsisMetricType setIsisMetricType, Void arg) {
    return setIsisMetricType;
  }

  @Override
  public Statement visitSetLocalPreference(SetLocalPreference setLocalPreference, Void arg) {
    return setLocalPreference;
  }

  @Override
  public Statement visitSetMetric(SetMetric setMetric, Void arg) {
    return setMetric;
  }

  @Override
  public Statement visitSetNextHop(SetNextHop setNextHop, Void arg) {
    return setNextHop;
  }

  @Override
  public Statement visitSetOrigin(SetOrigin setOrigin, Void arg) {
    return setOrigin;
  }

  @Override
  public Statement visitSetOspfMetricType(SetOspfMetricType setOspfMetricType, Void arg) {
    return setOspfMetricType;
  }

  @Override
  public Statement visitSetTag(SetTag setTag, Void arg) {
    return setTag;
  }

  @Override
  public Statement visitSetDefaultTag(SetDefaultTag setDefaultTag, Void arg) {
    return setDefaultTag;
  }

  @Override
  public Statement visitSetVarMetricType(SetVarMetricType setVarMetricType, Void arg) {
    return setVarMetricType;
  }

  @Override
  public Statement visitSetWeight(SetWeight setWeight, Void arg) {
    return setWeight;
  }

  @Override
  public Statement visitStaticStatement(StaticStatement staticStatement, Void arg) {
    return staticStatement;
  }

  @Override
  public Statement visitTraceableStatement(TraceableStatement traceableStatement, Void arg) {
    return new TraceableStatement(
        TraceElement.of(STRIP_TOKEN),
        traceableStatement.getInnerStatements().stream()
            .map(st -> st.accept(this, arg))
            .collect(ImmutableList.toImmutableList()));
  }
}
