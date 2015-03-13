package org.batfish.coordinator;

import org.batfish.common.WorkItem;

public interface WorkQueue {   
   
   public enum Type {azure, memory}
   
   long getLength();

   boolean enque(WorkItem workItem);   
}