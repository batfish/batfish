package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.representation.juniper.PsThenMetric2.Operator;
import org.junit.Test;

public class PsThenMetric2Test {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(new PsThenMetric2(1, Operator.SET), new PsThenMetric2(1, Operator.SET))
        .addEqualityGroup(new PsThenMetric2(2, Operator.SET))
        .addEqualityGroup(new PsThenMetric2(1, Operator.ADD))
        .addEqualityGroup(new PsThenMetric2(1, Operator.SUBTRACT))
        .testEquals();
  }

  List<Statement> getStatements(PsThenMetric2 p) {
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
    // No VI model support for metric2 yet
    // applyTo() is a no-op with TODO comment
    assertThat(getStatements(new PsThenMetric2(1, Operator.SET)), empty());
    assertThat(getStatements(new PsThenMetric2(2, Operator.SUBTRACT)), empty());
    assertThat(getStatements(new PsThenMetric2(3, Operator.ADD)), empty());
  }
}
