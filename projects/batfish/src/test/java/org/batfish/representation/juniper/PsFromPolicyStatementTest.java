package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.junit.Test;

public class PsFromPolicyStatementTest {
  @Test
  public void testConversion() {
    JuniperConfiguration jc = new JuniperConfiguration();
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("c")
            .build();
    c.setRoutingPolicies(
        java.util.Map.of(
            "existingPolicy",
            RoutingPolicy.builder().setName("existingPolicy").setOwner(c).build()));
    Warnings w = new Warnings();

    assertThat(
        new PsFromPolicyStatement("existingPolicy").toBooleanExpr(jc, c, w),
        equalTo(new CallExpr("existingPolicy")));

    assertThat(
        new PsFromPolicyStatement("nonexistent").toBooleanExpr(jc, c, w),
        equalTo(BooleanExprs.FALSE));
  }
}
