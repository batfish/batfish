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
   private Settings _settings;

   public BfCoordWorkHelper(String workMgr, BatfishLogger logger,
         Settings settings) {
      _coordWorkMgr = workMgr;
      _logger = logger;
      _settings = settings;
   }

   public boolean delContainer(String containerName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_CONTAINER_RSC)
               .queryParam(CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY, uriEncode(containerName));

         JSONObject jObj = getJsonResponse(webTarget);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   public boolean delEnvironment(String containerName, String testrigName, String envName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_ENVIRONMENT_RSC)
               .queryParam(CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY, uriEncode(containerName))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, uriEncode(testrigName))
               .queryParam(CoordConsts.SVC_ENV_NAME_KEY, uriEncode(envName));

         JSONObject jObj = getJsonResponse(webTarget);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }
   
   public boolean delQuestion(String containerName, String testrigName, String questionName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_QUESTION_RSC)
               .queryParam(CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY, uriEncode(containerName))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, uriEncode(testrigName))
               .queryParam(CoordConsts.SVC_QUESTION_NAME_KEY, uriEncode(questionName));

         JSONObject jObj = getJsonResponse(webTarget);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   public boolean delTestrig(String containerName, String testrigName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_TESTRIG_RSC)
               .queryParam(CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY, uriEncode(containerName))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, uriEncode(testrigName));

         JSONObject jObj = getJsonResponse(webTarget);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }
   
   private ClientBuilder getClientBuilder() throws Exception {
      return org.batfish.common.Util.getClientBuilder(_settings.getUseSsl(),
            _settings.getTrustAllSslCerts());
   }

   private JSONObject getJsonResponse(WebTarget webTarget) throws Exception {
      try {
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
            _logger.errorf("did not get success: %s %s\n", array.get(0),
                  array.get(1));
            return null;
         }

         return new JSONObject(array.get(1).toString());
      }
      catch (ProcessingException e) {
         if (e.getMessage().contains("ConnectException")) {
            _logger.errorf("unable to connect to coordinator at %s\n",
                  _coordWorkMgr);
            return null;
         }
         if (e.getMessage().contains("SSLHandshakeException")) {
            _logger
                  .errorf("SSL handshake exception while connecting to coordinator (Is the coordinator using SSL and using keys that you trust?)\n");
            return null;
         }
         if (e.getMessage().contains("SocketException: Unexpected end of file")) {
            _logger
                  .errorf("SocketException while connecting to coordinator. (Are you using SSL?)\n");
            return null;
         }
         throw e;
      }
   }

   public String getObject(String containerName, String testrigName,
         String objectName) {
      try {

         Client client = getClientBuilder().register(MultiPartFeature.class)
               .build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_WORK_GET_OBJECT_RSC)
               .queryParam(CoordConsts.SVC_API_KEY, _settings.getApiKey())
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY,
                     uriEncode(containerName))
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
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   private WebTarget getTarget(Client client, String resource) {

      String protocol = (_settings.getUseSsl()) ? "https" : "http";

      String urlString = String.format("%s://%s%s/%s", protocol, _coordWorkMgr,
            CoordConsts.SVC_BASE_WORK_MGR, resource);

      return client.target(urlString);
   }

   public WorkItem getWorkItemAnswerDiffQuestion(String questionName,
         String containerName, String testrigName, String envName,
         String diffEnvName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      wItem.addRequestParam(BfConsts.COMMAND_NXTNET_TRAFFIC, "");
      wItem.addRequestParam(BfConsts.COMMAND_GET_HISTORY, "");
      return wItem;
   }

   public WorkItem getWorkItemAnswerQuestion(String questionName,
         String containerName, String testrigName, String envName,
         String diffEnvName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      if (diffEnvName != null) {
         wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      }
      wItem.addRequestParam(BfConsts.COMMAND_NXTNET_TRAFFIC, "");
      wItem.addRequestParam(BfConsts.COMMAND_GET_HISTORY, "");
      return wItem;
   }

   public WorkItem getWorkItemGenerateDataPlane(String containerName,
         String testrigName, String envName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_WRITE_CP_FACTS, "");
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.COMMAND_NXTNET_DATA_PLANE, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGenerateDiffDataPlane(String containerName,
         String testrigName, String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_WRITE_CP_FACTS, "");
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.COMMAND_NXTNET_DATA_PLANE, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      return wItem;
   }

   public WorkItem getWorkItemGenerateFacts(String containerName,
         String testrigName, String envName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_WRITE_CP_FACTS, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGetDataPlane(String containerName,
         String testrigName, String envName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGetDiffDataPlane(String containerName,
         String testrigName, String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      return wItem;
   }

   public WorkItem getWorkItemGetFlowTraces(String containerName,
         String testrigName, String envName, String questionName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_QUERY, "");
      wItem.addRequestParam(BfConsts.ARG_PREDICATES,
            BfConsts.PREDICATE_FLOW_PATH_HISTORY);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemParseVendorIndependent(String containerName,
         String testrigName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
      wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
      return wItem;
   }

   public WorkItem getWorkItemParseVendorSpecific(String containerName,
         String testrigName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
      wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
      return wItem;
   }

   public WorkItem getWorkItemPostFlows(String containerName,
         String testrigName, String envName, String questionName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_NXTNET_TRAFFIC, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkStatusCode getWorkStatus(UUID parseWorkUUID) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_WORK_GET_WORKSTATUS_RSC).queryParam(
               CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_WORKID_KEY,
                     uriEncode(parseWorkUUID.toString()));

         JSONObject jObj = getJsonResponse(webTarget);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_WORKSTATUS_KEY)) {
            _logger
                  .errorf("workstatus key not found in: %s\n", jObj.toString());
            return null;
         }

         return WorkStatusCode.valueOf(jObj
               .getString(CoordConsts.SVC_WORKSTATUS_KEY));
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public String initContainer(String containerPrefix) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_INIT_CONTAINER_RSC).queryParam(
               CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_PREFIX_KEY,
                     uriEncode(containerPrefix));

         JSONObject jObj = getJsonResponse(webTarget);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_CONTAINER_NAME_KEY)) {
            _logger.errorf("container name key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         return jObj.getString(CoordConsts.SVC_CONTAINER_NAME_KEY);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public String[] listContainers() {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_LIST_CONTAINERS_RSC).queryParam(
               CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()));

         JSONObject jObj = getJsonResponse(webTarget);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_CONTAINER_LIST_KEY)) {
            _logger.errorf("container list key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         JSONArray containerArray = jObj
               .getJSONArray(CoordConsts.SVC_CONTAINER_LIST_KEY);

         String[] containerList = new String[containerArray.length()];

         for (int index = 0; index < containerArray.length(); index++) {
            containerList[index] = containerArray.getString(index);
         }

         return containerList;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public String[] listEnvironments(String containerName, String testrigName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_LIST_ENVIRONMENTS_RSC).queryParam(
               CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY, uriEncode(containerName))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, uriEncode(testrigName));

         JSONObject jObj = getJsonResponse(webTarget);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_ENVIRONMENT_LIST_KEY)) {
            _logger.errorf("environment list key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         JSONArray environmentArray = jObj
               .getJSONArray(CoordConsts.SVC_ENVIRONMENT_LIST_KEY);

         String[] environmentList = new String[environmentArray.length()];

         for (int index = 0; index < environmentArray.length(); index++) {
            environmentList[index] = environmentArray.getString(index);
         }

         return environmentList;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public String[] listQuestions(String containerName, String testrigName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_LIST_QUESTIONS_RSC).queryParam(
               CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY, uriEncode(containerName))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, uriEncode(testrigName));

         JSONObject jObj = getJsonResponse(webTarget);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_QUESTION_LIST_KEY)) {
            _logger.errorf("question list key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         JSONArray questionArray = jObj
               .getJSONArray(CoordConsts.SVC_QUESTION_LIST_KEY);

         String[] questionList = new String[questionArray.length()];

         for (int index = 0; index < questionArray.length(); index++) {
            questionList[index] = questionArray.getString(index);
         }

         return questionList;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public String[] listTestrigs(String containerName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_LIST_TESTRIGS_RSC).queryParam(
               CoordConsts.SVC_API_KEY, uriEncode(_settings.getApiKey()))
               .queryParam(CoordConsts.SVC_CONTAINER_NAME_KEY,
                     uriEncode(containerName));

         JSONObject jObj = getJsonResponse(webTarget);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_TESTRIG_LIST_KEY)) {
            _logger.errorf("testrig key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         JSONArray testrigArray = jObj
               .getJSONArray(CoordConsts.SVC_TESTRIG_LIST_KEY);

         String[] testrigList = new String[testrigArray.length()];

         for (int index = 0; index < testrigArray.length(); index++) {
            testrigList[index] = testrigArray.getString(index);
         }

         return testrigList;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public boolean postData(WebTarget webTarget, MultiPart multiPart)
         throws Exception {
      try {
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         _logger.infof(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("PostData: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("Error in PostData: %s %s\n", array.get(0),
                  array.get(1));
            return false;
         }
         return true;
      }
      catch (ProcessingException e) {
         if (e.getMessage().contains("ConnectException")) {
            _logger.errorf("unable to connect to coordinator at %s\n",
                  _coordWorkMgr);
            return false;
         }
         if (e.getMessage().contains("SSLHandshakeException")) {
            _logger
                  .errorf("SSL handshake exception while connecting to coordinator\n");
            return false;
         }
         throw e;
      }
   }

   public boolean queueWork(WorkItem wItem) {

      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_WORK_QUEUE_WORK_RSC).queryParam(
               CoordConsts.SVC_WORKITEM_KEY, uriEncode(wItem.toJsonString()))
               .queryParam(CoordConsts.SVC_API_KEY,
                     uriEncode(_settings.getApiKey()));

         JSONObject jObj = getJsonResponse(webTarget);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   public boolean uploadEnvironment(String containerName, String testrigName,
         String envName, String zipfileName) {
      try {

         Client client = getClientBuilder().register(MultiPartFeature.class)
               .build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_WORK_UPLOAD_ENV_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart apiKeyBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_API_KEY, _settings.getApiKey(),
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(apiKeyBodyPart);

         FormDataBodyPart containerNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_CONTAINER_NAME_KEY, containerName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(containerNameBodyPart);

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

         return postData(webTarget, multiPart);
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", zipfileName);
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

   public boolean uploadQuestion(String containerName, String testrigName,
         String qName, String qFileName, File paramsFile) {
      try {

         Client client = getClientBuilder().register(MultiPartFeature.class)
               .build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_WORK_UPLOAD_QUESTION_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart apiKeyBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_API_KEY, _settings.getApiKey(),
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(apiKeyBodyPart);

         FormDataBodyPart containerNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_CONTAINER_NAME_KEY, containerName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(containerNameBodyPart);

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

         return postData(webTarget, multiPart);
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s or %s\n", qFileName,
                  paramsFile.getAbsolutePath());
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

   public boolean uploadTestrig(String containerName, String testrigName,
         String zipfileName) {
      try {
         Client client = getClientBuilder().register(MultiPartFeature.class)
               .build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_WORK_UPLOAD_TESTRIG_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart apiKeyBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_API_KEY, _settings.getApiKey(),
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(apiKeyBodyPart);

         FormDataBodyPart containerNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_CONTAINER_NAME_KEY, containerName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(containerNameBodyPart);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_ZIPFILE_KEY, new File(zipfileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         return postData(webTarget, multiPart);
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", zipfileName);
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

   private String uriEncode(String input) {
      return UriComponent.encode(input,
            UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
   }
}