package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents address space paritions of nodes */
@ParametersAreNonnullByDefault
public final class AddressSpacePartitions {

  public static final AddressSpacePartitions EMPTY = new AddressSpacePartitions(ImmutableMap.of());

  private static final String PROP_PARTITION_MAPPING = "partitionMapping";

  @Nonnull private final Map<String, String> _partitionMapping;

  @JsonCreator
  private static @Nonnull AddressSpacePartitions create(
      @JsonProperty(PROP_PARTITION_MAPPING) Map<String, String> partitions) {

    return new AddressSpacePartitions(firstNonNull(partitions, ImmutableMap.of()));
  }

  public AddressSpacePartitions(Map<String, String> partitionMapping) {
    _partitionMapping = ImmutableMap.copyOf(partitionMapping);
  }

  @Nonnull
  @JsonProperty(PROP_PARTITION_MAPPING)
  public Map<String, String> getPartitionMapping() {
    return _partitionMapping;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddressSpacePartitions)) {
      return false;
    }
    AddressSpacePartitions that = (AddressSpacePartitions) o;
    return Objects.equal(_partitionMapping, that._partitionMapping);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_partitionMapping);
  }
}
