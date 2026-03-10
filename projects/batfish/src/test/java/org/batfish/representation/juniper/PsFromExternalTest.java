package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.junit.Test;

public class PsFromExternalTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsFromExternal(OspfMetricType.E1), new PsFromExternal(OspfMetricType.E1))
        .addEqualityGroup(new PsFromExternal(OspfMetricType.E2))
        .addEqualityGroup(new PsFromExternal(null))
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

    // Test specific types
    assertThat(
        new PsFromExternal(OspfMetricType.E1).toBooleanExpr(jc, c, w),
        equalTo(new MatchProtocol(RoutingProtocol.OSPF_E1)));
    assertThat(
        new PsFromExternal(OspfMetricType.E2).toBooleanExpr(jc, c, w),
        equalTo(new MatchProtocol(RoutingProtocol.OSPF_E2)));

    // Test null type (matches all external routes)
    assertThat(
        new PsFromExternal(null).toBooleanExpr(jc, c, w),
        equalTo(new MatchProtocol(RoutingProtocol.OSPF_E1, RoutingProtocol.OSPF_E2)));
  }

  @Test
  public void testSerialization() {
    // Test serialization for E1 type
    PsFromExternal e1 = new PsFromExternal(OspfMetricType.E1);
    assertThat(SerializationUtils.clone(e1), equalTo(e1));

    // Test serialization for E2 type
    PsFromExternal e2 = new PsFromExternal(OspfMetricType.E2);
    assertThat(SerializationUtils.clone(e2), equalTo(e2));

    // Test serialization for null type
    PsFromExternal nullType = new PsFromExternal(null);
    assertThat(SerializationUtils.clone(nullType), equalTo(nullType));
  }
}
