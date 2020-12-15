package org.batfish.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.common.util.BatfishObjectMapper;
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
  public void testJsonSerialization() {
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

    assertThat(
        BatfishObjectMapper.clone(completionMetadata, CompletionMetadata.class),
        equalTo(completionMetadata));
  }

  @Test
  public void testJsonDeserializationDeprecatedSourceLocation() throws JsonProcessingException {
    String json = readResource("org/batfish/common/util/deprecated_source_location.json", UTF_8);
    CompletionMetadata completionMetadataIn =
        BatfishObjectMapper.mapper().readValue(json, CompletionMetadata.class);

    CompletionMetadata completionMetadataExpected =
        CompletionMetadata.builder()
            .setLocations(
                ImmutableSet.of(
                    new LocationCompletionMetadata(
                        new InterfaceLocation("node", "interface"), true)))
            .build();

    assertThat(completionMetadataIn, equalTo(completionMetadataExpected));
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
