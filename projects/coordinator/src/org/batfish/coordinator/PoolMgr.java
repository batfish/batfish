package org.batfish.coordinator;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class PoolMgr {
   
   //the key should be of the form <ip or hostname>:<port>
   private HashMap<String,WorkerStatus> workerPool;
   
   public PoolMgr() {
      
      workerPool = new HashMap<String, WorkerStatus>();
      
      Runnable workerStatusRefreshTask = new WorkerStatusRefreshTask(this);
      ScheduledFuture<?> future = Executors.newScheduledThreadPool(1)
            .scheduleWithFixedDelay(workerStatusRefreshTask, 0, Main.getSettings().getPeriodWorkerStatusRefreshMs(), TimeUnit.MILLISECONDS);
      
   }
      
   public synchronized void addToPool(final String worker) {
      //start out as unknown and trigger refresh in the background
      workerPool.put(worker, new WorkerStatus(WorkerStatus.StatusCode.UNKNOWN));
      
      Thread thread = new Thread() {
         public void run() {
            RefreshWorkerStatus(worker);
         }
      };

      thread.start();
   }

   public synchronized void deleteFromPool(String worker) {
      if (workerPool.containsKey(worker)) {
         workerPool.remove(worker);
      }
   }

   private synchronized void updateWorkerStatus(String worker, WorkerStatus.StatusCode statusCode) {
      if (workerPool.containsKey(worker)) {
         workerPool.get(worker).UpdateStatus(statusCode);
      }
   }

   public synchronized HashMap<String, String> getPoolStatus() {
      HashMap<String, String> copy = new HashMap<String, String> ();

      for (Entry<String, WorkerStatus> entry : workerPool.entrySet()) {
           copy.put(entry.getKey(), entry.getValue().toString());
      }
      
     return copy;
   }

   private synchronized List<String> getAllWorkers() {
      List<String> workers = new LinkedList<String>();
      for (String worker : workerPool.keySet()) {
          workers.add(worker);
      }
      return workers;
   }
   
   public void RefreshWorkerStatus() {
      List<String> workers = getAllWorkers();
      for (String worker : workers) {
         RefreshWorkerStatus(worker);
      }      
   }
   
   public WorkerStatus getWorkerStatus(String worker) {
      if (workerPool.containsKey(worker))
         return workerPool.get(worker);
      else 
         return null;      
   }

   private void RefreshWorkerStatus(String worker) {
      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               worker, BatfishConstants.SERVICE_BASE_RESOURCE,
               BatfishConstants.SERVICE_GETSTATUS_RESOURCE));
         Invocation.Builder invocationBuilder = webTarget
               .request(MediaType.APPLICATION_JSON);
         Response response = invocationBuilder.get();

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals("")) {
            System.err.printf("got error while refreshing status: %s %s\n",
                  array.get(0), array.get(1));
            updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
            return;
         }

         JSONObject jObj = new JSONObject(array.get(1).toString());
         
         if (!jObj.has("idle")) {
            System.err.printf("did not see idle key in json response\n");
            updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
            return;            
         }
         
         boolean status = jObj.getBoolean("idle");
         
         //update the status, except leave the ones with TRYINGTOASSIGN alone
         if (getWorkerStatus(worker).getStatus() != WorkerStatus.StatusCode.TRYINGTOASSIGN) {
            updateWorkerStatus(worker, 
                  status? WorkerStatus.StatusCode.IDLE : WorkerStatus.StatusCode.BUSY);
         }
      }
      catch (ProcessingException e) {
         System.err.printf("unable to connect to %s: %s\n", worker, e.getStackTrace().toString());
         updateWorkerStatus(worker, WorkerStatus.StatusCode.UNREACHABLE);         
      }
      catch (Exception e) {
         System.err.printf("exception: %s\n", e.getStackTrace().toString());
         updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
      }
   }

   public synchronized String getWorkerForAssignment() {
      
      for (Entry<String,WorkerStatus> workerEntry : workerPool.entrySet()) {
         if (workerEntry.getValue().getStatus() == WorkerStatus.StatusCode.IDLE) {            
            updateWorkerStatus(workerEntry.getKey(), WorkerStatus.StatusCode.TRYINGTOASSIGN);            
            return workerEntry.getKey();
         }
      }
      
      return null;
   }

   final class WorkerStatusRefreshTask implements Runnable {

      PoolMgr _coordinator;

      public WorkerStatusRefreshTask(PoolMgr coordinator) {
         _coordinator = coordinator;
      }

      @Override
      public void run() {
         System.out.println(new Date() + " Firing work status refresh");
         _coordinator.RefreshWorkerStatus();
      }
   }

   public void markAssignmentResult(String worker, boolean assignmentSuccessful) {
         updateWorkerStatus(worker, 
               assignmentSuccessful? WorkerStatus.StatusCode.BUSY : WorkerStatus.StatusCode.IDLE);     
   }
}
