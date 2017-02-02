package org.batfish.client;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.WorkItem;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Pair;
import org.batfish.common.Version;
import org.batfish.common.util.CommonUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

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

   private void addFileMultiPart(MultiPart multiPart, String key,
         String filename) {
      multiPart.bodyPart(new FormDataBodyPart(key, new File(filename),
            MediaType.APPLICATION_OCTET_STREAM_TYPE));
   }

   private void addTextMultiPart(MultiPart multiPart, String key,
         String value) {
      multiPart.bodyPart(
            new FormDataBodyPart(key, value, MediaType.TEXT_PLAIN_TYPE));
   }

   public String checkApiKey() {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_CHECK_API_KEY_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());

         JSONObject jObj = postData(webTarget, multiPart);
         if (jObj == null) {
            return null;
         }
         return Boolean.toString(jObj.getBoolean(CoordConsts.SVC_API_KEY));
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public boolean delContainer(String containerName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_CONTAINER_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);

         JSONObject jObj = postData(webTarget, multiPart);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   public boolean delEnvironment(String containerName, String testrigName,
         String envName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_ENVIRONMENT_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addTextMultiPart(multiPart, CoordConsts.SVC_ENV_NAME_KEY, envName);

         JSONObject jObj = postData(webTarget, multiPart);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   public boolean delQuestion(String containerName, String testrigName,
         String questionName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_DEL_QUESTION_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addTextMultiPart(multiPart, CoordConsts.SVC_QUESTION_NAME_KEY,
               questionName);

         JSONObject jObj = postData(webTarget, multiPart);
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
               CoordConsts.SVC_DEL_TESTRIG_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);

         JSONObject jObj = postData(webTarget, multiPart);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   // private JSONObject getJsonResponse(WebTarget webTarget) throws Exception {
   // try {
   // Response response = webTarget.request(MediaType.APPLICATION_JSON)
   // .get();
   //
   // _logger.info(response.getStatus() + " " + response.getStatusInfo()
   // + " " + response + "\n");
   //
   // if (response.getStatus() != Response.Status.OK.getStatusCode()) {
   // _logger.errorf("Did not get an OK response from: %s\n", webTarget);
   // return null;
   // }
   //
   // String sobj = response.readEntity(String.class);
   // JSONArray array = new JSONArray(sobj);
   //
   // _logger.infof("response: %s [%s] [%s]\n", array.toString(),
   // array.get(0), array.get(1));
   //
   // if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
   // _logger.errorf("did not get success: %s %s\n", array.get(0),
   // array.get(1));
   // return null;
   // }
   //
   // return new JSONObject(array.get(1).toString());
   // }
   // catch (ProcessingException e) {
   // if (e.getMessage().contains("ConnectException")) {
   // _logger.errorf("unable to connect to coordinator at %s\n",
   // _coordWorkMgr);
   // return null;
   // }
   // if (e.getMessage().contains("SSLHandshakeException")) {
   // _logger
   // .errorf("SSL handshake exception while connecting to coordinator (Is the
   // coordinator using SSL and using keys that you trust?)\n");
   // return null;
   // }
   // if (e.getMessage().contains("SocketException: Unexpected end of file")) {
   // _logger
   // .errorf("SocketException while connecting to coordinator. (Are you using
   // SSL?)\n");
   // return null;
   // }
   // throw e;
   // }
   // }

   private ClientBuilder getClientBuilder() throws Exception {
      return CommonUtil
            .getClientBuilder(_settings.getUseSsl(),
                  _settings.getTrustAllSslCerts())
            .register(MultiPartFeature.class);
   }

   public String getObject(String containerName, String testrigName,
         String objectName) {
      try {

         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_GET_OBJECT_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_VERSION_KEY,
               Version.getVersion());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addTextMultiPart(multiPart, CoordConsts.SVC_OBJECT_NAME_KEY,
               objectName);

         Response response = webTarget
               .request(MediaType.APPLICATION_OCTET_STREAM)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         _logger.info(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("GetObject: Did not get an OK response\n");
            return null;
         }

         // see if we have a filename header
         // String outFileStr = objectName;
         //
         // MultivaluedMap<String, String> headers =
         // response.getStringHeaders();
         //
         // if (headers.containsKey(CoordConsts.SVC_FILENAME_HDR)) {
         // String value = headers.getFirst(CoordConsts.SVC_FILENAME_HDR);
         // if (value != null && !value.equals("")) {
         // outFileStr = value;
         // }
         // }

         File inFile = response.readEntity(File.class);

         File tmpOutFile = Files.createTempFile("batfish_client", null)
               .toFile();
         tmpOutFile.deleteOnExit();

         FileUtils.copyFile(inFile, tmpOutFile);
         if (!inFile.delete()) {
            throw new BatfishException("Failed to delete temporary file: "
                  + inFile.getAbsolutePath());
         }
         return tmpOutFile.getAbsolutePath();
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

   public WorkItem getWorkItemAnswerQuestion(String questionName,
         String containerName, String testrigName, String envName,
         String deltaTestrig, String deltaEnvName, boolean isDelta) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
      wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      if (deltaEnvName != null) {
         wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME,
               deltaEnvName);
      }
      if (deltaTestrig != null) {
         wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
      }
      if (isDelta) {
         wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      }
      return wItem;
   }

   public WorkItem getWorkItemCompileDeltaEnvironment(String containerName,
         String testrigName, String envName, String diffEnvName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_COMPILE_DIFF_ENVIRONMENT, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, diffEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      return wItem;
   }

   public WorkItem getWorkItemGenerateDataPlane(String containerName,
         String testrigName, String envName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      return wItem;
   }

   public WorkItem getWorkItemGenerateDeltaDataPlane(String containerName,
         String testrigName, String envName, String deltaTestrigName,
         String deltaEnvName) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
      wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrigName);
      wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, deltaEnvName);
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      return wItem;
   }

   public WorkItem getWorkItemParse(String containerName, String testrigName,
         boolean doDelta) {
      WorkItem wItem = new WorkItem(containerName, testrigName);
      wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
      wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
      wItem.addRequestParam(BfConsts.COMMAND_INIT_INFO, "");
      wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
      if (doDelta) {
         wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
      }
      return wItem;
   }

   public Pair<WorkStatusCode, String> getWorkStatus(UUID parseWorkUUID) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_GET_WORKSTATUS_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_WORKID_KEY,
               parseWorkUUID.toString());

         JSONObject jObj = postData(webTarget, multiPart);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_WORKSTATUS_KEY)) {
            _logger.errorf("workstatus key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         WorkStatusCode workStatus = WorkStatusCode
               .valueOf(jObj.getString(CoordConsts.SVC_WORKSTATUS_KEY));

         if (!jObj.has(CoordConsts.SVC_TASKSTATUS_KEY)) {
            _logger.errorf("taskstatus key not found in: %s\n",
                  jObj.toString());
         }
         String taskStr = jObj.getString(CoordConsts.SVC_TASKSTATUS_KEY);

         return new Pair<>(workStatus, taskStr);
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
               CoordConsts.SVC_INIT_CONTAINER_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_PREFIX_KEY,
               containerPrefix);

         JSONObject jObj = postData(webTarget, multiPart);
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

   public boolean isReachable(boolean chatty) throws Exception {

      WebTarget webTarget = null;

      try {
         Client client = getClientBuilder().build();
         webTarget = getTarget(client, "");

         Response response = webTarget.request().get();

         _logger.info(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("GetObject: Did not get an OK response\n");
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         if (e.getMessage().contains("ConnectException")) {
            if (chatty) {
               _logger.errorf(
                     "BF-Client: unable to connect to coordinator at %s\n",
                     webTarget.getUri().toString());
            }
            return false;
         }
         if (e.getMessage().contains("SSLHandshakeException")) {
            if (chatty) {
               _logger.errorf(
                     "SSL handshake exception while connecting to coordinator (Is the coordinator using SSL and using keys that you trust?)\n");
            }
            return false;
         }
         if (e.getMessage()
               .contains("SocketException: Unexpected end of file")) {
            if (chatty) {
               _logger.errorf(
                     "SocketException while connecting to coordinator. (Are you using SSL?)\n");
            }
            return false;
         }
         throw e;
      }
   }

   public String[] listContainers() {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_LIST_CONTAINERS_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());

         JSONObject jObj = postData(webTarget, multiPart);

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
               CoordConsts.SVC_LIST_ENVIRONMENTS_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);

         JSONObject jObj = postData(webTarget, multiPart);
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
               CoordConsts.SVC_LIST_QUESTIONS_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);

         JSONObject jObj = postData(webTarget, multiPart);
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

   public Map<String, String> listTestrigs(String containerName) {
      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_LIST_TESTRIGS_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         if (containerName != null) {
            addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
                  containerName);
         }

         JSONObject jObj = postData(webTarget, multiPart);
         if (jObj == null) {
            return null;
         }

         if (!jObj.has(CoordConsts.SVC_TESTRIG_LIST_KEY)) {
            _logger.errorf("testrig key not found in: %s\n", jObj.toString());
            return null;
         }

         JSONArray testrigArray = jObj
               .getJSONArray(CoordConsts.SVC_TESTRIG_LIST_KEY);

         Map<String, String> testrigs = new HashMap<>();

         for (int index = 0; index < testrigArray.length(); index++) {
            JSONObject jObjTestrig = testrigArray.getJSONObject(index);
            testrigs.put(
                  jObjTestrig.getString(CoordConsts.SVC_TESTRIG_NAME_KEY),
                  jObjTestrig.getString(CoordConsts.SVC_TESTRIG_INFO_KEY));
         }

         return testrigs;
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return null;
      }
   }

   public JSONObject postData(WebTarget webTarget, MultiPart multiPart)
         throws Exception {
      try {

         addTextMultiPart(multiPart, CoordConsts.SVC_VERSION_KEY,
               Version.getVersion());

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         _logger.infof(response.getStatus() + " " + response.getStatusInfo()
               + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("PostData: Did not get an OK response\n");
            return null;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _logger.infof("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("Error in PostData: %s %s\n", array.get(0),
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
            _logger.errorf(
                  "SSL handshake exception while connecting to coordinator (Is the coordinator using SSL and using keys that you trust?)\n");
            return null;
         }
         if (e.getMessage()
               .contains("SocketException: Unexpected end of file")) {
            _logger.errorf(
                  "SocketException while connecting to coordinator. (Are you using SSL?)\n");
            return null;
         }
         throw e;
      }
   }

   public boolean queueWork(WorkItem wItem) {

      try {
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_QUEUE_WORK_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_WORKITEM_KEY,
               wItem.toJsonString());
         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());

         JSONObject jObj = postData(webTarget, multiPart);
         return (jObj != null);
      }
      catch (Exception e) {
         _logger.errorf("exception: ");
         _logger.error(ExceptionUtils.getFullStackTrace(e) + "\n");
         return false;
      }
   }

   public boolean uploadCustomObject(String containerName, String testrigName,
         String objName, String objFileName) {
      try {

         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_PUT_OBJECT_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addTextMultiPart(multiPart, CoordConsts.SVC_OBJECT_NAME_KEY, objName);
         addFileMultiPart(multiPart, CoordConsts.SVC_FILE_KEY, objFileName);

         return postData(webTarget, multiPart) != null;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", objFileName);
         }
         else {
            _logger.errorf(
                  "Exception when uploading custom object to %s using (%s, %s, %s): %s\n",
                  _coordWorkMgr, testrigName, objName, objFileName,
                  ExceptionUtils.getStackTrace(e));
         }
         return false;
      }
   }

   public boolean uploadEnvironment(String containerName, String testrigName,
         String envName, String zipfileName) {
      try {

         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_UPLOAD_ENV_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addTextMultiPart(multiPart, CoordConsts.SVC_ENV_NAME_KEY, envName);
         addFileMultiPart(multiPart, CoordConsts.SVC_ZIPFILE_KEY, zipfileName);

         return postData(webTarget, multiPart) != null;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", zipfileName);
         }
         else {
            _logger.errorf(
                  "Exception when uploading environment to %s using (%s, %s, %s): %s\n",
                  _coordWorkMgr, testrigName, envName, zipfileName,
                  ExceptionUtils.getStackTrace(e));
         }
         return false;
      }
   }

   public boolean uploadQuestion(String containerName, String testrigName,
         String qName, String qFileName, String paramsFilename) {
      try {

         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_UPLOAD_QUESTION_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addTextMultiPart(multiPart, CoordConsts.SVC_QUESTION_NAME_KEY, qName);
         addFileMultiPart(multiPart, CoordConsts.SVC_FILE_KEY, qFileName);
         addFileMultiPart(multiPart, CoordConsts.SVC_FILE2_KEY, paramsFilename);

         return postData(webTarget, multiPart) != null;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf(
                  "File not found: %s (question file) or %s (temporary params file) \n",
                  qFileName, paramsFilename);
         }
         else {
            _logger.errorf(
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
         Client client = getClientBuilder().build();
         WebTarget webTarget = getTarget(client,
               CoordConsts.SVC_UPLOAD_TESTRIG_RSC);

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
               _settings.getApiKey());
         addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_NAME_KEY,
               containerName);
         addTextMultiPart(multiPart, CoordConsts.SVC_TESTRIG_NAME_KEY,
               testrigName);
         addFileMultiPart(multiPart, CoordConsts.SVC_ZIPFILE_KEY, zipfileName);

         return postData(webTarget, multiPart) != null;
      }
      catch (Exception e) {
         if (e.getMessage().contains("FileNotFoundException")) {
            _logger.errorf("File not found: %s\n", zipfileName);
         }
         else {
            _logger.errorf(
                  "Exception when uploading test rig to %s using (%s, %s, %s): %s\n",
                  _coordWorkMgr, containerName, testrigName, zipfileName,
                  ExceptionUtils.getStackTrace(e));
         }
         return false;
      }
   }

   // private String uriEncode(String input) {
   // return UriComponent.encode(input,
   // UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
   // }
}
