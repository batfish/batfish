package org.batfish.minesweeper.collectors;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RoutePolicyStatementCallCollector}. */
public class RoutePolicyStatementCallCollectorTest {
  private static final String HOSTNAME = "hostname";
  private static final String RM1 = "RM1";
  private static final String RM2 = "RM2";
  private Configuration _baseConfig;
  private NetworkFactory _nf;
  private RoutePolicyStatementCallCollector _collector;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new RoutePolicyStatementCallCollector();
  }

  @Test
  public void testVisitBufferedStatement() {
    BufferedStatement bs =
        new BufferedStatement(
            new If(new CallExpr(RM1), ImmutableList.of(Statements.ExitAccept.toStaticStatement())));

    Set<String> result =
        _collector.visitBufferedStatement(bs, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(RM1), result);
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
}
