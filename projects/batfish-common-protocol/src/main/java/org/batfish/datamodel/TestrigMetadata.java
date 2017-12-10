package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;

public class TestrigMetadata {

  private static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  private static final String PROP_ENVIRONMENTS = "environments";

  private Instant _creationTimestamp;

  private Map<String, EnvironmentMetadata> _environments;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp,
      @JsonProperty(PROP_ENVIRONMENTS) Map<String, EnvironmentMetadata> environments) {
    this._creationTimestamp = creationTimestamp;
    _environments = environments;
  }

  public TestrigMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp) {
    this._creationTimestamp = creationTimestamp;
    _environments = new HashMap<>();
    initializeEnvironment(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME);
  }

  public void initializeEnvironment(String environment) {
    _environments.put(environment, new EnvironmentMetadata(ProcessingStatus.UNINITIALIZED));
  }

  public static TestrigMetadata fromJsonStr(String jsonStr) throws IOException {
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    return mapper.readValue(jsonStr, TestrigMetadata.class);
  }

  @JsonProperty(PROP_CREATIONTIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_ENVIRONMENTS)
  public Map<String, EnvironmentMetadata> getEnvironments() {
    return _environments;
  }

  public String toJsonString() throws JsonProcessingException {
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    return mapper.writeValueAsString(this);
  }
}
