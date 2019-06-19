package org.batfish.coordinator;

import java.util.Date;

public class WorkerStatus {

  public enum StatusCode {
    BUSY,
    IDLE,
    TRYINGTOASSIGN,
    UNKNOWN,
    UNREACHABLE
  }

  private Date _lastUpdated;
  private StatusCode _statusCode;

  public WorkerStatus(StatusCode statusCode) {
    _statusCode = statusCode;
    _lastUpdated = new Date();
  }

  public Date getLastUpdateTime() {
    return _lastUpdated;
  }

  public StatusCode getStatus() {
    return _statusCode;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", _statusCode, _lastUpdated);
  }

  public void updateStatus(StatusCode statusCode) {
    _statusCode = statusCode;
    _lastUpdated = new Date();
  }
}
