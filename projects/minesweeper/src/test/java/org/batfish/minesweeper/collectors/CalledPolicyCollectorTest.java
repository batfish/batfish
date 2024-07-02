package org.batfish.minesweeper.collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CalledPolicyCollector}. */
public class CalledPolicyCollectorTest {
  private static final String HOSTNAME = "hostname";
  private static final String RM1 = "RM1";
  private static final String RM2 = "RM2";
  private static final String RM3 = "RM3";
  private static final String RM4 = "RM4";
  private Configuration _baseConfig;
  private NetworkFactory _nf;
  private CalledPolicyCollector _collector;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new CalledPolicyCollector();
  }

  @Test
  public void testVisitIf() {
    If ifstmt = new If(new CallExpr(RM2), ImmutableList.of(new CallStatement(RM1)));
    Set<String> result = _collector.visitIf(ifstmt, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM1, RM2), result);
  }

  @Test
  public void testVisitTraceableStatement() {
    TraceableStatement traceableStatement =
        new TraceableStatement(
            TraceElement.of("statement"), ImmutableList.of(new CallStatement(RM2)));

    Set<String> result =
        _collector.visitTraceableStatement(
            traceableStatement, new Tuple<>(new HashSet<>(), _baseConfig));
    assertEquals(ImmutableSet.of(RM2), result);
  }

  @Test
  public void testVisitCallStatement() {

    RoutingPolicy rm2 =
        _nf.routingPolicyBuilder()
            .setName(RM2)
            .setOwner(_baseConfig)
            .addStatement(new CallStatement("ignored"))
            .build();

    RoutingPolicy rm1 =
        _nf.routingPolicyBuilder().setName(RM1).addStatement(new CallStatement(RM2)).build();

    _baseConfig.setRoutingPolicies(ImmutableMap.of(RM2, rm2, RM1, rm1));

    assertEquals(
        ImmutableSet.of(RM2),
        _collector.visitAll(rm1.getStatements(), new Tuple<>(new HashSet<>(), _baseConfig)));
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c = new Conjunction(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result = _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc =
        new ConjunctionChain(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d = new Disjunction(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result = _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc =
        new FirstMatchChain(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitTrackSucceeded() {
    assertThat(
        _collector.visitTrackSucceeded(
            new TrackSucceeded("foo"), new Tuple<>(new HashSet<>(), _baseConfig)),
        empty());
  }

  @Test
  public void testVisitNot() {

    Not n = new Not(new CallExpr(RM1));

    Set<String> result = _collector.visitNot(n, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM1), result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(new CallExpr(RM1));
    wee.setPreStatements(ImmutableList.of(new CallStatement(RM2)));
    wee.setPostStatements(ImmutableList.of(new CallStatement(RM3)));
    wee.setPostTrueStatements(ImmutableList.of(new CallStatement(RM4)));

    Set<String> result =
        _collector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM1, RM2, RM3, RM4), result);
  }

  @Test
  public void testCallExprNested() {
    Map<String, RoutingPolicy> routingPolicies = new HashMap<>();
    _baseConfig.setRoutingPolicies(routingPolicies);
    RoutingPolicy rm1 =
        _nf.routingPolicyBuilder()
            .setOwner(_baseConfig)
            .setName("RM1")
            .addStatement(
                new If(
                    new CallExpr(RM2),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    routingPolicies.put(RM1, rm1);

    Set<String> result =
        _collector.visitCallExpr(new CallExpr(RM1), new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM1), result);
  }
}
