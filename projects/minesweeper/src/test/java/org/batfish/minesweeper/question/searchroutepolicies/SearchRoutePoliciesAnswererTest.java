package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.toClosedRange;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_ACTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_DIRECTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_PATH_OPTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_ROUTE_CONSTRAINTS;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.PathOption;
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
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.NextHopBgpPeerAddress;
import org.batfish.datamodel.answers.NextHopConcrete;
import org.batfish.datamodel.answers.NextHopSelf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
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
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchClusterListLength;
import org.batfish.datamodel.routing_policy.expr.MatchInterface;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
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
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^40$"))));
    AsPathAccessList asPath2 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_2, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^50$"))));

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
                DEFAULT_ACTION,
                DEFAULT_PATH_OPTION),
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            // the solver will produce the lowest possible value for a field, which is 0.0.0.1 for
            // the next-hop since it is constrained to not be 0.0.0.0
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute = inputRoute.toBuilder().setNextHopIp(Ip.AUTO).build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    new NextHopConcrete(NextHopDiscard.instance()).toString())));

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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            DENY,
            DEFAULT_PATH_OPTION);
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setCommunities(ImmutableSet.of(StandardCommunity.parse("21:30")))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setCommunities(ImmutableSet.of(StandardCommunity.parse("0:0")))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testNamedCommunityConstraints() {

    Configuration baseConfig = _batfish.loadConfigurations(_batfish.getSnapshot()).get(HOSTNAME);
    baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            "cme1",
            new CommunityIs(StandardCommunity.of(40, 33)),
            "cme2",
            new CommunityIs(StandardCommunity.of(3, 44))));

    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint(
                                "cme1", false, RegexConstraint.RegexType.STRUCTURE_NAME))))
                .build(),
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint(
                                "cme2", false, RegexConstraint.RegexType.STRUCTURE_NAME))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMixedCommunityConstraints() {

    Configuration baseConfig = _batfish.loadConfigurations(_batfish.getSnapshot()).get(HOSTNAME);
    baseConfig.setCommunityMatchExprs(
        ImmutableMap.of("cme1", new CommunityIs(StandardCommunity.of(40, 33))));

    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint(
                                "cme1", false, RegexConstraint.RegexType.STRUCTURE_NAME))))
                .build(),
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint("3:44", false, RegexConstraint.RegexType.REGEX))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testNegatedNamedCommunityConstraints() {

    Configuration baseConfig = _batfish.loadConfigurations(_batfish.getSnapshot()).get(HOSTNAME);
    baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            "cme1",
            new CommunityIs(StandardCommunity.of(40, 33)),
            "cme2",
            new CommunityIs(StandardCommunity.of(3, 44))));

    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint(
                                "cme1", true, RegexConstraint.RegexType.STRUCTURE_NAME),
                            new RegexConstraint("3:44", false, RegexConstraint.RegexType.REGEX))))
                .build(),
            BgpRouteConstraints.builder()
                .setCommunities(
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint(
                                "cme2", false, RegexConstraint.RegexType.STRUCTURE_NAME))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setCommunities(ImmutableSet.of(StandardCommunity.of(3, 44)))
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
            PERMIT,
            DEFAULT_PATH_OPTION);
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1, 40)))
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testSetAdministrativeCost() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetAdministrativeCost(new LiteralInt(8)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setAdminDist(0)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setAdminDist(8)
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_ADMINISTRATIVE_DISTANCE, "0", "8")));

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
  public void testSetExtendedCommunity() {
    _policyBuilder.setStatements(
        ImmutableList.of(
            new SetCommunities(
                new LiteralCommunitySet(CommunitySet.of(ExtendedCommunity.parse("0:2:40")))),
            new StaticStatement(Statements.ExitAccept)));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setCommunities(ImmutableSet.of())
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setCommunities(ImmutableSet.of(ExtendedCommunity.parse("0:2:40")))
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, "[]", "[0:2:40]")));

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
  public void testMatchExtendedCommunity() {
    _policyBuilder.addStatement(
        new If(
            new MatchCommunities(
                InputCommunities.instance(),
                new HasCommunity(new CommunityIs(ExtendedCommunity.parse("0:2:40")))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setCommunities(ImmutableSet.of(ExtendedCommunity.parse("0:2:40")))
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setCommunities(ImmutableSet.of(ExtendedCommunity.parse("0:2:40")))
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
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

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(10L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testAsSetsMatchingRanges2() {
    AsSetsMatchingRanges asExpr =
        AsSetsMatchingRanges.of(
            true, true, ImmutableList.of(Range.closed(3123456789L, 4123456789L)));
    _policyBuilder.addStatement(
        new If(
            MatchAsPath.of(InputAsPath.instance(), asExpr),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(3123456789L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(40L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(answer.getRows().size(), equalTo(0));
  }

  @Test
  public void testConstraintLocalPref() {
    _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setLocalPreference(LongSpace.of(200)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(200)
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
            BgpRouteConstraints.builder().build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(40L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testAsPathOutConstraints() {
    _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().build(),
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("/^40( |$)/"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(40L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
                    new RegexConstraints(
                        ImmutableList.of(
                            RegexConstraint.parse("/^40( |$)/"),
                            RegexConstraint.parse("!/(^| )40$/"))))
                .build(),
            BgpRouteConstraints.builder().build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(40L, 0L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testNegatedAsPathOutConstraints() {
    _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().build(),
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(
                        ImmutableList.of(
                            RegexConstraint.parse("/^40( |$)/"),
                            RegexConstraint.parse("!/(^| )40$/"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(40L, 0L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:16-32"))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromString("0.0.0.0/0:8-16");

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:16-32"))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange1 = PrefixRange.fromString("0.0.0.0/0:8-12");
    PrefixRange prefixRange2 = PrefixRange.fromString("0.0.0.0/0:13-16");

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setPrefix(new PrefixSpace(prefixRange1, prefixRange2))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:31-32"))),
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
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/31"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-32"))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromString("0.0.0.0/0:8-20");

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-31"))),
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
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testUnsolvableOutConstraints() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-31"))),
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
            PERMIT,
            DEFAULT_PATH_OPTION);

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
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setLocalPreference(45)
            .setMetric(56)
            .setTag(67)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
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
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setLocalPreference(3)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_LOCAL_PREFERENCE,
                    Long.toString(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE),
                    "3")));

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
  public void testPrependAsPath() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(4L)))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(4L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "[]", "[4]")));

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
  public void testPrependAsPathDeny() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(4L)))))
            .addStatement(new StaticStatement(Statements.ExitReject))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testPrependAsPathWithOutConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(4L)))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(
                        ImmutableList.of(RegexConstraint.parse("/.*( |^)40( |$).*/"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(40L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setAsPath(AsPath.ofSingletonAsSets(4L, 40L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "[40]", "[4, 40]")));

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
  public void testPrependAsPathWithUnsatisfiableConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(4L)))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setAsPath(
                    new RegexConstraints(ImmutableList.of(RegexConstraint.parse("/^40( |$)/"))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(answer.getRows().size(), equalTo(0));
  }

  @Test
  public void testSetWeight() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetWeight(new LiteralInt(3)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setWeight(3)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_WEIGHT, "0", "3")));

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
  public void testSetMetricIncr() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetMetric(new IncrementMetric(100)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setMetric(0)
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setMetric(100)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "0", "100")));

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
  public void testSetMetricIncrOverflow() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetMetric(new IncrementMetric(100L)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setMed(LongSpace.of(0xFFFFFF9CL)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setMetric(0xFFFFFF9CL)
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setMetric(0xFFFFFFFFL)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "4294967196", "4294967295")));

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
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/8:16-32"))),
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
                    new RegexConstraints(
                        ImmutableList.of(
                            new RegexConstraint("^4:44$", true),
                            new RegexConstraint("^5:55$", true))))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);

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
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
            PERMIT,
            DEFAULT_PATH_OPTION);

    answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    // in the OUT direction, the constraint is not solvable since the
    // route map does not set the next hop
    assertEquals(0, answer.getRows().size());
  }

  @Test
  public void testSetNextHopOutputConstraints() {
    Ip ip = Ip.parse("1.1.1.1");
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(ip))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(Prefix.create(ip, 32)).build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();
    BgpRoute outputRoute = inputRoute.toBuilder().setNextHopIp(ip).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).toString())));
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
  public void testDiscardNextHopOutputConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(Prefix.parse("1.0.0.0/8")).build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());
    assertEquals(0, answer.getRows().size());
  }

  @Test
  public void testDiscardNextHopNoConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();
    BgpRoute outputRoute =
        inputRoute.toBuilder().setNextHopConcrete(NextHopDiscard.instance()).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    new NextHopConcrete(NextHopDiscard.instance()).toString())));
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
  public void testDiscardThenSetNextHopOutConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
            .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(Ip.parse("1.1.1.1")))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder().setNextHopIp(Prefix.parse("1.0.0.0/8")).build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();
    BgpRoute outputRoute =
        inputRoute.toBuilder().setNextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).toString())));
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
  public void testUnreachableSetNextHop() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(DiscardNextHop.INSTANCE))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .addStatement(new SetNextHop(new IpNextHop(ImmutableList.of(Ip.parse("1.1.1.1")))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();
    BgpRoute outputRoute =
        inputRoute.toBuilder().setNextHopConcrete(NextHopDiscard.instance()).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    new NextHopConcrete(NextHopDiscard.instance()).toString())));
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
  public void testSelfNextHop() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(SelfNextHop.getInstance()))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute = inputRoute.toBuilder().setNextHop(NextHopSelf.instance()).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    NextHopSelf.instance().toString())));

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
  public void testSelfNextHopConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(SelfNextHop.getInstance()))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setNextHopIp(Prefix.parse("1.1.1.1/32")).build(),
            BgpRouteConstraints.builder().setNextHopIp(Prefix.parse("2.2.2.2/32")).build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute = inputRoute.toBuilder().setNextHop(NextHopSelf.instance()).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).toString(),
                    NextHopSelf.instance().toString())));

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
  public void testBgpPeerAddressNextHop() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(BgpPeerAddressNextHop.getInstance()))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute outputRoute =
        inputRoute.toBuilder().setNextHop(NextHopBgpPeerAddress.instance()).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("0.0.0.1"))).toString(),
                    NextHopBgpPeerAddress.instance().toString())));

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
  public void testOriginConstraints() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setOriginType(ImmutableSet.of(OriginType.INCOMPLETE))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testUnsatOriginConstraints() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            BgpRouteConstraints.builder()
                .setOriginType(ImmutableSet.of(OriginType.EGP, OriginType.IGP))
                .build(),
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

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
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.IBGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMatchClusterListLength() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(2)),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .setClusterList(ImmutableSet.of(0L, 1L))
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
  public void testMatchSourceVrf() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchSourceVrf("source1"),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMatchSourceVrfDeny() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchSourceVrf("source1"),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMatchSourceVrfAtMostOne() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new MatchSourceVrf("source1"), new MatchSourceVrf(("source2")))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testMatchInterface() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchInterface(ImmutableSet.of("int1")),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopConcrete(NextHopInterface.of("int1", Ip.parse("0.0.0.1")))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMatchInterfaceDeny() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchInterface(ImmutableSet.of("int1")),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMatchInterfaceAtMostOne() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new MatchInterface(ImmutableSet.of("int1")),
                            new MatchInterface(ImmutableSet.of("int2")))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
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

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testTrackSucceededDeny() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new TrackSucceeded("mytrack"),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testTrackSucceededNone() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new TrackSucceeded("mytrack"),
                            new Not(new TrackSucceeded(("mytrack"))))),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testUnsupportedStatement() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new ExcludeAsPath(new LiteralAsList(ImmutableList.of())))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    // the unsupported statement is ignored, so we still get an answer
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
  public void testUnsupportedIfStatement() {
    _policyBuilder
        .addStatement(
            new If(
                new MatchCommunities(
                    // currently we only support matching on InputCommunities
                    new LiteralCommunitySet(CommunitySet.empty()),
                    new HasCommunity(
                        org.batfish.datamodel.routing_policy.communities.AllStandardCommunities
                            .instance())),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    // the entire if statement is ignored since its guard contains an unsupported expression
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
  public void testUnsupportedStatementAvoid() {
    _policyBuilder
        .addStatement(
            new If(
                matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:8-16"))),
                ImmutableList.of(
                    new ExcludeAsPath(new LiteralAsList(ImmutableList.of())),
                    new StaticStatement(Statements.ExitAccept))))
        .addStatement(new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromString("1.0.0.0/32:8-24");

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            // the model will prefer the path that does not encounter the unsupported ExcludeAsPath
            // statement
            .setNetwork(Prefix.parse("1.0.0.0/24"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testUnsupportedStatementDeny() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromString("1.0.0.0/32:8-16"))),
            ImmutableList.of(
                new ExcludeAsPath(new LiteralAsList(ImmutableList.of())),
                new StaticStatement(Statements.ExitReject)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.fromString("1.0.0.0/32:8-24");

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder().setPrefix(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            // the model will ignore the unsupported ExcludeAsPath statement
            .setNetwork(Prefix.parse("1.0.0.0/16"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testMatchStaticRoute() {
    _policyBuilder.addStatement(
        new If(
            new MatchProtocol(RoutingProtocol.STATIC),
            ImmutableList.of(
                new SetLocalPreference(new LiteralLong(300)),
                new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    // paths that are only taken by non-BGP input routes are ignored
    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testPermitPerPathNonOverlap() {
    String regex1 = "^2[0-9]:30$";
    String regex2 = "^2[0-9]:40$";
    _policyBuilder.addStatement(
        new If(
            matchCommunityRegex(regex1),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    _policyBuilder.addStatement(
        new If(
            matchCommunityRegex(regex2),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            PathOption.NON_OVERLAP);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute1 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setCommunities(ImmutableSet.of(StandardCommunity.parse("20:30")))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute inputRoute2 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("8.8.8.0/24"))
            .setCommunities(ImmutableSet.of(StandardCommunity.parse("20:40")))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertEquals(answer.getRows().size(), 2);
    assertThat(
        answer.getRows().getData(),
        Matchers.containsInAnyOrder(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute1), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute1), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS)),
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute2), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute2), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testPermitPerPath() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/32")))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            PathOption.PER_PATH);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute1 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute inputRoute2 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRouteDiffs diff = new BgpRouteDiffs(ImmutableSet.of());

    assertEquals(answer.getRows().size(), 2);
    assertThat(
        answer.getRows().getData(),
        Matchers.containsInAnyOrder(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute1), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute1), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS)),
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute2), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute2), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testDenyPerPath() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/32")))),
            ImmutableList.of(new StaticStatement(Statements.ExitReject))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            EMPTY_CONSTRAINTS,
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            DENY,
            PathOption.PER_PATH);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute1 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    BgpRoute inputRoute2 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
            .build();

    assertEquals(answer.getRows().size(), 2);
    assertThat(
        answer.getRows().getData(),
        Matchers.containsInAnyOrder(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute1), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, nullValue(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, nullValue(), Schema.BGP_ROUTE_DIFFS)),
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute2), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, nullValue(), Schema.BGP_ROUTE),
                hasColumn(COL_DIFF, nullValue(), Schema.BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testLessPreferredPrefixesModel() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setPrefix(new PrefixSpace(PrefixRange.fromString("0.0.0.0/0:28-32")))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("128.0.0.0/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
  public void testLessPreferredPrefixesExactModel() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            DEFAULT_DIRECTION,
            BgpRouteConstraints.builder()
                .setPrefix(new PrefixSpace(PrefixRange.fromString("20.0.0.0/31:28-32")))
                .build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            PERMIT,
            DEFAULT_PATH_OPTION);

    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("20.0.0.0/32"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("0.0.0.1"))
            .setLocalPreference(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE)
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
