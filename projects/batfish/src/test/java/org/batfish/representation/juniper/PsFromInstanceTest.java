package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.junit.Test;

public class PsFromInstanceTest {
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
        new PsFromInstance("master").toBooleanExpr(jc, c, w),
        equalTo(new MatchSourceVrf("default")));

    assertThat(
        new PsFromInstance("MY_VRF").toBooleanExpr(jc, c, w),
        equalTo(new MatchSourceVrf("MY_VRF")));
  }
}
