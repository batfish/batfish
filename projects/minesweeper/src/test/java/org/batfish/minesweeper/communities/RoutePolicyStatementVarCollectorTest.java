package org.batfish.minesweeper.communities;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.minesweeper.CommunityVar;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RoutePolicyStatementVarCollector}. */
public class RoutePolicyStatementVarCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private RoutePolicyStatementVarCollector _varCollector;

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

    _varCollector = new RoutePolicyStatementVarCollector();
  }

  @Test
  public void testVisitAddCommunity() {
    AddCommunity ac = new AddCommunity(new LiteralCommunity(COMM1));

    Set<CommunityVar> result = _varCollector.visitAddCommunity(ac, _baseConfig);

    assertEquals(ImmutableSet.of(CommunityVar.from(COMM1)), result);
  }

  @Test
  public void testVisitBufferedStatement() {
    BufferedStatement bs = new BufferedStatement(new AddCommunity(new LiteralCommunity(COMM1)));

    Set<CommunityVar> result = _varCollector.visitBufferedStatement(bs, _baseConfig);

    assertEquals(ImmutableSet.of(CommunityVar.from(COMM1)), result);
  }

  @Test
  public void testVisitDeleteCommunity() {
    DeleteCommunity dc = new DeleteCommunity(new LiteralCommunity(COMM1));

    Set<CommunityVar> result = _varCollector.visitDeleteCommunity(dc, _baseConfig);

    assertEquals(ImmutableSet.of(CommunityVar.from(COMM1)), result);
  }

  @Test
  public void testVisitIf() {
    If ifStmt =
        new If(
            new MatchCommunitySet(new RegexCommunitySet("^20:")),
            ImmutableList.of(new DeleteCommunity(new LiteralCommunity(COMM1))),
            ImmutableList.of(new AddCommunity(new LiteralCommunity(COMM2))));

    Set<CommunityVar> result = _varCollector.visitIf(ifStmt, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(
            CommunityVar.from("^20:"), CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitSetCommunities() {
    SetCommunities sc = new SetCommunities(new LiteralCommunitySet(CommunitySet.of(COMM1)));

    Set<CommunityVar> result = _varCollector.visitSetCommunities(sc, _baseConfig);

    assertEquals(ImmutableSet.of(CommunityVar.from(COMM1)), result);
  }

  @Test
  public void testVisitSetCommunity() {
    SetCommunity sc = new SetCommunity(new LiteralCommunity(COMM1));

    Set<CommunityVar> result = _varCollector.visitSetCommunity(sc, _baseConfig);

    assertEquals(ImmutableSet.of(CommunityVar.from(COMM1)), result);
  }
}
