package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.identifiers.SnapshotId;

@ParametersAreNonnullByDefault
public final class SnapshotMetadata {

  @VisibleForTesting static final String PROP_CREATION_TIMESTAMP = "creationTimestamp";
  @VisibleForTesting static final String PROP_INITIALIZATION_METADATA = "initializationMetadata";
  @VisibleForTesting static final String PROP_PARENT_SNAPSHOT_ID = "parentSnapshotId";

  @JsonCreator
  private static @Nonnull SnapshotMetadata create(
      @JsonProperty(PROP_CREATION_TIMESTAMP) @Nullable Instant creationTimestamp,
      @JsonProperty(PROP_INITIALIZATION_METADATA) @Nullable
          InitializationMetadata initializationMetadata,
      @JsonProperty(PROP_PARENT_SNAPSHOT_ID) @Nullable SnapshotId parentSnapshot) {
    return new SnapshotMetadata(
        requireNonNull(creationTimestamp), requireNonNull(initializationMetadata), parentSnapshot);
  }

  private final Instant _creationTimestamp;

  private final InitializationMetadata _initializationMetadata;

  private final SnapshotId _parentSnapshotId;

  public SnapshotMetadata(
      Instant creationTimestamp,
      InitializationMetadata initializationMetadata,
      @Nullable SnapshotId parentSnapshot) {
    _creationTimestamp = creationTimestamp;
    _initializationMetadata = initializationMetadata;
    _parentSnapshotId = parentSnapshot;
  }

  public SnapshotMetadata(Instant creationTimestamp, @Nullable SnapshotId parentSnapshotId) {
    _creationTimestamp = creationTimestamp;
    _parentSnapshotId = parentSnapshotId;
    _initializationMetadata =
        new InitializationMetadata(ProcessingStatus.UNINITIALIZED, null, ImmutableList.of());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SnapshotMetadata)) {
      return false;
    }
    SnapshotMetadata rhs = (SnapshotMetadata) obj;
    return _creationTimestamp.equals(rhs._creationTimestamp)
        && _initializationMetadata.equals(rhs._initializationMetadata)
        && Objects.equals(_parentSnapshotId, rhs._parentSnapshotId);
  }

  @JsonProperty(PROP_CREATION_TIMESTAMP)
  public @Nonnull Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_INITIALIZATION_METADATA)
  public @Nonnull InitializationMetadata getInitializationMetadata() {
    return _initializationMetadata;
  }

  @JsonProperty(PROP_PARENT_SNAPSHOT_ID)
  public @Nullable SnapshotId getParentSnapshotId() {
    return _parentSnapshotId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_creationTimestamp, _initializationMetadata, _parentSnapshotId);
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_CREATION_TIMESTAMP, _creationTimestamp)
        .add(PROP_INITIALIZATION_METADATA, _initializationMetadata)
        .add(PROP_PARENT_SNAPSHOT_ID, _parentSnapshotId)
        .toString();
  }

  public @Nonnull SnapshotMetadata updateStatus(
      ProcessingStatus status, @Nullable String errMessage) {
    return new SnapshotMetadata(
        _creationTimestamp,
        _initializationMetadata.updateStatus(status, errMessage),
        _parentSnapshotId);
  }
}
