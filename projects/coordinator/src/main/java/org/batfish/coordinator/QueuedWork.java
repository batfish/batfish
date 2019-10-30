package org.batfish.coordinator;

import com.google.common.collect.ImmutableMap;
import io.opentracing.SpanContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.batfish.common.BfConsts;
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

  /** For tracing */
  private SpanContext _postAssignmentContext;

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

  SpanContext getPostAssignmentContext() {
    return _postAssignmentContext;
  }

  void setPostAssignmentContext(SpanContext postAssignmentContext) {
    _postAssignmentContext = postAssignmentContext;
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

  /**
   * Returns request params with names replaced with IDs. Adds SNAPSHOT_NAME for worker tasks that
   * need it.
   */
  public @Nonnull Map<String, String> resolveRequestParams() {
    Map<String, String> params = new HashMap<>(_workItem.getRequestParams());
    params.put(BfConsts.ARG_CONTAINER, _details.getNetworkId().getId());
    params.put(BfConsts.ARG_TESTRIG, _details.getSnapshotId().getId());
    params.put(BfConsts.ARG_SNAPSHOT_NAME, _workItem.getSnapshot());
    if (_details.getQuestionId() != null) {
      params.put(BfConsts.ARG_QUESTION_NAME, _details.getQuestionId().getId());
    }
    if (_details.getAnalysisId() != null) {
      params.put(BfConsts.ARG_ANALYSIS_NAME, _details.getAnalysisId().getId());
    }
    if (_details.getReferenceSnapshotId() != null) {
      params.put(BfConsts.ARG_DELTA_TESTRIG, _details.getReferenceSnapshotId().getId());
    }
    return ImmutableMap.copyOf(params);
  }
}
