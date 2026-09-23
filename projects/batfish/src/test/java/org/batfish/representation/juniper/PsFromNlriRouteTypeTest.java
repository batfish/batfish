package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.junit.Test;

public final class PsFromNlriRouteTypeTest {

  @Test
  public void testConversion() {
    assertThat(
        new PsFromNlriRouteType(5)
            .toBooleanExpr(
                new JuniperConfiguration(),
                Configuration.builder()
                    .setConfigurationFormat(ConfigurationFormat.JUNIPER)
                    .setHostname("host")
                    .build(),
                new Warnings()),
        equalTo(BooleanExprs.FALSE));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new PsFromNlriRouteType(3), new PsFromNlriRouteType(3))
        .addEqualityGroup(new PsFromNlriRouteType(5))
        .testEquals();
  }

  @Test
  public void testGetRouteType() {
    assertThat(new PsFromNlriRouteType(5).getRouteType(), equalTo(5));
  }
}
