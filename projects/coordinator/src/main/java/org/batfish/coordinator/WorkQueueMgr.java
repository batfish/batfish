package org.batfish.coordinator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.queues.AzureQueue;
import org.batfish.coordinator.queues.MemoryQueue;
import org.batfish.coordinator.queues.WorkQueue;
import org.batfish.datamodel.InitializationMetadata;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

// the design of this WorkQueueMgr is such that all synchronization sits here
// individual queues do not need to be synchronized

public class WorkQueueMgr {

  public enum QueueType {
    COMPLETED,
    INCOMPLETE
  }

  private Set<UUID> _blockingWork;

  private BatfishLogger _logger;

  private WorkQueue _queueCompletedWork;

  private WorkQueue _queueIncompleteWork;

  public WorkQueueMgr(BatfishLogger logger) {
    this(Main.getSettings().getQueueType(), logger);
  }

  public WorkQueueMgr(WorkQueue.Type wqType, BatfishLogger logger) {
    _blockingWork = new HashSet<>();
    _logger = logger;
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

  private void cleanUpInitMetaDataIfNeeded(String container, String testrig) throws IOException {
    InitializationMetadata metadata = WorkQueueMgr.getInitializationMetadata(container, testrig);
    if (metadata.getProcessingStatus() == ProcessingStatus.PARSING
        && getIncompleteWork(container, testrig, WorkType.PARSING) == null) {
      WorkQueueMgr.updateInitializationStatus(
          container, testrig, ProcessingStatus.PARSING_FAIL, null);
    } else if (metadata.getProcessingStatus() == ProcessingStatus.DATAPLANING
        && getIncompleteWork(container, testrig, WorkType.DATAPLANING) == null) {
      WorkQueueMgr.updateInitializationStatus(
          container, testrig, ProcessingStatus.DATAPLANING_FAIL, null);
    }
  }

  private QueuedWork generateAndQueueDataplaneWork(String container, String testrig)
      throws Exception {
    WorkItem newWItem = WorkItemBuilder.getWorkItemGenerateDataPlane(container, testrig);
    WorkDetails details = new WorkDetails(testrig, null, false, WorkType.DATAPLANING);
    QueuedWork newWork = new QueuedWork(newWItem, details);
    boolean queued = queueUnassignedWork(newWork);
    if (!queued) {
      throw new BatfishException("Failed to auto-queue dataplane work");
    }
    return newWork;
  }

  private QueuedWork getBlockerForDataplaningWork(QueuedWork work) throws IOException {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    QueuedWork currentParsingWork =
        getIncompleteWork(wItem.getContainerName(), wDetails.baseTestrig, WorkType.PARSING);

    InitializationMetadata metadata =
        WorkQueueMgr.getInitializationMetadata(wItem.getContainerName(), wDetails.baseTestrig);

    switch (metadata.getProcessingStatus()) {
      case UNINITIALIZED:
      case PARSING_FAIL:
      case PARSING:
        if (currentParsingWork == null) {
          throw new BatfishException(
              String.format(
                  "Cannot queue dataplane work for %s: Status is %s but no incomplete parsing work exists",
                  wDetails.baseTestrig, metadata.getProcessingStatus()));
        }
        return currentParsingWork;
      case PARSED:
        return currentParsingWork;
      case DATAPLANING:
        // we get here only when currentDataplaningWork is null; by virtue of the calling context
        throw new BatfishException(
            String.format(
                "Cannot queue dataplane work for %s: Status is %s but no incomplete dataplaning work exists",
                wDetails.baseTestrig, metadata.getProcessingStatus()));
      case DATAPLANED:
      case DATAPLANING_FAIL:
        return null;
      default:
        throw new BatfishException(
            "Unknown snapshot processingStatus: " + metadata.getProcessingStatus());
    }
  }

  /**
   * This function has a side effect It will inject a dataplane generation work in the queue if none
   * exists
   */
  private QueuedWork getBlockerForDataplaneDependentWork(QueuedWork work, String testrig)
      throws Exception {

    WorkItem wItem = work.getWorkItem();

    InitializationMetadata metadata =
        WorkQueueMgr.getInitializationMetadata(wItem.getContainerName(), testrig);

    QueuedWork parsingWork = getIncompleteWork(wItem.getContainerName(), testrig, WorkType.PARSING);

    QueuedWork dataplaningWork =
        getIncompleteWork(wItem.getContainerName(), testrig, WorkType.DATAPLANING);

    switch (metadata.getProcessingStatus()) {
      case UNINITIALIZED:
      case PARSING_FAIL:
      case PARSING:
        if (parsingWork == null) {
          throw new BatfishException(
              String.format(
                  "Cannot queue dataplane dependent work for %s: "
                      + "Status is %s but no incomplete parsing work exists",
                  testrig, metadata.getProcessingStatus()));
        }
        return parsingWork;
      case PARSED:
        if (parsingWork != null) {
          return parsingWork;
        }
        if (dataplaningWork != null) {
          return dataplaningWork;
        }
        return generateAndQueueDataplaneWork(wItem.getContainerName(), testrig);
      case DATAPLANING_FAIL:
      case DATAPLANING:
        if (dataplaningWork == null) {
          throw new BatfishException(
              String.format(
                  "Cannot queue dataplane dependent work for %s: "
                      + "Status is %s but no incomplete dataplaning work exists",
                  testrig, metadata.getProcessingStatus()));
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
      default:
        throw new BatfishException(
            "Unknown snapshot processingStatus: " + metadata.getProcessingStatus());
    }
  }

  private QueuedWork getBlockerForParsingDependentWork(QueuedWork work, String testrig)
      throws IOException {

    WorkItem wItem = work.getWorkItem();

    InitializationMetadata metadata =
        WorkQueueMgr.getInitializationMetadata(wItem.getContainerName(), testrig);

    QueuedWork parsingWork = getIncompleteWork(wItem.getContainerName(), testrig, WorkType.PARSING);

    switch (metadata.getProcessingStatus()) {
      case UNINITIALIZED:
      case PARSING:
      case PARSING_FAIL:
        if (parsingWork == null) {
          throw new BatfishException(
              String.format(
                  "Cannot queue parsing dependent work for %s: "
                      + "Status is %s but no incomplete parsing work exists",
                  testrig, metadata.getProcessingStatus()));
        }
        return parsingWork;
      case PARSED:
      case DATAPLANING:
      case DATAPLANED:
      case DATAPLANING_FAIL:
        return parsingWork;
      default:
        throw new BatfishException(
            "Unknown snapshot processingStatus: " + metadata.getProcessingStatus());
    }
  }

  /**
   * Get all completed work for the specified network and snapshot.
   *
   * @param network {@link NetworkId} to get completed work for.
   * @param snapshot {@link SnapshotId} to get completed work for.
   * @return {@link List} of completed {@link QueuedWork}.
   */
  public synchronized List<QueuedWork> getCompletedWork(NetworkId network, SnapshotId snapshot) {
    ImmutableList.Builder<QueuedWork> b = ImmutableList.builder();
    for (QueuedWork work : _queueCompletedWork) {
      if (work.getWorkItem().getContainerName().equals(network.getId())
          && work.getWorkItem().getTestrigName().equals(snapshot.getId())) {
        b.add(work);
      }
    }
    return b.build();
  }

  private synchronized QueuedWork getIncompleteWork(
      String container, String testrig, WorkType wType) {
    for (QueuedWork work : _queueIncompleteWork) {
      WorkDetails wDetails = work.getDetails();
      if (container.equals(work.getWorkItem().getContainerName())
          && ((testrig.equals(wDetails.baseTestrig))
              || (wDetails.isDifferential && testrig.equals(wDetails.deltaTestrig)))
          && (wType == null || wDetails.workType == wType)) {
        return work;
      }
    }
    return null;
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

  @Nonnull
  public List<QueuedWork> getWorkForChecking() {
    List<QueuedWork> workToCheck = new ArrayList<>();
    for (QueuedWork work : _queueIncompleteWork) {
      if (work.getStatus() == WorkStatusCode.ASSIGNED) {
        work.setStatus(WorkStatusCode.CHECKINGSTATUS);
        workToCheck.add(work);
      }
    }
    return workToCheck;
  }

  public synchronized List<QueuedWork> listIncompleteWork(
      String containerName, @Nullable String testrigName, @Nullable WorkType workType) {
    List<QueuedWork> retList = new LinkedList<>();
    for (QueuedWork work : _queueIncompleteWork) {
      // Add to queue if it matches container, testrig if provided, and work type if provided
      if (work.getWorkItem().getContainerName().equals(containerName)
          && (testrigName == null || work.getDetails().baseTestrig.equals(testrigName))
          && (workType == null || work.getDetails().workType == workType)) {
        retList.add(work);
      }
    }
    return retList;
  }

  public synchronized void makeWorkUnassigned(QueuedWork work) {
    work.setStatus(WorkStatusCode.UNASSIGNED);
  }

  // when assignment attempt ends in error, we do not try to reassign
  public synchronized void markAssignmentError(QueuedWork work) {
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
    WorkDetails wDetails = work.getDetails();
    if (wDetails.workType == WorkType.PARSING) {
      WorkQueueMgr.updateInitializationStatus(
          wItem.getContainerName(), wDetails.baseTestrig, ProcessingStatus.PARSING, null);
    } else if (wDetails.workType == WorkType.DATAPLANING) {
      WorkQueueMgr.updateInitializationStatus(
          wItem.getContainerName(), wDetails.baseTestrig, ProcessingStatus.DATAPLANING, null);
    }
  }

  public synchronized void processTaskCheckResult(QueuedWork work, Task task) throws Exception {

    // {Unscheduled, InProgress, TerminatedNormally, TerminatedAbnormally, TerminatedByUser
    // Unknown, UnreachableOrBadResponse}

    switch (task.getStatus()) {
      case Unscheduled:
      case InProgress:
        work.setStatus(WorkStatusCode.ASSIGNED);
        work.recordTaskCheckResult(task);
        break;
      case TerminatedAbnormally:
      case TerminatedByUser:
      case TerminatedNormally:
      case RequeueFailure:
        {
          // move the work to completed queue
          _queueIncompleteWork.delete(work);
          _queueCompletedWork.enque(work);
          work.setStatus(WorkStatusCode.fromTerminatedTaskStatus(task.getStatus()));
          work.recordTaskCheckResult(task);

          // update testrig metadata
          WorkItem wItem = work.getWorkItem();
          WorkDetails wDetails = work.getDetails();
          if (wDetails.workType == WorkType.PARSING) {
            ProcessingStatus status =
                (task.getStatus() == TaskStatus.TerminatedNormally)
                    ? ProcessingStatus.PARSED
                    : ProcessingStatus.PARSING_FAIL;
            WorkQueueMgr.updateInitializationStatus(
                wItem.getContainerName(), wDetails.baseTestrig, status, task.getErrMessage());
          } else if (wDetails.workType == WorkType.DATAPLANING) {
            // no change in status needed if task.getStatus() is RequeueFailure
            if (task.getStatus() == TaskStatus.TerminatedAbnormally
                || task.getStatus() == TaskStatus.TerminatedByUser) {
              WorkQueueMgr.updateInitializationStatus(
                  wItem.getContainerName(),
                  wDetails.baseTestrig,
                  ProcessingStatus.DATAPLANING_FAIL,
                  task.getErrMessage());
            } else if (task.getStatus() == TaskStatus.TerminatedNormally) {
              WorkQueueMgr.updateInitializationStatus(
                  wItem.getContainerName(),
                  wDetails.baseTestrig,
                  ProcessingStatus.DATAPLANED,
                  null);
            }
          }

          // check if we unblocked anything
          if (_blockingWork.contains(wItem.getId())) {
            _blockingWork.remove(wItem.getId());
            List<QueuedWork> requeueWorks = new LinkedList<>();
            for (QueuedWork incompleteWork : _queueIncompleteWork) {
              if (incompleteWork.getStatus() == WorkStatusCode.BLOCKED
                  && wDetails.isOverlappingInput(incompleteWork.getDetails())) {
                requeueWorks.add(incompleteWork);
              }
            }
            for (QueuedWork requeueWork : requeueWorks) {
              _queueIncompleteWork.delete(requeueWork);
              requeueWork.setStatus(WorkStatusCode.UNASSIGNED);
            }
            for (QueuedWork requeueWork : requeueWorks) {
              try {
                boolean queued = queueUnassignedWork(requeueWork);
                if (!queued) {
                  throw new BatfishException(
                      "Failed to requeue previously blocked work " + requeueWork.getId());
                }
              } catch (Exception e) {
                String stackTrace = Throwables.getStackTraceAsString(e);
                _logger.errorf("exception: %s\n", stackTrace);
                // put this work back on incomplete queue and process as if it terminatedabnormally
                // people may be checking its status and this work may be blocking others
                _queueIncompleteWork.enque(requeueWork);
                Task fakeTask =
                    new Task(
                        TaskStatus.RequeueFailure,
                        String.format("Couldn't requeue after unblocking.\n%s", e.getMessage()));
                processTaskCheckResult(requeueWork, fakeTask);
              }
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
            WorkDetails wDetails = work.getDetails();
            if (wDetails.workType == WorkType.PARSING
                || wDetails.workType == WorkType.DATAPLANING) {
              InitializationMetadata metadata =
                  WorkQueueMgr.getInitializationMetadata(
                      wItem.getContainerName(), wDetails.baseTestrig);
              if (wDetails.workType == WorkType.PARSING) {
                if (metadata.getProcessingStatus() != ProcessingStatus.PARSING) {
                  _logger.errorf(
                      "Unexpected status %s when parsing failed for %s",
                      metadata.getProcessingStatus(), wDetails.baseTestrig);
                } else {
                  WorkQueueMgr.updateInitializationStatus(
                      wItem.getContainerName(),
                      wDetails.baseTestrig,
                      ProcessingStatus.UNINITIALIZED,
                      task.getErrMessage());
                }
              } else { // wDetails.workType == WorkType.DATAPLANING
                if (metadata.getProcessingStatus() != ProcessingStatus.DATAPLANING) {
                  _logger.errorf(
                      "Unexpected status %s when dataplaning failed for %s",
                      metadata.getProcessingStatus(), wDetails.baseTestrig);
                } else {
                  WorkQueueMgr.updateInitializationStatus(
                      wItem.getContainerName(),
                      wDetails.baseTestrig,
                      ProcessingStatus.PARSED,
                      task.getErrMessage());
                }
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

  private boolean queueDependentAnsweringWork(QueuedWork work, boolean dataplaneDependent)
      throws Exception {
    WorkDetails wDetails = work.getDetails();

    QueuedWork baseBlocker =
        dataplaneDependent
            ? getBlockerForDataplaneDependentWork(work, wDetails.baseTestrig)
            : getBlockerForParsingDependentWork(work, wDetails.baseTestrig);

    if (baseBlocker != null) {
      return queueBlockedWork(work, baseBlocker);
    } else if (wDetails.isDifferential) {
      QueuedWork deltaBlocker =
          dataplaneDependent
              ? getBlockerForDataplaneDependentWork(work, wDetails.deltaTestrig)
              : getBlockerForParsingDependentWork(work, wDetails.deltaTestrig);
      if (deltaBlocker != null) {
        return queueBlockedWork(work, deltaBlocker);
      }
    }
    return _queueIncompleteWork.enque(work);
  }

  private boolean queueBlockedWork(QueuedWork work, QueuedWork blocker) {
    _blockingWork.add(blocker.getId());
    work.setStatus(WorkStatusCode.BLOCKED);
    return _queueIncompleteWork.enque(work);
  }

  private boolean queueDataplaningWork(QueuedWork work) throws Exception {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    QueuedWork currentDataplaningWork =
        getIncompleteWork(wItem.getContainerName(), wDetails.baseTestrig, WorkType.DATAPLANING);
    if (currentDataplaningWork != null) {
      throw new BatfishException("Dataplaning is already in queue/progress");
    }

    // see comment in queueParsingWork for justification
    QueuedWork ddWork =
        getIncompleteWork(
            wItem.getContainerName(), wDetails.baseTestrig, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
    if (ddWork != null) {
      throw new BatfishException("Cannot queue dataplaning work while other dependent work exists");
    }

    QueuedWork blocker = getBlockerForDataplaningWork(work);
    if (blocker == null) {
      return _queueIncompleteWork.enque(work);
    } else {
      return queueBlockedWork(work, blocker);
    }
  }

  private synchronized boolean queueParsingWork(QueuedWork work) throws Exception {

    WorkItem wItem = work.getWorkItem();
    WorkDetails wDetails = work.getDetails();

    // if incomplete work for this testrig exists, lets just reject this parsing work.
    // parsing work cannot proceeed in parallel because it may overwrite files used by others.
    // instead of rejecting, we could have queued it as BLOCKED but we risk cycles of BLOCKED work
    // this should not be a common case anyway, so we aren't losing much by rejecting it
    QueuedWork incompleteWork =
        getIncompleteWork(wItem.getContainerName(), wDetails.baseTestrig, null);
    if (incompleteWork != null) {
      throw new BatfishException("Cannot queue parsing work while other work is incomplete");
    } else {
      InitializationMetadata metadata =
          WorkQueueMgr.getInitializationMetadata(wItem.getContainerName(), wDetails.baseTestrig);
      if (metadata.getProcessingStatus() == ProcessingStatus.PARSING) {
        throw new BatfishException(
            String.format(
                "Cannot queue parsing work for %s: "
                    + "Status is PARSING but no incomplete parsing work exists",
                wDetails.baseTestrig));
      } else if (metadata.getProcessingStatus() == ProcessingStatus.DATAPLANING) {
        throw new BatfishException(
            String.format(
                "Cannot queue parsing work for %s: "
                    + "Status is DATAPLANING but no incomplete dataplaning work exists",
                wDetails.baseTestrig));
      }
    }

    return _queueIncompleteWork.enque(work);
  }

  public synchronized boolean queueUnassignedWork(QueuedWork work) throws Exception {
    QueuedWork previouslyQueuedWork = getWork(work.getId());
    if (previouslyQueuedWork != null) {
      throw new BatfishException("Duplicate work item");
    }
    WorkDetails wDetails = work.getDetails();
    cleanUpInitMetaDataIfNeeded(work.getWorkItem().getContainerName(), wDetails.baseTestrig);
    if (work.getDetails().isDifferential) {
      cleanUpInitMetaDataIfNeeded(work.getWorkItem().getContainerName(), wDetails.deltaTestrig);
    }
    switch (work.getDetails().workType) {
      case PARSING:
        return queueParsingWork(work);
      case DATAPLANING:
        return queueDataplaningWork(work);
      case INDEPENDENT_ANSWERING:
        // assume that this type of work shouldn't be blocked at all
        return _queueIncompleteWork.enque(work);
      case PARSING_DEPENDENT_ANSWERING:
        return queueDependentAnsweringWork(work, false);
      case DATAPLANE_DEPENDENT_ANSWERING:
        return queueDependentAnsweringWork(work, true);
      case UNKNOWN:
        return _queueIncompleteWork.enque(work);
      default:
        throw new BatfishException("Unknown WorkType " + work.getDetails().workType);
    }
  }

  private static void updateInitializationStatus(
      String network, String snapshot, ProcessingStatus status, String errMessage)
      throws IOException {
    // already resolved
    SnapshotMetadataMgr.updateInitializationStatus(
        new NetworkId(network), new SnapshotId(snapshot), status, errMessage);
  }

  @VisibleForTesting
  static InitializationMetadata getInitializationMetadata(String network, String snapshot)
      throws IOException {
    // already resolved
    return SnapshotMetadataMgr.getInitializationMetadata(
        new NetworkId(network), new SnapshotId(snapshot));
  }
}
