package org.batfish.minesweeper.question.comparepeergrouppolicies;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;
import projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies.SyntacticCompare;

public class SyntacticCompareTest {
  private static final String HOSTNAME = "hostname";
  private static final String AS_PATH_1 = "asPath1";
  private static final String AS_PATH_2 = "asPath2";
  private static final String PFX_LST_1 = "pfx1";
  private static final String PFX_LST_2 = "pfx2";
  private static final String COMM_LST_1 = "comm1";
  private static final String COMM_LST_2 = "comm2";

  private RoutingPolicy.Builder _policyBuilderBase;
  private RoutingPolicy.Builder _policyBuilderDelta;
  private SyntacticCompare _syntacticCompare;
  private Configuration _baseConfig;
  private Configuration _deltaConfig;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _baseConfig = cb.build();

    Configuration.Builder db =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _deltaConfig = db.build();

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
    _baseConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1, AS_PATH_2, asPath2));
    _deltaConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1Delta, AS_PATH_2, asPath2));

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

    _baseConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1, PFX_LST_2, f2));

    // Note: we do not add PFX_LST_2 to the delta config.
    _deltaConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1Delta));

    _baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:"),
            COMM_LST_2, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^0:0$")));
    _deltaConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^10:"),
            COMM_LST_2, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^0:0$")));
    _policyBuilderBase = nf.routingPolicyBuilder().setOwner(_baseConfig).setName("RM1");
    _policyBuilderDelta = nf.routingPolicyBuilder().setOwner(_baseConfig).setName("RM1");
    _syntacticCompare = new SyntacticCompare(_baseConfig, _deltaConfig);
    Map<String, RoutingPolicy> routingPolicies = new HashMap<>();
    Map<String, RoutingPolicy> deltaRoutingPolicies = new HashMap<>();
    _baseConfig.setRoutingPolicies(routingPolicies);
    _deltaConfig.setRoutingPolicies(deltaRoutingPolicies);
  }

  @Test
  public void testNoDifference() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    _baseConfig.getRoutingPolicies().put("RM1", base);
    _deltaConfig.getRoutingPolicies().put("RM1", delta);
    assertTrue(_syntacticCompare.areEqual(base.getName(), delta.getName()));
  }

  @Test
  public void testSyntacticDifference() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();
    _baseConfig.getRoutingPolicies().put("RM1", base);
    _deltaConfig.getRoutingPolicies().put("RM1", delta);
    assertFalse(_syntacticCompare.areEqual(base.getName(), delta.getName()));
  }

  @Test
  public void testContextDifference() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();
    _baseConfig.getRoutingPolicies().put("RM1", base);
    _deltaConfig.getRoutingPolicies().put("RM1", delta);
    assertFalse(_syntacticCompare.areEqual(base.getName(), delta.getName()));
  }

  /** Tests that difference in called policies result in differences in the callers */
  @Test
  public void testCalleeDifference() {
    RoutingPolicy calledPolicyBase =
        _policyBuilderBase
            .setName("RM2")
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();

    RoutingPolicy calledPolicyDelta =
        _policyBuilderBase
            .setName("RM2")
            .setOwner(_deltaConfig)
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();

    RoutingPolicy base = _policyBuilderBase.addStatement(new CallStatement("RM2")).build();
    RoutingPolicy delta = _policyBuilderDelta.addStatement(new CallStatement("RM2")).build();
    _baseConfig.getRoutingPolicies().put("RM1", base);
    _baseConfig.getRoutingPolicies().put("RM2", calledPolicyBase);
    _deltaConfig.getRoutingPolicies().put("RM1", delta);
    _baseConfig.getRoutingPolicies().put("RM2", calledPolicyDelta);
    assertFalse(_syntacticCompare.areEqual(base.getName(), delta.getName()));
  }

  /**
   * Tests that difference in called policies result in differences in the callers even if there are
   * multiple levels of nesting
   */
  @Test
  public void testNestedCalleeDifference() {
    RoutingPolicy calledPolicyBase1 =
        _policyBuilderBase
            .setName("RM2")
            .setOwner(_baseConfig)
            .addStatement(new CallStatement("RM3"))
            .build();

    RoutingPolicy calledPolicyDelta1 =
        _policyBuilderDelta
            .setName("RM2")
            .setOwner(_deltaConfig)
            .addStatement(new CallStatement("RM3"))
            .build();

    RoutingPolicy calledPolicyBase2 =
        _policyBuilderBase
            .setName("RM3")
            .setOwner(_baseConfig)
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();

    RoutingPolicy calledPolicyDelta2 =
        _policyBuilderDelta
            .setName("RM3")
            .setOwner(_deltaConfig)
            .addStatement(
                new If(
                    new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                    ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
            .build();

    RoutingPolicy base = _policyBuilderBase.addStatement(new CallStatement("RM2")).build();
    RoutingPolicy delta = _policyBuilderDelta.addStatement(new CallStatement("RM2")).build();
    _baseConfig.getRoutingPolicies().put("RM1", base);
    _baseConfig.getRoutingPolicies().put("RM2", calledPolicyBase1);
    _baseConfig.getRoutingPolicies().put("RM3", calledPolicyBase2);
    _deltaConfig.getRoutingPolicies().put("RM1", delta);
    _baseConfig.getRoutingPolicies().put("RM2", calledPolicyDelta1);
    _baseConfig.getRoutingPolicies().put("RM2", calledPolicyDelta2);
    assertFalse(_syntacticCompare.areEqual(base.getName(), delta.getName()));
  }
}
