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
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.minesweeper.CommunityVar;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CommunitySetMatchExprVarCollector}. */
public class CommunitySetMatchExprVarCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private CommunitySetMatchExprVarCollector _varCollector;

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

    _varCollector = new CommunitySetMatchExprVarCollector();
  }

  @Test
  public void testVisitCommunitySetAcl() {
    CommunitySetAcl csacl =
        new CommunitySetAcl(
            ImmutableList.of(
                new CommunitySetAclLine(LineAction.DENY, new HasCommunity(new CommunityIs(COMM1))),
                new CommunitySetAclLine(
                    LineAction.PERMIT, new HasCommunity(new CommunityIs(COMM2)))));

    Set<CommunityVar> result = _varCollector.visitCommunitySetAcl(csacl, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunitySetMatchAll() {
    CommunitySetMatchAll csma =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new HasCommunity(new CommunityIs(COMM1)),
                new HasCommunity(new CommunityIs(COMM2))));

    Set<CommunityVar> result = _varCollector.visitCommunitySetMatchAll(csma, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunitySetMatchAny() {
    CommunitySetMatchAny csma =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new HasCommunity(new CommunityIs(COMM1)),
                new HasCommunity(new CommunityIs(COMM2))));

    Set<CommunityVar> result = _varCollector.visitCommunitySetMatchAny(csma, _baseConfig);

    Set<CommunityVar> expected =
        ImmutableSet.of(CommunityVar.from(COMM1), CommunityVar.from(COMM2));

    assertEquals(expected, result);
  }

  @Test
  public void testVisitCommunitySetMatchExprReference() {
    String name = "name";
    _baseConfig.setCommunitySetMatchExprs(
        ImmutableMap.of(
            name,
            new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"))));
    CommunitySetMatchExprReference csmer = new CommunitySetMatchExprReference(name);

    Set<CommunityVar> result =
        _varCollector.visitCommunitySetMatchExprReference(csmer, _baseConfig);

    CommunityVar cvar = CommunityVar.from("^20:");

    assertEquals(ImmutableSet.of(cvar), result);
  }

  @Test
  public void testVisitCommunitySetNot() {
    CommunitySetNot csn = new CommunitySetNot(new HasCommunity(new CommunityIs(COMM1)));

    Set<CommunityVar> result = _varCollector.visitCommunitySetNot(csn, _baseConfig);

    CommunityVar cvar = CommunityVar.from(COMM1);

    assertEquals(ImmutableSet.of(cvar), result);
  }

  @Test
  public void testVisitHasCommunity() {
    HasCommunity hc = new HasCommunity(new CommunityIs(COMM1));

    Set<CommunityVar> result = _varCollector.visitHasCommunity(hc, _baseConfig);

    CommunityVar cvar = CommunityVar.from(COMM1);

    assertEquals(ImmutableSet.of(cvar), result);
  }
}
