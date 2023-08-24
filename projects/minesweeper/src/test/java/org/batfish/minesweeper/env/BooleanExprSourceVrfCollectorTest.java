package org.batfish.minesweeper.env;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link BooleanExprSourceVrfCollector}. */
public class BooleanExprSourceVrfCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private BooleanExprSourceVrfCollector _collector;

  private static final MatchSourceVrf VRF1 = new MatchSourceVrf("first");
  private static final MatchSourceVrf VRF2 = new MatchSourceVrf("second");

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new BooleanExprSourceVrfCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c = new Conjunction(ImmutableList.of(VRF1, VRF2));

    Set<String> result = _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of("first", "second");

    assertEquals(expected, result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(VRF1, VRF2));

    Set<String> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of("first", "second");

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d = new Disjunction(ImmutableList.of(VRF1, VRF2));

    Set<String> result = _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of("first", "second");

    assertEquals(expected, result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc = new FirstMatchChain(ImmutableList.of(VRF1, VRF2));

    Set<String> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of("first", "second");

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchSourceVrf() {
    assertEquals(
        _collector.visitMatchSourceVrf(VRF1, new Tuple<>(new HashSet<>(), _baseConfig)),
        ImmutableSet.of("first"));
  }

  @Test
  public void testVisitNot() {

    Not n = new Not(VRF1);

    Set<String> result = _collector.visitNot(n, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of("first");

    assertEquals(expected, result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    MatchSourceVrf vrf3 = new MatchSourceVrf("third");
    MatchSourceVrf vrf4 = new MatchSourceVrf("fourth");

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(VRF1);
    wee.setPreStatements(ImmutableList.of(new If(VRF2, ImmutableList.of())));
    wee.setPostStatements(ImmutableList.of(new If(vrf3, ImmutableList.of())));
    wee.setPostTrueStatements(ImmutableList.of(new If(vrf4, ImmutableList.of())));

    Set<String> result =
        _collector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of("first", "second", "third", "fourth");

    assertEquals(expected, result);
  }
}
