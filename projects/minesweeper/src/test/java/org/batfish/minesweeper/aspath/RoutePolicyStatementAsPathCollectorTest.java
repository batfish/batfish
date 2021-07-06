package org.batfish.minesweeper.aspath;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.RegexAsPathSetElem;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RoutePolicyStatementAsPathCollector}. */
public class RoutePolicyStatementAsPathCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private RoutePolicyStatementAsPathCollector _asPathCollector;

  private static final String ASPATH1 = " 40$";
  private static final String ASPATH2 = "^$";
  private static final String ASPATH3 = "^50 ";

  private LegacyMatchAsPath makeLegacyMatchAsPath(List<String> regexes) {
    return new LegacyMatchAsPath(
        new ExplicitAsPathSet(
            regexes.stream()
                .map(RegexAsPathSetElem::new)
                .collect(ImmutableList.toImmutableList())));
  }

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _asPathCollector = new RoutePolicyStatementAsPathCollector();
  }

  @Test
  public void testVisitBufferedStatement() {
    BufferedStatement bs =
        new BufferedStatement(
            new If(
                makeLegacyMatchAsPath(ImmutableList.of(ASPATH1, ASPATH2)),
                ImmutableList.of(Statements.ExitAccept.toStaticStatement())));

    Set<SymbolicAsPathRegex> result = _asPathCollector.visitBufferedStatement(bs, _baseConfig);

    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2)),
        result);
  }

  @Test
  public void testVisitIf() {
    If ifstmt =
        new If(
            makeLegacyMatchAsPath(ImmutableList.of(ASPATH1)),
            ImmutableList.of(
                new If(makeLegacyMatchAsPath(ImmutableList.of(ASPATH2)), ImmutableList.of())),
            ImmutableList.of(
                new If(makeLegacyMatchAsPath(ImmutableList.of(ASPATH3)), ImmutableList.of())));
    Set<SymbolicAsPathRegex> result = _asPathCollector.visitIf(ifstmt, _baseConfig);

    assertEquals(
        ImmutableSet.of(
            new SymbolicAsPathRegex(ASPATH1),
            new SymbolicAsPathRegex(ASPATH2),
            new SymbolicAsPathRegex(ASPATH3)),
        result);
  }

  @Test
  public void testVisitTraceableStatement() {
    TraceableStatement traceableStatement =
        new TraceableStatement(
            TraceElement.of("statement"),
            ImmutableList.of(
                new If(makeLegacyMatchAsPath(ImmutableList.of(ASPATH1)), ImmutableList.of()),
                new If(makeLegacyMatchAsPath(ImmutableList.of(ASPATH2)), ImmutableList.of())));

    Set<SymbolicAsPathRegex> result =
        _asPathCollector.visitTraceableStatement(traceableStatement, _baseConfig);
    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2)),
        result);
  }
}
