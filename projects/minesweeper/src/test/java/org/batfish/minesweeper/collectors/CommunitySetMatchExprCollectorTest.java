package org.batfish.minesweeper.collectors;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashSet;
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
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.TypesFirstAscendingSpaceSeparated;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CommunitySetMatchExprCollector}. */
public class CommunitySetMatchExprCollectorTest {
  private static final String HOSTNAME = "hostname";
  private static final String COMM_LST_1 = "comm1";
  private static final String COMM_LST_2 = "comm2";

  private static final Community COMM1 = StandardCommunity.parse("20:30");
  private static final Community COMM2 = StandardCommunity.parse("21:30");

  private CommunitySetMatchExprCollector _collector;

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
    _collector = new CommunitySetMatchExprCollector();
  }

  @Test
  public void testVisitCommunitySetAcl() {
    CommunitySetAcl csacl =
        new CommunitySetAcl(
            ImmutableList.of(
                new CommunitySetAclLine(LineAction.DENY, new HasCommunity(new CommunityIs(COMM1))),
                new CommunitySetAclLine(
                    LineAction.PERMIT, new HasCommunity(new CommunityIs(COMM2)))));

    Set<String> result =
        _collector.visitCommunitySetAcl(csacl, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunitySetMatchAll() {
    CommunitySetMatchAll csma =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new CommunitySetMatchExprReference(COMM_LST_1),
                new CommunitySetMatchExprReference(COMM_LST_2)));

    Set<String> result =
        _collector.visitCommunitySetMatchAll(csma, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunitySetMatchAny() {
    CommunitySetMatchAny csma =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new CommunitySetMatchExprReference(COMM_LST_1),
                new CommunitySetMatchExprReference(COMM_LST_2)));

    Set<String> result =
        _collector.visitCommunitySetMatchAny(csma, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunitySetMatchExprReference() {
    CommunitySetMatchExprReference csmer = new CommunitySetMatchExprReference(COMM_LST_2);

    Set<String> result =
        _collector.visitCommunitySetMatchExprReference(
            csmer, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunitySetMatchRegex() {
    CommunitySetMatchRegex cmsr =
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()),
            "^65000:123 65011:12[3]$");
    Set<String> result = cmsr.accept(_collector, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunitySetNot() {
    CommunitySetNot csn = new CommunitySetNot(new CommunitySetMatchExprReference(COMM_LST_1));

    Set<String> result =
        _collector.visitCommunitySetNot(csn, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1), result);
  }

  @Test
  public void testVisitHasCommunity() {
    HasCommunity hc = new HasCommunity(new CommunityIs(COMM1));

    Set<String> result = _collector.visitHasCommunity(hc, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }
}
