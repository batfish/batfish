package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;

/** A visitor of {@link Statement} that takes 1 generic argument and returns a generic value. */
public interface StatementVisitor<T, U> {

  T visitCallStatement(CallStatement callStatement, U arg);

  T visitComment(Comment comment, U arg);

  T visitIf(If if1, U arg);

  T visitPrependAsPath(PrependAsPath prependAsPath, U arg);

  T visitExcludeAsPath(ExcludeAsPath excludeAsPath, U arg);

  T visitRemoveTunnelEncapsulationAttribute(
      RemoveTunnelEncapsulationAttribute removeTunnelEncapsulationAttribute, U arg);

  T visitSetAdministrativeCost(SetAdministrativeCost setAdministrativeCost, U arg);

  T visitSetCommunities(SetCommunities setCommunities, U arg);

  T visitSetDefaultPolicy(SetDefaultPolicy setDefaultPolicy, U arg);

  T visitSetEigrpMetric(SetEigrpMetric setEigrpMetric, U arg);

  T visitSetIsisLevel(SetIsisLevel setIsisLevel, U arg);

  T visitSetIsisMetricType(SetIsisMetricType setIsisMetricType, U arg);

  T visitSetLocalPreference(SetLocalPreference setLocalPreference, U arg);

  T visitSetMetric(SetMetric setMetric, U arg);

  T visitSetNextHop(SetNextHop setNextHop, U arg);

  T visitSetOrigin(SetOrigin setOrigin, U arg);

  T visitSetOspfMetricType(SetOspfMetricType setOspfMetricType, U arg);

  T visitSetTag(SetTag setTag, U arg);

  T visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelEncapsulationAttribute, U arg);

  T visitSetDefaultTag(SetDefaultTag setDefaultTag, U arg);

  T visitSetVarMetricType(SetVarMetricType setVarMetricType, U arg);

  T visitSetWeight(SetWeight setWeight, U arg);

  T visitStaticStatement(StaticStatement staticStatement, U arg);

  T visitTraceableStatement(TraceableStatement traceableStatement, U arg);

  T visitReplaceAsesInAsSequence(ReplaceAsesInAsSequence replaceAsesInAsPathSequence);

  T visitSetOriginatorIp(SetOriginatorIp setOriginatorIp, U arg);
}
