package org.batfish.coordinator;

import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.queues.WorkQueue.Type;
import org.batfish.datamodel.EnvironmentMetadata;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.batfish.datamodel.TestrigMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link WorkQueueMgr}. */
public class WorkQueueMgrTest {

  private static final String CONTAINER = "container";

  private static final String BASE_TESTRIG = "baseTestrig";

  private static final String BASE_ENV = "baseEnv";

  private static final String DELTA_TESTRIG = "deltaTestrig";

  private static final String DELTA_ENV = "deltaEnv";

  @Rule public ExpectedException _thrown = ExpectedException.none();

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

  private WorkQueueMgr _workQueueMgr;

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
          _workQueueMgr.markAssignmentSuccess(work, "test");
          return work;
        }
      case QUEUE:
        {
          _workQueueMgr.queueUnassignedWork(action.work);
        }
        break;
      case STATUS_INPROGRESS:
        {
          Task task = new Task();
          task.setStatus(TaskStatus.InProgress);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      case STATUS_TERMINATED_ABNORMALLY:
        {
          Task task = new Task();
          task.setStatus(TaskStatus.TerminatedAbnormally);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      case STATUS_TERMINATED_NORMALLY:
        {
          Task task = new Task();
          task.setStatus(TaskStatus.TerminatedNormally);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      case STATUS_UNREACHABLE:
        {
          Task task = new Task();
          task.setStatus(TaskStatus.UnreachableOrBadResponse);
          _workQueueMgr.processTaskCheckResult(action.work, task);
        }
        break;
      default:
        throw new BatfishException("Unhandled action type " + action.action);
    }
    return null;
  }

  @Before
  public void init() {
    Main.mainInit(new String[0]);
    Main.setLogger(new BatfishLogger("debug", false));
    Main.getSettings().setContainersLocation(CommonUtil.createTempDirectory("wqmt"));
    _workQueueMgr = new WorkQueueMgr(Type.memory, Main.getLogger());
  }

  private void initTestrigMetadata(String testrig, String environment, ProcessingStatus status)
      throws JsonProcessingException {
    initTestrigMetadata(CONTAINER, testrig, environment, status);
  }

  private void initTestrigMetadata(
      String container, String testrig, String environment, ProcessingStatus status)
      throws JsonProcessingException {
    Path metadataPath = WorkMgr.getpathTestrigMetadata(container, testrig);
    metadataPath.getParent().toFile().mkdirs();
    TestrigMetadata trMetadata = new TestrigMetadata(Instant.now(), environment);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(environment);
    envMetadata.updateStatus(status);
    TestrigMetadataMgr.writeMetadata(trMetadata, metadataPath);
  }

  private void queueWork(String testrig, String environment, WorkType wType) throws Exception {
    QueuedWork work =
        new QueuedWork(
            new WorkItem(CONTAINER, testrig), new WorkDetails(testrig, environment, wType));
    _workQueueMgr.queueUnassignedWork(work);
  }

  private void queueWork(
      String baseTestrig, String baseEnv, String deltaTestrig, String deltaEnv, WorkType wType)
      throws Exception {
    QueuedWork work =
        new QueuedWork(
            new WorkItem(CONTAINER, baseTestrig),
            new WorkDetails(baseTestrig, baseEnv, deltaTestrig, deltaEnv, true, wType));
    _workQueueMgr.queueUnassignedWork(work);
  }

  private void workIsRejected(ProcessingStatus trStatus, WorkType wType) throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, trStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(CONTAINER, BASE_TESTRIG), new WorkDetails(BASE_TESTRIG, BASE_ENV, wType));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Cannot queue ");
    doAction(new Action(ActionType.QUEUE, work));
  }

  private void workIsRejected(
      ProcessingStatus baseTrStatus, ProcessingStatus deltaTrStatus, WorkType wType)
      throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, baseTrStatus);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, deltaTrStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(CONTAINER, BASE_TESTRIG),
            new WorkDetails(BASE_TESTRIG, BASE_ENV, DELTA_TESTRIG, DELTA_ENV, true, wType));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Cannot queue ");
    doAction(new Action(ActionType.QUEUE, work));
  }

  private void workIsQueued(
      ProcessingStatus trStatus, WorkType wType, WorkStatusCode qwStatus, long queueLength)
      throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, trStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(CONTAINER, BASE_TESTRIG), new WorkDetails(BASE_TESTRIG, BASE_ENV, wType));
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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, baseTrStatus);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, deltaTrStatus);
    QueuedWork work =
        new QueuedWork(
            new WorkItem(CONTAINER, BASE_TESTRIG),
            new WorkDetails(BASE_TESTRIG, BASE_ENV, DELTA_TESTRIG, DELTA_ENV, true, wType));
    doAction(new Action(ActionType.QUEUE, work));
    assertThat(work.getStatus(), equalTo(qwStatus));
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(queueLength));
  }

  @Test
  public void getMatchingWorkAbsent() throws Exception {
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    WorkItem wItem = new WorkItem(CONTAINER, "testrig");
    wItem.addRequestParam("key", "value");
    // get matching work should be null on an empty queue
    QueuedWork matchingWork = _workQueueMgr.getMatchingWork(wItem, QueueType.INCOMPLETE);
    assertThat(matchingWork, equalTo(null));

    // build two work items that do not match
    WorkItem wItem1 = new WorkItem(CONTAINER, "testrig");
    wItem1.addRequestParam("key1", "value1");
    QueuedWork work1 = new QueuedWork(wItem1, new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // should be null again
    QueuedWork matchingWorkAgain = _workQueueMgr.getMatchingWork(wItem, QueueType.INCOMPLETE);
    assertThat(matchingWorkAgain, equalTo(null));
  }

  @Test
  public void getMatchingWorkPresent() throws Exception {
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    WorkItem wItem1 = new WorkItem(CONTAINER, "testrig");
    wItem1.addRequestParam("key1", "value1");
    QueuedWork work1 = new QueuedWork(wItem1, new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // build a work item that should match wItem1
    WorkItem wItem3 = new WorkItem(CONTAINER, "testrig");
    wItem3.addRequestParam("key1", "value1");

    QueuedWork matchingWork = _workQueueMgr.getMatchingWork(wItem3, QueueType.INCOMPLETE);

    assertThat(matchingWork, equalTo(work1));
  }

  @Test
  public void listIncompleteWork() throws Exception {
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    initTestrigMetadata("other", "testrig", "env", ProcessingStatus.UNINITIALIZED);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem("other", "testrig"), new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    List<QueuedWork> works = _workQueueMgr.listIncompleteWork(CONTAINER);

    assertThat(works, equalTo(Collections.singletonList(work1)));
  }

  // BEGIN: DATAPLANE_INDEPENDENT_ANSWERING TESTS

  @Test
  public void diAnsweringForUninitializedBase() throws Exception {
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
  }

  @Test
  public void diAnsweringForParsingBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING, WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
  }

  @Test
  public void diAnsweringForParsingFailBase() throws Exception {
    workIsRejected(ProcessingStatus.PARSING_FAIL, WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
  }

  @Test
  public void diAnsweringForParsedBase() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringForDataplaningBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringForDataplaningFailBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringForDataplanedBase() throws Exception {
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringWithBaseParsingQueued() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.PARSING);
    workIsQueued(
        ProcessingStatus.UNINITIALIZED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.PARSING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.PARSING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        5);
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        6);
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        7);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        8);
  }

  @Test
  public void diAnsweringWithBaseDataplaningQueued() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.PARSED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.DATAPLANING);
    workIsQueued(
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        2);
    workIsQueued(
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        3);
    workIsQueued(
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        4);
    workIsQueued(
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        5);
  }

  @Test
  public void diAnsweringForUninitializedDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.PARSED,
        ProcessingStatus.UNINITIALIZED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
  }

  @Test
  public void diAnsweringForParsingFailDelta() throws Exception {
    workIsRejected(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
  }

  @Test
  public void diAnsweringForParsedDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringForDataplaningDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringForDataplaningFailDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringForDataplanedDelta() throws Exception {
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        1);
  }

  @Test
  public void diAnsweringWithDeltaParsingQueued() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.PARSED);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(DELTA_TESTRIG, DELTA_ENV, WorkType.PARSING);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.UNINITIALIZED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        2);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        3);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        4);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        5);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        6);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        7);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.BLOCKED,
        8);
  }

  @Test
  public void diAnsweringWithDeltaDataplaningQueued() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.PARSED);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, ProcessingStatus.PARSED);
    queueWork(DELTA_TESTRIG, DELTA_ENV, WorkType.DATAPLANING);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.PARSED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        2);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        3);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        4);
    workIsQueued(
        ProcessingStatus.PARSED,
        ProcessingStatus.DATAPLANED,
        WorkType.DATAPLANE_INDEPENDENT_ANSWERING,
        WorkStatusCode.UNASSIGNED,
        5);
  }

  // END: DATAPLANE_INDEPENDENT_ANSWERING TESTS

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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.PARSING);
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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.PARSING);
    workIsRejected(ProcessingStatus.DATAPLANING_FAIL, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithBaseParsingQueuedDataplaning() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.PARSING);
    workIsRejected(ProcessingStatus.DATAPLANING, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithBaseDataplaningQueued() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.PARSED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.DATAPLANING);
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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(DELTA_TESTRIG, DELTA_ENV, WorkType.PARSING);
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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(DELTA_TESTRIG, DELTA_ENV, WorkType.PARSING);
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING_FAIL,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithDeltaParsingQueuedDataplaning() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, ProcessingStatus.UNINITIALIZED);
    queueWork(DELTA_TESTRIG, DELTA_ENV, WorkType.PARSING);
    workIsRejected(
        ProcessingStatus.DATAPLANED,
        ProcessingStatus.DATAPLANING,
        WorkType.DATAPLANE_DEPENDENT_ANSWERING);
  }

  @Test
  public void ddAnsweringWithDeltaDataplaningQueued() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    initTestrigMetadata(DELTA_TESTRIG, DELTA_ENV, ProcessingStatus.PARSED);
    queueWork(DELTA_TESTRIG, DELTA_ENV, WorkType.DATAPLANING);
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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.PARSED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void parsingWithConflictingWorkQueued2() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.UNINITIALIZED);
    initTestrigMetadata("other", "other", ProcessingStatus.DATAPLANED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.PARSING); // this work interferes
    queueWork("other", "other", WorkType.PARSING); // this work does not interfere
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void parsingWithDeltaConflictingWorkQueued() throws Exception {
    initTestrigMetadata("other", "other", ProcessingStatus.DATAPLANED);
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    queueWork("other", "other", BASE_TESTRIG, BASE_ENV, WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void parsingWithNonConflictingWorkQueued() throws Exception {
    initTestrigMetadata("other", "other", ProcessingStatus.DATAPLANED);
    queueWork("other", "other", WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
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
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void dataplaningWithConflictingWorkQueued2() throws Exception {
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    initTestrigMetadata("other", "other", ProcessingStatus.UNINITIALIZED);
    queueWork(BASE_TESTRIG, BASE_ENV, WorkType.PARSING); // this work interferes
    queueWork("other", "other", WorkType.PARSING); // this work does not interfere
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.PARSING);
  }

  @Test
  public void dataplaningWithDeltaConflictingWorkQueued() throws Exception {
    initTestrigMetadata("other", "other", ProcessingStatus.DATAPLANED);
    initTestrigMetadata(BASE_TESTRIG, BASE_ENV, ProcessingStatus.DATAPLANED);
    queueWork("other", "other", BASE_TESTRIG, BASE_ENV, WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
    workIsRejected(ProcessingStatus.UNINITIALIZED, WorkType.DATAPLANING);
  }

  @Test
  public void dataplaningWithNonConflictingWorkQueued() throws Exception {
    initTestrigMetadata("other", "other", ProcessingStatus.DATAPLANED);
    queueWork("other", "other", WorkType.DATAPLANE_INDEPENDENT_ANSWERING);
    workIsQueued(ProcessingStatus.PARSED, WorkType.DATAPLANING, WorkStatusCode.UNASSIGNED, 2);
  }

  // END: DATAPLANING work tests

  @Test
  public void queueUnassignedWork() throws Exception {
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.PARSING));
    _workQueueMgr.queueUnassignedWork(work1);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.DATAPLANE_INDEPENDENT_ANSWERING));
    _workQueueMgr.queueUnassignedWork(work2);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
  }

  @Test
  public void queueUnassignedWorkUnknown() throws Exception {
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    _workQueueMgr.queueUnassignedWork(work1);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
  }

  @Test
  public void testGetWorkForChecking() throws Exception {
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    List<QueuedWork> workToCheck = _workQueueMgr.getWorkForChecking();

    // Make sure getWorkForChecking() returns no elements when the incomplete work queue is empty
    assertThat(workToCheck, empty());

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
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
    initTestrigMetadata("testrig", "env", ProcessingStatus.UNINITIALIZED);
    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env", WorkType.UNKNOWN));
    _workQueueMgr.queueUnassignedWork(work1);

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Duplicate work item");

    _workQueueMgr.queueUnassignedWork(work1);
  }

  @Test
  public void workIsUnblocked1() throws Exception {
    initTestrigMetadata("testrig", "env_default", ProcessingStatus.UNINITIALIZED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env_default", WorkType.PARSING));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env_default", WorkType.DATAPLANE_INDEPENDENT_ANSWERING));
    QueuedWork work3 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));

    doAction(new Action(ActionType.QUEUE, work1));
    doAction(new Action(ActionType.QUEUE, work2));
    doAction(new Action(ActionType.QUEUE, work3));

    assertThat(work2.getStatus(), equalTo(WorkStatusCode.BLOCKED));
    assertThat(work3.getStatus(), equalTo(WorkStatusCode.BLOCKED));

    QueuedWork aWork1 =
        doAction(new Action(ActionType.ASSIGN_SUCCESS, null)); // should be parsing work
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
    initTestrigMetadata("testrig", "env_default", ProcessingStatus.UNINITIALIZED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env_default", WorkType.PARSING));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));
    QueuedWork work3 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig"),
            new WorkDetails("testrig", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));

    doAction(new Action(ActionType.QUEUE, work1));
    doAction(new Action(ActionType.QUEUE, work2));
    doAction(new Action(ActionType.QUEUE, work3));

    assertThat(work2.getStatus(), equalTo(WorkStatusCode.BLOCKED));
    assertThat(work3.getStatus(), equalTo(WorkStatusCode.BLOCKED));

    QueuedWork aWork1 =
        doAction(new Action(ActionType.ASSIGN_SUCCESS, null)); // should be parsing work
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork1));

    QueuedWork aWork2 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork2));

    QueuedWork aWork3 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork3));

    QueuedWork aWork4 = doAction(new Action(ActionType.ASSIGN_SUCCESS, null));
    doAction(new Action(ActionType.STATUS_TERMINATED_NORMALLY, aWork4));

    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(0L));
  }
}
