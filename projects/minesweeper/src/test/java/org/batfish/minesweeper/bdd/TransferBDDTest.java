package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TransferBDD}. */
public class TransferBDDTest {

  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private RoutingPolicy.Builder _policyBuilder;
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private Graph _g;
  private PolicyQuotient _pq;
  private BDDRoute _anyRoute;

  private static final String COMMUNITY_NAME = "community";

  static final class MockBatfish extends IBatfishTestAdapter {
    private final SortedMap<String, Configuration> _baseConfigs;

    MockBatfish(SortedMap<String, Configuration> baseConfigs) {
      _baseConfigs = ImmutableSortedMap.copyOf(baseConfigs);
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
      if (getSnapshot().equals(snapshot)) {
        return _baseConfigs;
      }
      throw new IllegalArgumentException("Unknown snapshot: " + snapshot);
    }

    @Override
    public TopologyProvider getTopologyProvider() {
      return new TopologyProviderTestAdapter(this) {
        @Override
        public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
          return Topology.EMPTY;
        }
      };
    }

    @Override
    public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
      return ImmutableMap.of();
    }
  }

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME);

    _batfish = new MockBatfish(ImmutableSortedMap.of(HOSTNAME, _baseConfig));
    _anyRoute = new BDDRoute(0);

    _pq = new PolicyQuotient();
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  @Test
  public void testCaptureAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the policy does not perform any updates
    assertEquals(_anyRoute, announcementUpdates);
  }

  @Test
  public void testCaptureNone() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to no announcements
    assertTrue(acceptedAnnouncements.isZero());

    // no routes are produced
    assertEquals(tbdd.zeroedRecord(), announcementUpdates);
  }

  @Test
  public void testEmptyPolicy() {

    _g = new Graph(_batfish, _batfish.getSnapshot());
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, ImmutableList.of(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());

    // no routes are produced
    assertEquals(tbdd.zeroedRecord(), announcementUpdates);
  }

  @Test
  public void testOnePrefixRange() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), announcementUpdates);
  }

  @Test
  public void testTwoPrefixes() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)),
                    new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(20, 32)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    BDD expectedBDD1 =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    BDD expectedBDD2 =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(25, 32)));
    assertEquals(acceptedAnnouncements, expectedBDD1.or(expectedBDD2));

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), announcementUpdates);
  }

  @Test
  public void testPrefixIntersection() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(
                ImmutableList.of(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(20, 32)))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(20, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), announcementUpdates);
  }

  @Test
  public void testSetLocalPrefLiteral() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the local preference is now 42
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 42));
    assertEquals(expected, announcementUpdates);
  }

  @Test
  public void testMatchCommunity() {
    _baseConfig.setCommunityLists(
        ImmutableMap.of(
            COMMUNITY_NAME,
            new CommunityList(
                COMMUNITY_NAME,
                ImmutableList.of(
                    CommunityListLine.accepting(
                        new LiteralCommunity(StandardCommunity.parse("20:30"))),
                    CommunityListLine.accepting(
                        new LiteralCommunity(StandardCommunity.parse("30:30")))),
                false)));
    _policyBuilder.addStatement(
        new If(
            new MatchCommunitySet(new NamedCommunitySet(COMMUNITY_NAME)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    BDDRoute anyRoute2APs = new BDDRoute(2);
    BDD[] aps = anyRoute2APs.getCommunityAtomicPredicateBDDs();

    BDD expectedBDD = aps[0].or(aps[1]);
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRoute2APs), announcementUpdates);
  }

  @Test
  public void testDeleteCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new DeleteCommunity(new LiteralCommunity(StandardCommunity.parse("20:30"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 20:30 has the 0 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("20:30")))) {
      assertEquals(
          announcementUpdates.getFactory().zero(),
          announcementUpdates.getCommunityAtomicPredicateBDDs()[ap]);
    }
  }

  @Test
  public void testSetCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(StandardCommunity.parse("4:44"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates().get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      assertEquals(
          announcementUpdates.getFactory().one(),
          announcementUpdates.getCommunityAtomicPredicateBDDs()[ap]);
    }
  }

  @Test
  public void testSetExtendedCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(ExtendedCommunity.parse("0:4:44"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 0:4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .get(CommunityVar.from(ExtendedCommunity.parse("0:4:44")))) {
      assertEquals(
          announcementUpdates.getFactory().one(),
          announcementUpdates.getCommunityAtomicPredicateBDDs()[ap]);
    }
  }

  @Test
  public void testIsRelevantFor() {
    BDDRoute bddRoute = _anyRoute;
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(bddRoute.getPrefix());

    PrefixRange range = new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24));
    BDD rangeBdd = isRelevantFor(bddRoute, range);
    BDD notRangeBdd = rangeBdd.not();

    BDD matchingPrefix = ipSpaceToBDD.toBDD(Ip.parse("1.1.1.1"));
    BDD nonMatchingPrefix = ipSpaceToBDD.toBDD(Ip.parse("2.2.2.2"));
    BDD len24 = bddRoute.getPrefixLength().value(24);
    BDD len8 = bddRoute.getPrefixLength().value(8);
    BDD len32 = bddRoute.getPrefixLength().value(32);

    assertTrue(matchingPrefix.and(len24).imp(rangeBdd).isOne());
    assertTrue(matchingPrefix.and(len32).imp(notRangeBdd).isOne()); // prefix too long
    assertTrue(matchingPrefix.and(len8).imp(notRangeBdd).isOne()); // prefix too short
    assertTrue(nonMatchingPrefix.and(len24).imp(notRangeBdd).isOne()); // prefix doesn't match
  }

  @Test
  public void testIsRelevantFor_range32() {
    BDDRoute bddRoute = _anyRoute;

    PrefixRange range = new PrefixRange(Prefix.parse("0.0.0.0/0"), SubRange.singleton(32));
    BDD rangeBdd = isRelevantFor(bddRoute, range);
    BDD len0 = bddRoute.getPrefixLength().value(0);
    BDD len32 = bddRoute.getPrefixLength().value(32);

    assertTrue(len0.imp(rangeBdd.not()).isOne());
    assertTrue(len32.imp(rangeBdd).isOne());
  }
}
