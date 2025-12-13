package org.batfish.datamodel.routing_policy.as_path;

import static org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.AS_PATH_EXPR_VERIFIER;
import static org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.AS_PATH_MATCH_EXPR_VERIFIER;
import static org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.AS_PATH_SET_EXPR_VERIFIER;
import static org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.BOOLEAN_EXPR_VERIFIER;
import static org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.STATEMENT_VERIFIER;
import static org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.verify;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisMetricType;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathStructuresVerifier.AsPathStructuresVerifierContext;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
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

/** Test of {@link AsPathStructuresVerifier}. */
public final class AsPathStructuresVerifierTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBooleanExprVerifierUnrelated() {
    // no exception should be thrown while verifying non-as-path-related structures
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    assertNull(BooleanExprs.TRUE.accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new CallExpr("a").accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new HasRoute(new NamedPrefixSet("a")).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(new MatchColor(1).accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertNull(
        new MatchCommunities(
                InputCommunities.instance(), new CommunitySetMatchAny(ImmutableList.of()))
            .accept(BOOLEAN_EXPR_VERIFIER, ctx));
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
    // no exception should be thrown while verifying non-as-path-related structures
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    assertNull(new CallStatement("a").accept(STATEMENT_VERIFIER, ctx));
    assertNull(new Comment("a").accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new PrependAsPath(new LiteralAsList(ImmutableList.of())).accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new ExcludeAsPath(new LiteralAsList(ImmutableList.of())).accept(STATEMENT_VERIFIER, ctx));
    assertNull(
        new SetAdministrativeCost(new LiteralAdministrativeCost(1))
            .accept(STATEMENT_VERIFIER, ctx));
    assertNull(new SetCommunities(InputCommunities.instance()).accept(STATEMENT_VERIFIER, ctx));
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
    c.setAsPathMatchExprs(
        ImmutableMap.of("referenceToUndefined", AsPathMatchExprReference.of("undefined")));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    verify(c);
  }

  @Test
  public void testVerifyAsPathMatchExprs() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    c.setAsPathMatchExprs(
        ImmutableMap.of("referenceToUndefined", AsPathMatchExprReference.of("undefined")));
    AsPathStructuresVerifier v = new AsPathStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyAsPathMatchExprs();
  }

