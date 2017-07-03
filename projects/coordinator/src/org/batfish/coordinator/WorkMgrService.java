package org.batfish.coordinator;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.*;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.config.Settings;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.zip.ZipException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 */
/**
 *
 */
@Path(CoordConsts.SVC_CFG_WORK_MGR)
public class WorkMgrService {

   BatfishLogger _logger = Main.getLogger();
   Settings _settings = Main.getSettings();

   /**
    * Check if an API key is valid
    *
    * @param apiKey
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_CHECK_API_KEY)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray checkApiKey(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion) {
      try {
         _logger.info("WMS:checkApiKey " + apiKey + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");

         checkClientVersion(clientVersion);

         if (Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
                  new JSONObject().put(CoordConsts.SVC_KEY_API_KEY, true)));
         }
         else {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
                  new JSONObject().put(CoordConsts.SVC_KEY_API_KEY, false)));
         }
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:initContainer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:initContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   private void checkApiKeyValidity(String apiKey) throws Exception {
      if (!Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
         throw new AccessControlException("Invalid API key: " + apiKey);
      }
   }

   private void checkClientVersion(String clientVersion) throws Exception {
      Version.checkCompatibleVersion("Service", "Client", clientVersion);
   }

   private void checkContainerAccessibility(String apiKey, String containerName)
         throws Exception {
      if (!Main.getAuthorizer().isAccessibleContainer(apiKey, containerName,
            true)) {
         throw new AccessControlException(
               "container is not accessible by the api key");
      }
   }

   private void checkStringParam(String paramStr, String parameterName) {
      if (paramStr == null || paramStr.equals("")) {
         throw new IllegalArgumentException(
               parameterName + " is missing or empty");
      }
   }

   /**
    * Configures an analysis for the container
    *
    * @param apiKey
    * @param containerName
    * @param newAnalysisStr
    * @param addQuestionsStream
    * @param delQuestions
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_CONFIGURE_ANALYSIS)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray configureAnalysis(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_NEW_ANALYSIS) String newAnalysisStr,
         @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_NAME) String analysisName,
         @FormDataParam(CoordConsts.SVC_KEY_FILE) InputStream addQuestionsStream,
         @FormDataParam(CoordConsts.SVC_KEY_DEL_ANALYSIS_QUESTIONS) String delQuestions) {
      try {
         _logger.info("WMS:configureAnalysis " + apiKey + " " + containerName
               + " " + newAnalysisStr + " " + analysisName + " " + delQuestions
               + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(analysisName, "Analysis name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         boolean newAnalysis = (newAnalysisStr == null
               || newAnalysisStr.equals("")) ? false : true;

         Main.getWorkMgr().configureAnalysis(containerName, newAnalysis,
               analysisName, addQuestionsStream, delQuestions);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put("result", "successfully configured analysis"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException
            | ZipException e) {
         _logger.error(
               "WMS:configureAnalysis exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:configureAnalysis exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Deletes an analysis for the container
    *
    * @param apiKey
    * @param containerName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_DEL_ANALYSIS)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delAnalysis(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_NAME) String analysisName) {
      try {
         _logger.info("WMS:configureAnalysis " + apiKey + " " + containerName
               + " " + analysisName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(analysisName, "Analysis name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().delAnalysis(containerName, analysisName);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put("result", "successfully configured analysis"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException
            | ZipException e) {
         _logger.error(
               "WMS:configureAnalysis exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:configureAnalysis exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Deletes the specified container
    *
    * @param apiKey
    * @param containerName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_DEL_CONTAINER)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delContainer(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName) {
      try {
         _logger.info("WMS:delContainer " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().delContainer(containerName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:delContainer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Deletes the specified environment under the specified container and
    * testrig
    *
    * @param apiKey
    * @param containerName
    * @param envName
    * @param testrigName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_DEL_ENVIRONMENT)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delEnvironment(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String envName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName) {
      try {
         _logger.info("WMS:delEnvironment " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");
         checkStringParam(envName, "Environment name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().delEnvironment(containerName, testrigName, envName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger
               .error("WMS:delEnvironment exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delEnvironment exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Deletes the specified question under the specified container, testrig
    *
    * @param apiKey
    * @param containerName
    * @param questionName
    * @param testrigName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_DEL_QUESTION)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delQuestion(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName) {
      try {
         _logger.info("WMS:delQuestion " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");
         checkStringParam(questionName, "Question name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().delTestrigQuestion(containerName, testrigName,
               questionName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:delQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delQuestion exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Deletes the specified testrig under the specified container
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_DEL_TESTRIG)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delTestrig(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName) {
      try {
         _logger.info("WMS:delTestrig " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().delTestrig(containerName, testrigName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:delTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Get answers for an analysis (previously run)
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param analysisName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_GET_ANALYSIS_ANSWERS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getAnalysisAnswers(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String baseEnv,
         @FormDataParam(CoordConsts.SVC_KEY_DELTA_TESTRIG_NAME) String deltaTestrig,
         @FormDataParam(CoordConsts.SVC_KEY_DELTA_ENV_NAME) String deltaEnv,
         @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_NAME) String analysisName,
         @FormDataParam(CoordConsts.SVC_KEY_PRETTY_ANSWER) String prettyAnswer) {
      try {
         _logger.info("WMS:getAnswer " + apiKey + " " + containerName + " "
               + testrigName + " " + analysisName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Base testrig name");
         checkStringParam(baseEnv, "Base environment name");
         checkStringParam(analysisName, "Analysis name");
         checkStringParam(prettyAnswer, "Retrieve pretty-printed answers");
         boolean pretty = Boolean.parseBoolean(prettyAnswer);

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Map<String, String> answers = Main.getWorkMgr().getAnalysisAnswers(
               containerName, testrigName, baseEnv, deltaTestrig, deltaEnv,
               analysisName, pretty);

         BatfishObjectMapper mapper = new BatfishObjectMapper();
         String answersStr = mapper.writeValueAsString(answers);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               new JSONObject().put(CoordConsts.SVC_KEY_ANSWERS, answersStr)));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error(
               "WMS:getAnalysisAnswers exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getAnswer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Get answer for a question that was previously asked
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param questionName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_GET_ANSWER)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getAnswer(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String baseEnv,
         @FormDataParam(CoordConsts.SVC_KEY_DELTA_TESTRIG_NAME) String deltaTestrig,
         @FormDataParam(CoordConsts.SVC_KEY_DELTA_ENV_NAME) String deltaEnv,
         @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName,
         @FormDataParam(CoordConsts.SVC_KEY_PRETTY_ANSWER) String prettyAnswer) {
      try {
         _logger.info("WMS:getAnswer " + apiKey + " " + containerName + " "
               + testrigName + " " + questionName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Base testrig name");
         checkStringParam(baseEnv, "Base environment name");
         checkStringParam(questionName, "Question name");
         checkStringParam(prettyAnswer, "Retrieve pretty-printed answer");
         boolean pretty = Boolean.parseBoolean(prettyAnswer);

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         String answer = Main.getWorkMgr().getAnswer(containerName, testrigName,
               baseEnv, deltaTestrig, deltaEnv, questionName, pretty);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               new JSONObject().put(CoordConsts.SVC_KEY_ANSWER, answer)));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:getAnswer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getAnswer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      _logger.info("WMS:getInfo\n");
      try {
         JSONObject map = new JSONObject();
         map.put("Service name", "Batfish coordinator");
         map.put(CoordConsts.SVC_KEY_VERSION, Version.getVersion());
         map.put("APIs",
               "Enter ../application.wadl (relative to your URL) to see supported methods");

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, map));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Fetches the specified object from the specified container, testrig
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param objectName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_GET_OBJECT)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getObject(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_OBJECT_NAME) String objectName) {
      try {
         _logger.info(
               "WMS:getObject " + testrigName + " --> " + objectName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");
         checkStringParam(objectName, "Object name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         java.nio.file.Path file = Main.getWorkMgr()
               .getTestrigObject(containerName, testrigName, objectName);

         if (file == null || !Files.exists(file)) {
            return Response.status(Response.Status.NOT_FOUND)
                  .entity("File not found").type(MediaType.TEXT_PLAIN).build();
         }

         String filename = file.getFileName().toString();
         return Response.ok(file.toFile(), MediaType.APPLICATION_OCTET_STREAM)
               .header("Content-Disposition",
                     "attachment; filename=\"" + filename + "\"")
               .header(CoordConsts.SVC_FILENAME_HDR, filename).build();
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         return Response.status(Response.Status.BAD_REQUEST)
               .entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getObject exception: " + stackTrace);
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .entity(e.getCause()).type(MediaType.TEXT_PLAIN).build();
      }
   }

   @GET
   @Path(CoordConsts.SVC_RSC_GETSTATUS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         _logger.info("WMS:getWorkQueueStatus\n");
         JSONObject retObject = Main.getWorkMgr().getStatusJson();
         retObject.put("service-version", Version.getVersion());
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, retObject));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Obtains the counts of completed and incomplete work iterms
    *
    * @param apiKey
    * @param workId
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_GET_WORKSTATUS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkStatus(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_WORKID) String workId) {
      try {
         _logger.info("WMS:getWorkStatus " + workId + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(workId, "work id");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

         if (work == null) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_FAILURE,
                  "work with the specified id does not exist or is not inaccessible"));
         }

         checkContainerAccessibility(apiKey,
               work.getWorkItem().getContainerName());

         BatfishObjectMapper mapper = new BatfishObjectMapper();
         String taskStr = mapper
               .writeValueAsString(work.getLastTaskCheckResult());

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject()
                     .put(CoordConsts.SVC_KEY_WORKSTATUS,
                           work.getStatus().toString())
                     .put(CoordConsts.SVC_KEY_TASKSTATUS, taskStr))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:getWorkStatus exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkStatus exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Initializes a new container
    *
    * @param apiKey
    * @param containerPrefix
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_INIT_CONTAINER)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray initContainer(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_PREFIX) String containerPrefix) {
      try {
         _logger.info("WMS:initContainer " + containerPrefix + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         if (containerName == null || containerName.equals("")) {
            checkStringParam(containerPrefix, "Container prefix");
         }

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         String outputContainerName = Main.getWorkMgr()
               .initContainer(containerName, containerPrefix);

         Main.getAuthorizer().authorizeContainer(apiKey, outputContainerName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put(CoordConsts.SVC_KEY_CONTAINER_NAME,
                     outputContainerName))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:initContainer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:initContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Lists the analyses under the specified container
    *
    * @param apiKey
    * @param containerName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_LIST_ANALYSES)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listAnalyses(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName) {
      try {
         _logger
               .info("WMS:listAnalyses " + apiKey + " " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         JSONObject retObject = new JSONObject();

         for (String analysisName : Main.getWorkMgr()
               .listAnalyses(containerName)) {

            JSONObject analysisJson = new JSONObject();

            for (String questionName : Main.getWorkMgr()
                  .listAnalysisQuestions(containerName, analysisName)) {
               String questionText = Main.getWorkMgr().getAnalysisQuestion(
                     containerName, analysisName, questionName);

               analysisJson.put(questionName, new JSONObject(questionText));
            }

            retObject.put(analysisName, analysisJson);
         }

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put(CoordConsts.SVC_KEY_ANALYSIS_LIST, retObject))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:listTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * List the containers that the specified API key can access
    *
    * @param apiKey
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_LIST_CONTAINERS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listContainers(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion) {
      try {
         _logger.info("WMS:listContainers " + apiKey + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         if (!_settings.getDefaultKeyListings()
               && apiKey.equals(CoordConsts.DEFAULT_API_KEY)) {
            throw new AccessControlException(
                  "Listing containers is not allowed with Default API key");
         }

         SortedSet<String> containerList = Main.getWorkMgr()
               .listContainers(apiKey);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put(CoordConsts.SVC_KEY_CONTAINER_LIST,
                     new JSONArray(containerList)))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger
               .error("WMS:listContainers exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Lists the environments under the specified container, testrig
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_LIST_ENVIRONMENTS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listEnvironments(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName) {
      try {
         _logger.info(
               "WMS:listEnvironments " + apiKey + " " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         SortedSet<String> environmentList = Main.getWorkMgr()
               .listEnvironments(containerName, testrigName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put(CoordConsts.SVC_KEY_ENVIRONMENT_LIST,
                     new JSONArray(environmentList)))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error(
               "WMS:listEnvironment exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listEnvironment exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Lists the questions under the specified container, testrig
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_LIST_QUESTIONS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listQuestions(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName) {
      try {
         _logger.info(
               "WMS:listQuestions " + apiKey + " " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         SortedSet<String> questions = Main.getWorkMgr()
               .listQuestions(containerName, testrigName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put(CoordConsts.SVC_KEY_QUESTION_LIST,
                     new JSONArray(questions)))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:listQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listQuestion exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Lists the testrigs under the specified container
    *
    * @param apiKey
    * @param containerName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_LIST_TESTRIGS)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listTestrigs(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName) {
      try {
         _logger
               .info("WMS:listTestrigs " + apiKey + " " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         JSONArray retArray = new JSONArray();

         SortedSet<String> testrigList = Main.getWorkMgr()
               .listTestrigs(containerName);

         for (String testrig : testrigList) {
            String testrigInfo = Main.getWorkMgr().getTestrigInfo(containerName,
                  testrig);

            JSONObject jObject = new JSONObject()
                  .put(CoordConsts.SVC_KEY_TESTRIG_NAME, testrig)
                  .put(CoordConsts.SVC_KEY_TESTRIG_INFO, testrigInfo);

            retArray.put(jObject);
         }

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put(CoordConsts.SVC_KEY_TESTRIG_LIST, retArray))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:listTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Uploads a custom object under container, testrig.
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param qName
    * @param fileStream
    * @param paramFileStream
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_PUT_OBJECT)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray putObject(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_OBJECT_NAME) String objectName,
         @FormDataParam(CoordConsts.SVC_KEY_FILE) InputStream fileStream) {
      try {
         _logger.info("WMS:uploadQuestion " + apiKey + " " + containerName + " "
               + testrigName + " / " + objectName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");
         checkStringParam(objectName, "Object name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().putObject(containerName, testrigName, objectName,
               fileStream);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put("result", "successfully uploaded custom object"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error(
               "WMS:uploadCustomObject exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadCustomObject exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Queues new work
    *
    * @param apiKey
    * @param workItemStr
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_QUEUE_WORK)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr) {
      try {
         _logger.info("WMS:queueWork " + apiKey + " " + workItemStr + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(workItemStr, "Workitem string");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         WorkItem workItem = WorkItem.FromJsonString(workItemStr);

         checkContainerAccessibility(apiKey, workItem.getContainerName());

         boolean result = Main.getWorkMgr().queueWork(workItem);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               (new JSONObject().put("result", result))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:queueWork exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:queueWork exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   @GET
   @Path("test")
   @Produces(MediaType.TEXT_PLAIN)
   public String test() {
      try {
         _logger.info("WMS:getInfo\n");
         JSONArray id = new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS,
               Main.getWorkMgr().getStatusJson()));

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

   /**
    * Uploads a new environment under the container, testrig
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param envName
    * @param fileStream
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_UPLOAD_ENV)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadEnvironment(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_BASE_ENV_NAME) String baseEnvName,
         @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String envName,
         @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream) {
      try {
         _logger.info("WMS:uploadEnvironment " + apiKey + " " + containerName
               + " " + testrigName + " / " + envName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");
         checkStringParam(envName, "Environment name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().uploadEnvironment(containerName, testrigName,
               baseEnvName, envName, fileStream);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put("result", "successfully uploaded environment"))));

      }
      catch (BatfishException e) {
         _logger.error(
               "WMS:uploadEnvironment exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadEnvironment exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Uploads a new questions under container, testrig. Expects a file
    * containing the question and a file containing the parameters.
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param qName
    * @param fileStream
    * @param paramFileStream
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_UPLOAD_QUESTION)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadQuestion(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String qName,
         @FormDataParam(CoordConsts.SVC_KEY_FILE) InputStream fileStream,
         @FormDataParam(CoordConsts.SVC_KEY_FILE2) InputStream paramFileStream) {
      try {
         _logger.info("WMS:uploadQuestion " + apiKey + " " + containerName + " "
               + testrigName + " / " + qName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");
         checkStringParam(qName, "Question name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().uploadQuestion(containerName, testrigName, qName,
               fileStream, paramFileStream);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put("result", "successfully uploaded question"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger
               .error("WMS:uploadQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadQuestion exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }

   /**
    * Uploads a new testrig under the specified container
    *
    * @param apiKey
    * @param containerName
    * @param testrigName
    * @param fileStream
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_RSC_UPLOAD_TESTRIG)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadTestrig(
         @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
         @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
         @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
         @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream) {
      try {
         _logger.info("WMS:uploadTestrig " + apiKey + " " + containerName + " "
               + testrigName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");
         checkStringParam(testrigName, "Testrig name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().uploadTestrig(containerName, testrigName,
               fileStream);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, (new JSONObject()
                     .put("result", "successfully uploaded testrig"))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException
            | ZipException e) {
         _logger.error("WMS:uploadTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
      }
   }
}
