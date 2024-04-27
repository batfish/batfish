package org.batfish.minesweeper.question.comparepeergrouppolicies;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies.RoutingPolicyContextDiff;
import projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies.SyntacticDifference;

public class SyntacticDifferenceTest {
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  Batfish _batfish;

  private static final String HOSTNAME = "hostname";
  private static final String OTHERHOST = "otherhost";
  private static final String POLICY_NAME = "RM1";
  private static final String PFX_LST_1 = "pfx1";
  private static final String PFX_LST_2 = "pfx2";

  private RoutingPolicy.Builder _policyBuilderDelta;
  private RoutingPolicy.Builder _policyBuilderDeltaOther;
  private RoutingPolicy.Builder _policyBuilderBase;
  private RoutingPolicy.Builder _policyBuilderBaseOther;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();

    Configuration baseConfig =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED)
            .build();
    Configuration baseOtherConfig =
        nf.configurationBuilder()
            .setHostname(OTHERHOST)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED)
            .build();

    Configuration deltaConfig =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED)
            .build();
    Configuration deltaOtherConfig =
        nf.configurationBuilder()
            .setHostname(OTHERHOST)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED)
            .build();

    RouteFilterList f1 =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32")))));
    RouteFilterList f2 =
        new RouteFilterList(
            PFX_LST_2,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/31")))));
    RouteFilterList f1Delta =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32")))));

    baseConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1, PFX_LST_2, f2));
    baseOtherConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1, PFX_LST_2, f2));
    deltaConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1Delta, PFX_LST_2, f2));
    deltaOtherConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1Delta, PFX_LST_2, f2));

    _policyBuilderBase = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_NAME);
    _policyBuilderBaseOther =
        nf.routingPolicyBuilder().setOwner(baseOtherConfig).setName(POLICY_NAME);
    _policyBuilderDelta = nf.routingPolicyBuilder().setOwner(deltaConfig).setName(POLICY_NAME);
    _policyBuilderDeltaOther =
        nf.routingPolicyBuilder().setOwner(deltaOtherConfig).setName(POLICY_NAME);

    _batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(HOSTNAME, baseConfig, OTHERHOST, baseOtherConfig),
            ImmutableSortedMap.of(HOSTNAME, deltaConfig, OTHERHOST, deltaOtherConfig),
            _tempFolder);
  }

  @Test
  public void testSameDifferenceBothDevices() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy baseOther =
        _policyBuilderBaseOther
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();
    RoutingPolicy deltaOther =
        _policyBuilderDeltaOther
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    SyntacticDifference d1 = new SyntacticDifference(base, delta);
    SyntacticDifference d2 = new SyntacticDifference(baseOther, deltaOther);
    assertEquals(0, d1.compareTo(d2));
  }

  @Test
  public void testSameDifferenceDifferentName() {
    RoutingPolicy base =
        _policyBuilderBase
            .setName("NEW_RM")
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy baseOther =
        _policyBuilderBaseOther
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();
    RoutingPolicy deltaOther =
        _policyBuilderDeltaOther
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    SyntacticDifference d1 = new SyntacticDifference(base, delta);
    SyntacticDifference d2 = new SyntacticDifference(baseOther, deltaOther);
    assertEquals(0, d1.compareTo(d2));
  }

  @Test
  public void testDifferentDifference_current() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(new SetLocalPreference(new LiteralLong(100L)))
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy baseOther =
        _policyBuilderBaseOther
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();
    RoutingPolicy deltaOther =
        _policyBuilderDeltaOther
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    SyntacticDifference d1 = new SyntacticDifference(base, delta);
    SyntacticDifference d2 = new SyntacticDifference(baseOther, deltaOther);
    assertNotEquals(0, d1.compareTo(d2));
  }

  @Test
  public void testDifferentDifference_reference() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy baseOther =
        _policyBuilderBaseOther
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();
    RoutingPolicy deltaOther =
        _policyBuilderDeltaOther
            .addStatement(new SetLocalPreference(new LiteralLong(100L)))
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    SyntacticDifference d1 = new SyntacticDifference(base, delta);
    SyntacticDifference d2 = new SyntacticDifference(baseOther, deltaOther);
    assertNotEquals(0, d1.compareTo(d2));
  }

  /** The routing policies are equal but the contexts differ */
  @Test
  public void testDifferentDifference_context() {
    RoutingPolicy base =
        _policyBuilderBase
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy baseOther =
        _policyBuilderBaseOther
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();
    RoutingPolicy delta =
        _policyBuilderDelta
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();
    RoutingPolicy deltaOther =
        _policyBuilderDeltaOther
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    base.getOwner().setRouteFilterLists(ImmutableMap.of());

    SyntacticDifference d1 = new SyntacticDifference(base, delta);
    SyntacticDifference d2 = new SyntacticDifference(baseOther, deltaOther);
    assertEquals(0, d1.compareTo(d2));

    // But they will differ if we take contexts into consideration.
    SyntacticDifference d1Context =
        new SyntacticDifference(
            base, delta, new RoutingPolicyContextDiff(base.getOwner(), delta.getOwner()));
    SyntacticDifference d2Context =
        new SyntacticDifference(
            baseOther,
            deltaOther,
            new RoutingPolicyContextDiff(baseOther.getOwner(), deltaOther.getOwner()));
    assertNotEquals(0, d1Context.compareTo(d2Context));
  }

  /* Differences in nested calls do not matter. */
  @Test
  public void testDifference_recursive() {
    RoutingPolicy calledPolicyBase =
        _policyBuilderBase
            .setName("RM2")
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy calledPolicyDelta =
        _policyBuilderDelta
            .setName("RM2")
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy calledPolicyBaseOther =
        _policyBuilderBaseOther
            .setName("RM2")
            .addStatement(new Statements.StaticStatement(Statements.ExitReject))
            .build();

    RoutingPolicy calledPolicyDeltaOther =
        _policyBuilderDeltaOther
            .setName("RM2")
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy base = _policyBuilderBase.addStatement(new CallStatement("RM2")).build();
    RoutingPolicy baseOther =
        _policyBuilderBaseOther.addStatement(new CallStatement("RM2")).build();
    RoutingPolicy delta = _policyBuilderDelta.addStatement(new CallStatement("RM2")).build();
    RoutingPolicy deltaOther =
        _policyBuilderDeltaOther.addStatement(new CallStatement("RM2")).build();
    base.getOwner().getRoutingPolicies().put("RM1", base);
    base.getOwner().getRoutingPolicies().put("RM2", calledPolicyBase);
    baseOther.getOwner().getRoutingPolicies().put("RM1", baseOther);
    baseOther.getOwner().getRoutingPolicies().put("RM2", calledPolicyBaseOther);

    delta.getOwner().getRoutingPolicies().put("RM1", delta);
    delta.getOwner().getRoutingPolicies().put("RM2", calledPolicyDelta);
    deltaOther.getOwner().getRoutingPolicies().put("RM1", deltaOther);
    deltaOther.getOwner().getRoutingPolicies().put("RM2", calledPolicyDeltaOther);

    // Compare the differences between the two callers.
    SyntacticDifference d1 = new SyntacticDifference(base, delta);
    SyntacticDifference d2 = new SyntacticDifference(baseOther, deltaOther);
    assertEquals(0, d1.compareTo(d2));

    // If we change the called reference policy on one of the two devices, then the two differences
    // remain unchanged.
    calledPolicyDeltaOther.setStatements(
        ImmutableList.of(
            new Statements.StaticStatement(Statements.ExitAccept),
            new Statements.StaticStatement(Statements.ExitAccept)));
    assertEquals(0, d1.compareTo(d2));

    // Likewise, if we change the called base policy. (first restore the delta policy)
    calledPolicyDeltaOther.setStatements(
        ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept)));
    calledPolicyBase.setStatements(
        ImmutableList.of(
            new Statements.StaticStatement(Statements.ExitAccept),
            new Statements.StaticStatement(Statements.ExitAccept)));

    assertEquals(0, d1.compareTo(d2));
  }
}
