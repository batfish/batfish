package org.batfish.minesweeper.aspath;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RegexAsPathSetElem;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link BooleanExprAsPathCollector}. */
public class BooleanExprAsPathCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private BooleanExprAsPathCollector _collector;

  private static final String ASPATH1 = " 40$";
  private static final String ASPATH2 = "^$";

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new BooleanExprAsPathCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c =
        new Conjunction(
            ImmutableList.of(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH1))),
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH2)))));

    Set<SymbolicAsPathRegex> result = _collector.visitConjunction(c, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc =
        new ConjunctionChain(
            ImmutableList.of(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH1))),
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH2)))));

    Set<SymbolicAsPathRegex> result = _collector.visitConjunctionChain(cc, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d =
        new Disjunction(
            ImmutableList.of(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH1))),
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH2)))));

    Set<SymbolicAsPathRegex> result = _collector.visitDisjunction(d, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc =
        new FirstMatchChain(
            ImmutableList.of(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH1))),
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH2)))));

    Set<SymbolicAsPathRegex> result = _collector.visitFirstMatchChain(fmc, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchAsPath() {

    MatchAsPath matchAsPath = MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1));

    Set<SymbolicAsPathRegex> result = _collector.visitMatchAsPath(matchAsPath, _baseConfig);

    Set<SymbolicAsPathRegex> expected = ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchLegacyAsPathExplicit() {

    LegacyMatchAsPath matchAsPath =
        new LegacyMatchAsPath(
            new ExplicitAsPathSet(
                new RegexAsPathSetElem(ASPATH1), new RegexAsPathSetElem(ASPATH2)));

    Set<SymbolicAsPathRegex> result = _collector.visitMatchLegacyAsPath(matchAsPath, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchLegacyAsPathNamed() {

    String asPathName = "name";

    _baseConfig.setAsPathAccessLists(
        ImmutableMap.of(
            asPathName,
            new AsPathAccessList(
                asPathName,
                ImmutableList.of(
                    new AsPathAccessListLine(LineAction.PERMIT, ASPATH1),
                    new AsPathAccessListLine(LineAction.DENY, ASPATH2)))));

    LegacyMatchAsPath matchAsPath = new LegacyMatchAsPath(new NamedAsPathSet(asPathName));

    Set<SymbolicAsPathRegex> result = _collector.visitMatchLegacyAsPath(matchAsPath, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitNot() {

    Not n =
        new Not(
            new LegacyMatchAsPath(
                new ExplicitAsPathSet(
                    new RegexAsPathSetElem(ASPATH1), new RegexAsPathSetElem(ASPATH2))));

    Set<SymbolicAsPathRegex> result = _collector.visitNot(n, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    String asPath3 = "50";
    String asPath4 = "^40 50$";

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH1))));
    wee.setPreStatements(
        ImmutableList.of(
            new If(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(ASPATH2))),
                ImmutableList.of())));
    wee.setPostStatements(
        ImmutableList.of(
            new If(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(asPath3))),
                ImmutableList.of())));
    wee.setPostTrueStatements(
        ImmutableList.of(
            new If(
                new LegacyMatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(asPath4))),
                ImmutableList.of())));

    Set<SymbolicAsPathRegex> result = _collector.visitWithEnvironmentExpr(wee, _baseConfig);

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(ASPATH1, ASPATH2, asPath3, asPath4).stream()
            .map(SymbolicAsPathRegex::new)
            .collect(ImmutableSet.toImmutableSet());

    assertEquals(expected, result);
  }
}
