package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.representation.juniper.PsFromRouteType.Type;
import org.junit.Test;

public class PsFromRouteTypeTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new PsFromRouteType(Type.EXTERNAL), new PsFromRouteType(Type.EXTERNAL))
        .addEqualityGroup(new PsFromRouteType(Type.INTERNAL))
        .testEquals();
  }

  @Test
  public void testConversion() {
    JuniperConfiguration jc = new JuniperConfiguration();
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("c")
            .build();
    Warnings w = new Warnings();

    assertThat(
        new PsFromRouteType(Type.EXTERNAL).toBooleanExpr(jc, c, w),
        equalTo(new MatchProtocol(RoutingProtocol.BGP)));
    assertThat(
        new PsFromRouteType(Type.INTERNAL).toBooleanExpr(jc, c, w),
        equalTo(new MatchProtocol(RoutingProtocol.IBGP)));
  }
}
