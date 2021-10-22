package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpSessionProperties;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link AutoAs} */
public class AutoAsTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final AutoAs INSTANCE = AutoAs.instance();
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
  public void testEvaluateDirectionIn() {
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

  @Test
  public void testEvaluateDirectionIn_originalRouteNotBgp() {
    StaticRoute sr =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(1).build();
    Environment env =
        Environment.builder(C).setDirection(Direction.IN).setOriginalRoute(sr).build();
    _thrown.expect(AssertionError.class);
    INSTANCE.evaluate(env);
  }

  @Test
  public void testEvaluateDirectionIn_emptyAsPath() {
    // In the absence of an AS path, evaluates to remote AS. TODO Untested.
    BgpSessionProperties sessionProps =
        BgpSessionProperties.builder()
            .setRemoteAs(11111L)
            .setLocalAs(22222L)
            .setRemoteIp(Ip.parse("1.1.1.1"))
            .setLocalIp(Ip.parse("2.2.2.2"))
            .build();
    Environment env =
        Environment.builder(C)
            .setDirection(Direction.IN)
            .setBgpSessionProperties(sessionProps)
            .setOriginalRoute(BGP_ROUTE)
            .build();
    assertThat(INSTANCE.evaluate(env), equalTo(11111L));
  }

  @Test
  public void testEvaluateDirectionIn_nonSingletonAsSet() {
    Bgpv4Route originalRoute =
        BGP_ROUTE.toBuilder().setAsPath(AsPath.of(AsSet.of(11111L, 22222L))).build();
    Environment env =
        Environment.builder(C).setDirection(Direction.IN).setOriginalRoute(originalRoute).build();
    _thrown.expect(AssertionError.class);
    INSTANCE.evaluate(env);
  }

  @Test
  public void testEvaluateDirectionOut() {
    BgpSessionProperties sessionProps =
        BgpSessionProperties.builder()
            .setRemoteAs(11111L)
            .setLocalAs(22222L)
            .setRemoteIp(Ip.parse("1.1.1.1"))
            .setLocalIp(Ip.parse("2.2.2.2"))
            .build();
    Environment env =
        Environment.builder(C)
            .setDirection(Direction.OUT)
            .setBgpSessionProperties(sessionProps)
            .build();
    assertThat(INSTANCE.evaluate(env), equalTo(22222L));
  }

  @Test
  public void testEvaluateDirectionOut_noBgpSessionProps() {
    Environment env = Environment.builder(C).build();
    _thrown.expectMessage("Expected BGP session properties");
    INSTANCE.evaluate(env);
  }
}
