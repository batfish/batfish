package org.batfish.question.testroutepolicies;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE_DIFFS;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.table.TableDiff.baseColumnName;
import static org.batfish.datamodel.table.TableDiff.deltaColumnName;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TestRoutePoliciesAnswerer}. */
public class TestRoutePoliciesAnswererTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private RoutingPolicy.Builder _policyBuilder;
  private RoutingPolicy.Builder _deltaPolicyBuilder;
  private IBatfish _batfish;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
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
            new TestRoutePoliciesQuestion(Direction.IN, ImmutableList.of(), null, null), _batfish);
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
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName());
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    BgpRouteDiffs diffs = new BgpRouteDiffs(ImmutableSet.of());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(COL_OUTPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                // no diff
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
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName());
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(DENY.toString()), Schema.STRING),
                // no outputRoute
                hasColumn(COL_OUTPUT_ROUTE, nullValue(), Schema.BGP_ROUTE))));
  }

  @Test
  public void testChange() {
    RoutingPolicy policy =
        _policyBuilder
            .addStatement(new SetMetric(new LiteralLong(500L)))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .build();
    BgpRoute outputRoute = inputRoute.toBuilder().setMetric(500L).build();
    BgpRouteDiffs diff =
        new BgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "0", "500")));

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, policy.getName());
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(COL_OUTPUT_ROUTE, equalTo(outputRoute), Schema.BGP_ROUTE),
                // diff
                hasColumn(COL_DIFF, equalTo(diff), BGP_ROUTE_DIFFS))));
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
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME);
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
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME);
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
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    BgpRoute baseOutputRoute = inputRoute.toBuilder().setMetric(500).build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME);
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
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(DENY.toString()), Schema.STRING),
                hasColumn(
                    baseColumnName(COL_OUTPUT_ROUTE), equalTo(baseOutputRoute), Schema.BGP_ROUTE),
                hasColumn(deltaColumnName(COL_OUTPUT_ROUTE), nullValue(), Schema.BGP_ROUTE),
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
            .setOriginType(OriginType.IGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setProtocol(RoutingProtocol.BGP)
            .build();

    BgpRoute baseOutputRoute = inputRoute.toBuilder().setMetric(500).build();
    BgpRoute deltaOutputRoute = inputRoute.toBuilder().setMetric(600).build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(
            Direction.IN, ImmutableList.of(inputRoute), HOSTNAME, POLICY_NAME);
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
                hasColumn(COL_INPUT_ROUTE, equalTo(inputRoute), Schema.BGP_ROUTE),
                hasColumn(baseColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(deltaColumnName(COL_ACTION), equalTo(PERMIT.toString()), Schema.STRING),
                hasColumn(
                    baseColumnName(COL_OUTPUT_ROUTE), equalTo(baseOutputRoute), Schema.BGP_ROUTE),
                hasColumn(
                    deltaColumnName(COL_OUTPUT_ROUTE), equalTo(deltaOutputRoute), Schema.BGP_ROUTE),
                // diff
                hasColumn(COL_DIFF, equalTo(diffs), BGP_ROUTE_DIFFS))));
  }
}
