package org.batfish.datamodel;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link InterfaceForwardingBehavior}. */
public final class InterfaceForwardingBehaviorTest {
  @Test
  public void testEquals() {
    IpSpace a = Prefix.parse("10.0.0.0/8").toIpSpace();
    IpSpace b = Prefix.parse("10.0.0.0/16").toIpSpace();
    IpSpace e = EmptyIpSpace.INSTANCE;
    new EqualsTester()
        .addEqualityGroup(
            new InterfaceForwardingBehavior(a, e, e, e, e),
            new InterfaceForwardingBehavior(a, null, null, null, null))
        .addEqualityGroup(new InterfaceForwardingBehavior(b, e, e, e, e))
        .addEqualityGroup(new InterfaceForwardingBehavior(a, b, e, e, e))
        .addEqualityGroup(new InterfaceForwardingBehavior(a, e, b, e, e))
        .addEqualityGroup(new InterfaceForwardingBehavior(a, e, e, b, e))
        .addEqualityGroup(new InterfaceForwardingBehavior(a, e, e, e, b))
        .testEquals();
  }
}
