package org.batfish.coordinator;

import java.util.Date;
import java.util.UUID;
import org.batfish.common.BatfishException;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Pair;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.util.WorkItemBuilder;

public class QueuedWork {

  public enum WorkType {
    PARSING,
    DATAPLANING,
    ANSWERING, // includes analyzing
    UNKNOWN
  }

  public class Details {
    public String baseTestrig;
    public String baseEnvironment;
    public String deltaTestrig;
    public String deltaEnvironment;
    public WorkType workType;
    public boolean isDataplaneDependent;
    public boolean isDifferential;
  }

  String _assignedWorker;

  Date _dateAssigned;

  Date _dateCreated;
  Date _dateLastTaskCheckedStatus;
  Date _dateTerminated;

  Details _details;

  Task _lastTaskCheckResult;
  WorkStatusCode _status;

  WorkItem _workItem;

  public QueuedWork(WorkItem workItem) {
    _workItem = workItem;
    _status = WorkStatusCode.UNASSIGNED;
    _dateCreated = new Date();

    _details = new Details();
    Pair<Pair<String, String>, Pair<String, String>> settings =
        WorkItemBuilder.getBaseAndDeltaSettings(workItem);
    _details.baseTestrig = WorkItemBuilder.getBaseTestrig(settings);
    _details.baseEnvironment = WorkItemBuilder.getBaseEnvironment(settings);
    _details.deltaTestrig = WorkItemBuilder.getDeltaTestrig(settings);
    _details.deltaEnvironment = WorkItemBuilder.getDeltaEnvironment(settings);

    _details.workType = WorkType.UNKNOWN;
    if (WorkItemBuilder.isParsingWorkItem(workItem)) {
      _details.workType = WorkType.PARSING;
    }
    if (WorkItemBuilder.isDataplaningWorkItem(workItem)) {
      if (_details.workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate PARSING and DATAPLANING.");
      }
      _details.workType = WorkType.DATAPLANING;
    }
    if (WorkItemBuilder.isAnsweringWorkItem(workItem)
        || WorkItemBuilder.isAnalyzingWorkItem(workItem)) {
      if (_details.workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate ANSWERING from other work.");
      }
      _details.workType = WorkType.ANSWERING;

      //TODO: question type classification
    }
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

  public Details getDetails() {
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
        _workItem.toJsonString(),
        _status,
        _dateCreated,
        _dateAssigned,
        _dateTerminated,
        _assignedWorker,
        (_lastTaskCheckResult == null) ? "null" : _lastTaskCheckResult.getStatus(),
        _dateLastTaskCheckedStatus);
  }
}
