package org.batfish.coordinator;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

   Logger _logger = Main.initializeLogger();
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      _logger.info("WMS:getInfo\n");
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
         _logger.info("WMS:getWorkQueueStatus\n");
         return new JSONArray(Arrays.asList("", Main.getWorkMgr()
               .getWorkQueueStatusJson()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }
   
   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_QUEUE_WORK_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(@Context UriInfo ui) {
         try {
            _logger.info("WMS:queueWork " + ui + "\n");
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
            String stackTrace = ExceptionUtils.getFullStackTrace(e);
            _logger.error("WMS:queueWork exception: " + stackTrace);
            return new JSONArray(Arrays.asList("failure", e.getMessage()));
         }   
   }      

   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_GET_WORK_STATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkStatus(@QueryParam(CoordinatorConstants.SERVICE_WORKID_KEY) String workId) {
      try {
         _logger.info("WMS:getWorkStatus " + workId + "\n");

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
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkStatus exception: " + stackTrace);
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
         _logger.info("WMS:uploadTestRig " + name + "\n");

         Main.getWorkMgr().uploadTestrig(name, fileStream);

         return new JSONArray(
               Arrays.asList("", "successfully uploaded testrig"));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadTestRig exception: " + stackTrace);
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @GET
   @Path(CoordinatorConstants.SERVICE_WORK_GET_OBJECT_RESOURCE)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getObject(
         @QueryParam(CoordinatorConstants.SERVICE_WORK_OBJECT_KEY) String objectName) {
      try {
         _logger.info("WMS:getObject " + objectName + "\n");

         if (objectName == null || objectName.equals("")) {
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity("objectname not supplied").type(MediaType.TEXT_PLAIN)
                  .build();
         }

         File file = Main.getWorkMgr().getObject(objectName);

         if (file == null) {
            return Response.status(Response.Status.NOT_FOUND)
                  .entity("File not found").type(MediaType.TEXT_PLAIN).build();
         }

         return Response
               .ok(file, MediaType.APPLICATION_OCTET_STREAM)
               .header("Content-Disposition",
                     "attachment; filename=\"" + objectName + "\"").build();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getObject exception: " + stackTrace);
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .entity(e.getCause()).type(MediaType.TEXT_PLAIN).build();
      }
   }
}
