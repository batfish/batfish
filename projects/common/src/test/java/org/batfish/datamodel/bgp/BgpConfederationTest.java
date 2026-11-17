package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link BgpConfederation} */
class BgpConfederationTest {

  @Test
  void testEquals() {
    BgpConfederation bc = new BgpConfederation(1, ImmutableSet.of(2L));
    new EqualsTester()
        .addEqualityGroup(bc, bc, new BgpConfederation(1, ImmutableSet.of(2L)))
        .addEqualityGroup(new BgpConfederation(2, ImmutableSet.of(3L)))
        .addEqualityGroup(new BgpConfederation(1, ImmutableSet.of(3L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    BgpConfederation bc = new BgpConfederation(1, ImmutableSet.of(2L));
    assertThat(SerializationUtils.clone(bc), equalTo(bc));
  }

  @Test
  void testJsonSerialization() {
    BgpConfederation bc = new BgpConfederation(1, ImmutableSet.of(2L));
    assertThat(BatfishObjectMapper.clone(bc, BgpConfederation.class), equalTo(bc));
  }

  @Test
  void testRejectEmptyConfederation() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new BgpConfederation(1, ImmutableSet.of());
        });
  }
}
