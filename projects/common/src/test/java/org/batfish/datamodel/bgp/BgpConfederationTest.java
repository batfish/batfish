package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link BgpConfederation} */
public class BgpConfederationTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    BgpConfederation bc = new BgpConfederation(1, ImmutableSet.of(2L));
    new EqualsTester()
        .addEqualityGroup(bc, bc, new BgpConfederation(1, ImmutableSet.of(2L)))
        .addEqualityGroup(new BgpConfederation(2, ImmutableSet.of(3L)))
        .addEqualityGroup(new BgpConfederation(1, ImmutableSet.of(3L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    BgpConfederation bc = new BgpConfederation(1, ImmutableSet.of(2L));
    assertThat(SerializationUtils.clone(bc), equalTo(bc));
  }

  @Test
  public void testJsonSerialization() {
    BgpConfederation bc = new BgpConfederation(1, ImmutableSet.of(2L));
    assertThat(BatfishObjectMapper.clone(bc, BgpConfederation.class), equalTo(bc));
  }

  @Test
  public void testRejectEmptyConfederation() {
    thrown.expect(IllegalArgumentException.class);
    new BgpConfederation(1, ImmutableSet.of());
  }
}
