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
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorHighMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorLowMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityGlobalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.ExtendedCommunityLocalAdministratorMatch;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SiteOfOriginExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityHighMatch;
import org.batfish.datamodel.routing_policy.communities.StandardCommunityLowMatch;
import org.batfish.datamodel.routing_policy.communities.VpnDistinguisherExtendedCommunities;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongComparison;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CommunityMatchExprCollector}. */
public class CommunityMatchExprCollectorTest {

  private static final String HOSTNAME = "hostname";
  private static final String COMM_LST_1 = "comm1";
  private static final String COMM_LST_2 = "comm2";

  private static final Community COMM1 = StandardCommunity.parse("20:30");
  private static final Community COMM2 = StandardCommunity.parse("21:30");

  private CommunityMatchExprCollector _collector;

  private Configuration _config;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _config = cb.build();

    CommunityMatchExpr cyclic =
        new CommunityMatchAny(
            ImmutableSet.of(
                new CommunityMatchExprReference(COMM_LST_2),
                new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^30:")));
    _config.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1,
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"),
            COMM_LST_2,
            cyclic));
    _collector = new CommunityMatchExprCollector();
  }

  @Test
  public void testVisitCommunityAcl() {
    CommunityAcl cacl =
        new CommunityAcl(
            ImmutableList.of(
                new CommunityAclLine(
                    LineAction.PERMIT, new CommunityMatchExprReference(COMM_LST_1)),
                new CommunityAclLine(
                    LineAction.DENY, new CommunityMatchExprReference(COMM_LST_2))));

    Set<String> result = _collector.visitCommunityAcl(cacl, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunityIn() {
    CommunityIn ci = new CommunityIn(new LiteralCommunitySet(CommunitySet.of(COMM1, COMM2)));

    Set<String> result = _collector.visitCommunityIn(ci, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunityIs() {
    CommunityIs ci = new CommunityIs(COMM1);

    Set<String> result = _collector.visitCommunityIs(ci, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunityMatchAll() {
    CommunityMatchAll cma =
        new CommunityMatchAll(
            ImmutableList.of(
                new CommunityIs(COMM1),
                new CommunityMatchExprReference(COMM_LST_1),
                new CommunityMatchExprReference(COMM_LST_2)));

    Set<String> result =
        _collector.visitCommunityMatchAll(cma, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunityMatchAny() {
    CommunityMatchAny cma =
        new CommunityMatchAny(
            ImmutableList.of(
                new CommunityIs(COMM1),
                new CommunityMatchExprReference(COMM_LST_1),
                new CommunityMatchExprReference(COMM_LST_2)));

    Set<String> result =
        _collector.visitCommunityMatchAny(cma, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1, COMM_LST_2), result);
  }

  @Test
  public void testVisitCommunityMatchExprReference() {
    CommunityMatchExprReference cmer = new CommunityMatchExprReference(COMM_LST_1);

    Set<String> result =
        _collector.visitCommunityMatchExprReference(cmer, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_1), result);
  }

  @Test
  public void testVisitCommunityMatchRegex() {
    CommunityMatchRegex cmr = new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:");

    Set<String> result =
        _collector.visitCommunityMatchRegex(cmr, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitCommunityNot() {
    CommunityNot cn = new CommunityNot(new CommunityMatchExprReference(COMM_LST_2));

    Set<String> result = _collector.visitCommunityNot(cn, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(COMM_LST_2), result);
  }

  @Test
  public void testVisitExtendedCommunityGlobalAdministratorHighMatch() {
    ExtendedCommunityGlobalAdministratorHighMatch ec =
        new ExtendedCommunityGlobalAdministratorHighMatch(
            new IntComparison(IntComparator.EQ, new LiteralInt(3)));
    Set<String> result =
        _collector.visitExtendedCommunityGlobalAdministratorHighMatch(
            ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitExtendedCommunityGlobalAdministratorLowMatch() {
    ExtendedCommunityGlobalAdministratorLowMatch ec =
        new ExtendedCommunityGlobalAdministratorLowMatch(
            new IntComparison(IntComparator.EQ, new LiteralInt(3)));
    Set<String> result =
        _collector.visitExtendedCommunityGlobalAdministratorLowMatch(
            ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitExtendedCommunityGlobalAdministratorMatch() {
    ExtendedCommunityGlobalAdministratorMatch ec =
        new ExtendedCommunityGlobalAdministratorMatch(
            new LongComparison(IntComparator.EQ, new LiteralLong(3L)));
    Set<String> result =
        _collector.visitExtendedCommunityGlobalAdministratorMatch(
            ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitExtendedCommunityLocalAdministratorMatch() {
    ExtendedCommunityLocalAdministratorMatch ec =
        new ExtendedCommunityLocalAdministratorMatch(
            new IntComparison(IntComparator.EQ, new LiteralInt(3)));
    Set<String> result =
        _collector.visitExtendedCommunityLocalAdministratorMatch(
            ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitRouteTargetExtendedCommunities() {
    RouteTargetExtendedCommunities ec = RouteTargetExtendedCommunities.instance();
    Set<String> result =
        _collector.visitRouteTargetExtendedCommunities(ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitSiteOfOriginExtendedCommunities() {
    SiteOfOriginExtendedCommunities ec = SiteOfOriginExtendedCommunities.instance();
    Set<String> result =
        _collector.visitSiteOfOriginExtendedCommunities(ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitStandardCommunityHighMatch() {
    StandardCommunityHighMatch schm =
        new StandardCommunityHighMatch(new IntComparison(IntComparator.EQ, new LiteralInt(20)));

    Set<String> result =
        _collector.visitStandardCommunityHighMatch(schm, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitStandardCommunityHighMatchUnsupported() {
    StandardCommunityHighMatch schm =
        new StandardCommunityHighMatch(new IntComparison(IntComparator.LT, new LiteralInt(20)));

    Set<String> result =
        _collector.visitStandardCommunityHighMatch(schm, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitStandardCommunityLowMatch() {
    StandardCommunityLowMatch sclm =
        new StandardCommunityLowMatch(new IntComparison(IntComparator.EQ, new LiteralInt(30)));

    Set<String> result =
        _collector.visitStandardCommunityLowMatch(sclm, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitStandardCommunityLowMatchUnsupported() {
    StandardCommunityLowMatch sclm =
        new StandardCommunityLowMatch(new IntComparison(IntComparator.LT, new LiteralInt(30)));

    Set<String> result =
        _collector.visitStandardCommunityLowMatch(sclm, new Tuple<>(new HashSet<>(), _config));

    assertEquals(ImmutableSet.of(), result);
  }

  @Test
  public void testVisitVpnDistinguisherExtendedCommunities() {
    VpnDistinguisherExtendedCommunities ec = VpnDistinguisherExtendedCommunities.instance();
    Set<String> result =
        _collector.visitVpnDistinguisherExtendedCommunities(
            ec, new Tuple<>(new HashSet<>(), _config));
    assertEquals(ImmutableSet.of(), result);
  }
}
