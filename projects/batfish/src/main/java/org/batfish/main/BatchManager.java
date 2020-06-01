package org.batfish.main;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.Task;
import org.batfish.common.Task.Batch;
import org.batfish.config.Settings;

/** Helps manage task and batch creation within a Batfish worker */
public final class BatchManager {

  private final ConcurrentMap<String, Task> _taskLog;
  @Nonnull private static BatchManager _instance = new BatchManager();

  private BatchManager() {
    _taskLog = new ConcurrentHashMap<>();
  }

  @Nonnull
  public static BatchManager get() {
    return _instance;
  }

  @Nullable
  private synchronized Task getTask(Settings settings) {
    String taskId = settings.getTaskId();
    if (taskId == null) {
      return null;
    } else {
      return _taskLog.get(taskId);
    }
  }

  public synchronized AtomicInteger newBatch(Settings settings, String description, int jobs) {
    Batch batch = null;
    Task task = getTask(settings);
    if (task != null) {
      batch = task.newBatch(description);
      batch.setSize(jobs);
      return batch.getCompleted();
    } else {
      return new AtomicInteger();
    }
  }

  @Nullable
  public synchronized Task getTaskFromLog(String taskId) {
    return _taskLog.get(taskId);
  }

  public synchronized Task killTask(String taskId) {
    Task task = _taskLog.get(taskId);
    if (task == null) {
      throw new BatfishException("Task with provided id not found: " + taskId);
    } else if (task.getStatus().isTerminated()) {
      throw new BatfishException("Task with provided id already terminated " + taskId);
    } else {
      // update task details in case a new query for status check comes in
      task.newBatch("Got kill request");
      task.setStatus(TaskStatus.TerminatedByUser);
      task.setTerminated(new Date());
      task.setErrMessage("Terminated by user");

      // we die after a little bit, to allow for the response making it back to the coordinator
      new java.util.Timer()
          .schedule(
              new java.util.TimerTask() {
                @Override
                public void run() {
                  System.exit(0);
                }
              },
              3000);

      return task;
    }
  }

  synchronized void logTask(String taskId, Task task) throws Exception {
    if (_taskLog.containsKey(taskId)) {
      throw new Exception("duplicate UUID for task");
    } else {
      _taskLog.put(taskId, task);
    }
  }
}
