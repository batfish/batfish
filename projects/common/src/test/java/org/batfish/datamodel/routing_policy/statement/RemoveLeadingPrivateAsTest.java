package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nullable;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

/** Tests of {@link Statements#RemoveLeadingPrivateAs}. */
public class RemoveLeadingPrivateAsTest {
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          .setConfigurationFormat(ConfigurationFormat.JUNIPER)
          .build();

  /** The AS path {@code path} has after the statement, on a session to {@code remoteAs}. */
  private static AsPath execute(AsPath path, @Nullable Long remoteAs) {
    Bgpv4Route route =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setAsPath(path)
            .build();
    Bgpv4Route.Builder output = route.toBuilder();
    Environment.Builder env =
        Environment.builder(C)
            .setDirection(Direction.OUT)
            .setOriginalRoute(route)
            .setOutputRoute(output);
    if (remoteAs != null) {
      env.setBgpSessionProperties(
          BgpSessionProperties.builder()
              .setLocalAs(65000L)
              .setRemoteAs(remoteAs)
              .setLocalIp(Ip.parse("192.0.2.2"))
              .setRemoteIp(Ip.parse("192.0.2.3"))
              .build());
    }
    Statements.RemoveLeadingPrivateAs.toStaticStatement().execute(env.build());
    return output.getAsPath();
  }

  @Test
  public void testRemovesLeadingPrivateAs() {
    assertThat(
        execute(AsPath.ofSingletonAsSets(65001L, 1L, 65002L), 2L),
        equalTo(AsPath.ofSingletonAsSets(1L, 65002L)));
  }

  @Test
  public void testWithoutSession() {
    assertThat(
        execute(AsPath.ofSingletonAsSets(65001L, 1L), null), equalTo(AsPath.ofSingletonAsSets(1L)));
  }

  @Test
  public void testStopsAtPrivateRemoteAs() {
    assertThat(
        execute(AsPath.ofSingletonAsSets(65001L, 65100L, 65002L), 65100L),
        equalTo(AsPath.ofSingletonAsSets(65100L, 65002L)));
  }

  @Test
  public void testPublicRemoteAsLaterInPath() {
    assertThat(
        execute(AsPath.ofSingletonAsSets(65001L, 1L, 2L), 2L),
        equalTo(AsPath.ofSingletonAsSets(1L, 2L)));
  }

  @Test
  public void testRemoteAsFirst() {
    assertThat(
        execute(AsPath.ofSingletonAsSets(65100L, 65001L), 65100L),
        equalTo(AsPath.ofSingletonAsSets(65100L, 65001L)));
  }
}
