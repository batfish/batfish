package org.batfish.datamodel.routing_policy.as_path;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
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
 * Provides functionality to verify absence of undefined/cyclical references in as-path-related
 * structures.
 *
 * <p>It is the responsiblity of conversion code to handle undefined/cyclical references to
 * vendor-specific structures. Conversion code must not propagate undefined/cyclical references to
 * vendor-independent structures. Thus any such references to vendor-independent structures signify
 * errors in conversion code, rather than the input configuration text.
 */
public final class AsPathStructuresVerifier {

  /**
   * Verifies absence of undefined/cyclical references in as-path-related structures within the
   * input {@code configuration}.
   *
   * @throws VendorConversionException if an undefined or cyclical reference to a as-path-related
   *     structure is found in {@code configuration}.
   */
  public static void verify(Configuration configuration) {
    new AsPathStructuresVerifier(configuration).verify();
  }

  private static final class AsPathStructuresBooleanExprVerifier
      implements BooleanExprVisitor<Void, AsPathStructuresVerifierContext> {
    @Override
    public Void visitBooleanExprs(
        StaticBooleanExpr staticBooleanExpr, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchClusterListLength(
        MatchClusterListLength matchClusterListLength, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCallExpr(CallExpr callExpr, AsPathStructuresVerifierContext arg) {
      // No need to dereference and recurse, since all named routing policies will be visited
      // anyway.
      return null;
    }

    @Override
    public Void visitConjunction(Conjunction conjunction, AsPathStructuresVerifierContext arg) {
      conjunction.getConjuncts().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitConjunctionChain(
        ConjunctionChain conjunctionChain, AsPathStructuresVerifierContext arg) {
      conjunctionChain.getSubroutines().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitDisjunction(Disjunction disjunction, AsPathStructuresVerifierContext arg) {
      disjunction.getDisjuncts().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitFirstMatchChain(
        FirstMatchChain firstMatchChain, AsPathStructuresVerifierContext arg) {
      firstMatchChain.getSubroutines().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitTrackSucceeded(
        TrackSucceeded trackSucceeded, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitHasRoute(HasRoute hasRoute, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchAsPath(MatchAsPath matchAsPath, AsPathStructuresVerifierContext arg) {
      matchAsPath.getAsPathExpr().accept(AS_PATH_EXPR_VERIFIER, arg);
      matchAsPath.getAsPathMatchExpr().accept(AS_PATH_MATCH_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitMatchLegacyAsPath(
        LegacyMatchAsPath legacyMatchAsPath, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchBgpSessionType(
        MatchBgpSessionType matchBgpSessionType, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchColor(MatchColor matchColor, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchCommunities(
        MatchCommunities matchCommunities, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchInterface(
        MatchInterface matchInterface, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchIpv4(MatchIpv4 matchIpv4, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLocalPreference(
        MatchLocalPreference matchLocalPreference, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLocalRouteSourcePrefixLength(
        MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength,
        AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchMetric(MatchMetric matchMetric, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchPeerAddress(
        MatchPeerAddress matchPeerAddress, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchPrefixSet(
        MatchPrefixSet matchPrefixSet, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchProcessAsn(
        MatchProcessAsn matchProcessAsn, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchProtocol(
        MatchProtocol matchProtocol, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchRouteType(
        MatchRouteType matchRouteType, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchSourceProtocol(
        MatchSourceProtocol matchSourceProtocol, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchSourceVrf(
        MatchSourceVrf matchSourceVrf, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchTag(MatchTag matchTag, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitNot(Not not, AsPathStructuresVerifierContext arg) {
      return not.getExpr().accept(this, arg);
    }

    @Override
    public Void visitRouteIsClassful(
        RouteIsClassful routeIsClassful, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitWithEnvironmentExpr(
        WithEnvironmentExpr withEnvironmentExpr, AsPathStructuresVerifierContext arg) {
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

  private static final class AsPathMatchExprVerifier
      implements AsPathMatchExprVisitor<Void, AsPathStructuresVerifierContext> {

    @Override
    public Void visitAsPathMatchAny(
        AsPathMatchAny asPathMatchAny, AsPathStructuresVerifierContext arg) {
      asPathMatchAny.getDisjuncts().forEach(expr -> expr.accept(AS_PATH_MATCH_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitAsPathMatchExprReference(
        AsPathMatchExprReference asPathMatchExprReference, AsPathStructuresVerifierContext arg) {
      String name = asPathMatchExprReference.getName();
      if (arg._verifiedAsPathMatchExprReferences.contains(name)) {
        return null;
      }
      if (arg._asPathMatchExprReferenceStack.contains(name)) {
        // circular reference
        throw new VendorConversionException(
            String.format("Circular reference to AsPathMatchExpr: '%s'", name));
      }
      arg._asPathMatchExprReferenceStack.add(name);
      AsPathMatchExpr resolved = arg._asPathMatchExprs.get(name);
      if (resolved == null) {
        // undefined reference
        throw new VendorConversionException(
            String.format("Undefined reference to AsPathMatchExpr: '%s'", name));
      }
      resolved.accept(AS_PATH_MATCH_EXPR_VERIFIER, arg);
      arg._verifiedAsPathMatchExprReferences.add(name);
      arg._asPathMatchExprReferenceStack.remove(name);
      return null;
    }

    @Override
    public Void visitAsPathMatchRegex(
        AsPathMatchRegex asPathMatchRegex, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitAsSetsMatchingRanges(
        AsSetsMatchingRanges asSetsMatchingRanges, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitHasAsPathLength(
        HasAsPathLength hasAsPathLength, AsPathStructuresVerifierContext arg) {
      return null;
    }
  }

  private static final class AsPathExprVerifier
      implements AsPathExprVisitor<Void, AsPathStructuresVerifierContext> {

    @Override
    public Void visitAsPathExprReference(
        AsPathExprReference asPathExprReference, AsPathStructuresVerifierContext arg) {
      String name = asPathExprReference.getName();
      if (arg._verifiedAsPathExprReferences.contains(name)) {
        return null;
      }
      if (arg._asPathExprReferenceStack.contains(name)) {
        // circular reference
        throw new VendorConversionException(
            String.format("Circular reference to AsPathExpr: '%s'", name));
      }
      arg._asPathExprReferenceStack.add(name);
      AsPathExpr resolved = arg._asPathExprs.get(name);
      if (resolved == null) {
        // undefined reference
        throw new VendorConversionException(
            String.format("Undefined reference to AsPathExpr: '%s'", name));
      }
      resolved.accept(AS_PATH_EXPR_VERIFIER, arg);
      arg._verifiedAsPathExprReferences.add(name);
      arg._asPathExprReferenceStack.remove(name);
      return null;
    }

    @Override
    public Void visitDedupedAsPath(
        DedupedAsPath dedupedAsPath, AsPathStructuresVerifierContext arg) {
      dedupedAsPath.getAsPathExpr().accept(this, arg);
      return null;
    }

    @Override
    public Void visitInputAsPath(InputAsPath inputAsPath, AsPathStructuresVerifierContext arg) {
      return null;
    }
  }

  private static final class AsPathStructuresStatementVerifier
      implements StatementVisitor<Void, AsPathStructuresVerifierContext> {

    @Override
    public Void visitCallStatement(
        CallStatement callStatement, AsPathStructuresVerifierContext arg) {
      // No need to dereference and recurse, since all named routing policies will be visited
      // anyway.
      return null;
    }

    @Override
    public Void visitComment(Comment comment, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitIf(If if1, AsPathStructuresVerifierContext arg) {
      if1.getFalseStatements().stream().forEach(s -> s.accept(this, arg));
      if1.getTrueStatements().stream().forEach(s -> s.accept(this, arg));
      if1.getGuard().accept(BOOLEAN_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitPrependAsPath(
        PrependAsPath prependAsPath, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitReplaceAsesInAsSequence(ReplaceAsesInAsSequence replaceAsesInAsPathSequence) {
      return null;
    }

    @Override
    public Void visitSetOriginatorIp(
        SetOriginatorIp setOriginatorIp, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitExcludeAsPath(
        ExcludeAsPath excludeAsPath, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitRemoveTunnelEncapsulationAttribute(
        RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
        AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetAdministrativeCost(
        SetAdministrativeCost setAdministrativeCost, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetCommunities(
        SetCommunities setCommunities, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetDefaultPolicy(
        SetDefaultPolicy setDefaultPolicy, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetEigrpMetric(
        SetEigrpMetric setEigrpMetric, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetIsisLevel(SetIsisLevel setIsisLevel, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetIsisMetricType(
        SetIsisMetricType setIsisMetricType, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetLocalPreference(
        SetLocalPreference setLocalPreference, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetMetric(SetMetric setMetric, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetNextHop(SetNextHop setNextHop, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetOrigin(SetOrigin setOrigin, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetOspfMetricType(
        SetOspfMetricType setOspfMetricType, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetTag(SetTag setTag, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetDefaultTag(
        SetDefaultTag setDefaultTag, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetTunnelEncapsulationAttribute(
        SetTunnelEncapsulationAttribute setTunnelAttribute, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetVarMetricType(
        SetVarMetricType setVarMetricType, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetWeight(SetWeight setWeight, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitStaticStatement(
        StaticStatement staticStatement, AsPathStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitTraceableStatement(
        TraceableStatement traceableStatement, AsPathStructuresVerifierContext arg) {
      traceableStatement.getInnerStatements().stream().forEach(s -> s.accept(this, arg));
      return null;
    }
  }

  @VisibleForTesting
  static final class AsPathStructuresVerifierContext {

    static final class Builder {
      public @Nonnull AsPathStructuresVerifierContext build() {
        return new AsPathStructuresVerifierContext(
            _asPathExprs, _asPathMatchExprs, _routingPolicies);
      }

      public @Nonnull Builder setAsPathExprs(Map<String, AsPathExpr> asPathExprs) {
        _asPathExprs = asPathExprs;
        return this;
      }

      public @Nonnull Builder setAsPathMatchExprs(Map<String, AsPathMatchExpr> asPathMatchExprs) {
        _asPathMatchExprs = asPathMatchExprs;
        return this;
      }

      public @Nonnull Builder setRoutingPolicies(Map<String, RoutingPolicy> routingPolicies) {
        _routingPolicies = routingPolicies;
        return this;
      }

      private @Nonnull Map<String, AsPathExpr> _asPathExprs;
      private @Nonnull Map<String, AsPathMatchExpr> _asPathMatchExprs;
      private @Nonnull Map<String, RoutingPolicy> _routingPolicies;

      private Builder() {
        _asPathExprs = ImmutableMap.of();
        _asPathMatchExprs = ImmutableMap.of();
        _routingPolicies = ImmutableMap.of();
      }
    }

    @VisibleForTesting
    static @Nonnull Builder builder() {
      return new Builder();
    }

    @VisibleForTesting
    static @Nonnull AsPathStructuresVerifierContext fromConfiguration(Configuration c) {
      return builder()
          .setAsPathExprs(c.getAsPathExprs())
          .setAsPathMatchExprs(c.getAsPathMatchExprs())
          .setRoutingPolicies(c.getRoutingPolicies())
          .build();
    }

    private AsPathStructuresVerifierContext(
        Map<String, AsPathExpr> asPathExprs,
        Map<String, AsPathMatchExpr> asPathMatchExprs,
        Map<String, RoutingPolicy> routingPolicies) {
      _asPathExprs = asPathExprs;
      _asPathMatchExprs = asPathMatchExprs;
      _routingPolicies = routingPolicies;
      _verifiedAsPathMatchExprReferences = new HashSet<>();
      _verifiedAsPathExprReferences = new HashSet<>();
      _asPathMatchExprReferenceStack = new HashSet<>();
      _asPathExprReferenceStack = new HashSet<>();
    }

    private final @Nonnull Map<String, AsPathExpr> _asPathExprs;
    private final @Nonnull Map<String, AsPathMatchExpr> _asPathMatchExprs;
    private final @Nonnull Map<String, RoutingPolicy> _routingPolicies;
    private final @Nonnull Set<String> _verifiedAsPathMatchExprReferences;
    private final @Nonnull Set<String> _verifiedAsPathExprReferences;
    private final @Nonnull Set<String> _asPathMatchExprReferenceStack;
    private final @Nonnull Set<String> _asPathExprReferenceStack;
  }

  @VisibleForTesting
  static final AsPathStructuresBooleanExprVerifier BOOLEAN_EXPR_VERIFIER =
      new AsPathStructuresBooleanExprVerifier();

  @VisibleForTesting
  static final AsPathMatchExprVerifier AS_PATH_MATCH_EXPR_VERIFIER = new AsPathMatchExprVerifier();

  @VisibleForTesting
  static final AsPathExprVerifier AS_PATH_EXPR_VERIFIER = new AsPathExprVerifier();

  @VisibleForTesting
  static final AsPathStructuresStatementVerifier STATEMENT_VERIFIER =
      new AsPathStructuresStatementVerifier();

  private final AsPathStructuresVerifierContext _ctx;

  @VisibleForTesting
  AsPathStructuresVerifier(Configuration c) {
    _ctx = AsPathStructuresVerifierContext.fromConfiguration(c);
  }

  @VisibleForTesting
  void verify() {
    verifyRoutingPolicies();
    verifyAsPathExprs();
    verifyAsPathMatchExprs();
  }

  @VisibleForTesting
  void verifyAsPathMatchExprs() {
    _ctx._asPathMatchExprs.values().stream()
        .forEach(e -> e.accept(AS_PATH_MATCH_EXPR_VERIFIER, _ctx));
  }

  @VisibleForTesting
  void verifyAsPathExprs() {
    _ctx._asPathExprs.values().stream().forEach(e -> e.accept(AS_PATH_EXPR_VERIFIER, _ctx));
  }

  @VisibleForTesting
  void verifyRoutingPolicies() {
    _ctx._routingPolicies.values().stream()
        .flatMap(rp -> rp.getStatements().stream())
        .forEach(s -> s.accept(STATEMENT_VERIFIER, _ctx));
  }
}
