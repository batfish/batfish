package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class TestrigMetadata {

  public enum ProcessingStatus {
    NONE,
    PARSING,
    PARSED,
    PARSING_FAIL,
    DATAPLANING,
    DATAPLANED,
    DATAPLANING_FAIL
  }

  private static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  private static final String PROP_PROCESSING_STATUS = "processingStatus";

  private Instant _creationTimestamp;

  private ProcessingStatus _processingStatus;

  @JsonCreator
  public TestrigMetadata(@JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp) {
    this._creationTimestamp = creationTimestamp;
    _processingStatus = ProcessingStatus.NONE;
  }

  @JsonProperty(PROP_CREATIONTIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_PROCESSING_STATUS)
  public ProcessingStatus getProcessingStatus() {
    return _processingStatus;
  }

  @JsonProperty(PROP_PROCESSING_STATUS)
  public void setProcessingStatus(ProcessingStatus status) {
    _processingStatus = status;
  }
}
