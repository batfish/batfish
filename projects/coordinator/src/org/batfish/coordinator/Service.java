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

@Path(CoordinatorConstants.SERVICE_BASE_RESOURCE)
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
   @Path(CoordinatorConstants.SERVICE_GETSTATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      return new JSONArray(Arrays.asList("", Main.getCoordinator().getWorkStatus()));
   }
   
   //functions for pool management
   @GET
   @Path(CoordinatorConstants.SERVICE_UPDATEPOOL_RESOURCE)
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
   @Path(CoordinatorConstants.SERVICE_GETPOOLSTATUS_RESOURCE)
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
   @Path(CoordinatorConstants.SERVICE_QUEUE_WORK_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(@Context UriInfo ui) {
         try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

            for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
                  .entrySet()) {
                  
               if (entry.getKey().equals(CoordinatorConstants.SERVICE_QUEUE_WORK_PATH)) {
                  System.out.printf("work: %s\n", entry.getValue());

                  WorkItem workItem = new WorkItem(entry.getValue().get(0));

                  boolean result = Main.getCoordinator().queueWork(workItem);
                  
                  return new JSONArray(Arrays.asList("", result));
               }
               else {
                  System.out.println("Unknown key in work: " + entry.getKey());
               }
            }
            
            return new JSONArray(Arrays.asList("failure", "work not found"));            
             
         }
         catch (Exception e) {
            return new JSONArray(Arrays.asList("failure", e.getMessage()));
         }   
   }      

   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_STATUS_CHECK_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray workStatusCheck(@Context UriInfo ui) {
      try {
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {

            if (entry.getKey().equals(
                  CoordinatorConstants.SERVICE_WORK_STATUS_CHECK_PATH)) {
               System.out.printf("workid: %s\n", entry.getValue());

               WorkItem wItem = Main.getCoordinator().getWorkItem(
                     UUID.fromString(entry.getValue().get(0)));

               if (wItem == null) {
                  return new JSONArray(Arrays.asList("failure",
                        "work item not found"));
               }
               else {
                  return new JSONArray(Arrays.asList("", wItem.toJsonString()));
               }
            }
            else {
               System.out.println("Unknown key in work status check: "
                     + entry.getKey());
            }
         }

         return new JSONArray(Arrays.asList("failure",
               "work status check path not found"));

      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @POST
   @Path(CoordinatorConstants.SERVICE_UPLOAD_TESTRIG_RESOURCE)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadTestRig(
         @FormDataParam(CoordinatorConstants.SERVICE_TESTRIG_NAME_KEY) String name,
         @FormDataParam(CoordinatorConstants.SERVICE_TESTRIG_ZIPFILE_KEY) InputStream fileStream) {
      try {

         Main.getCoordinator().uploadTestrig(name, fileStream);

         return new JSONArray(
               Arrays.asList("", "successfully uploaded testrig"));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @GET
   @Path(CoordinatorConstants.SERVICE_GET_OBJECT_RESOURCE)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getObject(@Context UriInfo ui) {
      try {
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {

            if (entry.getKey().equals(
                  CoordinatorConstants.SERVICE_GET_OBJECT_PATH)) {
               System.out.printf("object: %s\n", entry.getValue());

               String objectName = entry.getValue().get(0);
               
               File file = Main.getCoordinator().getObject(objectName);

               if (file == null) {
                  return Response.status(Response.Status.NOT_FOUND)
                        .entity("File not found")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
               }

               return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                     .header("Content-Disposition", "attachment; filename=\"" + objectName + "\"")
                     .build();
            }
            else {
               System.out.println("Unknown key in work status check: "
                     + entry.getKey());
            }
         }

         //object not specified
         return Response.status(Response.Status.BAD_REQUEST)
               .entity("Object not specified")
               .type(MediaType.TEXT_PLAIN)
               .build();
      }
      catch (Exception e) {
         e.printStackTrace();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .entity(e.getCause())
               .type(MediaType.TEXT_PLAIN)
               .build();
      }
   }
}
