package org.batfish.coordinator;

import java.util.Date;

public class WorkerStatus {

   public enum StatusCode { IDLE, BUSY, UNREACHABLE, UNKNOWN }

   private StatusCode _statusCode;
   private Date _lastUpdated;
   
   public WorkerStatus(StatusCode statusCode) {
      _statusCode = statusCode;
      _lastUpdated = new Date();
   }
   
   public void UpdateStatus(StatusCode statusCode) {
      _statusCode = statusCode;
      _lastUpdated = new Date();
   }
   
   public StatusCode getStatus() {
      return _statusCode;
   }
   
   public Date getLastUpdateTime() {
      return _lastUpdated;
   }
   
   public String toString() {
      return String.format("%s (%s)", _statusCode, _lastUpdated);
   }
}