package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TunnelAttribute;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link SetTunnelAttribute} */
public class SetTunnelAttributeTest {
  @Test
  public void testEquals() {
    SetTunnelAttribute sta = new SetTunnelAttribute("foo");
    new EqualsTester()
        .addEqualityGroup(sta, sta, new SetTunnelAttribute("foo"))
        .addEqualityGroup(new SetTunnelAttribute("bar"))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    SetTunnelAttribute sta = new SetTunnelAttribute("foo");
    assertThat(SerializationUtils.clone(sta), equalTo(sta));
  }

  @Test
  public void testJsonSerialization() {
    SetTunnelAttribute sta = new SetTunnelAttribute("foo");
    assertThat(BatfishObjectMapper.clone(sta, SetTunnelAttribute.class), equalTo(sta));
  }

  @Test
  public void testExecuteNonBgpRoute() {
    SetTunnelAttribute sta = new SetTunnelAttribute("foo");
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_NX)
            .setHostname("c")
            .build();
    Bgpv4Route route = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).build();
    {
      // Don't crash when the tunnel attribute is undefined
      Bgpv4Route.Builder outputRoute = route.toBuilder();
      sta.execute(
          Environment.builder(c).setOriginalRoute(route).setOutputRoute(outputRoute).build());
      assertNull(outputRoute.build().getTunnelAttribute());
    }

    TunnelAttribute foo = new TunnelAttribute(Ip.parse("1.1.1.1"));
    c.setTunnelAttributes(ImmutableMap.of("foo", foo));
    {
      // Don't crash when applied to a non-BGP route
      StaticRoute nonBgpRoute = StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build();
      sta.execute(
          Environment.builder(c)
              .setOriginalRoute(nonBgpRoute)
              .setOutputRoute(nonBgpRoute.toBuilder())
              .build());
    }
    {
      // Update tunnel attribute correctly
      Bgpv4Route.Builder outputRoute = route.toBuilder();
      sta.execute(
          Environment.builder(c).setOriginalRoute(route).setOutputRoute(outputRoute).build());
      assertThat(outputRoute.build().getTunnelAttribute(), equalTo(foo));
    }
  }
}
