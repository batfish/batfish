package org.batfish.coordinator;

import java.util.UUID;

import org.batfish.common.CoordinatorConstants.WorkStatusCode;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.queues.AzureQueue;
import org.batfish.coordinator.queues.MemoryQueue;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

// the design of this WorkQueueMgr is such that all synchronization sits here
// individual queues do not need to be synchronized

public class WorkQueueMgr {

   public enum QueueType {INCOMPLETE, COMPLETED}

   private WorkQueue _queueIncompleteWork;
   private WorkQueue _queueCompletedWork;

   public WorkQueueMgr() {
      if (Main.getSettings().getQueueType() == WorkQueue.Type.azure) {
         String storageConnectionString = String.format(
               "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s", Main
                     .getSettings().getStorageProtocol(), Main.getSettings()
                     .getStorageAccountName(), Main.getSettings()
                     .getStorageAccountKey());

         _queueCompletedWork = new AzureQueue(Main.getSettings()
               .getQueueCompletedWork(), storageConnectionString);
         _queueIncompleteWork = new AzureQueue(Main.getSettings()
               .getQueueIncompleteWork(), storageConnectionString);
      }
      else if (Main.getSettings().getQueueType() == WorkQueue.Type.memory) {
         _queueCompletedWork = new MemoryQueue();
         _queueIncompleteWork = new MemoryQueue();
      }
      else {
         System.err.println("unsupported queue type: "
               + Main.getSettings().getQueueType());
         System.exit(1);
      }
   }
   
   public JSONObject getStatusJson() throws JSONException {
      
      JSONObject jObject = new JSONObject();
      
      jObject.put("incomplete-works", _queueIncompleteWork.getLength()); 
      jObject.put("completed-works", _queueCompletedWork.getLength()); 
      
      return jObject;
   }

   public synchronized boolean queueUnassignedWork(QueuedWork work) throws Exception {
      
      QueuedWork previouslyQueuedWork = getWork(work.getId());
      
      if (previouslyQueuedWork != null) {
         throw new Exception("Duplicate id for work");
      }
      
      return _queueIncompleteWork.enque(work);
   }
   
   public synchronized QueuedWork getWork(UUID workId) {
      QueuedWork work = getWork(workId, QueueType.INCOMPLETE);
      if (work == null) {
         work = getWork(workId, QueueType.COMPLETED);
      }      
      return work;
   }
   
   private synchronized QueuedWork getWork(UUID workId, QueueType qType) {
      switch (qType) {
         case COMPLETED:
            return _queueCompletedWork.getWork(workId);
         case INCOMPLETE:
            return _queueIncompleteWork.getWork(workId);     
      }            
      return null;
   }

   public synchronized long getLength(QueueType qType) {
      switch (qType) {
      case COMPLETED:
         return _queueCompletedWork.getLength();
      case INCOMPLETE:
         return _queueIncompleteWork.getLength();
      }
      return -1;
   }

   public synchronized QueuedWork getWorkForAssignment() {
      
      for (QueuedWork work : _queueIncompleteWork) {
         if (work.getStatus() == WorkStatusCode.UNASSIGNED) {
            work.setStatus(WorkStatusCode.TRYINGTOASSIGN);
            return work;
         }         
      }
      
      return null;
   }
 
   public QueuedWork getWorkForChecking() {
      
      for (QueuedWork work : _queueIncompleteWork) {
         if (work.getStatus() == WorkStatusCode.ASSIGNED) {
            work.setStatus(WorkStatusCode.CHECKINGTERMINATION);
            return work;
         }         
      }
      
      return null;
   }
   
   public synchronized void markAssignmentResult(QueuedWork work, boolean assignmentSuccessful) {
         work.setStatus(assignmentSuccessful? WorkStatusCode.ASSIGNED : WorkStatusCode.UNASSIGNED);
   }
   
   public synchronized void makeWorkUnassigned(QueuedWork work) {
      work.setStatus(WorkStatusCode.UNASSIGNED);
   }
}
 