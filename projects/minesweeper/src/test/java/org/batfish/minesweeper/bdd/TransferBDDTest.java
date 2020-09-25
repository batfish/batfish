package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighLowExprs;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.TransferParam;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TransferBDD}. */
public class TransferBDDTest {

  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private NetworkFactory _nf;
  private RoutingPolicy.Builder _policyBuilder;
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private Graph _g;
  private BDDRoute _anyRoute;

  private static final String COMMUNITY_NAME = "community";

  private static final String AS_PATH_NAME = "asPathName";

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
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = _nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME);

    _batfish = new MockBatfish(ImmutableSortedMap.of(HOSTNAME, _baseConfig));
    _anyRoute = new BDDRoute(1, 1);
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  @Test
  public void testCaptureAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the policy does not perform any updates
    assertEquals(_anyRoute, outAnnouncements);
  }

  @Test
  public void testCaptureNone() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to no announcements
    assertTrue(acceptedAnnouncements.isZero());

    // no routes are produced
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testEmptyPolicy() {

    _g = new Graph(_batfish, _batfish.getSnapshot(), true);
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, ImmutableList.of());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());

    // no routes are produced
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testOnePrefixRange() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testAcceptBeforeIf() {
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    new PrefixRange(Prefix.parse("1.0.0.0/32"), new SubRange(32, 32)))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(4)),
                Statements.ExitAccept.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    assertEquals(_anyRoute, outAnnouncements);
  }

  @Test
  public void testRejectBeforeIf() {
    _policyBuilder.addStatement(Statements.ExitReject.toStaticStatement());

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    new PrefixRange(Prefix.parse("1.0.0.0/32"), new SubRange(32, 32)))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(4)),
                Statements.ExitAccept.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());

    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testEarlyReturn() {
    _policyBuilder.addStatement(Statements.ReturnFalse.toStaticStatement());
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferResult result = tbdd.compute(ImmutableSet.of());
    TransferReturn ret = result.getReturnValue();
    BDD acceptedAnnouncements = ret.getSecond();
    BDDRoute outAnnouncements = ret.getFirst();

    assertTrue(acceptedAnnouncements.isZero());

    assertEquals(tbdd.zeroedRecord(), outAnnouncements);

    assertTrue(result.getExitAssignedValue().isZero());
    assertTrue(result.getReturnAssignedValue().isOne());
  }

  @Test
  public void testSetDefault() {
    _policyBuilder.addStatement(Statements.SetDefaultActionAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    assertEquals(_anyRoute, outAnnouncements);
  }

  @Test
  public void testPartialAccept() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement())));

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32)))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(3)),
                Statements.ExitAccept.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))
            .or(
                isRelevantFor(
                    _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32))));

    BDD reachesSecondIf =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));
    BDDRoute expected = tbdd.iteZero(expectedBDD, _anyRoute);
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(
        BDDInteger.makeFromValue(localPref.getFactory(), 32, 3).ite(reachesSecondIf, localPref));

    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testPartialReturn() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement())));

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32)))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(3)),
                Statements.ReturnFalse.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferResult result = tbdd.compute(ImmutableSet.of());
    TransferReturn ret = result.getReturnValue();

    BDD returnAssigned =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));

    BDD permitted =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)));

    assertEquals(returnAssigned, result.getReturnAssignedValue());

    assertEquals(returnAssigned.not(), result.getExitAssignedValue());

    assertEquals(permitted, ret.getSecond());
    // at the top level, the update to local preference is ignored since it only occurs on
    // paths that return false
    assertEquals(tbdd.iteZero(permitted, _anyRoute), ret.getFirst());
  }

  @Test
  public void testPartialReturnStatements() {
    List<Statement> stmts =
        ImmutableList.of(
            new If(
                matchPrefixSet(
                    ImmutableList.of(
                        new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
                ImmutableList.of(
                    new SetLocalPreference(new LiteralLong(2)),
                    Statements.ExitAccept.toStaticStatement())),
            new If(
                matchPrefixSet(
                    ImmutableList.of(
                        new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32)))),
                ImmutableList.of(
                    new SetLocalPreference(new LiteralLong(3)),
                    Statements.ReturnFalse.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    // indenting the parameter ensures that this will not be considered a top-level call,
    // so a default exit reject will not be added, and route updates on paths that return
    // false will not be removed
    TransferParam<BDDRoute> p = new TransferParam<>(new BDDRoute(_g), false).indent();
    TransferResult result = tbdd.compute(stmts, p);
    TransferReturn ret = result.getReturnValue();

    BDD returnAssigned =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));

    BDD exitAssigned =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)));

    BDDRoute expectedOut = new BDDRoute(_anyRoute);
    BDDInteger localPref2 = BDDInteger.makeFromValue(expectedOut.getFactory(), 32, 2);
    BDDInteger localPref3 = BDDInteger.makeFromValue(expectedOut.getFactory(), 32, 3);
    expectedOut.setLocalPref(
        localPref2.ite(exitAssigned, localPref3.ite(returnAssigned, expectedOut.getLocalPref())));

    assertEquals(returnAssigned, result.getReturnAssignedValue());

    assertEquals(exitAssigned, result.getExitAssignedValue());

    assertEquals(exitAssigned, ret.getSecond());

    assertEquals(expectedOut, ret.getFirst());
  }

  @Test
  public void testNestedIf() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
            ImmutableList.of(
                new If(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32)))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement())))));

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(32, 32)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testConjunction() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(
                ImmutableList.of(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(24, 32)))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(24, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testEmptyConjunction() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertEquals(acceptedAnnouncements, outAnnouncements.getFactory().one());

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testDisjunction() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(
                ImmutableList.of(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(24, 32)))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))
            .or(
                isRelevantFor(
                    _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(24, 32))));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testEmptyDisjunction() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertEquals(acceptedAnnouncements, outAnnouncements.getFactory().zero());

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testNot() {
    _policyBuilder.addStatement(
        new If(
            new Not(
                matchPrefixSet(
                    ImmutableList.of(
                        new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))
            .not();
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
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
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD1 =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    BDD expectedBDD2 =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(25, 32)));
    assertEquals(acceptedAnnouncements, expectedBDD1.or(expectedBDD2));

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
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
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(20, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testSetLocalPrefLiteral() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the local preference is now 42
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 42));
    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testSetMetric() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetMetric(new LiteralLong(50)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the metric is now 50, if the protocol is BGP
    BDDInteger expectedMed =
        BDDInteger.makeFromValue(_anyRoute.getFactory(), 32, 50)
            .ite(_anyRoute.getProtocolHistory().value(Protocol.BGP), _anyRoute.getMed());
    assertEquals(expectedMed, outAnnouncements.getMed());
  }

  @Test
  public void testBufferedStatement() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new BufferedStatement(new SetLocalPreference(new LiteralLong(42))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the local preference is now 42
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 42));
    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testMatchAsPath() {
    _baseConfig.setAsPathAccessLists(
        ImmutableMap.of(
            AS_PATH_NAME,
            new AsPathAccessList(
                AS_PATH_NAME,
                ImmutableList.of(
                    new AsPathAccessListLine(LineAction.PERMIT, " 40$"),
                    new AsPathAccessListLine(LineAction.DENY, "^$")))));
    _policyBuilder.addStatement(
        new If(
            new MatchAsPath(new NamedAsPathSet(AS_PATH_NAME)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRouteWithAPs = new BDDRoute(_g);
    BDD[] aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    // get the unique atomic predicate that corresponds to " 40$"
    Integer ap =
        _g.getAsPathRegexAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(new SymbolicAsPathRegex(" 40$"))
            .iterator()
            .next();

    BDD expectedBDD = aps[ap];
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRouteWithAPs), outAnnouncements);
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
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRoute3APs = new BDDRoute(_g);
    BDD[] aps = anyRoute3APs.getCommunityAtomicPredicates();

    // get the unique atomic predicates that respectively correspond to 20:30 and 30:30
    Integer ap2030 =
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from("^20:30$"))
            .iterator()
            .next();
    Integer ap3030 =
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from("^30:30$"))
            .iterator()
            .next();

    BDD expectedBDD = aps[ap2030].or(aps[ap3030]);
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRoute3APs), outAnnouncements);
  }

  @Test
  public void testDeleteCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new DeleteCommunity(new LiteralCommunity(StandardCommunity.parse("20:30"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 20:30 has the 0 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("20:30")))) {
      assertEquals(
          outAnnouncements.getFactory().zero(),
          outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testSetCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(StandardCommunity.parse("4:44"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testSetCommunityTwice() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(StandardCommunity.parse("3:33"))))
            .addStatement(new SetCommunity(new LiteralCommunity(StandardCommunity.parse("4:44"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
    // each atomic predicate for community 3:33 has the 0 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("3:33")))) {
      assertEquals(
          outAnnouncements.getFactory().zero(),
          outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testAddCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(StandardCommunity.parse("3:33"))))
            .addStatement(new AddCommunity(new LiteralCommunity(StandardCommunity.parse("4:44"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
    // each atomic predicate for community 3:33 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("3:33")))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testSetExtendedCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(ExtendedCommunity.parse("0:4:44"))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 0:4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(ExtendedCommunity.parse("0:4:44")))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testSetLargeCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetCommunity(new LiteralCommunity(LargeCommunity.of(10, 20, 30))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 0:4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(LargeCommunity.of(10, 20, 30)))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testSetCommunities() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    new CommunityExprsSet(
                        ImmutableSet.of(
                            new StandardCommunityHighLowExprs(
                                new LiteralInt(30), new LiteralInt(40)),
                            new StandardCommunityHighLowExprs(
                                new LiteralInt(40), new LiteralInt(40))))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    Map<CommunityVar, Set<Integer>> regexAPs =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    Set<Integer> aps3040 = regexAPs.get(CommunityVar.from(StandardCommunity.parse("30:40")));
    Set<Integer> aps4040 = regexAPs.get(CommunityVar.from(StandardCommunity.parse("40:40")));
    for (int i = 0; i < _g.getCommunityAtomicPredicates().getNumAtomicPredicates(); i++) {

      // each atomic predicate for 30:40 or 40:40 has the 1 BDD; all others have the 0 BDD
      assertEquals(
          aps3040.contains(i) || aps4040.contains(i)
              ? outAnnouncements.getFactory().one()
              : outAnnouncements.getFactory().zero(),
          outAnnouncements.getCommunityAtomicPredicates()[i]);
    }
  }

  @Test
  public void testMatchCommunities() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(),
                        new HasCommunity(
                            new StandardCommunityHighMatch(
                                new IntComparison(IntComparator.EQ, new LiteralInt(30))))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRoute = new BDDRoute(_g);
    BDD[] aps = anyRoute.getCommunityAtomicPredicates();

    // get the atomic predicates that correspond to ^30:
    Set<Integer> ap30 =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates().get(CommunityVar.from("^30:"));

    BDD expectedBDD =
        BDDRoute.factory.orAll(ap30.stream().map(i -> aps[i]).collect(Collectors.toList()));
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRoute), outAnnouncements);
  }

  @Test
  public void testMatchCommunityInHalves() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(),
                        new HasCommunity(
                            new CommunityMatchAll(
                                ImmutableList.of(
                                    new StandardCommunityHighMatch(
                                        new IntComparison(IntComparator.EQ, new LiteralInt(30))),
                                    new StandardCommunityLowMatch(
                                        new IntComparison(
                                            IntComparator.EQ, new LiteralInt(20))))))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRoute = new BDDRoute(_g);
    BDD[] aps = anyRoute.getCommunityAtomicPredicates();

    // only the APs that belong to both ^30: and :20$ should be allowed
    Set<Integer> ap30 =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates().get(CommunityVar.from("^30:"));
    Set<Integer> ap20 =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates().get(CommunityVar.from(":20$"));

    Set<Integer> intersection = new HashSet<>(ap30);
    intersection.retainAll(ap20);

    BDD expectedBDD =
        BDDRoute.factory.orAll(intersection.stream().map(i -> aps[i]).collect(Collectors.toList()));
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRoute), outAnnouncements);
  }

  @Test
  public void testCallStatement() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(new SetLocalPreference(new LiteralLong(300L)))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new CallStatement(calledPolicyName))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute expectedOut = new BDDRoute(_anyRoute);
    expectedOut.setLocalPref(BDDInteger.makeFromValue(expectedOut.getFactory(), 32, 300));

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());
    assertEquals(outAnnouncements, expectedOut);
  }

  @Test
  public void testNestedCalls() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(new SetLocalPreference(new LiteralLong(300L)))
            .build();

    String calledPolicyName2 = "calledPolicy2";

    RoutingPolicy calledPolicy2 =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
                    ImmutableList.of(
                        new CallStatement(calledPolicyName),
                        new StaticStatement(Statements.ExitAccept))))
            .build();

    RoutingPolicy policy =
        _policyBuilder.addStatement(new CallStatement(calledPolicyName2)).build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(
            calledPolicyName2, calledPolicy2, calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expected =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)));
    BDDRoute expectedOut = new BDDRoute(_anyRoute);
    expectedOut.setLocalPref(BDDInteger.makeFromValue(expectedOut.getFactory(), 32, 300));
    expectedOut = tbdd.ite(expected, expectedOut, tbdd.zeroedRecord());

    // the policy is applicable to all announcements
    assertEquals(acceptedAnnouncements, expected);
    assertEquals(outAnnouncements, expectedOut);
  }

  @Test
  public void testCallStatementEarlyExit() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new CallStatement(calledPolicyName))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot(), true);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();

    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // no accepted routes because the callee uniformly rejects
    assertTrue(acceptedAnnouncements.isZero());
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
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
