package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;

/** A visitor of {@link Statement} that takes 1 generic argument and returns a generic value. */
public interface StatementVisitor<T, U> {

  T visitAddCommunity(AddCommunity addCommunity, U arg);

  T visitBufferedStatement(BufferedStatement bufferedStatement, U arg);

  T visitCallStatement(CallStatement callStatement, U arg);

  T visitComment(Comment comment, U arg);

  T visitDeleteCommunity(DeleteCommunity deleteCommunity, U arg);

  T visitIf(If if1, U arg);

  T visitPrependAsPath(PrependAsPath prependAsPath, U arg);

  T visitRetainCommunity(RetainCommunity retainCommunity, U arg);

  T visitSetAdministrativeCost(SetAdministrativeCost setAdministrativeCost, U arg);

  T visitSetCommunities(SetCommunities setCommunities, U arg);

  T visitSetCommunity(SetCommunity setCommunity, U arg);

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

  T visitSetVarMetricType(SetVarMetricType setVarMetricType, U arg);

  T visitSetWeight(SetWeight setWeight, U arg);

  T visitStaticStatement(StaticStatement staticStatement, U arg);
}
