package org.batfish.datamodel.vendor_family.cumulus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IntegerSpace;
import org.junit.Test;

/** Test of {@link CumulusFamily} */
public final class CumulusFamilyTest {

  @Test
  public void testEquals() {
    Bridge.Builder bb =
        Bridge.builder().setPorts(ImmutableSet.of()).setPvid(1).setVids(IntegerSpace.EMPTY);
    CumulusFamily.Builder builder = CumulusFamily.builder().setBridge(bb.build());
    CumulusFamily c1 = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(c1, c1, builder.build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setBridge(bb.setPvid(2).build()).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(
            builder.setInterfaceClagSettings(
                ImmutableSortedMap.of("foo", InterfaceClagSettings.builder().build())))
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    CumulusFamily c =
        CumulusFamily.builder()
            .setBridge(
                Bridge.builder()
                    .setPorts(ImmutableSet.of())
                    .setPvid(1)
                    .setVids(IntegerSpace.EMPTY)
                    .build())
            .setInterfaceClagSettings(
                ImmutableSortedMap.of("foo", InterfaceClagSettings.builder().build()))
            .build();

    assertThat(BatfishObjectMapper.clone(c, CumulusFamily.class), equalTo(c));
  }

  @Test
  public void testJavaSerialization() {
    CumulusFamily c =
        CumulusFamily.builder()
            .setBridge(
                Bridge.builder()
                    .setPorts(ImmutableSet.of())
                    .setPvid(1)
                    .setVids(IntegerSpace.EMPTY)
                    .build())
            .setInterfaceClagSettings(
                ImmutableSortedMap.of("foo", InterfaceClagSettings.builder().build()))
            .build();

    assertThat(SerializationUtils.clone(c), equalTo(c));
  }
}
