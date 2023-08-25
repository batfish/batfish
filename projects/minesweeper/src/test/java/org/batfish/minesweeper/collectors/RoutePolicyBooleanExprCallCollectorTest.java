package org.batfish.minesweeper.collectors;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
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
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

public class RoutePolicyBooleanExprCallCollectorTest {
  private static final String HOSTNAME = "hostname";
  private static final String RM1 = "RM1";
  private static final String RM2 = "RM2";
  private static final String RM3 = "RM3";
  private static final String RM4 = "RM4";
  private RoutePolicyBooleanExprCallCollector _collector;
  private NetworkFactory _nf;
  private Configuration _config;

  @Before
  public void setup() throws IOException {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _config = cb.build();

    _collector = new RoutePolicyBooleanExprCallCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c = new Conjunction(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result = _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc =
        new ConjunctionChain(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d = new Disjunction(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result = _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc =
        new FirstMatchChain(ImmutableList.of(new CallExpr(RM2), new CallExpr(RM3)));

    Set<String> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(RM2, RM3), result);
  }

  @Test
  public void testVisitTrackSucceeded() {
    assertThat(
        _collector.visitTrackSucceeded(
            new TrackSucceeded("foo"), new Tuple<>(new HashSet<>(), _config)),
        empty());
  }

  @Test
  public void testVisitNot() {

    Not n = new Not(new CallExpr(RM1));

    Set<String> result = _collector.visitNot(n, new Tuple<>(new HashSet<>(), _config));

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
        _collector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(RM1, RM2, RM3, RM4), result);
  }

  @Test
  public void testCallExprNested() {
    Map<String, RoutingPolicy> routingPolicies = new HashMap<>();
    _config.setRoutingPolicies(routingPolicies);
    RoutingPolicy rm1 =
        _nf.routingPolicyBuilder()
            .setOwner(_config)
            .setName("RM1")
            .addStatement(
                new If(
                    new CallExpr(RM2),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    routingPolicies.put(RM1, rm1);

    Set<String> result =
        _collector.visitCallExpr(new CallExpr(RM1), new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(RM1), result);
  }
}
