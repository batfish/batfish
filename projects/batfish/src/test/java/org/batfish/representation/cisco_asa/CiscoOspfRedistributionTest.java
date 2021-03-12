package org.batfish.representation.cisco_asa;

import static org.batfish.datamodel.RoutingProtocol.BGP;
import static org.batfish.datamodel.routing_policy.statement.Statements.ExitAccept;
import static org.batfish.representation.cisco_asa.AsaConfiguration.NOT_DEFAULT_ROUTE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoOspfRedistributionTest {
  private static AsaConfiguration makeConfig() {
    AsaConfiguration config = new AsaConfiguration();
    config.setVendor(ConfigurationFormat.CISCO_ASA);
    return config;
  }

  private AsaConfiguration _config;
  private OspfProcess _proc;

  @Before
  public void before() {
    _proc = new OspfProcess("10");
    _config = makeConfig();
  }

  @Test
  public void testBasicConvertRedistributionPolicy() {
    OspfRedistributionPolicy rp = new OspfRedistributionPolicy(BGP);
    rp.setOnlyClassfulRoutes(true);
    rp.setOspfMetricType(OspfMetricType.E2);
    rp.setRouteMap("some-map");
    _config.getRouteMaps().put("some-map", new RouteMap("some-map"));

    If policy = _config.convertOspfRedistributionPolicy(rp, _proc);
    List<BooleanExpr> guard = ((Conjunction) policy.getGuard()).getConjuncts();
    assertThat(
        guard,
        contains(
            new MatchProtocol(BGP),
            NOT_DEFAULT_ROUTE,
            RouteIsClassful.instance(),
            new CallExpr("some-map")));
    assertThat(
        policy.getTrueStatements(),
        contains(
            new SetOspfMetricType(OspfMetricType.E2),
            new SetMetric(new LiteralLong(1L)),
            ExitAccept.toStaticStatement()));
  }

  @Test
  public void testConvertRedistributionPolicyMetric() {
    OspfRedistributionPolicy rp = new OspfRedistributionPolicy(BGP);
    rp.setOspfMetricType(OspfMetricType.E2);

    // Vendor default BGP metric is 1 for IOS.
    If policy = _config.convertOspfRedistributionPolicy(rp, _proc);
    assertThat(policy.getTrueStatements(), hasItem(new SetMetric(new LiteralLong(1L))));

    // Vendor default overridden by process default.
    _proc.setDefaultMetric(3L);
    policy = _config.convertOspfRedistributionPolicy(rp, _proc);
    assertThat(policy.getTrueStatements(), hasItem(new SetMetric(new LiteralLong(3L))));

    // RedistributionPolicy metric configured wins.
    rp.setMetric(5L);
    policy = _config.convertOspfRedistributionPolicy(rp, _proc);
    assertThat(policy.getTrueStatements(), hasItem(new SetMetric(new LiteralLong(5L))));
  }
}
