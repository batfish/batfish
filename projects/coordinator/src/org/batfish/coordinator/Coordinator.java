package org.batfish.coordinator;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.batfish.common.BatfishServiceConstants;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.queues.AzureQueue;
import org.batfish.coordinator.queues.MemoryQueue;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class Coordinator {
   
   private WorkQueue _queueAssignedWork;
   private WorkQueue _queueUnassignedWork;
   private WorkQueue _queueCompletedWork;   
   private Settings _settings;
   
   //the key should be of the form <ip or hostname>:<port>
   private HashMap<String,WorkerStatus> workerPool;
   
   public Coordinator(Settings settings) {
      
      this._settings = settings;
      
      if (_settings.getQueueType() == WorkQueue.Type.azure) {
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
      else if (_settings.getQueueType() == WorkQueue.Type.memory) {
         _queueAssignedWork = new MemoryQueue();
         _queueCompletedWork = new MemoryQueue();
         _queueUnassignedWork = new MemoryQueue();       
      }
      else {
         System.err.println("unsupported queue type: " + _settings.getQueueType());
         System.exit(1);
      }
      
      
      workerPool = new HashMap<String, WorkerStatus>();
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
   
   private void RefreshWorkerStatus(String worker) {
      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               worker, BatfishServiceConstants.SERVICE_BASE,
               BatfishServiceConstants.SERVICE_GETSTATUS));
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
         
         if (status)
            updateWorkerStatus(worker, WorkerStatus.StatusCode.IDLE);
         else 
            updateWorkerStatus(worker, WorkerStatus.StatusCode.BUSY);
         
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

   public boolean queueWork(WorkItem workItem) {
      return _queueUnassignedWork.enque(workItem);
   }
 }