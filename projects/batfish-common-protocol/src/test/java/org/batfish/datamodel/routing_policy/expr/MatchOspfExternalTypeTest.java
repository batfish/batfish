package org.batfish.datamodel.routing_policy.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

public class MatchOspfExternalTypeTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new MatchOspfExternalType(OspfMetricType.E1),
            new MatchOspfExternalType(OspfMetricType.E1))
        .addEqualityGroup(new MatchOspfExternalType(OspfMetricType.E2))
        .addEqualityGroup(new MatchOspfExternalType(null), new MatchOspfExternalType(null))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    MatchOspfExternalType matchE1 = new MatchOspfExternalType(OspfMetricType.E1);
    MatchOspfExternalType matchE2 = new MatchOspfExternalType(OspfMetricType.E2);
    MatchOspfExternalType matchAny = new MatchOspfExternalType(null);

    // Use a valid IP address for NextHopIp
    Ip validIp = Ip.parse("192.0.2.1"); // Using a TEST-NET-1 address (RFC 5737)

    // Create a non-OSPF route (static route) for negative testing
    StaticRoute nonOspfRoute =
        StaticRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopIp.of(validIp))
            .setAdmin(100)
            .setMetric(10)
            .build();

    // Create OSPF E1 route
    OspfExternalRoute ospfE1Route =
        OspfExternalRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopIp.of(validIp))
            .setAdmin(100)
            .setMetric(10)
            .setLsaMetric(10)
            .setArea(0)
            .setCostToAdvertiser(10)
            .setAdvertiser("test")
            .setOspfMetricType(OspfMetricType.E1)
            .setNonRouting(true) // Set nonRouting flag to true to allow next-hop IP only
            .build();

    // Create OSPF E2 route
    OspfExternalRoute ospfE2Route =
        OspfExternalRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopIp.of(validIp))
            .setAdmin(100)
            .setMetric(10)
            .setLsaMetric(10)
            .setArea(0)
            .setCostToAdvertiser(10)
            .setAdvertiser("test")
            .setOspfMetricType(OspfMetricType.E2)
            .setNonRouting(true) // Set nonRouting flag to true to allow next-hop IP only
            .build();

    // Create a configuration for the environment
    Configuration config =
        Configuration.builder()
            .setHostname("test-host")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();

    // Create environments with different routes
    Environment envNonOspf =
        Environment.builder(config)
            .setOriginalRoute(nonOspfRoute)
            .setDirection(Direction.IN)
            .build();
    Environment envOspfE1 =
        Environment.builder(config)
            .setOriginalRoute(ospfE1Route)
            .setDirection(Direction.IN)
            .build();
    Environment envOspfE2 =
        Environment.builder(config)
            .setOriginalRoute(ospfE2Route)
            .setDirection(Direction.IN)
            .build();
    Environment envNull = Environment.builder(config).setDirection(Direction.IN).build();

    // Test matching E1 routes
    assertFalse(matchE1.evaluate(envNonOspf).getBooleanValue());
    assertTrue(matchE1.evaluate(envOspfE1).getBooleanValue());
    assertFalse(matchE1.evaluate(envOspfE2).getBooleanValue());
    assertFalse(matchE1.evaluate(envNull).getBooleanValue());

    // Test matching E2 routes
    assertFalse(matchE2.evaluate(envNonOspf).getBooleanValue());
    assertFalse(matchE2.evaluate(envOspfE1).getBooleanValue());
    assertTrue(matchE2.evaluate(envOspfE2).getBooleanValue());
    assertFalse(matchE2.evaluate(envNull).getBooleanValue());

    // Test matching any external routes (null type)
    assertFalse(matchAny.evaluate(envNonOspf).getBooleanValue());
    assertTrue(matchAny.evaluate(envOspfE1).getBooleanValue());
    assertTrue(matchAny.evaluate(envOspfE2).getBooleanValue());
    assertFalse(matchAny.evaluate(envNull).getBooleanValue());
  }
}
