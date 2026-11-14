package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchColor2}. */
public final class MatchColor2Test {
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          // Not Juniper: Juniper policies read the output route, and these tests set the original.
          .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
          .build();
  private static final MatchColor2 MATCH_7 = new MatchColor2(7L);

  private static Bgpv4Route bgp(long value) {
    return Bgpv4Route.testBuilder().setNetwork(Prefix.parse("10.0.0.0/8")).setColor2(value).build();
  }

  private static boolean matches(Environment.Builder env) {
    return MATCH_7.evaluate(env.build()).getBooleanValue();
  }

  @Test
  public void testBgpRoute() {
    assertThat(matches(Environment.builder(C).setOriginalRoute(bgp(7L))), equalTo(true));
    assertThat(matches(Environment.builder(C).setOriginalRoute(bgp(8L))), equalTo(false));
  }

  @Test
  public void testBgpOutputAttributes() {
    Bgpv4Route route = bgp(8L);
    assertThat(
        matches(
            Environment.builder(C)
                .setOriginalRoute(route)
                .setOutputRoute(route.toBuilder().setColor2(7L))
                .setUseOutputAttributes(true)),
        equalTo(true));
  }

  @Test
  public void testStaticRouteHasZero() {
    StaticRoute route = StaticRoute.testBuilder().setNetwork(Prefix.parse("10.0.0.0/8")).build();
    assertThat(
        new MatchColor2(0L)
            .evaluate(Environment.builder(C).setOriginalRoute(route).build())
            .getBooleanValue(),
        equalTo(true));
  }

  @Test
  public void testOtherRouteHasZero() {
    ConnectedRoute route = new ConnectedRoute(Prefix.parse("10.0.0.0/8"), "eth0");
    assertThat(
        new MatchColor2(0L)
            .evaluate(Environment.builder(C).setOriginalRoute(route).build())
            .getBooleanValue(),
        equalTo(true));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new MatchColor2(7L), new MatchColor2(7L))
        .addEqualityGroup(new MatchColor2(8L))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    MatchColor2 m = new MatchColor2(4294967295L);
    assertThat(BatfishObjectMapper.clone(m, BooleanExpr.class), equalTo(m));
  }
}
