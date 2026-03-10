package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily.Builder;
import org.junit.Test;

/** Tests of {@link Ipv4UnicastAddressFamily} */
public class Ipv4UnicastAddressFamilyTest {
  @Test
  public void testEquals() {
    Builder builder = Ipv4UnicastAddressFamily.builder();
    Ipv4UnicastAddressFamily af = builder.build();
    new EqualsTester()
        .addEqualityGroup(af, af, builder.build())
        .addEqualityGroup(
            builder
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder().setSendCommunity(true).build())
                .build())
        .addEqualityGroup(builder.setExportPolicy("export").build())
        .addEqualityGroup(builder.setImportPolicy("import").build())
        .addEqualityGroup(builder.setExportPolicySources(ImmutableSortedSet.of("foo")).build())
        .addEqualityGroup(builder.setImportPolicySources(ImmutableSortedSet.of("bar")).build())
        .addEqualityGroup(builder.setRouteReflectorClient(true))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Ipv4UnicastAddressFamily af =
        Ipv4UnicastAddressFamily.builder()
            .setExportPolicy("export")
            .setImportPolicy("import")
            .setExportPolicySources(ImmutableSortedSet.of("foo"))
            .setRouteReflectorClient(true)
            .build();
    assertThat(SerializationUtils.clone(af), equalTo(af));
  }

  @Test
  public void testJsonSerialization() {
    Ipv4UnicastAddressFamily af =
        Ipv4UnicastAddressFamily.builder()
            .setExportPolicy("export")
            .setImportPolicy("import")
            .setExportPolicySources(ImmutableSortedSet.of("foo"))
            .setRouteReflectorClient(true)
            .build();
    assertThat(BatfishObjectMapper.clone(af, Ipv4UnicastAddressFamily.class), equalTo(af));
  }
}
