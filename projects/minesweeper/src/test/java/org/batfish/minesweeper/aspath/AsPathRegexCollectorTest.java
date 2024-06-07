package org.batfish.minesweeper.aspath;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link AsPathRegexCollector}. */
public class AsPathRegexCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private AsPathRegexCollector _collector;

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

    _collector = new AsPathRegexCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c =
        new Conjunction(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH2))));

    Set<SymbolicAsPathRegex> result =
        _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc =
        new ConjunctionChain(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH2))));

    Set<SymbolicAsPathRegex> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunctionOfAsPathMatches() {

    Disjunction d =
        new Disjunction(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH2))));

    Set<SymbolicAsPathRegex> result =
        _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex("(" + ASPATH1 + ")" + "|(" + ASPATH2 + ")"));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunctionOfNotAllAsPathMatches() {

    Disjunction d =
        new Disjunction(
            ImmutableList.of(
                new Disjunction(
                    BooleanExprs.FALSE,
                    MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1)),
                    MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH2)))));

    Set<SymbolicAsPathRegex> result =
        _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc =
        new FirstMatchChain(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH2))));

    Set<SymbolicAsPathRegex> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchAsPath() {

    MatchAsPath matchAsPath = MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1));

    Set<SymbolicAsPathRegex> result =
        _collector.visitMatchAsPath(matchAsPath, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected = ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1));

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

    Set<SymbolicAsPathRegex> result =
        _collector.visitMatchLegacyAsPath(matchAsPath, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitNot() {

    Not n =
        new Not(
            MatchAsPath.of(
                InputAsPath.instance(),
                AsPathMatchAny.of(
                    ImmutableList.of(AsPathMatchRegex.of(ASPATH1), AsPathMatchRegex.of(ASPATH2)))));

    Set<SymbolicAsPathRegex> result =
        _collector.visitNot(n, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    String asPath3 = "50";
    String asPath4 = "^40 50$";

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH1)));
    wee.setPreStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ASPATH2)),
                ImmutableList.of())));
    wee.setPostStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(asPath3)),
                ImmutableList.of())));
    wee.setPostTrueStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(asPath4)),
                ImmutableList.of())));

    Set<SymbolicAsPathRegex> result =
        _collector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<SymbolicAsPathRegex> expected =
        ImmutableSet.of(ASPATH1, ASPATH2, asPath3, asPath4).stream()
            .map(SymbolicAsPathRegex::new)
            .collect(ImmutableSet.toImmutableSet());

    assertEquals(expected, result);
  }
}
