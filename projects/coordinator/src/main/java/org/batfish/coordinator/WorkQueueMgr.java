package org.batfish.coordinator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Pair;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.coordinator.queues.AzureQueue;
import org.batfish.coordinator.queues.MemoryQueue;
import org.batfish.coordinator.queues.WorkQueue;
import org.batfish.datamodel.EnvironmentMetadata;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.batfish.datamodel.TestrigMetadata;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

// the design of this WorkQueueMgr is such that all synchronization sits here
// individual queues do not need to be synchronized

public class WorkQueueMgr {

  public enum QueueType {
    COMPLETED,
    INCOMPLETE
  }

  private WorkQueue _queueCompletedWork;

  private WorkQueue _queueIncompleteWork;

  public WorkQueueMgr() {
    this(Main.getSettings().getQueueType());
  }

  public WorkQueueMgr(WorkQueue.Type wqType) {
    switch (wqType) {
      case azure:
        String storageConnectionString =
            String.format(
                "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s",
                Main.getSettings().getStorageProtocol(),
                Main.getSettings().getStorageAccountName(),
                Main.getSettings().getStorageAccountKey());
        _queueCompletedWork =
            new AzureQueue(Main.getSettings().getQueueCompletedWork(), storageConnectionString);
        _queueIncompleteWork =
            new AzureQueue(Main.getSettings().getQueueIncompleteWork(), storageConnectionString);
        break;
      case memory:
        _queueCompletedWork = new MemoryQueue();
        _queueIncompleteWork = new MemoryQueue();
        break;
      default:
        throw new BatfishException("Unsupported queue type: " + wqType);
    }
  }

  public synchronized long getLength(QueueType qType) {
    switch (qType) {
      case COMPLETED:
        return _queueCompletedWork.getLength();
      case INCOMPLETE:
        return _queueIncompleteWork.getLength();
      default:
        return -1;
    }
  }

  public synchronized JSONObject getStatusJson() throws JSONException {

    JSONObject jObject = new JSONObject();

    jObject.put("incomplete-works", _queueIncompleteWork.getLength());
    for (QueuedWork work : _queueIncompleteWork) {
      jObject.put(work.getId().toString(), work.toString());
    }

    jObject.put("completed-works", _queueCompletedWork.getLength());
    for (QueuedWork work : _queueCompletedWork) {
      jObject.put(work.getId().toString(), work.toString());
    }

    return jObject;
  }

  public synchronized QueuedWork getMatchingWork(WorkItem workItem, QueueType qType) {
    switch (qType) {
      case COMPLETED:
        return getMatchingWork(workItem, _queueCompletedWork);
      case INCOMPLETE:
        return getMatchingWork(workItem, _queueIncompleteWork);
      default:
        throw new BatfishException("Unknown QueueType " + qType);
    }
  }

  private synchronized QueuedWork getMatchingWork(WorkItem workItem, WorkQueue queue) {
    for (QueuedWork work : queue) {
      if (work.getWorkItem().matches(workItem)) {
        return work;
      }
    }
    return null;
  }

  public synchronized QueuedWork getWork(UUID workId) {
    QueuedWork work = getWork(workId, QueueType.INCOMPLETE);
    if (work == null) {
      work = getWork(workId, QueueType.COMPLETED);
    }
    return work;
  }

  @Nullable
  private synchronized QueuedWork getWork(UUID workId, QueueType qType) {
    switch (qType) {
      case COMPLETED:
        return _queueCompletedWork.getWork(workId);
      case INCOMPLETE:
        return _queueIncompleteWork.getWork(workId);
      default:
        return null;
    }
  }

  @Nullable
  public synchronized QueuedWork getWorkForAssignment() {

    for (QueuedWork work : _queueIncompleteWork) {
      if (work.getStatus() == WorkStatusCode.UNASSIGNED) {
        work.setStatus(WorkStatusCode.TRYINGTOASSIGN);
        return work;
      }
    }

    return null;
  }

  @Nullable
  public QueuedWork getWorkForChecking() {
    for (QueuedWork work : _queueIncompleteWork) {
      if (work.getStatus() == WorkStatusCode.ASSIGNED) {
        work.setStatus(WorkStatusCode.CHECKINGSTATUS);
        return work;
      }
    }
    return null;
  }

