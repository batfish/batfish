package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceEnabledEffective;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceHumanName;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceMtuEffective;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.junit.Test;

/** Tests of {@link A10Configuration}. */
public class A10ConfigurationTest {
  @Test
  public void testGetInterfaceEnabledEffective() {
    Interface ethNullEnabled = new Interface(Interface.Type.ETHERNET, 1);
    Interface loopNullEnabled = new Interface(Interface.Type.LOOPBACK, 1);

    // Defaults
    // Ethernet is disabled by default
    assertFalse(getInterfaceEnabledEffective(ethNullEnabled));
    // Loopback is enabled by default
    assertTrue(getInterfaceEnabledEffective(loopNullEnabled));

    // Explicit enabled value set
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);
    eth.setEnabled(true);
    assertTrue(getInterfaceEnabledEffective(eth));
    eth.setEnabled(false);
    assertFalse(getInterfaceEnabledEffective(eth));
  }

  @Test
  public void testGetInterfaceMtuEffective() {
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);

    assertThat(getInterfaceMtuEffective(eth), equalTo(DEFAULT_MTU));
    eth.setMtu(1234);
    assertThat(getInterfaceMtuEffective(eth), equalTo(1234));
  }

  @Test
  public void testGetInterfaceHumanName() {
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.ETHERNET, 9)), equalTo("Ethernet 9"));
    assertThat(getInterfaceHumanName(new Interface(Interface.Type.TRUNK, 9)), equalTo("Trunk 9"));
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.LOOPBACK, 9)), equalTo("Loopback 9"));
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.VE, 9)), equalTo("VirtualEthernet 9"));
  }

  @Test
  public void testVrrpAAppliesToInterface() {
    org.batfish.datamodel.Interface.Builder ifaceBuilder =
        org.batfish.datamodel.Interface.builder().setName("placeholder");
    // No concrete address
    assertFalse(
        A10Configuration.vrrpAAppliesToInterface(
            ifaceBuilder.setType(InterfaceType.PHYSICAL).setAddress(null).build()));
    // Loopback interface
    assertFalse(
        A10Configuration.vrrpAAppliesToInterface(
            ifaceBuilder
                .setType(InterfaceType.LOOPBACK)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/32"))
                .build()));

    assertTrue(
        A10Configuration.vrrpAAppliesToInterface(
            ifaceBuilder
                .setType(InterfaceType.PHYSICAL)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/32"))
                .build()));
    assertTrue(
        A10Configuration.vrrpAAppliesToInterface(
            ifaceBuilder
                .setType(InterfaceType.AGGREGATED)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
                .build()));
  }
}
