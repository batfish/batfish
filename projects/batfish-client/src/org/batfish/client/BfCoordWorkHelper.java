package org.batfish.client;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.WorkItem;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.uri.UriComponent;

public class BfCoordWorkHelper {

   private String _coordWorkMgr;
   private BatfishLogger _logger;

   public BfCoordWorkHelper(String workMgr, BatfishLogger logger) {
      _coordWorkMgr = workMgr;
      _logger = logger;
   }

   public String getObject(String testrigName, String objectName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client
               .target(
                     String.format("http://%s%s/%s", _coordWorkMgr,
                           CoordConsts.SVC_BASE_WORK_MGR,
                           CoordConsts.SVC_WORK_GET_OBJECT_RSC))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName)
               .queryParam(CoordConsts.SVC_WORK_OBJECT_KEY, objectName);

         Response response = webTarget.request(
               MediaType.APPLICATION_OCTET_STREAM).get();

         _logger.info(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("GetObject: Did not get an OK response\n");
            return null;
         }

         // see if we have a filename header
         String outFileStr = objectName;

         MultivaluedMap<String, String> headers = response.getStringHeaders();

         if (headers.containsKey(CoordConsts.SVC_WORK_FILENAME_HDR)) {
            String value = headers.getFirst(CoordConsts.SVC_WORK_FILENAME_HDR);
            if (value != null && !value.equals("")) {
               outFileStr = value;
            }
         }

