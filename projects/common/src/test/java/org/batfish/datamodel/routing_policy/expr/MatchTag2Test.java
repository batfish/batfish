package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchTag2}. */
public final class MatchTag2Test {
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
          .build();
  private static final MatchTag2 EQ_7 = new MatchTag2(IntComparator.EQ, new LiteralLong(7L));

  private static Bgpv4Route bgp(long tag2) {
    return Bgpv4Route.testBuilder()
        .setNetwork(Prefix.parse("10.0.0.0/8"))
        .setOriginatorIp(Ip.ZERO)
        .setOriginType(OriginType.IGP)
        .setProtocol(RoutingProtocol.BGP)
        .setTag2(tag2)
        .build();
  }

  private static boolean matches(Environment.Builder env) {
    return EQ_7.evaluate(env.build()).getBooleanValue();
  }

  @Test
  public void testOriginalRoute() {
    assertThat(matches(Environment.builder(C).setOriginalRoute(bgp(7L))), equalTo(true));
    assertThat(matches(Environment.builder(C).setOriginalRoute(bgp(8L))), equalTo(false));
  }

  @Test
  public void testOutputAttributes() {
    Bgpv4Route route = bgp(8L);
    assertThat(
        matches(
            Environment.builder(C)
                .setOriginalRoute(route)
                .setOutputRoute(route.toBuilder().setTag2(7L))
                .setUseOutputAttributes(true)),
        equalTo(true));
  }

  private static StaticRoute staticRoute(long tag2) {
    return StaticRoute.testBuilder().setNetwork(Prefix.parse("10.0.0.0/8")).setTag2(tag2).build();
  }

  @Test
  public void testStaticRoute() {
    assertThat(matches(Environment.builder(C).setOriginalRoute(staticRoute(7L))), equalTo(true));
    assertThat(matches(Environment.builder(C).setOriginalRoute(staticRoute(8L))), equalTo(false));
  }

  @Test
  public void testStaticRouteOutputAttributes() {
    StaticRoute route = staticRoute(8L);
    assertThat(
        matches(
            Environment.builder(C)
                .setOriginalRoute(route)
                .setOutputRoute(route.toBuilder().setTag2(7L))
                .setUseOutputAttributes(true)),
        equalTo(true));
  }

  @Test
  public void testOtherRouteHasTag2Zero() {
    ConnectedRoute route = new ConnectedRoute(Prefix.parse("10.0.0.0/8"), "eth0");
    assertThat(
        new MatchTag2(IntComparator.EQ, new LiteralLong(0L))
            .evaluate(Environment.builder(C).setOriginalRoute(route).build())
            .getBooleanValue(),
        equalTo(true));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EQ_7, new MatchTag2(IntComparator.EQ, new LiteralLong(7L)))
        .addEqualityGroup(new MatchTag2(IntComparator.GE, new LiteralLong(7L)))
        .addEqualityGroup(new MatchTag2(IntComparator.EQ, new LiteralLong(8L)))
        .addEqualityGroup(new MatchTag(IntComparator.EQ, new LiteralLong(7L)))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    assertThat(BatfishObjectMapper.clone(EQ_7, BooleanExpr.class), equalTo(EQ_7));
    assertThat(SerializationUtils.clone(EQ_7), equalTo(EQ_7));
  }
}
