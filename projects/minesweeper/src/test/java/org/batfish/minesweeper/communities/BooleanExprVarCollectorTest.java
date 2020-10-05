package org.batfish.minesweeper.communities;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.minesweeper.CommunityVar;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link BooleanExprVarCollector}. */
public class BooleanExprVarCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private BooleanExprVarCollector _varCollector;

  private static final Community COMM1 = StandardCommunity.parse("20:30");
  private static final Community COMM2 = StandardCommunity.parse("21:30");

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _varCollector = new BooleanExprVarCollector();
  }

  @Test
  public void testVisitConjunction() {

    Conjunction c =
        new Conjunction(
            ImmutableList.of(
                new MatchCommunitySet(new LiteralCommunity(COMM1)),
                new MatchCommunitySet(new LiteralCommunity(COMM2))));

    Set<CommunityVar> result = _varCollector.visitConjunction(c, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitConjunctionChain() {

    ConjunctionChain cc =
        new ConjunctionChain(
            ImmutableList.of(
                new MatchCommunitySet(new LiteralCommunity(COMM1)),
                new MatchCommunitySet(new LiteralCommunity(COMM2))));

    Set<CommunityVar> result = _varCollector.visitConjunctionChain(cc, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitDisjunction() {

    Disjunction d =
        new Disjunction(
            ImmutableList.of(
                new MatchCommunitySet(new LiteralCommunity(COMM1)),
                new MatchCommunitySet(new LiteralCommunity(COMM2))));

    Set<CommunityVar> result = _varCollector.visitDisjunction(d, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitFirstMatchChain() {

    FirstMatchChain fmc =
        new FirstMatchChain(
            ImmutableList.of(
                new MatchCommunitySet(new LiteralCommunity(COMM1)),
                new MatchCommunitySet(new LiteralCommunity(COMM2))));

    Set<CommunityVar> result = _varCollector.visitFirstMatchChain(fmc, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchCommunities() {

    MatchCommunities mc =
        new MatchCommunities(
            new LiteralCommunitySet(CommunitySet.of(COMM1)),
            new HasCommunity(new CommunityIs(COMM2)));

    Set<CommunityVar> result = _varCollector.visitMatchCommunities(mc, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitMatchCommunitySet() {

    MatchCommunitySet mcs = new MatchCommunitySet(new LiteralCommunity(COMM1));

    Set<CommunityVar> result = _varCollector.visitMatchCommunitySet(mcs, _baseConfig);

    Set<CommunityVar> expected = ImmutableSet.of(CommunityVar.from(COMM1));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitNot() {

    Not n = new Not(new MatchCommunitySet(new LiteralCommunity(COMM1)));

    Set<CommunityVar> result = _varCollector.visitNot(n, _baseConfig);

    Set<CommunityVar> expected = ImmutableSet.of(CommunityVar.from(COMM1));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitWithEnvironmentExpr() {

    Community comm3 = StandardCommunity.parse("22:30");
    Community comm4 = StandardCommunity.parse("23:30");

    WithEnvironmentExpr wee = new WithEnvironmentExpr();
    wee.setExpr(new MatchCommunitySet(new LiteralCommunity(COMM1)));
    wee.setPreStatements(ImmutableList.of(new SetCommunity(new LiteralCommunity(COMM2))));
    wee.setPostStatements(ImmutableList.of(new SetCommunity(new LiteralCommunity(comm3))));
    wee.setPostTrueStatements(ImmutableList.of(new SetCommunity(new LiteralCommunity(comm4))));

    Set<CommunityVar> result = _varCollector.visitWithEnvironmentExpr(wee, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(COMM1, COMM2, comm3, comm4).stream()
            .map(CommunityVar::from)
            .collect(ImmutableSet.toImmutableSet());

    assertEquals(expected, result);
  }
}
