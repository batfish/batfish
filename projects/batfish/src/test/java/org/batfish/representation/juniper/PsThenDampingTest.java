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

public final class PsThenDampingTest {

  @Test
  public void testApplyTo() {
    PsThenDamping damping = new PsThenDamping("enabled");
    List<Statement> statements = new ArrayList<>();
    damping.applyTo(
        statements,
        new JuniperConfiguration(),
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("host")
            .build(),
        new Warnings());
    assertThat(damping.getProfile(), equalTo("enabled"));
    assertThat(statements, empty());
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new PsThenDamping("a"), new PsThenDamping("a"))
        .addEqualityGroup(new PsThenDamping("b"))
        .testEquals();
  }

  @Test
  public void testLastWins() {
    PsThens thens = new PsThens();
    PsThenDamping first = new PsThenDamping("first");
    PsThenDamping second = new PsThenDamping("second");

    assertThat(thens.addPsThen(first), empty());
    assertThat(thens.addPsThen(second), contains("damping"));
    assertThat(thens.getAllThens(), contains(second));
    assertThat(thens.addPsThen(second), contains("damping (dedup)"));
  }
}
