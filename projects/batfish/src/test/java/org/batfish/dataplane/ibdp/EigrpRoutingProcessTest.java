package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.EigrpInternalRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.eigrp.ClassicMetric;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.dataplane.rib.RibDelta;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link EigrpRoutingProcess} */
public class EigrpRoutingProcessTest {

  Configuration _c;
  EigrpProcess _process;
  EigrpRoutingProcess _routingProcess;
  private EigrpExternalRoute.Builder _externalRouteBuilder;
  private EigrpInternalRoute.Builder _internalRouteBuilder;
  private ClassicMetric _ifaceMetric;

  @Before
  public void setUp() {
    _c =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    _process =
        EigrpProcess.builder()
            .setAsNumber(1)
            .setMode(EigrpProcessMode.CLASSIC)
            .setMetricVersion(EigrpMetricVersion.V1)
            .setRouterId(Ip.ZERO)
            .build();
    _routingProcess = new EigrpRoutingProcess(_process, "vrf", RoutingPolicies.from(_c));
    _ifaceMetric =
        ClassicMetric.builder()
            .setValues(
                EigrpMetricValues.builder()
                    .setBandwidth(1)
                    .setDelay(2)
                    .setReliability(3)
                    .setEffectiveBandwidth(4)
                    .setMtu(5)
                    .build())
            .build();
    ClassicMetric metric =
        ClassicMetric.builder()
            .setValues(
                EigrpMetricValues.builder()
                    .setBandwidth(1)
                    .setDelay(2)
                    .setReliability(3)
                    .setEffectiveBandwidth(4)
                    .setMtu(5)
                    .build())
            .build();
    _externalRouteBuilder =
        EigrpExternalRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(222)
            .setProcessAsn(1L)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setDestinationAsn(2L)
            .setEigrpMetric(metric)
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setTag(3L);
    _internalRouteBuilder =
        EigrpInternalRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(222)
            .setProcessAsn(1L)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setEigrpMetric(metric)
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setTag(3L);
  }

  @Test
  public void testRedistributeNoPolicy() {
    // Do not crash
    _routingProcess.redistribute(
        RibDelta.adding(
            new AnnotatedRoute<>(
                ConnectedRoute.builder()
                    .setNetwork(Prefix.parse("1.1.1.0/24"))
                    .setNextHopInterface("Eth0")
                    .build(),
                "vrf")));
  }

  @Test
  public void testTransformAndFilterExternalRouteFromNeighborBlocked() {
    RoutingPolicy rp =
        RoutingPolicy.builder()
            .setName("rp")
            .setOwner(_c)
            .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
            .build();
    EigrpExternalRoute routeIn = _externalRouteBuilder.build();
    assertFalse(
        _routingProcess
            .transformAndFilterExternalRouteFromNeighbor(
                routeIn, _ifaceMetric, Ip.parse("9.9.9.9"), rp)
            .isPresent());
  }

  @Test
  public void testTransformAndFilterExternalRouteFromNeighborAllowed() {
    RoutingPolicy rp =
        RoutingPolicy.builder()
            .setName("rp")
            .setOwner(_c)
            .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
            .build();
    EigrpExternalRoute routeIn = _externalRouteBuilder.build();

    Ip ip = Ip.parse("9.9.9.9");
    Optional<EigrpExternalRoute> maybeRoute =
        _routingProcess.transformAndFilterExternalRouteFromNeighbor(routeIn, _ifaceMetric, ip, rp);
    assertTrue(maybeRoute.isPresent());
    EigrpExternalRoute route = maybeRoute.get();
    EigrpExternalRoute expected =
        EigrpExternalRoute.testBuilder()
            .setNextHopIp(ip)
            .setAdmin(_process.getExternalAdminCost())
            .setEigrpMetric(routeIn.getEigrpMetric().add(_ifaceMetric))
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setProcessAsn(_process.getAsn())
            .setTag(routeIn.getTag())
            .setNetwork(routeIn.getNetwork())
            .setDestinationAsn(routeIn.getDestinationAsn())
            .build();
    assertThat(route, equalTo(expected));
  }

  @Test
  public void testTransformAndFilterInternalRouteFromNeighborBlocked() {
    RoutingPolicy rp =
        RoutingPolicy.builder()
            .setName("rp")
            .setOwner(_c)
            .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
            .build();
    EigrpExternalRoute routeIn = _externalRouteBuilder.build();
    assertFalse(
        _routingProcess
            .transformAndFilterExternalRouteFromNeighbor(
                routeIn, _ifaceMetric, Ip.parse("9.9.9.9"), rp)
            .isPresent());
  }

  @Test
  public void testTransformAndFilterInternalRouteFromNeighborAllowed() {
    RoutingPolicy rp =
        RoutingPolicy.builder()
            .setName("rp")
            .setOwner(_c)
            .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
            .build();
    EigrpInternalRoute routeIn = _internalRouteBuilder.build();
    Ip ip = Ip.parse("9.9.9.9");
    Optional<EigrpInternalRoute> maybeRoute =
        _routingProcess.transformAndFilterInternalRouteFromNeighbor(routeIn, _ifaceMetric, ip, rp);
    assertTrue(maybeRoute.isPresent());
    EigrpInternalRoute route = maybeRoute.get();
    assertThat(route.getNextHopIp(), equalTo(ip));
    assertThat(route.getAdministrativeCost(), equalTo((long) _process.getInternalAdminCost()));
    assertThat(route.getEigrpMetric(), equalTo(routeIn.getEigrpMetric().add(_ifaceMetric)));
    assertThat(route.getProcessAsn(), equalTo(_process.getAsn()));
  }
}
