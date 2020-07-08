package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.minesweeper.Graph;
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

    _batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return ImmutableSortedMap.of(HOSTNAME, _baseConfig);
          }

          @Override
          public TopologyProvider getTopologyProvider() {
            return new TopologyProviderTestAdapter(_batfish) {
              @Override
              public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
                return Topology.EMPTY;
              }
            };
          }
        };

    _g = new Graph(_batfish, null, ImmutableMap.of(HOSTNAME, _baseConfig));
    _pq = new PolicyQuotient(_g);
    _anyRoute = new BDDRoute(ImmutableSet.of());
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  // TODO: Test a case where the input and output routes have a dependency on the value for some
  // field like a community or local pref.  How is
  // that represented in the final BDD Route?  E.g., if the input community is 3 then change it to
  // 4.

  @Test
  public void testCaptureAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

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

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(20, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), announcementUpdates);
  }

  /**
   * ** @Test public void testMatchLocalPrefNotSupported() { _policyBuilder.addStatement( new If(
   * new MatchLocalPreference(IntComparator.EQ, new LiteralInt(3)), ImmutableList.of(new
   * StaticStatement(Statements.ExitAccept)))); RoutingPolicy policy = _policyBuilder.build();
   *
   * <p>TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq); try {
   * tbdd.compute(ImmutableSet.of()).getReturnValue(); // BDD acceptedAnnouncements =
   * result.getSecond(); // BDDRoute announcementUpdates = result.getFirst(); fail(); } catch
   * (BatfishException ignored) { } } @Test public void testMatchLiteralCommunityNotSupported() {
   * _policyBuilder.addStatement( new If( new MatchCommunitySet(new
   * LiteralCommunity(StandardCommunity.of(23))), ImmutableList.of(new
   * StaticStatement(Statements.ExitAccept)))); RoutingPolicy policy = _policyBuilder.build();
   *
   * <p>TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
   *
   * <p>try { TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue(); // BDD
   * acceptedAnnouncements = result.getSecond(); // BDDRoute announcementUpdates =
   * result.getFirst(); fail(); } catch (BatfishException ignored) { } }
   *
   * <p>// TODO: some version of this should work, but we seem to need to declare the community list
   * first // in the config @Test public void testMatchCommunityList() {
   * _baseConfig.setCommunityLists( ImmutableMap.of( "clist", new CommunityList( "clist",
   * ImmutableList.of( CommunityListLine.accepting(new LiteralCommunity(StandardCommunity.of(23)))),
   * false))); _g = new Graph(_batfish, null, ImmutableMap.of(HOSTNAME, _baseConfig));
   * _policyBuilder.addStatement( new If( new MatchCommunitySet(new NamedCommunitySet("clist")),
   * ImmutableList.of(new StaticStatement(Statements.ExitAccept)))); RoutingPolicy policy =
   * _policyBuilder.build();
   *
   * <p>TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq); try {
   * TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue(); // BDD
   * acceptedAnnouncements = result.getSecond(); // BDDRoute announcementUpdates =
   * result.getFirst(); fail(); } catch (Exception ignored) { } }
   */
  @Test
  public void testSetLocalPrefLiteral() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the local preference is now 42
    BDDRoute expected = _anyRoute;
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 42));
    assertEquals(expected, announcementUpdates);
  }

  @Test
  public void testConditionalSetLocalPref() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(42)),
                new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    // the local preference is 42 when the prefix is in the specified range
    BDDRoute prefRoute = _anyRoute;
    BDDInteger localPref = prefRoute.getLocalPref();
    prefRoute.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 42));
    BDDRoute expected = tbdd.iteZero(expectedBDD, prefRoute);
    assertEquals(expected, announcementUpdates);
  }

  @Test
  public void testConditionalSetLocalPref2() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(42)),
                new StaticStatement(Statements.ExitAccept)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute announcementUpdates = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    BDD matchRoute = isRelevantFor(_anyRoute, PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")));

    // the local preference is 42 when the prefix is in the specified range
    BDDRoute prefRoute = new BDDRoute(ImmutableSet.of());
    BDDInteger localPref = prefRoute.getLocalPref();
    prefRoute.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 42));
    BDDRoute expected = tbdd.ite(matchRoute, prefRoute, _anyRoute);
    assertEquals(expected, announcementUpdates);
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
