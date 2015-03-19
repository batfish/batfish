package org.batfish.coordinator.queues;

import java.util.LinkedList;
import java.util.UUID;

import org.batfish.coordinator.QueuedWork;
import org.batfish.coordinator.WorkQueue;

// we don't synchronize on this queue
// all synchronization is in inside WorkQueueMgr

public class MemoryQueue implements WorkQueue {
   
   private LinkedList<QueuedWork> _queue;

   public MemoryQueue() {
         _queue = new LinkedList<QueuedWork>();
   }
   
   @Override
   public long getLength() {
      return _queue.size();
   }

   @Override
   public boolean enque(QueuedWork work) {
      return _queue.add(work);
   }

   @Override
   public QueuedWork getWork(UUID workItemId) {
      
      for (QueuedWork work : _queue) {
         if (work.getWorkItem().getId().equals(workItemId)) {
            return work;
         }
      }    
      
      return null;
   }

   @Override
   public QueuedWork deque() {
      if (_queue.size() == 0) 
         return null;
      
      return _queue.pop();
   }
}