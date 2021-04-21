package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.junit.Test;

public class RoutePolicyBooleanValidationStateIsTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new RoutePolicyBooleanValidationStateIs(true),
            new RoutePolicyBooleanValidationStateIs(true))
        .addEqualityGroup(new RoutePolicyBooleanValidationStateIs(false))
        .testEquals();
  }

  public BooleanExpr test(RoutePolicyBooleanValidationStateIs rp) {
    return rp.toBooleanExpr(
        new CiscoXrConfiguration(),
        Configuration.builder()
            .setHostname("h")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS_XR)
            .build(),
        new Warnings());
  }

  @Test
  public void testValidConversion() {
    RoutePolicyBooleanValidationStateIs rp = new RoutePolicyBooleanValidationStateIs(true);
    assertThat(test(rp), equalTo(BooleanExprs.TRUE));
  }

  @Test
  public void testInvalidConversion() {
    RoutePolicyBooleanValidationStateIs rp = new RoutePolicyBooleanValidationStateIs(false);
    assertThat(test(rp), equalTo(BooleanExprs.FALSE));
  }
}
