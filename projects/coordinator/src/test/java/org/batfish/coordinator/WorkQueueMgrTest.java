package org.batfish.coordinator;

import static org.batfish.coordinator.matchers.WorkQueueMatchers.hasWorkItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.id.IdManager;
import org.batfish.coordinator.queues.WorkQueue.Type;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.role.NodeRolesData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkQueueMgr}. */
public final class WorkQueueMgrTest {

  private static final String NETWORK = "container";

  private static final String SNAPSHOT = "snapshot";

  private static final String REFERENCE_SNAPSHOT = "referenceSnapshot";

  @Rule public ExpectedException _thrown = ExpectedException.none();
  private SnapshotMetadataMgr _snapshotMetadataManager;
  private WorkQueueMgr _workQueueMgr;

  private enum ActionType {
    ASSIGN_ERROR,
    ASSIGN_FAILURE,
    ASSIGN_SUCCESS,
    QUEUE,
    STATUS_INPROGRESS,
    STATUS_TERMINATED_NORMALLY,
    STATUS_TERMINATED_ABNORMALLY,
    STATUS_UNREACHABLE
  }

  private class Action {
    public ActionType action;
    public QueuedWork work;

    public Action(ActionType action, QueuedWork work) {
      this.action = action;
      this.work = work;
    }
  }

  private QueuedWork doAction(Action action) throws Exception {

    switch (action.action) {
      case ASSIGN_ERROR:
        {
          QueuedWork work = _workQueueMgr.getWorkForAssignment();
          _workQueueMgr.markAssignmentError(work);
          return work;
        }
      case ASSIGN_FAILURE:
        {
          QueuedWork work = _workQueueMgr.getWorkForAssignment();
          _workQueueMgr.markAssignmentFailure(work);
          return work;
        }
      case ASSIGN_SUCCESS:
        {
          QueuedWork work = _workQueueMgr.getWorkForAssignment();
          _workQueueMgr.markAssignmentSuccess(work, () -> new Task(TaskStatus.InProgress));
          return work;
        }
      case QUEUE:
        {
          _workQueueMgr.queueUnassignedWork(action.work);
        }
        break;
      case STATUS_INPROGRESS:
        {
          Task task = new Task(TaskStatus.InProgress);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      case STATUS_TERMINATED_ABNORMALLY:
        {
          Task task = new Task(TaskStatus.TerminatedAbnormally);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      case STATUS_TERMINATED_NORMALLY:
        {
          Task task = new Task(TaskStatus.TerminatedNormally);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      case STATUS_UNREACHABLE:
        {
          Task task = new Task(TaskStatus.UnreachableOrBadResponse);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
    }
    return null;
  }

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private NetworkId _networkId;

  private IdManager _idManager;

  @Before
  public void init() {
    Main.mainInit(new String[0]);
    Main.setLogger(new BatfishLogger("debug", false));
    WorkMgrTestUtils.initWorkManager(_folder);
    Main.getWorkMgr().initNetwork(NETWORK, null);
    _idManager = Main.getWorkMgr().getIdManager();
    _snapshotMetadataManager = Main.getWorkMgr().getSnapshotMetadataManager();
    _networkId = _idManager.getNetworkId(NETWORK).get();
    _workQueueMgr = new WorkQueueMgr(Type.memory, Main.getLogger(), _snapshotMetadataManager);
  }

  private void initSnapshotMetadata(String snapshot, ProcessingStatus status) throws IOException {
    initSnapshotMetadata(NETWORK, snapshot, status);
  }

  private void initSnapshotMetadata(String network, String snapshot, ProcessingStatus status)
      throws IOException {
    WorkMgrTestUtils.initSnapshotWithTopology(NETWORK, snapshot, ImmutableSet.of());
    NetworkId networkId = _idManager.getNetworkId(network).get();
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, networkId).get();
    _snapshotMetadataManager.writeMetadata(
        new SnapshotMetadata(Instant.now(), null).updateStatus(status, null),
        networkId,
        snapshotId);
  }

  private void queueWork(String snapshot, WorkType wType) throws Exception {
    QueuedWork work =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(wType)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());
    _workQueueMgr.queueUnassignedWork(work);
  }

  private void queueWork(String snapshot, String referenceSnapshot, WorkType wType)
      throws Exception {
    QueuedWork work =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(wType)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .setReferenceSnapshotId(
                    _idManager.getSnapshotId(referenceSnapshot, _networkId).get())
                .setIsDifferential(true)
                .build());
    _workQueueMgr.queueUnassignedWork(work);
  }

  private void workIsRejected(ProcessingStatus trStatus, WorkType wType) throws Exception {
    initSnapshotMetadata(SNAPSHOT, trStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(NETWORK, SNAPSHOT),
            WorkDetails.builder()
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(SNAPSHOT, _networkId).get())
                .setWorkType(wType)
                .build());
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Cannot queue ");
    doAction(new Action(ActionType.QUEUE, work));
  }

