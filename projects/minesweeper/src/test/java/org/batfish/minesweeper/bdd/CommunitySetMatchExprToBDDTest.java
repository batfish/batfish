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
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD} */
public class CommunitySetMatchExprToBDDTest {
  private static final String HOSTNAME = "hostname";
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private Graph _g;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private CommunitySetMatchExprToBDD _communitySetMatchExprToBDD;

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

  private BDD cvarToBDD(CommunityVar cvar) {
    BDD[] aps = _arg.getBDDRoute().getCommunityAtomicPredicates();
    Map<CommunityVar, Set<Integer>> regexAtomicPredicates =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    return BDDRoute.factory.orAll(
        regexAtomicPredicates.get(cvar).stream().map(i -> aps[i]).collect(Collectors.toList()));
  }
}
