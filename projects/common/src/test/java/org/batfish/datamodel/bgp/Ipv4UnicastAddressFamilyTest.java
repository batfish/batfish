package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily.Builder;
import org.junit.Test;

/** Tests of {@link Ipv4UnicastAddressFamily} */
public class Ipv4UnicastAddressFamilyTest {
  @Test
  public void testEquals() {
    Builder builder = Ipv4UnicastAddressFamily.builder();
    Ipv4UnicastAddressFamily af =
        builder
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setAllowLocalAsIn(true).build())
            .setExportPolicy("export")
            .setExportPolicySources(ImmutableSortedSet.of("export-source"))
            .setImportPolicy("import")
            .setImportPolicySources(ImmutableSortedSet.of("import-source"))
            .setRouteReflectorClient(true)
            .build();
    new EqualsTester()
        .addEqualityGroup(withToBuilder(af))
        .addEqualityGroup(
            withToBuilder(
                af.toBuilder()
                    .setAddressFamilyCapabilities(
                        AddressFamilyCapabilities.builder().setSendCommunity(true).build())
                    .build()))
        .addEqualityGroup(withToBuilder(af.toBuilder().setExportPolicy("other-export").build()))
        .addEqualityGroup(withToBuilder(af.toBuilder().setImportPolicy("other-import").build()))
        .addEqualityGroup(
            withToBuilder(
                af.toBuilder()
                    .setExportPolicySources(ImmutableSortedSet.of("other-export-source"))
                    .build()))
        .addEqualityGroup(
            withToBuilder(
                af.toBuilder()
                    .setImportPolicySources(ImmutableSortedSet.of("other-import-source"))
                    .build()))
        .addEqualityGroup(withToBuilder(af.toBuilder().setRouteReflectorClient(false).build()))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  private static List<Ipv4UnicastAddressFamily> withToBuilder(
      Ipv4UnicastAddressFamily addressFamily) {
    return Arrays.asList(addressFamily, addressFamily.toBuilder().build());
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
