package org.batfish.datamodel.routing_policy;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
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
import org.batfish.datamodel.routing_policy.statement.StatementVisitor;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;

/**
 * Provides functionality to verify absence of undefined references to routing policies in {@link
 * CallExpr} and {@link CallStatement}.
 *
 * <p>It is the responsibility of conversion code to handle undefined references to vendor-specific
 * structures. Conversion code must not propagate undefined references to vendor-independent
 * structures. Thus any such references to vendor-independent structures signify errors in
 * conversion code, rather than the input configuration text.
 */
public final class RoutingPolicyReferencesVerifier {

  /**
   * Verifies absence of undefined references to routing policies within the input {@code
   * configuration}.
   *
   * @throws VendorConversionException if an undefined reference to a routing policy is found in
   *     {@code configuration}.
   */
  public static void verify(Configuration configuration) {
    new RoutingPolicyReferencesVerifier(configuration).verify();
  }

  private static final class RoutingPolicyReferencesBooleanExprVerifier
      implements BooleanExprVisitor<Void, RoutingPolicyReferencesVerifierContext> {

    @Override
    public Void visitBooleanExprs(
        StaticBooleanExpr staticBooleanExpr, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchClusterListLength(
        MatchClusterListLength matchClusterListLength, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCallExpr(CallExpr callExpr, RoutingPolicyReferencesVerifierContext arg) {
      String name = callExpr.getCalledPolicyName();
      if (!arg._routingPolicies.containsKey(name)) {
        throw new VendorConversionException(
            String.format("Undefined reference to routing policy in CallExpr: '%s'", name));
      }
      return null;
    }

    @Override
    public Void visitConjunction(
        Conjunction conjunction, RoutingPolicyReferencesVerifierContext arg) {
      conjunction.getConjuncts().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitConjunctionChain(
        ConjunctionChain conjunctionChain, RoutingPolicyReferencesVerifierContext arg) {
      conjunctionChain.getSubroutines().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitDisjunction(
        Disjunction disjunction, RoutingPolicyReferencesVerifierContext arg) {
      disjunction.getDisjuncts().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitFirstMatchChain(
        FirstMatchChain firstMatchChain, RoutingPolicyReferencesVerifierContext arg) {
      firstMatchChain.getSubroutines().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitTrackSucceeded(
        TrackSucceeded trackSucceeded, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitHasRoute(HasRoute hasRoute, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchAsPath(
        org.batfish.datamodel.routing_policy.as_path.MatchAsPath matchAsPath,
        RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLegacyAsPath(
        LegacyMatchAsPath legacyMatchAsPath, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchBgpSessionType(
        MatchBgpSessionType matchBgpSessionType, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchColor(MatchColor matchColor, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchCommunities(
        org.batfish.datamodel.routing_policy.communities.MatchCommunities matchCommunities,
        RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchInterface(
        MatchInterface matchInterface, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchIpv4(MatchIpv4 matchIpv4, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLocalPreference(
        MatchLocalPreference matchLocalPreference, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLocalRouteSourcePrefixLength(
        MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength,
        RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchMetric(
        MatchMetric matchMetric, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchPeerAddress(
        MatchPeerAddress matchPeerAddress, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchPrefixSet(
        MatchPrefixSet matchPrefixSet, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchProcessAsn(
        MatchProcessAsn matchProcessAsn, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchProtocol(
        MatchProtocol matchProtocol, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchRouteType(
        MatchRouteType matchRouteType, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchSourceProtocol(
        MatchSourceProtocol matchSourceProtocol, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchSourceVrf(
        MatchSourceVrf matchSourceVrf, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchTag(MatchTag matchTag, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitNot(Not not, RoutingPolicyReferencesVerifierContext arg) {
      return not.getExpr().accept(this, arg);
    }

    @Override
    public Void visitRouteIsClassful(
        RouteIsClassful routeIsClassful, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitWithEnvironmentExpr(
        WithEnvironmentExpr withEnvironmentExpr, RoutingPolicyReferencesVerifierContext arg) {
      withEnvironmentExpr.getExpr().accept(this, arg);
      withEnvironmentExpr.getPostStatements().stream()
          .forEach(s -> s.accept(STATEMENT_VERIFIER, arg));
      withEnvironmentExpr.getPostTrueStatements().stream()
          .forEach(s -> s.accept(STATEMENT_VERIFIER, arg));
      withEnvironmentExpr.getPreStatements().stream()
          .forEach(s -> s.accept(STATEMENT_VERIFIER, arg));
      return null;
    }
  }

  private static final class RoutingPolicyReferencesStatementVerifier
      implements StatementVisitor<Void, RoutingPolicyReferencesVerifierContext> {

    @Override
    public Void visitCallStatement(
        CallStatement callStatement, RoutingPolicyReferencesVerifierContext arg) {
      String name = callStatement.getCalledPolicyName();
      if (!arg._routingPolicies.containsKey(name)) {
        throw new VendorConversionException(
            String.format("Undefined reference to routing policy in CallStatement: '%s'", name));
      }
      return null;
    }

    @Override
    public Void visitComment(Comment comment, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitIf(If if1, RoutingPolicyReferencesVerifierContext arg) {
      if1.getFalseStatements().stream().forEach(s -> s.accept(this, arg));
      if1.getTrueStatements().stream().forEach(s -> s.accept(this, arg));
      if1.getGuard().accept(BOOLEAN_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitReplaceAsesInAsSequence(ReplaceAsesInAsSequence replaceAsesInAsPathSequence) {
      return null;
    }

    @Override
    public Void visitSetOriginatorIp(
        SetOriginatorIp setOriginatorIp, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitPrependAsPath(
        PrependAsPath prependAsPath, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitExcludeAsPath(
        ExcludeAsPath excludeAsPath, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitRemoveTunnelEncapsulationAttribute(
        RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
        RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetAdministrativeCost(
        SetAdministrativeCost setAdministrativeCost, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetCommunities(
        org.batfish.datamodel.routing_policy.communities.SetCommunities setCommunities,
        RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetDefaultPolicy(
        SetDefaultPolicy setDefaultPolicy, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetEigrpMetric(
        SetEigrpMetric setEigrpMetric, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetIsisLevel(
        SetIsisLevel setIsisLevel, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetIsisMetricType(
        SetIsisMetricType setIsisMetricType, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetLocalPreference(
        SetLocalPreference setLocalPreference, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetMetric(SetMetric setMetric, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetNextHop(SetNextHop setNextHop, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetOrigin(SetOrigin setOrigin, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetOspfMetricType(
        SetOspfMetricType setOspfMetricType, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetTag(SetTag setTag, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetDefaultTag(
        SetDefaultTag setDefaultTag, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetTunnelEncapsulationAttribute(
        SetTunnelEncapsulationAttribute setTunnelEncapsulationAttribute,
        RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetVarMetricType(
        SetVarMetricType setVarMetricType, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetWeight(SetWeight setWeight, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitStaticStatement(
        StaticStatement staticStatement, RoutingPolicyReferencesVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitTraceableStatement(
        TraceableStatement traceableStatement, RoutingPolicyReferencesVerifierContext arg) {
      traceableStatement.getInnerStatements().stream().forEach(s -> s.accept(this, arg));
      return null;
    }
  }

  @VisibleForTesting
  static final class RoutingPolicyReferencesVerifierContext {
    @VisibleForTesting
    static @Nonnull RoutingPolicyReferencesVerifierContext fromConfiguration(Configuration c) {
      return new RoutingPolicyReferencesVerifierContext(c.getRoutingPolicies());
    }

    private RoutingPolicyReferencesVerifierContext(Map<String, RoutingPolicy> routingPolicies) {
      _routingPolicies = routingPolicies;
    }

    private final @Nonnull Map<String, RoutingPolicy> _routingPolicies;
  }

  @VisibleForTesting
  static final RoutingPolicyReferencesBooleanExprVerifier BOOLEAN_EXPR_VERIFIER =
      new RoutingPolicyReferencesBooleanExprVerifier();

  @VisibleForTesting
  static final RoutingPolicyReferencesStatementVerifier STATEMENT_VERIFIER =
      new RoutingPolicyReferencesStatementVerifier();

  private final RoutingPolicyReferencesVerifierContext _ctx;

  @VisibleForTesting
  RoutingPolicyReferencesVerifier(Configuration c) {
    _ctx = RoutingPolicyReferencesVerifierContext.fromConfiguration(c);
  }

  @VisibleForTesting
  void verify() {
    verifyRoutingPolicies();
  }

  @VisibleForTesting
  void verifyRoutingPolicies() {
    _ctx._routingPolicies.values().stream()
        .flatMap(rp -> rp.getStatements().stream())
        .forEach(s -> s.accept(STATEMENT_VERIFIER, _ctx));
  }
}
