package org.batfish.minesweeper.question.compareroutepolicies;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.table.TableDiff.baseColumnName;
import static org.batfish.datamodel.table.TableDiff.deltaColumnName;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_ACTION;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_DIFF;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_INPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_NODE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_OUTPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_POLICY_NAME;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_PROPOSED_POLICY_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.question.compareRoutePolicies.CompareRoutePoliciesAnswerer;
import org.batfish.minesweeper.question.compareRoutePolicies.CompareRoutePoliciesQuestion;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CompareRoutePoliciesAnswerer}. */
public class CompareRoutePoliciesAnswererTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_1_NAME = "policy1";
  private static final String POLICY_2_NAME = "policy2";
  private static final String AS_PATH_1 = "asPath1";
  private static final String AS_PATH_2 = "asPath2";
  private static final String COMMUNITY_LIST_1 = "comm1";
  private static final String PFX_LST_1 = "pfx1";
  private static final String PFX_LST_2 = "pfx2";

  private static final Environment.Direction DEFAULT_DIRECTION = Environment.Direction.IN;

  private RoutingPolicy.Builder _policyBuilder_1;
  private RoutingPolicy.Builder _policyBuilder_2;
  private IBatfish _batfish;

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
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    Configuration baseConfig = cb.build();

    // AsPathAccessList only properly initializes its state upon deserialization
    AsPathAccessList asPath1 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^40$"))));
    AsPathAccessList asPath2 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_2, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^50$"))));

    baseConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1, AS_PATH_2, asPath2));
    CommunitySet comm1 = CommunitySet.of(StandardCommunity.of(0, 0));
    baseConfig.setCommunitySets(ImmutableMap.of(COMMUNITY_LIST_1, comm1));

    RouteFilterList f1 =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.1/32")))));
    RouteFilterList f2 =
        new RouteFilterList(
            PFX_LST_2,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/31")))));

    baseConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1, PFX_LST_2, f2));

    nf.vrfBuilder().setOwner(baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder_1 = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_1_NAME);
    _policyBuilder_2 = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_2_NAME);
    _batfish = new MockBatfish(ImmutableSortedMap.of(HOSTNAME, baseConfig));
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  private MatchCommunities matchCommunityRegex(String regex) {
    return new MatchCommunities(
        InputCommunities.instance(),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), regex)));
  }

  @Test
  public void testBothPermit() {
    RoutingPolicy policy1 =
        _policyBuilder_1.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    RoutingPolicy policy2 =
        _policyBuilder_2.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat("the two are equivalent", answer.getRows().getData().isEmpty());
  }

  @Test
  public void testPermitDeny() {
    RoutingPolicy policy1 =
        _policyBuilder_1.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    RoutingPolicy policy2 =
        _policyBuilder_2.addStatement(new StaticStatement(Statements.ExitReject)).build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests permit/deny differences when one of the statements is ignored. */
  @Test
  public void testPermitDenyDeadCode() {
    RoutingPolicy policy1 =
        _policyBuilder_1.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new StaticStatement(Statements.ExitReject))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests that differences in local preference are detected. */
  @Test
  public void testLocalPrefDifference() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetLocalPreference(new LiteralLong(200)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetLocalPreference(new LiteralLong(100)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_LOCAL_PREFERENCE, "100", "200")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests that differences in tag are detected. */
  @Test
  public void testSetTagDifference() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetTag(new LiteralLong(0)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetTag(new LiteralLong(1)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_TAG, "1", "0")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests that differences in MED are detected. */
  @Test
  public void testSetMedDifference() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetMetric(new LiteralLong(0)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetMetric(new LiteralLong(1)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "1", "0")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests that differences in weight are detected. */
  @Test
  public void testWeightDifference() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetWeight(new LiteralInt(10)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetWeight(new LiteralInt(0)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_WEIGHT, "0", "10")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests that differences in nexthop IP are detected. */
  @Test
  public void testSetNextHop() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(Ip.parse("1.1.1.1")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_NEXT_HOP_IP, "0.0.0.1", "1.1.1.1")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests that when the nexthop IP is discarded the difference is captured. */
  @Test
  public void testDiscardNexthop() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(BgpRoute.PROP_NEXT_HOP_IP, "0.0.0.1", Ip.create(-1).toString())));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /**
   * Tests differences in OSPF Metric. We currently ignore this. It needs to be added to
   * BgpRoute/BgpRouteDiff.
   */
  @Test
  public void testOspfMetric() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetOspfMetricType(OspfMetricType.E1))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetOspfMetricType(OspfMetricType.E2))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        "the two are equivalent because we ignore OSPF metric differences",
        answer.getRows().getData().isEmpty());
  }

  /**
   * Tests differences in Administrative Distance. We currently ignore this. It needs to be added to
   * BgpRoute/BgpRouteDiff.
   */
  @Test
  public void testAdministrativeDistance() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetAdministrativeCost(new LiteralInt(20)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetAdministrativeCost(new LiteralInt(10)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        "the two are equivalent because we ignore AD differences",
        answer.getRows().getData().isEmpty());
  }

  /** Test differences in set communities. */
  @Test
  public void testSetCommunity() {
    RoutingPolicy policy1 =
        _policyBuilder_1.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("0:0"))))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, "[0:0]", "[]")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests differences resulting from different treatment of incoming route's AS path. */
  @Test
  public void testMatchAs() {
    RoutingPolicy policy1 =
        _policyBuilder_1.addStatement(new StaticStatement(Statements.ExitAccept)).build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new StaticStatement(Statements.ExitReject))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of())
            .setAsPath(AsPath.ofSingletonAsSets(40L))
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests differences from Prefix Matching. */
  @Test
  public void testMatchPrefixes() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(
                new If(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("10.0.0.0/24"), new SubRange(32, 32)))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(
                new If(
                    matchPrefixSet(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("10.0.0.0/24"), new SubRange(16, 32)))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of())
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests differences from Prefix Matching. */
  @Test
  public void testMatchPrefixesSplit() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_1)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_2)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    BgpRoute inputRoute_1 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of())
            .build();

    BgpRoute inputRoute_2 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/31"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of())
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute_1), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS)),
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute_2), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests differences resulting from different treatment of incoming route's communities. */
  @Test
  public void testMatchCommunity() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(),
                        new HasCommunity(
                            new StandardCommunityHighMatch(
                                new IntComparison(IntComparator.EQ, new LiteralInt(0))))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of())
            .build();
    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /**
   * Test match-set of communities. First policy sets community 1:1 if it matches on community 0:0.
   * Second policy always sets community 1:1.
   */
  @Test
  public void testMatchSetCommunity() {
    Community comm0 = StandardCommunity.parse("0:0");
    Community comm1 = StandardCommunity.parse("1:1");

    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(), new HasCommunity(new CommunityIs(comm0))),
                    ImmutableList.of(
                        new SetCommunities(
                            CommunitySetUnion.of(
                                InputCommunities.instance(),
                                new LiteralCommunitySet(CommunitySet.of(comm1)))))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(comm1)))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of())
            .build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, "[1:1]", "[]")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /** Tests multiple differences in set values. */
  @Test
  public void testMultipleSetDifferences() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(new SetLocalPreference(new LiteralLong(200)))
            .addStatement(new SetTag(new LiteralLong(10)))
            .addStatement(new SetMetric(new LiteralLong(0)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy policy2 =
        _policyBuilder_2
            .addStatement(new SetMetric(new LiteralLong(100)))
            .addStatement(new SetLocalPreference(new LiteralLong(0)))
            .addStatement(new SetTag(new LiteralLong(0)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(BgpRoute.PROP_METRIC, "100", "0"),
                new BgpRouteDiff(BgpRoute.PROP_LOCAL_PREFERENCE, "0", "200"),
                new BgpRouteDiff(BgpRoute.PROP_TAG, "0", "10")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, anything(), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  /**
   * Tests that deleting a community-list produces an appropriate counterexample. This test
   * exercises the fact that we add output route constraints to model generation. There is one path
   * for each route-map, where the path condition for each is true (no constraints). However, the
   * route with input community 1:1 is treated differently; naive model generation would not give us
   * a proper counterexample. This test showcases that model generation takes the output into
   * consideration (requiring that the outputs differ).
   */
  @Test
  public void testDeleteCommunity() {
    RoutingPolicy policy1 =
        _policyBuilder_1
            .addStatement(
                new SetCommunities(
                    new CommunitySetDifference(
                        InputCommunities.instance(),
                        new CommunityIs(StandardCommunity.parse("1:1")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy policy2 =
        _policyBuilder_2.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    CompareRoutePoliciesQuestion question =
        new CompareRoutePoliciesQuestion(
            DEFAULT_DIRECTION, policy1.getName(), policy2.getName(), HOSTNAME);
    CompareRoutePoliciesAnswerer answerer = new CompareRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1, 1)))
            .build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, "[1:1]", "[]")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_1_NAME), Schema.STRING),
                hasColumn(COL_PROPOSED_POLICY_NAME, equalTo(POLICY_2_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), anything(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }
}
