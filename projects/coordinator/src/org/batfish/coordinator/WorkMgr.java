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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WorkMgr {
   
   private WorkQueueMgr _workQueueMgr;
   
   public WorkMgr() {

      _workQueueMgr = new WorkQueueMgr();
      
      Runnable assignWorkTask = new AssignWorkTask(this);
      Executors.newScheduledThreadPool(1)
            .scheduleWithFixedDelay(assignWorkTask, 0, Main.getSettings().getPeriodAssignWorkMs(), TimeUnit.MILLISECONDS);

      Runnable checkWorkTask = new CheckWorkTask(this);
      Executors.newScheduledThreadPool(1)
            .scheduleWithFixedDelay(checkWorkTask, 0, Main.getSettings().getPeriodCheckWorkMs(), TimeUnit.MILLISECONDS);
   }
   
   public JSONObject getWorkQueueStatusJson() throws JSONException {
      return _workQueueMgr.getStatusJson();
   }
   
   public boolean queueWork(WorkItem workItem) throws Exception {
      
      QueuedWork work = new QueuedWork(workItem);

      boolean success = _workQueueMgr.queueUnassignedWork(work);                                      
      
      //if this was the only job on the queue, trigger AssignWork on another thread
      if (success &&  _workQueueMgr.getLength(WorkQueueMgr.QueueType.UNASSIGNED) == 1) {
         
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

//      boolean assigned = false;
//      
//      QueuedWork work = _queueUnassignedWork.deque();
//      String idleWorker = Main.getPoolMgr().getIdleWorker();
//
//      if (work != null && idleWorker != null) {
//         assigned = AssignWork(work, idleWorker);
//      }
//      else if (work == null) {
//         System.out.println("AssignWork: No unassigned work");
//         return;         
//      }
//      else if (idleWorker == null) {
//         System.out.println("AssignWork: No idle worker");         
//         return;                  
//      }
//         
//      //if we didn't succeed, return the work to the queue and the worker to the pool manager
//      if (!assigned) {
//         if (work != null) {
//            _queueUnassignedWork.enque(work);
//         }
//         
//      }
//      
   }

   private void CheckWork() {
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

   public QueuedWork getWork(UUID workItemId) {
      return _workQueueMgr.getWork(workItemId);
   }

   public File getObject(String objectName) {
      File file = new File(Main.getSettings().getTestrigStorageLocation() + "/" + objectName);
      
      if (file.isFile()) {
         return file;
      }

      return null;
   }   
   
   final class AssignWorkTask implements Runnable {
      
      private WorkMgr _workMgr;
      
      public AssignWorkTask(WorkMgr workMgr) {
         _workMgr = workMgr;
      }
      
      @Override
      public void run() {
         System.out.println(new Date() + " Assigning work");
         _workMgr.AssignWork();
      }
   }

   final class CheckWorkTask implements Runnable {
      
      private WorkMgr _workMgr;
      
      public CheckWorkTask(WorkMgr workMgr) {
         _workMgr = workMgr;
      }
      
      @Override
      public void run() {
         System.out.println(new Date() + " Checking work");
         _workMgr.CheckWork();
      }
   }
 }


