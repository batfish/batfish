package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link VrfForwardingBehavior}. */
public final class VrfForwardingBehaviorTest {
  @Test
  public void testEquals() {
    IpSpace a = Prefix.parse("10.0.0.0/8").toIpSpace();
    IpSpace b = Prefix.parse("10.0.0.0/16").toIpSpace();
    IpSpace e = EmptyIpSpace.INSTANCE;
    InterfaceForwardingBehavior ifb = new InterfaceForwardingBehavior(a, e, e, e, e);
    Edge edge = Edge.of("n1", "i1", "n2", "i2");
    new EqualsTester()
        .addEqualityGroup(
            new VrfForwardingBehavior(
                ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), e, e),
            new VrfForwardingBehavior(
                ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), e, e))
        .addEqualityGroup(
            new VrfForwardingBehavior(
                ImmutableMap.of(edge, a), ImmutableMap.of(), ImmutableMap.of(), e, e))
        .addEqualityGroup(
            new VrfForwardingBehavior(
                ImmutableMap.of(), ImmutableMap.of("i1", ifb), ImmutableMap.of(), e, e))
        .addEqualityGroup(
            new VrfForwardingBehavior(
                ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of("v2", a), e, e))
        .addEqualityGroup(
            new VrfForwardingBehavior(
                ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), a, e))
        .addEqualityGroup(
            new VrfForwardingBehavior(
                ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), e, b))
        .testEquals();
  }
}
