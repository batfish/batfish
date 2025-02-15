package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.ConfigAtomicPredicatesTestUtils.forDevice;
import static org.batfish.minesweeper.bdd.AsPathMatchExprToRegexes.ASSUMED_MAX_AS_PATH_LENGTH;
import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantForDestination;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.DUMMY_BGP_SESSION_PROPERTIES;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.toSymbolicBgpOutputRoute;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.toQuestionBgpRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
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
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.MatchClusterListLength;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchInterface;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.UnchangedNextHop;
import org.batfish.datamodel.routing_policy.expr.VarInt;
import org.batfish.datamodel.routing_policy.expr.VarLong;
import org.batfish.datamodel.routing_policy.expr.VarOrigin;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetDefaultTag;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.AsPathRegexAtomicPredicates;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value;
import org.batfish.minesweeper.bdd.TransferBDD.Context;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;
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
  private ConfigAtomicPredicates _configAPs;

  @Rule public ExpectedException _expectedException = ExpectedException.none();

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
  }

  private BDDRoute anyRoute(BDDFactory factory) {
    return new BDDRoute(factory, 1, 1, 0, 0, 0, ImmutableList.of());
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  /**
   * Compare the results of the symbolic route analysis with Batfish's concrete route simulation.
   * For each path returned by the symbolic analysis, we solve for an input route that goes down
   * that path, simulate it through the route map, and compare the result to what the symbolic
   * analysis expects.
   *
   * @param policy the route policy being checked
   * @param paths the results of the symbolic analysis -- a set of paths through the policy
   * @param factory the BDD factory
   * @return a boolean indicating whether the check succeeded
   */
  private boolean validatePaths(
      RoutingPolicy policy, List<TransferReturn> paths, BDDFactory factory) {
    for (TransferReturn path : paths) {
      // solve for an input route and environment that causes execution to go down this path
      BDD fullConstraints =
          path.getInputConstraints()
              .and(new BDDRoute(factory, _configAPs).bgpWellFormednessConstraints());
      BDD fullModel = ModelGeneration.constraintsToModel(fullConstraints, _configAPs);
      AbstractRoute inRoute = ModelGeneration.satAssignmentToInputRoute(fullModel, _configAPs);
      Tuple<Predicate<String>, String> env =
          ModelGeneration.satAssignmentToEnvironment(fullModel, _configAPs);

      // simulate the input route in that environment;
      // for good measure we simulate twice, with the policy respectively considered an import and
      // export policy
      Result<? extends AbstractRoute, Bgpv4Route> inResult =
          TestRoutePoliciesAnswerer.simulatePolicy(
              policy,
              inRoute,
              DUMMY_BGP_SESSION_PROPERTIES,
              Environment.Direction.IN,
              env.getFirst(),
              env.getSecond());

      Result<? extends AbstractRoute, Bgpv4Route> outResult =
          TestRoutePoliciesAnswerer.simulatePolicy(
              policy,
              inRoute,
              DUMMY_BGP_SESSION_PROPERTIES,
              Environment.Direction.OUT,
              env.getFirst(),
              env.getSecond());

      // update the atomic predicates to include any prepended ASes on this path
      ConfigAtomicPredicates configAPsCopy = new ConfigAtomicPredicates(_configAPs);
      AsPathRegexAtomicPredicates aps = configAPsCopy.getAsPathRegexAtomicPredicates();
      aps.prependAPs(path.getOutputRoute().getPrependedASes());

      // compare the simulated results to that produced by the symbolic analysis
      LineAction action = path.getAccepted() ? LineAction.PERMIT : LineAction.DENY;
      boolean inValidate =
          ModelGeneration.validateModel(
              fullModel,
              path.getOutputRoute(),
              configAPsCopy,
              action,
              Environment.Direction.IN,
              inResult.setOutputRoute(
                  toSymbolicBgpOutputRoute(
                      toQuestionBgpRoute(inResult.getOutputRoute()), path.getOutputRoute())));
      boolean outValidate =
          ModelGeneration.validateModel(
              fullModel,
              path.getOutputRoute(),
              configAPsCopy,
              action,
              Environment.Direction.OUT,
              outResult.setOutputRoute(
                  toSymbolicBgpOutputRoute(
                      toQuestionBgpRoute(outResult.getOutputRoute()), path.getOutputRoute())));
      if (!inValidate || !outValidate) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void testExitAccept() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testExitReject() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testEmptyPolicy() {

    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(_policyBuilder.build());
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(_policyBuilder.build(), paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchPrefixRange() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRoute = anyRoute(tbdd.getFactory());
    BDD expectedBDD = isRelevantForDestination(anyRoute, PrefixRange.fromString("1.0.0.0/8:16-24"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), expectedBDD, true),
            new TransferReturn(anyRoute(tbdd.getFactory()), expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testAcceptBeforeIf() {
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:32-32"))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(4)),
                Statements.ExitAccept.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testRejectBeforeIf() {
    _policyBuilder.addStatement(Statements.ExitReject.toStaticStatement());

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:32-32"))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(4)),
                Statements.ExitAccept.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testEarlyReturn() {
    _policyBuilder.addStatement(Statements.ReturnFalse.toStaticStatement());
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSuppress() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
            ImmutableList.of(Statements.Suppress.toStaticStatement())));
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRoute = anyRoute(tbdd.getFactory());
    BDD expectedBDD = isRelevantForDestination(anyRoute, PrefixRange.fromString("0.0.0.0/0:32-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), expectedBDD, false),
            new TransferReturn(anyRoute(tbdd.getFactory()), expectedBDD.not(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsuppress() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:31-32"))),
            ImmutableList.of(Statements.Suppress.toStaticStatement())));
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
            ImmutableList.of(Statements.Unsuppress.toStaticStatement())));

    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRoute = anyRoute(tbdd.getFactory());
    BDD suppressed = isRelevantForDestination(anyRoute, PrefixRange.fromString("1.0.0.0/8:31-32"));
    BDD unsuppressed =
        isRelevantForDestination(anyRoute, PrefixRange.fromString("0.0.0.0/0:32-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute, suppressed.and(unsuppressed), true),
            new TransferReturn(anyRoute, suppressed.and(unsuppressed.not()), false),
            new TransferReturn(anyRoute, suppressed.not().and(unsuppressed), true),
            new TransferReturn(anyRoute, suppressed.not().and(unsuppressed.not()), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetDefault() {
    _policyBuilder
        .addStatement(Statements.SetDefaultActionAccept.toStaticStatement())
        .addStatement(Statements.DefaultAction.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetLocalDefault() {
    _policyBuilder
        .addStatement(Statements.SetLocalDefaultActionAccept.toStaticStatement())
        .addStatement(Statements.ReturnLocalDefaultAction.toStaticStatement());

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPartialAccept() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement())));

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:31-32"))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(3)),
                Statements.ExitAccept.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDD if1 =
        isRelevantForDestination(
            anyRoute(tbdd.getFactory()), PrefixRange.fromString("0.0.0.0/0:32-32"));
    BDD if2 =
        isRelevantForDestination(
            anyRoute(tbdd.getFactory()), PrefixRange.fromString("1.0.0.0/8:31-32"));

    BDDRoute localPref3 = anyRoute(tbdd.getFactory());
    localPref3.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 3));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), if1, true),
            new TransferReturn(localPref3, if1.not().and(if2), true),
            new TransferReturn(anyRoute(tbdd.getFactory()), if1.not().and(if2.not()), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPartialReturn() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement())));

    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:31-32"))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(3)),
                Statements.ReturnFalse.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDD if1 =
        isRelevantForDestination(
            anyRoute(tbdd.getFactory()), PrefixRange.fromString("0.0.0.0/0:32-32"));
    BDD if2 =
        isRelevantForDestination(
            anyRoute(tbdd.getFactory()), PrefixRange.fromString("1.0.0.0/8:31-32"));

    BDDRoute localPref3 = anyRoute(tbdd.getFactory());
    localPref3.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 3));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), if1, true),
            new TransferReturn(localPref3, if1.not().and(if2), false),
            new TransferReturn(anyRoute(tbdd.getFactory()), if1.not().and(if2.not()), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPartialReturnStatements() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(2)),
                Statements.ExitAccept.toStaticStatement())));
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:31-32"))),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(3)),
                Statements.ReturnFalse.toStaticStatement())));

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDD if1 =
        isRelevantForDestination(
            anyRoute(tbdd.getFactory()), PrefixRange.fromString("0.0.0.0/0:32-32"));
    BDD if2 =
        isRelevantForDestination(
            anyRoute(tbdd.getFactory()), PrefixRange.fromString("1.0.0.0/8:31-32"));

    BDDRoute localPref2 = anyRoute(tbdd.getFactory());
    localPref2.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 2));

    BDDRoute localPref3 = anyRoute(tbdd.getFactory());
    localPref3.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 3));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(localPref2, if1, true),
            new TransferReturn(localPref3, if1.not().and(if2), false),
            new TransferReturn(anyRoute(tbdd.getFactory()), if1.not().and(if2.not()), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testNestedIf() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
            ImmutableList.of(
                new If(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:31-32"))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement())))));

    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD if1 = isRelevantForDestination(any, PrefixRange.fromString("0.0.0.0/0:32-32"));
    BDD if2 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:31-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, if1.and(if2), true),
            new TransferReturn(any, if1.and(if2.not()), false),
            new TransferReturn(any, if1.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    // currently we assume announcements are IPv4
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testNot() {
    _policyBuilder.addStatement(
        new If(
            new Not(matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24")))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD if1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24")).not();

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, if1.not(), false), new TransferReturn(any, if1, true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testTwoPrefixes() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    PrefixRange.fromString("1.0.0.0/8:16-24"),
                    PrefixRange.fromString("1.2.0.0/16:20-32"))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD expectedBDD1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDD expectedBDD2 = isRelevantForDestination(any, PrefixRange.fromString("1.2.0.0/16:25-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD1.or(expectedBDD2), true),
            new TransferReturn(any, expectedBDD1.or(expectedBDD2).not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPrefixIntersection() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(
                ImmutableList.of(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.2.0.0/16:20-32"))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD expectedBDD1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDD expectedBDD2 = isRelevantForDestination(any, PrefixRange.fromString("1.2.0.0/16:20-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD1.not(), false),
            new TransferReturn(any, expectedBDD1.and(expectedBDD2.not()), false),
            new TransferReturn(any, expectedBDD1.and(expectedBDD2), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchEmptyPrefixSet() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD expectedBDD = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:31-31"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD, true),
            new TransferReturn(any, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetLocalPrefLiteral() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(MutableBDDInteger.makeFromValue(localPref.getFactory(), 32, 42));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetLocalPrefAndReject() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    // the local preference is now 42
    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(MutableBDDInteger.makeFromValue(localPref.getFactory(), 32, 42));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetOrigin() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expectedRoute = anyRoute(tbdd.getFactory());
    expectedRoute.getOriginType().setValue(OriginType.INCOMPLETE);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expectedRoute, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedSetOrigin() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetOrigin(new VarOrigin("var")))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expectedRoute = anyRoute(tbdd.getFactory());
    expectedRoute.setUnsupported(true);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expectedRoute, tbdd.getFactory().one(), true)));
  }

  @Test
  public void testSetOpsfMetricType_1() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetOspfMetricType(OspfMetricType.E1))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expectedRoute = anyRoute(tbdd.getFactory());
    expectedRoute.getOspfMetric().setValue(OspfType.E1);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expectedRoute, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetOpsfMetricType_2() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetOspfMetricType(OspfMetricType.E2))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expectedRoute = anyRoute(tbdd.getFactory());
    expectedRoute.getOspfMetric().setValue(OspfType.E2);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expectedRoute, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetMetric() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetMetric(new LiteralLong(50)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger med = expected.getMed();
    expected.setMed(MutableBDDInteger.makeFromValue(med.getFactory(), 32, 50));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetTag() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetTag(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger tag = expected.getTag();
    expected.setTag(MutableBDDInteger.makeFromValue(tag.getFactory(), 32, 42));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetTunnelEncapsulationAttribute() {
    TunnelEncapsulationAttribute attr =
        new TunnelEncapsulationAttribute(Ip.FIRST_CLASS_A_PRIVATE_IP);
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetTunnelEncapsulationAttribute(new LiteralTunnelEncapsulationAttribute(attr)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);
    expected.getTunnelEncapsulationAttribute().setValue(Value.literal(attr));
    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));

    List<TransferReturn> paths = tbdd.computePaths(policy);
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testRemoveTunnelEncapsulationAttribute() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(RemoveTunnelEncapsulationAttribute.instance())
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);
    expected.getTunnelEncapsulationAttribute().setValue(Value.absent());
    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));

    List<TransferReturn> paths = tbdd.computePaths(policy);
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetWeight() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetWeight(new LiteralInt(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger weight = expected.getWeight();
    expected.setWeight(MutableBDDInteger.makeFromValue(weight.getFactory(), 16, 42));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetWeightUnsupported() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetWeight(new VarInt("var")))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
  }

  @Test
  public void testSetAdminDistance() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetAdministrativeCost(new LiteralInt(255)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger ad = expected.getAdminDist();
    expected.setAdminDist(MutableBDDInteger.makeFromValue(ad.getFactory(), 8, 255));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetAdminDistanceUnsupported() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetAdministrativeCost(new VarInt("var")))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, any.getTag().value(42), true),
            new TransferReturn(any, any.getTag().value(42).not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetThenMatchTag() {
    List<Statement> stmts =
        ImmutableList.of(
            new SetTag(new LiteralLong(42)),
            new If(
                new MatchTag(IntComparator.EQ, new LiteralLong(42)),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute expected = new BDDRoute(any);
    MutableBDDInteger tag = expected.getTag();
    expected.setTag(MutableBDDInteger.makeFromValue(tag.getFactory(), 32, 42));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, any.getTag().value(42), true),
            new TransferReturn(expected, any.getTag().value(42).not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again but use the output attributes for matching
    setup(ConfigurationFormat.JUNIPER);
    policy = _policyBuilder.setStatements(stmts).build();
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // do the analysis once more, but after a directive to read from intermediate attributes
    setup(ConfigurationFormat.CISCO_IOS);
    policy =
        _policyBuilder
            .setStatements(
                ImmutableList.<Statement>builder()
                    .add(new StaticStatement(Statements.SetWriteIntermediateBgpAttributes))
                    .add(new StaticStatement(Statements.SetReadIntermediateBgpAttributes))
                    .addAll(stmts)
                    .build())
            .build();
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD tag01 = any.getTag().value(0).or(any.getTag().value(1));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, tag01, true), new TransferReturn(any, tag01.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchMetric() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchMetric(IntComparator.EQ, new LiteralLong(42)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, any.getMed().value(42), true),
            new TransferReturn(any, any.getMed().value(42).not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchMetricGE() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchMetric(IntComparator.GE, new LiteralLong(2)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD metricGETwo = any.getMed().value(0).or(any.getMed().value(1)).not();

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, metricGETwo, true),
            new TransferReturn(any, metricGETwo.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetThenMatchMetric() {
    List<Statement> stmts =
        ImmutableList.of(
            new SetMetric(new LiteralLong(42)),
            new If(
                new MatchMetric(IntComparator.EQ, new LiteralLong(42)),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute expected = new BDDRoute(any);
    MutableBDDInteger metric = expected.getMed();
    expected.setMed(MutableBDDInteger.makeFromValue(metric.getFactory(), 32, 42));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, any.getMed().value(42), true),
            new TransferReturn(expected, any.getMed().value(42).not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again but use the output attributes for matching
    setup(ConfigurationFormat.JUNIPER);
    policy = _policyBuilder.setStatements(stmts).build();
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchMetricUnsupported() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchMetric(IntComparator.EQ, new VarLong("var")),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    any.setUnsupported(true);

    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), false)));
  }

  @Test
  public void testMatchClusterListLength() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(42)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, any.getClusterListLength().value(42), true),
            new TransferReturn(any, any.getClusterListLength().value(42).not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchClusterListLengthLE() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    MatchClusterListLength.of(IntComparator.LE, new LiteralInt(2)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD clusterListLengthLETwo =
        any.getClusterListLength()
            .value(0)
            .or(any.getClusterListLength().value(1))
            .or(any.getClusterListLength().value(2));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, clusterListLengthLETwo, true),
            new TransferReturn(any, clusterListLengthLETwo.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchClusterListLengthUnsupported() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    MatchClusterListLength.of(IntComparator.EQ, new VarInt("var")),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    any.setUnsupported(true);

    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), false)));
  }

  @Test
  public void testTrackSucceeded() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new TrackSucceeded("mytrack"),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD trackPred = any.getTracks()[0];

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, trackPred, true),
            new TransferReturn(any, trackPred.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchSourceVrf() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchSourceVrf("mysource"),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD sourcePred = any.getSourceVrfs().value(1);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, sourcePred, true),
            new TransferReturn(any, sourcePred.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchInterface() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchInterface(ImmutableSet.of("int1", "int2")),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD intPred = any.getNextHopInterfaces().value(1).or(any.getNextHopInterfaces().value(2));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, intPred, true), new TransferReturn(any, intPred.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchNextHop() {
    _policyBuilder.addStatement(
        new If(
            new MatchPrefixSet(
                new IpPrefix(NextHopIp.instance(), new LiteralInt(Prefix.MAX_PREFIX_LENGTH)),
                new ExplicitPrefixSet(
                    new PrefixSpace(
                        ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/32")))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD expectedBDD = any.getNextHop().toBDD(Prefix.parse("1.0.0.0/32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD, true),
            new TransferReturn(any, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testNoMatchNextHop() {
    _policyBuilder.addStatement(
        new If(
            new MatchPrefixSet(
                new IpPrefix(NextHopIp.instance(), new LiteralInt(Prefix.MAX_PREFIX_LENGTH)),
                new ExplicitPrefixSet(
                    new PrefixSpace(
                        ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedMatchNextHop() {
    _policyBuilder
        .addStatement(
            new If(
                new MatchPrefixSet(
                    new IpPrefix(NextHopIp.instance(), new LiteralInt(8)),
                    new ExplicitPrefixSet(
                        new PrefixSpace(
                            ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")))))),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
  }

  @Test
  public void testDiscardNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setNextHopType(BDDRoute.NextHopType.DISCARDED);
    expected.setNextHopSet(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSelfNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(SelfNextHop.getInstance()))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setNextHopType(BDDRoute.NextHopType.SELF);
    expected.setNextHopSet(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPeerAddressNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(BgpPeerAddressNextHop.getInstance()))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setNextHopType(BDDRoute.NextHopType.BGP_PEER_ADDRESS);
    expected.setNextHopSet(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(Ip.parse("1.1.1.1")))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setNextHopSet(true);
    expected.setNextHop(
        MutableBDDInteger.makeFromValue(expected.getFactory(), 32, Ip.parse("1.1.1.1").asLong()));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testDiscardThenSetNextHop() {
    _policyBuilder
        .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
        .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(Ip.parse("1.1.1.1")))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setNextHopSet(true);
    expected.setNextHop(
        MutableBDDInteger.makeFromValue(expected.getFactory(), 32, Ip.parse("1.1.1.1").asLong()));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedSetNextHop() {
    _policyBuilder.addStatement(new SetNextHop(UnchangedNextHop.getInstance()));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setNextHopSet(true);
    expected.setUnsupported(true);

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
  }

  @Test
  public void testMatchProtocol() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.OSPF),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDDomain<RoutingProtocol> protocol = any.getProtocolHistory();

    BDD expectedBDD = protocol.value(RoutingProtocol.BGP).or(protocol.value(RoutingProtocol.OSPF));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD, true),
            new TransferReturn(any, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testConditionalDefaultAction2() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    ImmutableList.of(new StaticStatement(Statements.SetDefaultActionAccept))))
            .addStatement(new StaticStatement(Statements.DefaultAction))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDD expectedBDD = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD, true),
            new TransferReturn(any, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnreachableSetDefaultAction() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    ImmutableList.of(
                        new StaticStatement(Statements.SetDefaultActionAccept),
                        new StaticStatement(Statements.ExitReject))))
            .addStatement(new StaticStatement(Statements.DefaultAction))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDD expectedBDD = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, expectedBDD, false),
            new TransferReturn(any, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testLegacyMatchAsPath() {
    _baseConfig.setAsPathAccessLists(
        ImmutableMap.of(
            AS_PATH_NAME,
            // AsPathAccessList only properly initializes its state upon deserialization
            SerializationUtils.clone(
                new AsPathAccessList(
                    AS_PATH_NAME,
                    ImmutableList.of(
                        new AsPathAccessListLine(LineAction.PERMIT, " 40$"),
                        new AsPathAccessListLine(LineAction.DENY, "^$"))))));
    _policyBuilder.addStatement(
        new If(
            new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_NAME)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRouteWithAPs = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDDDomain<Integer> aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    // get the unique atomic predicate that corresponds to " 40$"
    Integer ap =
        _configAPs
            .getAsPathRegexAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(new SymbolicAsPathRegex(" 40$"))
            .iterator()
            .next();

    BDD expectedBDD = aps.value(ap);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRouteWithAPs, expectedBDD, true),
            new TransferReturn(anyRouteWithAPs, expectedBDD.not(), false)));

    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRouteWithAPs = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDDDomain<Integer> aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    assertEquals(3, _configAPs.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    Map<SymbolicAsPathRegex, Set<Integer>> regexMap =
        _configAPs.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    // get the unique atomic predicates that correspond to " 40$" and "^$"
    Integer ap1 = regexMap.get(new SymbolicAsPathRegex(" 40$")).iterator().next();
    Integer ap2 = regexMap.get(new SymbolicAsPathRegex("^$")).iterator().next();

    BDD expectedBDD = aps.value(ap1).or(aps.value(ap2));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRouteWithAPs, expectedBDD, true),
            new TransferReturn(anyRouteWithAPs, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again for a platform that matches on the output route;
    // the behavior should be the same since the as-path has not been updated
    setup(ConfigurationFormat.JUNIPER);
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRouteWithAPs, expectedBDD, true),
            new TransferReturn(anyRouteWithAPs, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testMatchAsPathLengthAny() {
    _policyBuilder.addStatement(
        new If(
            MatchAsPath.of(
                InputAsPath.instance(),
                HasAsPathLength.of(
                    new IntComparison(
                        IntComparator.LE, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    assertEquals(1, _configAPs.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    TransferBDD tbdd = new TransferBDD(_configAPs);
    BDDFactory factory = tbdd.getFactory();
    BDDRoute anyRouteWithAPs = new BDDRoute(tbdd.getFactory(), _configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    assertEquals(paths, ImmutableList.of(new TransferReturn(anyRouteWithAPs, factory.one(), true)));
    assertTrue(validatePaths(policy, paths, factory));
  }

  @Test
  public void testMatchAsPathLengthNone() {
    _policyBuilder.addStatement(
        new If(
            MatchAsPath.of(
                InputAsPath.instance(),
                HasAsPathLength.of(new IntComparison(IntComparator.GT, new LiteralInt(3)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    assertEquals(1, _configAPs.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    TransferBDD tbdd = new TransferBDD(_configAPs);
    BDDFactory factory = tbdd.getFactory();
    BDDRoute anyRouteWithAPs = new BDDRoute(tbdd.getFactory(), _configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);
    assertEquals(
        paths, ImmutableList.of(new TransferReturn(anyRouteWithAPs, factory.one(), false)));
    assertTrue(validatePaths(policy, paths, factory));
  }

  @Test
  public void testConjunction() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(
                ImmutableList.of(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:24-32"))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD conj1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDD conj2 = isRelevantForDestination(any, PrefixRange.fromString("0.0.0.0/0:24-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, conj1.not(), false),
            new TransferReturn(any, conj1.and(conj2.not()), false),
            new TransferReturn(any, conj1.and(conj2), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedConjunction() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(
                ImmutableList.of(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    // MatchColor is unsupported, so it should be ignored
                    new MatchColor(0L),
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:24-32"))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute unsupported = new BDDRoute(any);
    unsupported.setUnsupported(true);

    BDD conj1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDD conj2 = isRelevantForDestination(any, PrefixRange.fromString("0.0.0.0/0:24-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, conj1.not(), false),
            new TransferReturn(unsupported, conj1.and(conj2.not()), false),
            new TransferReturn(unsupported, conj1.and(conj2), true)));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expectedOut = anyRoute(tbdd.getFactory());
    expectedOut.setLocalPref(MutableBDDInteger.makeFromValue(expectedOut.getFactory(), 32, 300));

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expectedOut, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD conj1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));

    BDDRoute localPref300 = new BDDRoute(any);
    MutableBDDInteger localPref = localPref300.getLocalPref();
    localPref300.setLocalPref(MutableBDDInteger.makeFromValue(localPref.getFactory(), 32, 300));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, conj1.not(), true),
            new TransferReturn(localPref300, conj1, true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testApplyLongExprModification() {
    TransferBDD tbdd = new TransferBDD(forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME));
    TransferParam p = new TransferParam(tbdd.getOriginalRoute(), false);

    MutableBDDInteger toBeModified =
        MutableBDDInteger.makeFromIndex(tbdd.getFactory(), 32, 0, false);
    MutableBDDInteger value5 = MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 5);
    value5.setValue(5);

    assertThat(
        TransferBDD.applyLongExprModification(p, toBeModified, new IncrementMetric(5)),
        equalTo(toBeModified.addClipping(value5)));
    assertThat(
        TransferBDD.applyLongExprModification(p, toBeModified, new DecrementMetric(5)),
        equalTo(toBeModified.subClipping(value5)));
    assertThat(
        TransferBDD.applyLongExprModification(p, toBeModified, new IncrementLocalPreference(5)),
        equalTo(toBeModified.addClipping(value5)));
    assertThat(
        TransferBDD.applyLongExprModification(p, toBeModified, new DecrementLocalPreference(5)),
        equalTo(toBeModified.subClipping(value5)));
  }

  @Test
  public void testEmptyConjunction() {
    _policyBuilder.addStatement(
        new If(
            new Conjunction(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testDisjunction() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(
                ImmutableList.of(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:24-32"))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD disj1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDD disj2 = isRelevantForDestination(any, PrefixRange.fromString("0.0.0.0/0:24-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, disj1, true),
            new TransferReturn(any, disj1.not().and(disj2), true),
            new TransferReturn(any, disj1.not().and(disj2.not()), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedDisjunction() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(
                ImmutableList.of(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                    // MatchColor is unsupported, so it should be ignored
                    new MatchColor(0L),
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:24-32"))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute unsupported = new BDDRoute(any);
    unsupported.setUnsupported(true);

    BDD disj1 = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDD disj2 = isRelevantForDestination(any, PrefixRange.fromString("0.0.0.0/0:24-32"));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, disj1, true),
            new TransferReturn(unsupported, disj1.not().and(disj2), true),
            new TransferReturn(unsupported, disj1.not().and(disj2.not()), false)));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD firstDisjunctBDD = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));
    BDDRoute localPref32 = new BDDRoute(any);
    localPref32.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 300));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, firstDisjunctBDD, true),
            new TransferReturn(localPref32, firstDisjunctBDD.not(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testEmptyDisjunction() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(ImmutableList.of()),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    assertEquals(paths, ImmutableList.of(new TransferReturn(any, tbdd.getFactory().one(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testDisjunctionForAsPathGroup() {
    _policyBuilder.addStatement(
        new If(
            new Disjunction(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(" 40$")),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of("^$"))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRouteWithAPs = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDDDomain<Integer> aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    assertEquals(2, _configAPs.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    Map<SymbolicAsPathRegex, Set<Integer>> regexMap =
        _configAPs.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    // get the unique atomic predicate that corresponds to the union of " 40$" and "^$"
    Integer ap =
        regexMap
            .get(
                SymbolicAsPathRegex.union(
                    ImmutableList.of(
                        new SymbolicAsPathRegex(" 40$"), new SymbolicAsPathRegex("^$"))))
            .iterator()
            .next();

    BDD expectedBDD = aps.value(ap);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRouteWithAPs, expectedBDD, true),
            new TransferReturn(anyRouteWithAPs, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testAsSetsMatchingRanges() {
    AsSetsMatchingRanges asExpr =
        AsSetsMatchingRanges.of(true, true, ImmutableList.of(Range.closed(10L, 15L)));
    _policyBuilder.addStatement(
        new If(
            MatchAsPath.of(InputAsPath.instance(), asExpr),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRouteWithAPs = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDDDomain<Integer> aps = anyRouteWithAPs.getAsPathRegexAtomicPredicates();

    assertEquals(2, _configAPs.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    Map<SymbolicAsPathRegex, Set<Integer>> regexMap =
        _configAPs.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    // get the unique atomic predicate that corresponds to the AsSetsMatchingRanges constraint
    Integer ap = regexMap.get(new SymbolicAsPathRegex(asExpr)).iterator().next();

    BDD expectedBDD = aps.value(ap);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRouteWithAPs, expectedBDD, true),
            new TransferReturn(anyRouteWithAPs, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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

    _configAPs =
        forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            // add another community to be tracked by the analysis
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("3:33"))),
            null);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    // each atomic predicate for community 4:44 has the 0 BDD
    for (int ap :
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      expected.getCommunityAtomicPredicates()[ap] = tbdd.getFactory().zero();
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs =
        forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            // add another community to be tracked by the analysis
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("3:33"))),
            null);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    // each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      expected.getCommunityAtomicPredicates()[ap] = tbdd.getFactory().one();
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testAddDeleteCommunity() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("5:55"))))))
            .addStatement(
                new SetCommunities(
                    new CommunitySetDifference(
                        InputCommunities.instance(),
                        new CommunityIs(StandardCommunity.parse("4:44")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    _configAPs =
        forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            // add another community to be tracked by the analysis
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("3:33"))),
            null);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    // each atomic predicate for community 4:44 has the 0 BDD
    for (int ap :
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      expected.getCommunityAtomicPredicates()[ap] = tbdd.getFactory().zero();
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testAddCommunitiesTwice() {
    List<Statement> stmts =
        ImmutableList.of(
            new SetCommunities(
                CommunitySetUnion.of(
                    InputCommunities.instance(),
                    new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("4:44"))))),
            new SetCommunities(
                CommunitySetUnion.of(
                    InputCommunities.instance(),
                    new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("5:55"))))),
            new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();
    _configAPs =
        forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            // add another community to be tracked by the analysis
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("3:33"))),
            null);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    // each atomic predicate for community 5:55 has the 1 BDD
    for (int ap :
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("5:55")))) {
      expected.getCommunityAtomicPredicates()[ap] = tbdd.getFactory().one();
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // do the analysis once more, but after a directive to read from intermediate attributes
    setup(ConfigurationFormat.CISCO_IOS);
    policy =
        _policyBuilder
            .setStatements(
                ImmutableList.<Statement>builder()
                    .add(new StaticStatement(Statements.SetWriteIntermediateBgpAttributes))
                    .add(new StaticStatement(Statements.SetReadIntermediateBgpAttributes))
                    .addAll(stmts)
                    .build())
            .build();
    paths = tbdd.computePaths(policy);

    // now additionally each atomic predicate for community 4:44 has the 1 BDD
    for (int ap :
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("4:44")))) {
      expected.getCommunityAtomicPredicates()[ap] = tbdd.getFactory().one();
    }
    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs =
        forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            // make sure that the standard community 4:44 is not confused with the extended
            // community 0:4:44
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("4:44"))),
            null);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    BDD[] expectedCommAPs = expected.getCommunityAtomicPredicates();
    Set<Integer> setAPs =
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(ExtendedCommunity.parse("0:4:44")));
    // each atomic predicate for community 0:4:44 has the 1 BDD; all others have the 0 BDD
    for (int i = 0; i < expectedCommAPs.length; i++) {
      if (setAPs.contains(i)) {
        expectedCommAPs[i] = tbdd.getFactory().one();
      } else {
        expectedCommAPs[i] = tbdd.getFactory().zero();
      }
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetExtendedCommunityAdditive() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("5:55")))))
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(
                            CommunitySet.of(ExtendedCommunity.parse("0:4:44"))))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    BDD[] expectedCommAPs = expected.getCommunityAtomicPredicates();
    Set<Integer> setAPs =
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(ExtendedCommunity.parse("0:4:44")));
    // each atomic predicate for community 0:4:44 has the 1 BDD
    for (int i = 0; i < expectedCommAPs.length; i++) {
      if (setAPs.contains(i)) {
        expectedCommAPs[i] = tbdd.getFactory().one();
      }
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetExtendedCommunityNotAdditive() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new SetCommunities(
                    new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("5:55")))))
            .addStatement(
                new SetCommunities(
                    new LiteralCommunitySet(CommunitySet.of(ExtendedCommunity.parse("0:4:44")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    BDD[] expectedCommAPs = expected.getCommunityAtomicPredicates();
    Set<Integer> setAPs =
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(ExtendedCommunity.parse("0:4:44")));
    // each atomic predicate for community 0:4:44 has the 1 BDD; all others have the 0 BDD
    for (int i = 0; i < expectedCommAPs.length; i++) {
      if (setAPs.contains(i)) {
        expectedCommAPs[i] = tbdd.getFactory().one();
      } else {
        expectedCommAPs[i] = tbdd.getFactory().zero();
      }
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    BDD[] expectedCommAPs = expected.getCommunityAtomicPredicates();
    Set<Integer> setAPs =
        tbdd.getCommunityAtomicPredicates().get(CommunityVar.from(LargeCommunity.of(10, 20, 30)));
    // each atomic predicate for the large community that is set has the 1 BDD; all others have the
    // 0 BDD
    for (int i = 0; i < expectedCommAPs.length; i++) {
      if (setAPs.contains(i)) {
        expectedCommAPs[i] = tbdd.getFactory().one();
      } else {
        expectedCommAPs[i] = tbdd.getFactory().zero();
      }
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);

    BDD[] expectedCommAPs = expected.getCommunityAtomicPredicates();
    Set<Integer> setAPs =
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("30:40")));
    setAPs.addAll(
        tbdd.getCommunityAtomicPredicates()
            .get(CommunityVar.from(StandardCommunity.parse("40:40"))));
    // each atomic predicate for 30:40 and 40:40 has the 1 BDD; all others have the 0 BDD
    for (int i = 0; i < expectedCommAPs.length; i++) {
      if (setAPs.contains(i)) {
        expectedCommAPs[i] = tbdd.getFactory().one();
      } else {
        expectedCommAPs[i] = tbdd.getFactory().zero();
      }
    }

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRoute = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD[] aps = anyRoute.getCommunityAtomicPredicates();
    // get the atomic predicates that correspond to ^30:
    Set<Integer> ap30 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from("^30:"));

    BDD expectedBDD =
        tbdd.getFactory().orAll(ap30.stream().map(i -> aps[i]).collect(Collectors.toList()));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute, expectedBDD, true),
            new TransferReturn(anyRoute, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRoute = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD[] aps = anyRoute.getCommunityAtomicPredicates();

    // only the APs that belong to both ^30: and :20$ should be allowed
    Set<Integer> ap30 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from("^30:"));
    Set<Integer> ap20 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(":20$"));

    Set<Integer> intersection = new HashSet<>(ap30);
    intersection.retainAll(ap20);

    BDD expectedBDD =
        tbdd.getFactory()
            .orAll(intersection.stream().map(i -> aps[i]).collect(Collectors.toList()));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(anyRoute, expectedBDD, true),
            new TransferReturn(anyRoute, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetThenMatchCommunities() {
    Community comm = StandardCommunity.parse("30:40");
    List<Statement> stmts =
        ImmutableList.of(
            new SetCommunities(
                CommunitySetUnion.of(
                    InputCommunities.instance(), new LiteralCommunitySet(CommunitySet.of(comm)))),
            new If(
                new MatchCommunities(
                    InputCommunities.instance(), new HasCommunity(new CommunityIs(comm))),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD[] aps = expected.getCommunityAtomicPredicates();
    // get the atomic predicates that correspond to 30:40
    Set<Integer> ap3040 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(comm));

    // the original route must have the community 30:40
    BDD expectedBDD =
        tbdd.getFactory().orAll(ap3040.stream().map(i -> aps[i]).collect(Collectors.toList()));

    // each atomic predicate for 30:40 has the 1 BDD
    for (int i : ap3040) {
      aps[i] = tbdd.getFactory().one();
    }

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, expectedBDD, true),
            new TransferReturn(expected, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again but use the output attributes for matching
    setup(ConfigurationFormat.JUNIPER);
    policy = _policyBuilder.setStatements(stmts).build();
    paths = tbdd.computePaths(policy);

    // all input routes satisfy the if statement now, so there is only one path
    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetThenMatchOtherCommunity() {
    Community comm1 = StandardCommunity.parse("30:40");
    Community comm2 = StandardCommunity.parse("10:20");
    List<Statement> stmts =
        ImmutableList.of(
            new SetCommunities(
                CommunitySetUnion.of(
                    InputCommunities.instance(), new LiteralCommunitySet(CommunitySet.of(comm1)))),
            new If(
                new MatchCommunities(
                    InputCommunities.instance(), new HasCommunity(new CommunityIs(comm2))),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD[] aps = expected.getCommunityAtomicPredicates();
    Set<Integer> ap1020 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(comm2));
    Set<Integer> ap3040 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(comm1));

    // the original route must have the community 10:20
    BDD expectedBDD =
        tbdd.getFactory().orAll(ap1020.stream().map(i -> aps[i]).collect(Collectors.toList()));

    // each atomic predicate for 30:40 has the 1 BDD
    for (int i : ap3040) {
      aps[i] = tbdd.getFactory().one();
    }

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, expectedBDD, true),
            new TransferReturn(expected, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again but use the output attributes for matching
    setup(ConfigurationFormat.JUNIPER);
    policy = _policyBuilder.setStatements(stmts).build();
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, expectedBDD, true),
            new TransferReturn(expected, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testDeleteThenMatchCommunity() {
    Community comm = StandardCommunity.parse("30:40");
    List<Statement> stmts =
        ImmutableList.of(
            new SetCommunities(
                new CommunitySetDifference(InputCommunities.instance(), new CommunityIs(comm))),
            new If(
                new MatchCommunities(
                    InputCommunities.instance(), new HasCommunity(new CommunityIs(comm))),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    // the analysis will use the original route for matching
    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = new BDDRoute(tbdd.getFactory(), _configAPs);
    BDD[] aps = expected.getCommunityAtomicPredicates();
    Set<Integer> ap3040 =
        _configAPs
            .getStandardCommunityAtomicPredicates()
            .getRegexAtomicPredicates()
            .get(CommunityVar.from(comm));

    // the original route must have community 30:40
    BDD expectedBDD =
        tbdd.getFactory().orAll(ap3040.stream().map(i -> aps[i]).collect(Collectors.toList()));

    // each atomic predicate for 30:40 has the 0 BDD
    for (int i : ap3040) {
      aps[i] = tbdd.getFactory().zero();
    }

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, expectedBDD, true),
            new TransferReturn(expected, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again but use the output attributes for matching
    setup(ConfigurationFormat.JUNIPER);
    policy = _policyBuilder.setStatements(stmts).build();
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute localPref300 = anyRoute(tbdd.getFactory());
    localPref300.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 300));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(localPref300, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);

    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute localPref300 = anyRoute(tbdd.getFactory());
    localPref300.setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 300));

    List<TransferReturn> expectedPaths =
        ImmutableList.of(new TransferReturn(localPref300, tbdd.getFactory().one(), true));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
            .setName(calledPolicyName2)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    matchPrefixSet(ImmutableList.of(PrefixRange.fromString("0.0.0.0/0:32-32"))),
                    ImmutableList.of(
                        new CallStatement(calledPolicyName),
                        new StaticStatement(Statements.ExitAccept))))
            .build();

    RoutingPolicy policy =
        _policyBuilder.addStatement(new CallStatement(calledPolicyName2)).build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(
            calledPolicyName2, calledPolicy2, calledPolicyName, calledPolicy, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute anyRoute = anyRoute(tbdd.getFactory());

    BDD expectedBDD = isRelevantForDestination(anyRoute, PrefixRange.fromString("0.0.0.0/0:32-32"));
    BDDRoute expectedOut = new BDDRoute(anyRoute);
    expectedOut.setLocalPref(MutableBDDInteger.makeFromValue(expectedOut.getFactory(), 32, 300));

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expectedOut, expectedBDD, true),
            new TransferReturn(anyRoute, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<TransferReturn> expectedPaths =
        ImmutableList.of(
            new TransferReturn(anyRoute(tbdd.getFactory()), tbdd.getFactory().one(), false));
    assertEquals(expectedPaths, paths);
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
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

    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    // the tag is now 42 and local pref is 44
    BDDRoute expected = anyRoute(tbdd.getFactory());
    MutableBDDInteger tag = expected.getTag();
    expected.setTag(MutableBDDInteger.makeFromValue(tag.getFactory(), 32, 42));
    MutableBDDInteger localPref = expected.getLocalPref();
    expected.setLocalPref(MutableBDDInteger.makeFromValue(localPref.getFactory(), 32, 44));

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedExpression() {
    _policyBuilder.addStatement(
        new If(new MatchColor(0L), ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), false)));
  }

  @Test
  public void testUnsupportedMatchCommunities() {
    _policyBuilder.addStatement(
        new If(
            new MatchCommunities(
                // currently we only support matching on InputCommunities
                new LiteralCommunitySet(CommunitySet.empty()),
                new HasCommunity(AllStandardCommunities.instance())),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), false)));
  }

  @Test
  public void testUnsupportedStatement() {
    _policyBuilder
        .addStatement(new ExcludeAsPath(new LiteralAsList(ImmutableList.of())))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
  }

  @Test
  public void testPrependAsPath() {
    _policyBuilder
        .addStatement(new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(42L)))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setPrependedASes(ImmutableList.of(42L));

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));

    // now do the analysis again for a platform that reads from output attributes;
    // it should still work since we aren't later matching on the updated as-path
    setup(ConfigurationFormat.JUNIPER);
    paths = tbdd.computePaths(policy);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPrependAsPaths() {
    _policyBuilder
        .addStatement(new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(42L)))))
        .addStatement(
            new PrependAsPath(
                new LiteralAsList(ImmutableList.of(new ExplicitAs(44L), new ExplicitAs(4L)))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    // the first
    expected.setPrependedASes(ImmutableList.of(44L, 4L, 42L));

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testFirstMatchChain() {
    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new MatchMetric(IntComparator.EQ, new LiteralLong(42)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .addStatement(new StaticStatement(Statements.FallThrough))
            .build();

    String policy2Name = "policy2";
    RoutingPolicy policy2 =
        _nf.routingPolicyBuilder()
            .setName(policy2Name)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.BGP),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new FirstMatchChain(
                        ImmutableList.of(new CallExpr(policy1Name), new CallExpr(policy2Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(policy1Name, policy1, policy2Name, policy2, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD if1 = any.getMed().value(42);

    BDD if2 = any.getProtocolHistory().value(RoutingProtocol.BGP);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, if1, true),
            new TransferReturn(any, if1.not().and(if2), true),
            new TransferReturn(any, if1.not().and(if2.not()), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testFirstMatchChainReturnFalse() {
    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new MatchMetric(IntComparator.EQ, new LiteralLong(42)),
                    ImmutableList.of(new StaticStatement(Statements.ReturnFalse))))
            .addStatement(new StaticStatement(Statements.FallThrough))
            .build();

    String policy2Name = "policy2";
    RoutingPolicy policy2 =
        _nf.routingPolicyBuilder()
            .setName(policy2Name)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.BGP),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new FirstMatchChain(
                        ImmutableList.of(new CallExpr(policy1Name), new CallExpr(policy2Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(policy1Name, policy1, policy2Name, policy2, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD if1 = any.getMed().value(42);

    BDD if2 = any.getProtocolHistory().value(RoutingProtocol.BGP);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(any, if1, false),
            new TransferReturn(any, if1.not().and(if2), true),
            new TransferReturn(any, if1.not().and(if2.not()), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testSetDefaultPolicy() {
    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(new StaticStatement(Statements.FallThrough))
            .build();

    String defaultName = "default";
    RoutingPolicy defaultPolicy =
        _nf.routingPolicyBuilder()
            .setName(defaultName)
            .setOwner(_baseConfig)
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetDefaultPolicy(defaultName))
            .addStatement(
                new If(
                    new FirstMatchChain(ImmutableList.of(new CallExpr(policy1Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(policy1Name, policy1, defaultName, defaultPolicy, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    assertEquals(paths, ImmutableList.of(new TransferReturn(any, any.getFactory().one(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testNoDefaultPolicy() {
    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(new StaticStatement(Statements.FallThrough))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new FirstMatchChain(ImmutableList.of(new CallExpr(policy1Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(ImmutableMap.of(policy1Name, policy1, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    _expectedException.expect(BatfishException.class);
    tbdd.computePaths(policy);
  }

  @Test
  public void testFirstMatchChainStateful() {

    // test proper stateful behavior for Juniper chains
    setup(ConfigurationFormat.JUNIPER);

    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(new SetTag(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.FallThrough))
            .build();

    String policy2Name = "policy2";
    RoutingPolicy policy2 =
        _nf.routingPolicyBuilder()
            .setName(policy2Name)
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new MatchTag(IntComparator.EQ, new LiteralLong(42)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new FirstMatchChain(
                        ImmutableList.of(new CallExpr(policy1Name), new CallExpr(policy2Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(policy1Name, policy1, policy2Name, policy2, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute expected = new BDDRoute(any);
    MutableBDDInteger tag = expected.getTag();
    expected.setTag(MutableBDDInteger.makeFromValue(tag.getFactory(), 32, 42));

    // the tag set in policy1 is matched upon in policy2
    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testFirstMatchChainOverwrite() {

    // test proper stateful behavior for Juniper chains
    setup(ConfigurationFormat.JUNIPER);

    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(new SetTag(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.FallThrough))
            .build();

    String policy2Name = "policy2";
    RoutingPolicy policy2 =
        _nf.routingPolicyBuilder()
            .setName(policy2Name)
            .setOwner(_baseConfig)
            .addStatement(new SetTag(new LiteralLong(10)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new FirstMatchChain(
                        ImmutableList.of(new CallExpr(policy1Name), new CallExpr(policy2Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(policy1Name, policy1, policy2Name, policy2, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute expected = new BDDRoute(any);
    MutableBDDInteger tag = expected.getTag();
    expected.setTag(MutableBDDInteger.makeFromValue(tag.getFactory(), 32, 10));

    // the tag set in policy1 is overwritten in policy2
    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testFirstMatchChainImplicitFallThrough() {

    // test proper stateful behavior for Juniper chains
    setup(ConfigurationFormat.JUNIPER);

    String policy1Name = "policy1";
    RoutingPolicy policy1 =
        _nf.routingPolicyBuilder()
            .setName(policy1Name)
            .setOwner(_baseConfig)
            .addStatement(new SetTag(new LiteralLong(42)))
            .build();

    String policy2Name = "policy2";
    RoutingPolicy policy2 =
        _nf.routingPolicyBuilder()
            .setName(policy2Name)
            .setOwner(_baseConfig)
            .addStatement(new SetMetric(new LiteralLong(42)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new FirstMatchChain(
                        ImmutableList.of(new CallExpr(policy1Name), new CallExpr(policy2Name))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(policy1Name, policy1, policy2Name, policy2, POLICY_NAME, policy));
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute expected = new BDDRoute(any);
    MutableBDDInteger tag = expected.getTag();
    expected.setTag(MutableBDDInteger.makeFromValue(tag.getFactory(), 32, 42));
    MutableBDDInteger med = expected.getMed();
    expected.setMed(MutableBDDInteger.makeFromValue(med.getFactory(), 32, 42));

    // the updates from both policies take effect
    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testPartialUnsupportedStatement() {
    _policyBuilder
        .addStatement(
            new If(
                matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-24"))),
                // the SelfNextHop construct is not supported
                ImmutableList.of(
                    new SetNextHop(UnchangedNextHop.getInstance()),
                    new StaticStatement(Statements.ExitAccept))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());

    BDD expectedBDD = isRelevantForDestination(any, PrefixRange.fromString("1.0.0.0/8:16-24"));

    BDDRoute expectedOut = new BDDRoute(any);
    expectedOut.setNextHopSet(true);
    expectedOut.setUnsupported(true);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expectedOut, expectedBDD, true),
            new TransferReturn(any, expectedBDD.not(), true)));
  }

  @Test
  public void testUnsupportedStaticStatement() {
    _policyBuilder
        .addStatement(Statements.RemovePrivateAs.toStaticStatement())
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), true)));
  }

  @Test
  public void testMatchStaticRoute() {
    List<Statement> stmts =
        ImmutableList.of(
            new If(
                new MatchProtocol(RoutingProtocol.STATIC),
                ImmutableList.of(
                    new SetLocalPreference(new LiteralLong(300)),
                    new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();

    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute any = anyRoute(tbdd.getFactory());
    BDDRoute expected = new BDDRoute(any);

    MutableBDDInteger lp = expected.getLocalPref();
    expected.setLocalPref(MutableBDDInteger.makeFromValue(lp.getFactory(), 32, 300));

    BDDDomain<RoutingProtocol> protocol = any.getProtocolHistory();
    BDD expectedBDD = protocol.value(RoutingProtocol.STATIC);

    assertEquals(
        paths,
        ImmutableList.of(
            new TransferReturn(expected, expectedBDD, true),
            new TransferReturn(any, expectedBDD.not(), false)));
    assertTrue(validatePaths(policy, paths, tbdd.getFactory()));
  }

  @Test
  public void testUnsupportedStatementDeny() {
    _policyBuilder
        .addStatement(new SetTag(new VarLong("var")))
        .addStatement(new StaticStatement(Statements.ExitReject));
    RoutingPolicy policy = _policyBuilder.build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);

    BDDRoute expected = anyRoute(tbdd.getFactory());
    expected.setUnsupported(true);

    assertEquals(
        paths, ImmutableList.of(new TransferReturn(expected, tbdd.getFactory().one(), false)));
  }

  @Test
  public void testIsRelevantFor() {
    BDDRoute bddRoute = anyRoute(JFactory.init(100, 100));
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(bddRoute.getPrefix());

    PrefixRange range = PrefixRange.fromString("1.0.0.0/8:16-24");
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
    BDDRoute bddRoute = anyRoute(JFactory.init(100, 100));

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
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    assertFalse(Context.forPolicy(_policyBuilder.build()).useOutputAttributes());

    // useOutputAttributes is true for JUNIPER
    setup(ConfigurationFormat.JUNIPER);
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    assertTrue(Context.forPolicy(_policyBuilder.build()).useOutputAttributes());
  }

  @Test
  public void testDistinctBDDFactories() {
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    TransferBDD tbdd1 = new TransferBDD(_configAPs);
    TransferBDD tbdd2 = new TransferBDD(_configAPs);
    assertNotSame(tbdd1.getFactory(), tbdd2.getFactory());
  }

  /**
   * Tests that a simple exponential paths with identical output is correctly collapsed or not when
   * retainAllPaths is false or true.
   */
  @Test
  public void testDontRetainAllPaths() {
    int numIfs = 10;
    // Make a list of N different If statements, each of which has a different guard but the
    // same Set.
    List<Statement> manyMatchesSameResult = new ArrayList<>();
    for (int i = 0; i < numIfs; ++i) {
      manyMatchesSameResult.add(
          new If(
              new MatchCommunities(
                  new InputCommunities(),
                  new HasCommunity(new CommunityIs(StandardCommunity.of(i)))),
              ImmutableList.of(new SetMetric(new LiteralLong(5)))));
    }
    _policyBuilder.setStatements(manyMatchesSameResult).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);
    assertThat(
        _configAPs.getStandardCommunityAtomicPredicates().getNumAtomicPredicates(),
        equalTo(numIfs + 1));

    TransferBDD tbdd = new TransferBDD(_configAPs);
    Context context = Context.create("policy", "config", false, _baseConfig);

    BDDRoute routeParam = new BDDRoute(tbdd.getOriginalRoute());
    TransferBDDState initial =
        new TransferBDDState(new TransferParam(routeParam, false), new TransferResult(routeParam));
    assertThat(
        tbdd.computePaths(initial, manyMatchesSameResult, context, true), hasSize(1 << numIfs));

    routeParam = new BDDRoute(tbdd.getOriginalRoute());
    initial =
        new TransferBDDState(new TransferParam(routeParam, false), new TransferResult(routeParam));
    assertThat(tbdd.computePaths(initial, manyMatchesSameResult, context, false), hasSize(2));
  }
}
