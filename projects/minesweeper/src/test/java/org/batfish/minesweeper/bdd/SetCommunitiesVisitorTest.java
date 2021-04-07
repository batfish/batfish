package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighLowExprs;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SetCommunitiesVisitor}. */
public class SetCommunitiesVisitorTest {
  private static final String HOSTNAME = "hostname";
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private Graph _g;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private SetCommunitiesVisitor _scVisitor;

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
                new LiteralCommunity(StandardCommunity.parse("20:30")),
                new LiteralCommunity(StandardCommunity.parse("21:30"))),
            null);
    BDDRoute bddRoute = new BDDRoute(_g);
    TransferBDD transferBDD = new TransferBDD(_g, _baseConfig, ImmutableList.of());
    _arg = new CommunitySetMatchExprToBDD.Arg(transferBDD, bddRoute);

    _scVisitor = new SetCommunitiesVisitor();
  }

  @Test
  public void testVisitCommunityExprsSet() {
    CommunityExprsSet ces =
        CommunityExprsSet.of(
            new StandardCommunityHighLowExprs(new LiteralInt(20), new LiteralInt(30)));

    CommunityVar cvar2030 = CommunityVar.from(StandardCommunity.parse("20:30"));

    CommunityAPDispositions result = _scVisitor.visitCommunityExprsSet(ces, _arg);

    Map<CommunityVar, Set<Integer>> commAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    assertEquals(
        CommunityAPDispositions.exactly(commAPs.get(cvar2030), _arg.getBDDRoute()), result);
  }

  @Test
  public void testVisitCommunitySetDifference() {
    CommunitySetDifference csd =
        new CommunitySetDifference(
            new LiteralCommunitySet(
                CommunitySet.of(
                    StandardCommunity.parse("20:30"), StandardCommunity.parse("21:30"))),
            new CommunityIs(StandardCommunity.parse("21:30")));

    CommunityVar cvar2030 = CommunityVar.from(StandardCommunity.parse("20:30"));

    CommunityAPDispositions result = _scVisitor.visitCommunitySetDifference(csd, _arg);

    Map<CommunityVar, Set<Integer>> commAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    assertEquals(
        CommunityAPDispositions.exactly(commAPs.get(cvar2030), _arg.getBDDRoute()), result);
  }

  @Test
  public void testVisitCommunitySetExprReference() {
    String name = "name";
    _baseConfig.setCommunitySetExprs(
        ImmutableMap.of(
            name, new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30")))));
    CommunitySetExprReference cser = new CommunitySetExprReference(name);

    CommunityVar cvar2030 = CommunityVar.from(StandardCommunity.parse("20:30"));

    CommunityAPDispositions result = _scVisitor.visitCommunitySetExprReference(cser, _arg);

    Map<CommunityVar, Set<Integer>> commAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    assertEquals(
        CommunityAPDispositions.exactly(commAPs.get(cvar2030), _arg.getBDDRoute()), result);
  }

  @Test
  public void testVisitCommunitySetReference() {
    String name = "name";
    _baseConfig.setCommunitySets(
        ImmutableMap.of(name, CommunitySet.of(StandardCommunity.parse("20:30"))));
    CommunitySetReference csr = new CommunitySetReference(name);

    CommunityVar cvar2030 = CommunityVar.from(StandardCommunity.parse("20:30"));

    CommunityAPDispositions result = _scVisitor.visitCommunitySetReference(csr, _arg);

    Map<CommunityVar, Set<Integer>> commAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    assertEquals(
        CommunityAPDispositions.exactly(commAPs.get(cvar2030), _arg.getBDDRoute()), result);
  }

  @Test
  public void testVisitCommunitySetUnion() {
    CommunitySetUnion csu =
        CommunitySetUnion.of(
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30"))),
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("21:30"))));

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("21:30"));

    CommunityAPDispositions result = _scVisitor.visitCommunitySetUnion(csu, _arg);

    Map<CommunityVar, Set<Integer>> commAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    assertEquals(
        CommunityAPDispositions.exactly(
            ImmutableSet.<Integer>builder()
                .addAll(commAPs.get(cvar1))
                .addAll(commAPs.get(cvar2))
                .build(),
            _arg.getBDDRoute()),
        result);
  }

  @Test
  public void testVisitInputCommunities() {
    CommunityAPDispositions result =
        _scVisitor.visitInputCommunities(InputCommunities.instance(), _arg);
    assertEquals(new CommunityAPDispositions(ImmutableSet.of(), ImmutableSet.of()), result);
  }

  @Test
  public void testVisitLiteralCommunitySet() {
    LiteralCommunitySet lcs =
        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30")));

    CommunityVar cvar2030 = CommunityVar.from(StandardCommunity.parse("20:30"));

    CommunityAPDispositions result = _scVisitor.visitLiteralCommunitySet(lcs, _arg);

    Map<CommunityVar, Set<Integer>> commAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    assertEquals(
        CommunityAPDispositions.exactly(commAPs.get(cvar2030), _arg.getBDDRoute()), result);
  }
}
