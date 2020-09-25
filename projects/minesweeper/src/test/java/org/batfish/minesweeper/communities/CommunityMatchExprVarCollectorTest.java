package org.batfish.minesweeper.communities;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CommunityMatchExprVarCollector}. */
public class CommunityMatchExprVarCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private CommunityMatchExprVarCollector _varCollector;

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

    _varCollector = new CommunityMatchExprVarCollector();
  }

  @Test
  public void testVisitCommunityAcl() {
    CommunityAcl cacl =
        new CommunityAcl(
            ImmutableList.of(
                new CommunityAclLine(LineAction.PERMIT, new CommunityIs(COMM1)),
                new CommunityAclLine(LineAction.DENY, new CommunityIs(COMM2))));

    Set<CommunityVar> result = _varCollector.visitCommunityAcl(cacl, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunityIn() {
    CommunityIn ci = new CommunityIn(new LiteralCommunitySet(CommunitySet.of(COMM1, COMM2)));

    Set<CommunityVar> result = _varCollector.visitCommunityIn(ci, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunityIs() {
    CommunityIs ci = new CommunityIs(COMM1);

    Set<CommunityVar> result = _varCollector.visitCommunityIs(ci, _baseConfig);

    Set<CommunityVar> expected = ImmutableSet.of(CommunityVar.from(COMM1));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunityMatchAll() {
    CommunityMatchAll cma =
        new CommunityMatchAll(ImmutableList.of(new CommunityIs(COMM1), new CommunityIs(COMM2)));

    Set<CommunityVar> result = _varCollector.visitCommunityMatchAll(cma, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunityMatchAny() {
    CommunityMatchAny cma =
        new CommunityMatchAny(ImmutableList.of(new CommunityIs(COMM1), new CommunityIs(COMM2)));

    Set<CommunityVar> result = _varCollector.visitCommunityMatchAny(cma, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunityMatchExprReference() {
    String name = "name";
    _baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(name, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:")));
    CommunityMatchExprReference cmer = new CommunityMatchExprReference(name);

    Set<CommunityVar> result = _varCollector.visitCommunityMatchExprReference(cmer, _baseConfig);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(ImmutableSet.of(cvar), result);
  }

  @Test
  public void testVisitCommunityMatchRegex() {
    CommunityMatchRegex cmr = new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:");

    Set<CommunityVar> result = _varCollector.visitCommunityMatchRegex(cmr, _baseConfig);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(ImmutableSet.of(cvar), result);
  }

  @Test
  public void testVisitCommunityNot() {
    CommunityNot cn = new CommunityNot(new CommunityIs(COMM1));

    Set<CommunityVar> result = _varCollector.visitCommunityNot(cn, _baseConfig);

    CommunityVar cvar = CommunityVar.from(COMM1);

    assertEquals(ImmutableSet.of(cvar), result);
  }

  @Test
  public void testVisitStandardCommunityHighMatch() {
    StandardCommunityHighMatch schm =
        new StandardCommunityHighMatch(new IntComparison(IntComparator.EQ, new LiteralInt(20)));

    Set<CommunityVar> result = _varCollector.visitStandardCommunityHighMatch(schm, _baseConfig);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(ImmutableSet.of(cvar), result);
  }

  @Test
  public void testVisitStandardCommunityLowMatch() {
    StandardCommunityLowMatch sclm =
        new StandardCommunityLowMatch(new IntComparison(IntComparator.EQ, new LiteralInt(30)));

    Set<CommunityVar> result = _varCollector.visitStandardCommunityLowMatch(sclm, _baseConfig);

    CommunityVar cvar = CommunityVar.from(":30$");

    assertEquals(ImmutableSet.of(cvar), result);
  }
}
