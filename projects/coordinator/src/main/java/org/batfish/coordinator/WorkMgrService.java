package org.batfish.coordinator;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.pojo.WorkStatus;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.Variable;
import org.batfish.identifiers.NetworkId;
import org.batfish.version.BatfishVersion;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/** */
@Path(CoordConsts.SVC_CFG_WORK_MGR)
public class WorkMgrService {

  public static final List<Class<?>> REQUIRED_FEATURES =
      ImmutableList.of(ExceptionMapper.class, JettisonFeature.class, MultiPartFeature.class);

  BatfishLogger _logger = Main.getLogger();

  private static JSONArray successResponse(Object entity) throws JSONException {
    return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, entity));
  }

  private static JSONArray failureResponse(Object entity) throws JSONException {
    return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_FAILURE, entity));
  }

  @POST
  @Path(CoordConsts.SVC_RSC_AUTO_COMPLETE)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray autoComplete(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_COMPLETION_TYPE) String completionType,
      /* Optional: not needed for some completions */
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_QUERY) String queryArg,
      /* Optional */
      @FormDataParam(CoordConsts.SVC_KEY_MAX_SUGGESTIONS) String maxSuggestions)
      throws JSONException {
    try {
      _logger.infof("WMS:autoComplete %s %s %s\n", completionType, queryArg, maxSuggestions);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(completionType, "Completion type");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      Variable.Type varType = Variable.Type.fromString(completionType);

      String query = firstNonNull(queryArg, "");
      List<AutocompleteSuggestion> answer =
          Main.getWorkMgr()
              .autoComplete(
                  networkName,
                  snapshotName,
                  varType,
                  firstNonNull(query, ""),
                  Strings.isNullOrEmpty(maxSuggestions)
                      ? Integer.MAX_VALUE
                      : Integer.parseInt(maxSuggestions));
      if (answer == null) {
        return failureResponse(
            "There was a problem getting Autocomplete suggestions - network or snapshot does not"
                + " exist!");
      }

      List<String> serializedSuggestions =
          answer.stream()
              .map(BatfishObjectMapper::writeStringRuntimeError)
              .collect(Collectors.toList());

      InputValidationNotes validationNotes =
          Main.getWorkMgr().validateInput(networkName, snapshotName, varType, query);

      String serializedMetadata = BatfishObjectMapper.writeString(validationNotes);

      return successResponse(
          new JSONObject()
              .put(CoordConsts.SVC_KEY_SUGGESTIONS, serializedSuggestions)
              .put(CoordConsts.SVC_KEY_QUERY_METADATA, serializedMetadata));
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
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion)
      throws JSONException {
    try {
      _logger.infof("WMS:checkApiKey %s\n", apiKey);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");

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

  private void checkNetworkAccessibility(String apiKey, String networkName) {
    if (!Main.getAuthorizer().isAccessibleNetwork(apiKey, networkName, true)) {
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
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_ASSERTION) String assertion)
      throws JSONException {
    try {
      _logger.infof(
          "WMS:configureQuestionTemplate: q: %s e: %s a: %s\n",
          questionTemplate, exceptions, assertion);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(questionTemplate, "Question template");

      checkApiKeyValidity(apiKey);

      Question inputQuestion = Question.parseQuestion(questionTemplate);
      Question outputQuestion = inputQuestion.configureTemplate(exceptions, assertion);
      String outputQuestionStr = BatfishObjectMapper.writeString(outputQuestion);

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
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName)
      throws JSONException {
    try {
      _logger.infof("WMS:delNetwork %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      boolean status = Main.getWorkMgr().delNetwork(networkName);

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
  @Deprecated
  public JSONArray delQuestion(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName)
      throws JSONException {
    try {
      _logger.infof("WMS:delQuestion %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(questionName, "Question name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      checkArgument(
          Main.getWorkMgr().delQuestion(networkName, questionName),
          "Could not find ad-hoc question %s under network %s",
          questionName,
          networkName);

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
   * @param networkName The name of the network in which the snapshot resides
   * @param snapshotName The name of the snapshot to delete
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_DEL_SNAPSHOT)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray delSnapshot(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName)
      throws JSONException {
    try {
      _logger.infof("WMS:delSnapshot %s\n", networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      if (!Main.getWorkMgr().delSnapshot(networkName, snapshotName)) {
        throw new IllegalArgumentException(
            String.format(
                "Could not delete non-existent snapshot:%s in network:%s",
                snapshotName, networkName));
      }

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
   * Get answer to a previously asked question
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the question was asked
   * @param snapshotName The name of the snapshot on which the question was asked
   * @param referenceSnapshot The name of the reference snapshot on which the question was asked
   * @param questionName The name of the question
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_ANSWER)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAnswer(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_REFERENCE_SNAPSHOT_NAME) String referenceSnapshot,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr /* optional */)
      throws JSONException {
    try {
      _logger.infof("WMS:getAnswer %s %s %s %s\n", apiKey, networkName, snapshotName, questionName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Current snapshot name");
      checkStringParam(questionName, "Question name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      if (!Strings.isNullOrEmpty(workItemStr)) {
        WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);
        if (!workItem.getNetwork().equals(networkName)
            || !workItem.getSnapshot().equals(snapshotName)) {
          return failureResponse(
              "Mismatch in parameters: WorkItem is not for the supplied network or snapshot");
        }
        QueuedWork work = Main.getWorkMgr().getMatchingWork(workItem, QueueType.INCOMPLETE);
        if (work != null) {
          String taskStr = BatfishObjectMapper.writeString(work.getLastTaskCheckResult());
          return successResponse(
              new JSONObject()
                  .put(CoordConsts.SVC_KEY_WORKID, work.getWorkItem().getId())
                  .put(CoordConsts.SVC_KEY_WORKSTATUS, work.getStatus().toString())
                  .put(CoordConsts.SVC_KEY_TASKSTATUS, taskStr));
        }
      }

      String answer =
          Main.getWorkMgr()
              .getAnswerString(networkName, snapshotName, questionName, referenceSnapshot);

      return successResponse(new JSONObject().put(CoordConsts.SVC_KEY_ANSWER, answer));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getAnswer exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getAnswer exception for apikey:%s in network:%s, snapshot:%s, referencesnapshot:%s; "
              + "exception:%s",
          apiKey, networkName, snapshotName, referenceSnapshot, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Get metrics for answers for a previously run ad-hoc question
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the answer resides
   * @param snapshotName The name of the snapshot on which the question was run
   * @param referenceSnapshotName The name of the reference snapshot on which the question was run
   * @param questionName The name of the question
   * @param answerRowsOptionsStr Options specifying how to process the rows of the answer
   * @param workItemStr The work item
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_ANSWER_ROWS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAnswerRows(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_REFERENCE_SNAPSHOT_NAME) String referenceSnapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName,
      @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_ANSWERS_OPTIONS) String answerRowsOptionsStr,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr /* optional */)
      throws JSONException {
    try {
      _logger.infof(
          "WMS:getAnswerRows %s %s %s %s %s %s\n",
          apiKey,
          networkName,
          snapshotName,
          referenceSnapshotName,
          questionName,
          answerRowsOptionsStr);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Current snapshot name");
      checkStringParam(questionName, "Question name");
      AnswerRowsOptions answersRowsOptions =
          BatfishObjectMapper.mapper()
              .readValue(answerRowsOptionsStr, new TypeReference<AnswerRowsOptions>() {});

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      JSONObject response = new JSONObject();

      if (!Strings.isNullOrEmpty(workItemStr)) {
        WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);
        if (!workItem.getNetwork().equals(networkName)
            || !workItem.getSnapshot().equals(snapshotName)) {
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

      String rawAnswer =
          Main.getWorkMgr()
              .getAnswerString(networkName, snapshotName, questionName, referenceSnapshotName);

      Answer answer = Main.getWorkMgr().processAnswerRows(rawAnswer, answersRowsOptions);

      String answerStr = BatfishObjectMapper.writeString(answer);

      return successResponse(response.put(CoordConsts.SVC_KEY_ANSWER, answerStr));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getAnswerRows exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getAnswerRows exception for apikey:%s in network:%s, snapshot:%s, "
              + "referencesnapshot:%s, question:%s; exception:%s",
          apiKey, networkName, snapshotName, referenceSnapshotName, questionName, stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Get metrics for answers for a previously run ad-hoc question
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the answer resides
   * @param snapshotName The name of the snapshot on which the question was run
   * @param referenceSnapshotName The name of the reference snapshot on which the question was run
   * @param questionName The name of the question
   * @param answerRowsOptionsStr Options specifying how to process the rows of the answer
   * @param workItemStr The work item
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_ANSWER_ROWS2)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAnswerRows2(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_REFERENCE_SNAPSHOT_NAME) String referenceSnapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String questionName,
      @FormDataParam(CoordConsts.SVC_KEY_ANALYSIS_ANSWERS_OPTIONS) String answerRowsOptionsStr,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr /* optional */)
      throws JSONException {
    try {
      _logger.infof(
          "WMS:getAnswerRows2 %s %s %s %s %s %s\n",
          apiKey,
          networkName,
          snapshotName,
          referenceSnapshotName,
          questionName,
          answerRowsOptionsStr);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Current snapshot name");
      checkStringParam(questionName, "Question name");
      AnswerRowsOptions answersRowsOptions =
          BatfishObjectMapper.mapper()
              .readValue(answerRowsOptionsStr, new TypeReference<AnswerRowsOptions>() {});

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      JSONObject response = new JSONObject();

      if (!Strings.isNullOrEmpty(workItemStr)) {
        WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);
        if (!workItem.getNetwork().equals(networkName)
            || !workItem.getSnapshot().equals(snapshotName)) {
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

      String rawAnswer =
          Main.getWorkMgr()
              .getAnswerString(networkName, snapshotName, questionName, referenceSnapshotName);

      Answer answer = Main.getWorkMgr().processAnswerRows2(rawAnswer, answersRowsOptions);

      String answerStr = BatfishObjectMapper.writePrettyString(answer);

      return successResponse(response.put(CoordConsts.SVC_KEY_ANSWER, answerStr));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getAnswerRows2 exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getAnswerRows2 exception for apikey:%s in network:%s, snapshot:%s, "
              + "referencesnapshot:%s, question:%s; exception:%s\n",
          apiKey, networkName, snapshotName, referenceSnapshotName, questionName, stackTrace);
      return failureResponse(Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Get metrics for answers for a previously asked ad-hoc question
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network in which the question resides
   * @param snapshotName The name of the snapshot on which the question was run
   * @param referenceSnapshotName The name of the reference snapshot on which the question was run
   * @param question The name of the question
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_ANSWER_METRICS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAnswerMetrics(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_REFERENCE_SNAPSHOT_NAME) String referenceSnapshotName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String question,
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr)
      throws JSONException {
    try {
      _logger.infof(
          "WMS:getAnswerMetrics %s %s %s %s %s\n",
          apiKey, networkName, snapshotName, referenceSnapshotName, question);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Base snapshot name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      JSONObject response = new JSONObject();

      if (!Strings.isNullOrEmpty(workItemStr)) {
        WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);
        if (!workItem.getNetwork().equals(networkName)
            || !workItem.getSnapshot().equals(snapshotName)) {
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

      AnswerMetadata answerMetadata =
          Main.getWorkMgr()
              .getAnswerMetadata(networkName, snapshotName, question, referenceSnapshotName);

      String answerStr = BatfishObjectMapper.writeString(answerMetadata);

      return successResponse(response.put(CoordConsts.SVC_KEY_ANSWER, answerStr));
    } catch (IllegalArgumentException | AccessControlException e) {
      _logger.errorf("WMS:getAnswerMetrics exception: %s\n", e.getMessage());
      return failureResponse(e.getMessage());
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS:getAnswerMetrics exception for apikey:%s in network:%s, snapshot:%s, "
              + "referenceSnapshot:%s, question:%s; exception:%s",
          apiKey, networkName, snapshotName, referenceSnapshotName, question, stackTrace);
      return failureResponse(e.getMessage());
    }
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

      if (!Main.getWorkMgr().getIdManager().hasNetworkId(networkName)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity("Network '" + networkName + "' not found")
            .type(MediaType.TEXT_PLAIN)
            .build();
      }

      checkNetworkAccessibility(apiKey, networkName);

      Container network = Main.getWorkMgr().getContainer(networkName);
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
  public JSONArray getInfo() throws JSONException {
    _logger.info("WMS:getInfo\n");
    try {
      JSONObject map = new JSONObject();
      map.put("Service name", "Batfish coordinator");
      map.put(CoordConsts.SVC_KEY_VERSION, BatfishVersion.getVersionStatic());
      map.put("APIs", "Enter ../application.wadl (relative to your URL) to see supported methods");

      return successResponse(map);
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getInfo exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  @POST
  @Path(CoordConsts.SVC_RSC_GET_QUESTION_TEMPLATES)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray getQuestionTemplates(@FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey)
      throws JSONException {
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
      _logger.errorf("WMS:getQuestionTemplates exception: %s\n", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  @GET
  @Path(CoordConsts.SVC_RSC_GETSTATUS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getStatus() throws JSONException {
    try {
      _logger.info("WMS:getStatus\n");
      JSONObject retObject = Main.getWorkMgr().getStatusJson();
      retObject.put("service-version", BatfishVersion.getVersionStatic());
      return successResponse(retObject);
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf("WMS:getStatus exception: %s", stackTrace);
      return failureResponse(e.getMessage());
    }
  }

  /**
   * Obtain the counts of completed and incomplete work items <br>
   * Deprecated in favor of {@link
   * org.batfish.coordinator.resources.WorkResource#getWorkStatus(String) }
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param workId The work ID to check
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_GET_WORKSTATUS)
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONArray getWorkStatus(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion,
      @FormDataParam(CoordConsts.SVC_KEY_WORKID) String workId)
      throws JSONException {
    try {
      _logger.debugf("WMS:getWorkStatus %s\n", workId);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(workId, "work id");

      checkApiKeyValidity(apiKey);

      QueuedWork work = Main.getWorkMgr().getWork(UUID.fromString(workId));

      if (work == null) {
        return failureResponse("work with the specified id does not exist or is not inaccessible");
      }

      NetworkId networkId = work.getDetails().getNetworkId();
      Optional<String> networkOpt =
          Main.getWorkMgr().getNetworkNames().stream()
              .filter(
                  n ->
                      Main.getWorkMgr()
                          .getIdManager()
                          .getNetworkId(n)
                          .equals(Optional.of(networkId)))
              .findFirst();
      checkArgument(networkOpt.isPresent(), "Invalid network ID: %s", networkId);

      checkNetworkAccessibility(apiKey, networkOpt.get());

      String taskStr = BatfishObjectMapper.writeString(work.getLastTaskCheckResult());

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
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_PREFIX) String networkPrefix)
      throws JSONException {
    try {
      _logger.infof("WMS:initNetwork %s\n", networkPrefix);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      if (networkName == null || networkName.equals("")) {
        checkStringParam(networkPrefix, "Network prefix");
      }

      checkApiKeyValidity(apiKey);

      String outputNetworkName = Main.getWorkMgr().initNetwork(networkName, networkPrefix);

      Main.getAuthorizer().authorizeContainer(apiKey, outputNetworkName);

      return successResponse(
          new JSONObject()
              .put(CoordConsts.SVC_KEY_NETWORK_NAME, outputNetworkName)
              .put(CoordConsts.SVC_KEY_CONTAINER_NAME, outputNetworkName));
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
   * Here for backwards compatibility. Throws exception because it never worked correctly.
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
    // TODO: remove this api call
    throw new UnsupportedOperationException();
  }

  /**
   * List the networks that the specified API key can access
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @return On success, a JSON object with key {@link CoordConsts#SVC_KEY_NETWORK_LIST
   *     "networklist"} and a list of the accessible networks as the value. TODO document failure
   *     response
   */
  @POST
  @Path(CoordConsts.SVC_RSC_LIST_NETWORKS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray listNetworks(
      @FormDataParam(CoordConsts.SVC_KEY_API_KEY) String apiKey,
      @FormDataParam(CoordConsts.SVC_KEY_VERSION) String clientVersion)
      throws JSONException {
    _logger.infof("WMS:listNetworks %s\n", apiKey);
    try {
      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");

      checkApiKeyValidity(apiKey);

      SortedSet<String> networks = Main.getWorkMgr().listNetworks(apiKey);
      return successResponse(
          new JSONObject()
              .put(CoordConsts.SVC_KEY_CONTAINER_LIST, networks)
              .put(CoordConsts.SVC_KEY_NETWORK_LIST, networks));
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
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_SNAPSHOT_NAME) String snapshotName, /* optional */
      @Nullable @FormDataParam(CoordConsts.SVC_KEY_WORK_TYPE) WorkType workType /* optional */)
      throws JSONException {
    try {
      _logger.infof("WMS:listIncompleteWork %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      if (snapshotName != null) {
        checkStringParam(snapshotName, "Snapshot name");
      }

      checkApiKeyValidity(apiKey);

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
              .put(CoordConsts.SVC_KEY_WORK_LIST, BatfishObjectMapper.writeString(workList)));
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
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_VERBOSE) boolean verbose)
      throws JSONException {
    try {
      _logger.infof("WMS:listQuestions %s %s\n", apiKey, networkName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      JSONObject retObject = new JSONObject();
      Set<String> questions = Main.getWorkMgr().listQuestions(networkName, verbose);
      checkArgument(questions != null, "Non-existent network: %s", networkName);
      for (String questionName : questions) {
        String questionText = Main.getWorkMgr().getQuestion(networkName, questionName);
        checkArgument(
            questionText != null,
            "Content for ad-hoc question %s under network %s unexpectedly not found",
            questionName,
            networkName);
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
      @FormDataParam(CoordConsts.SVC_KEY_WORKITEM) String workItemStr)
      throws JSONException {
    try {
      _logger.infof("WMS:queueWork %s %s\n", apiKey, workItemStr);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(workItemStr, "Workitem string");

      checkApiKeyValidity(apiKey);

      WorkItem workItem = BatfishObjectMapper.mapper().readValue(workItemStr, WorkItem.class);

      checkNetworkAccessibility(apiKey, workItem.getNetwork());

      QueuedWork work = Main.getWorkMgr().getMatchingWork(workItem, QueueType.INCOMPLETE);
      if (work != null) {
        return failureResponse(
            new JSONObject()
                .put("Duplicate workId", work.getId())
                .put("This workId", workItem.getId()));
      }
      boolean result = Main.getWorkMgr().queueWork(workItem);

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
   * Upload a new question in the specified network.
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network into which to put the question
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
      @FormDataParam(CoordConsts.SVC_KEY_NETWORK_NAME) String networkName,
      @FormDataParam(CoordConsts.SVC_KEY_QUESTION_NAME) String qName,
      @FormDataParam(CoordConsts.SVC_KEY_FILE) String questionJson)
      throws JSONException {
    try {
      _logger.infof("WMS:uploadQuestion %s %s/%s\n", apiKey, networkName, qName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(qName, "Question name");

      checkApiKeyValidity(apiKey);

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
   * @param fileStream The {@link InputStream} from which the new snapshot is read
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
      @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream)
      throws JSONException {
    try {
      _logger.infof("WMS:uploadSnapshot %s %s %s\n", apiKey, networkName, snapshotName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      checkApiKeyValidity(apiKey);

      checkNetworkAccessibility(apiKey, networkName);

      if (!Main.getWorkMgr().uploadSnapshot(networkName, snapshotName, fileStream)) {
        return failureResponse(
            String.format("Snapshot with name: '%s' already exists", snapshotName));
      }
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
}
