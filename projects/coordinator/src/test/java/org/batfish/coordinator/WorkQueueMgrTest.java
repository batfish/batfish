package org.batfish.coordinator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import java.time.Instant;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.WorkItem;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.queues.WorkQueue.Type;
import org.batfish.datamodel.EnvironmentMetadata;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.batfish.datamodel.TestrigMetadata;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link WorkQueueMgr}. */
public class WorkQueueMgrTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private void initTestrigMetadata(String testrig, String environment, ProcessingStatus status)
      throws JsonProcessingException {
    Path metadataPath = WorkMgr.getpathTestrigMetadata("container", testrig);
    metadataPath.getParent().toFile().mkdirs();
    TestrigMetadata trMetadata = new TestrigMetadata(Instant.now(), environment);
    EnvironmentMetadata envMetadata = trMetadata.getEnvironments().get(environment);
    envMetadata.updateStatus(status);
    TestrigMetadataMgr.writeMetadata(trMetadata, metadataPath);
  }

  @Test
  public void qetMatchingWorkAbsent() throws Exception {
    WorkQueueMgr workQueueMgr = new WorkQueueMgr(Type.memory, new BatfishLogger("debug", false));
    WorkItem wItem1 = new WorkItem("container", "testrig");
    wItem1.addRequestParam("key1", "value1");
    QueuedWork work1 = new QueuedWork(wItem1, new WorkDetails());
    QueuedWork work2 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work1);
    workQueueMgr.queueUnassignedWork(work2);

    // build a work item that matches none of the queued works
    WorkItem wItem3 = new WorkItem("container", "testrig");
    wItem3.addRequestParam("key3", "value3");

    QueuedWork matchingWork = workQueueMgr.getMatchingWork(wItem3, QueueType.INCOMPLETE);

    assertThat(matchingWork, equalTo(null));
  }

  @Test
  public void qetMatchingWorkPresent() throws Exception {
    WorkQueueMgr workQueueMgr = new WorkQueueMgr(Type.memory, new BatfishLogger("debug", false));
    WorkItem wItem1 = new WorkItem("container", "testrig");
    wItem1.addRequestParam("key1", "value1");
    QueuedWork work1 = new QueuedWork(wItem1, new WorkDetails());
    QueuedWork work2 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work1);
    workQueueMgr.queueUnassignedWork(work2);

    // build a work item that should match wItem1
    WorkItem wItem3 = new WorkItem("container", "testrig");
    wItem3.addRequestParam("key1", "value1");

    QueuedWork matchingWork = workQueueMgr.getMatchingWork(wItem3, QueueType.INCOMPLETE);

    assertThat(matchingWork, equalTo(work1));
  }

  @Test
  public void queueUnassignedWorkParsingSuccess() throws Exception {
    Main.mainInit(new String[0]);
    Main.setLogger(new BatfishLogger("debug", false));
    Main.getSettings().setContainersLocation(CommonUtil.createTempDirectory("wqmt"));
    WorkQueueMgr workQueueMgr = new WorkQueueMgr(Type.memory, Main.getLogger());

    initTestrigMetadata("testrig1", "env_default", ProcessingStatus.DATAPLANED);
    WorkDetails details1 =
        new WorkDetails(
            "testrig1", "env_default", null, null, false, WorkType.DATAPLANE_DEPENDENT_ANSWERING);
    QueuedWork work1 = new QueuedWork(new WorkItem("container", "testrig1"), details1);
    workQueueMgr.queueUnassignedWork(work1);

    // queue parsing for a different testrig; should succeed
    initTestrigMetadata("testrig2", "env_default", ProcessingStatus.UNINITIALIZED);
    WorkDetails details2 =
        new WorkDetails("testrig2", "env_default", null, null, false, WorkType.PARSING);
    QueuedWork work2 = new QueuedWork(new WorkItem("container", "testrig2"), details2);
    workQueueMgr.queueUnassignedWork(work2);

    assertThat(workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
    assertThat(work2.getStatus(), equalTo(WorkStatusCode.UNASSIGNED));
  }

  @Test
  public void queueUnassignedWorkParsingFailure() throws Exception {
    WorkQueueMgr workQueueMgr = new WorkQueueMgr(Type.memory, new BatfishLogger("debug", false));
    QueuedWork work1 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work1);
    assertThat(workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
    QueuedWork work2 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work2);
    assertThat(workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
  }

  @Test
  public void queueUnassignedWorkUnknown() throws Exception {
    WorkQueueMgr workQueueMgr = new WorkQueueMgr(Type.memory, new BatfishLogger("debug", false));
    QueuedWork work1 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work1);
    assertThat(workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(1L));
    QueuedWork work2 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work2);
    assertThat(workQueueMgr.getLength(QueueType.INCOMPLETE), equalTo(2L));
  }

  @Test
  public void queueUnassignedWorkDuplicate() throws Exception {
    WorkQueueMgr workQueueMgr = new WorkQueueMgr(Type.memory, new BatfishLogger("debug", false));
    QueuedWork work1 = new QueuedWork(new WorkItem("container", "testrig"), new WorkDetails());
    workQueueMgr.queueUnassignedWork(work1);

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Duplicate work item");

    workQueueMgr.queueUnassignedWork(work1);
  }
}
