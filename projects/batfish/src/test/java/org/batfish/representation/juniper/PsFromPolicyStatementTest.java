package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.junit.Test;

public class PsFromPolicyStatementTest {
  @Test
  public void testConversion() {
    JuniperConfiguration jc = new JuniperConfiguration();
    String existingPolicy = "exists";
    jc.getMasterLogicalSystem()
        .getPolicyStatements()
        .put(existingPolicy, new PolicyStatement(existingPolicy));
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("c")
            .build();
    Warnings w = new Warnings();

    // This statement is sometimes converted before the VI statement model is complete.
    // Make sure that we check the VS model, not the VI model.
    assertThat(c.getRoutingPolicies(), not(hasKey(existingPolicy)));
    assertThat(
        new PsFromPolicyStatement(existingPolicy).toBooleanExpr(jc, c, w),
        equalTo(new CallExpr(existingPolicy)));

    assertThat(
        new PsFromPolicyStatement("nonexistent").toBooleanExpr(jc, c, w),
        equalTo(BooleanExprs.FALSE));
  }
}
