package org.batfish.coordinator;

import java.util.UUID;

import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.queues.AzureQueue;
import org.batfish.coordinator.queues.MemoryQueue;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

// the design of this WorkQueueMgr is such that all synchronization sits here
// individual queues do not need to be synchronized

public class WorkQueueMgr {

   public enum QueueType {ASSIGNED, UNASSIGNED, COMPLETED}

   private WorkQueue _queueAssignedWork;
   private WorkQueue _queueUnassignedWork;
   private WorkQueue _queueCompletedWork;

   public WorkQueueMgr() {
      if (Main.getSettings().getQueueType() == WorkQueue.Type.azure) {
         String storageConnectionString = String.format(
               "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s", Main
                     .getSettings().getStorageProtocol(), Main.getSettings()
                     .getStorageAccountName(), Main.getSettings()
                     .getStorageAccountKey());

         _queueAssignedWork = new AzureQueue(Main.getSettings()
               .getQueueAssignedWork(), storageConnectionString);
         _queueCompletedWork = new AzureQueue(Main.getSettings()
               .getQueueCompletedWork(), storageConnectionString);
         _queueUnassignedWork = new AzureQueue(Main.getSettings()
               .getQueueUnassignedWork(), storageConnectionString);
      }
      else if (Main.getSettings().getQueueType() == WorkQueue.Type.memory) {
         _queueAssignedWork = new MemoryQueue();
         _queueCompletedWork = new MemoryQueue();
         _queueUnassignedWork = new MemoryQueue();
      }
      else {
         System.err.println("unsupported queue type: "
               + Main.getSettings().getQueueType());
         System.exit(1);
      }
   }
   
   public JSONObject getStatusJson() throws JSONException {
      
      JSONObject jObject = new JSONObject();
      
      jObject.put("unassigned-works", _queueUnassignedWork.getLength()); 
      jObject.put("assigned-works", _queueAssignedWork.getLength()); 
      jObject.put("completed-works", _queueCompletedWork.getLength()); 
      
      return jObject;
   }

   public synchronized boolean queueUnassignedWork(QueuedWork work) throws Exception {
      
      QueuedWork previouslyQueuedWork = getWork(work.getId());
      
      if (previouslyQueuedWork != null) {
         throw new Exception("Duplicate id for work");
      }
      
      return _queueUnassignedWork.enque(work);
   }
   
   public synchronized QueuedWork getWork(UUID workId) {
      //first, look for the work item in the assigned queue (most likely to be found there?)
      //if not found there, in the unassigned queue
      //if still not found, in the completed queue
      QueuedWork work = getWork(workId, QueueType.ASSIGNED);
      if (work == null) {
         work = getWork(workId, QueueType.UNASSIGNED);
         if (work == null) {
            work = getWork(workId, QueueType.COMPLETED);
         }
      }
      
      return work;
   }
   
   private synchronized QueuedWork getWork(UUID workId, QueueType qType) {
      switch (qType) {
         case ASSIGNED:
            return _queueAssignedWork.getWork(workId);
         case COMPLETED:
            return _queueCompletedWork.getWork(workId);
         case UNASSIGNED:
            return _queueUnassignedWork.getWork(workId);     
      }            
      return null;
   }

   public synchronized long getLength(QueueType qType) {
      switch (qType) {
      case ASSIGNED:
         return _queueAssignedWork.getLength();
      case COMPLETED:
         return _queueCompletedWork.getLength();
      case UNASSIGNED:
         return _queueUnassignedWork.getLength();
      }
      return -1;
   }

}