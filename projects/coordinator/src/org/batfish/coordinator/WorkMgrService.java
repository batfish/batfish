package org.batfish.coordinator;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.*;
import org.batfish.common.util.BatfishObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
@Path(CoordConsts.SVC_BASE_WORK_MGR)
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
   @Path(CoordConsts.SVC_CHECK_API_KEY_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray checkApiKey(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion) {
      try {
         _logger.info("WMS:checkApiKey " + apiKey + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");

         checkClientVersion(clientVersion);

         if (Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
                  new JSONObject().put(CoordConsts.SVC_API_KEY, true)));
         }
         else {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
                  new JSONObject().put(CoordConsts.SVC_API_KEY, false)));
         }
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:initContainer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:initContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
    * Deletes the specified container
    *
    * @param apiKey
    * @param containerName
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_DEL_CONTAINER_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delContainer(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName) {
      try {
         _logger.info("WMS:delContainer " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerName, "Container name");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);
         checkContainerAccessibility(apiKey, containerName);

         Main.getWorkMgr().delContainer(containerName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:delContainer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_DEL_ENVIRONMENT_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delEnvironment(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_ENV_NAME_KEY) String envName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName) {
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

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger
               .error("WMS:delEnvironment exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delEnvironment exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_DEL_QUESTION_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delQuestion(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_QUESTION_NAME_KEY) String questionName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName) {
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

         Main.getWorkMgr().delQuestion(containerName, testrigName,
               questionName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:delQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delQuestion exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_DEL_TESTRIG_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray delTestrig(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName) {
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

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("result", "true"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:delTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:delTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      _logger.info("WMS:getInfo\n");
      return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
            "Batfish coordinator v" + Version.getVersion()
                  + ". Enter ../application.wadl (relative to your URL) to see supported methods"));
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
   @Path(CoordConsts.SVC_GET_OBJECT_RSC)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getObject(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_OBJECT_NAME_KEY) String objectName) {
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

         File file = Main.getWorkMgr().getObject(containerName, testrigName,
               objectName);

         if (file == null) {
            return Response.status(Response.Status.NOT_FOUND)
                  .entity("File not found").type(MediaType.TEXT_PLAIN).build();
         }

         return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
               .header("Content-Disposition",
                     "attachment; filename=\"" + file.getName() + "\"")
               .header(CoordConsts.SVC_FILENAME_HDR, file.getName()).build();
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
   @Path(CoordConsts.SVC_GETSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         _logger.info("WMS:getWorkQueueStatus\n");
         JSONObject retObject = Main.getWorkMgr().getStatusJson();
         retObject.put("service-version", Version.getVersion());
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, retObject));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_GET_WORKSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkStatus(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_WORKID_KEY) String workId) {
      try {
         _logger.info("WMS:getWorkStatus " + workId + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(workId, "work id");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

         if (work == null) {
            return new JSONArray(Arrays.asList(CoordConsts.SVC_FAILURE_KEY,
                  "work with the specified id does not exist or is not inaccessible"));
         }

         checkContainerAccessibility(apiKey,
               work.getWorkItem().getContainerName());

         BatfishObjectMapper mapper = new BatfishObjectMapper();
         String taskStr = mapper
               .writeValueAsString(work.getLastTaskCheckResult());

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject()
                     .put(CoordConsts.SVC_WORKSTATUS_KEY,
                           work.getStatus().toString())
                     .put(CoordConsts.SVC_TASKSTATUS_KEY, taskStr))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:getWorkStatus exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:getWorkStatus exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_INIT_CONTAINER_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray initContainer(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_PREFIX_KEY) String containerPrefix) {
      try {
         _logger.info("WMS:initContainer " + containerPrefix + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");
         checkStringParam(containerPrefix, "Container prefix");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         String containerName = Main.getWorkMgr()
               .initContainer(containerPrefix);

         Main.getAuthorizer().authorizeContainer(apiKey, containerName);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, (new JSONObject()
                     .put(CoordConsts.SVC_CONTAINER_NAME_KEY, containerName))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:initContainer exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:initContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }

   /**
    * List the containers that the specified API key can access
    *
    * @param apiKey
    * @return
    */
   @POST
   @Path(CoordConsts.SVC_LIST_CONTAINERS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listContainers(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion) {
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

         String[] containerList = Main.getWorkMgr().listContainers(apiKey);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put(CoordConsts.SVC_CONTAINER_LIST_KEY,
                     new JSONArray(Arrays.asList(containerList))))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger
               .error("WMS:listContainers exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listContainer exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_LIST_ENVIRONMENTS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listEnvironments(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName) {
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

         String[] environmentList = Main.getWorkMgr()
               .listEnvironments(containerName, testrigName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put(CoordConsts.SVC_ENVIRONMENT_LIST_KEY,
                     new JSONArray(Arrays.asList(environmentList))))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error(
               "WMS:listEnvironment exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listEnvironment exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_LIST_QUESTIONS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listQuestions(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName) {
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

         String[] questionList = Main.getWorkMgr().listQuestions(containerName,
               testrigName);

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put(CoordConsts.SVC_QUESTION_LIST_KEY,
                     new JSONArray(Arrays.asList(questionList))))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:listQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listQuestion exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_LIST_TESTRIGS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray listTestrigs(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName) {
      try {
         _logger
               .info("WMS:listTestrigs " + apiKey + " " + containerName + "\n");

         checkStringParam(apiKey, "API key");
         checkStringParam(clientVersion, "Client version");

         checkApiKeyValidity(apiKey);
         checkClientVersion(clientVersion);

         if (!_settings.getDefaultKeyListings()
               && apiKey.equals(CoordConsts.DEFAULT_API_KEY)) {
            throw new AccessControlException(
                  "Listing all testrigs is not allowed with Default API key");
         }

         List<String> containerList = new LinkedList<>();

         if (containerName == null || containerName.equals("")) {
            containerList.addAll(
                  Arrays.asList(Main.getWorkMgr().listContainers(apiKey)));
         }
         else {
            checkContainerAccessibility(apiKey, containerName);
            containerList.add(containerName);
         }

         JSONArray retArray = new JSONArray();

         for (String container : containerList) {
            String[] testrigList = Main.getWorkMgr().listTestrigs(container);

            for (String testrig : testrigList) {
               String testrigInfo = Main.getWorkMgr().getTestrigInfo(container,
                     testrig);

               JSONObject jObject = new JSONObject()
                     .put(CoordConsts.SVC_TESTRIG_NAME_KEY,
                           container + "/" + testrig)
                     .put(CoordConsts.SVC_TESTRIG_INFO_KEY, testrigInfo);

               retArray.put(jObject);
            }
         }

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, (new JSONObject()
                     .put(CoordConsts.SVC_TESTRIG_LIST_KEY, retArray))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:listTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:listTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_PUT_OBJECT_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray putObject(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_OBJECT_NAME_KEY) String objectName,
         @FormDataParam(CoordConsts.SVC_FILE_KEY) InputStream fileStream) {
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
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, (new JSONObject()
                     .put("result", "successfully uploaded custom object"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error(
               "WMS:uploadCustomObject exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadCustomObject exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_QUEUE_WORK_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray queueWork(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_WORKITEM_KEY) String workItemStr) {
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

         return new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("result", result))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger.error("WMS:queueWork exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:queueWork exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }

   @GET
   @Path("test")
   @Produces(MediaType.TEXT_PLAIN)
   public String test() {
      try {
         _logger.info("WMS:getInfo\n");
         JSONArray id = new JSONArray(Arrays.asList(CoordConsts.SVC_SUCCESS_KEY,
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
   @Path(CoordConsts.SVC_UPLOAD_ENV_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadEnvironment(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_ENV_NAME_KEY) String envName,
         @FormDataParam(CoordConsts.SVC_ZIPFILE_KEY) InputStream fileStream) {
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
               envName, fileStream);

         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, (new JSONObject()
                     .put("result", "successfully uploaded environment"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException
            | ZipException e) {
         _logger.error(
               "WMS:uploadEnvironment exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadEnvironment exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_UPLOAD_QUESTION_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadQuestion(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_QUESTION_NAME_KEY) String qName,
         @FormDataParam(CoordConsts.SVC_FILE_KEY) InputStream fileStream,
         @FormDataParam(CoordConsts.SVC_FILE2_KEY) InputStream paramFileStream) {
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
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, (new JSONObject()
                     .put("result", "successfully uploaded question"))));

      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException e) {
         _logger
               .error("WMS:uploadQuestion exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadQuestion exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
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
   @Path(CoordConsts.SVC_UPLOAD_TESTRIG_RSC)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray uploadTestrig(
         @FormDataParam(CoordConsts.SVC_API_KEY) String apiKey,
         @FormDataParam(CoordConsts.SVC_VERSION_KEY) String clientVersion,
         @FormDataParam(CoordConsts.SVC_CONTAINER_NAME_KEY) String containerName,
         @FormDataParam(CoordConsts.SVC_TESTRIG_NAME_KEY) String testrigName,
         @FormDataParam(CoordConsts.SVC_ZIPFILE_KEY) InputStream fileStream) {
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
               Arrays.asList(CoordConsts.SVC_SUCCESS_KEY, (new JSONObject()
                     .put("result", "successfully uploaded testrig"))));
      }
      catch (FileExistsException | FileNotFoundException
            | IllegalArgumentException | AccessControlException
            | ZipException e) {
         _logger.error("WMS:uploadTestrig exception: " + e.getMessage() + "\n");
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("WMS:uploadTestrig exception: " + stackTrace);
         return new JSONArray(
               Arrays.asList(CoordConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }
}
