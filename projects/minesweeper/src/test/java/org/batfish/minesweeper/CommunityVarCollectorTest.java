package org.batfish.minesweeper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.EmptyCommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link CommunityVarCollector} */
public class CommunityVarCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;

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
  }

  @Test
  public void testCommunityList() {
    CommunityList cl =
        new CommunityList(
            "name",
            ImmutableList.of(
                new CommunityListLine(LineAction.DENY, new LiteralCommunity(COMM1)),
                new CommunityListLine(LineAction.PERMIT, new LiteralCommunity(COMM2))),
            true);

    Set<CommunityVar> result = CommunityVarCollector.collectCommunityVars(_baseConfig, cl);

    Set<CommunityVar> expected =
        ImmutableSet.of(toRegexCommunityVar(COMM1), toRegexCommunityVar(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testEmptyCommunitySetExpr() {
    EmptyCommunitySetExpr ecse = EmptyCommunitySetExpr.INSTANCE;

    Set<CommunityVar> result = CommunityVarCollector.collectCommunityVars(_baseConfig, ecse);

    Set<CommunityVar> expected = ImmutableSet.of();

    assertEquals(expected, result);
  }

  @Test
  public void testLiteralCommunity() {
    LiteralCommunity lc = new LiteralCommunity(COMM1);

    Set<CommunityVar> result = CommunityVarCollector.collectCommunityVars(_baseConfig, lc);

    Set<CommunityVar> expected = ImmutableSet.of(CommunityVar.from(COMM1));

    assertEquals(expected, result);
  }

  @Test
  public void testLiteralCommunitySet() {
    LiteralCommunitySet lcs = new LiteralCommunitySet(ImmutableList.of(COMM1, COMM2));

    Set<CommunityVar> result = CommunityVarCollector.collectCommunityVars(_baseConfig, lcs);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testNamedCommunitySet() {
    String name = "name";
    CommunityList cl =
        new CommunityList(
            name,
            ImmutableList.of(
                new CommunityListLine(LineAction.DENY, new LiteralCommunity(COMM1)),
                new CommunityListLine(LineAction.PERMIT, new LiteralCommunity(COMM2))),
            true);

    _baseConfig.setCommunityLists(ImmutableMap.of(name, cl));

    NamedCommunitySet ncs = new NamedCommunitySet(name);

    Set<CommunityVar> result = CommunityVarCollector.collectCommunityVars(_baseConfig, ncs);

    Set<CommunityVar> expected =
        ImmutableSet.of(toRegexCommunityVar(COMM1), toRegexCommunityVar(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testRegexCommunitySet() {
    RegexCommunitySet rcs = new RegexCommunitySet("^20:");

    Set<CommunityVar> result = CommunityVarCollector.collectCommunityVars(_baseConfig, rcs);

    Set<CommunityVar> expected = ImmutableSet.of(CommunityVar.from("^20:"));

    assertEquals(expected, result);
  }

  private static CommunityVar toRegexCommunityVar(Community c) {
    return CommunityVar.from("^" + c.matchString() + "$");
  }
}
