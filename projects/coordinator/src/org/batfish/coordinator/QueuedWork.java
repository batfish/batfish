package org.batfish.coordinator;

import java.util.Date;
import java.util.UUID;

import org.batfish.common.CoordinatorConstants.WorkStatusCode;
import org.batfish.common.WorkItem;

public class QueuedWork {
   
   WorkItem _workItem;   
   
   WorkStatusCode _status;
   
   Date _dateCreated;   
   Date _dateAssigned;
   Date _dateTerminated;
   
   String _assignedWorker;      
   
   public QueuedWork(WorkItem workItem) {
      _workItem = workItem;
      _status = WorkStatusCode.UNASSIGNED;
      _dateCreated = new Date();
   }
   
   public void setAssignment(String assignedWorker) {
      _assignedWorker = assignedWorker;
      _dateAssigned = new Date();
   }
   
   public String getAssignedWorker() {
      return _assignedWorker;
   }
   
   public UUID getId() {
      return _workItem.getId();
   }
   
   public WorkStatusCode getStatus() {
      return _status;
   }
   
   public WorkItem getWorkItem() {
      return _workItem;
   }

   public void setStatus(WorkStatusCode status) {
      _status = status;
   }
}