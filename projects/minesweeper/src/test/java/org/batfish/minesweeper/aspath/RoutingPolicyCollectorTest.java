package org.batfish.minesweeper.aspath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOriginatorIp;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RoutingPolicyCollector}. */
public class RoutingPolicyCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private NetworkFactory _nf;
  private RoutingPolicyCollector<SymbolicAsPathRegex> _asPathCollector;

  private static final String ASPATH1 = " 40$";
  private static final String ASPATH2 = "^$";
  private static final String ASPATH3 = "^50 ";

  private BooleanExpr makeMatchAsPath(List<String> regexes) {
    return MatchAsPath.of(
        InputAsPath.instance(),
        AsPathMatchAny.of(regexes.stream().map(AsPathMatchRegex::of).collect(Collectors.toList())));
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _asPathCollector = new AsPathRegexCollector();
  }

  @Test
  public void testVisitIf() {
    If ifstmt =
        new If(
            makeMatchAsPath(ImmutableList.of(ASPATH1)),
            ImmutableList.of(
                new If(makeMatchAsPath(ImmutableList.of(ASPATH2)), ImmutableList.of())),
            ImmutableList.of(
                new If(makeMatchAsPath(ImmutableList.of(ASPATH3)), ImmutableList.of())));
    Set<SymbolicAsPathRegex> result =
        _asPathCollector.visitIf(ifstmt, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(
        ImmutableSet.of(
            new SymbolicAsPathRegex(ASPATH1),
            new SymbolicAsPathRegex(ASPATH2),
            new SymbolicAsPathRegex(ASPATH3)),
        result);
  }

  @Test
  public void testVisitSetOriginatorIp() {
    assertEquals(
        _asPathCollector.visitSetOriginatorIp(
            SetOriginatorIp.of(NextHopIp.instance()), new Tuple<>(new HashSet<>(), _baseConfig)),
        ImmutableSet.of());
  }

  @Test
  public void testVisitTraceableStatement() {
    TraceableStatement traceableStatement =
        new TraceableStatement(
            TraceElement.of("statement"),
            ImmutableList.of(
                new If(makeMatchAsPath(ImmutableList.of(ASPATH1)), ImmutableList.of()),
                new If(makeMatchAsPath(ImmutableList.of(ASPATH2)), ImmutableList.of())));

    Set<SymbolicAsPathRegex> result =
        _asPathCollector.visitTraceableStatement(
            traceableStatement, new Tuple<>(new HashSet<>(), _baseConfig));
    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2)),
        result);
  }

  @Test
  public void testVisitExcludeAsPath() {
    ExcludeAsPath excludeAsPath =
        new ExcludeAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));

    assertEquals(
        ImmutableSet.of(),
        _asPathCollector.visitExcludeAsPath(
            excludeAsPath, new Tuple<>(new HashSet<>(), _baseConfig)));
  }

  @Test
  public void testVisitCallStatement() {
    String calledPolicyName = "calledPolicy";

    If iff =
        new If(
            makeMatchAsPath(ImmutableList.of(ASPATH1, ASPATH2)),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement()));

    RoutingPolicy calledPolicy =
        _nf.routingPolicyBuilder()
            .setName(calledPolicyName)
            .setOwner(_baseConfig)
            .addStatement(iff)
            .build();

    RoutingPolicy policy =
        _nf.routingPolicyBuilder()
            .setName("BASE_POLICY")
            .addStatement(new CallStatement(calledPolicyName))
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of(calledPolicyName, calledPolicy, "BASE_POLICY", policy));

    Set<SymbolicAsPathRegex> ifResult =
        _asPathCollector.visitIf(iff, new Tuple<>(new HashSet<>(), _baseConfig));
    Set<SymbolicAsPathRegex> callStmtResult =
        _asPathCollector.visitAll(
            policy.getStatements(), new Tuple<>(new HashSet<>(), _baseConfig));
    assertEquals(ifResult, callStmtResult);
  }

  @Test
  public void testVisitCallMissingPolicy() {
    assertThat(
        _asPathCollector.visitCallExpr(
            new CallExpr("foo"), new Tuple<>(new HashSet<>(), _baseConfig)),
        empty());
  }
}
