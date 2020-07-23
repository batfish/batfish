package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_ACTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_INPUT_ROUTE;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_NODE;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_OUTPUT_ROUTE;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_POLICY_NAME;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action.DENY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SearchRoutePoliciesAnswerer}. */
public class SearchRoutePoliciesAnswererTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private static final String COMMUNITY_NAME = "community";
  private static final String REGEX_COMMUNITY_NAME = "regexCommunity";

  private static final RouteConstraints EMPTY_CONSTRAINTS = RouteConstraints.builder().build();
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
    baseConfig.setCommunityLists(
        ImmutableMap.of(
            COMMUNITY_NAME,
            new CommunityList(
                COMMUNITY_NAME,
                ImmutableList.of(
                    CommunityListLine.accepting(
                        new LiteralCommunity(StandardCommunity.parse("20:30")))),
                false),
            REGEX_COMMUNITY_NAME,
            new CommunityList(
                REGEX_COMMUNITY_NAME,
                ImmutableList.of(CommunityListLine.accepting(new RegexCommunitySet("^2[0-9]:30$"))),
                false)));
    nf.vrfBuilder().setOwner(baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_NAME);
    _batfish = new MockBatfish(ImmutableSortedMap.of(HOSTNAME, baseConfig));
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  private MatchCommunitySet matchNamedCommunity(String name) {
    return new MatchCommunitySet(new NamedCommunitySet(name));
  }

  @Test
  public void testPermitAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            EMPTY_CONSTRAINTS, EMPTY_CONSTRAINTS, HOSTNAME, policy.getName(), Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testInputAPrefix() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().setPrefixSpace(new PrefixSpace(prefixRange)).build(),
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testOutputAPrefix() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            EMPTY_CONSTRAINTS,
            RouteConstraints.builder().setPrefixSpace(new PrefixSpace(prefixRange)).build(),
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testDenyNone() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            EMPTY_CONSTRAINTS, EMPTY_CONSTRAINTS, HOSTNAME, policy.getName(), DENY);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testMatchRegexCommunity() {
    _policyBuilder.addStatement(
        new If(
            matchNamedCommunity(REGEX_COMMUNITY_NAME),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder()
                .setCommunities(CommunitySet.of(StandardCommunity.of(20, 30)))
                .setComplementCommunities(true)
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testCommunityConstraints() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder()
                .setCommunities(CommunitySet.of(StandardCommunity.of(40, 33)))
                .build(),
            RouteConstraints.builder()
                .setCommunities(CommunitySet.of(StandardCommunity.of(3, 44)))
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
            .setCommunities(
                ImmutableSet.of(StandardCommunity.of(40, 33), StandardCommunity.of(3, 44)))
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testUnsolvableCommunityConstraint() {
    _policyBuilder.setStatements(
        ImmutableList.of(
            new DeleteCommunity(new NamedCommunitySet(COMMUNITY_NAME)),
            new StaticStatement(Statements.ExitAccept)));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            EMPTY_CONSTRAINTS,
            RouteConstraints.builder()
                .setCommunities(CommunitySet.of(StandardCommunity.of(20, 30)))
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
            new DeleteCommunity(new RegexCommunitySet("^[12]:40$")),
            new SetCommunity(new LiteralCommunity(StandardCommunity.parse("2:40"))),
            new StaticStatement(Statements.ExitAccept)));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder()
                .setCommunities(CommunitySet.of(StandardCommunity.of(1, 40)))
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
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1, 40)))
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2, 40)))
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), Schema.BGP_ROUTE))));
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
            RouteConstraints.builder().setPrefixSpace(new PrefixSpace(prefixRange)).build(),
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
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
            RouteConstraints.builder()
                .setPrefixSpace(new PrefixSpace(prefixRange1, prefixRange2))
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
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
            RouteConstraints.builder()
                .setPrefixSpace(new PrefixSpace(prefixRange))
                .setComplementPrefixSpace(true)
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
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
            RouteConstraints.builder().setPrefixSpace(new PrefixSpace(prefixRange)).build(),
            EMPTY_CONSTRAINTS,
            HOSTNAME,
            policy.getName(),
            Action.DENY);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING))));
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
            RouteConstraints.builder().setPrefixSpace(new PrefixSpace(prefixRange)).build(),
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
            EMPTY_CONSTRAINTS,
            RouteConstraints.builder().setPrefixSpace(new PrefixSpace(prefixRange)).build(),
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
    IntegerSpace localPref = IntegerSpace.builder().including(45).build();
    IntegerSpace med = IntegerSpace.builder().including(56).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder()
                .setPrefixSpace(new PrefixSpace(prefixRange))
                .setLocalPref(localPref)
                .setMed(med)
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
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testComplementCommunities() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 32)))),
            ImmutableList.of(
                new SetCommunity(new LiteralCommunity(StandardCommunity.parse("4:44"))),
                new StaticStatement(Statements.ExitAccept)),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            EMPTY_CONSTRAINTS,
            RouteConstraints.builder()
                .setCommunities(CommunitySet.of(StandardCommunity.of(4, 44)))
                .setComplementCommunities(true)
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
            .build();

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE))));
  }

  @Test
  public void testSatAssignToCommunitiesSat() {
    CommunityVar regex = CommunityVar.other("[234]:50");
    CommunityVar literal = CommunityVar.from(StandardCommunity.of(2, 50));
    BDDRoute r = new BDDRoute(ImmutableSet.of(regex, literal));
    Map<CommunityVar, BDD> commBDDs = r.getCommunities();
    BDD sat = commBDDs.get(regex).and(commBDDs.get(literal).not());

    ResultOrUnsat<Set<Community>> result =
        SearchRoutePoliciesAnswerer.satAssignmentToCommunities(sat, r);

    Community expected = StandardCommunity.of(3, 50);

    assertEquals(ImmutableSet.of(expected), result.getResult());
  }

  @Test
  public void testSatAssignToCommunitiesUnsat() {
    CommunityVar regex = CommunityVar.other("[234]:50");
    CommunityVar regex2 = CommunityVar.other("[12]:50");
    CommunityVar regex3 = CommunityVar.other("[34]:50");
    BDDRoute r = new BDDRoute(ImmutableSet.of(regex, regex2, regex3));
    Map<CommunityVar, BDD> commBDDs = r.getCommunities();
    BDD unsat = commBDDs.get(regex).and(commBDDs.get(regex2).not()).and(commBDDs.get(regex3).not());

    ResultOrUnsat<Set<Community>> result =
        SearchRoutePoliciesAnswerer.satAssignmentToCommunities(unsat, r);

    assertNull(result.getResult());
    assertEquals(unsat, result.getUnsatConstraints());
  }
}
