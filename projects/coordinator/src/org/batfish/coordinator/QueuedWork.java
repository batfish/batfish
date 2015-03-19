package org.batfish.coordinator;

import java.util.Date;

import org.batfish.common.WorkItem;

public class QueuedWork {
   
   WorkItem _workItem;   
   
   Date _dateCreated;   
   Date _dateAssigned;
   Date _dataCompleted;
   
   String _assignedWorker;      
   
   public QueuedWork(WorkItem workItem) {
      _workItem = workItem;
      _dateCreated = new Date();
   }
   
   public void setAssignment(String assignedWorker) {
      _assignedWorker = assignedWorker;
      _dateAssigned = new Date();
   }
   
   public String getAssignedWorker() {
      return _assignedWorker;
   }
   
   public WorkItem getWorkItem() {
      return _workItem;
   }
}