  public synchronized void makeWorkUnassigned(QueuedWork work) {
    work.setStatus(WorkStatusCode.UNASSIGNED);
  }

  // when assignment attempt ends in error, we do not try to reassign
  public synchronized void markAssignmentError(QueuedWork work) throws Exception {
    // move the work to completed queue
    _queueIncompleteWork.delete(work);
    _queueCompletedWork.enque(work);
    work.setStatus(WorkStatusCode.ASSIGNMENTERROR);
  }

  public synchronized void markAssignmentFailure(QueuedWork work) {
    work.setStatus(WorkStatusCode.UNASSIGNED);
  }

  public synchronized void markAssignmentSuccess(QueuedWork work, String assignedWorker)
      throws IOException {
    work.setAssignment(assignedWorker);
    // update testrig metadata
    WorkItem wItem = work.getWorkItem();
    if (WorkItemBuilder.isParsingWorkItem(wItem) || WorkItemBuilder.isDataplaningWorkItem(wItem)) {
      Pair<Pair<String, String>, Pair<String, String>> settings =
          WorkItemBuilder.getBaseAndDeltaSettings(wItem);
      String baseTestrig = WorkItemBuilder.getBaseTestrig(settings);
      String baseEnv = WorkItemBuilder.getBaseEnvironment(settings);
      TestrigMetadata trMetadata =
          TestrigMetadataMgr.readMetadata(wItem.getContainerName(), baseTestrig);
      EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(baseEnv);
      if (WorkItemBuilder.isParsingWorkItem(wItem)) {
        ProcessingStatus status = ProcessingStatus.PARSING;
        envMetadata.updateStatus(status);
        TestrigMetadataMgr.writeMetadata(trMetadata, wItem.getContainerName(), baseTestrig);
      }
      if (WorkItemBuilder.isDataplaningWorkItem(wItem)) {
        ProcessingStatus status = ProcessingStatus.DATAPLANING;
        envMetadata.updateStatus(status);
        TestrigMetadataMgr.writeMetadata(trMetadata, wItem.getContainerName(), baseTestrig);
      }
    }
  }

  public synchronized void processTaskCheckResult(QueuedWork work, Task task) throws Exception {

    // {Unscheduled, InProgress, TerminatedNormally, TerminatedAbnormally,
    // Unknown, UnreachableOrBadResponse}

    switch (task.getStatus()) {
      case Unscheduled:
      case InProgress:
        work.setStatus(WorkStatusCode.ASSIGNED);
        work.recordTaskCheckResult(task);
        break;
      case TerminatedNormally:
      case TerminatedAbnormally:
        // move the work to completed queue
        _queueIncompleteWork.delete(work);
        _queueCompletedWork.enque(work);
        work.setStatus(
            (task.getStatus() == TaskStatus.TerminatedNormally)
                ? WorkStatusCode.TERMINATEDNORMALLY
                : WorkStatusCode.TERMINATEDABNORMALLY);
        work.recordTaskCheckResult(task);
        WorkItem wItem = work.getWorkItem();
        // update testrig metadata
        if (WorkItemBuilder.isParsingWorkItem(wItem)
            || WorkItemBuilder.isDataplaningWorkItem(wItem)) {
          Pair<Pair<String, String>, Pair<String, String>> settings =
              WorkItemBuilder.getBaseAndDeltaSettings(wItem);
          String baseTestrig = WorkItemBuilder.getBaseTestrig(settings);
          String baseEnv = WorkItemBuilder.getBaseEnvironment(settings);
          TestrigMetadata trMetadata =
              TestrigMetadataMgr.readMetadata(wItem.getContainerName(), baseTestrig);
          EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(baseEnv);
          if (WorkItemBuilder.isParsingWorkItem(wItem)) {
            ProcessingStatus status =
                (task.getStatus() == TaskStatus.TerminatedNormally)
                    ? ProcessingStatus.PARSED
                    : ProcessingStatus.PARSING_FAIL;
            envMetadata.updateStatus(status);
            TestrigMetadataMgr.writeMetadata(trMetadata, wItem.getContainerName(), baseTestrig);
          }
          if (WorkItemBuilder.isDataplaningWorkItem(wItem)) {
            ProcessingStatus status =
                (task.getStatus() == TaskStatus.TerminatedNormally)
                    ? ProcessingStatus.DATAPLANED
                    : ProcessingStatus.DATAPLANING_FAIL;
            envMetadata.updateStatus(status);
            TestrigMetadataMgr.writeMetadata(trMetadata, wItem.getContainerName(), baseTestrig);
          }
        }
        break;
      case Unknown:
        // we mark this unassigned, so we try to schedule it again
        work.setStatus(WorkStatusCode.UNASSIGNED);
        work.clearAssignment();
        break;
      case UnreachableOrBadResponse:
        if (work.getLastTaskCheckResult().getStatus() == TaskStatus.UnreachableOrBadResponse) {
          // if we saw the same thing last time around, free the task to be
          // scheduled elsewhere
          work.setStatus(WorkStatusCode.UNASSIGNED);
          work.clearAssignment();
        } else {
          work.setStatus(WorkStatusCode.ASSIGNED);
          work.recordTaskCheckResult(task);
        }
        break;
      default:
        throw new BatfishException(
            "Unhandled " + TaskStatus.class.getCanonicalName() + ": " + task.getStatus());
    }
  }

