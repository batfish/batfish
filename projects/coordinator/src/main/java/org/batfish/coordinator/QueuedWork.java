package org.batfish.coordinator;

import java.util.Date;
import java.util.UUID;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;

public class QueuedWork {

  String _assignedWorker;

  Date _dateAssigned;

  Date _dateCreated;
  Date _dateLastTaskCheckedStatus;
  Date _dateTerminated;

  WorkDetails _details;

  Task _lastTaskCheckResult;
  WorkStatusCode _status;

  WorkItem _workItem;

  public QueuedWork(WorkItem workItem, WorkDetails details) {
    _workItem = workItem;
    _status = WorkStatusCode.UNASSIGNED;
    _dateCreated = new Date();
    _details = details;
  }

  public void clearAssignment() {
    _dateAssigned = null;
    _assignedWorker = null;

    _lastTaskCheckResult = null;
    _dateLastTaskCheckedStatus = null;
  }

  public String getAssignedWorker() {
    return _assignedWorker;
  }

  public WorkDetails getDetails() {
    return _details;
  }

  public UUID getId() {
    return _workItem.getId();
  }

  public Task getLastTaskCheckResult() {
    return _lastTaskCheckResult;
  }

  public WorkStatusCode getStatus() {
    return _status;
  }

  public WorkItem getWorkItem() {
    return _workItem;
  }

  public void recordTaskCheckResult(Task task) {
    _lastTaskCheckResult = task;
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
    return String.format(
        "%s [%s] [%s %s %s] [%s] [%s, %s]",
        _workItem.toString(),
        _status,
        _dateCreated,
        _dateAssigned,
        _dateTerminated,
        _assignedWorker,
        (_lastTaskCheckResult == null) ? "null" : _lastTaskCheckResult.getStatus(),
        _dateLastTaskCheckedStatus);
  }
}
