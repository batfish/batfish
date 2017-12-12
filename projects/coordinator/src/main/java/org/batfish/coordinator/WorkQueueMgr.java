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
import org.batfish.coordinator.WorkDetails.WorkType;
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

  private QueuedWork getBlockerForDataplaningWork(QueuedWork work) throws IOException {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    QueuedWork currentParsingWork =
        getIncompleteParsingWork(
            wItem.getContainerName(), wDetails.baseTestrig, wDetails.baseEnvironment);

    TestrigMetadata trMetadata =
        TestrigMetadataMgr.readMetadata(wItem.getContainerName(), wDetails.baseTestrig);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(wDetails.baseEnvironment);

    switch (envMetadata.getProcessingStatus()) {
    case UNINITIALIZED:
      if (currentParsingWork == null) {
        throw new BatfishException("Cannot compute dataplane without parsing");
      }
      return currentParsingWork;
    case PARSING:
      if (currentParsingWork == null) {
        throw new BatfishException("Testrig is PARSING but no parsing work found");
      }
      return currentParsingWork;
    case PARSED:
      return currentParsingWork;
    case PARSING_FAIL:
      if (currentParsingWork == null) {
        throw new BatfishException("Testrig is PARSING_FAIL and no other parsing work found");
      }
      return currentParsingWork;
    case DATAPLANING:
      // we get here only when currentDataplaningWork is null; by virtue of the calling context
      throw new BatfishException("Testrig status is DATAPLANING but no dataplane work found");
    case DATAPLANED:
    case DATAPLANING_FAIL:
      return null;
    default:
      throw new BatfishException(
          "Unknown environment processingStatus: " + envMetadata.getProcessingStatus());
    }
  }

  private QueuedWork getBlockerForDataplaneDependentWork(
      QueuedWork work, String testrig, String environment) throws Exception {

    WorkItem wItem = work.getWorkItem();

    TestrigMetadata trMetadata = TestrigMetadataMgr.readMetadata(wItem.getContainerName(), testrig);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(environment);

    QueuedWork parsingWork =
        getIncompleteParsingWork(wItem.getContainerName(), testrig, environment);

    QueuedWork dataplaningWork =
        getIncompleteDataplaningWork(wItem.getContainerName(), testrig, environment);

    switch (envMetadata.getProcessingStatus()) {
    case UNINITIALIZED:
      if (parsingWork == null) {
        throw new BatfishException("Cannot answer/analyze without parsing " + testrig);
      }
      return parsingWork;
    case PARSING:
      if (parsingWork == null) {
        throw new BatfishException("Status is PARSING but no parsing work found for " + testrig);
      }
      return parsingWork;
    case PARSED:
      if (parsingWork != null) {
        return parsingWork;
      }
      if (dataplaningWork != null) {
        return dataplaningWork;
      }
      // generate dataplane work
      WorkItem newWItem =
          WorkItemBuilder.getWorkItemGenerateDataPlane(
              wItem.getContainerName(), testrig, environment);
      WorkDetails details = new WorkDetails();
      details.baseTestrig = testrig;
      details.baseEnvironment = environment;
      details.isDifferential = false;
      details.isDataplaneDependent = false;
      details.workType = WorkType.DATAPLANING;
      QueuedWork newWork = new QueuedWork(newWItem, details);
      boolean queued = queueUnassignedWork(newWork);
      if (!queued) {
        throw new BatfishException("Failed to queue new dataplane work");
      }
      return newWork;
    case PARSING_FAIL:
      if (parsingWork == null) {
        throw new BatfishException(
            "Status is PARSING_FAIL and no parsing work found for " + testrig);
      }
      return parsingWork;
    case DATAPLANING:
      if (dataplaningWork == null) {
        throw new BatfishException("Status is DATAPLANING but no such work found for " + testrig);
      }
      return dataplaningWork;
    case DATAPLANED:
      if (parsingWork != null) {
        return parsingWork;
      }
      if (dataplaningWork != null) {
        return dataplaningWork;
      }
      return null;
    case DATAPLANING_FAIL:
      if (dataplaningWork == null) {
        throw new BatfishException(
            "Status is DATAPLAINING_FAIL. No other Dataplaning work. Rejecting more dataplaneDependent work");
      }
      return dataplaningWork;
    default:
      throw new BatfishException(
          "Unknown environment processingStatus: " + envMetadata.getProcessingStatus());
    }
  }

  private QueuedWork getBlockerForDataplaneIndependentWork(
      QueuedWork work, String testrig, String environment) throws IOException {

    WorkItem wItem = work.getWorkItem();

    TestrigMetadata trMetadata = TestrigMetadataMgr.readMetadata(wItem.getContainerName(), testrig);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(environment);

    QueuedWork parsingWork =
        getIncompleteParsingWork(wItem.getContainerName(), testrig, environment);

    switch (envMetadata.getProcessingStatus()) {
    case UNINITIALIZED:
      if (parsingWork == null) {
        throw new BatfishException("Cannot answer/analyze without parsing " + testrig);
      }
      return parsingWork;
    case PARSING:
      if (parsingWork == null) {
        throw new BatfishException("Status is PARSING but no parsing work found for " + testrig);
      }
      return parsingWork;
    case PARSED:
      if (parsingWork == null) {
        return null;
      } else {
        return parsingWork;
      }
    case PARSING_FAIL:
      if (parsingWork == null) {
        throw new BatfishException(
            "Status is PARSING_FAIL and no other parsing work found for " + testrig);
      }
      return parsingWork;
    case DATAPLANING:
    case DATAPLANED:
    case DATAPLANING_FAIL:
      return null;
    default:
      throw new BatfishException(
          "Unknown environment processingStatus: " + envMetadata.getProcessingStatus());
    }
  }

  private synchronized QueuedWork getIncompleteDataplaningWork(
      String container, String testrig, String environment) {
    for (QueuedWork work : _queueIncompleteWork) {
      WorkDetails wDetails = work.getDetails();
      if (container.equals(work.getWorkItem().getContainerName())
          && testrig.equals(wDetails.baseTestrig)
          && environment.equals(wDetails.baseEnvironment)
          && wDetails.workType == WorkType.DATAPLANING) {
        return work;
      }
    }
    return null;
  }

  private synchronized QueuedWork getIncompleteParsingWork(
      String container, String testrig, String environment) {
    for (QueuedWork work : _queueIncompleteWork) {
      WorkDetails wDetails = work.getDetails();
      if (container.equals(work.getWorkItem().getContainerName())
          && testrig.equals(wDetails.baseTestrig)
          && environment.equals(wDetails.baseEnvironment)
          && wDetails.workType == WorkType.PARSING) {
        return work;
      }
    }
    return null;
  }

  private synchronized List<QueuedWork> getIncompleteDataplaneDependentWorks(
      String container, String testrig, String environment) {
    List<QueuedWork> retList = new LinkedList<>();
    for (QueuedWork work : _queueIncompleteWork) {
      WorkDetails wDetails = work.getDetails();
      if (container.equals(work.getWorkItem().getContainerName())
          && work.getDetails().isDataplaneDependent
          && ((testrig.equals(wDetails.baseTestrig) && environment.equals(wDetails.baseEnvironment))
          || (wDetails.isDifferential
          && testrig.equals(wDetails.deltaTestrig)
          && environment.equals(wDetails.deltaEnvironment)))) {
        retList.add(work);
      }
    }
    return retList;
  }

  private synchronized List<QueuedWork> getIncompleteWorks(
      String container, String testrig, String environment) {
    List<QueuedWork> retList = new LinkedList<>();
    for (QueuedWork work : _queueIncompleteWork) {
      WorkDetails wDetails = work.getDetails();
      if (container.equals(work.getWorkItem().getContainerName())
          && ((testrig.equals(wDetails.baseTestrig) && environment.equals(wDetails.baseEnvironment))
          || (wDetails.isDifferential
          && testrig.equals(wDetails.deltaTestrig)
          && environment.equals(wDetails.deltaEnvironment)))) {
        retList.add(work);
      }
    }
    return retList;
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
        {
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
        }
        break;
      case Unknown:
        // we mark this unassigned, so we try to schedule it again
        work.setStatus(WorkStatusCode.UNASSIGNED);
        work.clearAssignment();
        break;
      case UnreachableOrBadResponse:
        {
          if (work.getLastTaskCheckResult().getStatus() == TaskStatus.UnreachableOrBadResponse) {
            // if we saw the same thing last time around, free the task to be scheduled elsewhere
            work.setStatus(WorkStatusCode.UNASSIGNED);
            work.clearAssignment();
            work.recordTaskCheckResult(task);
            // update testrig metadata
            WorkItem wItem = work.getWorkItem();
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
                if (envMetadata.getProcessingStatus() != ProcessingStatus.PARSING) {
                  throw new BatfishException(
                      "Unexpected status when parsing work failed: "
                          + envMetadata.getProcessingStatus());
                }
                ProcessingStatus status = ProcessingStatus.UNINITIALIZED;
                envMetadata.updateStatus(status);
                TestrigMetadataMgr.writeMetadata(trMetadata, wItem.getContainerName(), baseTestrig);
              }
              if (WorkItemBuilder.isDataplaningWorkItem(wItem)) {
                if (envMetadata.getProcessingStatus() != ProcessingStatus.DATAPLANING) {
                  throw new BatfishException(
                      "Unexpected status when dataplaning work failed: "
                          + envMetadata.getProcessingStatus());
                }
                ProcessingStatus status = ProcessingStatus.PARSED;
                envMetadata.updateStatus(status);
                TestrigMetadataMgr.writeMetadata(trMetadata, wItem.getContainerName(), baseTestrig);
              }
            }
          } else {
            work.setStatus(WorkStatusCode.ASSIGNED);
            work.recordTaskCheckResult(task);
          }
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

    WorkDetails wDetails = work.getDetails();

    switch (wDetails.workType) {
      case PARSING:
        return queueParsingWork(work);
      case DATAPLANING:
        return queueDataplaningWork(work);
      case ANSWERING:
        if (wDetails.isDataplaneDependent) {
          return queueDataplaneDependentWork(work);
        } else {
          return queueDataplaneIndependentWork(work);
        }
      case UNKNOWN:
        return _queueIncompleteWork.enque(work);
      default:
        throw new BatfishException("Unknown WorkType " + wDetails.workType);
    }
  }

  private boolean queueDataplaneDependentWork(QueuedWork work) throws Exception {
    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    QueuedWork baseBlocker =
        getBlockerForDataplaneDependentWork(work, wDetails.baseTestrig, wDetails.baseEnvironment);
    if (baseBlocker != null) {
      work.setStatus(WorkStatusCode.BLOCKED);
      return _queueIncompleteWork.enque(work);
    } else if (wDetails.isDifferential) {
      QueuedWork deltaBlocker =
          getBlockerForDataplaneDependentWork(work, wDetails.deltaTestrig, wDetails.deltaEnvironment);
      if (deltaBlocker != null) {
        work.setStatus(WorkStatusCode.BLOCKED);
        return _queueIncompleteWork.enque(work);
      } else {
        return _queueIncompleteWork.enque(work);
      }
    } else {
      // neither baseBlocked nor differential
      return _queueIncompleteWork.enque(work);
    }
  }

  private boolean queueDataplaneIndependentWork(QueuedWork work) throws Exception {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    QueuedWork baseBlocker =
        getBlockerForDataplaneIndependentWork(work, wDetails.baseTestrig, wDetails.baseEnvironment);
    if (baseBlocker != null) {
      work.setStatus(WorkStatusCode.BLOCKED);
      return _queueIncompleteWork.enque(work);
    } else if (wDetails.isDifferential) {
      QueuedWork deltaBlocker =
          getBlockerForDataplaneIndependentWork(
              work, wDetails.deltaTestrig, wDetails.deltaEnvironment);
      if (deltaBlocker != null) {
        work.setStatus(WorkStatusCode.BLOCKED);
        return _queueIncompleteWork.enque(work);
      } else {
        return _queueIncompleteWork.enque(work);
      }
    } else {
      // neither baseBlocked nor differential
      return _queueIncompleteWork.enque(work);
    }
  }

  private boolean queueDataplaningWork(QueuedWork work) throws Exception {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    QueuedWork currentDataplaningWork =
        getIncompleteDataplaningWork(
            wItem.getContainerName(), wDetails.baseTestrig, wDetails.baseEnvironment);
    if (currentDataplaningWork != null) {
      throw new BatfishException("Dataplaning is already in queue/progress");
    }

    // see comment in queueParsingWork for justification
    List<QueuedWork> workList =
        getIncompleteDataplaneDependentWorks(
            wItem.getContainerName(), wDetails.baseTestrig, wDetails.baseEnvironment);
    if (workList.size() != 0) {
      throw new BatfishException("Cannot queue dataplaning work while other dependent work exists");
    }

    QueuedWork blocker = getBlockerForDataplaningWork(work);
    if (blocker == null) {
      return _queueIncompleteWork.enque(work);
    } else {
      work.setStatus(WorkStatusCode.BLOCKED);
      return _queueIncompleteWork.enque(work);
    }
  }

  private synchronized boolean queueParsingWork(QueuedWork work) throws Exception {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    // if incomplete work for this testrig exists, lets just reject this parsing work
    // parsing work cannot proceeed in parallel because it may overwrite files used by others
    // instead of rejecting, we could have queued it as BLOCKED but we risk cycles of BLOCKED work
    // this should not be a common case anyway, so we aren't losing much by rejecting it
    List<QueuedWork> workList =
        getIncompleteWorks(
            wItem.getContainerName(), wDetails.baseTestrig, wDetails.baseEnvironment);
    if (workList.size() != 0) {
      throw new BatfishException("Cannot queue parsing work while other work is incomplete");
    }

    return _queueIncompleteWork.enque(work);
  }
}