  public synchronized boolean queueUnassignedWork(QueuedWork work) throws Exception {
    QueuedWork previouslyQueuedWork = getWork(work.getId());
    if (previouslyQueuedWork != null) {
      throw new BatfishException("Duplicate work item");
    }
    WorkItem wItem = work.getWorkItem();
    Pair<Pair<String, String>, Pair<String, String>> settings =
        WorkItemBuilder.getBaseAndDeltaSettings(wItem);
    String baseTestrig = WorkItemBuilder.getBaseTestrig(settings);
    String baseEnv = WorkItemBuilder.getBaseEnvironment(settings);
    String deltaTestrig = WorkItemBuilder.getDeltaTestrig(settings);
    String deltaEnv = WorkItemBuilder.getDeltaEnvironment(settings);
    TestrigMetadata trMetadata =
        TestrigMetadataMgr.readMetadata(wItem.getContainerName(), baseTestrig);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(baseEnv);

    if (WorkItemBuilder.isParsingWorkItem(wItem)) {
      return queueParsingWork(work, wItem.getContainerName(), baseTestrig, baseEnv);
    } else if (WorkItemBuilder.isDataplaningWorkItem(wItem)) {
      return queueDataplaningWork(work, baseTestrig, baseEnv, envMetadata);
    } else if (WorkItemBuilder.isNonDataplaneDependentWork()) {
      if (WorkItemBuilder.isDifferentialWork()) {

      } else {
        return queueNonDataplaneDependentWork(work, baseTestrig, baseEnv, envMetadata);
      }
    } else if (WorkItemBuilder.isDataplaneDependentWork()) {
      return queueDataplaneDependentWork(work, baseTestrig, baseEnv, envMetadata);
    } else {
      return _queueIncompleteWork.enque(work);
    }
  }

  private boolean queueDataplaningWork(
      QueuedWork work,
      String container,
      String testrig,
      String environment,
      EnvironmentMetadata metadata)
      throws Exception {

    QueuedWork currentDataplaningWork =
        getIncompleteDataplaningWork(container, testrig, environment);
    if (currentDataplaningWork != null) {
      throw new BatfishException("Dataplaning is already in queue/progress");
    }

    // see comment in queueParsingWork for justification
    List<QueuedWork> workList =
        getIncompleteDataplaneDependentWorks(container, testrig, environment);
    if (workList.size() != 0) {
      throw new BatfishException("Cannot queue dataplaning work while other dependent work exists");
    }

    QueuedWork currentParsingWork = getIncompleteParsingWork(container, testrig, environment);

    switch (metadata.getProcessingStatus()) {
      case UNINITIALIZED:
        if (currentParsingWork == null) {
          throw new BatfishException("Cannot compute dataplane without parsing");
        }
        work.setStatus(WorkStatusCode.BLOCKED);
        return _queueIncompleteWork.enque(work);
      case PARSING:
        if (currentParsingWork == null) {
          throw new BatfishException("Testrig status is parsing but no parsing work found");
        }
        work.setStatus(WorkStatusCode.BLOCKED);
        return _queueIncompleteWork.enque(work);
      case PARSED:
        if (currentParsingWork == null) {
          return _queueIncompleteWork.enque(work);
        } else {
          work.setStatus(WorkStatusCode.BLOCKED);
          return _queueIncompleteWork.enque(work);
        }
      case PARSING_FAIL:
        if (currentParsingWork == null) {
          throw new BatfishException("Testrig is PARSING_FAIL and no other parsing work found");
        }
        work.setStatus(WorkStatusCode.BLOCKED);
        return _queueIncompleteWork.enque(work);
      case DATAPLANING:
        // we are here because currentDataplaningWork is null
        throw new BatfishException("Testrig status is DATAPLANING but no dataplane work found");
      case DATAPLANED:
      case DATAPLANING_FAIL:
        return _queueIncompleteWork.enque(work);
      default:
        throw new BatfishException(
            "Unknown environment processingStatus: " + metadata.getProcessingStatus());
    }
  }

