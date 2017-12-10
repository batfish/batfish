package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EnvironmentMetadata {

  public enum ProcessingStatus {
    UNINITIALIZED,
    PARSING,
    PARSED,
    PARSING_FAIL,
    DATAPLANING,
    DATAPLANED,
    DATAPLANING_FAIL
  }

  private static final String PROP_PROCESSING_STATUS = "processingStatus";

  private ProcessingStatus _processingStatus;

  @JsonCreator
  public EnvironmentMetadata(@JsonProperty(PROP_PROCESSING_STATUS) ProcessingStatus status) {
    _processingStatus = status;
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
