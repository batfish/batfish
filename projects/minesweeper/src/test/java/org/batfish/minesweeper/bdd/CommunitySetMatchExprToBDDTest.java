package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.communities.TypesFirstAscendingSpaceSeparated;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.ConfigAtomicPredicatesTestUtils;
import org.batfish.minesweeper.bdd.TransferBDD.Context;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD} */
public class CommunitySetMatchExprToBDDTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private ConfigAtomicPredicates _configAPs;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private CommunitySetMatchExprToBDD _communitySetMatchExprToBDD;

  @Rule public ExpectedException _expectedException = ExpectedException.none();

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

    _configAPs =
        ConfigAtomicPredicatesTestUtils.forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            ImmutableSet.of(
                CommunityVar.from("^20:"),
                CommunityVar.from(":30$"),
                CommunityVar.from(StandardCommunity.parse("20:30")),
                CommunityVar.from(StandardCommunity.parse("21:30"))),
            null);
    TransferBDD transferBDD = new TransferBDD(_configAPs);
    RoutingPolicy testPolicy =
        nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME).build();
    BDDRoute bddRoute = new BDDRoute(transferBDD.getFactory(), _configAPs);
    _arg = new CommunitySetMatchExprToBDD.Arg(transferBDD, bddRoute, Context.forPolicy(testPolicy));

    _communitySetMatchExprToBDD = new CommunitySetMatchExprToBDD();
  }

  @Test
  public void testVisitCommunitySetAcl() {
    CommunitySetAcl csacl =
        new CommunitySetAcl(
            ImmutableList.of(
                new CommunitySetAclLine(
                    LineAction.DENY,
                    new HasCommunity(new CommunityIs(StandardCommunity.parse("20:30")))),
                new CommunitySetAclLine(
                    LineAction.PERMIT,
                    new HasCommunity(
                        new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:")))));

    BDD result = _communitySetMatchExprToBDD.visitCommunitySetAcl(csacl, _arg);

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from("^20:");

    assertEquals(cvarToBDD(cvar1).not().and(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunitySetHasSize() {
    List<HasSize> treatFalse =
        ImmutableList.of(
            new HasSize(new IntComparison(IntComparator.LT, new LiteralInt(5))),
            new HasSize(new IntComparison(IntComparator.LT, new LiteralInt(64))),
            new HasSize(new IntComparison(IntComparator.LE, new LiteralInt(5))),
            new HasSize(new IntComparison(IntComparator.LE, new LiteralInt(63))),
            new HasSize(new IntComparison(IntComparator.GT, new LiteralInt(5))),
            new HasSize(new IntComparison(IntComparator.GE, new LiteralInt(5))));
    List<HasSize> treatTrue =
        ImmutableList.of(
            new HasSize(new IntComparison(IntComparator.LT, new LiteralInt(100))),
            new HasSize(new IntComparison(IntComparator.LT, new LiteralInt(65))),
            new HasSize(new IntComparison(IntComparator.LE, new LiteralInt(64))),
            new HasSize(new IntComparison(IntComparator.LE, new LiteralInt(500))),
            new HasSize(new IntComparison(IntComparator.GT, new LiteralInt(-5))),
            new HasSize(new IntComparison(IntComparator.GE, new LiteralInt(0))));

    for (HasSize hs : treatFalse) {
      assertThat(_communitySetMatchExprToBDD.visitHasSize(hs, _arg), isZero());
    }
    for (HasSize hs : treatTrue) {
      assertThat(_communitySetMatchExprToBDD.visitHasSize(hs, _arg), isOne());
    }

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            _communitySetMatchExprToBDD.visitHasSize(
                new HasSize(new IntComparison(IntComparator.EQ, new LiteralInt(5))), _arg));
  }

  @Test
  public void testVisitCommunitySetMatchAll() {
    CommunitySetMatchAll csma =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new HasCommunity(
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:")),
                new HasCommunity(
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":30$"))));

    BDD result = _communitySetMatchExprToBDD.visitCommunitySetMatchAll(csma, _arg);

    CommunityVar cvar1 = CommunityVar.from("^20:");
    CommunityVar cvar2 = CommunityVar.from(":30$");

    assertEquals(cvarToBDD(cvar1).and(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunitySetMatchAny() {
    CommunitySetMatchAny csma =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new HasCommunity(
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:")),
                new HasCommunity(
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":30$"))));

    BDD result = _communitySetMatchExprToBDD.visitCommunitySetMatchAny(csma, _arg);

    CommunityVar cvar1 = CommunityVar.from("^20:");
    CommunityVar cvar2 = CommunityVar.from(":30$");

    assertEquals(cvarToBDD(cvar1).or(cvarToBDD(cvar2)), result);
  }

  @Test
  public void testVisitCommunitySetMatchExprReference() {
    String name = "name";
    _baseConfig.setCommunitySetMatchExprs(
        ImmutableMap.of(
            name,
            new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"))));
    CommunitySetMatchExprReference csmer = new CommunitySetMatchExprReference(name);

    BDD result = _communitySetMatchExprToBDD.visitCommunitySetMatchExprReference(csmer, _arg);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitCommunitySetMatchRegex() {
    CommunitySetMatchRegex csmr =
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()),
            "^65000:123 65011:12[3]$");
    _expectedException.expect(UnsupportedOperationException.class);
    _communitySetMatchExprToBDD.visitCommunitySetMatchRegex(csmr, _arg);
  }

  @Test
  public void testVisitCommunityNot() {
    HasCommunity hc =
        new HasCommunity(new CommunityNot(new CommunityIs(StandardCommunity.parse("20:30"))));

    BDD result = _communitySetMatchExprToBDD.visitHasCommunity(hc, _arg);
    BDD[] aps = _arg.getBDDRoute().getCommunityAtomicPredicates();
    Set<Integer> notCommunityAps =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("20:30")));
    BDD expected =
        result
            .getFactory()
            .orAll(
                IntStream.range(0, aps.length)
                    .filter(i -> !notCommunityAps.contains(i))
                    .mapToObj(i -> aps[i])
                    .toList());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunitySetNot() {
    CommunitySetNot csn =
        new CommunitySetNot(new HasCommunity(new CommunityIs(StandardCommunity.parse("20:30"))));

    BDD result = _communitySetMatchExprToBDD.visitCommunitySetNot(csn, _arg);

    CommunityVar cvar = CommunityVar.from(StandardCommunity.parse("20:30"));

    assertEquals(cvarToBDD(cvar).not(), result);
  }

  @Test
  public void testVisitHasCommunity() {
    HasCommunity hc =
        new HasCommunity(
            new CommunityMatchAll(
                ImmutableList.of(
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"),
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":30$"))));

    BDD result = _communitySetMatchExprToBDD.visitHasCommunity(hc, _arg);

    CommunityVar cvar = CommunityVar.from(StandardCommunity.parse("20:30"));

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitHasCommunity2() {
    HasCommunity hc =
        new HasCommunity(
            new CommunityAcl(
                ImmutableList.of(
                    new CommunityAclLine(
                        LineAction.DENY, new CommunityIs(StandardCommunity.parse("20:30"))),
                    new CommunityAclLine(
                        LineAction.PERMIT, new CommunityIs(StandardCommunity.parse("21:30"))))));

    BDD result = _communitySetMatchExprToBDD.visitHasCommunity(hc, _arg);

    CommunityVar cvar = CommunityVar.from(StandardCommunity.parse("21:30"));

    assertEquals(cvarToBDD(cvar), result);
  }

  @Test
  public void testVisitHasSize() {
    HasSize hs = new HasSize(new IntComparison(IntComparator.EQ, new LiteralInt(32)));
    _expectedException.expect(UnsupportedOperationException.class);
    _communitySetMatchExprToBDD.visitHasSize(hs, _arg);
  }

  private BDD cvarToBDD(CommunityVar cvar) {
    BDD[] aps = _arg.getBDDRoute().getCommunityAtomicPredicates();
    Map<CommunityVar, Set<Integer>> regexAtomicPredicates =
        _configAPs.getStandardCommunityAtomicPredicates().getRegexAtomicPredicates();
    return _arg.getTransferBDD()
        .getFactory()
        .orAll(
            regexAtomicPredicates.get(cvar).stream().map(i -> aps[i]).collect(Collectors.toList()));
  }
}
