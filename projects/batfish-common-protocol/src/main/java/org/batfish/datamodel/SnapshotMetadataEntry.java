package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;
import static org.batfish.common.BfConsts.PROP_METADATA;
import static org.batfish.common.BfConsts.PROP_NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class SnapshotMetadataEntry {
  @JsonCreator
  private static @Nonnull SnapshotMetadataEntry create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_METADATA) @Nullable SnapshotMetadata metadata) {
    return new SnapshotMetadataEntry(requireNonNull(name), requireNonNull(metadata));
  }

  private final SnapshotMetadata _metadata;

  private final String _name;

  public SnapshotMetadataEntry(String name, SnapshotMetadata metadata) {
    _name = name;
    _metadata = metadata;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SnapshotMetadataEntry)) {
      return false;
    }
    SnapshotMetadataEntry rhs = (SnapshotMetadataEntry) obj;
    return _name.equals(rhs._name) && _metadata.equals(rhs._metadata);
  }

  @JsonProperty(PROP_METADATA)
  public @Nonnull SnapshotMetadata getMetadata() {
    return _metadata;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _metadata);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_NAME, _name)
        .add(PROP_METADATA, _metadata)
        .toString();
  }
}
