package org.batfish.minesweeper.collectors;

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
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link AsPathNameBooleanExprCollector}. */
public class AsPathNameBooleanExprCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private AsPathNameBooleanExprCollector _collector;

  private static final String ASPATH_LST_1 = "lst1";
  private static final String ASPATH_LST_2 = "lst2";
  private static final String ASPATH_LST_3 = "lst3";
  private static final String ASPATH_LST_4 = "lst4";

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _baseConfig.setAsPathMatchExprs(
        ImmutableMap.of(
            ASPATH_LST_1,
            AsPathMatchRegex.of(" 10$"),
            ASPATH_LST_2,
            AsPathMatchRegex.of(" 20$"),
            ASPATH_LST_3,
            AsPathMatchRegex.of(" 30$"),
            ASPATH_LST_4,
            AsPathMatchRegex.of(" 40$")));
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new AsPathNameBooleanExprCollector();
  }

  @Test
  public void testVisitConjunction() {
    Conjunction c =
        new Conjunction(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_2))));

    Set<String> result = _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _baseConfig));
    assertEquals(ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2), result);
  }

  @Test
  public void testVisitConjunctionChain() {
    ConjunctionChain cc =
        new ConjunctionChain(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_2))));

    Set<String> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2), result);
  }

  @Test
  public void testVisitDisjunction() {
    Disjunction d =
        new Disjunction(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_2))));

    Set<String> result = _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2), result);
  }

  @Test
  public void testVisitFirstMatchChain() {
    FirstMatchChain fmc =
        new FirstMatchChain(
            ImmutableList.of(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_1)),
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_2))));

    Set<String> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2), result);
  }

  @Test
  public void testVisitMatchAsPath() {
    MatchAsPath matchAsPath =
        MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_1));

    Set<String> result =
        _collector.visitMatchAsPath(matchAsPath, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(ASPATH_LST_1), result);
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
                    new AsPathAccessListLine(LineAction.PERMIT, "40"),
                    new AsPathAccessListLine(LineAction.DENY, "30")))));

    LegacyMatchAsPath matchAsPath = new LegacyMatchAsPath(new NamedAsPathSet(asPathName));

    Set<String> result =
        _collector.visitMatchLegacyAsPath(matchAsPath, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(asPathName), result);
  }

  @Test
  public void testVisitNot() {
    Not n =
        new Not(
            MatchAsPath.of(
                InputAsPath.instance(),
                AsPathMatchAny.of(
                    ImmutableList.of(
                        AsPathMatchExprReference.of(ASPATH_LST_1),
                        AsPathMatchExprReference.of(ASPATH_LST_2)))));

    Set<String> result = _collector.visitNot(n, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2), result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {
    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_1)));
    wee.setPreStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_2)),
                ImmutableList.of())));
    wee.setPostStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_3)),
                ImmutableList.of())));
    wee.setPostTrueStatements(
        ImmutableList.of(
            new If(
                MatchAsPath.of(InputAsPath.instance(), AsPathMatchExprReference.of(ASPATH_LST_4)),
                ImmutableList.of())));

    Set<String> result =
        _collector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _baseConfig));

    assertEquals(ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2, ASPATH_LST_3, ASPATH_LST_4), result);
  }
}
