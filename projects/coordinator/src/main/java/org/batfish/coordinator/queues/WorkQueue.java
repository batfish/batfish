package org.batfish.coordinator.queues;

import java.util.UUID;
import org.batfish.coordinator.QueuedWork;

public interface WorkQueue extends Iterable<QueuedWork> {

  enum Type {
    azure,
    memory
  }

  boolean delete(QueuedWork qWork);

  QueuedWork deque();

  boolean enque(QueuedWork qWork);

  long getLength();

  QueuedWork getWork(UUID workItemId);
}
