package org.batfish.symbolic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Test for {@link IngressLocation}. */
public final class IngressLocationTest {

  @Test
  public void testToString_interface() {
    assertEquals("node[iface]", IngressLocation.interfaceLink("node", "iface").toString());
  }

  @Test
  public void testToString_vrf() {
    assertEquals("node[@vrf(vrf)]", IngressLocation.vrf("node", "vrf").toString());
  }

  @Test
  public void testToString_escape() {
    assertEquals("\"<\"[\",\"]", IngressLocation.interfaceLink("<", ",").toString());
  }
}
