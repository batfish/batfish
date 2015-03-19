package org.batfish.coordinator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.batfish.common.BatfishConstants;
import org.batfish.common.WorkItem;
import org.batfish.common.BatfishConstants.WorkStatus;
import org.batfish.coordinator.queues.AzureQueue;
import org.batfish.coordinator.queues.MemoryQueue;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class WorkMgr {
   
   private WorkQueue _queueAssignedWork;
   private WorkQueue _queueUnassignedWork;
   private WorkQueue _queueCompletedWork;   
   
   public WorkMgr() {
      
      if (Main.getSettings().getQueueType() == WorkQueue.Type.azure) {
         String storageConnectionString = String.format(
               "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s",
               Main.getSettings().getStorageProtocol(), 
               Main.getSettings().getStorageAccountName(),
               Main.getSettings().getStorageAccountKey());

         _queueAssignedWork = new AzureQueue(Main.getSettings().getQueueAssignedWork(),
               storageConnectionString);
         _queueCompletedWork = new AzureQueue(Main.getSettings().getQueueCompletedWork(),
               storageConnectionString);
         _queueUnassignedWork = new AzureQueue(
               Main.getSettings().getQueueUnassignedWork(), storageConnectionString);
      }
      else if (Main.getSettings().getQueueType() == WorkQueue.Type.memory) {
         _queueAssignedWork = new MemoryQueue();
         _queueCompletedWork = new MemoryQueue();
         _queueUnassignedWork = new MemoryQueue();       
      }
      else {
         System.err.println("unsupported queue type: " + Main.getSettings().getQueueType());
         System.exit(1);
      }                  
   }
      
   public String getWorkStatus() {
    
      String retString = "";
      
      retString += "Length of unassigned work queue = " + _queueUnassignedWork.getLength() + "\n";
      retString += "Length of assigned work queue = " + _queueAssignedWork.getLength() + "\n";
      retString += "Length of completed work queue = " + _queueCompletedWork.getLength();
      
      return retString;
   }
   
   public boolean queueWork(WorkItem workItem) throws Exception {
      QueuedWork work = new QueuedWork(workItem);
      boolean success = _queueUnassignedWork.enque(work);
      
      //if this was the only job on the queue, trigger AssignWork on another thread
      if (success &&  _queueUnassignedWork.getLength() == 1) {
         
         Thread thread = new Thread() {
            public void run() {
               AssignWork();
            }
         };

         thread.start();
      }
      
      return success;
   }


   private void AssignWork() {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }


   public void uploadTestrig(String name, InputStream fileStream) throws Exception {

      File testrigDir = new File(Main.getSettings().getTestrigStorageLocation() + "/" + name);

      if (testrigDir.exists()) {
         throw new Exception("test rig with the same name exists");
      }

      if (!testrigDir.mkdirs()) {
         throw new Exception("failed to create directory "
               + testrigDir.getAbsolutePath());
      }

      try (OutputStream fileOutputStream = new FileOutputStream(
            testrigDir.getAbsolutePath() + "/" + name + ".zip")) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }
   }

   public WorkItem getWorkItem(UUID workItemId) {
       return _queueUnassignedWork.getWork(workItemId).getWorkItem();
   }

   public File getObject(String objectName) {
      File file = new File(Main.getSettings().getTestrigStorageLocation() + "/" + objectName);
      
      if (file.isFile()) {
         return file;
      }

      return null;
   }   
 }