         File outdir = new File("client");
         outdir.mkdirs();
         File inFile = response.readEntity(File.class);
         File outFile = Paths.get(outdir.getAbsolutePath().toString(),
               outFileStr).toFile();
         FileUtils.copyFile(inFile, outFile);
         if (!inFile.delete()) {
            throw new BatfishException("Failed to delete temporary file: "
                  + inFile.getAbsolutePath());
         }
         return outFile.getAbsolutePath();
      }
      catch (Exception e) {
         _logger.errorf("Exception in getObject from %s using (%s, %s)\n",
               _coordWorkMgr, testrigName, objectName);
         e.printStackTrace();
         return null;
      }
   }

   public String getResultsObjectNameAnswerQuestion(String envName,
         String questionName) {
      return null;
   }

   public String getResultsObjectNameCreateZ3Encoding(String envName) {
      return Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
            BfConsts.RELPATH_Z3_DATA_PLANE_FILE).toString();
   }

   public String getResultsObjectNameGenerateDataPlane(String envName) {
      return null;
   }

   public String getResultsObjectNameGenerateFacts(String envName) {
      return Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
            BfConsts.RELPATH_FACT_DUMP_DIR).toString();
   }

   public String getResultsObjectNameGetDataPlane(String envName) {
      return Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
            BfConsts.RELPATH_DATA_PLANE_DIR).toString();
   }

   public String getResultsObjectNameGetFlowTraces(String envName,
         String questionName) {
      return Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
            BfConsts.RELPATH_QUERY_DUMP_DIR).toString();
   }

   public String getResultsObjectNameParseVendorIndependent() {
      return BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR;
   }

   public String getResultsObjectNameParseVendorSpecific() {
      return BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR;
   }

   public String getResultsObjectNamePostFlows(String envName,
         String questionName) {
      return null;
   }

   public WorkItem getWorkItemAnswerDiffQuestion(String questionName,
         String testrigName, String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      wItem.addRequestParam(BfConsts.COMMAND_POST_FLOWS, "");
      wItem.addRequestParam(BfConsts.COMMAND_POST_DIFFERENTIAL_FLOWS, "");

      wItem.addRequestParam(BfConsts.COMMAND_GET_HISTORY, "");
      wItem.addRequestParam(BfConsts.COMMAND_GET_DIFFERENTIAL_HISTORY, "");

      return wItem;
   }

   public WorkItem getWorkItemAnswerQuestion(String questionName,
         String testrigName, String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);

      if (diffEnvName != null) {
         wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      }

      wItem.addRequestParam(BfConsts.COMMAND_POST_FLOWS, "");
      wItem.addRequestParam(BfConsts.COMMAND_POST_DIFFERENTIAL_FLOWS, "");

      wItem.addRequestParam(BfConsts.COMMAND_GET_HISTORY, "");
      wItem.addRequestParam(BfConsts.COMMAND_GET_DIFFERENTIAL_HISTORY, "");

      return wItem;
   }

   public WorkItem getWorkItemCreateZ3Encoding(String testrigName,
         String envName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_SYNTHESIZE_Z3_DATA_PLANE, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGenerateDataPlane(String testrigName,
         String envName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_CREATE_WORKSPACE, "");
      wItem.addRequestParam(BfConsts.COMMAND_FACTS, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGenerateDiffDataPlane(String testrigName,
         String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_CREATE_WORKSPACE, "");
      wItem.addRequestParam(BfConsts.COMMAND_FACTS, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      return wItem;
   }

   public WorkItem getWorkItemGenerateFacts(String testrigName, String envName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_GENERATE_FACT, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGetDataPlane(String testrigName, String envName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGetDiffDataPlane(String testrigName,
         String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      return wItem;
   }

   public WorkItem getWorkItemGetFlowTraces(String testrigName, String envName,
         String questionName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_QUERY, "");
      wItem.addRequestParam(BfConsts.ARG_PREDICATES,
            BfConsts.PREDICATE_FLOW_PATH_HISTORY);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemParseVendorIndependent(String testrigName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
      wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
      return wItem;
   }

   public WorkItem getWorkItemParseVendorSpecific(String testrigName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
      wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
      return wItem;
   }

   public WorkItem getWorkItemPostFlows(String testrigName, String envName,
         String questionName) {
      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_POST_FLOWS, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkStatusCode getWorkStatus(UUID parseWorkUUID) {
      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", _coordWorkMgr,
                     CoordConsts.SVC_BASE_WORK_MGR,
                     CoordConsts.SVC_WORK_GET_WORKSTATUS_RSC)).queryParam(
               CoordConsts.SVC_WORKID_KEY,
               UriComponent.encode(parseWorkUUID.toString(),
                     UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         _logger.info(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("Did not get an OK response from: %s\n", webTarget);
            return null;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("got error while checking work status: %s %s\n",
                  array.get(0), array.get(1));
            return null;
         }

         JSONObject jObj = new JSONObject(array.get(1).toString());

         if (!jObj.has(CoordConsts.SVC_WORKSTATUS_KEY)) {
            _logger
                  .errorf("workstatus key not found in: %s\n", jObj.toString());
            return null;
         }

         return WorkStatusCode.valueOf(jObj
               .getString(CoordConsts.SVC_WORKSTATUS_KEY));
      }
      catch (ProcessingException e) {
         _logger.errorf("unable to connect to %s: %s\n", _coordWorkMgr, e
               .getStackTrace().toString());
         return null;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         e.printStackTrace();
         return null;
      }
   }

   public boolean queueWork(WorkItem wItem) {

      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", _coordWorkMgr,
                     CoordConsts.SVC_BASE_WORK_MGR,
                     CoordConsts.SVC_WORK_QUEUE_WORK_RSC)).queryParam(
               CoordConsts.SVC_WORKITEM_KEY,
               UriComponent.encode(wItem.toJsonString(),
                     UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("QueueWork: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("got error while queuing work: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         _logger.errorf("unable to connect to %s: %s\n", _coordWorkMgr, e
               .getStackTrace().toString());
         return false;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         e.printStackTrace();
         return false;
      }
   }

   public boolean uploadEnvironment(String testrigName, String envName,
         String zipfileName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _coordWorkMgr, CoordConsts.SVC_BASE_WORK_MGR,
               CoordConsts.SVC_WORK_UPLOAD_ENV_RSC));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FormDataBodyPart envNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_ENV_NAME_KEY, envName, MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(envNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_ZIPFILE_KEY, new File(zipfileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         _logger.infof(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err
                  .printf("UploadEnvironment: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("got error while uploading environment: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", zipfileName);
         }
         else if (e.getMessage().contains("ConnectException")) {
            _logger.errorf("ERROR: Could not talk to coordinator\n");
         }
         else {
            _logger
                  .errorf(
                        "Exception when uploading environment to %s using (%s, %s, %s): %s\n",
                        _coordWorkMgr, testrigName, envName, zipfileName,
                        ExceptionUtils.getStackTrace(e));
         }
         return false;
      }
   }

   public boolean uploadQuestion(String testrigName, String qName,
         String qFileName, File paramsFile) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _coordWorkMgr, CoordConsts.SVC_BASE_WORK_MGR,
               CoordConsts.SVC_WORK_UPLOAD_QUESTION_RSC));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FormDataBodyPart qNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_QUESTION_NAME_KEY, qName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(qNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_FILE_KEY, new File(qFileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         FileDataBodyPart paramFileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_FILE2_KEY, paramsFile,
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(paramFileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         _logger.infof(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("UploadQuestion: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("Error while uploading question [%s]: %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s or %s\n", qFileName,
                  paramsFile.getAbsolutePath());
         }
         else if (e.getMessage().contains("ConnectException")) {
            _logger.error("ERROR: Could not talk to coordinator\n");
         }
         else {
            _logger
                  .errorf(
                        "Exception when uploading question to %s using (%s, %s, %s): %s\n",
                        _coordWorkMgr, testrigName, qName, qFileName,
                        ExceptionUtils.getStackTrace(e));
         }
         return false;
      }
   }

   public boolean uploadTestrig(String testrigName, String zipfileName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _coordWorkMgr, CoordConsts.SVC_BASE_WORK_MGR,
               CoordConsts.SVC_WORK_UPLOAD_TESTRIG_RSC));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_ZIPFILE_KEY, new File(zipfileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         _logger.info(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("UploadTestrig: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("Error while uploading test rig [%s]: %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", zipfileName);
         }
         else if (e.getMessage().contains("ConnectException")) {
            _logger.errorf("ERROR: Could not talk to coordinator\n");
         }
         else {
            _logger
                  .errorf(
                        "Exception when uploading test rig to %s using (%s, %s): %s\n",
                        _coordWorkMgr, testrigName, zipfileName,
                        ExceptionUtils.getStackTrace(e));
         }
         return false;
      }
   }
}