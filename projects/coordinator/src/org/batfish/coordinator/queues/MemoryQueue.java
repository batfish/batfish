package org.batfish.coordinator.queues;

import java.util.LinkedList;

import org.batfish.coordinator.WorkItem;
import org.batfish.coordinator.WorkQueue;

public class MemoryQueue implements WorkQueue {
   
   private LinkedList<WorkItem> _queue;

   public MemoryQueue() {
         _queue = new LinkedList<WorkItem>();
   }
   
   public long getLength() {
      return _queue.size();
   }
}