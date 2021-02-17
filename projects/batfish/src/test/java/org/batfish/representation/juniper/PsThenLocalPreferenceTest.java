package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.representation.juniper.PsThenLocalPreference.Operator;
import org.junit.Test;

public class PsThenLocalPreferenceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(
            new PsThenLocalPreference(1, Operator.SET), new PsThenLocalPreference(1, Operator.SET))
        .addEqualityGroup(new PsThenLocalPreference(2, Operator.SET))
        .addEqualityGroup(new PsThenLocalPreference(1, Operator.ADD))
        .addEqualityGroup(new PsThenLocalPreference(1, Operator.SUBTRACT))
        .testEquals();
  }

  List<Statement> getStatements(PsThenLocalPreference p) {
    List<Statement> statements = new LinkedList<>();
    JuniperConfiguration fakeVsConfig = new JuniperConfiguration();
    fakeVsConfig.setHostname("c");
    Configuration fakeConfig =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    p.applyTo(statements, fakeVsConfig, fakeConfig, new Warnings());
    return statements;
  }

  @Test
  public void testConversion() {
    assertThat(
        getStatements(new PsThenLocalPreference(1, Operator.SET)),
        contains(new SetLocalPreference(new LiteralLong(1))));
    assertThat(
        getStatements(new PsThenLocalPreference(2, Operator.SUBTRACT)),
        contains(new SetLocalPreference(new DecrementLocalPreference(2))));
    assertThat(
        getStatements(new PsThenLocalPreference(3, Operator.ADD)),
        contains(new SetLocalPreference(new IncrementLocalPreference(3))));
  }
}
