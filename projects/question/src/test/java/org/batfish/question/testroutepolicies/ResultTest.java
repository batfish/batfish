package org.batfish.question.testroutepolicies;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.trace.Tracer;
import org.junit.Test;

public class ResultTest extends TestCase {

  @Test
  public void testEquals() {
    Bgpv4Route route =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHop.legacyConverter("iface", null))
            .setNetwork(Prefix.ZERO)
            .build();
    Bgpv4Route routeOther =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.parse("2.2.2.2"))
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHop.legacyConverter("iface", null))
            .setNetwork(Prefix.ZERO)
            .build();
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setTraceElement(TraceElement.of("other"));
    tracer.endSubTrace();
    new EqualsTester()
        .addEqualityGroup(
            new Result(
                new RoutingPolicyId("host", "policy"), route, PERMIT, route, ImmutableList.of()),
            new Result(
                new RoutingPolicyId("host", "policy"), route, PERMIT, route, ImmutableList.of()))
        .addEqualityGroup(
            new Result(
                new RoutingPolicyId("other", "other"), route, PERMIT, route, ImmutableList.of()))
        .addEqualityGroup(
            new Result(
                new RoutingPolicyId("host", "policy"),
                routeOther,
                PERMIT,
                route,
                ImmutableList.of()))
        .addEqualityGroup(
            new Result(
                new RoutingPolicyId("host", "policy"), route, DENY, null, ImmutableList.of()))
        .addEqualityGroup(
            new Result(
                new RoutingPolicyId("host", "policy"),
                route,
                PERMIT,
                routeOther,
                ImmutableList.of()))
        .addEqualityGroup(
            new Result(
                new RoutingPolicyId("host", "policy"), route, PERMIT, route, tracer.getTrace()))
        .testEquals();
  }
}
