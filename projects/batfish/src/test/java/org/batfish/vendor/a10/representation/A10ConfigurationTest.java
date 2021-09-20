package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceEnabledEffective;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceMtuEffective;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}
