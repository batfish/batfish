package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.NatPool;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceAddressValidationTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInvalidAddresses() {
    JuniperConfiguration jc = parseJuniperConfig(_folder, "interface-address-validation-invalid");
    assertThat(
        jc.getWarnings().getFatalRedFlagWarnings(),
        containsInAnyOrder(
            hasText(
                "FATAL: Cannot assign network address as ip address: 192.0.2.0/24 on interface"
                    + " ge-0/0/0.0"),
            hasText(
                "FATAL: Cannot assign network address as ip address: 192.0.2.4/30 on interface"
                    + " ge-0/0/0.0"),
            hasText(
                "FATAL: Cannot assign broadcast address as ip address: 192.0.2.3/30 on interface"
                    + " ge-0/0/0.0"),
            hasText(
                "FATAL: Cannot assign broadcast address as ip address: 198.51.100.255/24 on"
                    + " interface ge-0/0/0.0")));
    Interface iface =
        jc.getMasterLogicalSystem().getInterfaces().get("ge-0/0/0").getUnits().get("ge-0/0/0.0");
    assertThat(
        iface.getAllAddresses(),
        containsInAnyOrder(
            ConcreteInterfaceAddress.parse("192.0.2.1/30"),
            ConcreteInterfaceAddress.parse("192.0.2.0/24"),
            ConcreteInterfaceAddress.parse("192.0.2.4/30"),
            ConcreteInterfaceAddress.parse("192.0.2.3/30"),
            ConcreteInterfaceAddress.parse("198.51.100.255/24"),
            ConcreteInterfaceAddress.parse("192.0.2.2/30")));
    assertThat(iface.getPrimaryAddress(), equalTo(ConcreteInterfaceAddress.parse("192.0.2.0/24")));
    assertThat(
        iface.getPreferredAddress(), equalTo(ConcreteInterfaceAddress.parse("192.0.2.3/30")));
    assertThat(
        iface.getVrrpGroups().get(1).getSourceAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.0.2.3/30")));
  }

  @Test
  public void testValidAddresses() {
    JuniperConfiguration jc = parseJuniperConfig(_folder, "interface-address-validation-valid");
    assertThat(jc.getWarnings().getFatalRedFlagWarnings(), empty());
    Interface iface =
        jc.getMasterLogicalSystem().getInterfaces().get("ge-0/0/0").getUnits().get("ge-0/0/0.0");
    assertThat(
        iface.getAllAddresses(),
        containsInAnyOrder(
            ConcreteInterfaceAddress.parse("192.0.2.1/24"),
            ConcreteInterfaceAddress.parse("192.0.2.0/31"),
            ConcreteInterfaceAddress.parse("192.0.2.1/31"),
            ConcreteInterfaceAddress.parse("192.0.2.0/32"),
            ConcreteInterfaceAddress.parse("192.0.2.255/32"),
            ConcreteInterfaceAddress.parse("198.51.100.0/32"),
            ConcreteInterfaceAddress.parse("198.51.100.255/32"),
            ConcreteInterfaceAddress.parse("192.0.2.0/22"),
            ConcreteInterfaceAddress.parse("192.0.2.255/22")));
    NatPool pool = jc.getMasterLogicalSystem().getNatSource().getPools().get("POOL");
    assertThat(pool.getFromAddress(), equalTo(Ip.parse("192.0.2.0")));
    assertThat(pool.getToAddress(), equalTo(Ip.parse("192.0.2.255")));
  }
}
