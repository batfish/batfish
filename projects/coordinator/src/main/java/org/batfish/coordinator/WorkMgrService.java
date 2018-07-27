package org.batfish.coordinator;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.opentracing.util.GlobalTracer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileExistsException;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.AnalysisMetadataMgr.AnalysisType;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.AutocompleteSuggestion.CompletionType;
import org.batfish.datamodel.pojo.WorkStatus;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataParam;

/** */
@Path(CoordConsts.SVC_CFG_WORK_MGR)
public class WorkMgrService {

  BatfishLogger _logger = Main.getLogger();

  private static JSONArray successResponse(Object entity) {
    return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, entity));
  }

  private static JSONArray failureResponse(Object entity) {
    return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_FAILURE, entity));
  }

  @POST
  @Path(CoordConsts.SVC_RSC_AUTO_COMPLETE)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray autoComplete(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      /* Optional: not needed for some completions */
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_COMPLETION_TYPE) String completionType,
      @FormDataParam(CoordConsts.SVC_KEY_QUERY) String query,
      /* Optional */
      @FormDataParam(CoordConsts.SVC_KEY_MAX_SUGGESTIONS) String maxSuggestions) {
    try {
      _logger.infof("WMS:autoComplete %s %s %s\n", completionType, query, maxSuggestions);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(completionType, "Completion type");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      List<AutocompleteSuggestion> answer =
          Main.getWorkMgr()
              .autoComplete(
                  networkName,
                  snapshotName,
                  CompletionType.valueOf(completionType.toUpperCase()),
                  query,
                  Strings.isNullOrEmpty(maxSuggestions)
                      ? Integer.MAX_VALUE
                      : Integer.parseInt(maxSuggestions));
      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_SUGGESTIONS, answer));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:autoComplete exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:autoComplete exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Check if an API key is valid
   *
   * @param apiKey The API key to check
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_CHECK_API_KEY)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray checkApiKey(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion) {
    try {
      _logger.infof("WMS:checkApiKey %s\n", apiKey);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");

      checkClientVersion(clientVersion);
      boolean valid = Main.getAuthorizer().isValidWorkApiKey(apiKey);
      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_API_KEY, valid));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:checkApiKey exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:checkApiKey exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  private void checkApiKeyValidity(String apiKey) {
    if (!Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
      throw new AccessControlException("Invalid API key: " + apiKey);
    }
  }

  private void checkClientVersion(String clientVersion) {
    Version.checkCompatibleVersion("Service", "Client", clientVersion);
  }

  private void checkNetworkAccessibility(String apiKey, String networkName) {
    if (!Main.getAuthorizer().isAccessibleContainer(apiKey, networkName, true)) {
      throw new AccessControlException(
          String.format("network '%s' is not accessible by the api key '%s", networkName, apiKey));
    }
  }

  private void checkStringParam(String paramStr, String parameterName) {
    if (Strings.isNullOrEmpty(paramStr)) {
      throw new IllegalArgumentException(parameterName + " is missing or empty");
    }
  }

  /**
   * Configures an analysis for the network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param networkName The name of the network to configure
   * @param newAnalysisStr The string representation of a new analysis to configure
   * @param analysisName The name of the analysis to configure
   * @param addQuestionsStream A stream providing the questions for the analysis
   * @param delQuestions A list of questions to delete from the analysis
   * @param suggested An optional boolean indicating whether analysis is suggested (default: false).
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_CONFIGURE_ANALYSIS)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray configureAnalysis(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_NEW_ANALYSIS) String newAnalysisStr,
      @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_NAME) String analysisName,
      @FormDataParam(CoordConsts.SVC_KEY_FILE) InputStream addQuestionsStream,
      @FormDataParam(CoordConsts.SVC_KEY_DEL_ANALYSIS_QUESTIONS) String delQuestions,
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_SUGGESTED) Boolean suggested) {
    try {
      _logger.infof(
          "WMS:configureAnalysis %s %s %s %s %s\n",
          apiKey, networkName, newAnalysisStr, analysisName, delQuestions);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(analysisName, "Analysis name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Map<String, String> questionsToAdd = new HashMap<>();
      if (addQuestionsStream != null) {
        Map<String, Object> streamValue;
        try {
          streamValue =
              BatfishObjectMapper.mapper()
                  .readValue(addQuestionsStream, new TypeReference<Map<String, Object>>() {});
          for (Entry<String, Object> entry : streamValue.entrySet()) {
            String textValue = BatfishObjectMapper.writePrettyString(entry.getValue());
            questionsToAdd.put(entry.getKey(), textValue);
          }
        } catch (IOException e) {
          throw new BatfishException("Failed to read question JSON from input stream", e);
        }
      }
      boolean newAnalysis = !Strings.isNullOrEmpty(newAnalysisStr);
      List<String> questionsToDelete = new ArrayList<>();
      if (!Strings.isNullOrEmpty(delQuestions)) {
        JSONArray delQuestionsArray = new JSONArray(delQuestions);
        for (int i = 0; i < delQuestionsArray.length(); i++) {
          questionsToDelete.add(delQuestionsArray.getString(i));
        }
      }

      Main.getWorkMgr()
          .configureAnalysis(
              networkName, newAnalysis, analysisName, questionsToAdd, questionsToDelete, suggested);

      return successResponse(new JSONObject().put("result", "successfully configured analysis"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:configureAnalysis exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:configureAnalysis exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Add/replace exceptions and assertions to question template
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param questionTemplate The template to extend (JSON string)
   * @param exceptions The exceptions to add (JSON string)
   * @param assertion The assertions to add (JSON string)
   * @return packages the JSON of the resulting template
   */
  @POST
  @Path(CoordConsts.SVC_RSC_CONFIGURE_QUESTION_TEMPLATE)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray configureQuestionTemplate(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION) String questionTemplate,
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_EXCEPTIONS) String exceptions,
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_ASSERTION) String assertion) {
    try {
      _logger.infof(
          "WMS:configureQuestionTemplate: q: %s e: %s a: %s\n",
          questionTemplate, exceptions, assertion);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(questionTemplate, "Question template");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      Question inputQuestion = Question.parseQuestion(questionTemplate);
      Question outputQuestion = inputQuestion.configureTemplate(exceptions, assertion);
      String outputQuestionStr = BatfishObjectMapper.writePrettyString(outputQuestion);

      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_QUESTION, outputQuestionStr));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:configureQuestionTemplates exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:configureQuestionTemplates exception for apikey:%s; exception:%s",
          apiKey, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Delete an analysis from the network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the analysis resides
   * @param analysisName The name of the analysis to delete
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_ANALYSIS)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray delAnalysis(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_NAME) String analysisName) {
    try {
      _logger.infof("WMS:delAnalysis %s %s %s", apiKey, networkName, analysisName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(analysisName, "Analysis name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Main.getWorkMgr().delAnalysis(networkName, analysisName);

      return successResponse(new JSONObject().put("result", "successfully configured analysis"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:delAnalysis exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:delAnalysis exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Delete the specified network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param containerName The name of the network to delete
   * @return TODO: document JSON response
   * @deprecated because containers were renamed to networks. Use {@link #delNetwork(String, String,
   *     String) delNetwork} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_CONTAINER)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray delContainer(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName) {
    return delNetwork(apiKey, clientVersion, containerName);
  }

  /**
   * Delete the specified network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param networkName The name of the network to delete
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_NETWORK)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray delNetwork(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName) {
    try {
      _logger.infof("WMS:delNetwork %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      boolean status = Main.getWorkMgr().delContainer(networkName);

      return successResponse(new JSONObject().put("result", status));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:delNetwork exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:delNetwork exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Deletes the specified environment under the specified network and snapshot
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the environment and snapshot reside
   * @param envName The name of the environment to delete
   * @param snapshotName The name of the snapshot in which the environment resides
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_ENVIRONMENT)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray delEnvironment(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String envName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName) {
    try {
      _logger.infof("WMS:delEnvironment %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");
      checkStringParam(envName, "Environment name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Main.getWorkMgr().delEnvironment(networkName, snapshotName, envName);

      return successResponse(new JSONObject().put("result", "true"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:delEnvironment exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:delEnvironment exception for apikey:%s in network:%s, snapshot:%s; exception:%s",
          apiKey, networkName, snapshotName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Delete the specified question under the specified network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the question resides
   * @param questionName The name of the question to delete
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_QUESTION)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray delQuestion(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName) {
    try {
      _logger.infof("WMS:delQuestion %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(questionName, "Question name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Main.getWorkMgr().delQuestion(networkName, questionName);

      return successResponse(new JSONObject().put("result", "true"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:delQuestion exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:delQuestion exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Deletes the specified snapshot in the specified network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param containerName The name of the network in which the snapshot resides
   * @param testrigName The name of the snapshot to delete
   * @return TODO: document JSON response
   * @deprecated because testrigs were renamed to snapshots. Use {@link #delSnapshot(String, String,
   *     String, String) delSnapshot} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_TESTRIG)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray delTestrig(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName) {
    return delSnapshot(apiKey, clientVersion, containerName, testrigName);
  }

  /**
   * Deletes the specified snapshot in the specified network
   *
   * @param apiKey The API key of the requester
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the snapshot resides
   * @param snapshotName The name of the snapshot to delete
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_SNAPSHOT)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray delSnapshot(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName) {
    try {
      _logger.infof("WMS:delSnapshot %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Main.getWorkMgr().delTestrig(networkName, snapshotName);

      return successResponse(new JSONObject().put("result", "true"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:delSnapshot exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:delSnapshot exception for apikey:%s in network:%s, snapshot:%s; exception:%s",
          apiKey, networkName, snapshotName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Get answers for a previously run analysis
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the analysis resides
   * @param snapshotName The name of the snapshot on which the analysis was run
   * @param baseEnv The name of the base environment on which the analysis was run
   * @param deltaSnapshot The name of the delta snapshot on which the analysis was run
   * @param deltaEnv The name of the delta environment on which the analysis was run
   * @param analysisName The name of the analysis
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_ANALYSIS_ANSWERS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAnalysisAnswers(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String baseEnv,
      @FormDataParam(CoordConsts.SVC_KEY_DELTA_TESTRIG_NAME) String deltaSnapshot,
      @FormDataParam(CoordConsts.SVC_KEY_DELTA_ENV_NAME) String deltaEnv,
      @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_NAME) String analysisName,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr /* optional */) {
    try {
      _logger.infof(
          "WMS:getAnalysisAnswers %s %s %s %s\n", apiKey, networkName, snapshotName, analysisName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Base snapshot name");
      checkStringParam(baseEnv, "Base environment name");
      checkStringParam(analysisName, "Analysis name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      JSONObject response = new JSONObject();

      if (!Strings.isNullOrEmpty(workItemStr)) {
        WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);
        if (!workItem.getContainerName().equals(networkName)
            || !workItem.getTestrigName().equals(snapshotName)) {
          return failureResponse(
              "Mismatch in parameters: WorkItem is not for the supplied network or snapshot");
        }
        QueuedWork work = Main.getWorkMgr().getMatchingWork(workItem, QueueType.INCOMPLETE);
        if (work != null) {
          String taskStr = BatfishObjectMapper.writePrettyString(work.getLastTaskCheckResult());
          response
              .put(CoordConsts.SVC_KEY_WORKID, work.getWorkItem().getId())
              .put(CoordConsts.SVC_KEY_WORKSTATUS, work.getStatus().toString())
              .put(CoordConsts.SVC_KEY_TASKSTATUS, taskStr);
        }
      }

      Map<String, String> answers =
          Main.getWorkMgr()
              .getAnalysisAnswers(
                  networkName, snapshotName, baseEnv, deltaSnapshot, deltaEnv, analysisName);

      String answersStr = BatfishObjectMapper.writePrettyString(answers);

      return successResponse(response.put(CoordConsts.SVC_KEY_ANSWERS, answersStr));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getAnalysisAnswers exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getAnalysisAnswers exception for apikey:%s in network:%s, snapshot:%s, "
              + "deltasnapshot:%s; exception:%s",
          apiKey,
          networkName,
          snapshotName,
          deltaSnapshot == null ? "" : deltaSnapshot,
          stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Get answer to a previously asked question
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the question was asked
   * @param snapshotName The name of the snapshot on which the question was asked
   * @param baseEnv The name of the base environment on which the question was asked
   * @param deltaSnapshot The name of the delta snapshot on which the question was asked
   * @param deltaEnv The name of the delta environment on which the question was asked
   * @param questionName The name of the question
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_ANSWER)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAnswer(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_ENV_NAME) String baseEnv,
      @FormDataParam(CoordConsts.SVC_KEY_DELTA_TESTRIG_NAME) String deltaSnapshot,
      @FormDataParam(CoordConsts.SVC_KEY_DELTA_ENV_NAME) String deltaEnv,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr /* optional */) {
    try {
      _logger.infof("WMS:getAnswer %s %s %s %s\n", apiKey, networkName, snapshotName, questionName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Base snapshot name");
      checkStringParam(baseEnv, "Base environment name");
      checkStringParam(questionName, "Question name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      if (!Strings.isNullOrEmpty(workItemStr)) {
        WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);
        if (!workItem.getContainerName().equals(networkName)
            || !workItem.getTestrigName().equals(snapshotName)) {
          return failureResponse(
              "Mismatch in parameters: WorkItem is not for the supplied network or snapshot");
        }
        QueuedWork work = Main.getWorkMgr().getMatchingWork(workItem, QueueType.INCOMPLETE);
        if (work != null) {
          String taskStr = BatfishObjectMapper.writePrettyString(work.getLastTaskCheckResult());
          return successResponse(
              new JSONObject()
                  .put(CoordConsts.SVC_KEY_WORKID, work.getWorkItem().getId())
                  .put(CoordConsts.SVC_KEY_WORKSTATUS, work.getStatus().toString())
                  .put(CoordConsts.SVC_KEY_TASKSTATUS, taskStr));
        }
      }

      String answer =
          Main.getWorkMgr()
              .getAnswer(networkName, snapshotName, baseEnv, deltaSnapshot, deltaEnv, questionName);

      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_ANSWER, answer));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getAnswer exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getAnswer exception for apikey:%s in network:%s, snapshot:%s, deltasnapshot:%s; "
              + "exception:%s",
          apiKey,
          networkName,
          snapshotName,
          deltaSnapshot == null ? "" : deltaSnapshot,
          stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Get content of the configuration file
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the question was asked
   * @param snapshotName The name of the snapshot in which the question was asked
   * @param configName The name of the configuration file in which the question was asked
   * @return A {@link Response Response} with an entity consists either a string of the file content
   *     of the configuration file {@code configName} or an error message if: the configuration file
   *     {@code configName} does not exist or the {@code apiKey} has no access to the network {@code
   *     networkName}
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_CONFIGURATION)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfiguration(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_CONFIGURATION_NAME) String configName) {
    try {
      _logger.infof("WMS:getConfiguration %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      java.nio.file.Path networkDir =
          Main.getSettings().getContainersLocation().resolve(networkName).toAbsolutePath();
      if (networkDir == null || !Files.exists(networkDir)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity("Network '" + networkName + "' not found")
            .type(MediaType.TEXT_PLAIN)
            .build();
      }

      checkNetworkAccessibility(apiKey, networkName);

      String configContent =
          Main.getWorkMgr().getConfiguration(networkName, snapshotName, configName);

      return Response.ok(configContent).build();
    } catch (AccessControlException e) {
      return Response.status(Status.FORBIDDEN)
          .entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN)
          .build();
    } catch (BatfishException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN)
          .build();
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getConfiguration exception: %s", stackTrace);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(e.getCause())
          .type(MediaType.TEXT_PLAIN)
          .build();
    }
  }

  /**
   * Get information of the network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The name of the network in which the question was asked
   * @return A {@link Response Response} with an entity consists either a json representation of the
   *     network {@code containerName} or an error message if: the network {@code containerName}
   *     does not exist or the {@code apiKey} has no access to the network {@code containerName}
   * @deprecated because containers were renamed to networks. Use {@link #getNetwork(String, String,
   *     String) getNetwork} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_CONTAINER)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public Response getContainer(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName) {
    return getNetwork(apiKey, clientVersion, containerName);
  }

  /**
   * Get information of the network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the question was asked
   * @return A {@link Response Response} with an entity consists either a json representation of the
   *     network {@code networkName} or an error message if: the network {@code networkName} does
   *     not exist or the {@code apiKey} has no access to the network {@code networkName}
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_NETWORK)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getNetwork(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName) {
    try {
      _logger.infof("WMS:getNetwork %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      java.nio.file.Path networkDir =
          Main.getSettings().getContainersLocation().resolve(networkName).toAbsolutePath();
      if (networkDir == null || !Files.exists(networkDir)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity("Network '" + networkName + "' not found")
            .type(MediaType.TEXT_PLAIN)
            .build();
      }

      checkNetworkAccessibility(apiKey, networkName);

      Container network = Main.getWorkMgr().getContainer(networkDir);
      String networkString = BatfishObjectMapper.writeString(network);

      return Response.ok(networkString).build();
    } catch (AccessControlException e) {
      return Response.status(Status.FORBIDDEN)
          .entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN)
          .build();
    } catch (BatfishException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN)
          .build();
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getNetwork exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(e.getCause())
          .type(MediaType.TEXT_PLAIN)
          .build();
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
      map.put("APIs", "Enter ../application.wadl (relative to your URL) to see supported methods");

      return successResponse(map);
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getInfo exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Fetches the specified object from the specified network, snapshot
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The network in which the object resides
   * @param snapshotName The snapshot in which the object resides
   * @param objectName The name of the object
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_OBJECT)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getObject(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_OBJECT_NAME) String objectName) {
    try {
      _logger.infof("WMS:getObject %s --> %s\n", snapshotName, objectName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");
      checkStringParam(objectName, "Object name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      java.nio.file.Path file =
          Main.getWorkMgr().getTestrigObject(networkName, snapshotName, objectName);

      if (file == null || !Files.exists(file)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity("File not found")
            .type(MediaType.TEXT_PLAIN)
            .build();
      }

      String filename = file.getFileName().toString();
      return Response.ok(file.toFile(), MediaType.APPLICATION_OCTET_STREAM)
          .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
          .header(CoordConsts.SVC_FILENAME_HDR, filename)
          .build();
    } catch (IllegalArgumentException | AccessControlException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN)
          .build();
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getObject exception for apikey:%s in network:%s, snapshot:%s; exception:%s",
          apiKey, networkName, snapshotName, stackTrace);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(e.getCause())
          .type(MediaType.TEXT_PLAIN)
          .build();
    }
  }

  @POST
  @Path(CoordConsts.SVC_RSC_GET_PARSING_RESULTS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getParsingResults(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName) {
    try {
      _logger.infof("WMS:getParsingResults %s %s %s\n", apiKey, networkName, snapshotName);
      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      return successResponse(Main.getWorkMgr().getParsingResults(networkName, snapshotName));
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getParsingResults exception for apikey:%s in network:%s, snapshot:%s; exception:%s",
          apiKey, networkName, snapshotName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  @POST
  @Path(CoordConsts.SVC_RSC_GET_QUESTION_TEMPLATES)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getQuestionTemplates(@FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey) {
    try {
      _logger.infof("WMS:getQuestionTemplates %s\n", apiKey);

      checkStringParam(apiKey, "API key");

      checkApiKeyValidity(apiKey);

      Map<String, String> questionTemplates = Main.getQuestionTemplates();

      if (questionTemplates == null) {
        return failureResponse("Question templates dir is not configured");
      } else {
        return successResponse(
            new JSONObject().put(CoordConsts.SVC_KEY_QUESTION_LIST, questionTemplates));
      }
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getQuestionTemplates exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  @GET
  @Path(CoordConsts.SVC_RSC_GETSTATUS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getStatus() {
    try {
      _logger.info("WMS:getStatus\n");
      JSONObject retObject = Main.getWorkMgr().getStatusJson();
      retObject.put("service-version", Version.getVersion());
      return successResponse(retObject);
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getStatus exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Obtain the counts of completed and incomplete work items
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param workId The work ID to check
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_WORKSTATUS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getWorkStatus(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_WORKID) String workId) {
    try {
      _logger.infof("WMS:getWorkStatus %s\n", workId);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(workId, "work id");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

      if (work == null) {
        return failureResponse("work with the specified id does not exist or is not inaccessible");
      }

      checkNetworkAccessibility(apiKey, work.getWorkItem().getContainerName());

      String taskStr = BatfishObjectMapper.writePrettyString(work.getLastTaskCheckResult());

      // TODO: Use pojo.WorkStatus instead of this custom Json
      return successResponse(
          new JSONObject()
              .put(CoordConsts.SVC_KEY_WORKSTATUS, work.getStatus().toString())
              .put(CoordConsts.SVC_KEY_TASKSTATUS, taskStr));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getWorkStatus exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getWorkStatus exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Initialize a new network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The name of the network to initialize (overrides {@code containerPrefix})
   * @param containerPrefix The prefix used to generate the network name (ignored if {@code
   *     containerName} is not empty)
   * @return TODO: document JSON response
   * @deprecated because containers were renamed to networks. Use {@link #initNetwork(String,
   *     String, String, String) initNetwork} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_INIT_CONTAINER)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray initContainer(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_PREFIX) String containerPrefix) {
    return initNetworkHelper(
        apiKey, clientVersion, containerName, containerPrefix, CoordConsts.SVC_KEY_CONTAINER_NAME);
  }

  /**
   * Initialize a new network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network to initialize (overrides {@code networkPrefix})
   * @param networkPrefix The prefix used to generate the network name (ignored if {@code
   *     networkName} is not empty)
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_INIT_NETWORK)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray initNetwork(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_PREFIX) String networkPrefix) {
    return initNetworkHelper(
        apiKey, clientVersion, networkName, networkPrefix, CoordConsts.SVC_KEY_NETWORK_NAME);
  }

  private JSONArray initNetworkHelper(
      String apiKey,
      String clientVersion,
      String networkName,
      String networkPrefix,
      String responseKey) {
    try {
      _logger.infof("WMS:initNetwork %s\n", networkPrefix);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      if (networkName == null || networkName.equals("")) {
        checkStringParam(networkPrefix, "Network prefix");
      }

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      String outputNetworkName = Main.getWorkMgr().initContainer(networkName, networkPrefix);
      _logger.infof("Initialized network:%s using api-key:%s\n", outputNetworkName, apiKey);

      Main.getAuthorizer().authorizeContainer(apiKey, outputNetworkName);

      return successResponse(new JSONObject().put(responseKey, outputNetworkName));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:initNetwork exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:initNetwork exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Kill the specified work
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param workId The work ID to kill
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_KILL_WORK)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray killWork(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_WORKID) String workId) {
    try {
      _logger.infof("WMS:killWork %s\n", workId);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(workId, "work id");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

      if (work == null) {
        return failureResponse("work with the specified id does not exist or is not inaccessible");
      }

      checkNetworkAccessibility(apiKey, work.getWorkItem().getContainerName());

      boolean killed = Main.getWorkMgr().killWork(work);

      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_RESULT, killed));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:killWork exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:killWork exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * List the analyses under the specified network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network whose analyses are to be listed
   * @param analysisType Optional enum {@link AnalysisType} indicating which analyses to list,
   *     keeping null equivalent to {@link AnalysisType#ALL} for backward compatibility
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_ANALYSES)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listAnalyses(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_TYPE) AnalysisType analysisType) {
    try {
      _logger.infof("WMS:listAnalyses %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      JSONObject retObject = new JSONObject();

      for (String analysisName :
          Main.getWorkMgr()
              .listAnalyses(networkName, firstNonNull(analysisType, AnalysisType.USER))) {

        JSONObject analysisJson = new JSONObject();

        for (String questionName :
            Main.getWorkMgr().listAnalysisQuestions(networkName, analysisName)) {
          String questionText =
              Main.getWorkMgr().getAnalysisQuestion(networkName, analysisName, questionName);

          analysisJson.put(questionName, new JSONObject(questionText));
        }

        retObject.put(analysisName, analysisJson);
      }

      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_ANALYSIS_LIST, retObject));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:listAnalyses exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:listAnalyses exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * List the networks that the specified API key can access
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @return TODO: document JSON response
   * @deprecated because containers were renamed to networks. Use {@link #listNetworks(String,
   *     String) listNetworks} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_CONTAINERS)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray listContainers(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion) {
    return listNetworksHelper(apiKey, clientVersion, CoordConsts.SVC_KEY_CONTAINER_LIST);
  }

  /**
   * List the networks that the specified API key can access
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_NETWORKS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listNetworks(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion) {
    return listNetworksHelper(apiKey, clientVersion, CoordConsts.SVC_KEY_NETWORK_LIST);
  }

  private JSONArray listNetworksHelper(String apiKey, String clientVersion, String responseKey) {
    _logger.infof("WMS:listNetworks %s\n", apiKey);
    try {
      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      SortedSet<String> networks = Main.getWorkMgr().listContainers(apiKey);
      return successResponse(new JSONObject().put(responseKey, networks));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:listNetworks exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:listNetworks exception for apikey:%s, exception:%s", apiKey, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Lists the environments under the specified network, snapshot
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The network in which the snapshot and environments reside
   * @param snapshotName The name of the snapshot whose environments are to be listed
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_ENVIRONMENTS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listEnvironments(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName) {
    try {
      _logger.infof("WMS:listEnvironments %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      SortedSet<String> environmentList =
          Main.getWorkMgr().listEnvironments(networkName, snapshotName);

      return successResponse(
          new JSONObject()
              .put(CoordConsts.SVC_KEY_ENVIRONMENT_LIST, new JSONArray(environmentList)));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:listEnvironments exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:listEnvironments exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * List incomplete work of the specified type for the specified network and snapshot
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network for which to list work
   * @param snapshotName (optional) The name of the snapshot for which to list work
   * @param workType (optional) The type of work to list
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_INCOMPLETE_WORK)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listIncompleteWork(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName, /* optional */
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_WORK_TYPE) WorkType workType /* optional */) {
    try {
      _logger.infof("WMS:listIncompleteWork %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      if (snapshotName != null) {
        checkStringParam(snapshotName, "Snapshot name");
      }

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      List<WorkStatus> workList = new LinkedList<>();
      for (QueuedWork work :
          Main.getWorkMgr().listIncompleteWork(networkName, snapshotName, workType)) {
        WorkStatus workStatus =
            new WorkStatus(work.getWorkItem(), work.getStatus(), work.getLastTaskCheckResult());
        workList.add(workStatus);
      }

      return successResponse(
          new JSONObject()
              .put(CoordConsts.SVC_KEY_WORK_LIST, BatfishObjectMapper.writePrettyString(workList)));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:listIncompleteWork exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:listIncompleteWork exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * List the questions in the specified network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the questions reside
   * @param verbose The flag to show all questions, including internal ones
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_QUESTIONS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listQuestions(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_VERBOSE) boolean verbose) {
    try {
      _logger.infof("WMS:listQuestions %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      JSONObject retObject = new JSONObject();

      for (String questionName : Main.getWorkMgr().listQuestions(networkName, verbose)) {
        String questionText = Main.getWorkMgr().getQuestion(networkName, questionName);

        retObject.put(questionName, new JSONObject(questionText));
      }

      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_QUESTION_LIST, retObject));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:listQuestions exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:listQuestions exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * List the snapshots under the specified network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The name of the network whose testrigs are to be listed
   * @return TODO: document JSON response
   * @deprecated because testrigs were renamed to snapshots. Use {@link #listSnapshots(String,
   *     String, String) listSnapshots} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_TESTRIGS)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray listTestrigs(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName) {
    return listSnapshotsHelper(
        apiKey,
        clientVersion,
        containerName,
        CoordConsts.SVC_KEY_TESTRIG_NAME,
        CoordConsts.SVC_KEY_TESTRIG_INFO,
        CoordConsts.SVC_KEY_TESTRIG_METADATA,
        CoordConsts.SVC_KEY_TESTRIG_LIST);
  }

  /**
   * List the snapshots under the specified network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network whose snapshots are to be listed
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_SNAPSHOTS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listSnapshots(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName) {
    return listSnapshotsHelper(
        apiKey,
        clientVersion,
        networkName,
        CoordConsts.SVC_KEY_SNAPSHOT_NAME,
        CoordConsts.SVC_KEY_SNAPSHOT_INFO,
        CoordConsts.SVC_KEY_SNAPSHOT_METADATA,
        CoordConsts.SVC_KEY_SNAPSHOT_LIST);
  }

  private JSONArray listSnapshotsHelper(
      String apiKey,
      String clientVersion,
      String networkName,
      String nameKey,
      String infoKey,
      String metadataKey,
      String listKey) {
    try {
      _logger.infof("WMS:listSnapshots %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      JSONArray retArray = new JSONArray();

      List<String> snapshotList = Main.getWorkMgr().listTestrigs(networkName);

      for (String snapshot : snapshotList) {
        try {
          String snapshotInfo = Main.getWorkMgr().getTestrigInfo(networkName, snapshot);
          TestrigMetadata ssMetadata = Main.getWorkMgr().getTestrigMetadata(networkName, snapshot);

          JSONObject jObject =
              new JSONObject()
                  .put(nameKey, snapshot)
                  .put(infoKey, snapshotInfo)
                  .put(metadataKey, BatfishObjectMapper.writePrettyString(ssMetadata));

          retArray.put(jObject);
        } catch (Exception e) {
          _logger.warnf(
              "Error listing snapshot %s in network %s: %s",
              networkName, snapshot, Throwables.getStackTraceAsString(e));
        }
      }

      return successResponse(new JSONObject().put(listKey, retArray));
    } catch (Exception e) {
      _logger.errorf(
          "WMS:listSnapshots exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, Throwables.getStackTraceAsString(e));
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Upload a custom object under the specified network, snapshot.
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the snapshot resides
   * @param snapshotName The name of the snapshot under which to upload the object
   * @param objectName The name of the object to upload
   * @param fileStream The stream from which the object is read
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_PUT_OBJECT)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray putObject(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_OBJECT_NAME) String objectName,
      @FormDataParam(CoordConsts.SVC_KEY_FILE) InputStream fileStream) {
    try {
      _logger.infof("WMS:putObject %s %s %s / %s\n", apiKey, networkName, snapshotName, objectName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");
      checkStringParam(objectName, "Object name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Main.getWorkMgr().putObject(networkName, snapshotName, objectName, fileStream);

      return successResponse(new JSONObject().put("result", "successfully uploaded custom object"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:putObject exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:putObject exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Queue a new work item
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param workItemStr The work item to queue
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_QUEUE_WORK)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray queueWork(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr) {
    try {
      _logger.infof("WMS:queueWork %s %s\n", apiKey, workItemStr);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(workItemStr, "Workitem string");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

      WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);

      checkNetworkAccessibility(apiKey, workItem.getContainerName());

      QueuedWork work = Main.getWorkMgr().getMatchingWork(workItem, QueueType.INCOMPLETE);
      if (work != null) {
        return failureResponse(new JSONObject().put("Duplicate workId", work.getId()));
      }
      boolean result = Main.getWorkMgr().queueWork(workItem);

      if (GlobalTracer.get().activeSpan() != null) {
        GlobalTracer.get().activeSpan().setTag("work-id", workItem.getId().toString());
      }

      return successResponse(new JSONObject().put("result", result));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:queueWork exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:queueWork exception for apikey:%s; exception:%s", apiKey, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Sync snapshots
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The network to sync snapshots for
   * @param pluginId The plugin id to use for syncing
   * @return TODO: document JSON response
   * @deprecated because testrigs were renamed to snapshots. Use {@link
   *     #syncSnapshotsSyncNow(String, String, String, String, String) syncSnapshots} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_SYNC_TESTRIGS_SYNC_NOW)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray syncTestrigsSyncNow(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
      @FormDataParam(CoordConsts.SVC_KEY_PLUGIN_ID) String pluginId,
      @FormDataParam(CoordConsts.SVC_KEY_FORCE) String forceStr) {
    return syncSnapshotsSyncNow(apiKey, clientVersion, containerName, pluginId, forceStr);
  }

  /**
   * Sync snapshots
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The network to sync snapshots for
   * @param pluginId The plugin id to use for syncing
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_SYNC_SNAPSHOTS_SYNC_NOW)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray syncSnapshotsSyncNow(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_PLUGIN_ID) String pluginId,
      @FormDataParam(CoordConsts.SVC_KEY_FORCE) String forceStr) {
    try {
      _logger.infof("WMS:syncSnapshotsSyncNow %s %s %s\n", apiKey, networkName, pluginId);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(pluginId, "Plugin Id");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      boolean force = !Strings.isNullOrEmpty(forceStr) && Boolean.parseBoolean(forceStr);

      int numCommits = Main.getWorkMgr().syncTestrigsSyncNow(networkName, pluginId, force);

      return successResponse(new JSONObject().put("numCommits", numCommits));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:syncSnapshotsSyncNow exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:syncSnapshotsSyncNow exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Update settings for syncing snapshots
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The network to sync snapshots for
   * @param pluginId The plugin id to use for syncing
   * @param settingsStr The stringified version of settings
   * @return TODO: document JSON response
   * @deprecated because testrigs were renamed to snapshots. Use {@link
   *     #syncSnapshotsUpdateSettings(String, String, String, String, String)
   *     syncSnapshotsUpdateSettings} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_SYNC_TESTRIGS_UPDATE_SETTINGS)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray syncTestrigsUpdateSettings(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
      @FormDataParam(CoordConsts.SVC_KEY_PLUGIN_ID) String pluginId,
      @FormDataParam(CoordConsts.SVC_KEY_SETTINGS) String settingsStr) {
    return syncSnapshotsUpdateSettings(apiKey, clientVersion, containerName, pluginId, settingsStr);
  }

  /**
   * Update settings for syncing snapshots
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The network to sync snapshots for
   * @param pluginId The plugin id to use for syncing
   * @param settingsStr The stringified version of settings
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_SYNC_SNAPSHOTS_UPDATE_SETTINGS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray syncSnapshotsUpdateSettings(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_PLUGIN_ID) String pluginId,
      @FormDataParam(CoordConsts.SVC_KEY_SETTINGS) String settingsStr) {
    try {
      _logger.infof(
          "WMS:syncSnapshotsUpdateSettings %s %s %s %s\n",
          apiKey, networkName, pluginId, settingsStr);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(pluginId, "Plugin Id");
      checkStringParam(settingsStr, "Settings");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Map<String, String> settings =
          BatfishObjectMapper.mapper()
              .readValue(settingsStr, new TypeReference<Map<String, String>>() {});

      boolean result =
          Main.getWorkMgr().syncTestrigsUpdateSettings(networkName, pluginId, settings);

      return successResponse(new JSONObject().put("result", result));
    } catch (FileExistsException
        | FileNotFoundException
        | IllegalArgumentException
        | AccessControlException e) {
      _logger.errorf("WMS:syncSnapshotsUpdateSettings exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:syncSnapshotsUpdateSettings exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  @GET
  @Path("test")
  @Produces(MediaType.TEXT_PLAIN)
  public String test() {
    try {
      _logger.info("WMS:test\n");
      JSONArray id = successResponse(Main.getWorkMgr().getStatusJson());

      return id.toString();

      // return Response.ok()
      // .entity(id)
      // // .header("Access-Control-Allow-Origin","*")
      // .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
      // .allow("OPTIONS")
      // .build();
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:test exception: %s", stackTrace);
      // return Response.serverError().build();
      return "got error";
    }
  }

  /**
   * Uploads a new environment under the container, testrig
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The name of the container under which the testrig resides
   * @param testrigName The name of the testrig under which to upload the new environment
   * @param baseEnvName The base environment name from which the new environment initially inherits
   * @param envName The name of the new environment to create
   * @param fileStream The stream from which the contents of the new environment are read. These
   *     contents overwrite those inherited from any base environment.
   * @return TODO: document JSON response
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
      _logger.infof(
          "WMS:uploadEnvironment %s %s %s/%s\n", apiKey, containerName, testrigName, envName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(containerName, "Container name");
      checkStringParam(testrigName, "Testrig name");
      checkStringParam(envName, "Environment name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, containerName);

      Main.getWorkMgr()
          .uploadEnvironment(containerName, testrigName, baseEnvName, envName, fileStream);

      return successResponse(new JSONObject().put("result", "successfully uploaded environment"));

    } catch (BatfishException e) {
      _logger.errorf("WMS:uploadEnvironment exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:uploadEnvironment exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Upload a new question in the specified network and asks it in the specified snapshot. A file
   * containing the question and a file containing the parameters must be provided.
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network into which to put the question
   * @param snapshotName The name of the snapshot about which to ask the question
   * @param qName The name of the question
   * @param questionJson The JSON form of the question
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_UPLOAD_QUESTION)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray uploadQuestion(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String qName,
      @FormDataParam(CoordConsts.SVC_KEY_FILE) String questionJson) {
    try {
      _logger.infof("WMS:uploadQuestion %s %s %s/%s\n", apiKey, networkName, snapshotName, qName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(qName, "Question name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      Main.getWorkMgr().uploadQuestion(networkName, qName, questionJson);

      return successResponse(new JSONObject().put("result", "successfully uploaded question"));

    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:uploadQuestion exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:uploadQuestion exception for apikey:%s in network:%s; exception:%s",
          apiKey, networkName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Uploads a new snapshot under the specified network
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network under which to upload the new snapshot
   * @param snapshotName The name of the new snapshot to create
   * @param fileStream The stream from which the new snapshot is read
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_UPLOAD_SNAPSHOT)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray uploadSnapshot(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream,
      @FormDataParam(CoordConsts.SVC_KEY_AUTO_ANALYZE) String autoAnalyzeStr) {
    try {
      _logger.infof("WMS:uploadSnapshot %s %s %s\n", apiKey, networkName, snapshotName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);
      checkNetworkAccessibility(apiKey, networkName);

      boolean autoAnalyze = false;
      if (!Strings.isNullOrEmpty(autoAnalyzeStr)) {
        autoAnalyze = Boolean.parseBoolean(autoAnalyzeStr);
      }

      if (GlobalTracer.get().activeSpan() != null) {
        GlobalTracer.get()
            .activeSpan()
            .setTag("network-name", networkName)
            .setTag("snapshot-name", snapshotName);
      }

      Main.getWorkMgr().uploadSnapshot(networkName, snapshotName, fileStream, autoAnalyze);
      _logger.infof(
          "Uploaded snapshot:%s for network:%s using api-key:%s\n",
          snapshotName, networkName, apiKey);
      return successResponse(new JSONObject().put("result", "successfully uploaded snapshot"));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:uploadSnapshot exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:uploadSnapshot exception for apikey:%s in network:%s, snapshot:%s; exception:%s",
          apiKey, networkName, snapshotName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Uploads a new testrig under the specified container
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param containerName The name of the container under which to upload the new testrig
   * @param testrigName The name of the new testrig to create
   * @param fileStream The stream from which the new testrig is read
   * @return TODO: document JSON response
   * @deprecated because testrigs were renamed to snapshots. Use {@link #uploadSnapshot(String,
   *     String, String, String, InputStream, String)} instead.
   */
  @POST
  @Path(CoordConsts.SVC_RSC_UPLOAD_TESTRIG)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray uploadTestrig(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_CONTAINER_NAME) String containerName,
      @FormDataParam(CoordConsts.SVC_KEY_TESTRIG_NAME) String testrigName,
      @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream,
      @FormDataParam(CoordConsts.SVC_KEY_AUTO_ANALYZE_TESTRIG) String autoAnalyzeStr) {
    return uploadSnapshot(
        apiKey, clientVersion, containerName, testrigName, fileStream, autoAnalyzeStr);
  }
}
