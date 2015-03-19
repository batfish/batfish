package org.batfish.coordinator;

import org.batfish.common.*;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path(CoordinatorConstants.SERVICE_BASE_WORK_MGR)
public class WorkMgrService {

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      return new JSONArray(
            Arrays.asList(
                  "",
                  "Batfish coordinator: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_GET_WORK_QUEUE_STATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkQueueStatus() {
      try {
         return new JSONArray(Arrays.asList("", Main.getWorkMgr()
               .getWorkQueueStatusJson()));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }
   
   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_QUEUE_WORK_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(@Context UriInfo ui) {
         try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

            for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
                  .entrySet()) {
                  
               if (entry.getKey().equals(CoordinatorConstants.SERVICE_WORK_QUEUE_WORK_PATH)) {
                  System.out.printf("work: %s\n", entry.getValue());

                  WorkItem workItem = new WorkItem(entry.getValue().get(0));

                  boolean result = Main.getWorkMgr().queueWork(workItem);
                  
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
   @Path(CoordinatorConstants.SERVICE_WORK_GET_WORK_STATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkStatus(@QueryParam(CoordinatorConstants.SERVICE_WORKID_KEY) String workId) {
      try {

         if (workId == null || workId.equals("")) {
            return new JSONArray(Arrays.asList("failure", "workid not supplied"));            
         }
         
         QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

         if (work == null) {
            return new JSONArray(Arrays.asList("failure",
                  "work with the specified id not found"));
         }
         else {
            return new JSONArray(
                  Arrays.asList("", (new JSONObject().put("status", work
                        .getStatus().toString()))));
         }
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @POST
   @Path(CoordinatorConstants.SERVICE_WORK_UPLOAD_TESTRIG_RESOURCE)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadTestRig(
         @FormDataParam(CoordinatorConstants.SERVICE_TESTRIG_NAME_KEY) String name,
         @FormDataParam(CoordinatorConstants.SERVICE_TESTRIG_ZIPFILE_KEY) InputStream fileStream) {
      try {

         Main.getWorkMgr().uploadTestrig(name, fileStream);

         return new JSONArray(
               Arrays.asList("", "successfully uploaded testrig"));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_GET_OBJECT_RESOURCE)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getObject(@Context UriInfo ui) {
      try {
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {

            if (entry.getKey().equals(
                  CoordinatorConstants.SERVICE_WORK_GET_OBJECT_PATH)) {
               System.out.printf("object: %s\n", entry.getValue());

               String objectName = entry.getValue().get(0);
               
               File file = Main.getWorkMgr().getObject(objectName);

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
