package org.batfish.minesweeper.collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CommunityNameCollector}. */
public class CommunityNameCollectorTest {

  private static final String HOSTNAME = "hostname";
  private static final String COMM_LST_1 = "comm1";
  private static final String COMM_LST_2 = "comm2";
  private static final String COMM_LST_3 = "comm3";
  private static final String COMM_LST_4 = "comm4";

  private CommunityNameCollector _collector;

  private Configuration _config;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _config = cb.build();

    CommunitySetMatchExpr cyclic =
        new CommunitySetMatchAny(
            ImmutableSet.of(
                new CommunitySetMatchExprReference(COMM_LST_2),
                new HasCommunity(
                    new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"))));
    _config.setCommunitySetMatchExprs(
        ImmutableMap.of(
            COMM_LST_1,
            new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^10:")),
            COMM_LST_2,
            cyclic));

    CommunitySetExpr cyclicSet =
        CommunitySetUnion.of(
            ImmutableSet.of(
                new CommunitySetReference(COMM_LST_2),
                new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30")))));
    _config.setCommunitySetExprs(
        ImmutableMap.of(
            COMM_LST_1,
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30"))),
            COMM_LST_2,
            cyclicSet,
            COMM_LST_3,
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("30:30"))),
            COMM_LST_4,
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("40:30")))));
    _collector = new CommunityNameCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c =
        new Conjunction(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_1)),
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_2))));

    Set<String> result = _collector.visitConjunction(c, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc =
        new ConjunctionChain(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_1)),
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_2))));

    Set<String> result =
        _collector.visitConjunctionChain(cc, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d =
        new Disjunction(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_1)),
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_2))));

    Set<String> result = _collector.visitDisjunction(d, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc =
        new FirstMatchChain(
            ImmutableList.of(
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_1)),
                new MatchCommunities(
                    InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_2))));

    Set<String> result =
        _collector.visitFirstMatchChain(fmc, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitTrackSucceeded() {
    assertThat(
        _collector.visitTrackSucceeded(
            new TrackSucceeded("foo"), new Tuple<>(new HashSet<>(), _config)),
        empty());
  }

  @Test
  public void testVisitNot() {

    Not n =
        new Not(
            new MatchCommunities(
                InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_1)));

    Set<String> result = _collector.visitNot(n, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1), result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(
        new MatchCommunities(
            InputCommunities.instance(), new CommunitySetMatchExprReference(COMM_LST_1)));
    wee.setPreStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference(COMM_LST_2))));
    wee.setPostStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference(COMM_LST_3))));
    wee.setPostTrueStatements(
        ImmutableList.of(new SetCommunities(new CommunitySetExprReference(COMM_LST_4))));

    Set<String> result =
        _collector.visitWithEnvironmentExpr(wee, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2, COMM_LST_3, COMM_LST_4), result);
  }
}
