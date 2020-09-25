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
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.bgp.community.StandardCommunity;
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
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link org.batfish.minesweeper.bdd.CommunityMatchExprToBDD}. */
public class CommunityMatchExprToBDDTest {
  private static final String HOSTNAME = "hostname";
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private Graph _g;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private CommunityMatchExprToBDD _communityMatchExprToBDD;

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

    _g =
        new Graph(
            _batfish,
            _batfish.getSnapshot(),
            null,
            null,
            ImmutableSet.of(
                new RegexCommunitySet("^20:"),
                new RegexCommunitySet(":30$"),
                new LiteralCommunity(StandardCommunity.parse("20:30")),
                new LiteralCommunity(StandardCommunity.parse("21:30"))),
            null,
            true);
    BDDRoute bddRoute = new BDDRoute(_g);
    TransferBDD transferBDD = new TransferBDD(_g, _baseConfig, ImmutableList.of());
    _arg = new CommunitySetMatchExprToBDD.Arg(transferBDD, bddRoute);

    _communityMatchExprToBDD = new CommunityMatchExprToBDD();
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

    CommunityVar cvar1 = CommunityVar.from(".*");
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("20:30"));

    assertEquals(cvarToBDD(cvar1).diff(cvarToBDD(cvar2)), result);
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
  public void testVisitStandardCommunityLowMatch() {
    StandardCommunityLowMatch sclm =
        new StandardCommunityLowMatch(new IntComparison(IntComparator.EQ, new LiteralInt(30)));

    BDD result = _communityMatchExprToBDD.visitStandardCommunityLowMatch(sclm, _arg);

    CommunityVar cvar = CommunityVar.from(":30$");

    assertEquals(cvarToBDD(cvar), result);
  }

  private BDD cvarToBDD(CommunityVar cvar) {
    BDD[] aps = _arg.getBDDRoute().getCommunityAtomicPredicates();
    Map<CommunityVar, Set<Integer>> regexAtomicPredicates =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    return BDDRoute.factory.orAll(
        regexAtomicPredicates.get(cvar).stream().map(i -> aps[i]).collect(Collectors.toList()));
  }
}
