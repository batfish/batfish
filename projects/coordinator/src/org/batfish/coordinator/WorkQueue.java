package org.batfish.coordinator;

import java.util.UUID;

public interface WorkQueue extends Iterable<QueuedWork> {

   public enum Type {
      azure,
      memory
   }

   boolean delete(QueuedWork qWork);

   QueuedWork deque();

   boolean enque(QueuedWork qWork) throws Exception;

   long getLength();

   QueuedWork getWork(UUID workItemId);
}