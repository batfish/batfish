package org.batfish.coordinator;

import java.util.UUID;

import org.batfish.common.WorkItem;

public interface WorkQueue {   
   
   public enum Type {azure, memory}
   
   long getLength();

   boolean enque(QueuedWork qWork) throws Exception;

   QueuedWork getWork(UUID workItemId);   
}