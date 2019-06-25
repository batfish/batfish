package org.batfish.coordinator.queues;

import java.util.LinkedList;
import java.util.UUID;
import javax.annotation.Nullable;
import org.batfish.coordinator.QueuedWork;

// we don't synchronize on this queue
// all synchronization is in inside WorkQueueMgr

public class MemoryQueue extends LinkedList<QueuedWork> implements WorkQueue {

  @Override
  public boolean delete(QueuedWork qWork) {
    return remove(qWork);
  }

  @Nullable
  @Override
  public QueuedWork deque() {
    if (size() == 0) {
      return null;
    }

    return pop();
  }

  @Override
  public boolean enque(QueuedWork work) {
    return add(work);
  }

  @Override
  public long getLength() {
    return size();
  }

  @Nullable
  @Override
  public QueuedWork getWork(UUID workItemId) {
    for (QueuedWork work : this) {
      if (work.getWorkItem().getId().equals(workItemId)) {
        return work;
      }
    }

    return null;
  }
}
