package org.batfish.coordinator;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.batfish.common.*;

import java.util.Arrays;
import java.util.HashMap;
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

   Logger _logger = Main.initializeLogger();

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      _logger.info("PMS:getInfo\n");
      return new JSONArray(
            Arrays.asList(
                  CoordConsts.SVC_SUCCESS_KEY,
                  "Batfish coordinator: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(CoordConsts.SVC_POOL_GETSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         _logger.info("PMS:getStatus\n");
         HashMap<String, String> poolStatus = Main.getPoolMgr().getPoolStatus();
         JSONObject obj = new JSONObject(poolStatus);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               obj.toString()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("PMS:getStatus exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }

   // functions for pool management
   @GET
   @Path(CoordConsts.SVC_POOL_UPDATE_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray updatePool(@Context UriInfo ui) {
      try {
         _logger.info("PMS:updatePool " + ui.toString() + "\n");
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {
            _logger.info(String.format("PMS:updatePool: key = %s value = %s\n",
                  entry.getKey(), entry.getValue()));

            if (entry.getKey().equals("add")) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for options that have no
                  // value
                  if (!worker.equals("")) {
                     Main.getPoolMgr().addToPool(worker);
                  }
               }
            }
            else if (entry.getKey().equals("del")) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for options that have no
                  // value
                  if (!worker.equals("")) {
                     Main.getPoolMgr().deleteFromPool(worker);
                  }
               }
            }
            else {
               return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                     "Got unknown command " + entry.getKey()
                           + ". Other commands may have been applied."));
            }
         }
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("PMS:updatePool exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }

      return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, "done"));
   }
}
