package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link KernelRoute}. */
public final class KernelRouteTest {

  @Test
  public void testEquals() {
    KernelRoute.Builder builder = KernelRoute.builder();
    KernelRoute kr = builder.setNetwork(Prefix.strict("10.0.0.0/24")).build();
    new EqualsTester()
        .addEqualityGroup(kr, kr, builder.build())
        .addEqualityGroup(builder.setNetwork(Prefix.ZERO).build())
        .addEqualityGroup((builder.setTag(5L).build()))
        .addEqualityGroup((builder.setRequiredOwnedIp(Ip.ZERO).build()))
        .addEqualityGroup(builder.setNonForwarding(false).build())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    KernelRoute route =
        KernelRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setTag(5L)
            .setRequiredOwnedIp(Ip.ZERO)
            // skip setNonForwarding since its value is not jackson-serialized
            .build();
    assertEquals(route, BatfishObjectMapper.clone(route, KernelRoute.class));
  }

  @Test
  public void testSerialization() {
    KernelRoute route =
        KernelRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setTag(5L)
            .setRequiredOwnedIp(Ip.ZERO)
            .setNonForwarding(false)
            .build();
    assertEquals(route, SerializationUtils.clone(route));
  }
}
