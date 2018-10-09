package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.batfish.identifiers.SnapshotId;

public final class TestrigMetadata {
  // Visible for testing
  static final String PROP_CREATION_TIMESTAMP = "creationTimestamp";
  static final String PROP_ENVIRONMENTS = "environments";
  static final String PROP_PARENT_SNAPSHOT_ID = "parentSnapshotId";

  private Instant _creationTimestamp;

  private Map<String, EnvironmentMetadata> _environments;

  private SnapshotId _parentSnapshotId;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATION_TIMESTAMP) Instant creationTimestamp,
      @JsonProperty(PROP_ENVIRONMENTS) Map<String, EnvironmentMetadata> environments,
      @JsonProperty(PROP_PARENT_SNAPSHOT_ID) SnapshotId parentSnapshot) {
    _creationTimestamp = creationTimestamp;
    _environments = environments;
    _parentSnapshotId = parentSnapshot;
  }

  public TestrigMetadata(
      Instant creationTimestamp, String environment, @Nullable SnapshotId parentSnapshotId) {
    _creationTimestamp = creationTimestamp;
    _environments = new HashMap<>();
    _parentSnapshotId = parentSnapshotId;
    initializeEnvironment(environment);
  }

  public void initializeEnvironment(String environment) {
    _environments.put(
        environment,
        new EnvironmentMetadata(ProcessingStatus.UNINITIALIZED, null, new LinkedList<>()));
  }

  @JsonProperty(PROP_CREATION_TIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_ENVIRONMENTS)
  public Map<String, EnvironmentMetadata> getEnvironments() {
    return _environments;
  }

  @JsonProperty(PROP_PARENT_SNAPSHOT_ID)
  public SnapshotId getParentSnapshotId() {
    return _parentSnapshotId;
  }
}
