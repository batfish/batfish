package org.batfish.minesweeper.question.comparepeergrouppolicies;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;
import projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies.RoutingPolicyContextDiff;

public class RoutingPolicyContextDiffTest {
  private static final String HOSTNAME = "hostname";
  private static final String AS_PATH_1 = "asPath1";
  private static final String AS_PATH_2 = "asPath2";
  private static final String PFX_LST_1 = "pfx1";
  private static final String PFX_LST_2 = "pfx2";
  private static final String COMM_LST_1 = "comm1";
  private static final String COMM_LST_2 = "comm2";
  private RoutingPolicyContextDiff _contextDiff;
  private RoutingPolicy.Builder _policyBuilder;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    Configuration baseConfig = cb.build();

    Configuration.Builder db =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    Configuration deltaConfig = db.build();

    // AsPathAccessList only properly initializes its state upon deserialization
    AsPathAccessList asPath1 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^40$"))));
    AsPathAccessList asPath2 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_2, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^50$"))));

    AsPathAccessList asPath1Delta =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^30$"))));
    baseConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1, AS_PATH_2, asPath2));
    deltaConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1Delta, AS_PATH_2, asPath2));

    RouteFilterList f1 =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.1/32")))));
    RouteFilterList f2 =
        new RouteFilterList(
            PFX_LST_2,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/31")))));
    RouteFilterList f1Delta =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.1/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.2/32")))));

    baseConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1, PFX_LST_2, f2));

    // Note: we do not add PFX_LST_2 to the delta config.
    deltaConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1Delta));

    baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"),
            COMM_LST_2, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^0:0$")));
    deltaConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^10:"),
            COMM_LST_2, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^0:0$")));
    _contextDiff = new RoutingPolicyContextDiff(baseConfig, deltaConfig);
    _policyBuilder = nf.routingPolicyBuilder().setOwner(baseConfig).setName("RM1");
  }

  @Test
  public void testAsPathDiff() {
    assertEquals(ImmutableSet.of(AS_PATH_1), _contextDiff.getAsPathAccessListsDiff());
  }

  @Test
  public void testRouteFilterListDiff() {
    assertEquals(ImmutableSet.of(PFX_LST_1, PFX_LST_2), _contextDiff.getRouteFilterListsDiff());
  }

  @Test
  public void testCommunityMatchExprDiff() {
    assertEquals(ImmutableSet.of(COMM_LST_1), _contextDiff.getCommunityListsDiff());
  }

  /**
   * Test that the difference due to the as-path is found if the AS-Path list that differs is used.
   */
  @Test
  public void testDifferOnAsPath() {
    RoutingPolicy p =
        _policyBuilder
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    assertTrue(_contextDiff.differ(p));
  }

  /**
   * Test that the difference due to the community list is found if the community list that differs
   * is used.
   */
  @Test
  public void testDifferOnCommunity() {
    RoutingPolicy p =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(),
                        new HasCommunity(new CommunityMatchExprReference(COMM_LST_1))),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    assertTrue(_contextDiff.differ(p));
  }

  /**
   * Test that the difference due to the route-filter is found if the route-filter list that differs
   * is used.
   */
  @Test
  public void testDifferOnPrefixList() {
    RoutingPolicy p =
        _policyBuilder
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    assertTrue(_contextDiff.differ(p));
  }

  /** Test that differences are ignored if they are not used in the given routing policy */
  @Test
  public void testDifferencesIgnored() {
    RoutingPolicy p =
        _policyBuilder
            .addStatement(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_2)),
                            new MatchCommunities(
                                InputCommunities.instance(),
                                new HasCommunity(new CommunityMatchExprReference(COMM_LST_2))))),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    assertFalse(_contextDiff.differ(p));
  }
}
