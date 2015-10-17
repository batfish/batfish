package org.batfish.coordinator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BfConsts;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class PoolMgr {

   final class WorkerStatusRefreshTask implements Runnable {
      @Override
      public void run() {
         Main.getPoolMgr().refreshWorkerStatus();
      }
   }

   private Logger _logger;

   // the key should be of the form <ip or hostname>:<port>
   private HashMap<String, WorkerStatus> workerPool;

   public PoolMgr() {
      _logger = Main.initializeLogger();
      workerPool = new HashMap<String, WorkerStatus>();

      Runnable workerStatusRefreshTask = new WorkerStatusRefreshTask();
      Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
            workerStatusRefreshTask, 0,
            Main.getSettings().getPeriodWorkerStatusRefreshMs(),
            TimeUnit.MILLISECONDS);

   }

   public synchronized void addToPool(final String worker) {
      // start out as unknown and trigger refresh in the background
      workerPool.put(worker, new WorkerStatus(WorkerStatus.StatusCode.UNKNOWN));

      Thread thread = new Thread() {
         @Override
         public void run() {
            refreshWorkerStatus(worker);
         }
      };

      thread.start();
   }

   public synchronized void deleteFromPool(String worker) {
      if (workerPool.containsKey(worker)) {
         workerPool.remove(worker);
      }
   }

   private synchronized List<String> getAllWorkers() {
      List<String> workers = new LinkedList<String>();
      for (String worker : workerPool.keySet()) {
         workers.add(worker);
      }
      return workers;
   }

   public synchronized HashMap<String, String> getPoolStatus() {
      HashMap<String, String> copy = new HashMap<String, String>();

      for (Entry<String, WorkerStatus> entry : workerPool.entrySet()) {
         copy.put(entry.getKey(), entry.getValue().toString());
      }

      return copy;
   }

   public synchronized String getWorkerForAssignment() {

      for (Entry<String, WorkerStatus> workerEntry : workerPool.entrySet()) {
         if (workerEntry.getValue().getStatus() == WorkerStatus.StatusCode.IDLE) {
            updateWorkerStatus(workerEntry.getKey(),
                  WorkerStatus.StatusCode.TRYINGTOASSIGN);
            return workerEntry.getKey();
         }
      }

      return null;
   }

   public WorkerStatus getWorkerStatus(String worker) {
      if (workerPool.containsKey(worker)) {
         return workerPool.get(worker);
      }
      else {
         return null;
      }
   }

   public void markAssignmentResult(String worker, boolean assignmentSuccessful) {
      updateWorkerStatus(worker,
            assignmentSuccessful ? WorkerStatus.StatusCode.BUSY
                  : WorkerStatus.StatusCode.IDLE);
   }

   public void refreshWorkerStatus() {
      // _logger.info("PM:RefreshWorkerStatus: entered\n");
      List<String> workers = getAllWorkers();
      for (String worker : workers) {
         refreshWorkerStatus(worker);
      }
   }

   private void refreshWorkerStatus(String worker) {
      // _logger.debug("PM:RefreshWorkerStatus: refreshing status of " + worker
      // +"\n");

      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_GET_STATUS_RSC));
         Invocation.Builder invocationBuilder = webTarget
               .request(MediaType.APPLICATION_JSON);
         Response response = invocationBuilder.get();

         // _logger.debug(webTarget.getUri());

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.error("PM:RefreshWorkerStatus: Got non-OK response "
                  + response.getStatus() + "\n");
         }
         else {
            String sobj = response.readEntity(String.class);
            JSONArray array = new JSONArray(sobj);

            // _logger.info(String.format("response: %s [%s] [%s]\n",
            // array.toString(), array.get(0), array.get(1)));

            if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
               _logger.error(String.format(
                     "got error while refreshing status: %s %s\n",
                     array.get(0), array.get(1)));
               updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
               return;
            }

            JSONObject jObj = new JSONObject(array.get(1).toString());

            if (!jObj.has("idle")) {
               _logger.error(String
                     .format("did not see idle key in json response\n"));
               updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
               return;
            }

            boolean status = jObj.getBoolean("idle");

            // update the status, except leave the ones with TRYINGTOASSIGN
            // alone
            if (getWorkerStatus(worker).getStatus() != WorkerStatus.StatusCode.TRYINGTOASSIGN) {
               updateWorkerStatus(worker, status ? WorkerStatus.StatusCode.IDLE
                     : WorkerStatus.StatusCode.BUSY);
            }
         }
      }
      catch (ProcessingException e) {
         _logger.error(String.format("unable to connect to %s: %s\n", worker,
               e.getMessage()));
         updateWorkerStatus(worker, WorkerStatus.StatusCode.UNREACHABLE);
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error(String.format("exception: %s\n", stackTrace));
         updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
      }
   }

   private synchronized void updateWorkerStatus(String worker,
         WorkerStatus.StatusCode statusCode) {
      if (workerPool.containsKey(worker)) {
         workerPool.get(worker).UpdateStatus(statusCode);
      }
   }
}
