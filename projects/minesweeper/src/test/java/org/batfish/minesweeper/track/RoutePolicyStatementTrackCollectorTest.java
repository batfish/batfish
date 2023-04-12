package org.batfish.minesweeper.track;

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
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RoutePolicyStatementTrackCollector}. */
public class RoutePolicyStatementTrackCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private RoutePolicyStatementTrackCollector _trackCollector;
  private NetworkFactory _nf;

  private static final TrackSucceeded TRACK1 = new TrackSucceeded("first");
  private static final TrackSucceeded TRACK2 = new TrackSucceeded("second");

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _trackCollector = new RoutePolicyStatementTrackCollector();
  }

  @Test
  public void testVisitBufferedStatement() {
    BufferedStatement bs = new BufferedStatement(new If(TRACK1, ImmutableList.of()));

    Set<String> result =
        _trackCollector.visitBufferedStatement(bs, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(TRACK1.getTrackName()), result);
  }

  @Test
  public void testVisitIf() {
    TrackSucceeded track3 = new TrackSucceeded("third");
    If ifStmt =
        new If(
            TRACK1,
            ImmutableList.of(new If(TRACK2, ImmutableList.of())),
            ImmutableList.of(new If(track3, ImmutableList.of())));

    Set<String> result = _trackCollector.visitIf(ifStmt, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected =
        ImmutableSet.of(TRACK1.getTrackName(), TRACK2.getTrackName(), track3.getTrackName());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitTraceableStatement() {
    TraceableStatement traceableStatement =
        new TraceableStatement(
            TraceElement.of("statement"), ImmutableList.of(new If(TRACK1, ImmutableList.of())));

    assertEquals(
        _trackCollector.visitTraceableStatement(
            traceableStatement, new Tuple<>(new HashSet<>(), _baseConfig)),
        ImmutableSet.of(TRACK1.getTrackName()));
  }

  @Test
  public void testVisitCallStatement() {
    String calledPolicyName = "calledPolicy";

    BufferedStatement bs = new BufferedStatement(new If(TRACK1, ImmutableList.of()));

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(bs)
            .build();

    RoutingPolicy policy =
        _nf.routingPolicyBuilder()
            .setName("BASE_POLICY")
            .addStatement(new CallStatement(calledPolicyName))
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, "BASE_POLICY", policy));

    Set<String> bufferedStmtResult =
        _trackCollector.visitBufferedStatement(bs, new Tuple<>(new HashSet<>(), _baseConfig));
    Set<String> callStmtResult =
        _trackCollector.visitAll(policy.getStatements(), new Tuple<>(new HashSet<>(), _baseConfig));
    assertEquals(bufferedStmtResult, callStmtResult);
  }
}
