package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantForDestination;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighLowExprs;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchIpv6;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RegexAsPathSetElem;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetDefaultTag;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

  @Rule public ExpectedException _exception = ExpectedException.none();

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
    setup(ConfigurationFormat.CISCO_IOS);
  }

  public void setup(ConfigurationFormat format) {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder().setHostname(HOSTNAME).setConfigurationFormat(format);
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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

    _g = new Graph(_batfish, _batfish.getSnapshot());
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
  public void testSuppress() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
            ImmutableList.of(Statements.Suppress.toStaticStatement())));
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferResult result = tbdd.compute(ImmutableSet.of());
    TransferReturn ret = result.getReturnValue();
    BDD acceptedAnnouncements = ret.getSecond();
    BDDRoute outAnnouncements = ret.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
                _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))
            .not();
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(expectedBDD, _anyRoute), outAnnouncements);
  }

  @Test
  public void testUnsuppress() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32)))),
            ImmutableList.of(Statements.Suppress.toStaticStatement())));
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))),
            ImmutableList.of(Statements.Unsuppress.toStaticStatement())));

    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
                _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))
            .or(
                isRelevantForDestination(
                        _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32)))
                    .not());

    BDDRoute expected = tbdd.iteZero(expectedBDD, _anyRoute);

    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testSetDefault() {
    _policyBuilder.addStatement(Statements.SetDefaultActionAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
                _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)))
            .or(
                isRelevantForDestination(
                    _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 32))));

    BDD reachesSecondIf =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferResult result = tbdd.compute(ImmutableSet.of());
    TransferReturn ret = result.getReturnValue();

    BDD returnAssigned =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));

    BDD permitted =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)));

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    // indenting the parameter ensures that this will not be considered a top-level call,
    // so a default exit reject will not be added, and route updates on paths that return
    // false will not be removed
    TransferParam<BDDRoute> p = new TransferParam<>(new BDDRoute(_g), false).indent();
    TransferResult result = tbdd.compute(stmts, p);
    TransferReturn ret = result.getReturnValue();

    BDD returnAssigned =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));

    BDD exitAssigned =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)));

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(32, 32)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testIfFalse() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    BooleanExprs.FALSE,
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testMatchIpv4() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    MatchIpv4.instance(),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // currently we assume announcements are IPv4
    assertTrue(acceptedAnnouncements.isOne());
    assertEquals(_anyRoute, outAnnouncements);
  }

  @Test
  public void testMatchIpv6() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    MatchIpv6.instance(),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(24, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testStatefulConjunction() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    BooleanExprs.TRUE,
                    ImmutableList.of(new SetLocalPreference(new LiteralLong(300L)))))
            .addStatement(new StaticStatement(Statements.ReturnTrue))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(new CallExpr(calledPolicyName), BooleanExprs.TRUE)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
  public void testUnreachableStatefulConjunction() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    BooleanExprs.TRUE,
                    ImmutableList.of(new SetLocalPreference(new LiteralLong(300L)))))
            .addStatement(new StaticStatement(Statements.ReturnTrue))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new StaticStatement(Statements.ExitReject))
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(new CallExpr(calledPolicyName), BooleanExprs.TRUE)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());
    assertEquals(outAnnouncements, tbdd.zeroedRecord());
  }

  @Test
  public void testConjunctionShortCircuit() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(new SetLocalPreference(new LiteralLong(300L)))
            .addStatement(new StaticStatement(Statements.ReturnTrue))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            matchPrefixSet(
                                ImmutableList.of(
                                    new PrefixRange(
                                        Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                            new CallExpr(calledPolicyName))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    BDD firstConjunctBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(
        BDDInteger.makeFromValue(localPref.getFactory(), 32, 300).ite(firstConjunctBDD, localPref));

    // the updates in the second conjunct should not occur unless the first conjunct is true
    assertEquals(outAnnouncements, expected);
  }

  @Test
  public void testEmptyConjunction() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
                _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))
            .or(
                isRelevantForDestination(
                    _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(24, 32))));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testDisjunctionShortCircuit() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(new SetLocalPreference(new LiteralLong(300L)))
            .addStatement(new StaticStatement(Statements.ReturnTrue))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new Disjunction(
                        ImmutableList.of(
                            matchPrefixSet(
                                ImmutableList.of(
                                    new PrefixRange(
                                        Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                            new CallExpr(calledPolicyName))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    BDD firstDisjunctBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(
        BDDInteger.makeFromValue(localPref.getFactory(), 32, 300)
            .ite(firstDisjunctBDD.not(), localPref));

    // the updates in the second disjunct should not occur unless the first disjunct is false
    assertEquals(outAnnouncements, expected);
  }

  @Test
  public void testEmptyDisjunction() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
                _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD1 =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    BDD expectedBDD2 =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(25, 32)));
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.2.0.0/16"), new SubRange(20, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testMatchEmptyPrefixSet() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testMatchNamedPrefixSet() {
    String name = "name";
    _baseConfig.setRouteFilterLists(
        ImmutableMap.of(
            name,
            new RouteFilterList(
                name,
                ImmutableList.of(
                    new RouteFilterLine(LineAction.DENY, Prefix.ZERO, new SubRange(32, 32)),
                    new RouteFilterLine(
                        LineAction.PERMIT, Prefix.parse("1.0.0.0/8"), new SubRange(31, 32))))));
    _policyBuilder.addStatement(
        new If(
            new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(name)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(31, 31)));
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the metric is now 50
    BDDInteger expectedMed = BDDInteger.makeFromValue(_anyRoute.getFactory(), 32, 50);
    assertEquals(expectedMed, outAnnouncements.getMed());
  }

  @Test
  public void testSetTag() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetTag(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the tag is now 42
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger tag = expected.getTag();
    expected.setTag(BDDInteger.makeFromValue(tag.getFactory(), 32, 42));
    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testMatchTag() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchTag(IntComparator.EQ, new LiteralLong(42)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertEquals(acceptedAnnouncements, _anyRoute.getTag().value(42));
    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testSetThenMatchTag() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetTag(new LiteralLong(42)))
            .addStatement(
                new If(
                    new MatchTag(IntComparator.EQ, new LiteralLong(42)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();
    // the original route's tag must be 42
    assertEquals(acceptedAnnouncements, _anyRoute.getTag().value(42));
    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);

    // now do the analysis again but use the output attributes for matching
    tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), true);
    result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    acceptedAnnouncements = result.getSecond();
    outAnnouncements = result.getFirst();
    // the original route's tag is irrelevant
    assertTrue(acceptedAnnouncements.isOne());
    // the tag is now 42
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger tag = expected.getTag();
    expected.setTag(BDDInteger.makeFromValue(tag.getFactory(), 32, 42));
    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testMatchTagLT() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchTag(IntComparator.LT, new LiteralLong(2)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertEquals(
        acceptedAnnouncements, _anyRoute.getTag().value(0).or(_anyRoute.getTag().value(1)));
    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testMatchNextHop() {
    _policyBuilder.addStatement(
        new If(
            new MatchPrefixSet(
                new IpPrefix(NextHopIp.instance(), new LiteralInt(Prefix.MAX_PREFIX_LENGTH)),
                new ExplicitPrefixSet(
                    new PrefixSpace(
                        ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        TransferBDD.firstBitsEqual(
            _anyRoute.getNextHop().getBitvec(), Prefix.parse("1.0.0.0/8"), 8);
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testUnsupportedMatchNextHop() {
    _policyBuilder.addStatement(
        new If(
            new MatchPrefixSet(
                new IpPrefix(NextHopIp.instance(), new LiteralInt(8)),
                new ExplicitPrefixSet(
                    new PrefixSpace(
                        ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());

    // when matching on the next-hop IP, we require the prefix length to be 32
    _exception.expect(UnsupportedOperationException.class);
    tbdd.compute(ImmutableSet.of());
  }

  @Test
  public void testDiscardNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    BDDRoute expected = new BDDRoute(_anyRoute);
    expected.setNextHopDiscarded(expected.getFactory().one());
    expected.setNextHopSet(expected.getFactory().one());
    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testSetNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(Ip.parse("1.1.1.1")))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());

    BDDRoute expected = new BDDRoute(_anyRoute);
    expected.setNextHopSet(expected.getFactory().one());
    expected.setNextHop(
        BDDInteger.makeFromValue(expected.getFactory(), 32, Ip.parse("1.1.1.1").asLong()));
    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testUnsupportedSetNextHop() {
    _policyBuilder.addStatement(new SetNextHop(SelfNextHop.getInstance()));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());

    _exception.expect(UnsupportedOperationException.class);
    tbdd.compute(ImmutableSet.of());
  }

  @Test
  public void testConditionalDefaultAction() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    BooleanExprs.CALL_EXPR_CONTEXT,
                    ImmutableList.of(),
                    ImmutableList.of(new StaticStatement(Statements.SetDefaultActionAccept))))
            .addStatement(new StaticStatement(Statements.DefaultAction))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());
    assertEquals(_anyRoute, outAnnouncements);
  }

  @Test
  public void testConditionalDefaultAction2() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                    ImmutableList.of(new StaticStatement(Statements.SetDefaultActionAccept))))
            .addStatement(new StaticStatement(Statements.DefaultAction))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expectedBDD =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    assertEquals(acceptedAnnouncements, expectedBDD);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, _anyRoute), outAnnouncements);
  }

  @Test
  public void testUnreachableSetDefaultAction() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))),
                    ImmutableList.of(
                        new StaticStatement(Statements.SetDefaultActionAccept),
                        new StaticStatement(Statements.ExitReject))))
            .addStatement(new StaticStatement(Statements.DefaultAction))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isZero());

    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testUnreachableUnhandled() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    BooleanExprs.CALL_EXPR_CONTEXT,
                    // currently we don't handle SetDefaultTag, but this branch is unreachable
                    ImmutableList.of(new SetDefaultTag(new LiteralLong(0L))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    assertTrue(acceptedAnnouncements.isOne());
    assertEquals(_anyRoute, outAnnouncements);
  }

  @Test
  public void testBufferedStatement() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new BufferedStatement(new SetLocalPreference(new LiteralLong(42))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
  public void testLegacyMatchAsPath() {
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
            new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_NAME)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
  public void testMatchExplicitAsPath() {
    _policyBuilder.addStatement(
        new If(
            new LegacyMatchAsPath(
                new ExplicitAsPathSet(
                    ImmutableList.of(
                        new RegexAsPathSetElem(" 40$"), new RegexAsPathSetElem("^$")))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRouteWithAPs = new BDDRoute(_g);
    BDD[] aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    assertEquals(3, _g.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    Map<SymbolicAsPathRegex, Set<Integer>> regexMap =
        _g.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    // get the unique atomic predicates that correspond to " 40$" and "^$"
    Integer ap1 = regexMap.get(new SymbolicAsPathRegex(" 40$")).iterator().next();
    Integer ap2 = regexMap.get(new SymbolicAsPathRegex("^$")).iterator().next();

    BDD expectedBDD = aps[ap1].or(aps[ap2]);
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRouteWithAPs), outAnnouncements);
  }

  @Test
  public void testMatchAsPath() {
    _policyBuilder.addStatement(
        new If(
            MatchAsPath.of(
                InputAsPath.instance(),
                AsPathMatchAny.of(
                    ImmutableList.of(AsPathMatchRegex.of(" 40$"), AsPathMatchRegex.of("^$")))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRouteWithAPs = new BDDRoute(_g);
    BDD[] aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    assertEquals(3, _g.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    Map<SymbolicAsPathRegex, Set<Integer>> regexMap =
        _g.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    // get the unique atomic predicates that correspond to " 40$" and "^$"
    Integer ap1 = regexMap.get(new SymbolicAsPathRegex(" 40$")).iterator().next();
    Integer ap2 = regexMap.get(new SymbolicAsPathRegex("^$")).iterator().next();

    BDD expectedBDD = aps[ap1].or(aps[ap2]);
    assertEquals(expectedBDD, acceptedAnnouncements);

    assertEquals(tbdd.iteZero(acceptedAnnouncements, anyRouteWithAPs), outAnnouncements);
  }

  @Test
  public void testDeleteCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    new CommunitySetDifference(
                        InputCommunities.instance(),
                        new CommunityIs(StandardCommunity.parse("4:44")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    _g =
        new Graph(
            _batfish,
            _batfish.getSnapshot(),
            null,
            null,
            // add another community to be tracked by the analysis
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("3:33"))),
            null);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDDRoute anyRoute = new BDDRoute(_g);

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // each atomic predicate for community 4:44 has the 0 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      assertEquals(
          outAnnouncements.getFactory().zero(),
          outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }

    // each atomic predicate for community 3:33 is unchanged
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("3:33")))) {
      assertEquals(
          anyRoute.getCommunityAtomicPredicates()[ap],
          outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testAddCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("4:44"))))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g =
        new Graph(
            _batfish,
            _batfish.getSnapshot(),
            null,
            null,
            // add another community to be tracked by the analysis
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("3:33"))),
            null);

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    BDDRoute anyRoute = new BDDRoute(_g);

    // each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      assertEquals(
          outAnnouncements.getFactory().one(), outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
    // each atomic predicate for community 3:33 is unchanged
    for (int ap :
        _g.getCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("3:33")))) {
      assertEquals(
          anyRoute.getCommunityAtomicPredicates()[ap],
          outAnnouncements.getCommunityAtomicPredicates()[ap]);
    }
  }

  @Test
  public void testSetExtendedCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    new LiteralCommunitySet(CommunitySet.of(ExtendedCommunity.parse("0:4:44")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
            .addStatement(
                new SetCommunities(
                    new LiteralCommunitySet(CommunitySet.of(LargeCommunity.of(10, 20, 30)))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
  public void testSetThenMatchCommunities() {
    Community comm = StandardCommunity.parse("30:40");
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(comm)))))
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(), new HasCommunity(new CommunityIs(comm))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _g = new Graph(_batfish, _batfish.getSnapshot());

    BDDRoute anyRoute = new BDDRoute(_g);
    BDD[] aps = anyRoute.getCommunityAtomicPredicates();
    // get the atomic predicates that correspond to 30:40
    Set<Integer> ap3040 =
        _g.getCommunityAtomicPredicates().getRegexAtomicPredicates().get(CommunityVar.from(comm));

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();
    // the original route must have the community 30:40
    BDD expectedBDD =
        BDDRoute.factory.orAll(ap3040.stream().map(i -> aps[i]).collect(Collectors.toList()));
    assertEquals(expectedBDD, acceptedAnnouncements);
    BDDRoute expected = tbdd.iteZero(acceptedAnnouncements, anyRoute);
    assertEquals(expected, outAnnouncements);

    // now do the analysis again but use the output attributes for matching
    tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), true);
    result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    acceptedAnnouncements = result.getSecond();
    outAnnouncements = result.getFirst();
    // the original route is now unconstrained
    assertTrue(acceptedAnnouncements.isOne());
    for (int i = 0; i < _g.getCommunityAtomicPredicates().getNumAtomicPredicates(); i++) {
      // each atomic predicate for 30:40 has the 1 BDD; all others have their original values
      assertEquals(
          ap3040.contains(i) ? outAnnouncements.getFactory().one() : aps[i],
          outAnnouncements.getCommunityAtomicPredicates()[i]);
    }
  }

  @Test
  public void testCallStatement() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    BooleanExprs.CALL_STATEMENT_CONTEXT,
                    ImmutableList.of(new SetLocalPreference(new LiteralLong(300L)))))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new CallStatement(calledPolicyName))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
  public void testCallExpr() {
    String calledPolicyName = "calledPolicy";

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    BooleanExprs.CALL_EXPR_CONTEXT,
                    ImmutableList.of(
                        new SetLocalPreference(new LiteralLong(300L)),
                        new StaticStatement(Statements.ReturnTrue))))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new CallExpr(calledPolicyName),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _g = new Graph(_batfish, _batfish.getSnapshot());

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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    BDD expected =
        isRelevantForDestination(
            _anyRoute, new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(32, 32)));
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
    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();

    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // no accepted routes because the callee uniformly rejects
    assertTrue(acceptedAnnouncements.isZero());
    assertEquals(tbdd.zeroedRecord(), outAnnouncements);
  }

  @Test
  public void testTraceableStatement() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new TraceableStatement(
                    TraceElement.of("text"),
                    ImmutableList.of(
                        new SetTag(new LiteralLong(42)),
                        new SetLocalPreference(new LiteralLong(44)),
                        new StaticStatement(Statements.ExitAccept))))
            .build();

    _g = new Graph(_batfish, _batfish.getSnapshot());

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements());
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outAnnouncements = result.getFirst();

    // the policy is applicable to all announcements
    assertTrue(acceptedAnnouncements.isOne());

    // the tag is now 42 and local pref is 44
    BDDRoute expected = new BDDRoute(_anyRoute);
    BDDInteger tag = expected.getTag();
    expected.setTag(BDDInteger.makeFromValue(tag.getFactory(), 32, 42));
    BDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(BDDInteger.makeFromValue(localPref.getFactory(), 32, 44));

    assertEquals(expected, outAnnouncements);
  }

  @Test
  public void testIsRelevantFor() {
    BDDRoute bddRoute = _anyRoute;
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(bddRoute.getPrefix());

    PrefixRange range = new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24));
    BDD rangeBdd = isRelevantForDestination(bddRoute, range);
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
    BDD rangeBdd = isRelevantForDestination(bddRoute, range);
    BDD len0 = bddRoute.getPrefixLength().value(0);
    BDD len32 = bddRoute.getPrefixLength().value(32);

    assertTrue(len0.imp(rangeBdd.not()).isOne());
    assertTrue(len32.imp(rangeBdd).isOne());
  }

  @Test
  public void testUseOutputAttributes() {
    // useOutputAttributes is false for CISCO_IOS
    setup(ConfigurationFormat.CISCO_IOS);
    _g = new Graph(_batfish, _batfish.getSnapshot());
    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, ImmutableList.of());
    assertFalse(tbdd.getUseOutputAttributes());

    // useOutputAttributes is true for JUNIPER
    setup(ConfigurationFormat.JUNIPER);
    _g = new Graph(_batfish, _batfish.getSnapshot());
    tbdd = new TransferBDD(_g, _baseConfig, ImmutableList.of());
    assertTrue(tbdd.getUseOutputAttributes());
  }
}
