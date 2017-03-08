package org.batfish.coordinator;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Path(CoordConsts.SVC_BASE_POOL_MGR)
public class PoolMgrService {

   BatfishLogger _logger = Main.getLogger();

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      _logger.info("PMS:getInfo\n");
      return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
            "Batfish coordinator v" + Version.getVersion()
                  + ". Enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(CoordConsts.SVC_POOL_GETSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         _logger.info("PMS:getStatus\n");
         HashMap<String, String> poolStatus = Main.getPoolMgr().getPoolStatus();
         JSONObject obj = new JSONObject(poolStatus);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, obj.toString()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("PMS:getStatus exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }

   // functions for pool management
   @GET
   @Path(CoordConsts.SVC_POOL_UPDATE_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray updatePool(@Context UriInfo ui) {
      try {
         _logger.info("PMS:updatePool got " + ui.toString() + "\n");
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         String workerVersion = null;
         List<String> workersToAdd = new LinkedList<>();
         List<String> workersToDelete = new LinkedList<>();

         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {
            _logger.info(String.format("PMS:updatePool: key = %s value = %s\n",
                  entry.getKey(), entry.getValue()));

            if (entry.getKey().equals(CoordConsts.SVC_ADD_WORKER_KEY)) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for keys without values
                  if (!worker.equals("")) {
                     workersToAdd.add(worker);
                  }
               }
            }
            else if (entry.getKey().equals(CoordConsts.SVC_DEL_WORKER_KEY)) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for keys without values
                  if (!worker.equals("")) {
                     workersToDelete.add(worker);
                  }
               }
            }
            else if (entry.getKey().equals(CoordConsts.SVC_VERSION_KEY)) {
               if (entry.getValue().size() > 1) {
                  return new JSONArray(Arrays.asList(
                        CoordConsts.SVC_FAILURE_KEY,
                        "Got " + entry.getValue().size() + " version values"));
               }

               workerVersion = entry.getValue().get(0);
            }

            else {
               return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                     "Got unknown command " + entry.getKey()));
            }
         }

         // we can delete without checking for version
         for (String worker : workersToDelete) {
            Main.getPoolMgr().deleteFromPool(worker);
         }

         if (workersToAdd.size() > 0) {
            if (workerVersion == null) {
               return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                     "Worker version not specified"));
            }
            if (!Version.isCompatibleVersion("Service", "Worker",
                  workerVersion)) {
               return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                     "Worker version " + workerVersion
                           + "is incompatible with coordinator version "
                           + Version.getVersion()));
            }

            for (String worker : workersToAdd) {
               Main.getPoolMgr().addToPool(worker);
            }
         }
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("PMS:updatePool exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }

      return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, "done"));
   }
}
