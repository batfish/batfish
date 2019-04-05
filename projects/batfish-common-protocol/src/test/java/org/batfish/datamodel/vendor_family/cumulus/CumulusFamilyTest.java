package org.batfish.datamodel.vendor_family.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CumulusFamily} */
public final class CumulusFamilyTest {

  @Test
  public void testEquals() {
    CumulusFamily.Builder builder = CumulusFamily.builder();
    CumulusFamily c1 = builder.build();

    new EqualsTester()
        .addEqualityGroup(c1, c1, builder.build())
        .addEqualityGroup(
            builder.setInterfaceClagSettings(
                ImmutableSortedMap.of("foo", InterfaceClagSettings.builder().build())))
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() throws IOException {
    CumulusFamily c =
        CumulusFamily.builder()
            .setInterfaceClagSettings(
                ImmutableSortedMap.of("foo", InterfaceClagSettings.builder().build()))
            .build();

    assertThat(BatfishObjectMapper.clone(c, CumulusFamily.class), equalTo(c));
  }

  @Test
  public void testJavaSerialization() {
    CumulusFamily c =
        CumulusFamily.builder()
            .setInterfaceClagSettings(
                ImmutableSortedMap.of("foo", InterfaceClagSettings.builder().build()))
            .build();

    assertThat(SerializationUtils.clone(c), equalTo(c));
  }
}
