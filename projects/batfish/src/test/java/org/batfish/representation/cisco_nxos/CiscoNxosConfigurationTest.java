package org.batfish.representation.cisco_nxos;

import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.toOspfDeadInterval;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.toOspfHelloInterval;
import static org.batfish.representation.cisco_nxos.OspfInterface.DEFAULT_DEAD_INTERVAL_S;
import static org.batfish.representation.cisco_nxos.OspfInterface.DEFAULT_HELLO_INTERVAL_S;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests for {@link CiscoNxosConfiguration} class */
public class CiscoNxosConfigurationTest {

  @Test
  public void testToOspfDeadIntervalExplicit() {
    Interface iface = new Interface("FastEthernet0/0", null, CiscoNxosInterfaceType.ETHERNET, null);
    iface.getOrCreateOspf().setDeadIntervalS(7);
    // Explicitly set dead interval should be preferred over inference
    assertThat(toOspfDeadInterval(iface), equalTo(7));
  }

  @Test
  public void testToOspfDeadIntervalFromHello() {
    Interface iface = new Interface("FastEthernet0/0", null, CiscoNxosInterfaceType.ETHERNET, null);

    int helloInterval = 1;
    iface.getOrCreateOspf().setHelloIntervalS(helloInterval);
    // Since the dead interval is not set, it should be inferred as four times the hello interval
    assertThat(
        toOspfDeadInterval(iface), equalTo(OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval));
  }

  @Test
  public void testToOspfDeadIntervalDefault() {
    Interface iface = new Interface("FastEthernet0/0", null, CiscoNxosInterfaceType.ETHERNET, null);

    // Since the dead interval and hello interval are not set, it should be the default value
    assertThat(toOspfDeadInterval(iface), equalTo(DEFAULT_DEAD_INTERVAL_S));
  }

  @Test
  public void testToOspfHelloIntervalExplicit() {
    Interface iface = new Interface("FastEthernet0/0", null, CiscoNxosInterfaceType.ETHERNET, null);

    iface.getOrCreateOspf().setHelloIntervalS(7);
    // Explicitly set hello interval should be preferred over default
    assertThat(toOspfHelloInterval(iface), equalTo(7));
  }

  @Test
  public void testToOspfHelloIntervalDefault() {
    Interface iface = new Interface("FastEthernet0/0", null, CiscoNxosInterfaceType.ETHERNET, null);

    // Since the hello interval is not set, it should be the default value
    assertThat(toOspfHelloInterval(iface), equalTo(DEFAULT_HELLO_INTERVAL_S));
  }
}
