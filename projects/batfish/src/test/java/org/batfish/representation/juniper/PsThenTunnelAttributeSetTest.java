package org.batfish.representation.juniper;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.testing.EqualsTester;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
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

  @Test
  public void testConversion() {
    JuniperConfiguration vsConfig = new JuniperConfiguration();
    vsConfig.setHostname("c");
    Configuration viConfig =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    {
      // Generates correct statement for a valid tunnel attribute
      String tunnelAttrName = "valid";
      Ip remoteEndPoint = Ip.parse("1.1.1.1");
      TunnelAttribute ta = createTunnelAttribute(TunnelAttribute.Type.IPIP, remoteEndPoint);
      vsConfig.getMasterLogicalSystem().getTunnelAttributes().put(tunnelAttrName, ta);

      List<Statement> statements = new LinkedList<>();
      PsThenTunnelAttributeSet psThenSet = new PsThenTunnelAttributeSet(tunnelAttrName);
      Warnings warnings = new Warnings(true, true, true);
      psThenSet.applyTo(statements, vsConfig, viConfig, warnings);
      assertThat(
          statements,
          contains(
              new SetTunnelEncapsulationAttribute(
                  new LiteralTunnelEncapsulationAttribute(
                      new TunnelEncapsulationAttribute(remoteEndPoint)))));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
    {
      // Does not generate a statement and files a warning for a tunnel attribute with no type set
      String tunnelAttrName = "no-type";
      TunnelAttribute ta = createTunnelAttribute(null, Ip.parse("1.1.1.1"));
      vsConfig.getMasterLogicalSystem().getTunnelAttributes().put(tunnelAttrName, ta);

      List<Statement> statements = new LinkedList<>();
      PsThenTunnelAttributeSet psThenSet = new PsThenTunnelAttributeSet(tunnelAttrName);
      Warnings warnings = new Warnings(true, true, true);
      psThenSet.applyTo(statements, vsConfig, viConfig, warnings);
      assertThat(statements, empty());
      assertThat(
          warnings.getRedFlagWarnings(),
          hasItem(hasText("Ignoring tunnel-attribute no-type because its tunnel-type is not set")));
    }
    {
      // Does not generate a statement and files a warning for a tunnel attribute with no remote
      // endpoint set
      String tunnelAttrName = "no-remote-endpoint";
      TunnelAttribute ta = createTunnelAttribute(TunnelAttribute.Type.IPIP, null);
      vsConfig.getMasterLogicalSystem().getTunnelAttributes().put(tunnelAttrName, ta);

      List<Statement> statements = new LinkedList<>();
      PsThenTunnelAttributeSet psThenSet = new PsThenTunnelAttributeSet(tunnelAttrName);
      Warnings warnings = new Warnings(true, true, true);
      psThenSet.applyTo(statements, vsConfig, viConfig, warnings);
      assertThat(statements, empty());
      assertThat(
          warnings.getRedFlagWarnings(),
          hasItem(
              hasText(
                  "Ignoring tunnel-attribute no-remote-endpoint because its remote-end-point is not"
                      + " set")));
    }
    {
      // Does not generate a statement or warnings if the tunnel attribute is undefined (the issue
      // will show up as an undefined reference)
      String tunnelAttrName = "undefined";
      List<Statement> statements = new LinkedList<>();
      PsThenTunnelAttributeSet psThenSet = new PsThenTunnelAttributeSet(tunnelAttrName);
      Warnings warnings = new Warnings(true, true, true);
      psThenSet.applyTo(statements, vsConfig, viConfig, warnings);
      assertThat(statements, empty());
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
  }

  private static TunnelAttribute createTunnelAttribute(
      TunnelAttribute.Type type, Ip remoteEndPoint) {
    TunnelAttribute ta = new TunnelAttribute();
    ta.setType(type);
    ta.setRemoteEndPoint(remoteEndPoint);
    return ta;
  }
}
