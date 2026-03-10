package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.junit.Test;

/** Tests of {@link SetTunnelEncapsulationAttribute} */
public class SetTunnelEncapsulationAttributeTest {
  @Test
  public void testEquals() {
    LiteralTunnelEncapsulationAttribute tunnelEncapAttrExpr =
        new LiteralTunnelEncapsulationAttribute(
            new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")));
    SetTunnelEncapsulationAttribute sta = new SetTunnelEncapsulationAttribute(tunnelEncapAttrExpr);
    new EqualsTester()
        .addEqualityGroup(sta, sta, new SetTunnelEncapsulationAttribute(tunnelEncapAttrExpr))
        .addEqualityGroup(
            new SetTunnelEncapsulationAttribute(
                new LiteralTunnelEncapsulationAttribute(
                    new TunnelEncapsulationAttribute(Ip.parse("2.2.2.2")))))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    LiteralTunnelEncapsulationAttribute tunnelEncapAttrExpr =
        new LiteralTunnelEncapsulationAttribute(
            new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")));
    SetTunnelEncapsulationAttribute sta = new SetTunnelEncapsulationAttribute(tunnelEncapAttrExpr);
    assertThat(SerializationUtils.clone(sta), equalTo(sta));
  }

  @Test
  public void testJsonSerialization() {
    LiteralTunnelEncapsulationAttribute tunnelEncapAttrExpr =
        new LiteralTunnelEncapsulationAttribute(
            new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")));
    SetTunnelEncapsulationAttribute sta = new SetTunnelEncapsulationAttribute(tunnelEncapAttrExpr);
    assertThat(BatfishObjectMapper.clone(sta, SetTunnelEncapsulationAttribute.class), equalTo(sta));
  }

  @Test
  public void testExecute() {
    TunnelEncapsulationAttribute tunnelEncapAttr =
        new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1"));
    LiteralTunnelEncapsulationAttribute tunnelEncapAttrExpr =
        new LiteralTunnelEncapsulationAttribute(tunnelEncapAttr);
    SetTunnelEncapsulationAttribute sta = new SetTunnelEncapsulationAttribute(tunnelEncapAttrExpr);
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.FLAT_JUNIPER)
            .setHostname("c")
            .build();
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
      Bgpv4Route route = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).build();
      Bgpv4Route.Builder outputRoute = route.toBuilder();
      sta.execute(
          Environment.builder(c).setOriginalRoute(route).setOutputRoute(outputRoute).build());
      assertThat(outputRoute.build().getTunnelEncapsulationAttribute(), equalTo(tunnelEncapAttr));
    }
  }
}
