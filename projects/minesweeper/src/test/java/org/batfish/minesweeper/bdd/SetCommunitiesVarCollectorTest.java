package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighLowExprs;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SetCommunitiesVarCollector}. */
public class SetCommunitiesVarCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private SetCommunitiesVarCollector _varCollector;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _varCollector = new SetCommunitiesVarCollector();
  }

  @Test
  public void testVisitCommunityExprsSet() {
    CommunityExprsSet ces =
        CommunityExprsSet.of(
            new StandardCommunityHighLowExprs(new LiteralInt(20), new LiteralInt(30)),
            new StandardCommunityHighLowExprs(new LiteralInt(21), new LiteralInt(30)));

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("21:30"));

    Set<CommunityVar> result = _varCollector.visitCommunityExprsSet(ces, _baseConfig);

    assertEquals(ImmutableSet.of(cvar1, cvar2), result);
  }

  @Test
  public void testVisitCommunitySetDifference() {
    CommunitySetDifference csd =
        new CommunitySetDifference(
            new InputCommunities(),
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"));

    Set<CommunityVar> result = _varCollector.visitCommunitySetDifference(csd, _baseConfig);

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunitySetExprReference() {
    String name = "name";
    _baseConfig.setCommunitySetExprs(
        ImmutableMap.of(
            name,
            new LiteralCommunitySet(
                CommunitySet.of(
                    StandardCommunity.parse("20:30"), StandardCommunity.parse("21:30")))));
    CommunitySetExprReference cser = new CommunitySetExprReference(name);

    Set<CommunityVar> result = _varCollector.visitCommunitySetExprReference(cser, _baseConfig);

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("21:30"));

    assertEquals(ImmutableSet.of(cvar1, cvar2), result);
  }

  @Test
  public void testVisitCommunitySetReference() {
    String name = "name";
    _baseConfig.setCommunitySets(
        ImmutableMap.of(
            name,
            CommunitySet.of(StandardCommunity.parse("20:30"), StandardCommunity.parse("21:30"))));
    CommunitySetReference csr = new CommunitySetReference(name);

    Set<CommunityVar> result = _varCollector.visitCommunitySetReference(csr, _baseConfig);

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("21:30"));

    assertEquals(ImmutableSet.of(cvar1, cvar2), result);
  }

  @Test
  public void testVisitCommunitySetUnion() {
    CommunitySetUnion csu =
        CommunitySetUnion.of(
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("20:30"))),
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("21:30"))));

    Set<CommunityVar> result = _varCollector.visitCommunitySetUnion(csu, _baseConfig);

    CommunityVar cvar1 = CommunityVar.from(StandardCommunity.parse("20:30"));
    CommunityVar cvar2 = CommunityVar.from(StandardCommunity.parse("21:30"));

    assertEquals(ImmutableSet.of(cvar1, cvar2), result);
  }
}
