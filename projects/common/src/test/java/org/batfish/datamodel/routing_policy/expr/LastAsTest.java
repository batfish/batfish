package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

/** Tests of {@link LastAs} */
public final class LastAsTest {

  private static final LastAs INSTANCE = LastAs.instance();
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
          .build();
  private static final Bgpv4Route BGP_ROUTE =
      Bgpv4Route.testBuilder()
          .setNetwork(Prefix.parse("1.1.1.1/31"))
          .setAdmin(1)
          .setOriginatorIp(Ip.parse("2.2.2.2"))
          .setOriginType(OriginType.EGP)
          .setProtocol(RoutingProtocol.BGP)
          .build();

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(LastAs.instance()), equalTo(LastAs.instance()));
  }

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(LastAs.instance(), AsExpr.class), equalTo(LastAs.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester().addEqualityGroup(LastAs.instance(), LastAs.instance()).testEquals();
  }

  @Test
  public void testEvaluate() {
    Environment.Builder envBuilder = Environment.builder(C).setDirection(Direction.IN);

    // If not useOutputAttributes or readFromIntermediateBgpAttributes, use original route AS path
    Bgpv4Route originalRoute =
        BGP_ROUTE.toBuilder().setAsPath(AsPath.ofSingletonAsSets(11111L, 22222L)).build();
    assertThat(
        INSTANCE.evaluate(envBuilder.setOriginalRoute(originalRoute).build()), equalTo(11111L));

    // If readFromIntermediateBgpAttributes but not useOutputAttributes, use intermediate attrs
    Bgpv4Route.Builder intermediateAttrs =
        BGP_ROUTE.toBuilder().setAsPath(AsPath.ofSingletonAsSets(33333L, 44444L));
    assertThat(
        INSTANCE.evaluate(
            envBuilder
                .setReadFromIntermediateBgpAttributes(true)
                .setIntermediateBgpAttributes(intermediateAttrs)
                .build()),
        equalTo(33333L));

    // If useOutputAttributes but output route is not BGP, still use intermediate attrs
    StaticRoute.Builder sr =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(1);
    assertThat(
        INSTANCE.evaluate(envBuilder.setUseOutputAttributes(true).setOutputRoute(sr).build()),
        equalTo(33333L));

    // If useOutputAttributes and output route is BGP, should use output route's AS path
    Bgpv4Route.Builder outputRoute =
        BGP_ROUTE.toBuilder().setAsPath(AsPath.ofSingletonAsSets(55555L, 66666L));
    assertThat(INSTANCE.evaluate(envBuilder.setOutputRoute(outputRoute).build()), equalTo(55555L));
  }

  @Test(expected = AssertionError.class)
  public void testEvaluate_originalRouteNotBgp() {
    StaticRoute sr =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(1).build();
    Environment env =
        Environment.builder(C).setDirection(Direction.IN).setOriginalRoute(sr).build();
    INSTANCE.evaluate(env);
  }

  @Test(expected = IllegalStateException.class)
  public void testEvaluate_emptyAsPath() {
    Environment env =
        Environment.builder(C).setDirection(Direction.IN).setOriginalRoute(BGP_ROUTE).build();
    INSTANCE.evaluate(env);
  }

  @Test
  public void testEvaluate_nonSingletonAsSet() {
    Bgpv4Route originalRoute =
        BGP_ROUTE.toBuilder().setAsPath(AsPath.of(AsSet.of(11111L, 22222L))).build();
    Environment env =
        Environment.builder(C).setDirection(Direction.IN).setOriginalRoute(originalRoute).build();

    assertThat(INSTANCE.evaluate(env), equalTo(11111L));
  }
}
