package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;

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
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link RemoveTunnelEncapsulationAttribute} */
public class RemoveTunnelEncapsulationAttributeTest {
  @Test
  public void testEquals() {
    RemoveTunnelEncapsulationAttribute rta = RemoveTunnelEncapsulationAttribute.instance();
    new EqualsTester()
        .addEqualityGroup(rta, rta, RemoveTunnelEncapsulationAttribute.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    RemoveTunnelEncapsulationAttribute rta = RemoveTunnelEncapsulationAttribute.instance();
    assertThat(SerializationUtils.clone(rta), equalTo(rta));
  }

  @Test
  public void testJsonSerialization() {
    RemoveTunnelEncapsulationAttribute rta = RemoveTunnelEncapsulationAttribute.instance();
    assertThat(
        BatfishObjectMapper.clone(rta, RemoveTunnelEncapsulationAttribute.class), equalTo(rta));
  }

  @Test
  public void testExecute() {
    RemoveTunnelEncapsulationAttribute rta = RemoveTunnelEncapsulationAttribute.instance();
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_NX)
            .setHostname("c")
            .build();
    {
      // Don't crash when applied to a non-BGP route
      StaticRoute nonBgpRoute = StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build();
      rta.execute(
          Environment.builder(c)
              .setOriginalRoute(nonBgpRoute)
              .setOutputRoute(nonBgpRoute.toBuilder())
              .build());
    }
    {
      // Update tunnel attribute correctly
      TunnelEncapsulationAttribute foo = new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1"));
      Bgpv4Route route =
          Bgpv4Route.testBuilder()
              .setNetwork(Prefix.ZERO)
              .setTunnelEncapsulationAttribute(foo)
              .build();
      Bgpv4Route.Builder outputRoute = route.toBuilder();
      rta.execute(
          Environment.builder(c).setOriginalRoute(route).setOutputRoute(outputRoute).build());
      assertNull(outputRoute.build().getTunnelEncapsulationAttribute());
    }
  }
}
