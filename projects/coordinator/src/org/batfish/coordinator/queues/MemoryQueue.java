package org.batfish.coordinator.queues;

import java.util.LinkedList;
import java.util.UUID;

import org.batfish.coordinator.QueuedWork;
import org.batfish.coordinator.WorkQueue;

public class MemoryQueue implements WorkQueue {
   
   private LinkedList<QueuedWork> _queue;

   public MemoryQueue() {
         _queue = new LinkedList<QueuedWork>();
   }
   
   public long getLength() {
      return _queue.size();
   }

   public synchronized boolean enque(QueuedWork work) throws Exception {
      
      if (getWork(work.getWorkItem().getId()) != null) {
         throw new Exception("Attempt to insert a duplicate work item!");
      }
      
      return _queue.add(work);
   }

   public synchronized QueuedWork getWork(UUID workItemId) {
      
      for (QueuedWork work : _queue) {
         if (work.getWorkItem().getId().equals(workItemId)) {
            return work;
         }
      }    
      
      return null;
   }
}