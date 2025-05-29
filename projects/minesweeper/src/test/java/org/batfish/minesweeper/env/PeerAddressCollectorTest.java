package org.batfish.minesweeper.env;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.MatchPeerAddress;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link org.batfish.minesweeper.env.PeerAddressCollector}. */
public class PeerAddressCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private PeerAddressCollector _collector;

  private static final Ip ONES = Ip.parse("1.1.1.1");
  private static final Ip TWOS = Ip.parse("2.2.2.2");
  private static final Ip THREES = Ip.parse("3.3.3.3");
  private static final MatchPeerAddress PA1 = new MatchPeerAddress(ONES, TWOS);
  private static final MatchPeerAddress PA2 = new MatchPeerAddress(THREES);

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new PeerAddressCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c = new Conjunction(ImmutableList.of(PA1, PA2));

    Set<Ip> result = _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<Ip> expected = ImmutableSet.of(ONES, TWOS, THREES);

    assertEquals(expected, result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(PA1, PA2));

    Set<Ip> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<Ip> expected = ImmutableSet.of(ONES, TWOS, THREES);

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d = new Disjunction(ImmutableList.of(PA1, PA2));

    Set<Ip> result = _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<Ip> expected = ImmutableSet.of(ONES, TWOS, THREES);

    assertEquals(expected, result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc = new FirstMatchChain(ImmutableList.of(PA1, PA2));

    Set<Ip> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<Ip> expected = ImmutableSet.of(ONES, TWOS, THREES);

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchPeerAddress() {
    assertEquals(
        _collector.visitMatchPeerAddress(PA1, new Tuple<>(new HashSet<>(), _baseConfig)),
        ImmutableSet.of(ONES, TWOS));
  }

  @Test
  public void testVisitNot() {

    Not n = new Not(PA1);

    Set<Ip> result = _collector.visitNot(n, new Tuple<>(new HashSet<>(), _baseConfig));

    Set<Ip> expected = ImmutableSet.of(ONES, TWOS);

    assertEquals(expected, result);
  }
}
