package org.batfish.datamodel.hsrp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.junit.Test;

/** Test of {@link HsrpGroup} */
public class HsrpGroupTest {
  @Test
  public void testJsonSerialization() {
    HsrpGroup objEmpty = HsrpGroup.builder().build();
    assertThat(objEmpty, equalTo(BatfishObjectMapper.clone(objEmpty, HsrpGroup.class)));

    HsrpGroup obj =
        HsrpGroup.builder()
            .setPriority(2)
            .setHelloTime(3)
            .setHoldTime(4)
            .setPreempt(true)
            .setAuthentication("auth")
            .setVirtualAddresses(ImmutableSet.of(Ip.ZERO, Ip.MAX))
            .setTrackActions(ImmutableSortedMap.of("1", new DecrementPriority(12)))
            .setSourceAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
            .build();
    assertThat(obj, equalTo(BatfishObjectMapper.clone(obj, HsrpGroup.class)));
  }
}
