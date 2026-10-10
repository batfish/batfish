package org.batfish.datamodel.routing_policy;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
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

/**
 * Base visitor that recursively traverses routing-policy statements and boolean expressions.
 *
 * <p>Subclasses define an empty result and how to combine child results. They can override methods
 * for the nodes they handle and hooks for called or default policies. The default implementation
 * returns the empty result for leaf nodes.
 */
@ParametersAreNonnullByDefault
public abstract class RoutingPolicyTraverser<T, U>
    implements StatementVisitor<T, U>, BooleanExprVisitor<T, U> {

  /** Returns the result for a node with no relevant children. */
  protected abstract T defaultResult();

  /** Combines results from child nodes. */
  protected abstract T combineResults(Stream<T> results);

  /** Returns the result of traversing a called policy. */
  protected T visitCalledPolicy(String policyName, U arg) {
    return defaultResult();
  }

  /** Returns the result of traversing a default policy. */
  protected T visitDefaultPolicy(String policyName, U arg) {
    return defaultResult();
  }

  /** Traverses {@code statements}. */
  public final T visitStatements(List<Statement> statements, U arg) {
    return combineResults(statements.stream().map(statement -> statement.accept(this, arg)));
  }

  /** Traverses {@code expressions}. */
  public final T visitBooleanExpressions(List<BooleanExpr> expressions, U arg) {
    return combineResults(expressions.stream().map(expression -> expression.accept(this, arg)));
  }

  @Override
  public T visitCallStatement(CallStatement callStatement, U arg) {
    return visitCalledPolicy(callStatement.getCalledPolicyName(), arg);
  }

  @Override
  public T visitComment(Comment comment, U arg) {
    return defaultResult();
  }

  @Override
  public T visitIf(If ifStatement, U arg) {
    return combineResults(
        Stream.of(
            ifStatement.getGuard().accept(this, arg),
            visitStatements(ifStatement.getTrueStatements(), arg),
            visitStatements(ifStatement.getFalseStatements(), arg)));
  }

  @Override
  public T visitPrependAsPath(PrependAsPath prependAsPath, U arg) {
    return defaultResult();
  }

  @Override
  public T visitExcludeAsPath(ExcludeAsPath excludeAsPath, U arg) {
    return defaultResult();
  }

  @Override
  public T visitRemoveTunnelEncapsulationAttribute(
      RemoveTunnelEncapsulationAttribute removeTunnelEncapsulationAttribute, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetAdministrativeCost(SetAdministrativeCost setAdministrativeCost, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetCommunities(SetCommunities setCommunities, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetDefaultPolicy(SetDefaultPolicy setDefaultPolicy, U arg) {
    return visitDefaultPolicy(setDefaultPolicy.getDefaultPolicy(), arg);
  }

  @Override
  public T visitSetEigrpMetric(SetEigrpMetric setEigrpMetric, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetIsisLevel(SetIsisLevel setIsisLevel, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetIsisMetricType(SetIsisMetricType setIsisMetricType, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetLocalPreference(SetLocalPreference setLocalPreference, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetMetric(SetMetric setMetric, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetNextHop(SetNextHop setNextHop, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetOrigin(SetOrigin setOrigin, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetOspfMetricType(SetOspfMetricType setOspfMetricType, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetTag(SetTag setTag, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelEncapsulationAttribute, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetDefaultTag(SetDefaultTag setDefaultTag, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetVarMetricType(SetVarMetricType setVarMetricType, U arg) {
    return defaultResult();
  }

  @Override
  public T visitSetWeight(SetWeight setWeight, U arg) {
    return defaultResult();
  }

  @Override
  public T visitStaticStatement(StaticStatement staticStatement, U arg) {
    return defaultResult();
  }

  @Override
  public T visitTraceableStatement(TraceableStatement traceableStatement, U arg) {
    return visitStatements(traceableStatement.getInnerStatements(), arg);
  }

  @Override
  public T visitReplaceAsesInAsSequence(ReplaceAsesInAsSequence replaceAsesInAsSequence) {
    return defaultResult();
  }

  @Override
  public T visitSetOriginatorIp(SetOriginatorIp setOriginatorIp, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchClusterListLength(MatchClusterListLength matchClusterListLength, U arg) {
    return defaultResult();
  }

  @Override
  public T visitBooleanExprs(StaticBooleanExpr staticBooleanExpr, U arg) {
    return defaultResult();
  }

  @Override
  public T visitCallExpr(CallExpr callExpr, U arg) {
    return visitCalledPolicy(callExpr.getCalledPolicyName(), arg);
  }

  @Override
  public T visitConjunction(Conjunction conjunction, U arg) {
    return visitBooleanExpressions(conjunction.getConjuncts(), arg);
  }

  @Override
  public T visitConjunctionChain(ConjunctionChain conjunctionChain, U arg) {
    return visitBooleanExpressions(conjunctionChain.getSubroutines(), arg);
  }

  @Override
  public T visitDisjunction(Disjunction disjunction, U arg) {
    return visitBooleanExpressions(disjunction.getDisjuncts(), arg);
  }

  @Override
  public T visitFirstMatchChain(FirstMatchChain firstMatchChain, U arg) {
    return visitBooleanExpressions(firstMatchChain.getSubroutines(), arg);
  }

  @Override
  public T visitTrackSucceeded(TrackSucceeded trackSucceeded, U arg) {
    return defaultResult();
  }

  @Override
  public T visitHasRoute(HasRoute hasRoute, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchAsPath(MatchAsPath matchAsPath, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchBgpSessionType(MatchBgpSessionType matchBgpSessionType, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchLegacyAsPath(LegacyMatchAsPath legacyMatchAsPath, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchColor(MatchColor matchColor, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchCommunities(MatchCommunities matchCommunities, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchInterface(MatchInterface matchInterface, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchIpv4(MatchIpv4 matchIpv4, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchLocalPreference(MatchLocalPreference matchLocalPreference, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchMetric(MatchMetric matchMetric, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchPeerAddress(MatchPeerAddress matchPeerAddress, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchPrefixSet(MatchPrefixSet matchPrefixSet, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchProcessAsn(MatchProcessAsn matchProcessAsn, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchProtocol(MatchProtocol matchProtocol, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchRouteType(MatchRouteType matchRouteType, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchSourceProtocol(MatchSourceProtocol matchSourceProtocol, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchSourceVrf(MatchSourceVrf matchSourceVrf, U arg) {
    return defaultResult();
  }

  @Override
  public T visitMatchTag(MatchTag matchTag, U arg) {
    return defaultResult();
  }

  @Override
  public T visitNot(Not not, U arg) {
    return not.getExpr().accept(this, arg);
  }

  @Override
  public T visitRouteIsClassful(RouteIsClassful routeIsClassful, U arg) {
    return defaultResult();
  }

  @Override
  public T visitWithEnvironmentExpr(WithEnvironmentExpr withEnvironmentExpr, U arg) {
    return combineResults(
        Stream.of(
            withEnvironmentExpr.getExpr().accept(this, arg),
            visitStatements(withEnvironmentExpr.getPreStatements(), arg),
            visitStatements(withEnvironmentExpr.getPostStatements(), arg),
            visitStatements(withEnvironmentExpr.getPostTrueStatements(), arg)));
  }
}