  private synchronized boolean queueParsingWork(
      QueuedWork work, String container, String testrig, String environment) throws Exception {

    // if incomplete work for this testrig exists, lets just reject this parsing work
    // parsing work cannot proceeed in parallel because it may overwrite files used by others
    // instead of rejecting, we could have queued it as BLOCKED but we risk cycles of BLOCKED work
    // this should not be a common case anyway, so we aren't losing much by rejecting it
    List<QueuedWork> workList = getIncompleteWorks(container, testrig, environment);
    if (workList.size() != 0) {
      throw new BatfishException("Cannot queue parsing work while other work is incomplete");
    }

    return _queueIncompleteWork.enque(work);
  }

  private synchronized QueuedWork getIncompleteDataplaningWork(
      String container, String testrig, String environment) {
    for (QueuedWork work : _queueIncompleteWork) {
      WorkItem wItem = work.getWorkItem();
      if (WorkItemBuilder.isDataplaningWorkItem(wItem, container, testrig, environment)) {
        return work;
      }
    }
    return null;
  }

  private synchronized QueuedWork getIncompleteParsingWork(
      String container, String testrig, String environment) {
    for (QueuedWork work : _queueIncompleteWork) {
      WorkItem wItem = work.getWorkItem();
      if (WorkItemBuilder.isParsingWorkItem(wItem, container, testrig, environment)) {
        return work;
      }
    }
    return null;
  }

  private synchronized List<QueuedWork> getIncompleteDataplaneDependentWorks(
      String container, String testrig, String environment) {
    List<QueuedWork> retList = new LinkedList<>();
    for (QueuedWork work : _queueIncompleteWork) {
      WorkItem wItem = work.getWorkItem();
      Pair<Pair<String, String>, Pair<String, String>> settings =
          WorkItemBuilder.getBaseAndDeltaSettings(wItem);
      String baseTestrig = WorkItemBuilder.getBaseTestrig(settings);
      String baseEnv = WorkItemBuilder.getBaseEnvironment(settings);
      String deltaTestrig = WorkItemBuilder.getDeltaTestrig(settings);
      String deltaEnv = WorkItemBuilder.getDeltaEnvironment(settings);
      if (container.equals(wItem.getContainerName())
          && work.isDataplaneDependentWork()
          && ((testrig.equals(baseTestrig) && environment.equals(baseEnv))
              || (testrig.equals(deltaTestrig) && environment.equals(deltaEnv)))) {
        retList.add(work);
      }
    }
    return retList;
  }

  private synchronized List<QueuedWork> getIncompleteWorks(
      String container, String testrig, String environment) {
    List<QueuedWork> retList = new LinkedList<>();
    for (QueuedWork work : _queueIncompleteWork) {
      WorkItem wItem = work.getWorkItem();
      Pair<Pair<String, String>, Pair<String, String>> settings =
          WorkItemBuilder.getBaseAndDeltaSettings(wItem);
      String baseTestrig = WorkItemBuilder.getBaseTestrig(settings);
      String baseEnv = WorkItemBuilder.getBaseEnvironment(settings);
      String deltaTestrig = WorkItemBuilder.getDeltaTestrig(settings);
      String deltaEnv = WorkItemBuilder.getDeltaEnvironment(settings);
      if (container.equals(wItem.getContainerName())
          && ((testrig.equals(baseTestrig) && environment.equals(baseEnv))
              || (testrig.equals(deltaTestrig) && environment.equals(deltaEnv)))) {
        retList.add(work);
      }
    }
    return retList;
  }
}
