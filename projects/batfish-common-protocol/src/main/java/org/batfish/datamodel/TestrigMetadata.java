package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.LinkedList;
import javax.annotation.Nullable;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.identifiers.SnapshotId;

public final class TestrigMetadata {
  // Visible for testing
  static final String PROP_CREATION_TIMESTAMP = "creationTimestamp";
  static final String PROP_INITIALIZATION_METADATA = "initializationMetadata";
  static final String PROP_PARENT_SNAPSHOT_ID = "parentSnapshotId";

  private Instant _creationTimestamp;

  private InitializationMetadata _initializationMetadata;

  private SnapshotId _parentSnapshotId;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATION_TIMESTAMP) Instant creationTimestamp,
      @JsonProperty(PROP_INITIALIZATION_METADATA) InitializationMetadata initializationMetadata,
      @JsonProperty(PROP_PARENT_SNAPSHOT_ID) SnapshotId parentSnapshot) {
    _creationTimestamp = creationTimestamp;
    _initializationMetadata = initializationMetadata;
    _parentSnapshotId = parentSnapshot;
  }

  public TestrigMetadata(Instant creationTimestamp, @Nullable SnapshotId parentSnapshotId) {
    _creationTimestamp = creationTimestamp;
    _parentSnapshotId = parentSnapshotId;
    _initializationMetadata =
        new InitializationMetadata(ProcessingStatus.UNINITIALIZED, null, new LinkedList<>());
  }

  @JsonProperty(PROP_CREATION_TIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_INITIALIZATION_METADATA)
  public InitializationMetadata getInitializationMetadata() {
    return _initializationMetadata;
  }

  @JsonProperty(PROP_PARENT_SNAPSHOT_ID)
  public SnapshotId getParentSnapshotId() {
    return _parentSnapshotId;
  }
}
