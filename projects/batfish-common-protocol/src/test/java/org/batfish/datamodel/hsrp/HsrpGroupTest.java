package org.batfish.datamodel.hsrp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.junit.Test;

/** Test of {@link HsrpGroup} */
public class HsrpGroupTest {
  @Test
  public void testJsonSerialization() {
    HsrpGroup objEmpty = HsrpGroup.builder().setGroupNumber(1).build();
    assertThat(objEmpty, equalTo(BatfishObjectMapper.clone(objEmpty, HsrpGroup.class)));

    HsrpGroup obj =
        HsrpGroup.builder()
            .setGroupNumber(1)
            .setPriority(2)
            .setHelloTime(3)
            .setHoldTime(4)
            .setPreempt(true)
            .setAuthentication("auth")
            .setIps(ImmutableSet.of(Ip.ZERO, Ip.MAX))
            .setTrackActions(ImmutableSortedMap.of("1", new DecrementPriority(12)))
            .build();
    assertThat(obj, equalTo(BatfishObjectMapper.clone(obj, HsrpGroup.class)));
  }
}
