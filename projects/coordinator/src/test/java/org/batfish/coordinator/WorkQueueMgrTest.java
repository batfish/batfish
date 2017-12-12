package org.batfish.coordinator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import java.time.Instant;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.WorkItemBuilder;
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

  private final String CONTAINER = "container";

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
    Path metadataPath = WorkMgr.getpathTestrigMetadata(CONTAINER, testrig);
    metadataPath.getParent().toFile().mkdirs();
    TestrigMetadata trMetadata = new TestrigMetadata(Instant.now(), environment);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(environment);
    envMetadata.updateStatus(status);
    TestrigMetadataMgr.writeMetadata(trMetadata, metadataPath);
  }

  @Test
  public void qetMatchingWorkAbsent() throws Exception {
    WorkItem wItem1 = new WorkItem(CONTAINER, "testrig");
    wItem1.addRequestParam("key1", "value1");
    QueuedWork work1 = new QueuedWork(wItem1, new WorkDetails());
    QueuedWork work2 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // build a work item that matches none of the queued works
    WorkItem wItem3 = new WorkItem(CONTAINER, "testrig");
    wItem3.addRequestParam("key3", "value3");

    QueuedWork matchingWork = _workQueueMgr.getMatchingWork(wItem3, QueueType.INCOMPLETE);

    assertThat(matchingWork, equalTo(null));
  }

  @Test
  public void qetMatchingWorkPresent() throws Exception {
    WorkItem wItem1 = new WorkItem(CONTAINER, "testrig");
    wItem1.addRequestParam("key1", "value1");
    QueuedWork work1 = new QueuedWork(wItem1, new WorkDetails());
    QueuedWork work2 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work1);
    _workQueueMgr.queueUnassignedWork(work2);

    // build a work item that should match wItem1
    WorkItem wItem3 = new WorkItem(CONTAINER, "testrig");
    wItem3.addRequestParam("key1", "value1");

    QueuedWork matchingWork = _workQueueMgr.getMatchingWork(wItem3, QueueType.INCOMPLETE);

    assertThat(matchingWork, equalTo(work1));
  }

  @Test
  public void answeringIsRejectedForNonParsedTestrigs() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.PARSING_FAIL);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        "Cannot queue dataplane dependent work for testrig1 / env_default: "
            + "Status is PARSING_FAIL but no incomplete parsing work exists");

    doAction(new Action(ActionType.QUEUE, work1));
  }

  @Test
  public void answeringIsQueuedForParsedTestrigs() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.DATAPLANING);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_INDEPENDENT_ANSWERING));

    doAction(new Action(ActionType.QUEUE, work1));
    assertThat(work1.getStatus(), equalTo(WorkStatusCode.UNASSIGNED));
  }

  @Test
  public void dpAnsweringIsBlockedForNonDataplanedTestrigs() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.PARSED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));

    doAction(new Action(ActionType.QUEUE, work1));
    assertThat(work1.getStatus(), equalTo(WorkStatusCode.BLOCKED));

    // also assert that a dataplane job was inserted
    QueuedWork dpWork =
        _workQueueMgr.getMatchingWork(
            WorkItemBuilder.getWorkItemGenerateDataPlane(CONTAINER, "testrig1", "env_default"),
            QueueType.INCOMPLETE);
    assertThat(dpWork.getStatus(), equalTo(WorkStatusCode.UNASSIGNED));
  }

  @Test
  public void dpAnsweringIsRejectedForNonDataplanedTestrigs() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.DATAPLANING_FAIL);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        "Cannot queue dataplane dependent work for testrig1 / env_default: "
            + "Status is DATAPLANING_FAIL but no incomplete dataplaning work exists");

    doAction(new Action(ActionType.QUEUE, work1));
  }

  @Test
  public void dpAnsweringIsQueuedForDataplanedTestrigs() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.DATAPLANED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));

    doAction(new Action(ActionType.QUEUE, work1));
    assertThat(work1.getStatus(), equalTo(WorkStatusCode.UNASSIGNED));
  }

  @Test
  public void parsingIsNotBlockedByOtherTestrigs() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.DATAPLANED);
    initTestrigMetadata("testrig2", "env_default", ProcessingStatus.UNINITIALIZED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig2"),
            new WorkDetails("testrig2", "env_default", WorkType.PARSING));

    doAction(new Action(ActionType.QUEUE, work1));
    doAction(new Action(ActionType.QUEUE, work2));

    assertThat(work1.getStatus(), equalTo(WorkStatusCode.UNASSIGNED));
  }

  @Test
  public void parsingIsRejectedWhenOtherWorkIsPresent() throws Exception {
    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.DATAPLANED);

    QueuedWork work1 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig1"),
            new WorkDetails("testrig1", "env_default", WorkType.DATAPLANE_DEPENDENT_ANSWERING));
    QueuedWork work2 =
        new QueuedWork(
            new WorkItem(CONTAINER, "testrig2"),
            new WorkDetails("testrig1", "env_default", WorkType.PARSING));

    doAction(new Action(ActionType.QUEUE, work1));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Cannot queue parsing work while other work is incomplete");

    doAction(new Action(ActionType.QUEUE, work2));
  }

  @Test
  public void queueUnassignedWorkParsingFailure() throws Exception {
    QueuedWork work1 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work1);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
    QueuedWork work2 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work2);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
  }

  @Test
  public void queueUnassignedWorkUnknown() throws Exception {
    QueuedWork work1 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work1);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
    QueuedWork work2 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work2);
    assertThat(_workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
  }

  @Test
  public void queueUnassignedWorkDuplicate() throws Exception {
    QueuedWork work1 = new QueuedWork(new WorkItem(CONTAINER, "testrig"), new WorkDetails());
    _workQueueMgr.queueUnassignedWork(work1);

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Duplicate work item");

    _workQueueMgr.queueUnassignedWork(work1);
  }

  @Test
  public void workIsUnblocked() throws Exception {
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
}
