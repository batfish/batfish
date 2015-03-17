package org.batfish.coordinator.queues;

import java.util.LinkedList;
import java.util.UUID;

import org.batfish.common.WorkItem;
import org.batfish.coordinator.WorkQueue;

public class MemoryQueue implements WorkQueue {
   
   private LinkedList<WorkItem> _queue;

   public MemoryQueue() {
         _queue = new LinkedList<WorkItem>();
   }
   
   public long getLength() {
      return _queue.size();
   }

   @Override
   public synchronized boolean enque(WorkItem workItem) throws Exception {
      
      if (getWorkItem(workItem.getId()) != null) {
         throw new Exception("Attempt to insert a duplicate work item!");
      }
      
      return _queue.add(workItem);
   }

   @Override
   public synchronized WorkItem getWorkItem(UUID workItemId) {
      
      for (WorkItem wItem : _queue) {
         if (wItem.getId().equals(workItemId)) {
            return wItem;
         }
      }    
      
      return null;
   }
}