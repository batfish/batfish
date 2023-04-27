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
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link BooleanExprTrackCollector}. */
public class BooleanExprTrackCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private BooleanExprTrackCollector _trackCollector;

  private static final TrackSucceeded TRACK1 = new TrackSucceeded("first");
  private static final TrackSucceeded TRACK2 = new TrackSucceeded("second");

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _trackCollector = new BooleanExprTrackCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c = new Conjunction(ImmutableList.of(TRACK1, TRACK2));

    Set<String> result =
        _trackCollector.visitConjunction(c, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of(TRACK1.getTrackName(), TRACK2.getTrackName());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(TRACK1, TRACK2));

    Set<String> result =
        _trackCollector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of(TRACK1.getTrackName(), TRACK2.getTrackName());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d = new Disjunction(ImmutableList.of(TRACK1, TRACK2));

    Set<String> result =
        _trackCollector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of(TRACK1.getTrackName(), TRACK2.getTrackName());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc = new FirstMatchChain(ImmutableList.of(TRACK1, TRACK2));

    Set<String> result =
        _trackCollector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of(TRACK1.getTrackName(), TRACK2.getTrackName());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitTrackSucceeded() {
    assertEquals(
        _trackCollector.visitTrackSucceeded(TRACK1, new Tuple<>(new HashSet<>(), _baseConfig)),
        ImmutableSet.of(TRACK1.getTrackName()));
  }

  @Test
  public void testVisitNot() {

    Not n = new Not(TRACK1);

    Set<String> result = _trackCollector.visitNot(n, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected = ImmutableSet.of(TRACK1.getTrackName());

    assertEquals(expected, result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    TrackSucceeded track3 = new TrackSucceeded("third");
    TrackSucceeded track4 = new TrackSucceeded("fourth");

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(TRACK1);
    wee.setPreStatements(ImmutableList.of(new If(TRACK2, ImmutableList.of())));
    wee.setPostStatements(ImmutableList.of(new If(track3, ImmutableList.of())));
    wee.setPostTrueStatements(ImmutableList.of(new If(track4, ImmutableList.of())));

    Set<String> result =
        _trackCollector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<String> expected =
        ImmutableSet.of(TRACK1, TRACK2, track3, track4).stream()
            .map(TrackSucceeded::getTrackName)
            .collect(ImmutableSet.toImmutableSet());

    assertEquals(expected, result);
  }
}
