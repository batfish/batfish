package org.batfish.representation.juniper;

import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.DecrementAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.IncrementAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.representation.juniper.PsThenPreference.Operator;
import org.junit.Test;

public class PsThenPreferenceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(
            new PsThenPreference(1, Operator.SET), new PsThenPreference(1, Operator.SET))
        .addEqualityGroup(new PsThenPreference(2, Operator.SET))
        .addEqualityGroup(new PsThenPreference(1, Operator.ADD))
        .addEqualityGroup(new PsThenPreference(1, Operator.SUBTRACT))
        .testEquals();
  }

  List<Statement> getStatements(PsThenPreference p) {
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
        getStatements(new PsThenPreference(1, Operator.SET)),
        contains(new SetAdministrativeCost(new LiteralAdministrativeCost(1))));
    assertThat(
        getStatements(new PsThenPreference(2, Operator.SUBTRACT)),
        contains(new SetAdministrativeCost(new DecrementAdministrativeCost(2, 0))));
    assertThat(
        getStatements(new PsThenPreference(3, Operator.ADD)),
        contains(
            new SetAdministrativeCost(new IncrementAdministrativeCost(3, MAX_ADMIN_DISTANCE))));
  }
}
