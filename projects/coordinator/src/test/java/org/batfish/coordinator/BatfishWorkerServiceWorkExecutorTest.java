package org.batfish.coordinator;

import static org.batfish.coordinator.BatfishWorkerServiceWorkExecutor.getRunArgs;
import static org.batfish.coordinator.BatfishWorkerServiceWorkExecutor.processLaunchResult;
import static org.batfish.coordinator.SubmissionResult.Type.BUSY;
import static org.batfish.coordinator.SubmissionResult.Type.ERROR;
import static org.batfish.coordinator.SubmissionResult.Type.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishWorkerService;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.LaunchResult;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link BatfishWorkerServiceWorkExecutor}. */
public final class BatfishWorkerServiceWorkExecutorTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testProcessLaunchResult() {
    String taskId = "foo";
    {
      LaunchResult lr = LaunchResult.busy();
      SubmissionResult sr = processLaunchResult(lr, taskId, BATFISH_WORKER_SERVICE);
      assertThat(sr.getType(), equalTo(BUSY));
    }
    {
      LaunchResult lr = LaunchResult.launched();
      SubmissionResult sr = processLaunchResult(lr, taskId, BATFISH_WORKER_SERVICE);
      assertThat(sr.getType(), equalTo(SUCCESS));
      assertThat(sr.getTaskHandle().checkTask(), sameInstance(TEST_TASK));
    }
    {
      LaunchResult lr = LaunchResult.error("bar");
      SubmissionResult sr = processLaunchResult(lr, taskId, BATFISH_WORKER_SERVICE);
      assertThat(sr.getType(), equalTo(ERROR));
      assertThat(sr.getMessage(), equalTo("bar"));
    }
  }

  @Test
  public void testGetRunArgs() {
    Path containersLocation = Paths.get("/dev/null");

    assertThat(
        getRunArgs(getTestQueuedWork(), containersLocation),
        equalTo(
            ImmutableList.of(
                "-baz",
                "-container",
                "n",
                "-foo",
                "bar",
                "-snapshotname",
                "s",
                "-storagebase",
                "/dev/null",
                "-testrig",
                "s")));
  }

  @Test
  public void testDoSubmit() {
    QueuedWork qw = getTestQueuedWork();
    BatfishWorkerServiceWorkExecutor we =
        new BatfishWorkerServiceWorkExecutor(
            new BatfishLogger(BatfishLogger.LEVELSTR_ERROR, false),
            Paths.get("/dev/null"),
            BATFISH_WORKER_SERVICE);
    _thrown.expect(UnsupportedOperationException.class);
    _thrown.expectMessage("launched");
    we.doSubmit(qw);
  }

  @Test
  public void testSubmit_success() {
    QueuedWork qw = getTestQueuedWork();
    BatfishWorkerServiceWorkExecutor we =
        new BatfishWorkerServiceWorkExecutor(
            new BatfishLogger(BatfishLogger.LEVELSTR_ERROR, false),
            Paths.get("/dev/null"),
            BATFISH_WORKER_SERVICE_RUNNABLE);
    SubmissionResult sr = we.submit(qw);

    assertThat(sr.getType(), equalTo(SUCCESS));
    assertThat(sr.getTaskHandle().checkTask(), sameInstance(TEST_TASK));
  }

  @Test
  public void testSubmit_exception() {
    QueuedWork qw = getTestQueuedWork();
    BatfishWorkerServiceWorkExecutor we =
        new BatfishWorkerServiceWorkExecutor(
            new BatfishLogger(BatfishLogger.LEVELSTR_ERROR, false),
            Paths.get("/dev/null"),
            BATFISH_WORKER_SERVICE);
    SubmissionResult sr = we.submit(qw);

    assertThat(sr.getType(), equalTo(ERROR));
    assertThat(sr.getMessage(), containsString("Exception submitting work"));
  }

  private static @Nonnull QueuedWork getTestQueuedWork() {
    UUID uuid = UUID.randomUUID();
    WorkItem wi = new WorkItem(uuid, "n", "s", ImmutableMap.of("foo", "bar", "baz", ""));
    WorkDetails wd =
        new WorkDetails(
            new NetworkId("n"), new SnapshotId("s"), false, WorkType.DATAPLANING, null, null);
    return new QueuedWork(wi, wd);
  }

  private static final Task TEST_TASK = new Task(TaskStatus.Unknown);
  private static final BatfishWorkerService BATFISH_WORKER_SERVICE =
      new BatfishWorkerService() {
        @Override
        public @Nullable Task getTaskStatus(String taskId) {
          return TEST_TASK;
        }

        @Override
        public LaunchResult runTask(String taskId, String[] args) {
          throw new UnsupportedOperationException("launched");
        }
      };

  private static final BatfishWorkerService BATFISH_WORKER_SERVICE_RUNNABLE =
      new BatfishWorkerService() {
        @Override
        public @Nullable Task getTaskStatus(String taskId) {
          return TEST_TASK;
        }

        @Override
        public LaunchResult runTask(String taskId, String[] args) {
          return LaunchResult.launched();
        }
      };
}
