package org.batfish.coordinator;

import java.util.ArrayList;
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

import org.batfish.common.*;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Path(CoordinatorConstants.SERVICE_BASE)
public class Service {

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      return new JSONArray(
            Arrays.asList(
                  "",
                  "Batfish coordinator: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(CoordinatorConstants.SERVICE_GETSTATUS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      return new JSONArray(Arrays.asList("", Main.getCoordinator().getWorkStatus()));
   }
   
   //functions for pool management
   @GET
   @Path(CoordinatorConstants.SERVICE_UPDATEPOOL)
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
                     Main.getCoordinator().addToPool(worker);
                  }
               }
            }
            else if (entry.getKey().equals("del")) {
               for (String worker : entry.getValue()) {
                  // don't add empty values; occurs for options that have no value
                  if (!worker.equals("")) {
                     Main.getCoordinator().deleteFromPool(worker);
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
   
   @GET
   @Path(CoordinatorConstants.SERVICE_GETPOOLSTATUS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getPoolStatus() {
         try {
                HashMap<String, String> poolStatus = Main.getCoordinator().getPoolStatus();
                
                JSONObject obj = new JSONObject(poolStatus);
                
                return new JSONArray(Arrays.asList("", obj.toString()));
         }
         catch (Exception e) {
            return new JSONArray(Arrays.asList("failure", e.getMessage()));
         }   
   }      
   
   @GET
   @Path(CoordinatorConstants.SERVICE_QUEUE_WORK)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(@Context UriInfo ui) {
         try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

            WorkItem workItem = new WorkItem();
            
            for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
                  .entrySet()) {
                  
               if (entry.getKey().equals("id")) {
                  workItem.setId(entry.getValue().get(0));
               }
               else {                  
                  workItem.addRequestParam(entry.getKey(), entry.getValue().get(0));
               }
               
            }
             
            boolean result = Main.getCoordinator().queueWork(workItem);
                  
            return new JSONArray(Arrays.asList("", result));
         }
         catch (Exception e) {
            return new JSONArray(Arrays.asList("failure", e.getMessage()));
         }   
   }      

}
