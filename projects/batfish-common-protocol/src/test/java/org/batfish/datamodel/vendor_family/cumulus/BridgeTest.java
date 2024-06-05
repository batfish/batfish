package org.batfish.datamodel.vendor_family.cumulus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IntegerSpace;
import org.junit.Test;

/** Test of {@link Bridge} */
public final class BridgeTest {

  @Test
  public void testEquals() {
    Bridge.Builder builder = Bridge.builder();
    Bridge bridge =
        builder.setPorts(ImmutableSet.of()).setPvid(1).setVids(IntegerSpace.EMPTY).build();
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(bridge, bridge, builder.build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setPorts(ImmutableSet.of("a")).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setPvid(5).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setVids(IntegerSpace.of(5)).build())
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    Bridge bridge =
        Bridge.builder()
            .setPorts(ImmutableSet.of("a"))
            .setPvid(2)
            .setVids(IntegerSpace.of(2))
            .build();

    assertThat(BatfishObjectMapper.clone(bridge, Bridge.class), equalTo(bridge));
  }

  @Test
  public void testJavaSerialization() {
    Bridge bridge =
        Bridge.builder()
            .setPorts(ImmutableSet.of("a"))
            .setPvid(2)
            .setVids(IntegerSpace.of(2))
            .build();

    assertThat(SerializationUtils.clone(bridge), equalTo(bridge));
  }
}
