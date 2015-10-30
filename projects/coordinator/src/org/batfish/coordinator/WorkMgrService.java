package org.batfish.coordinator;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.batfish.common.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path(CoordConsts.SVC_BASE_WORK_MGR)
public class WorkMgrService {

   Logger _logger = Main.initializeLogger();

   // @GET
   // @Path("test")
   // @Produces(MediaType.APPLICATION_JSON)
   // public Response test() {
   // try {
   // _logger.info("WMS:getInfo\n");
   // JSONArray id = new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
   // Main.getWorkMgr()
   // .getStatusJson()));
   //
   // return Response.ok()
   // .entity(id)
   // // .header("Access-Control-Allow-Origin","*")
   // .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
   // .allow("OPTIONS")
   // .build();
   // }
   // catch (Exception e) {
   // String stackTrace = ExceptionUtils.getFullStackTrace(e);
   // _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
   // return Response.serverError().build();
   // }
   // }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      _logger.info("WMS:getInfo\n");
      return new JSONArray(
            Arrays.asList(
                  CoordConsts.SVC_SUCCESS_KEY,
                  "Batfish coordinator: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(CoordConsts.SVC_WORK_GET_OBJECT_RSC)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getObject(
         @QueryParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @QueryParam(CoordConsts.SVC_WORK_OBJECT_KEY) String objectName) {
      try {
         _logger.info("WMS:getObject " + testrigName + " --> " + objectName
               + "\n");

         if (testrigName == null || testrigName.equals("")) {
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity("testrigname not supplied")
                  .type(MediaType.TEXT_PLAIN).build();
         }
         if (objectName == null || objectName.equals("")) {
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity("objectname not supplied").type(MediaType.TEXT_PLAIN)
                  .build();
         }

         File file = Main.getWorkMgr().getObject(testrigName, objectName);

         if (file == null) {
            return Response.status(Response.Status.NOT_FOUND)
                  .entity("File not found").type(MediaType.TEXT_PLAIN).build();
         }

         return Response
               .ok(file, MediaType.APPLICATION_OCTET_STREAM)
               .header("Content-Disposition",
                     "attachment; filename=\"" + file.getName() + "\"")
               .header(CoordConsts.SVC_WORK_FILENAME_HDR, file.getName())
               .build();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getObject exception: " + stackTrace);
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .entity(e.getCause()).type(MediaType.TEXT_PLAIN).build();
      }
   }

   @GET
   @Path(CoordConsts.SVC_WORK_GETSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         _logger.info("WMS:getWorkQueueStatus\n");
         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, Main
               .getWorkMgr().getStatusJson()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }

   @GET
   @Path(CoordConsts.SVC_WORK_GET_WORKSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkStatus(
         @QueryParam(CoordConsts.SVC_WORKID_KEY) String workId) {
      try {
         _logger.info("WMS:getWorkStatus " + workId + "\n");

         if (workId == null || workId.equals("")) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                  "workid not supplied"));
         }

         QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

         if (work == null) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                  "work with the specified id not found"));
         }
         else {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
                  (new JSONObject().put(CoordConsts.SVC_WORKSTATUS_KEY, work
                        .getStatus().toString()))));
         }
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkStatus exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }

   @GET
   @Path(CoordConsts.SVC_WORK_QUEUE_WORK_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(
         @QueryParam(CoordConsts.SVC_WORKITEM_KEY) String workItemStr) {
      try {
         _logger.info("WMS:queueWork " + workItemStr + "\n");

         if (workItemStr == null || workItemStr.equals("")) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                  "work item not supplied"));
         }

         WorkItem workItem = WorkItem.FromJsonString(workItemStr);

         boolean result = Main.getWorkMgr().queueWork(workItem);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, result));
      }
      catch (FileExistsException e) {
         _logger.error("WMS:uploadTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:queueWork exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }

   @GET
   @Path("test")
   @Produces(MediaType.TEXT_PLAIN)
   public String test() {
      try {
         _logger.info("WMS:getInfo\n");
         JSONArray id = new JSONArray(Arrays.asList(
               CoordConsts.SVC_SUCCESS_KEY, Main.getWorkMgr().getStatusJson()));

         return id.toString();

         // return Response.ok()
         // .entity(id)
         // // .header("Access-Control-Allow-Origin","*")
         // .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
         // .allow("OPTIONS")
         // .build();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
         // return Response.serverError().build();
         return "got error";
      }
   }

   @POST
   @Path(CoordConsts.SVC_WORK_UPLOAD_ENV_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadEnvironment(
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_ENV_NAME_KEY) String envName,
         @FormDataParam(CoordConsts.SVC_ZIPFILE_KEY) InputStream fileStream) {
      try {
         _logger.info("WMS:uploadEnvironment " + testrigName + " / " + envName
               + "\n");

         Main.getWorkMgr().uploadEnvironment(testrigName, envName, fileStream);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               "successfully uploaded environment"));
      }
      catch (FileNotFoundException | FileExistsException e) {
         _logger.error("WMS:uploadEnvironment exception: " + e.getMessage()
               + "\n");
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadEnvironment exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }

   @POST
   @Path(CoordConsts.SVC_WORK_UPLOAD_QUESTION_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadQuestion(
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_QUESTION_NAME_KEY) String qName,
         @FormDataParam(CoordConsts.SVC_FILE_KEY) InputStream fileStream,
         @FormDataParam(CoordConsts.SVC_FILE2_KEY) InputStream paramFileStream) {
      try {
         _logger.info("WMS:uploadQuestion " + testrigName + " / " + qName
               + "\n");

         Main.getWorkMgr().uploadQuestion(testrigName, qName, fileStream,
               paramFileStream);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               "successfully uploaded question"));
      }
      catch (FileNotFoundException | FileExistsException e) {
         _logger
               .error("WMS:uploadQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadQuestion exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }

   @POST
   @Path(CoordConsts.SVC_WORK_UPLOAD_TESTRIG_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadTestrig(
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String name,
         @FormDataParam(CoordConsts.SVC_ZIPFILE_KEY) InputStream fileStream) {
      try {
         _logger.info("WMS:uploadTestrig " + name + "\n");

         if (name == null || name.equals("")) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                  "Testrig name not supplied"));
         }

         Main.getWorkMgr().uploadTestrig(name, fileStream);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               "successfully uploaded testrig"));
      }
      catch (FileExistsException e) {
         _logger.error("WMS:uploadTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadTestrig exception: " + stackTrace);
         return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
               e.getMessage()));
      }
   }
}
