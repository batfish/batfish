package org.batfish.coordinator.queues;

import java.util.LinkedList;
import java.util.UUID;

import org.batfish.coordinator.QueuedWork;
import org.batfish.coordinator.WorkQueue;

// we don't synchronize on this queue
// all synchronization is in inside WorkQueueMgr

public class MemoryQueue extends LinkedList<QueuedWork> implements WorkQueue {

   private static final long serialVersionUID = -6556862067531610584L;

   @Override
   public boolean delete(QueuedWork qWork) {
      return remove(qWork);
   }

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