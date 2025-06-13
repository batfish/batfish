package org.batfish.datamodel.bgp.community;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.AllExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.AllLargeCommunities;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorHighMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorLowMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityLocalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.OpaqueExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.communities.SiteOfOriginExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.communities.VpnDistinguisherExtendedCommunities;
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
import org.batfish.datamodel.routing_policy.expr.MatchOspfExternalType;
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
 * Provides functionality to verify absence of undefined/cyclical references in community-related
 * structures.
 *
 * <p>It is the responsiblity of conversion code to handle undefined/cyclical references to
 * vendor-specific structures. Conversion code must not propagate undefined/cyclical references to
 * vendor-independent structures. Thus any such references to vendor-independent structures signify
 * errors in conversion code, rather than the input configuration text.
 */
public final class CommunityStructuresVerifier {

  /**
   * Verifies absence of undefined/cyclical references in community-related structures within the
   * input {@code configuration}.
   *
   * @throws VendorConversionException if an undefined or cyclical reference to a community-related
   *     structure is found in {@code configuration}.
   */
  public static void verify(Configuration configuration) {
    new CommunityStructuresVerifier(configuration).verify();
  }

  private static final class CommunityStructuresBooleanExprVerifier
      implements BooleanExprVisitor<Void, CommunityStructuresVerifierContext> {

    @Override
    public Void visitBooleanExprs(
        StaticBooleanExpr staticBooleanExpr, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchClusterListLength(
        MatchClusterListLength matchClusterListLength, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCallExpr(CallExpr callExpr, CommunityStructuresVerifierContext arg) {
      // No need to dereference and recurse, since all named routing policies will be visited
      // anyway.
      return null;
    }

    @Override
    public Void visitConjunction(Conjunction conjunction, CommunityStructuresVerifierContext arg) {
      conjunction.getConjuncts().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitConjunctionChain(
        ConjunctionChain conjunctionChain, CommunityStructuresVerifierContext arg) {
      conjunctionChain.getSubroutines().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitDisjunction(Disjunction disjunction, CommunityStructuresVerifierContext arg) {
      disjunction.getDisjuncts().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitFirstMatchChain(
        FirstMatchChain firstMatchChain, CommunityStructuresVerifierContext arg) {
      firstMatchChain.getSubroutines().stream().forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitTrackSucceeded(
        TrackSucceeded trackSucceeded, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitHasRoute(HasRoute hasRoute, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchAsPath(MatchAsPath matchAsPath, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLegacyAsPath(
        LegacyMatchAsPath legacyMatchAsPath, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchBgpSessionType(
        MatchBgpSessionType matchBgpSessionType, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchColor(MatchColor matchColor, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchCommunities(
        MatchCommunities matchCommunities, CommunityStructuresVerifierContext arg) {
      matchCommunities.getCommunitySetExpr().accept(COMMUNITY_SET_EXPR_VERIFIER, arg);
      matchCommunities.getCommunitySetMatchExpr().accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitMatchInterface(
        MatchInterface matchInterface, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchIpv4(MatchIpv4 matchIpv4, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLocalPreference(
        MatchLocalPreference matchLocalPreference, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchLocalRouteSourcePrefixLength(
        MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength,
        CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchMetric(MatchMetric matchMetric, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchOspfExternalType(
        MatchOspfExternalType matchOspfExternalType, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchPeerAddress(
        MatchPeerAddress matchPeerAddress, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchPrefixSet(
        MatchPrefixSet matchPrefixSet, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchProcessAsn(
        MatchProcessAsn matchProcessAsn, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchProtocol(
        MatchProtocol matchProtocol, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchRouteType(
        MatchRouteType matchRouteType, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchSourceProtocol(
        MatchSourceProtocol matchSourceProtocol, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchSourceVrf(
        MatchSourceVrf matchSourceVrf, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitMatchTag(MatchTag matchTag, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitNot(Not not, CommunityStructuresVerifierContext arg) {
      return not.getExpr().accept(this, arg);
    }

    @Override
    public Void visitRouteIsClassful(
        RouteIsClassful routeIsClassful, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitWithEnvironmentExpr(
        WithEnvironmentExpr withEnvironmentExpr, CommunityStructuresVerifierContext arg) {
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

  private static final class CommunityMatchExprVerifier
      implements CommunityMatchExprVisitor<Void, CommunityStructuresVerifierContext> {

    @Override
    public Void visitAllExtendedCommunities(
        AllExtendedCommunities allExtendedCommunities, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitAllLargeCommunities(
        AllLargeCommunities allLargeCommunities, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitAllStandardCommunities(
        AllStandardCommunities allStandardCommunities, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCommunityAcl(
        CommunityAcl communityAcl, CommunityStructuresVerifierContext arg) {
      communityAcl.getLines().stream()
          .map(CommunityAclLine::getCommunityMatchExpr)
          .forEach(expr -> expr.accept(this, arg));
      return null;
    }

    @Override
    public Void visitCommunityIn(CommunityIn communityIn, CommunityStructuresVerifierContext arg) {
      communityIn.getCommunitySetExpr().accept(COMMUNITY_SET_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitCommunityIs(CommunityIs communityIs, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCommunityMatchAll(
        CommunityMatchAll communityMatchAll, CommunityStructuresVerifierContext arg) {
      communityMatchAll.getExprs().forEach(expr -> expr.accept(COMMUNITY_MATCH_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitCommunityMatchAny(
        CommunityMatchAny communityMatchAny, CommunityStructuresVerifierContext arg) {
      communityMatchAny.getExprs().forEach(expr -> expr.accept(COMMUNITY_MATCH_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitCommunityMatchExprReference(
        CommunityMatchExprReference communityMatchExprReference,
        CommunityStructuresVerifierContext arg) {
      String name = communityMatchExprReference.getName();
      if (arg._verifiedCommunityMatchExprs.contains(name)) {
        return null;
      }
      if (arg._visitedCommunityMatchExprs.contains(name)) {
        // circular reference
        throw new VendorConversionException(
            String.format("Circular reference to communityMatchExpr: '%s'", name));
      }
      arg._visitedCommunityMatchExprs.add(name);
      CommunityMatchExpr resolved = arg._communityMatchExprs.get(name);
      if (resolved == null) {
        // undefined reference
        throw new VendorConversionException(
            String.format("Undefined reference to communityMatchExpr: '%s'", name));
      }
      resolved.accept(COMMUNITY_MATCH_EXPR_VERIFIER, arg);
      arg._verifiedCommunityMatchExprs.add(name);
      arg._visitedCommunityMatchExprs.remove(name);
      return null;
    }

    @Override
    public Void visitCommunityMatchRegex(
        CommunityMatchRegex communityMatchRegex, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCommunityNot(
        CommunityNot communityNot, CommunityStructuresVerifierContext arg) {
      communityNot.getExpr().accept(COMMUNITY_MATCH_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitExtendedCommunityGlobalAdministratorHighMatch(
        ExtendedCommunityGlobalAdministratorHighMatch extendedCommunityGlobalAdministratorHighMatch,
        CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitExtendedCommunityGlobalAdministratorLowMatch(
        ExtendedCommunityGlobalAdministratorLowMatch extendedCommunityGlobalAdministratorLowMatch,
        CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitExtendedCommunityGlobalAdministratorMatch(
        ExtendedCommunityGlobalAdministratorMatch extendedCommunityGlobalAdministratorMatch,
        CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitExtendedCommunityLocalAdministratorMatch(
        ExtendedCommunityLocalAdministratorMatch extendedCommunityLocalAdministratorMatch,
        CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitOpaqueExtendedCommunities(
        OpaqueExtendedCommunities opaqueExtendedCommunities,
        CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitRouteTargetExtendedCommunities(
        RouteTargetExtendedCommunities routeTargetExtendedCommunities,
        CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSiteOfOriginExtendedCommunities(
        SiteOfOriginExtendedCommunities siteOfOriginExtendedCommunities,
        CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitStandardCommunityHighMatch(
        StandardCommunityHighMatch standardCommunityHighMatch,
        CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitStandardCommunityLowMatch(
        StandardCommunityLowMatch standardCommunityLowMatch,
        CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitVpnDistinguisherExtendedCommunities(
        VpnDistinguisherExtendedCommunities vpnDistinguisherExtendedCommunities,
        CommunityStructuresVerifierContext arg) {
      return null;
    }
  }

  private static final class CommunitySetMatchExprVerifier
      implements CommunitySetMatchExprVisitor<Void, CommunityStructuresVerifierContext> {

    @Override
    public Void visitCommunitySetAcl(
        CommunitySetAcl communitySetAcl, CommunityStructuresVerifierContext arg) {
      communitySetAcl.getLines().stream()
          .map(CommunitySetAclLine::getCommunitySetMatchExpr)
          .forEach(expr -> expr.accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitCommunitySetMatchAll(
        CommunitySetMatchAll communitySetMatchAll, CommunityStructuresVerifierContext arg) {
      communitySetMatchAll
          .getExprs()
          .forEach(expr -> expr.accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitCommunitySetMatchAny(
        CommunitySetMatchAny communitySetMatchAny, CommunityStructuresVerifierContext arg) {
      communitySetMatchAny
          .getExprs()
          .forEach(expr -> expr.accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitCommunitySetMatchExprReference(
        CommunitySetMatchExprReference communitySetMatchExprReference,
        CommunityStructuresVerifierContext arg) {
      String name = communitySetMatchExprReference.getName();
      if (arg._verifiedCommunitySetMatchExprs.contains(name)) {
        return null;
      }
      if (arg._visitedCommunitySetMatchExprs.contains(name)) {
        // circular reference
        throw new VendorConversionException(
            String.format("Circular reference to communitySetMatchExpr: '%s'", name));
      }
      arg._visitedCommunitySetMatchExprs.add(name);
      CommunitySetMatchExpr resolved = arg._communitySetMatchExprs.get(name);
      if (resolved == null) {
        // undefined reference
        throw new VendorConversionException(
            String.format("Undefined reference to communitySetMatchExpr: '%s'", name));
      }
      resolved.accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, arg);
      arg._verifiedCommunitySetMatchExprs.add(name);
      arg._visitedCommunitySetMatchExprs.remove(name);
      return null;
    }

    @Override
    public Void visitCommunitySetMatchRegex(
        CommunitySetMatchRegex communitySetMatchRegex, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitCommunitySetNot(
        CommunitySetNot communitySetNot, CommunityStructuresVerifierContext arg) {
      communitySetNot.getExpr().accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitHasCommunity(
        HasCommunity hasCommunity, CommunityStructuresVerifierContext arg) {
      hasCommunity.getExpr().accept(COMMUNITY_MATCH_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitHasSize(HasSize hasSize, CommunityStructuresVerifierContext arg) {
      return null;
    }
  }

  private static final class CommunitySetExprVerifier
      implements CommunitySetExprVisitor<Void, CommunityStructuresVerifierContext> {

    @Override
    public Void visitCommunityExprsSet(
        CommunityExprsSet communityExprsSet, CommunityStructuresVerifierContext arg) {
      // nothing that can be checked statically
      return null;
    }

    @Override
    public Void visitCommunitySetDifference(
        CommunitySetDifference communitySetDifference, CommunityStructuresVerifierContext arg) {
      communitySetDifference.getInitial().accept(COMMUNITY_SET_EXPR_VERIFIER, arg);
      communitySetDifference.getRemovalCriterion().accept(COMMUNITY_MATCH_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitCommunitySetExprReference(
        CommunitySetExprReference communitySetExprReference,
        CommunityStructuresVerifierContext arg) {
      String name = communitySetExprReference.getName();
      if (arg._verifiedCommunitySetExprs.contains(name)) {
        return null;
      }
      if (arg._visitedCommunitySetExprs.contains(name)) {
        // circular reference
        throw new VendorConversionException(
            String.format("Circular reference to CommunitySetExpr: '%s'", name));
      }
      arg._visitedCommunitySetExprs.add(name);
      CommunitySetExpr resolved = arg._communitySetExprs.get(name);
      if (resolved == null) {
        // undefined reference
        throw new VendorConversionException(
            String.format("Undefined reference to CommunitySetExpr: '%s'", name));
      }
      resolved.accept(COMMUNITY_SET_EXPR_VERIFIER, arg);
      arg._verifiedCommunitySetExprs.add(name);
      arg._visitedCommunitySetExprs.remove(name);
      return null;
    }

    @Override
    public Void visitCommunitySetReference(
        CommunitySetReference communitySetReference, CommunityStructuresVerifierContext arg) {
      String name = communitySetReference.getName();
      if (!arg._communitySets.containsKey(name)) {
        // undefined reference
        throw new VendorConversionException(
            String.format("Undefined reference to CommunitySet: '%s'", name));
      }
      return null;
    }

    @Override
    public Void visitCommunitySetUnion(
        CommunitySetUnion communitySetUnion, CommunityStructuresVerifierContext arg) {
      communitySetUnion.getExprs().forEach(expr -> expr.accept(COMMUNITY_SET_EXPR_VERIFIER, arg));
      return null;
    }

    @Override
    public Void visitInputCommunities(
        InputCommunities inputCommunities, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitLiteralCommunitySet(
        LiteralCommunitySet literalCommunitySet, CommunityStructuresVerifierContext arg) {
      return null;
    }
  }

  private static final class CommunityStructuresStatementVerifier
      implements StatementVisitor<Void, CommunityStructuresVerifierContext> {

    @Override
    public Void visitCallStatement(
        CallStatement callStatement, CommunityStructuresVerifierContext arg) {
      // No need to dereference and recurse, since all named routing policies will be visited
      // anyway.
      return null;
    }

    @Override
    public Void visitComment(Comment comment, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitIf(If if1, CommunityStructuresVerifierContext arg) {
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
        SetOriginatorIp setOriginatorIp, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitPrependAsPath(
        PrependAsPath prependAsPath, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitExcludeAsPath(
        ExcludeAsPath excludeAsPath, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitRemoveTunnelEncapsulationAttribute(
        RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
        CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetAdministrativeCost(
        SetAdministrativeCost setAdministrativeCost, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetCommunities(
        SetCommunities setCommunities, CommunityStructuresVerifierContext arg) {
      setCommunities.getCommunitySetExpr().accept(COMMUNITY_SET_EXPR_VERIFIER, arg);
      return null;
    }

    @Override
    public Void visitSetDefaultPolicy(
        SetDefaultPolicy setDefaultPolicy, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetEigrpMetric(
        SetEigrpMetric setEigrpMetric, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetIsisLevel(
        SetIsisLevel setIsisLevel, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetIsisMetricType(
        SetIsisMetricType setIsisMetricType, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetLocalPreference(
        SetLocalPreference setLocalPreference, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetMetric(SetMetric setMetric, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetNextHop(SetNextHop setNextHop, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetOrigin(SetOrigin setOrigin, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetOspfMetricType(
        SetOspfMetricType setOspfMetricType, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetTag(SetTag setTag, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetDefaultTag(
        SetDefaultTag setDefaultTag, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetTunnelEncapsulationAttribute(
        SetTunnelEncapsulationAttribute setTunnelEncapsulationAttribute,
        CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetVarMetricType(
        SetVarMetricType setVarMetricType, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitSetWeight(SetWeight setWeight, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitStaticStatement(
        StaticStatement staticStatement, CommunityStructuresVerifierContext arg) {
      return null;
    }

    @Override
    public Void visitTraceableStatement(
        TraceableStatement traceableStatement, CommunityStructuresVerifierContext arg) {
      traceableStatement.getInnerStatements().stream().forEach(s -> s.accept(this, arg));
      return null;
    }
  }

  @VisibleForTesting
  static final class CommunityStructuresVerifierContext {

    static final class Builder {
      public @Nonnull CommunityStructuresVerifierContext build() {
        return new CommunityStructuresVerifierContext(
            _communityMatchExprs,
            _communitySetExprs,
            _communitySetMatchExprs,
            _communitySets,
            _routingPolicies);
      }

      public @Nonnull Builder setCommunityMatchExprs(
          Map<String, CommunityMatchExpr> communityMatchExprs) {
        _communityMatchExprs = communityMatchExprs;
        return this;
      }

      public @Nonnull Builder setCommunitySetExprs(
          Map<String, CommunitySetExpr> communitySetExprs) {
        _communitySetExprs = communitySetExprs;
        return this;
      }

      public @Nonnull Builder setCommunitySetMatchExprs(
          Map<String, CommunitySetMatchExpr> communitySetMatchExprs) {
        _communitySetMatchExprs = communitySetMatchExprs;
        return this;
      }

      public @Nonnull Builder setCommunitySets(Map<String, CommunitySet> communitySets) {
        _communitySets = communitySets;
        return this;
      }

      public @Nonnull Builder setRoutingPolicies(Map<String, RoutingPolicy> routingPolicies) {
        _routingPolicies = routingPolicies;
        return this;
      }

      private @Nonnull Map<String, CommunityMatchExpr> _communityMatchExprs;
      private @Nonnull Map<String, CommunitySetExpr> _communitySetExprs;
      private @Nonnull Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
      private @Nonnull Map<String, CommunitySet> _communitySets;
      private @Nonnull Map<String, RoutingPolicy> _routingPolicies;

      private Builder() {
        _communityMatchExprs = ImmutableMap.of();
        _communitySetExprs = ImmutableMap.of();
        _communitySetMatchExprs = ImmutableMap.of();
        _communitySets = ImmutableMap.of();
        _routingPolicies = ImmutableMap.of();
      }
    }

    @VisibleForTesting
    static @Nonnull Builder builder() {
      return new Builder();
    }

    @VisibleForTesting
    static @Nonnull CommunityStructuresVerifierContext fromConfiguration(Configuration c) {
      return builder()
          .setCommunityMatchExprs(c.getCommunityMatchExprs())
          .setCommunitySetExprs(c.getCommunitySetExprs())
          .setCommunitySetMatchExprs(c.getCommunitySetMatchExprs())
          .setCommunitySets(c.getCommunitySets())
          .setRoutingPolicies(c.getRoutingPolicies())
          .build();
    }

    private CommunityStructuresVerifierContext(
        Map<String, CommunityMatchExpr> communityMatchExprs,
        Map<String, CommunitySetExpr> communitySetExprs,
        Map<String, CommunitySetMatchExpr> communitySetMatchExprs,
        Map<String, CommunitySet> communitySets,
        Map<String, RoutingPolicy> routingPolicies) {
      _communityMatchExprs = communityMatchExprs;
      _communitySetExprs = communitySetExprs;
      _communitySetMatchExprs = communitySetMatchExprs;
      _communitySets = communitySets;
      _routingPolicies = routingPolicies;
      _verifiedCommunityMatchExprs = new HashSet<>();
      _verifiedCommunitySetMatchExprs = new HashSet<>();
      _verifiedCommunitySetExprs = new HashSet<>();
      _visitedCommunityMatchExprs = new HashSet<>();
      _visitedCommunitySetMatchExprs = new HashSet<>();
      _visitedCommunitySetExprs = new HashSet<>();
    }

    private final @Nonnull Map<String, CommunityMatchExpr> _communityMatchExprs;
    private final @Nonnull Map<String, CommunitySetExpr> _communitySetExprs;
    private final @Nonnull Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
    private final @Nonnull Map<String, CommunitySet> _communitySets;
    private final @Nonnull Map<String, RoutingPolicy> _routingPolicies;
    private final @Nonnull Set<String> _verifiedCommunityMatchExprs;
    private final @Nonnull Set<String> _verifiedCommunitySetMatchExprs;
    private final @Nonnull Set<String> _verifiedCommunitySetExprs;
    private final @Nonnull Set<String> _visitedCommunityMatchExprs;
    private final @Nonnull Set<String> _visitedCommunitySetMatchExprs;
    private final @Nonnull Set<String> _visitedCommunitySetExprs;
  }

  @VisibleForTesting
  static final CommunityStructuresBooleanExprVerifier BOOLEAN_EXPR_VERIFIER =
      new CommunityStructuresBooleanExprVerifier();

  @VisibleForTesting
  static final CommunityMatchExprVerifier COMMUNITY_MATCH_EXPR_VERIFIER =
      new CommunityMatchExprVerifier();

  @VisibleForTesting
  static final CommunitySetMatchExprVerifier COMMUNITY_SET_MATCH_EXPR_VERIFIER =
      new CommunitySetMatchExprVerifier();

  @VisibleForTesting
  static final CommunitySetExprVerifier COMMUNITY_SET_EXPR_VERIFIER =
      new CommunitySetExprVerifier();

  @VisibleForTesting
  static final CommunityStructuresStatementVerifier STATEMENT_VERIFIER =
      new CommunityStructuresStatementVerifier();

  private final CommunityStructuresVerifierContext _ctx;

  @VisibleForTesting
  CommunityStructuresVerifier(Configuration c) {
    _ctx = CommunityStructuresVerifierContext.fromConfiguration(c);
  }

  @VisibleForTesting
  void verify() {
    verifyRoutingPolicies();
    verifyCommunityMatchExprs();
    verifyCommunitySetMatchExprs();
    verifyCommunitySetExprs();
  }

  @VisibleForTesting
  void verifyCommunityMatchExprs() {
    _ctx._communityMatchExprs.values().stream()
        .forEach(e -> e.accept(COMMUNITY_MATCH_EXPR_VERIFIER, _ctx));
  }

  @VisibleForTesting
  void verifyCommunitySetMatchExprs() {
    _ctx._communitySetMatchExprs.values().stream()
        .forEach(e -> e.accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, _ctx));
  }

  @VisibleForTesting
  void verifyCommunitySetExprs() {
    _ctx._communitySetExprs.values().stream()
        .forEach(e -> e.accept(COMMUNITY_SET_EXPR_VERIFIER, _ctx));
  }

  @VisibleForTesting
  void verifyRoutingPolicies() {
    _ctx._routingPolicies.values().stream()
        .flatMap(rp -> rp.getStatements().stream())
        .forEach(s -> s.accept(STATEMENT_VERIFIER, _ctx));
  }
}
