package org.batfish.coordinator;

import java.util.Date;
import java.util.UUID;

import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.WorkItem;

public class QueuedWork {

   String _assignedWorker;

   Date _dateAssigned;

   Date _dateCreated;
   Date _dateLastTaskCheckedStatus;
   Date _dateTerminated;

   TaskStatus _lastTaskCheckedStatus;
   WorkStatusCode _status;

   WorkItem _workItem;

   public QueuedWork(WorkItem workItem) {
      _workItem = workItem;
      _status = WorkStatusCode.UNASSIGNED;
      _dateCreated = new Date();
   }

   public void clearAssignment() {
      _dateAssigned = null;
      _assignedWorker = null;

      _lastTaskCheckedStatus = null;
      _dateLastTaskCheckedStatus = null;
   }

   public String getAssignedWorker() {
      return _assignedWorker;
   }

   public UUID getId() {
      return _workItem.getId();
   }

   public TaskStatus getLastTaskCheckedStatus() {
      return _lastTaskCheckedStatus;
   }

   public WorkStatusCode getStatus() {
      return _status;
   }

   public WorkItem getWorkItem() {
      return _workItem;
   }

   public void recordTaskStatusCheckResult(TaskStatus status) {
      _lastTaskCheckedStatus = status;
      _dateLastTaskCheckedStatus = new Date();
   }

   public void setAssignment(String assignedWorker) {
      _status = WorkStatusCode.ASSIGNED;
      _assignedWorker = assignedWorker;
      _dateAssigned = new Date();
   }

   public void setStatus(WorkStatusCode status) {
      _status = status;
   }

   @Override
   public String toString() {
      return String.format("%s [%s] [%s %s %s] [%s] [%s, %s]",
            _workItem.toJsonString(), _status, _dateCreated, _dateAssigned,
            _dateTerminated, _assignedWorker, _lastTaskCheckedStatus,
            _dateLastTaskCheckedStatus);
   }
}