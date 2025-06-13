package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.expr.MatchOspfExternalType;
import org.junit.Test;

public class PsFromExternalTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsFromExternal(OspfMetricType.E1), new PsFromExternal(OspfMetricType.E1))
        .addEqualityGroup(new PsFromExternal(OspfMetricType.E2))
        .testEquals();
  }

  @Test
  public void testToBooleanExpr() {
    JuniperConfiguration jc = new JuniperConfiguration();
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("c")
            .build();
    Warnings w = new Warnings();

    assertThat(
        new PsFromExternal(OspfMetricType.E1).toBooleanExpr(jc, c, w),
        equalTo(new MatchOspfExternalType(OspfMetricType.E1)));
    assertThat(
        new PsFromExternal(OspfMetricType.E2).toBooleanExpr(jc, c, w),
        equalTo(new MatchOspfExternalType(OspfMetricType.E2)));
  }
}
