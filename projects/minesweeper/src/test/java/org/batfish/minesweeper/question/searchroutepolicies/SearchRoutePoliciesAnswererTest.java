package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_ACTION;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_NODE;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.COL_POLICY_NAME;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action.DENY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteConstraints;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SearchRoutePoliciesAnswerer}. */
public class SearchRoutePoliciesAnswererTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private RoutingPolicy.Builder _policyBuilder;
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

    _batfish =
        new MockBatfish(
            ImmutableSortedMap.of(HOSTNAME, baseConfig),
            ImmutableSortedMap.of(HOSTNAME, deltaConfig));
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  // TODO NEXT: Revert
  // my change to TransferBDD to allow all routes in the output, and update tests.  Then
  // run all bazel tests.  Then try real networks.

  @Test
  public void testPermitAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().build(), HOSTNAME, policy.getName(), Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING))));
  }

  @Test
  public void testPermitAPrefix() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    PrefixRange prefixRange = PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32"));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().setPrefixRange(prefixRange).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING))));
  }

  @Test
  public void testDenyAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().build(), HOSTNAME, policy.getName(), DENY);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testDenyAllButOne() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(ImmutableList.of(PrefixRange.fromPrefix(Prefix.parse("1.0.0.0/8")))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = PrefixRange.ALL;

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().setPrefixRange(prefixRange).build(),
            HOSTNAME,
            policy.getName(),
            DENY);
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
  public void testSolvableConstraints() {
    _policyBuilder.addStatement(
        new If(
            matchPrefixSet(
                ImmutableList.of(new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 32)))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    PrefixRange prefixRange = new PrefixRange(Prefix.parse("0.0.0.0/0"), new SubRange(8, 20));

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().setPrefixRange(prefixRange).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node(HOSTNAME)), Schema.NODE),
                hasColumn(COL_POLICY_NAME, equalTo(policy.getName()), Schema.STRING),
                hasColumn(COL_ACTION, equalTo(PERMIT.toString()), Schema.STRING))));
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
            RouteConstraints.builder().setPrefixRange(prefixRange).build(),
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
            RouteConstraints.builder().setPrefixRange(prefixRange).build(),
            HOSTNAME,
            policy.getName(),
            Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }

  @Test
  public void testPermitNone() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    SearchRoutePoliciesQuestion question =
        new SearchRoutePoliciesQuestion(
            RouteConstraints.builder().build(), HOSTNAME, policy.getName(), Action.PERMIT);
    SearchRoutePoliciesAnswerer answerer = new SearchRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    assertEquals(answer.getRows().size(), 0);
  }
}
