package org.batfish.coordinator;

import org.batfish.common.*;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path(CoordinatorConstants.SERVICE_BASE_POOL_MGR)
public class PoolMgrService {

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      return new JSONArray(
            Arrays.asList(
                  "",
                  "Batfish coordinator: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(CoordinatorConstants.SERVICE_POOL_GETSTATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
         try {
                HashMap<String, String> poolStatus = Main.getPoolMgr().getPoolStatus();
                
                JSONObject obj = new JSONObject(poolStatus);
                
                return new JSONArray(Arrays.asList("", obj.toString()));
         }
         catch (Exception e) {
            return new JSONArray(Arrays.asList("failure", e.getMessage()));
         }   
   }      
   
   
   //functions for pool management
   @GET
   @Path(CoordinatorConstants.SERVICE_POOL_UPDATE_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray updatePool(@Context UriInfo ui) {
      try {
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {
            System.out.printf("updatepool: key = %s value = %s\n",
                  entry.getKey(), entry.getValue());

            if (entry.getKey().equals("add")) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for options that have no value
                  if (!worker.equals("")) {
                     Main.getPoolMgr().addToPool(worker);
                  }
               }
            }
            else if (entry.getKey().equals("del")) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for options that have no value
                  if (!worker.equals("")) {
                     Main.getPoolMgr().deleteFromPool(worker);
                  }
               }
            }
            else {
               return new JSONArray(Arrays.asList("failure",
                     "Got unknown command " + entry.getKey()
                           + ". Other commands may have been applied."));
            }
         }
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }

      return new JSONArray(Arrays.asList("", "done"));
   }   
}
