package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.representation.juniper.PsThenMetric.Operator;
import org.junit.Test;

public class PsThenMetricTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(new PsThenMetric(1, Operator.SET), new PsThenMetric(1, Operator.SET))
        .addEqualityGroup(new PsThenMetric(2, Operator.SET))
        .addEqualityGroup(new PsThenMetric(1, Operator.ADD))
        .addEqualityGroup(new PsThenMetric(1, Operator.SUBTRACT))
        .testEquals();
  }

  List<Statement> getStatements(PsThenMetric p) {
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
        getStatements(new PsThenMetric(1, Operator.SET)),
        contains(new SetMetric(new LiteralLong(1))));
    assertThat(
        getStatements(new PsThenMetric(2, Operator.SUBTRACT)),
        contains(new SetMetric(new DecrementMetric(2))));
    assertThat(
        getStatements(new PsThenMetric(3, Operator.ADD)),
        contains(new SetMetric(new IncrementMetric(3))));
  }
}
