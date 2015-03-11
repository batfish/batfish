package org.batfish.coordinator;

import java.util.HashMap;
import java.util.List;

public class Coordinator {

   public enum WorkerStatus { IDLE, BUSY, UNREACHABLE }
   
   //this needs to be generalized to host things elsewhere
   private static final boolean UseAzureQueues = true;
   
   private WorkQueue _queueAssignedWork;
   private WorkQueue _queueUnassignedWork;
   private WorkQueue _queueCompletedWork;   
   private Settings _settings;
   
   private HashMap<String,String> workerPool;
   
   public Coordinator(Settings settings) {
      
      this._settings = settings;
      
      if (UseAzureQueues) {
         String storageConnectionString = String.format(
               "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s",
               settings.getStorageProtocol(), settings.getStorageAccountName(),
               settings.getStorageAccountKey());

         _queueAssignedWork = new AzureQueue(settings.getQueueAssignedWork(),
               storageConnectionString);
         _queueCompletedWork = new AzureQueue(settings.getQueueCompletedWork(),
               storageConnectionString);
         _queueUnassignedWork = new AzureQueue(
               settings.getQueueUnassignedWork(), storageConnectionString);
      }
      
      workerPool = new HashMap<String, String>();
   }
      
   public String getWorkStatus() {
    
      String retString = "";
      
      retString += "Length of unassigned work queue = " + _queueUnassignedWork.getLength() + "\n";
      retString += "Length of assigned work queue = " + _queueAssignedWork.getLength() + "\n";
      retString += "Length of completed work queue = " + _queueCompletedWork.getLength();
      
      return retString;
   }
   
   public void run() {
      try {
      Thread.sleep(5 * 60 * 1000);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public synchronized void addToPool(String worker) {
      // TODO: actually poll for status instead of assuming idle
      workerPool.put(worker, WorkerStatus.IDLE.toString());
   }

   public synchronized void deleteFromPool(String worker) {
      if (workerPool.containsKey(worker)) {
         workerPool.remove(worker);
      }
   }

   public synchronized HashMap<String, String> getPoolStatus() {
      HashMap<String, String> copy = new HashMap<String, String> (workerPool);
      return copy;
   }
}