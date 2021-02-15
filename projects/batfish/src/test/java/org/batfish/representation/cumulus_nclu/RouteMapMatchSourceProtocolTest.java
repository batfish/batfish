package org.batfish.representation.cumulus_nclu;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.Warnings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EigrpInternalRoute;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.OspfRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RipInternalRoute;
import org.batfish.datamodel.RipRoute;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.eigrp.ClassicMetric;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.representation.cumulus_nclu.RouteMapMatchSourceProtocol.Protocol;
import org.junit.Test;

public class RouteMapMatchSourceProtocolTest {

  @Test
  public void testConstructionAndGetter() {
    RouteMapMatchSourceProtocol match = new RouteMapMatchSourceProtocol(Protocol.BGP);
    assertThat(match.getProtocol(), equalTo(Protocol.BGP));
  }

  private static void assertAction(Protocol p, AbstractRoute route, boolean action) {
    Configuration c = Configuration.builder().setHostname("c").build();
    CumulusNcluConfiguration config = new CumulusNcluConfiguration();
    RouteMapMatchSourceProtocol match = new RouteMapMatchSourceProtocol(p);
    BooleanExpr expr = match.toBooleanExpr(c, config, new Warnings());
    assertThat(
        expr.evaluate(Environment.builder(c).setOriginalRoute(route).build()),
        equalTo(Result.builder().setBooleanValue(action).build()));
  }

  private static void assertPermitted(Protocol p, AbstractRoute route) {
    assertAction(p, route, true);
  }

  private static void assertDenied(Protocol p, AbstractRoute route) {
    assertAction(p, route, false);
  }

  @Test
  public void testToBooleanExpr() {
    Bgpv4Route bgp =
        Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).setProtocol(RoutingProtocol.BGP).build();
    Bgpv4Route ibgp =
        Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).setProtocol(RoutingProtocol.IBGP).build();
    ConnectedRoute connected =
        ConnectedRoute.builder().setNetwork(Prefix.ZERO).setNextHopInterface("eth1").build();
    EigrpInternalRoute eigrp =
        EigrpInternalRoute.testBuilder()
            .setEigrpMetric(
                ClassicMetric.builder()
                    .setValues(EigrpMetricValues.builder().setDelay(5L).build())
                    .build())
            .setProcessAsn(5L)
            .setDestinationAsn(6L)
            .setNetwork(Prefix.ZERO)
            .build();
    IsisRoute isis =
        IsisRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setProtocol(RoutingProtocol.ISIS_L1)
            .setLevel(IsisLevel.LEVEL_1)
            .build();
    KernelRoute kernel = KernelRoute.builder().setNetwork(Prefix.ZERO).build();
    OspfRoute ospf = OspfExternalType1Route.testBuilder().setNetwork(Prefix.ZERO).build();
    StaticRoute sr = StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build();
    RipRoute rip = RipInternalRoute.builder().setNetwork(Prefix.ZERO).build();

    assertDenied(Protocol.BGP, sr);
    assertPermitted(Protocol.BGP, bgp);
    assertPermitted(Protocol.BGP, ibgp);
    assertDenied(Protocol.CONNECTED, sr);
    assertPermitted(Protocol.CONNECTED, connected);
    assertDenied(Protocol.EIGRP, sr);
    assertPermitted(Protocol.EIGRP, eigrp);
    assertDenied(Protocol.ISIS, sr);
    assertPermitted(Protocol.ISIS, isis);
    assertDenied(Protocol.KERNEL, sr);
    assertPermitted(Protocol.KERNEL, kernel);
    assertDenied(Protocol.OSPF, sr);
    assertPermitted(Protocol.OSPF, ospf);
    assertDenied(Protocol.RIP, sr);
    assertPermitted(Protocol.RIP, rip);
    assertDenied(Protocol.STATIC, connected);
    assertPermitted(Protocol.STATIC, sr);
  }
}
