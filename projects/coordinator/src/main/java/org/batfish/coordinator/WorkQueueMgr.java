package org.batfish.coordinator;

import java.io.IOException;
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
    return _queueIncompleteWork.enque(work);

    //  prep for multi-worker support
    //    WorkItem wItem = work.getWorkItem();
    //    Tuple4<String, String, String, String> settings =
    //        WorkItemBuilder.getBaseAndDeltaSettings(wItem);
    //    String baseTestrig = settings._1();
    //    String baseEnv = settings._2();
    //    String deltaTestrig = settings._3();
    //    String deltaEnv = settings._4();
    //    TestrigMetadata trMetadata =
    //        TestrigMetadataMgr.readMetadata(wItem.getContainerName(), baseTestrig);
    //    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(baseEnv);
    //
    //    if (WorkItemBuilder.isParsingWorkItem(wItem)) {
    //      return queueParsingWork(work, baseTestrig, baseEnv, envMetadata);
    //    } else if (WorkItemBuilder.isDataplaningWorkItem(wItem)) {
    //      return queueDataplaningWork(work, baseTestrig, baseEnv, envMetadata);
    //    } else if (WorkItemBuilder.isNonDataplaneDependentWork()) {
    //      if (WorkItemBuilder.isDifferentialWork()) {
    //
    //      } else {
    //        return queueNonDataplaneDependentWork(work, baseTestrig, baseEnv, envMetadata);
    //      }
    //    } else if (WorkItemBuilder.isDataplaneDependentWork()) {
    //      return queueDataplaneDependentWork(work, baseTestrig, baseEnv, envMetadata);
    //    } else {
    //      return _queueIncompleteWork.enque(work);
    //    }
  }

  //  prep for multi-worker support
  //  private boolean queueDataplaningWork(
  //      QueuedWork work, String baseTestrig, String baseEnv, EnvironmentMetadata metadata) {
  //    switch (metadata.getProcessingStatus()) {
  //      case UNINITIALIZED:
  //        // TODO: is there parsing work?
  //      case PARSING:
  //        // TODO: find the parsing work
  //        work.setStatus(WorkStatusCode.BLOCKED);
  //        return _queueIncompleteWork.enque(work);
  //      case PARSED:
  //        return _queueIncompleteWork.enque(work);
  //      case PARSING_FAIL:
  //        // TODO: what to do here?
  //      case DATAPLANING:
  //        // do not requeue?
  //
  //      case DATAPLANED:
  //        // queue
  //
  //      case DATAPLANING_FAIL:
  //        // queue
  //
  //      default:
  //        throw new BatfishException(
  //            "Unknown environment processingStatus: " + metadata.getProcessingStatus());
  //    }
  //  }
  //
  //  private synchronized boolean queueParsingWork(
  //      QueuedWork work, String baseTestrig, String baseEnv, EnvironmentMetadata metadata) {
  //
  //    switch (metadata.getProcessingStatus()) {
  //      case UNINITIALIZED:
  //        // is there another parsing work?
  //        return _queueIncompleteWork.enque(work);
  //      case PARSING:
  //
  //      case PARSED:
  //
  //      case PARSING_FAIL:
  //
  //      case DATAPLANING:
  //
  //      case DATAPLANED:
  //
  //      case DATAPLANING_FAIL:
  //
  //      default:
  //        throw new BatfishException(
  //            "Unknown environment processingStatus: " + metadata.getProcessingStatus());
  //    }
  //  }
}