  @Test
  public void testVerifyAsPathExprs() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);

    c.setAsPathExprs(ImmutableMap.of("referenceToUndefined", AsPathExprReference.of("undefined")));
    AsPathStructuresVerifier v = new AsPathStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyAsPathExprs();
  }

  @Test
  public void testVerifyRoutingPolicies() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    RoutingPolicy rp = new RoutingPolicy("rp", c);
    rp.setStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(
                    AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                ImmutableList.of(),
                ImmutableList.of())));
    c.setRoutingPolicies(ImmutableMap.of("rp", rp));
    AsPathStructuresVerifier v = new AsPathStructuresVerifier(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    v.verifyRoutingPolicies();
  }

  @Test
  public void testVisitAsPathMatchAny() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    AsPathMatchAny.of(ImmutableList.of(AsPathMatchExprReference.of("undefined")))
        .accept(AS_PATH_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitAsPathMatchExprReferenceDefined() {
    AsPathStructuresVerifierContext ctx =
        AsPathStructuresVerifierContext.builder()
            .setAsPathMatchExprs(ImmutableMap.of("defined", AsPathMatchAny.of(ImmutableList.of())))
            .build();

    assertNull(AsPathMatchExprReference.of("defined").accept(AS_PATH_MATCH_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitAsPathMatchExprReferenceUndefined() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    AsPathMatchExprReference.of("undefined").accept(AS_PATH_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitAsPathMatchExprReferenceCircular() {
    AsPathStructuresVerifierContext ctx =
        AsPathStructuresVerifierContext.builder()
            .setAsPathMatchExprs(
                ImmutableMap.of("circular", AsPathMatchExprReference.of("circular")))
            .build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Circular reference"));
    AsPathMatchExprReference.of("circular").accept(AS_PATH_MATCH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitAsPathExprReferenceDefined() {
    AsPathStructuresVerifierContext ctx =
        AsPathStructuresVerifierContext.builder()
            .setAsPathExprs(ImmutableMap.of("defined", InputAsPath.instance()))
            .build();

    assertNull(AsPathExprReference.of("defined").accept(AS_PATH_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitAsPathExprReferenceUndefined() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    AsPathExprReference.of("undefined").accept(AS_PATH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitAsPathExprReferenceCircular() {
    AsPathStructuresVerifierContext ctx =
        AsPathStructuresVerifierContext.builder()
            .setAsPathExprs(ImmutableMap.of("circular", AsPathExprReference.of("circular")))
            .build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Circular reference"));
    AsPathExprReference.of("circular").accept(AS_PATH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitConjunction() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new Conjunction(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitConjunctionChain() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new ConjunctionChain(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitDisjunction() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new Disjunction(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitFirstMatchChain() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new FirstMatchChain(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined"))))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitTrackSucceeded() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    // In the future, RibExpr is anticipated to potentially include as-path.
    assertNull(new TrackSucceeded("foo").accept(BOOLEAN_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitIfGuard() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new If(
            MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined")),
            ImmutableList.of(),
            ImmutableList.of())
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitIfTrue() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new If(
            BooleanExprs.TRUE,
            ImmutableList.of(
                new If(
                    MatchAsPath.of(
                        AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                    ImmutableList.of(),
                    ImmutableList.of())),
            ImmutableList.of())
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitIfFalse() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new If(
            BooleanExprs.TRUE,
            ImmutableList.of(),
            ImmutableList.of(
                new If(
                    MatchAsPath.of(
                        AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                    ImmutableList.of(),
                    ImmutableList.of())))
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitMatchAsPathAsPathExpr() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    MatchAsPath.of(AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of()))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitMatchAsPathAsPathMatchExpr() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined"))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitNot() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new Not(MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined")))
        .accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitTraceableStatement() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    new TraceableStatement(
            TraceElement.of("text"),
            ImmutableList.of(
                new If(
                    MatchAsPath.of(
                        AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                    ImmutableList.of(),
                    ImmutableList.of())))
        .accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of("undefined")));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentPost() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(BooleanExprs.TRUE);
    expr.setPostStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(
                    AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                ImmutableList.of(),
                ImmutableList.of())));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentPostTrue() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(BooleanExprs.TRUE);
    expr.setPostTrueStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(
                    AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                ImmutableList.of(),
                ImmutableList.of())));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitWithEnvironmentPre() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();
    WithEnvironmentExpr expr = new WithEnvironmentExpr();
    expr.setExpr(BooleanExprs.TRUE);
    expr.setPreStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(
                    AsPathExprReference.of("undefined"), AsPathMatchAny.of(ImmutableList.of())),
                ImmutableList.of(),
                ImmutableList.of())));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));

    expr.accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitDedupedAsPath() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    DedupedAsPath.of(AsPathExprReference.of("undefined")).accept(AS_PATH_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitLegacyMatchAsPathDefined() {
    AsPathStructuresVerifierContext ctx =
        AsPathStructuresVerifierContext.builder()
            .setAsPathAccessLists(ImmutableMap.of("defined", new AsPathAccessList("defined", null)))
            .build();

    assertNull(
        new LegacyMatchAsPath(new NamedAsPathSet("defined")).accept(BOOLEAN_EXPR_VERIFIER, ctx));
  }

  @Test
  public void testVisitLegacyMatchAsPathUndefined() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to AsPathAccessList"));
    new LegacyMatchAsPath(new NamedAsPathSet("undefined")).accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitNamedAsPathSetDefined() {
    AsPathStructuresVerifierContext ctx =
        AsPathStructuresVerifierContext.builder()
            .setAsPathAccessLists(ImmutableMap.of("defined", new AsPathAccessList("defined", null)))
            .build();

    assertNull(AS_PATH_SET_EXPR_VERIFIER.accept(new NamedAsPathSet("defined"), ctx));
  }

  @Test
  public void testVisitNamedAsPathSetUndefined() {
    AsPathStructuresVerifierContext ctx = AsPathStructuresVerifierContext.builder().build();

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to AsPathAccessList"));
    AS_PATH_SET_EXPR_VERIFIER.accept(new NamedAsPathSet("undefined"), ctx);
  }
}
