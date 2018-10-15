package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

public class InitializationMetadata {

  public enum ProcessingStatus {
    UNINITIALIZED,
    PARSING,
    PARSED,
    PARSING_FAIL,
    DATAPLANING,
    DATAPLANED,
    DATAPLANING_FAIL
  }

  private static final String PROP_CURRENT_STATUS = "currentStatus";

  private static final String PROP_ERR_MESSAGE = "errMessage";

  private static final String PROP_STATUS_HISTORY = "statusHistory";

  private ProcessingStatus _currentStatus;

  private String _errMessage;

  private List<String> _statusHistory;

  @JsonCreator
  public InitializationMetadata(
      @JsonProperty(PROP_CURRENT_STATUS) ProcessingStatus status,
      @Nullable @JsonProperty(PROP_ERR_MESSAGE) String errMessage,
      @JsonProperty(PROP_STATUS_HISTORY) List<String> statusHistory) {
    _currentStatus = status;
    _errMessage = errMessage;
    _statusHistory = statusHistory;
    if (_statusHistory == null) {
      _statusHistory = new LinkedList<>();
    }
  }

  @JsonProperty(PROP_CURRENT_STATUS)
  public ProcessingStatus getProcessingStatus() {
    return _currentStatus;
  }

  @JsonProperty(PROP_ERR_MESSAGE)
  public String getErrMessage() {
    return _errMessage;
  }

  @JsonProperty(PROP_STATUS_HISTORY)
  public List<String> getStatusHistory() {
    return _statusHistory;
  }

  public void updateStatus(ProcessingStatus status, String errMessage) {
    _currentStatus = status;
    if (errMessage != null) {
      _errMessage = errMessage;
    }
    _statusHistory.add("Status changed to " + status + " at " + Instant.now());
  }
}
