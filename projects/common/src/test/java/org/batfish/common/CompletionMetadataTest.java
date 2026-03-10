package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.specifier.InterfaceLocation;
import org.junit.Test;

public class CompletionMetadataTest {

  @Test
  public void testJavaSerialization() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setFilterNames(ImmutableSet.of("filter"))
            .setInterfaces(ImmutableSet.of(NodeInterfacePair.of("node", "interface")))
            .setIps(ImmutableSet.of(Ip.parse("1.1.1.1")))
            .setLocations(
                ImmutableSet.of(
                    new LocationCompletionMetadata(
                        new InterfaceLocation("node", "interface"), true)))
            .setMlagIds(ImmutableSet.of("mlag"))
            .setNodes(ImmutableSet.of("node"))
            .setPrefixes(ImmutableSet.of("prefix"))
            .setStructureNames(ImmutableSet.of("structure"))
            .setVrfs(ImmutableSet.of("vrf"))
            .setZones(ImmutableSet.of("zone"))
            .build();

    assertThat(SerializationUtils.clone(completionMetadata), equalTo(completionMetadata));
  }

  @Test
  public void testEquals() {
    CompletionMetadata.Builder builder = CompletionMetadata.builder();
    CompletionMetadata initial = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(initial, initial, builder.build(), CompletionMetadata.EMPTY)
        .addEqualityGroup(builder.setFilterNames(ImmutableSet.of("filter")).build())
        .addEqualityGroup(
            builder
                .setInterfaces(ImmutableSet.of(NodeInterfacePair.of("node", "interface")))
                .build())
        .addEqualityGroup(builder.setIps(ImmutableSet.of(Ip.parse("1.1.1.1"))).build())
        .addEqualityGroup(
            builder
                .setLocations(
                    ImmutableSet.of(
                        new LocationCompletionMetadata(
                            new InterfaceLocation("node", "interface"), true)))
                .build())
        .addEqualityGroup(builder.setMlagIds(ImmutableSet.of("mlag")).build())
        .addEqualityGroup(builder.setNodes(ImmutableSet.of("node")).build())
        .addEqualityGroup(builder.setPrefixes(ImmutableSet.of("prefix")).build())
        .addEqualityGroup(builder.setStructureNames(ImmutableSet.of("structure")).build())
        .addEqualityGroup(builder.setVrfs(ImmutableSet.of("vrf")).build())
        .addEqualityGroup(builder.setZones(ImmutableSet.of("zone")).build())
        .testEquals();
  }
}
