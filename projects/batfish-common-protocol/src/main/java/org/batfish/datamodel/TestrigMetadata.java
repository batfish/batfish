package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;

public final class TestrigMetadata {

  static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  static final String PROP_ENVIRONMENTS = "environments";
  static final String PROP_PARENTSNAPSHOT = "parentSnapshot";

  private Instant _creationTimestamp;

  private Map<String, EnvironmentMetadata> _environments;

  private String _parentSnapshot;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp,
      @JsonProperty(PROP_ENVIRONMENTS) Map<String, EnvironmentMetadata> environments,
      @JsonProperty(PROP_PARENTSNAPSHOT) String parentSnapshot) {
    _creationTimestamp = creationTimestamp;
    _environments = environments;
    _parentSnapshot = parentSnapshot;
  }

  public TestrigMetadata(
      Instant creationTimestamp, String environment, @Nullable String parentSnapshot) {
    _creationTimestamp = creationTimestamp;
    _environments = new HashMap<>();
    _parentSnapshot = parentSnapshot;
    initializeEnvironment(environment);
  }

  public void initializeEnvironment(String environment) {
    _environments.put(
        environment,
        new EnvironmentMetadata(ProcessingStatus.UNINITIALIZED, null, new LinkedList<>()));
  }

  @JsonProperty(PROP_CREATIONTIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_ENVIRONMENTS)
  public Map<String, EnvironmentMetadata> getEnvironments() {
    return _environments;
  }

  @JsonProperty(PROP_PARENTSNAPSHOT)
  public String getParentSnapshot() {
    return _parentSnapshot;
  }
}
