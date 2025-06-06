package org.batfish.minesweeper.question.transferbddvalidation;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.routing_policy.expr.IntComparator.GT;
import static org.batfish.minesweeper.ConfigAtomicPredicatesTestUtils.forDevice;
import static org.batfish.minesweeper.question.transferbddvalidation.TransferBDDValidationAnswerer.COL_CONCRETE_ACTION;
import static org.batfish.minesweeper.question.transferbddvalidation.TransferBDDValidationAnswerer.COL_CONCRETE_OUTPUT_ROUTE;
import static org.batfish.minesweeper.question.transferbddvalidation.TransferBDDValidationAnswerer.COL_SEED;
import static org.batfish.minesweeper.question.transferbddvalidation.TransferBDDValidationAnswerer.COL_SYMBOLIC_ACTION;
import static org.batfish.minesweeper.question.transferbddvalidation.TransferBDDValidationAnswerer.COL_SYMBOLIC_OUTPUT_ROUTE;
import static org.batfish.minesweeper.question.transferbddvalidation.TransferBDDValidationAnswerer.MAX_COMMUNITIES_SIZE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_DIFF;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.UnchangedNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TransferBDDValidationAnswerer}. */
public class TransferBDDValidationAnswererTest {

  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private NetworkFactory _nf;
  private RoutingPolicy.Builder _policyBuilder;
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private ConfigAtomicPredicates _configAPs;

