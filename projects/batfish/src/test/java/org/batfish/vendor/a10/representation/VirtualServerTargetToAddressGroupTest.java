package org.batfish.vendor.a10.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.Ip;
import org.batfish.referencelibrary.AddressGroup;
import org.junit.Test;

/** Test of {@link VirtualServerTargetToAddressGroup}. */
public class VirtualServerTargetToAddressGroupTest {
  @Test
  public void testVisitAddress() {
    Ip ip = Ip.parse("10.10.10.10");
    VirtualServerTargetAddress vsta = new VirtualServerTargetAddress(ip);
    assertThat(
        new VirtualServerTargetToAddressGroup("name").visit(vsta),
        equalTo(new AddressGroup(ImmutableSortedSet.of("10.10.10.10"), "name")));
  }
}
