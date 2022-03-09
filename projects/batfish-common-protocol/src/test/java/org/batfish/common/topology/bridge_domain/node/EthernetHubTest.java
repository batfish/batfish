package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link EthernetHub}. */
public final class EthernetHubTest {

  @Test
  public void testEquals() {
    EthernetHub obj = new EthernetHub("a");
    new EqualsTester()
        .addEqualityGroup(obj, new EthernetHub("a"))
        .addEqualityGroup(new EthernetHub("b"))
        .testEquals();
  }
}
