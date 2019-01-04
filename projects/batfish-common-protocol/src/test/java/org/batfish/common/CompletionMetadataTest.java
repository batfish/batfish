package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class CompletionMetadataTest {

  @Test
  public void testEquals() {
    CompletionMetadata.Builder builder = CompletionMetadata.builder();
    CompletionMetadata initial = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(initial, initial, builder.build(), new CompletionMetadata())
        .addEqualityGroup(builder.setAddressBooks(ImmutableSet.of("addressBook")).build())
        .addEqualityGroup(builder.setAddressGroups(ImmutableSet.of("addressGroup")).build())
        .addEqualityGroup(builder.setFilterNames(ImmutableSet.of("filter")).build())
        .addEqualityGroup(
            builder
                .setInterfaces(ImmutableSet.of(new NodeInterfacePair("interface", "node")))
                .build())
        .addEqualityGroup(builder.setIps(ImmutableSet.of("ip")).build())
        .addEqualityGroup(builder.setPrefixes(ImmutableSet.of("prefix")).build())
        .addEqualityGroup(builder.setStructureNames(ImmutableSet.of("structure")).build())
        .addEqualityGroup(builder.setVrfs(ImmutableSet.of("vrf")).build())
        .addEqualityGroup(builder.setZones(ImmutableSet.of("zone")).build())
        .testEquals();

    assertThat(CompletionMetadata.builder().build(), equalTo(new CompletionMetadata()));
  }
}
