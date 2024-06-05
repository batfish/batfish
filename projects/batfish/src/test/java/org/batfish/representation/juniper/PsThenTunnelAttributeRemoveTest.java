package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public class PsThenTunnelAttributeRemoveTest {
  @Test
  public void testEquals() {
    PsThenTunnelAttributeRemove then = PsThenTunnelAttributeRemove.INSTANCE;
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(then, PsThenTunnelAttributeRemove.INSTANCE)
        .testEquals();
  }

  List<Statement> getStatements(PsThenTunnelAttributeRemove p) {
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
        getStatements(PsThenTunnelAttributeRemove.INSTANCE),
        contains(RemoveTunnelEncapsulationAttribute.instance()));
  }
}
