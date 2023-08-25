package org.batfish.minesweeper.collectors;

import static org.junit.Assert.assertEquals;

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
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighLowExprs;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CommunitySetExprCollector}. */
public class CommunitySetExprCollectorTest {
  private static final String HOSTNAME = "hostname";
  private static final String COMM_LST_1 = "comm1";
  private static final String COMM_LST_2 = "comm2";

  private CommunitySetExprCollector _collector;

  private Configuration _config;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _config = cb.build();

    CommunitySetExpr cyclic =
        CommunitySetUnion.of(
            ImmutableSet.of(
                new CommunitySetReference(COMM_LST_2),
                new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30")))));
    _config.setCommunitySetExprs(
        ImmutableMap.of(
            COMM_LST_1,
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30"))),
            COMM_LST_2,
            cyclic));
    _collector = new CommunitySetExprCollector();
  }

  @Test
  public void testVisitCommunityExprsSet() {
    CommunityExprsSet ces =
        CommunityExprsSet.of(
            new StandardCommunityHighLowExprs(new LiteralInt(20), new LiteralInt(30)),
            new StandardCommunityHighLowExprs(new LiteralInt(21), new LiteralInt(30)));

    Set<String> result =
        _collector.visitCommunityExprsSet(ces, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunitySetDifference() {
    CommunitySetDifference csd =
        new CommunitySetDifference(
            new CommunitySetExprReference(COMM_LST_2),
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"));

    Set<String> result =
        _collector.visitCommunitySetDifference(csd, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunitySetExprReference() {
    Set<String> result =
        _collector.visitCommunitySetExprReference(
            new CommunitySetExprReference(COMM_LST_2), new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunitySetReference() {
    Set<String> result =
        _collector.visitCommunitySetReference(
            new CommunitySetReference(COMM_LST_2), new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunitySetUnion() {
    CommunitySetUnion csu =
        CommunitySetUnion.of(
            new CommunitySetExprReference(COMM_LST_1), new CommunitySetExprReference(COMM_LST_2));

    Set<String> result =
        _collector.visitCommunitySetUnion(csu, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitInputCommunities() {

    Set<String> result =
        _collector.visitInputCommunities(
            new InputCommunities(), new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }
}
