package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.AllExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.AllLargeCommunities;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorHighMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorLowMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityLocalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.OpaqueExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SiteOfOriginExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.communities.VpnDistinguisherExtendedCommunities;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongComparison;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.CommunityVar.Type;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.ConfigAtomicPredicatesTestUtils;
import org.batfish.minesweeper.bdd.TransferBDD.Context;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link org.batfish.minesweeper.bdd.CommunityMatchExprToBDD}. */
public class CommunityMatchExprToBDDTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private CommunityMatchExprToBDD _communityMatchExprToBDD;

  @Rule public ExpectedException _exception = ExpectedException.none();

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _batfish = new TransferBDDTest.MockBatfish(ImmutableSortedMap.of(HOSTNAME, _baseConfig));

    ConfigAtomicPredicates configAPs =
        ConfigAtomicPredicatesTestUtils.forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            ImmutableSet.of(
                CommunityVar.from("^20:"),
                CommunityVar.from(":30$"),
                CommunityVar.from(StandardCommunity.parse("20:30")),
                CommunityVar.from(StandardCommunity.parse("21:30")),
                // a route-target extended community
                CommunityVar.from(ExtendedCommunity.target(1, 65555)),
                // a site-of-origin extended community
                CommunityVar.from(ExtendedCommunity.of(0x0003, 1, 1)),
                // a vpn distinguisher extended community
                CommunityVar.from(ExtendedCommunity.of(0x0010, 1, 1)),
                // a few opaque communities
                CommunityVar.from(ExtendedCommunity.opaque(true, 3, 40)),
                CommunityVar.from(ExtendedCommunity.opaque(false, 4, 50)),
                CommunityVar.from(LargeCommunity.of(20, 20, 20))),
            null);
    TransferBDD transferBDD = new TransferBDD(configAPs);
    RoutingPolicy testPolicy =
        nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME).build();
    BDDRoute bddRoute = new BDDRoute(transferBDD.getFactory(), configAPs);
    _arg = new CommunitySetMatchExprToBDD.Arg(transferBDD, bddRoute, Context.forPolicy(testPolicy));

    _communityMatchExprToBDD = new CommunityMatchExprToBDD();
  }

  @Test
  public void testVisitAllExtendedCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitAllExtendedCommunities(
            AllExtendedCommunities.instance(), _arg);

    BDD expected =
        _arg.getTransferBDD()
            .getFactory()
            .orAll(
                ImmutableSet.of(
                        CommunityVar.from(ExtendedCommunity.target(1, 65555)),
                        CommunityVar.from(ExtendedCommunity.of(0x0003, 1, 1)),
                        CommunityVar.from(ExtendedCommunity.of(0x0010, 1, 1)),
                        CommunityVar.from(ExtendedCommunity.opaque(true, 3, 40)),
                        CommunityVar.from(ExtendedCommunity.opaque(false, 4, 50)))
                    .stream()
                    .map(this::cvarToBDD)
                    .collect(ImmutableSet.toImmutableSet()));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitAllLargeCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitAllLargeCommunities(AllLargeCommunities.instance(), _arg);

    assertEquals(cvarToBDD(CommunityVar.from(LargeCommunity.of(20, 20, 20))), result);
  }

  @Test
  public void testVisitAllStandardCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitAllStandardCommunities(
            AllStandardCommunities.instance(), _arg);

    BDD expected =
        _arg.getTransferBDD()
            .getFactory()
            .orAll(
                _arg.getTransferBDD().getCommunityAtomicPredicates().keySet().stream()
                    .filter(
                        c ->
                            c.getType() == Type.REGEX
                                || c.getLiteralValue() instanceof StandardCommunity)
                    .map(this::cvarToBDD)
                    .collect(ImmutableSet.toImmutableSet()));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunityAcl() {
    CommunityAcl cacl =
        new CommunityAcl(
            ImmutableList.of(
                new CommunityAclLine(
                    LineAction.DENY, new CommunityIs(StandardCommunity.parse("20:30"))),
                new CommunityAclLine(
                    LineAction.PERMIT,
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"))));

    BDD result = _communityMatchExprToBDD.visitCommunityAcl(cacl, _arg);

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from("^20:");

    assertEquals(cvarToBDD(cvar1).not().and(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunityIn() {
    CommunityIn ci =
        new CommunityIn(
            new LiteralCommunitySet(
                CommunitySet.of(
                    StandardCommunity.parse("20:30"), StandardCommunity.parse("21:30"))));

    BDD result = _communityMatchExprToBDD.visitCommunityIn(ci, _arg);

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("21:30"));

    assertEquals(cvarToBDD(cvar1).or(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunityIs() {
    CommunityIs ci = new CommunityIs(StandardCommunity.parse("20:30"));

    BDD result = _communityMatchExprToBDD.visitCommunityIs(ci, _arg);

    CommunityVar cvar = CommunityVar.from(StandardCommunity.parse("20:30"));

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitCommunityMatchAll() {
    CommunityMatchAll cma =
        new CommunityMatchAll(
            ImmutableList.of(
                new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"),
                new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":30$")));

    BDD result = _communityMatchExprToBDD.visitCommunityMatchAll(cma, _arg);

    CommunityVar cvar1 = CommunityVar.from("^20:");
    CommunityVar cvar2 = CommunityVar.from(":30$");

    assertEquals(cvarToBDD(cvar1).and(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunityMatchAny() {
    CommunityMatchAny cma =
        new CommunityMatchAny(
            ImmutableList.of(
                new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"),
                new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":30$")));

    BDD result = _communityMatchExprToBDD.visitCommunityMatchAny(cma, _arg);

    CommunityVar cvar1 = CommunityVar.from("^20:");
    CommunityVar cvar2 = CommunityVar.from(":30$");

    assertEquals(cvarToBDD(cvar1).or(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunityMatchExprReference() {
    String name = "name";
    _baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(name, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:")));
    CommunityMatchExprReference cmer = new CommunityMatchExprReference(name);

    BDD result = _communityMatchExprToBDD.visitCommunityMatchExprReference(cmer, _arg);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitCommunityMatchRegex() {
    CommunityMatchRegex cmr = new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:");

    BDD result = _communityMatchExprToBDD.visitCommunityMatchRegex(cmr, _arg);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitCommunityNot() {
    CommunityNot cn = new CommunityNot(new CommunityIs(StandardCommunity.parse("20:30")));

    BDD result = _communityMatchExprToBDD.visitCommunityNot(cn, _arg);

    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("20:30"));

    assertEquals(cvarToBDD(cvar2).not(), result);
  }

  @Test
  public void testVisitExtendedCommunityGlobalAdministratorHighMatch() {
    ExtendedCommunityGlobalAdministratorHighMatch ec =
        new ExtendedCommunityGlobalAdministratorHighMatch(
            new IntComparison(IntComparator.EQ, new LiteralInt(3)));
    _exception.expect(UnsupportedOperationException.class);
    _communityMatchExprToBDD.visitExtendedCommunityGlobalAdministratorHighMatch(ec, _arg);
  }

  @Test
  public void testVisitExtendedCommunityGlobalAdministratorLowMatch() {
    ExtendedCommunityGlobalAdministratorLowMatch ec =
        new ExtendedCommunityGlobalAdministratorLowMatch(
            new IntComparison(IntComparator.EQ, new LiteralInt(3)));
    _exception.expect(UnsupportedOperationException.class);
    _communityMatchExprToBDD.visitExtendedCommunityGlobalAdministratorLowMatch(ec, _arg);
  }

  @Test
  public void testVisitExtendedCommunityGlobalAdministratorMatch() {
    ExtendedCommunityGlobalAdministratorMatch ec =
        new ExtendedCommunityGlobalAdministratorMatch(
            new LongComparison(IntComparator.EQ, new LiteralLong(3L)));
    _exception.expect(UnsupportedOperationException.class);
    _communityMatchExprToBDD.visitExtendedCommunityGlobalAdministratorMatch(ec, _arg);
  }

  @Test
  public void testVisitExtendedCommunityLocalAdministratorMatch() {
    ExtendedCommunityLocalAdministratorMatch ec =
        new ExtendedCommunityLocalAdministratorMatch(
            new IntComparison(IntComparator.EQ, new LiteralInt(3)));
    _exception.expect(UnsupportedOperationException.class);
    _communityMatchExprToBDD.visitExtendedCommunityLocalAdministratorMatch(ec, _arg);
  }

  @Test
  public void testVisitOpaqueExtendedCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitOpaqueExtendedCommunities(
            OpaqueExtendedCommunities.of(true, 3), _arg);

    assertEquals(cvarToBDD(CommunityVar.from(ExtendedCommunity.opaque(true, 3, 40))), result);
  }

  @Test
  public void testVisitRouteTargetExtendedCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitRouteTargetExtendedCommunities(
            RouteTargetExtendedCommunities.instance(), _arg);

    assertEquals(cvarToBDD(CommunityVar.from(ExtendedCommunity.target(1, 65555))), result);
  }

  @Test
  public void testVisitSiteOfOriginExtendedCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitSiteOfOriginExtendedCommunities(
            SiteOfOriginExtendedCommunities.instance(), _arg);

    assertEquals(cvarToBDD(CommunityVar.from(ExtendedCommunity.of(0x0003, 1, 1))), result);
  }

  @Test
  public void testVisitStandardCommunityHighMatch() {
    StandardCommunityHighMatch schm =
        new StandardCommunityHighMatch(new IntComparison(IntComparator.EQ, new LiteralInt(20)));

    BDD result = _communityMatchExprToBDD.visitStandardCommunityHighMatch(schm, _arg);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitStandardCommunityHighMatchUnsupported() {
    StandardCommunityHighMatch schm =
        new StandardCommunityHighMatch(new IntComparison(IntComparator.LT, new LiteralInt(20)));

    _exception.expect(UnsupportedOperationException.class);
    _communityMatchExprToBDD.visitStandardCommunityHighMatch(schm, _arg);
  }

  @Test
  public void testVisitStandardCommunityLowMatch() {
    StandardCommunityLowMatch sclm =
        new StandardCommunityLowMatch(new IntComparison(IntComparator.EQ, new LiteralInt(30)));

    BDD result = _communityMatchExprToBDD.visitStandardCommunityLowMatch(sclm, _arg);

    CommunityVar cvar = CommunityVar.from(":30$");

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitStandardCommunityLowMatchUnsupported() {
    StandardCommunityLowMatch sclm =
        new StandardCommunityLowMatch(new IntComparison(IntComparator.LT, new LiteralInt(20)));

    _exception.expect(UnsupportedOperationException.class);
    _communityMatchExprToBDD.visitStandardCommunityLowMatch(sclm, _arg);
  }

  @Test
  public void testVisitVPNDistinguisherExtendedCommunities() {
    BDD result =
        _communityMatchExprToBDD.visitVpnDistinguisherExtendedCommunities(
            VpnDistinguisherExtendedCommunities.instance(), _arg);

    assertEquals(cvarToBDD(CommunityVar.from(ExtendedCommunity.of(0x0010, 1, 1))), result);
  }

  private BDD cvarToBDD(CommunityVar cvar) {
    BDD[] aps = _arg.getBDDRoute().getCommunityAtomicPredicates();
    Map<CommunityVar, Set<Integer>> regexAtomicPredicates =
        _arg.getTransferBDD().getCommunityAtomicPredicates();
    return _arg.getTransferBDD()
        .getFactory()
        .orAll(
            regexAtomicPredicates.get(cvar).stream().map(i -> aps[i]).collect(Collectors.toList()));
  }
}
