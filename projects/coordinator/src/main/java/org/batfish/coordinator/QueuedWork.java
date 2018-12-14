package org.batfish.coordinator;

import java.util.Date;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.datamodel.pojo.WorkStatus;

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

  public synchronized void clearAssignment() {
    _dateAssigned = null;
    _assignedWorker = null;

    _lastTaskCheckResult = null;
    _dateLastTaskCheckedStatus = null;
  }

  public String getAssignedWorker() {
    return _assignedWorker;
  }

  public Date getDateCreated() {
    return _dateCreated;
  }

  public Date getDateTerminated() {
    return _dateTerminated;
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

  public synchronized void recordTaskCheckResult(Task task) {
    _lastTaskCheckResult = task;
    _dateLastTaskCheckedStatus = new Date();
  }

  public synchronized void setAssignment(String assignedWorker) {
    _status = WorkStatusCode.ASSIGNED;
    _assignedWorker = assignedWorker;
    _dateAssigned = new Date();
  }

  public synchronized void setStatus(WorkStatusCode status) {
    if (_status.isTerminated() && !status.isTerminated()) {
      throw new IllegalStateException(
          "Status of terminated work shouldn't be updated to non-terminated. Current: "
              + _status
              + ". Desired = "
              + status);
    }
    if (!_status.isTerminated() && status.isTerminated()) {
      _dateTerminated = new Date();
    }
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

  public @Nonnull WorkStatus toWorkStatus() {
    return new WorkStatus(_workItem, _status, _lastTaskCheckResult);
  }
}