  private TransferBDDValidationAnswerer _answerer;

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
    Vrf vrf =
        _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = _nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME);

    BgpProcess bgp =
        _nf.bgpProcessBuilder()
            .setRouterId(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setEbgpAdminCost(0)
            .setIbgpAdminCost(0)
            .setLocalAdminCost(0)
            .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setVrf(vrf)
            .build();

    BgpActivePeerConfig bgpPeer =
        BgpActivePeerConfig.builder()
            .setGroup("testGroup")
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setImportPolicy(POLICY_NAME)
                    .setExportPolicy(POLICY_NAME)
                    .build())
            .build();
    bgp.setNeighbors(ImmutableSortedMap.of(Ip.FIRST_CLASS_A_PRIVATE_IP, bgpPeer));

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(HOSTNAME, _baseConfig);
    _batfish = new MockBatfish(configs);
    _answerer =
        new TransferBDDValidationAnswerer(
            new TransferBDDValidationQuestion(null, null, true, 123456789), _batfish);
  }

  @Test
  public void testEndToEndQuestion() {
    _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement()).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDDValidationQuestion question = new TransferBDDValidationQuestion();

    TransferBDDValidationAnswerer answerer = new TransferBDDValidationAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(answer.getRows().getData(), empty());
  }

  @Test
  public void testConsistent() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement()).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<Row> rows = _answerer.validatePaths(policy, paths, tbdd, Environment.Direction.IN);

    assertThat(rows, empty());
  }

  @Test
  public void testWrongAction() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement()).build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    // flip the accepted boolean in the one path, to create a violation
    List<TransferReturn> badPaths = ImmutableList.of(paths.get(0).setAccepted(false));

    List<Row> rows = _answerer.validatePaths(policy, badPaths, tbdd, Environment.Direction.OUT);

    // there are two answers, since we simulate the path in both the IN and OUT directions
    assertThat(rows.size(), equalTo(1));

    assertThat(
        rows,
        Matchers.contains(
            allOf(
                hasColumn(COL_SYMBOLIC_ACTION, equalTo(LineAction.DENY.toString()), Schema.STRING),
                hasColumn(
                    COL_CONCRETE_ACTION, equalTo(LineAction.PERMIT.toString()), Schema.STRING),
                hasColumn(COL_SYMBOLIC_OUTPUT_ROUTE, nullValue(), Schema.OBJECT),
                hasColumn(COL_CONCRETE_OUTPUT_ROUTE, notNullValue(), Schema.OBJECT),
                hasColumn(COL_SEED, 123456789L, Schema.LONG))));
  }

  @Test
  public void testWrongOutputRoute() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(200)))
            .addStatement(Statements.ExitAccept.toStaticStatement())
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> badPaths = tbdd.computePaths(policy);
    // change the local pref in the symbolic results, to create a violation
    badPaths.forEach(
        p ->
            p.getOutputRoute()
                .setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 300)));

    List<Row> rows = _answerer.validatePaths(policy, badPaths, tbdd, Environment.Direction.IN);

    // there are two answers, since we simulate the path in both the IN and OUT directions
    assertThat(rows.size(), equalTo(1));

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_LOCAL_PREFERENCE, "300", "200")));
    assertThat(
        rows,
        Matchers.contains(
            allOf(
                hasColumn(
                    COL_SYMBOLIC_ACTION, equalTo(LineAction.PERMIT.toString()), Schema.STRING),
                hasColumn(
                    COL_CONCRETE_ACTION, equalTo(LineAction.PERMIT.toString()), Schema.STRING),
                hasColumn(COL_SYMBOLIC_OUTPUT_ROUTE, notNullValue(), Schema.OBJECT),
                hasColumn(COL_CONCRETE_OUTPUT_ROUTE, notNullValue(), Schema.OBJECT),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS),
                hasColumn(COL_SEED, 123456789L, Schema.LONG))));
  }

  @Test
  public void testUnsupported() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(200)))
            // the next statement is currently unsupported
            .addStatement(new SetNextHop(UnchangedNextHop.getInstance()))
            .addStatement(Statements.ExitAccept.toStaticStatement())
            .build();
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME);

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> badPaths = tbdd.computePaths(policy);
    // change the local pref in the symbolic results, to create a violation
    badPaths.forEach(
        p ->
            p.getOutputRoute()
                .setLocalPref(MutableBDDInteger.makeFromValue(tbdd.getFactory(), 32, 300)));

    List<Row> rows = _answerer.validatePaths(policy, badPaths, tbdd, Environment.Direction.OUT);

    // there are no answers, since paths with unsupported features are ignored
    assertThat(rows, empty());
  }

  @Test
  public void testTooManyCommunities() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(),
                        new HasSize(new IntComparison(GT, new LiteralInt(MAX_COMMUNITIES_SIZE)))),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement())))
            .build();
    _configAPs =
        forDevice(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            // add a lot of communities, so it's very likely that a random route will satisfy the
            // route policy's conditional guard
            IntStream.range(1, 200)
                .mapToObj(
                    i -> CommunityVar.from(StandardCommunity.parse(String.valueOf(i) + ":100")))
                .collect(ImmutableSet.toImmutableSet()),
            ImmutableSet.of());

    TransferBDD tbdd = new TransferBDD(_configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy);
    List<Row> rows = _answerer.validatePaths(policy, paths, tbdd, Environment.Direction.IN);

    // there are no answers, since we ensure that routes won't have more than MAX_COMMUNITIES_SIZE
    // communities on them
    assertThat(rows, empty());
  }

  @Test
  public void testManyNegatedCommunities() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement()).build();
    Set<CommunityVar> cvars =
        IntStream.range(1, MAX_COMMUNITIES_SIZE + 1)
            .mapToObj(i -> CommunityVar.from(StandardCommunity.parse(String.valueOf(i) + ":100")))
            .collect(ImmutableSet.toImmutableSet());
    _configAPs = forDevice(_batfish, _batfish.getSnapshot(), HOSTNAME, cvars, ImmutableSet.of());

    TransferBDD tbdd = new TransferBDD(_configAPs);
    BDDRoute orig = tbdd.getOriginalRoute();
    // create a path that requires that none of the communities above be on the input route
    TransferReturn path =
        new TransferReturn(
            tbdd.getOriginalRoute(),
            tbdd.getFactory()
                .orAll(
                    cvars.stream()
                        .flatMap(
                            cvar ->
                                _configAPs
                                    .getStandardCommunityAtomicPredicates()
                                    .getRegexAtomicPredicates()
                                    .get(cvar)
                                    .stream())
                        .map(i -> orig.getCommunityAtomicPredicates()[i])
                        .collect(Collectors.toList()))
                .not(),
            true);

    List<Row> rows =
        _answerer.validatePaths(policy, ImmutableList.of(path), tbdd, Environment.Direction.OUT);
    assertThat(rows, empty());
  }
}
