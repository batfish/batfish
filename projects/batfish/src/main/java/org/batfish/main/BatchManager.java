package org.batfish.main;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  synchronized void logTask(String taskId, Task task) throws Exception {
    if (_taskLog.containsKey(taskId)) {
      throw new Exception("duplicate UUID for task");
    } else {
      _taskLog.put(taskId, task);
    }
  }
}
