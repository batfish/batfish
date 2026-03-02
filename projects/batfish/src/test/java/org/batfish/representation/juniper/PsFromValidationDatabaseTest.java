package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.representation.juniper.PsFromValidationDatabase.State;
import org.junit.Test;

public class PsFromValidationDatabaseTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsFromValidationDatabase(State.VALID), new PsFromValidationDatabase(State.VALID))
        .addEqualityGroup(new PsFromValidationDatabase(State.INVALID))
        .addEqualityGroup(new PsFromValidationDatabase(State.UNKNOWN))
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

    // RPKI is not modeled; all routes are treated as "valid".
    assertThat(
        new PsFromValidationDatabase(State.VALID).toBooleanExpr(jc, c, w),
        equalTo(BooleanExprs.TRUE));
    assertThat(
        new PsFromValidationDatabase(State.UNKNOWN).toBooleanExpr(jc, c, w),
        equalTo(BooleanExprs.FALSE));
    assertThat(
        new PsFromValidationDatabase(State.INVALID).toBooleanExpr(jc, c, w),
        equalTo(BooleanExprs.FALSE));
  }
}
