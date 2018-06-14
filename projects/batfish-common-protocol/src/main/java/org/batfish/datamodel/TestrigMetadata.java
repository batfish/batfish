package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;

public final class TestrigMetadata {

  private static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  private static final String PROP_ENVIRONMENTS = "environments";

  private Instant _creationTimestamp;

  private Map<String, EnvironmentMetadata> _environments;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp,
      @JsonProperty(PROP_ENVIRONMENTS) Map<String, EnvironmentMetadata> environments) {
    _creationTimestamp = creationTimestamp;
    _environments = environments;
  }

  public TestrigMetadata(Instant creationTimestamp, String environment) {
    _creationTimestamp = creationTimestamp;
    _environments = new HashMap<>();
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
}
