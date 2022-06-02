package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TunnelAttribute;
import org.batfish.datamodel.routing_policy.statement.SetTunnelAttribute;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public class PsThenTunnelAttributeSetTest {
  @Test
  public void testEquals() {
    PsThenTunnelAttributeSet then = new PsThenTunnelAttributeSet("foo");
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(then, then, new PsThenTunnelAttributeSet("foo"))
        .addEqualityGroup(new PsThenTunnelAttributeSet("bar"))
        .testEquals();
  }

  List<Statement> getStatements(PsThenTunnelAttributeSet p, boolean defineTunnelAttribute) {
    List<Statement> statements = new LinkedList<>();
    JuniperConfiguration fakeVsConfig = new JuniperConfiguration();
    fakeVsConfig.setHostname("c");
    Configuration fakeConfig =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    if (defineTunnelAttribute) {
      fakeConfig
          .getTunnelAttributes()
          .put(p.getTunnelAttributeName(), new TunnelAttribute(Ip.parse("1.1.1.1")));
    }
    p.applyTo(statements, fakeVsConfig, fakeConfig, new Warnings());
    return statements;
  }

  @Test
  public void testConversion() {
    assertThat(
        getStatements(new PsThenTunnelAttributeSet("foo"), true),
        contains(new SetTunnelAttribute("foo")));
    assertThat(getStatements(new PsThenTunnelAttributeSet("foo"), false), empty());
  }
}