  private void workIsRejected(
      ProcessingStatus baseTrStatus, ProcessingStatus deltaTrStatus, WorkType wType)
      throws Exception {
    initSnapshotMetadata(SNAPSHOT, baseTrStatus);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, deltaTrStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(NETWORK, SNAPSHOT),
            WorkDetails.builder()
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(SNAPSHOT, _networkId).get())
                .setReferenceSnapshotId(
                    _idManager.getSnapshotId(REFERENCE_SNAPSHOT, _networkId).get())
                .setIsDifferential(true)
                .setWorkType(wType)
                .build());
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Cannot queue ");
    doAction(new Action(ActionType.QUEUE, work));
  }

  private void workIsQueued(
      ProcessingStatus trStatus, WorkType wType, WorkStatusCode qwStatus, long queueLength)
      throws Exception {
    initSnapshotMetadata(SNAPSHOT, trStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(NETWORK, SNAPSHOT),
            WorkDetails.builder()
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(SNAPSHOT, _networkId).get())
                .setWorkType(wType)
                .build());
    doAction(new Action(ActionType.QUEUE, work));
    assertThat(work.getStatus(), equalTo(qwStatus));
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(queueLength));
  }

  private void workIsQueued(
      ProcessingStatus baseTrStatus,
      ProcessingStatus deltaTrStatus,
      WorkType wType,
      WorkStatusCode qwStatus,
      long queueLength)
      throws Exception {
    initSnapshotMetadata(SNAPSHOT, baseTrStatus);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, deltaTrStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(NETWORK, SNAPSHOT),
            WorkDetails.builder()
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(SNAPSHOT, _networkId).get())
                .setReferenceSnapshotId(
                    _idManager.getSnapshotId(REFERENCE_SNAPSHOT, _networkId).get())
                .setIsDifferential(true)
                .setWorkType(wType)
                .build());
    doAction(new Action(ActionType.QUEUE, work));
    assertThat(work.getStatus(), equalTo(qwStatus));
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(queueLength));
  }

  @Test
  public void getCompletedWork() throws Exception {
    String snapshot = "snapshot";
    WorkMgrTestUtils.initSnapshotWithTopology(NETWORK, snapshot, ImmutableSet.of());
    SnapshotId snapshot1Id = _idManager.getSnapshotId(snapshot, _networkId).get();

    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setNetworkId(_networkId)
            .setSnapshotId(snapshot1Id)
            .setWorkType(WorkType.UNKNOWN);
    QueuedWork work1 = new QueuedWork(new WorkItem(NETWORK, snapshot), builder.build());
    QueuedWork work2 = new QueuedWork(new WorkItem(NETWORK, snapshot), builder.build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // No items in complete queue yet
    List<QueuedWork> works0 = _workQueueMgr.getCompletedWork(_networkId, snapshot1Id);

    // Move one item from incomplete to complete queue
    _workQueueMgr.markAssignmentError(work1);
    List<QueuedWork> works1 = _workQueueMgr.getCompletedWork(_networkId, snapshot1Id);

    // Move a second item from incomplete to complete queue
    _workQueueMgr.markAssignmentError(work2);
    List<QueuedWork> works2 = _workQueueMgr.getCompletedWork(_networkId, snapshot1Id);

    // Confirm we don't see any work items when the complete queue is empty
    assertThat(works0, iterableWithSize(0));

    // Confirm we only see the one work item in the complete queue
    assertThat(works1, contains(hasWorkItem(equalTo(work1.getWorkItem()))));

    // Confirm we see both work items after they're both complete
    assertThat(
        works2,
        containsInAnyOrder(
            ImmutableList.of(
                hasWorkItem(equalTo(work1.getWorkItem())),
                hasWorkItem(equalTo(work2.getWorkItem())))));
  }

  @Test
  public void getCompletedWorkBadFilter() {
    // Make sure we get no results or error filtering on a bogus snapshot
    List<QueuedWork> works0 =
        _workQueueMgr.getCompletedWork(new NetworkId("bogus"), new SnapshotId("bogus"));
    assertThat(works0, iterableWithSize(0));
  }

  @Test
  public void getCompletedWorkFilter() throws Exception {
    String snapshot1 = "snapshot1";
    String snapshot2 = "snapshot2";
    String network1 = "network1";
    String network2 = "network2";
    Main.getWorkMgr().initNetwork(network1, null);
    Main.getWorkMgr().initNetwork(network2, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network1, snapshot1, ImmutableSet.of());
    WorkMgrTestUtils.initSnapshotWithTopology(network1, snapshot2, ImmutableSet.of());
    WorkMgrTestUtils.initSnapshotWithTopology(network2, snapshot1, ImmutableSet.of());

    NetworkId network1Id = _idManager.getNetworkId(network1).get();
    NetworkId network2Id = _idManager.getNetworkId(network2).get();
    SnapshotId network1Snapshot1Id = _idManager.getSnapshotId(snapshot1, network1Id).get();

    WorkDetails.Builder builder = WorkDetails.builder().setWorkType(WorkType.UNKNOWN);
    QueuedWork network1Snapshot1Work1 =
        new QueuedWork(
            new WorkItem(network1, snapshot1),
            builder.setNetworkId(network1Id).setSnapshotId(network1Snapshot1Id).build());
    QueuedWork network1Snapshot2Work =
        new QueuedWork(
            new WorkItem(network1, snapshot2),
            builder
                .setNetworkId(network1Id)
                .setSnapshotId(_idManager.getSnapshotId(snapshot2, network1Id).get())
                .build());
    QueuedWork network2Snapshot1Work =
        new QueuedWork(
            new WorkItem(network2, snapshot1),
            builder
                .setNetworkId(network2Id)
                .setSnapshotId(_idManager.getSnapshotId(snapshot1, network2Id).get())
                .build());
    _workQueueMgr.queueUnassignedWork(network1Snapshot1Work1);
    _workQueueMgr.queueUnassignedWork(network1Snapshot2Work);
    _workQueueMgr.queueUnassignedWork(network2Snapshot1Work);

    // Move one item for each network and snapshot from incomplete to complete queue
    _workQueueMgr.markAssignmentError(network1Snapshot1Work1);
    _workQueueMgr.markAssignmentError(network1Snapshot2Work);
    _workQueueMgr.markAssignmentError(network2Snapshot1Work);
    List<QueuedWork> works1 = _workQueueMgr.getCompletedWork(network1Id, network1Snapshot1Id);

    // Confirm we only see the network 1, snapshot 1 work item in the complete queue
    // i.e. make sure we don't see items from the other network or snapshot
    assertThat(works1, contains(hasWorkItem(equalTo(network1Snapshot1Work1.getWorkItem()))));
  }

  @Test
  public void getMatchingWorkAbsent() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    WorkItem wItem = new WorkItem(NETWORK, snapshot);
    wItem.addRequestParam("key", "value");
    // get matching work should be null on an empty queue
    QueuedWork matchingWork = _workQueueMgr.getMatchingWork(wItem, QueueType.INCOMPLETE);
    assertThat(matchingWork, equalTo(null));

    // build two work items that do not match
    WorkItem wItem1 = new WorkItem(NETWORK, snapshot);
    wItem1.addRequestParam("key1", "value1");
    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setWorkType(WorkType.UNKNOWN)
            .setNetworkId(_networkId)
            .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get());
    QueuedWork work1 = new QueuedWork(wItem1, builder.build());
    QueuedWork work2 = new QueuedWork(new WorkItem(NETWORK, snapshot), builder.build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // should be null again
    QueuedWork matchingWorkAgain = _workQueueMgr.getMatchingWork(wItem, QueueType.INCOMPLETE);
    assertThat(matchingWorkAgain, equalTo(null));
  }

  @Test
  public void getMatchingWorkPresent() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    WorkItem wItem1 = new WorkItem(NETWORK, snapshot);
    wItem1.addRequestParam("key1", "value1");
    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setWorkType(WorkType.UNKNOWN)
            .setNetworkId(_networkId)
            .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get());
    QueuedWork work1 = new QueuedWork(wItem1, builder.build());
    QueuedWork work2 = new QueuedWork(new WorkItem(NETWORK, snapshot), builder.build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // build a work item that should match wItem1
    WorkItem wItem3 = new WorkItem(NETWORK, snapshot);
    wItem3.addRequestParam("key1", "value1");

    QueuedWork matchingWork = _workQueueMgr.getMatchingWork(wItem3, QueueType.INCOMPLETE);

    assertThat(matchingWork, equalTo(work1));
  }

  @Test
  public void listIncompleteWork() throws Exception {
    String snapshot = "snapshot1";
    String otherNetwork = "other";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    Main.getWorkMgr().initNetwork(otherNetwork, null);
    WorkMgrTestUtils.initSnapshotWithTopology(otherNetwork, snapshot, ImmutableSet.of());
    initSnapshotMetadata(otherNetwork, snapshot, ProcessingStatus.UNINITIALIZED);
    NetworkId otherNetworkId = _idManager.getNetworkId(otherNetwork).get();
    WorkDetails.Builder builder = WorkDetails.builder().setWorkType(WorkType.UNKNOWN);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            builder
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(otherNetwork, snapshot),
            builder
                .setNetworkId(otherNetworkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, otherNetworkId).get())
                .build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    List<QueuedWork> works = _workQueueMgr.listIncompleteWork(_networkId, null, null);

    assertThat(works, equalTo(Collections.singletonList(work1)));
  }

  @Test
  public void listIncompleteWorkForSpecificStatus() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setNetworkId(_networkId)
            .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get());
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.PARSING).build());
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.UNKNOWN).build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    List<QueuedWork> parsingWorks =
        _workQueueMgr.listIncompleteWork(_networkId, null, WorkType.PARSING);

    assertThat(parsingWorks, equalTo(Collections.singletonList(work1)));
  }

  @Test
  public void listIncompleteWorkForSpecificSnapshot() throws Exception {
    String snapshot1 = "snapshot1";
    String snapshot2 = "snapshot2";
    initSnapshotMetadata(snapshot1, ProcessingStatus.UNINITIALIZED);
    initSnapshotMetadata(snapshot2, ProcessingStatus.UNINITIALIZED);
    WorkDetails.Builder builder =
        WorkDetails.builder().setWorkType(WorkType.UNKNOWN).setNetworkId(_networkId);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot1),
            builder.setSnapshotId(_idManager.getSnapshotId(snapshot1, _networkId).get()).build());
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot1),
            builder.setSnapshotId(_idManager.getSnapshotId(snapshot2, _networkId).get()).build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot1, _networkId).get();
    List<QueuedWork> parsingWorks = _workQueueMgr.listIncompleteWork(_networkId, snapshotId, null);

    assertThat(parsingWorks, equalTo(Collections.singletonList(work1)));
  }

  // BEGIN: INDEPENDENT_ANSWERING TESTS

  @Test
  public void indAnsweringIsQueued() throws Exception {
    workIsQueued(
        ProcessingStatus.UNINITIALIZED,
        WorkType.INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  // END: INDEPENDENT_ANSWERING_TESTS

  // BEGIN: PARSING_DEPENDENT_ANSWERING TESTS

  @Test
  public void pdAnsweringForUninitializedBase() throws Exception {
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING_DEPENDENT_ANSWERING);
  }

  @Test
  public void pdAnsweringForParsingBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING, WorkType.PARSING_DEPENDENT_ANSWERING);
  }

  @Test
  public void pdAnsweringForParsingFailBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING_FAIL, WorkType.PARSING_DEPENDENT_ANSWERING);
  }

  @Test
  public void pdAnsweringForParsedBase() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringForDataplaningBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringForDataplaningFailBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringForDataplanedBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringWithBaseParsingQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(SNAPSHOT, WorkType.PARSING);
    workIsQueued(
        ProcessingStatus.UNINITIALIZED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.PARSING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.PARSING, WorkType.PARSING_DEPENDENT_ANSWERING, WorkStatusCode.BLOCKED, 4);
    workIsQueued(
        ProcessingStatus.PARSED, WorkType.PARSING_DEPENDENT_ANSWERING, WorkStatusCode.BLOCKED, 5);
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        6);
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        7);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        8);
  }

  @Test
  public void pdAnsweringWithBaseDataplaningQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.PARSED);
    queueWork(SNAPSHOT, WorkType.DATAPLANING);
    workIsQueued(
        ProcessingStatus.PARSED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        2);
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        3);
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        4);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        5);
  }

  @Test
  public void pdAnsweringForUninitializedDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.PARSED,
        ProcessingStatus.UNINITIALIZED,
        WorkType.PARSING_DEPENDENT_ANSWERING);
  }

  @Test
  public void pdAnsweringForParsingFailDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING);
  }

  @Test
  public void pdAnsweringForParsedDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringForDataplaningDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringForDataplaningFailDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringForDataplanedDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void pdAnsweringWithDeltaParsingQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.PARSED);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(REFERENCE_SNAPSHOT, WorkType.PARSING);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.UNINITIALIZED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        5);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        6);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        7);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        8);
  }

  @Test
  public void pdAnsweringWithDeltaDataplaningQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.PARSED);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, ProcessingStatus.PARSED);
    queueWork(REFERENCE_SNAPSHOT, WorkType.DATAPLANING);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        2);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        3);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        4);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANED,
        WorkType.PARSING_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        5);
  }

  // END: PARSING_DEPENDENT_ANSWERING TESTS

  // BEGIN: DATAPLANE_DEPENDENT_ANSWERING TESTS

  @Test
  public void ddAnsweringForUninitializedBase() throws Exception {
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForParsingBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForParsingFailBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING_FAIL, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForParsedBase() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2); // it is 2 because dataplane work is auto-generated
  }

  @Test
  public void ddAnsweringForDataplaningBase() throws Exception {
    workIsRejected(ProcessingStatus.DATAPLANING, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForDataplaningFailBase() throws Exception {
    workIsRejected(ProcessingStatus.DATAPLANING_FAIL, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForDataplanedBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void ddAnsweringWithBaseParsingQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(SNAPSHOT, WorkType.PARSING);
    workIsQueued(
        ProcessingStatus.UNINITIALIZED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.PARSING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.PARSING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.PARSED, WorkType.DATAPLANE_DEPENDENT_ANSWERING, WorkStatusCode.BLOCKED, 5);
    // the cases for DATAPLANING and DATAPLANING_FAIL are below
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        6);
  }

  @Test
  public void ddAnsweringWithBaseParsingQueuedDataplaningFail() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(SNAPSHOT, WorkType.PARSING);
    workIsRejected(ProcessingStatus.DATAPLANING_FAIL, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithBaseParsingQueuedDataplaning() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(SNAPSHOT, WorkType.PARSING);
    workIsRejected(ProcessingStatus.DATAPLANING, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithBaseDataplaningQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.PARSED);
    queueWork(SNAPSHOT, WorkType.DATAPLANING);
    workIsQueued(
        ProcessingStatus.PARSED, WorkType.DATAPLANE_DEPENDENT_ANSWERING, WorkStatusCode.BLOCKED, 2);
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        5);
  }

  @Test
  public void ddAnsweringForUninitializedDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.UNINITIALIZED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForParsingFailDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.PARSING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForParsedDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2); // it is 2 because dataplane work is auto-generated
  }

  @Test
  public void ddAnsweringForDataplaningDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForDataplaningFailDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringForDataplanedDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void ddAnsweringWithDeltaParsingQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(REFERENCE_SNAPSHOT, WorkType.PARSING);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.UNINITIALIZED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.PARSING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.PARSING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        5);
    // the cases for DATAPLANING and DATAPLANING_FAIL are below
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        6);
  }

  @Test
  public void ddAnsweringWithDeltaParsingQueuedDataplaningFail() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(REFERENCE_SNAPSHOT, WorkType.PARSING);
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithDeltaParsingQueuedDataplaning() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    queueWork(REFERENCE_SNAPSHOT, WorkType.PARSING);
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithDeltaDataplaningQueued() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    initSnapshotMetadata(REFERENCE_SNAPSHOT, ProcessingStatus.PARSED);
    queueWork(REFERENCE_SNAPSHOT, WorkType.DATAPLANING);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        5);
  }

  //  END: DATAPLANE_DEPENDENT_ANSWERING TESTS

  // BEGIN: PARSING work tests

  @Test
  public void parsingForUninitializedBase() throws Exception {
    workIsQueued(ProcessingStatus.UNINITIALIZED, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingAnsweringForParsingBase() throws Exception {
    workIsQueued(ProcessingStatus.PARSING, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingForParsingFailBase() throws Exception {
    workIsQueued(ProcessingStatus.PARSING_FAIL, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingForParsedBase() throws Exception {
    workIsQueued(ProcessingStatus.PARSED, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingForDataplaningBase() throws Exception {
    workIsQueued(ProcessingStatus.DATAPLANING, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingForDataplaningFailBase() throws Exception {
    workIsQueued(ProcessingStatus.DATAPLANING_FAIL, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingForDataplanedBase() throws Exception {
    workIsQueued(ProcessingStatus.DATAPLANED, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void parsingWithConflictingWorkQueued1() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.PARSED);
    queueWork(SNAPSHOT, WorkType.PARSING_DEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void parsingWithConflictingWorkQueued2() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.UNINITIALIZED);
    initSnapshotMetadata("other", ProcessingStatus.DATAPLANED);
    queueWork(SNAPSHOT, WorkType.PARSING); // this work interferes
    queueWork("other", WorkType.PARSING); // this work does not interfere
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void parsingWithDeltaConflictingWorkQueued() throws Exception {
    initSnapshotMetadata("other", ProcessingStatus.DATAPLANED);
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    queueWork("other", SNAPSHOT, WorkType.PARSING_DEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void parsingWithNonConflictingWorkQueued() throws Exception {
    initSnapshotMetadata("other", ProcessingStatus.DATAPLANED);
    queueWork("other", WorkType.PARSING_DEPENDENT_ANSWERING);
    workIsQueued(ProcessingStatus.UNINITIALIZED, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 2);
  }

  // END: PARSING TESTS

  // BEGIN: DATAPLANING work tests

  @Test
  public void dataplaningForUninitializedBase() throws Exception {
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.DATAPLANING);
  }

  @Test
  public void dataplaningForParsingBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING, WorkType.DATAPLANING);
  }

  @Test
  public void dataplaningForParsingFailBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING_FAIL, WorkType.DATAPLANING);
  }

  @Test
  public void dataplaningForParsedBase() throws Exception {
    workIsQueued(ProcessingStatus.PARSED, WorkType.DATAPLANING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void dataplaningForDataplaningBase() throws Exception {
    workIsQueued(ProcessingStatus.DATAPLANING, WorkType.DATAPLANING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void dataplaningForDataplaningFailBase() throws Exception {
    workIsQueued(ProcessingStatus.DATAPLANING_FAIL, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void dataplaningForDataplanedBase() throws Exception {
    workIsQueued(ProcessingStatus.DATAPLANED, WorkType.PARSING, WorkStatusCode.UNASSIGNED, 1);
  }

  @Test
  public void dataplaningWithConflictingWorkQueued1() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    queueWork(SNAPSHOT, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void dataplaningWithConflictingWorkQueued2() throws Exception {
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    initSnapshotMetadata("other", ProcessingStatus.UNINITIALIZED);
    queueWork(SNAPSHOT, WorkType.PARSING); // this work interferes
    queueWork("other", WorkType.PARSING); // this work does not interfere
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void dataplaningWithDeltaConflictingWorkQueued() throws Exception {
    initSnapshotMetadata("other", ProcessingStatus.DATAPLANED);
    initSnapshotMetadata(SNAPSHOT, ProcessingStatus.DATAPLANED);
    queueWork("other", SNAPSHOT, WorkType.PARSING_DEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.DATAPLANING);
  }

  @Test
  public void dataplaningWithNonConflictingWorkQueued() throws Exception {
    initSnapshotMetadata("other", ProcessingStatus.DATAPLANED);
    queueWork("other", WorkType.PARSING_DEPENDENT_ANSWERING);
    workIsQueued(ProcessingStatus.PARSED, WorkType.DATAPLANING, WorkStatusCode.UNASSIGNED, 2);
  }

  @Test
  public void dataplaningAfterParsingFailure() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, _networkId).get();
    WorkDetails.Builder builder =
        WorkDetails.builder().setNetworkId(_networkId).setSnapshotId(snapshotId);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.PARSING).build());
    _workQueueMgr.queueUnassignedWork(work1);
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.DATAPLANING).build());
    _workQueueMgr.queueUnassignedWork(work2);

    QueuedWork aWork1 =
        doAction(new Action(ActionType.ASSIGN_SUCCESS, null)); // should be parsing work (work1)
    doAction(new Action(ActionType.STATUS_TERMINATED_ABNORMALLY, aWork1));

    // work2 should be left with terminatedqueuefail status and the testrig in parsing_fail state
    assertThat(work2.getStatus(), equalTo(WorkStatusCode.REQUEUEFAILURE));
    assertThat(
        _snapshotMetadataManager
            .getInitializationMetadata(_networkId, snapshotId)
            .getProcessingStatus(),
        equalTo(ProcessingStatus.PARSING_FAIL));
  }

  // END: DATAPLANING work tests

  @Test
  public void queueUnassignedWork() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setNetworkId(_networkId)
            .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get());
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.PARSING).build());
    _workQueueMgr.queueUnassignedWork(work1);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            builder.setWorkType(WorkType.PARSING_DEPENDENT_ANSWERING).build());
    _workQueueMgr.queueUnassignedWork(work2);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
  }

  @Test
  public void queueUnassignedWorkUnknown() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(WorkType.UNKNOWN)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());
    _workQueueMgr.queueUnassignedWork(work1);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
  }

  @Test
  public void testGetWorkForChecking() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    List<QueuedWork> workToCheck = _workQueueMgr.getWorkForChecking();

    // Make sure getWorkForChecking() returns no elements when the incomplete work queue is empty
    assertThat(workToCheck, empty());

    WorkDetails.Builder builder =
        WorkDetails.builder()
            .setNetworkId(_networkId)
            .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
            .setWorkType(WorkType.UNKNOWN);
    QueuedWork work1 = new QueuedWork(new WorkItem(NETWORK, snapshot), builder.build());
    QueuedWork work2 = new QueuedWork(new WorkItem(NETWORK, snapshot), builder.build());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);
    workToCheck = _workQueueMgr.getWorkForChecking();

    // Make sure unassigned items on the queue are not returned in getWorkForChecking()
    assertThat(workToCheck, empty());

    work2.setStatus(WorkStatusCode.ASSIGNED);
    workToCheck = _workQueueMgr.getWorkForChecking();

    // Make sure only one item is returned from getWorkForChecking() when there is only one assigned
    // item on the queue
    assertThat(workToCheck, iterableWithSize(1));

    // Make sure the correct work item was returned
    assertSame(workToCheck.get(0), work2);

    // When getWorkForChecking() is called, work2 should transition from ASSIGNED to CHECKINGSTATUS
    assertThat(work2.getStatus(), equalTo(WorkStatusCode.CHECKINGSTATUS));

    workToCheck = _workQueueMgr.getWorkForChecking();

    // Since work2 status is CHECKINGSTATUS (and work1 is UNASSIGNED), nothing should show up in
    // getWorkForChecking()
    assertThat(workToCheck, empty());

    work1.setStatus(WorkStatusCode.ASSIGNED);
    work2.setStatus(WorkStatusCode.ASSIGNED);
    workToCheck = _workQueueMgr.getWorkForChecking();

    // With multiple assigned items now queued, getWorkForChecking() should return multiple items
    assertThat(workToCheck, iterableWithSize(2));
  }

  @Test
  public void queueUnassignedWorkDuplicate() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(WorkType.UNKNOWN)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());
    _workQueueMgr.queueUnassignedWork(work1);

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Duplicate work item");

    _workQueueMgr.queueUnassignedWork(work1);
  }

  @Test
  public void workIsUnblocked1() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);

    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, _networkId).get();
    WorkDetails.Builder builder =
        WorkDetails.builder().setNetworkId(_networkId).setSnapshotId(snapshotId);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.PARSING).build());
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            builder.setWorkType(WorkType.PARSING_DEPENDENT_ANSWERING).build());
    QueuedWork work3 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            builder.setWorkType(WorkType.DATAPLANE_DEPENDENT_ANSWERING).build());

    doAction(new Action(ActionType.QUEUE, work1));
    doAction(new Action(ActionType.QUEUE, work2));
    doAction(new Action(ActionType.QUEUE, work3));

    assertThat(work2.getStatus(), equalTo(WorkStatusCode.BLOCKED));
    assertThat(work3.getStatus(), equalTo(WorkStatusCode.BLOCKED));

    QueuedWork aWork1 =
        doAction(new Action(ActionType.ASSIGN_SUCCESS, null)); // should be parsing work
    Main.getWorkMgr()
        .getStorage()
        .storeNodeRoles(
            _networkId,
            NodeRolesData.builder().build(),
            _idManager.getSnapshotNodeRolesId(_networkId, snapshotId));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork1));

    QueuedWork aWork2 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork2));

    QueuedWork aWork3 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork3));

    QueuedWork aWork4 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork4));

    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(0L));
  }

  @Test
  public void workIsUnblocked2() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);

    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, _networkId).get();
    WorkDetails.Builder builder =
        WorkDetails.builder().setNetworkId(_networkId).setSnapshotId(snapshotId);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot), builder.setWorkType(WorkType.PARSING).build());
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            builder.setWorkType(WorkType.DATAPLANE_DEPENDENT_ANSWERING).build());
    QueuedWork work3 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            builder.setWorkType(WorkType.DATAPLANE_DEPENDENT_ANSWERING).build());

    doAction(new Action(ActionType.QUEUE, work1));
    doAction(new Action(ActionType.QUEUE, work2));
    doAction(new Action(ActionType.QUEUE, work3));

    assertThat(work2.getStatus(), equalTo(WorkStatusCode.BLOCKED));
    assertThat(work3.getStatus(), equalTo(WorkStatusCode.BLOCKED));

    QueuedWork aWork1 =
        doAction(new Action(ActionType.ASSIGN_SUCCESS, null)); // should be parsing work
    Main.getWorkMgr()
        .getStorage()
        .storeNodeRoles(
            _networkId,
            NodeRolesData.builder().build(),
            _idManager.getSnapshotNodeRolesId(_networkId, snapshotId));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork1));

    QueuedWork aWork2 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork2));

    QueuedWork aWork3 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork3));

    QueuedWork aWork4 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork4));

    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(0L));
  }

  @Test
  public void processTaskCheckTerminatedByUser() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(WorkType.PARSING)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());

    doAction(new Action(ActionType.QUEUE, work1));
    _workQueueMgr.processTaskCheckResult(work1, new Task(TaskStatus.TerminatedByUser, "Fake"));

    /*
     * after processing the termination task,
     *  1) the status of work should be terminatedbyuser
     *  2) incomplete queue should be empty
     */
    assertThat(work1.getStatus(), equalTo(WorkStatusCode.TERMINATEDBYUSER));
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(0L));
  }

  @Test
  public void testProcessTaskCheckResultPromoteSuccessfulParse() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, _networkId).get();
    NodeRolesId snapshotNodeRolesId = _idManager.getSnapshotNodeRolesId(_networkId, snapshotId);
    Main.getWorkMgr()
        .getStorage()
        .storeNodeRoles(_networkId, NodeRolesData.builder().build(), snapshotNodeRolesId);

    assertFalse(_idManager.hasNetworkNodeRolesId(_networkId));

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(WorkType.PARSING)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());

    doAction(new Action(ActionType.QUEUE, work1));
    _workQueueMgr.processTaskCheckResult(work1, new Task(TaskStatus.TerminatedNormally, "Fake"));

    assertThat(_idManager.getNetworkNodeRolesId(_networkId).get(), equalTo(snapshotNodeRolesId));
  }

  @Test
  public void testProcessTaskCheckResultPromoteUnsuccessfulParse() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, _networkId).get();
    NodeRolesId snapshotNodeRolesId = _idManager.getSnapshotNodeRolesId(_networkId, snapshotId);
    Main.getWorkMgr()
        .getStorage()
        .storeNodeRoles(_networkId, NodeRolesData.builder().build(), snapshotNodeRolesId);

    assertFalse(_idManager.hasNetworkNodeRolesId(_networkId));

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(WorkType.PARSING)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());

    doAction(new Action(ActionType.QUEUE, work1));
    _workQueueMgr.processTaskCheckResult(work1, new Task(TaskStatus.TerminatedAbnormally, "Fake"));

    assertFalse(_idManager.hasNetworkNodeRolesId(_networkId));
  }

  @Test
  public void testProcessTaskCheckResultPromoteNoChange() throws Exception {
    String snapshot = "snapshot1";
    initSnapshotMetadata(snapshot, ProcessingStatus.UNINITIALIZED);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, _networkId).get();
    NodeRolesId snapshotNodeRolesId = _idManager.getSnapshotNodeRolesId(_networkId, snapshotId);
    Main.getWorkMgr()
        .getStorage()
        .storeNodeRoles(_networkId, NodeRolesData.builder().build(), snapshotNodeRolesId);
    NodeRolesId oldNodeRolesId = new NodeRolesId("old");
    _idManager.assignNetworkNodeRolesId(_networkId, oldNodeRolesId);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(NETWORK, snapshot),
            WorkDetails.builder()
                .setWorkType(WorkType.PARSING)
                .setNetworkId(_networkId)
                .setSnapshotId(_idManager.getSnapshotId(snapshot, _networkId).get())
                .build());

    doAction(new Action(ActionType.QUEUE, work1));
    _workQueueMgr.processTaskCheckResult(work1, new Task(TaskStatus.TerminatedNormally, "Fake"));

    assertThat(_idManager.getNetworkNodeRolesId(_networkId).get(), equalTo(oldNodeRolesId));
  }
}
