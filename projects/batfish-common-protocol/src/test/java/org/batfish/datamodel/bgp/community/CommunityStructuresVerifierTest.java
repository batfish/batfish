package org.batfish.datamodel.bgp.community;

import static org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.BOOLEAN_EXPR_VERIFIER;
import static org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.COMMUNITY_MATCH_EXPR_VERIFIER;
import static org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.COMMUNITY_SET_EXPR_VERIFIER;
import static org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.COMMUNITY_SET_MATCH_EXPR_VERIFIER;
import static org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.STATEMENT_VERIFIER;
import static org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.verify;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.bgp.community.CommunityStructuresVerifier.CommunityStructuresVerifierContext;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisMetricType;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprs;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.communities.TypesFirstAscendingSpaceSeparated;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralEigrpMetric;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralIsisLevel;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType.Type;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchLocalPreference;
import org.batfish.datamodel.routing_policy.expr.MatchLocalRouteSourcePrefixLength;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProcessAsn;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchRouteType;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.VarRouteType;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
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
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link CommunityStructuresVerifier}. */
public final class CommunityStructuresVerifierTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBooleanExprVerifierUnrelated() {
    // no exception should be thrown while verifying non-community-related structures
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    assertNull(BooleanExprs.TRUE.accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new CallExpr("a").accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new HasRoute(new NamedPrefixSet("a")).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new LegacyMatchAsPath(new NamedAsPathSet("a")).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchBgpSessionType(Type.EBGP).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchColor(1).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(MatchIpv4.instance().accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(
        new MatchLocalPreference(IntComparator.EQ, new LiteralLong(1))
            .accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(
        new MatchLocalRouteSourcePrefixLength(SubRange.singleton(1))
            .accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(
        new MatchMetric(IntComparator.EQ, new LiteralLong(1)).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(
        new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("a"))
            .accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchProcessAsn(1L).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchProtocol(RoutingProtocol.BGP).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchRouteType(new VarRouteType("a")).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchSourceVrf("a").accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(
        new MatchTag(IntComparator.EQ, new LiteralLong(1L)).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(RouteIsClassful.instance().accept(BOOLEAN_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testStatementVerifierUnrelated() {
    // no exception should be thrown while verifying non-community-related structures
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    assertNull(new CallStatement("a").accept(STATEMENT_VERIFIER, ctx));
    assertNull(new Comment("a").accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new PrependAsPath(new LiteralAsList(ImmutableList.of())).accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new ExcludeAsPath(new LiteralAsList(ImmutableList.of())).accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new SetAdministrativeCost(new LiteralAdministrativeCost(1))
            .accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetDefaultPolicy("a").accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new SetEigrpMetric(
                new LiteralEigrpMetric(
                    EigrpMetricValues.builder().setBandwidth(1).setDelay(1).build()))
            .accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new SetIsisLevel(new LiteralIsisLevel(IsisLevel.LEVEL_1)).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetIsisMetricType(IsisMetricType.EXTERNAL).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetLocalPreference(new LiteralLong(1L)).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetMetric(new LiteralLong(1L)).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetNextHop(DiscardNextHop.INSTANCE).accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new SetOrigin(new LiteralOrigin(OriginType.EGP, null)).accept(STATEMENT_VERIFIER, ctx));
    assertNull(SetOriginatorIp.of(NextHopIp.instance()).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetOspfMetricType(OspfMetricType.E1).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetTag(new LiteralLong(1L)).accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetVarMetricType("a").accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetWeight(new LiteralInt(1)).accept(STATEMENT_VERIFIER, ctx));
    assertNull(Statements.ExitAccept.toStaticStatement().accept(STATEMENT_VERIFIER, ctx));
  }

  @Test
  public void testVerify() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    c.setCommunityMatchExprs(
        ImmutableMap.of("referenceToUndefined", new CommunityMatchExprReference("undefined")));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    verify(c);
  }

  @Test
  public void testVerifyCommunityMatchExprs() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    c.setCommunityMatchExprs(
        ImmutableMap.of("referenceToUndefined", new CommunityMatchExprReference("undefined")));
    CommunityStructuresVerifier v = new CommunityStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyCommunityMatchExprs();
  }

  @Test
  public void testVerifyCommunitySetMatchExprs() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    c.setCommunitySetMatchExprs(
        ImmutableMap.of("referenceToUndefined", new CommunitySetMatchExprReference("undefined")));
    CommunityStructuresVerifier v = new CommunityStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyCommunitySetMatchExprs();
  }

  @Test
  public void testVerifyCommunitySetExprs() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);

    c.setCommunitySetExprs(
        ImmutableMap.of("referenceToUndefined", new CommunitySetExprReference("undefined")));
    CommunityStructuresVerifier v = new CommunityStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyCommunitySetExprs();
  }

  @Test
  public void testVerifyRoutingPolicies() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    RoutingPolicy rp = new RoutingPolicy("rp", c);
    rp.setStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))));
    c.setRoutingPolicies(ImmutableMap.of("rp", rp));
    CommunityStructuresVerifier v = new CommunityStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyRoutingPolicies();
  }

  @Test
  public void testVisitCommunityMatchExprReferenceDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunityMatchExprs(ImmutableMap.of("defined", AllStandardCommunities.instance()))
            .build();

    assertNull(
        new CommunityMatchExprReference("defined").accept(COMMUNITY_MATCH_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunityMatchExprReferenceUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new CommunityMatchExprReference("undefined").accept(COMMUNITY_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunityMatchExprReferenceCircular() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunityMatchExprs(
                ImmutableMap.of("circular", new CommunityMatchExprReference("circular")))
            .build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Circular reference"));
    new CommunityMatchExprReference("circular").accept(COMMUNITY_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetMatchExprReferenceDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySetMatchExprs(
                ImmutableMap.of(
                    "defined",
                    new CommunitySetMatchRegex(
                        new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()),
                        "a")))
            .build();

    assertNull(
        new CommunitySetMatchExprReference("defined")
            .accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunitySetMatchExprReferenceUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new CommunitySetMatchExprReference("undefined").accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetMatchExprReferenceCircular() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySetMatchExprs(
                ImmutableMap.of("circular", new CommunitySetMatchExprReference("circular")))
            .build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Circular reference"));
    new CommunitySetMatchExprReference("circular").accept(COMMUNITY_SET_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetExprReferenceDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySetExprs(ImmutableMap.of("defined", CommunitySetExprs.empty()))
            .build();

    assertNull(new CommunitySetExprReference("defined").accept(COMMUNITY_SET_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunitySetExprReferenceUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new CommunitySetExprReference("undefined").accept(COMMUNITY_SET_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetExprReferenceCircular() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySetExprs(
                ImmutableMap.of("circular", new CommunitySetExprReference("circular")))
            .build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Circular reference"));
    new CommunitySetExprReference("circular").accept(COMMUNITY_SET_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetReferenceDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySets(ImmutableMap.of("defined", CommunitySet.empty()))
            .build();

    assertNull(new CommunitySetReference("defined").accept(COMMUNITY_SET_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunitySetReferenceUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new CommunitySetReference("undefined").accept(COMMUNITY_SET_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetUnionDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySetExprs(ImmutableMap.of("defined", CommunitySetExprs.empty()))
            .build();

    assertNull(
        CommunitySetUnion.of(new CommunitySetExprReference("defined"))
            .accept(COMMUNITY_SET_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunitySetUnionUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    CommunitySetUnion.of(new CommunitySetExprReference("undefined"))
        .accept(COMMUNITY_SET_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetDifferenceSetDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunitySetExprs(ImmutableMap.of("defined", CommunitySetExprs.empty()))
            .build();

    assertNull(
        new CommunitySetDifference(
                new CommunitySetExprReference("defined"), AllStandardCommunities.instance())
            .accept(COMMUNITY_SET_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunitySetDifferenceSetUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new CommunitySetDifference(
            new CommunitySetExprReference("undefined"), AllStandardCommunities.instance())
        .accept(COMMUNITY_SET_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCommunitySetDifferenceMatchDefined() {
    CommunityStructuresVerifierContext ctx =
        CommunityStructuresVerifierContext.builder()
            .setCommunityMatchExprs(ImmutableMap.of("defined", AllStandardCommunities.instance()))
            .build();

    assertNull(
        new CommunitySetDifference(
                CommunitySetExprs.empty(), new CommunityMatchExprReference("defined"))
            .accept(COMMUNITY_SET_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitCommunitySetDifferenceMatchUndefined() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new CommunitySetDifference(
            CommunitySetExprs.empty(), new CommunityMatchExprReference("undefined"))
        .accept(COMMUNITY_SET_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitConjunction() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new Conjunction(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitConjunctionChain() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new ConjunctionChain(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitDisjunction() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new Disjunction(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitFirstMatchChain() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new FirstMatchChain(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitTrackSucceeded() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    // In the future, RibExpr is anticipated to potentially include communities.
    assertNull(new TrackSucceeded("foo").accept(BOOLEAN_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitIfGuard() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new If(
            new MatchCommunities(
                InputCommunities.instance(), new CommunitySetMatchExprReference("undefined")),
            ImmutableList.of(),
            ImmutableList.of())
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitIfTrue() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new If(
            BooleanExprs.TRUE,
            ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))),
            ImmutableList.of())
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitIfFalse() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new If(
            BooleanExprs.TRUE,
            ImmutableList.of(),
            ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))))
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitMatchCommunities() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new MatchCommunities(
            InputCommunities.instance(), new CommunitySetMatchExprReference("undefined"))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitNot() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new Not(
            new MatchCommunities(
                InputCommunities.instance(), new CommunitySetMatchExprReference("undefined")))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitSetCommunities() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new SetCommunities(new CommunitySetExprReference("undefined")).accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitTraceableStatement() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new TraceableStatement(
            TraceElement.of("text"),
            ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))))
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(
        new MatchCommunities(
            InputCommunities.instance(), new CommunitySetMatchExprReference("undefined")));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentPost() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(BooleanExprs.TRUE);
    expr.setPostStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentPostTrue() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(BooleanExprs.TRUE);
    expr.setPostTrueStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentPre() {
    CommunityStructuresVerifierContext ctx = CommunityStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(BooleanExprs.TRUE);
    expr.setPreStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference("undefined"))));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }
}
