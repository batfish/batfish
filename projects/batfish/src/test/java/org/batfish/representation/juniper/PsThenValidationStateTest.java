package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public final class PsThenValidationStateTest {

  @Test
  public void testConversion() {
    List<Statement> statements = new ArrayList<>();
    new PsThenValidationState(PsThenValidationState.State.INVALID)
        .applyTo(
            statements,
            new JuniperConfiguration(),
            Configuration.builder()
                .setConfigurationFormat(ConfigurationFormat.JUNIPER)
                .setHostname("host")
                .build(),
            new Warnings());
    assertThat(statements, empty());
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsThenValidationState(PsThenValidationState.State.INVALID),
            new PsThenValidationState(PsThenValidationState.State.INVALID))
        .addEqualityGroup(new PsThenValidationState(PsThenValidationState.State.UNKNOWN))
        .addEqualityGroup(new PsThenValidationState(PsThenValidationState.State.VALID))
        .testEquals();
  }

  @Test
  public void testGetState() {
    assertThat(
        new PsThenValidationState(PsThenValidationState.State.UNKNOWN).getState(),
        equalTo(PsThenValidationState.State.UNKNOWN));
  }

  @Test
  public void testLastWins() {
    PsThens thens = new PsThens();
    thens.addPsThen(new PsThenValidationState(PsThenValidationState.State.INVALID));
    assertThat(
        thens.addPsThen(new PsThenValidationState(PsThenValidationState.State.VALID)),
        contains("validation-state"));
    assertThat(
        thens.getAllThens(),
        contains(new PsThenValidationState(PsThenValidationState.State.VALID)));
  }
}
