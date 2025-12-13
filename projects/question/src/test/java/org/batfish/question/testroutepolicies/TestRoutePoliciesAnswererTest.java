package org.batfish.question.testroutepolicies;

import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_PREFERENCE;
import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE_DIFFS;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.datamodel.table.TableDiff.baseColumnName;
import static org.batfish.datamodel.table.TableDiff.deltaColumnName;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_ACTION;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_DIFF;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_INPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_NODE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_OUTPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_POLICY_NAME;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_TRACE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.toDiffRow;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.answers.NextHopConcrete;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.questions.BgpSessionProperties;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.trace.Tracer;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TestRoutePoliciesAnswerer}. */
public class TestRoutePoliciesAnswererTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";

  private static final BgpSessionProperties BGP_SESSION_PROPERTIES =
      new BgpSessionProperties(22, 33, Ip.parse("2.2.2.2"), Ip.parse("3.3.3.3"));
  private RoutingPolicy.Builder _policyBuilder;
  private RoutingPolicy.Builder _deltaPolicyBuilder;
  private IBatfish _batfish;

  @Before
  public void setup() {
    setup(ConfigurationFormat.CISCO_IOS);
  }

  private void setup(ConfigurationFormat format) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setHostname(HOSTNAME).setConfigurationFormat(format);
    Configuration baseConfig = cb.build();
    nf.vrfBuilder().setOwner(baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_NAME);

    Configuration deltaConfig = cb.build();
    nf.vrfBuilder().setOwner(deltaConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _deltaPolicyBuilder = nf.routingPolicyBuilder().setOwner(deltaConfig).setName(POLICY_NAME);

    _batfish =
        new MockBatfish(
            ImmutableSortedMap.of(HOSTNAME, baseConfig),
            ImmutableSortedMap.of(HOSTNAME, deltaConfig));
  }

  /** Test that we handle potential nulls in question parameters */
  @Test
  public void testConstructorWithNullsInQuestion() {
    TestRoutePoliciesAnswerer answerer =
        new TestRoutePoliciesAnswerer(
            new TestRoutePoliciesQuestion(Direction.IN, ImmutableList.of(), null, null, null),
            _batfish);
    assertEquals(answerer.getNodeSpecifier(), AllNodesNodeSpecifier.INSTANCE);
    assertEquals(answerer.getPolicySpecifier(), ALL_ROUTING_POLICIES);
  }

  @Test
  public void testPermit() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diffs = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                // no diff
                hasColumn(COL_DIFF, equalTo(diffs), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testRouteOrder() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    BgpRoute r1 =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();
    BgpRoute r2 = r1.toBuilder().setNetwork(Prefix.parse("1.2.3.0/24")).build();

    {
      TestRoutePoliciesQuestion question =
          new TestRoutePoliciesQuestion(
              Direction.IN, ImmutableList.of(r1, r2), HOSTNAME, policy.getName(), null);
      TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);
      TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());
      List<Row> rows = answer.getRowsList();
      assertThat(rows, hasSize(2));
      assertThat(
          ((BgpRoute) rows.get(0).get(COL_INPUT_ROUTE, BGP_ROUTE)).getNetwork(),
          equalTo(r1.getNetwork()));
      assertThat(
          ((BgpRoute) rows.get(1).get(COL_INPUT_ROUTE, BGP_ROUTE)).getNetwork(),
          equalTo(r2.getNetwork()));
    }
    {
      TestRoutePoliciesQuestion question =
          new TestRoutePoliciesQuestion(
              Direction.IN, ImmutableList.of(r2, r1), HOSTNAME, policy.getName(), null);
      TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);
      TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());
      List<Row> rows = answer.getRowsList();
      assertThat(rows, hasSize(2));
      assertThat(
          ((BgpRoute) rows.get(0).get(COL_INPUT_ROUTE, BGP_ROUTE)).getNetwork(),
          equalTo(r2.getNetwork()));
      assertThat(
          ((BgpRoute) rows.get(1).get(COL_INPUT_ROUTE, BGP_ROUTE)).getNetwork(),
          equalTo(r1.getNetwork()));
    }
  }

  @Test
  public void testPermitOut() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.OUT, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    BgpRoute outputRoute =
        inputRoute.toBuilder().setNextHopIp(Route.UNSET_ROUTE_NEXT_HOP_IP).build();
    BgpRouteDiffs diffs =
        new BgpRouteDiffs(
            ImmutableSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).toString(),
                    new NextHopConcrete(NextHopDiscard.instance()).toString())));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diffs), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testDeny() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING),
                // no outputRoute
                hasColumn(COL_OUTPUT_ROUTE, nullValue(), BGP_ROUTE))));
  }

  @Test
  public void testChange() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetMetric(new LiteralLong(500L)))
            .addStatement(new SetTag(new LiteralLong(600L)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .build();
    BgpRoute outputRoute = inputRoute.toBuilder().setMetric(500L).setTag(600L).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSortedSet.of(
                new BgpRouteDiff(BgpRoute.PROP_METRIC, "0", "500"),
                new BgpRouteDiff(BgpRoute.PROP_TAG, "0", "600")));

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), BGP_ROUTE),
                // diff
                hasColumn(COL_DIFF, equalTo(diff), BGP_ROUTE_DIFFS),
                hasColumn(
                    COL_TRACE, equalTo(ImmutableList.of()), Schema.list(Schema.TRACE_TREE)))));
  }

  @Test
  public void testNoChange() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setAsPath(AsPath.ofSingletonAsSets(1L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2L)))
            .setLocalPreference(3L)
            .setMetric(4L)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setSrcProtocol(RoutingProtocol.AGGREGATE)
            .setTag(34)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("2.2.2.2")))
            .setWeight(19)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diffs = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                // no diff
                hasColumn(COL_DIFF, equalTo(diffs), BGP_ROUTE_DIFFS))));
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

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setAsPath(AsPath.ofSingletonAsSets(1L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2L)))
            .setLocalPreference(3L)
            .setMetric(4L)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setSrcProtocol(RoutingProtocol.AGGREGATE)
            .setTag(34)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("2.2.2.2")))
            .setWeight(19)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    // the route is denied since it does not have the local pref 42
    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING),
                // no outputRoute
                hasColumn(COL_OUTPUT_ROUTE, nullValue(), BGP_ROUTE))));

    // now try again but with a format that uses the output attributes for matching
    setup(ConfigurationFormat.JUNIPER);
    policy = _policyBuilder.setStatements(stmts).build();
    question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    answer = answerer.answer(_batfish.getSnapshot());

    BgpRoute outputRoute = inputRoute.toBuilder().setMetric(42L).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "4", "42")));

    // the route is accepted since we match on the output attributes
    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testSetAdministrativeDistance() {

    List<Statement> stmts =
        ImmutableList.of(
            new SetAdministrativeCost(new LiteralAdministrativeCost(255)),
            new StaticStatement(Statements.ExitAccept));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setAsPath(AsPath.ofSingletonAsSets(1L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2L)))
            .setLocalPreference(3L)
            .setMetric(4L)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setSrcProtocol(RoutingProtocol.AGGREGATE)
            .setTag(34)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("2.2.2.2")))
            .setWeight(19)
            .setAdminDist(0L)
            .build();

    BgpRoute outputRoute = inputRoute.toBuilder().setAdminDist(255L).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSortedSet.of(
                new BgpRouteDiff(BgpRoute.PROP_ADMINISTRATIVE_DISTANCE, "0", "255")));

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);

    TableAnswerElement answer =
        (new TestRoutePoliciesAnswerer(question, _batfish)).answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), BGP_ROUTE),
                hasColumn(COL_DIFF, equalTo(diff), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testSetNextHopRemoteAddress() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetNextHop(BgpPeerAddressNextHop.getInstance()))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    BgpRoute outputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("3.3.3.3"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN,
            ImmutableList.of(inputRoute),
            HOSTNAME,
            policy.getName(),
            BGP_SESSION_PROPERTIES);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diffs =
        new BgpRouteDiffs(
            ImmutableSortedSet.of(
                new BgpRouteDiff(
                    BgpRoute.PROP_NEXT_HOP,
                    new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).toString(),
                    new NextHopConcrete(NextHopIp.of(Ip.parse("3.3.3.3"))).toString())));

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), BGP_ROUTE),
                // no diff
                hasColumn(COL_DIFF, equalTo(diffs), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testDiffBothDeny() {
    _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    _deltaPolicyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME, null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    // both policies permit and make the same modification, so no difference
    TableAnswerElement diffAnswer =
        (TableAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertEquals(diffAnswer.getRows().size(), 0);
  }

  @Test
  public void testDiffSameChange() {
    _policyBuilder
        .addStatement(new SetMetric(new LiteralLong(500L)))
        .addStatement(new StaticStatement(Statements.ExitAccept))
        .build();

    _deltaPolicyBuilder
        .addStatement(new SetMetric(new LiteralLong(500L)))
        .addStatement(new StaticStatement(Statements.ExitAccept))
        .build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME, null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    // both policies permit and make the same modification, so no difference
    TableAnswerElement diffAnswer =
        (TableAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertEquals(diffAnswer.getRows().size(), 0);
  }

  @Test
  public void testDiffAction() {
    _policyBuilder
        .addStatement(new SetMetric(new LiteralLong(500L)))
        .addStatement(new StaticStatement(Statements.ExitAccept))
        .build();

    _deltaPolicyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    BgpRoute baseOutputRoute = inputRoute.toBuilder().setMetric(500).build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME, null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    // both policies permit and make the same modification, so no difference
    TableAnswerElement diffAnswer =
        (TableAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertThat(
        diffAnswer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), equalTo(baseOutputRoute), BGP_ROUTE),
                hasColumn(deltaColumnName(COL_OUTPUT_ROUTE), nullValue(), BGP_ROUTE),
                // diff
                hasColumn(
                    COL_DIFF, equalTo(new BgpRouteDiffs(ImmutableSet.of())), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testDiffChange() {
    _policyBuilder
        .addStatement(new SetMetric(new LiteralLong(500L)))
        .addStatement(new StaticStatement(Statements.ExitAccept))
        .build();

    _deltaPolicyBuilder
        .addStatement(new SetMetric(new LiteralLong(600L)))
        .addStatement(new StaticStatement(Statements.ExitAccept))
        .build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setProtocol(RoutingProtocol.BGP)
            .build();

    BgpRoute baseOutputRoute = inputRoute.toBuilder().setMetric(500).build();
    BgpRoute deltaOutputRoute = inputRoute.toBuilder().setMetric(600).build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME, null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    BgpRouteDiffs diffs =
        new BgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "600", "500")));

    // both policies permit and make the same modification, so no difference
    TableAnswerElement diffAnswer =
        (TableAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertThat(
        diffAnswer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(POLICY_NAME), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(baseColumnName(COL_OUTPUT_ROUTE), equalTo(baseOutputRoute), BGP_ROUTE),
                hasColumn(deltaColumnName(COL_OUTPUT_ROUTE), equalTo(deltaOutputRoute), BGP_ROUTE),
                // diff
                hasColumn(COL_DIFF, equalTo(diffs), BGP_ROUTE_DIFFS))));
  }

  @Test
  public void testTrace() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(
                new TraceableStatement(
                    TraceElement.of("term"),
                    ImmutableList.of(new StaticStatement(Statements.ExitAccept))))
            .build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName(), null);
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);
    TableAnswerElement answer = answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(
                    COL_TRACE, contains(isTraceTree("term")), Schema.list(Schema.TRACE_TREE)))));
  }

  @Test
  public void testToDiffRow_DiffResultButSame() {
    RoutingPolicyId rpid = new RoutingPolicyId("n", "p");
    Bgpv4Route inputRoute =
        Bgpv4Route.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .build();
    Tracer t = new Tracer();
    t.newSubTrace();
    t.setTraceElement(TraceElement.builder().add("elt").build());
    t.endSubTrace();
    Result ref = new Result(rpid, inputRoute, DENY, null, ImmutableList.of());
    Result snap = new Result(rpid, inputRoute, DENY, null, t.getTrace());

    assertThat(ref, not(equalTo(snap)));
    assertThat(toDiffRow(snap, ref), nullValue());
  }

  @Test
  public void testStaticRouteAccept() {
    List<Statement> stmts =
        ImmutableList.of(
            new If(
                new Conjunction(
                    ImmutableList.of(
                        new MatchProtocol(RoutingProtocol.STATIC),
                        new MatchTag(IntComparator.EQ, new LiteralLong(100)))),
                ImmutableList.of(
                    new SetCommunities(
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(30, 40)))),
                    new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();

    StaticRoute inputRoute =
        StaticRoute.builder()
            .setAdministrativeCost(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(0)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setTag(100)
            .build();

    Result<StaticRoute, Bgpv4Route> result =
        TestRoutePoliciesAnswerer.simulatePolicyWithStaticRoute(
            policy, inputRoute, Direction.IN, null, null);

    Bgpv4Route outputRoute =
        Bgpv4Route.builder()
            .setTag(100)
            .setCommunities(CommunitySet.of(StandardCommunity.of(30, 40)))
            .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.NETWORK)
            .setOriginType(OriginType.INCOMPLETE)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .build();

    assertEquals(result.getInputRoute(), inputRoute);
    assertEquals(result.getAction(), PERMIT);
    assertEquals(result.getOutputRoute(), outputRoute);
  }

  @Test
  public void testStaticRouteDeny() {
    List<Statement> stmts =
        ImmutableList.of(
            new If(
                new Conjunction(
                    ImmutableList.of(
                        new MatchProtocol(RoutingProtocol.STATIC),
                        new MatchTag(IntComparator.EQ, new LiteralLong(100)))),
                ImmutableList.of(
                    new SetCommunities(
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(30, 40)))),
                    new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.setStatements(stmts).build();

    StaticRoute inputRoute =
        StaticRoute.builder()
            .setAdministrativeCost(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(0)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .build();

    Result<StaticRoute, Bgpv4Route> result =
        TestRoutePoliciesAnswerer.simulatePolicyWithStaticRoute(
            policy, inputRoute, Direction.IN, null, null);

    assertEquals(result.getInputRoute(), inputRoute);
    assertEquals(result.getAction(), DENY);
    assertThat(result.getOutputRoute(), nullValue());
  }
}
