package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.toClosedRange;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action.DENY;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_ACTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_DIRECTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_ROUTE_CONSTRAINTS;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_ACTION;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_DIFF;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_INPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_NODE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_OUTPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_POLICY_NAME;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.community.StandardCommunity;
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
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SearchRoutePoliciesAnswerer}. */
public class SearchRoutePoliciesAnswererTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private static final String AS_PATH_1 = "asPath1";
  private static final String AS_PATH_2 = "asPath2";

  private static final BgpRouteConstraints EMPTY_CONSTRAINTS =
      BgpRouteConstraints.builder().build();
  private RoutingPolicy.Builder _policyBuilder;
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
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration baseConfig = cb.build();

    // AsPathAccessList only properly initializes its state upon deserialization
    AsPathAccessList asPath1 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, "^40$"))));
    AsPathAccessList asPath2 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_2, ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, "^50$"))));

    baseConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1, AS_PATH_2, asPath2));

    nf.vrfBuilder().setOwner(baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_NAME);
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

  /** Test that we handle potential nulls in question parameters */
  @Test
  public void testConstructorWithNullsInQuestion() {
    SearchRoutePoliciesAnswerer answerer =
        new SearchRoutePoliciesAnswerer(
            new SearchRoutePoliciesQuestion(
                DEFAULT_DIRECTION,
                DEFAULT_ROUTE_CONSTRAINTS,
                DEFAULT_ROUTE_CONSTRAINTS,
                null,
                null,
                DEFAULT_ACTION),
            _batfish);
    assertEquals(answerer.getNodeSpecifier(), AllNodesNodeSpecifier.INSTANCE);
    assertEquals(answerer.getPolicySpecifier(), ALL_ROUTING_POLICIES);
  }

  @Test
  public void testPermitAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            // the solver will produce the lowest possible value for a field, which is 0.0.0.1 for
            // the next-hop since it is constrained to not be 0.0.0.0
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testPermitAllOut() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            Environment.Direction.OUT,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRoute outputRoute = inputRoute.toBuilder().setNextHopIp(Ip.AUTO).build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(BgpRoute.PROP_NEXT_HOP_IP, "0.0.0.1", Ip.AUTO.toString())));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testInputAPrefix() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testOutputAPrefix() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testDenyNone() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testMatchRegexCommunity() {
    _policyBuilder.addStatement(
        new If(
            matchCommunityRegex("^2[0-9]:30$"),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("!20:30"))))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setCommunities(ImmutableSet.of(StandardCommunity.parse("21:30")))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testMatchGeneralRegexCommunity() {
    _policyBuilder.addStatement(
        new If(
            matchCommunityRegex("^.*$"),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setCommunities(ImmutableSet.of(StandardCommunity.parse("0:0")))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testCommunityConstraints() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("40:33"))))
                .build(),
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("3:44"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(
                ImmutableSet.of(StandardCommunity.of(40, 33), StandardCommunity.of(3, 44)))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testUnsolvableCommunityConstraint() {
    _policyBuilder.setStatements(
        ImmutableList.of(
            new SetCommunities(
                new CommunitySetDifference(
                    InputCommunities.instance(),
                    new CommunityIs(StandardCommunity.parse("20:30")))),
            new StaticStatement(Statements.ExitAccept)));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("20:30"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testDeleteSetCommunityConstraint() {
    _policyBuilder.setStatements(
        ImmutableList.of(
            new SetCommunities(
                new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("2:40")))),
            new StaticStatement(Statements.ExitAccept)));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("1:40"))))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1, 40)))
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2, 40)))
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, "[1:40]", "[2:40]")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testNoOutputCommunities() {
    _policyBuilder.setStatements(
        ImmutableList.of(
            new SetCommunities(
                new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("2:40")))),
            new StaticStatement(Statements.ExitAccept)));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(new RegexConstraint(".*", true))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testMatchNamedAsPath() {
    _policyBuilder.addStatement(
        new If(
            new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setAsPath(AsPath.ofSingletonAsSets(40L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testIncompatibleAsPathMatches() {
    _policyBuilder.addStatement(
        new If(
            new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
            ImmutableList.of(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_2)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(answer.getRows().size(), equalTo(0));
  }

  @Test
  public void testAsPathConstraints() {
    _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("/^40( |$)/"))))
                .build(),
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("/(^| )50$/"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setAsPath(AsPath.ofSingletonAsSets(40L, 50L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testNegatedAsPathConstraints() {
    _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("/^40( |$)/"))))
                .build(),
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("!/(^| )40$/"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setAsPath(AsPath.ofSingletonAsSets(40L, 0L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testSolvableConstraints() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    new PrefixRange(Prefix.parse("1.0.0.0/32"), new SubRange(16, 32)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(8, 16));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testPrefixUnion() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    new PrefixRange(Prefix.parse("1.0.0.0/32"), new SubRange(16, 32)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange1 = new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(8, 12));
    PrefixRange prefixRange2 = new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(13, 16));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setPrefix(new PrefixSpace(prefixRange1, prefixRange2))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testComplementPrefixes() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(
                    new PrefixRange(Prefix.parse("1.0.0.0/32"), new SubRange(31, 32)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setPrefix(new PrefixSpace(prefixRange))
                .setComplementPrefix(true)
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/31"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testSolvableConstraintsDeny() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 32)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(8, 20));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.DENY);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, nullValue(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, nullValue(), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testUnsolvableConstraints() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 31)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testUnsolvableOutConstraints() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 31)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testPermitARoute() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));
    LongSpace localPref = LongSpace.builder().including(45L).build();
    LongSpace med = LongSpace.builder().including(56L).build();
    LongSpace tag = LongSpace.builder().including(67L).build();
    Prefix nextHop = Prefix.parse("1.0.0.0/8");
    Set<RoutingProtocol> protocol = ImmutableSet.of(RoutingProtocol.AGGREGATE);

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setPrefix(new PrefixSpace(prefixRange))
                .setLocalPreference(localPref)
                .setMed(med)
                .setTag(tag)
                .setNextHopIp(nextHop)
                .setProtocol(protocol)
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setLocalPreference(45)
            .setMetric(56)
            .setTag(67)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.AGGREGATE)
            .setNextHopIp(Ip.parse("1.0.0.0"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testSetLocalPref() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetLocalPreference(new LiteralLong(3)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setLocalPreference(3)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_LOCAL_PREFERENCE, "0", "3")));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testComplementCommunities() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 32)))),
            ImmutableList.of(
                new SetCommunities(
                    new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("4:44")))),
                new StaticStatement(Statements.ExitAccept)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(new RegexConstraint("^4:44$", true))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testTwoVersionsOfACommunity() {
    String regex = "^2[0-9]:30$";
    _policyBuilder.addStatement(
        new If(
            matchCommunityRegex(regex),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(ImmutableList.of(new RegexConstraint(regex, true))))
                .build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(0, answer.getRows().size());
  }

  @Test
  public void testNextHopOutConstraints() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    Prefix nextHop = Prefix.parse("1.0.0.0/8");

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(nextHop).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.0.0.0"))
            .build();
    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());
    // in the IN direction, the constraint is solvable since the output
    // route will get the input route's next-hop IP by default
    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));

    question =
        new SearchRoutePoliciesQuestion(
            Environment.Direction.OUT,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(nextHop).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    // in the OUT direction, the constraint is not solvable since the
    // route map does not set the next hop
    assertEquals(0, answer.getRows().size());
  }

  @Test
  public void testSetNextHopOutConstraints() {
    Ip ip = Ip.parse("1.1.1.1");
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(ip))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            Environment.Direction.OUT,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(Prefix.create(ip, 32)).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();
    BgpRoute outputRoute = inputRoute.toBuilder().setNextHopIp(ip).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_NEXT_HOP_IP, "0.0.0.1", "1.1.1.1")));
    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testDiscardNextHopOutConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            Environment.Direction.OUT,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(Prefix.parse("1.0.0.0/8")).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());
    assertEquals(0, answer.getRows().size());
  }

  @Test
  public void testToClosedRange() {
    Range<Long> r1 = toClosedRange(Range.closed(5L, 10L));
    Range<Long> r2 = toClosedRange(Range.closedOpen(5L, 11L));
    Range<Long> r3 = toClosedRange(Range.openClosed(4L, 10L));
    Range<Long> r4 = toClosedRange(Range.open(4L, 11L));

    assertEquals(BoundType.CLOSED, r1.lowerBoundType());
    assertThat(r1.lowerEndpoint(), equalTo(5L));
    assertEquals(BoundType.CLOSED, r1.upperBoundType());
    assertThat(r1.upperEndpoint(), equalTo(10L));

    assertEquals(BoundType.CLOSED, r2.lowerBoundType());
    assertThat(r2.lowerEndpoint(), equalTo(5L));
    assertEquals(BoundType.CLOSED, r2.upperBoundType());
    assertThat(r2.upperEndpoint(), equalTo(10L));

    assertEquals(BoundType.CLOSED, r3.lowerBoundType());
    assertThat(r3.lowerEndpoint(), equalTo(5L));
    assertEquals(BoundType.CLOSED, r3.upperBoundType());
    assertThat(r3.upperEndpoint(), equalTo(10L));

    assertEquals(BoundType.CLOSED, r4.lowerBoundType());
    assertThat(r4.lowerEndpoint(), equalTo(5L));
    assertEquals(BoundType.CLOSED, r4.upperBoundType());
    assertThat(r4.upperEndpoint(), equalTo(10L));
  }

  @Test
  public void testMatchProtocol() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchProtocol(RoutingProtocol.IBGP),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.IBGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }
}
