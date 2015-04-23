package org.batfish.coordinator;

import java.util.UUID;

public interface WorkQueue extends Iterable<QueuedWork> {   
   
   public enum Type {azure, memory}
   
   long getLength();

   boolean enque(QueuedWork qWork) throws Exception;

   boolean delete(QueuedWork qWork);
   
   QueuedWork deque();
   
   QueuedWork getWork(UUID workItemId);   
}