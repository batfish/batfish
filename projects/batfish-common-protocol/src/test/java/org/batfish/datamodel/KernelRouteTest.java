package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public final class KernelRouteTest {

  @Test
  public void testEquals() {
    KernelRoute.Builder builder = KernelRoute.builder();
    KernelRoute kr = builder.setNetwork(Prefix.strict("10.0.0.0/24")).build();
    new EqualsTester()
        .addEqualityGroup(kr, kr, builder.build())
        .addEqualityGroup(builder.setNetwork(Prefix.ZERO).build())
        .addEqualityGroup((builder.setTag(5L).build()))
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    KernelRoute route = new KernelRoute(Prefix.ZERO);
    assertEquals(route, BatfishObjectMapper.clone(route, KernelRoute.class));
  }

  @Test
  public void testSerialization() {
    KernelRoute route = new KernelRoute(Prefix.ZERO);
    assertEquals(route, SerializationUtils.clone(route));
  }
}
