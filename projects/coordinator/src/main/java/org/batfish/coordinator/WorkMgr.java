package org.batfish.coordinator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.ColumnSortOption;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Pair;
import org.batfish.common.Task;
import org.batfish.common.Warnings;
import org.batfish.common.WorkItem;
import org.batfish.common.plugin.AbstractCoordinator;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UnzipUtility;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.common.util.ZipUtility;
import org.batfish.coordinator.AnalysisMetadataMgr.AnalysisType;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.config.Settings;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.AnalysisMetadata;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerMetadataUtil;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.AutocompleteSuggestion.CompletionType;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.Metrics;
import org.batfish.datamodel.answers.MinorIssueConfig;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.datamodel.questions.BgpPropertySpecifier;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.OspfPropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.ExcludedRows;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.storage.FileBasedStorageDirectoryProvider;
import org.batfish.storage.StorageProvider;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.uri.UriComponent;

public class WorkMgr extends AbstractCoordinator {

  static final class AssignWorkTask implements Runnable {
    @Override
    public void run() {
      Main.getWorkMgr().checkTasks();
      Main.getWorkMgr().assignWork();
    }
  }

  private static final Set<String> CONTAINER_FILENAMES = initContainerFilenames();

  private static final Set<String> ENV_FILENAMES = initEnvFilenames();

  private static final int MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES = 10;

  private static Set<String> initContainerFilenames() {
    return ImmutableSet.of(
        BfConsts.RELPATH_REFERENCE_LIBRARY_PATH, BfConsts.RELPATH_NODE_ROLES_PATH);
  }

  private static Set<String> initEnvFilenames() {
    Set<String> envFilenames = new HashSet<>();
    envFilenames.add(BfConsts.RELPATH_NODE_BLACKLIST_FILE);
    envFilenames.add(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
    envFilenames.add(BfConsts.RELPATH_EDGE_BLACKLIST_FILE);
    envFilenames.add(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES);
    envFilenames.add(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES);
    envFilenames.add(BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS);
    return envFilenames;
  }

  private final IdManager _idManager;

  private final BatfishLogger _logger;

  private final Settings _settings;

  private WorkQueueMgr _workQueueMgr;

  private final StorageProvider _storage;

  public WorkMgr(
      Settings settings,
      BatfishLogger logger,
      @Nonnull IdManager idManager,
      @Nonnull StorageProvider storage) {
    super(false);
    _settings = settings;
    _idManager = idManager;
    _storage = storage;
    _logger = logger;
    _workQueueMgr = new WorkQueueMgr(logger);
  }

  @VisibleForTesting
  public @Nonnull StorageProvider getStorage() {
    return _storage;
  }

  private void assignWork() {

    try {
      QueuedWork work = _workQueueMgr.getWorkForAssignment();

      // get out if no work was found
      if (work == null) {
        // _logger.info("WM:AssignWork: No unassigned work\n");
        return;
      }

      String idleWorker = Main.getPoolMgr().getWorkerForAssignment();

      // get out if no idle worker was found, but release the work first
      if (idleWorker == null) {
        _workQueueMgr.markAssignmentFailure(work);

        _logger.info("WM:AssignWork: No idle worker\n");
        return;
      }

      assignWork(work, idleWorker);
    } catch (Exception e) {
      _logger.errorf("Got exception in assignWork: %s\n", Throwables.getStackTraceAsString(e));
    }
  }

  private void assignWork(QueuedWork work, String worker) {

    _logger.infof("WM:AssignWork: Trying to assign %s to %s\n", work, worker);

    boolean assignmentError = false;
    boolean assigned = false;

    Client client = null;
    SpanContext queueWorkSpan = work.getWorkItem().getSourceSpan();
    try (ActiveSpan assignWorkSpan =
        GlobalTracer.get()
            .buildSpan("Assign Work")
            .addReference(References.FOLLOWS_FROM, queueWorkSpan)
            .startActive()) {
      assert assignWorkSpan != null; // avoid unused warning
      // get the task and add other standard stuff
      JSONObject task = new JSONObject(work.getWorkItem().getRequestParams());
      task.put(BfConsts.ARG_CONTAINER, work.getWorkItem().getContainerName());
      task.put(
          BfConsts.ARG_STORAGE_BASE,
          Main.getSettings().getContainersLocation().toAbsolutePath().toString());
      task.put(BfConsts.ARG_TESTRIG, work.getWorkItem().getTestrigName());

      client =
          CommonUtil.createHttpClientBuilder(
                  _settings.getSslPoolDisable(),
                  _settings.getSslPoolTrustAllCerts(),
                  _settings.getSslPoolKeystoreFile(),
                  _settings.getSslPoolKeystorePassword(),
                  _settings.getSslPoolTruststoreFile(),
                  _settings.getSslPoolTruststorePassword(),
                  true)
              .build();

      String protocol = _settings.getSslPoolDisable() ? "http" : "https";
      WebTarget webTarget =
          client
              .target(
                  String.format(
                      "%s://%s%s/%s",
                      protocol, worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_RUN_TASK_RSC))
              .queryParam(
                  BfConsts.SVC_TASKID_KEY,
                  UriComponent.encode(
                      work.getId().toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
              .queryParam(
                  BfConsts.SVC_TASK_KEY,
                  UriComponent.encode(
                      task.toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));

      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.errorf("WM:AssignWork: Got non-OK response %s\n", response.getStatus());
      } else {
        String sobj = response.readEntity(String.class);
        JSONArray array = new JSONArray(sobj);
        _logger.info(
            String.format(
                "WM:AssignWork: response: %s [%s] [%s]\n",
                array.toString(), array.get(0), array.get(1)));

        if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
          _logger.error(
              String.format("ERROR in assigning task: %s %s\n", array.get(0), array.get(1)));

          assignmentError = true;
        } else {
          assigned = true;
        }
      }
    } catch (ProcessingException e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.error(String.format("Unable to connect to worker at %s: %s\n", worker, stackTrace));
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.error(String.format("Exception assigning work: %s\n", stackTrace));
    } finally {
      if (client != null) {
        client.close();
      }
    }

    if (work.getStatus() == WorkStatusCode.TERMINATEDBYUSER) {
      if (assigned) {
        killWork(work, worker);
      }
      return;
    }

    // mark the assignment results for both work and worker
    if (assignmentError) {
      try {
        _workQueueMgr.markAssignmentError(work);
      } catch (Exception e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        _logger.errorf("Unable to markAssignmentError for work %s: %s\n", work, stackTrace);
      }
    } else if (assigned) {
      try {
        _workQueueMgr.markAssignmentSuccess(work, worker);
      } catch (Exception e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        _logger.errorf("Unable to markAssignmentSuccess for work %s: %s\n", work, stackTrace);
      }

    } else {
      _workQueueMgr.markAssignmentFailure(work);
    }

    Main.getPoolMgr().markAssignmentResult(worker, assigned);
  }

  private void checkTasks() {
    try {
      List<QueuedWork> workToCheck = _workQueueMgr.getWorkForChecking();
      for (QueuedWork work : workToCheck) {
        String assignedWorker = work.getAssignedWorker();
        if (assignedWorker == null) {
          _logger.errorf("WM:CheckWork no assigned worker for %s\n", work);
          _workQueueMgr.makeWorkUnassigned(work);
          continue;
        }
        checkTask(work, assignedWorker);
      }
    } catch (Exception e) {
      _logger.errorf("Got exception in checkTasks: %s\n", Throwables.getStackTraceAsString(e));
    }
  }

  public List<AutocompleteSuggestion> autoComplete(
      String container,
      String testrig,
      CompletionType completionType,
      String query,
      int maxSuggestions)
      throws IOException {
    switch (completionType) {
      case BGP_PROPERTY:
        {
          List<AutocompleteSuggestion> suggestions = BgpPropertySpecifier.autoComplete(query);
          return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
        }
      case INTERFACE_PROPERTY:
        {
          List<AutocompleteSuggestion> suggestions = InterfacePropertySpecifier.autoComplete(query);
          return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
        }
      case NAMED_STRUCTURE:
        {
          List<AutocompleteSuggestion> suggestions = NamedStructureSpecifier.autoComplete(query);
          return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
        }
      case NODE:
        {
          checkArgument(
              !isNullOrEmpty(testrig),
              "Snapshot name should be supplied for 'NODE' autoCompletion");
          List<AutocompleteSuggestion> suggestions =
              NodesSpecifier.autoComplete(
                  query, getNodes(container, testrig), getNodeRolesData(container));
          return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
        }
      case NODE_PROPERTY:
        {
          List<AutocompleteSuggestion> suggestions = NodePropertySpecifier.autoComplete(query);
          return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
        }
      case OSPF_PROPERTY:
        {
          List<AutocompleteSuggestion> suggestions = OspfPropertySpecifier.autoComplete(query);
          return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
        }
      default:
        throw new UnsupportedOperationException("Unsupported completion type: " + completionType);
    }
  }

  private void checkTask(QueuedWork work, String worker) {
    _logger.infof("WM:CheckWork: Trying to check %s on %s\n", work, worker);

    Task task = new Task(TaskStatus.UnreachableOrBadResponse);

    Client client = null;
    SpanContext queueWorkSpan = work.getWorkItem().getSourceSpan();
    try (ActiveSpan checkTaskSpan =
        GlobalTracer.get()
            .buildSpan("Checking Task Status")
            .addReference(References.FOLLOWS_FROM, queueWorkSpan)
            .startActive()) {
      assert checkTaskSpan != null; // avoid unused warning
      client =
          CommonUtil.createHttpClientBuilder(
                  _settings.getSslPoolDisable(),
                  _settings.getSslPoolTrustAllCerts(),
                  _settings.getSslPoolKeystoreFile(),
                  _settings.getSslPoolKeystorePassword(),
                  _settings.getSslPoolTruststoreFile(),
                  _settings.getSslPoolTruststorePassword(),
                  true)
              .build();

      String protocol = _settings.getSslPoolDisable() ? "http" : "https";
      WebTarget webTarget =
          client
              .target(
                  String.format(
                      "%s://%s%s/%s",
                      protocol, worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_GET_TASKSTATUS_RSC))
              .queryParam(
                  BfConsts.SVC_TASKID_KEY,
                  UriComponent.encode(
                      work.getId().toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.errorf("WM:CheckTask: Got non-OK response %s\n", response.getStatus());
      } else {
        String sobj = response.readEntity(String.class);
        JSONArray array = new JSONArray(sobj);
        _logger.info(String.format("response: %s [%s] [%s]\n", array, array.get(0), array.get(1)));

        if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
          _logger.error(
              String.format(
                  "got error while refreshing status: %s %s\n", array.get(0), array.get(1)));
        } else {
          String taskStr = array.get(1).toString();
          task = BatfishObjectMapper.mapper().readValue(taskStr, Task.class);
          if (task.getStatus() == null) {
            _logger.error("did not see status key in json response\n");
          }
        }
      }
    } catch (ProcessingException e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.error(String.format("unable to connect to %s: %s\n", worker, stackTrace));
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.error(String.format("exception: %s\n", stackTrace));
    } finally {
      if (client != null) {
        client.close();
      }
    }

    if (work.getStatus() == WorkStatusCode.TERMINATEDBYUSER) {
      return;
    }

    try {
      _workQueueMgr.processTaskCheckResult(work, task);
    } catch (Exception e) {
      _logger.errorf("exception: %s\n", Throwables.getStackTraceAsString(e));
    }

    // if the task ended, send a hint to the pool manager to look up worker status
    if (task.getStatus().isTerminated()) {
      Main.getPoolMgr().refreshWorkerStatus(worker);
    }
  }

  WorkDetails computeWorkDetails(WorkItem workItem) {

    WorkType workType = WorkType.UNKNOWN;

    if (WorkItemBuilder.isParsingWorkItem(workItem)) {
      workType = WorkType.PARSING;
    }

    if (WorkItemBuilder.isDataplaningWorkItem(workItem)) {
      if (workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate PARSING and DATAPLANING.");
      }
      workType = WorkType.DATAPLANING;
    }

    if (WorkItemBuilder.isAnsweringWorkItem(workItem)) {
      if (workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate ANSWER from other work.");
      }
      Question question =
          Question.parseQuestion(
              getQuestion(
                  workItem.getContainerName(),
                  WorkItemBuilder.getQuestionName(workItem),
                  WorkItemBuilder.getAnalysisName(workItem)));
      workType =
          question.getIndependent()
              ? WorkType.INDEPENDENT_ANSWERING
              : question.getDataPlane()
                  ? WorkType.DATAPLANE_DEPENDENT_ANSWERING
                  : WorkType.PARSING_DEPENDENT_ANSWERING;
    }

    if (WorkItemBuilder.isAnalyzingWorkItem(workItem)) {
      if (workType != WorkType.UNKNOWN) {
        throw new BatfishException("Cannot do composite work. Separate ANALYZE from other work.");
      }
      String aName = WorkItemBuilder.getAnalysisName(workItem);
      if (aName == null) {
        throw new BatfishException("Analysis name not provided for ANALYZE work");
      }
      Set<String> qNames = listAnalysisQuestions(workItem.getContainerName(), aName);
      // compute the strongest dependency among the embedded questions
      workType = WorkType.INDEPENDENT_ANSWERING;
      for (String qName : qNames) {
        Question question =
            Question.parseQuestion(getQuestion(workItem.getContainerName(), qName, aName));
        if (question.getDataPlane()) {
          workType = WorkType.DATAPLANE_DEPENDENT_ANSWERING;
          break;
        }
        if (!question.getIndependent()) {
          workType = WorkType.PARSING_DEPENDENT_ANSWERING;
        }
      }
    }

    Pair<Pair<String, String>, Pair<String, String>> settings =
        WorkItemBuilder.getBaseAndDeltaSettings(workItem);
    WorkDetails details =
        new WorkDetails(
            WorkItemBuilder.getBaseTestrig(settings),
            WorkItemBuilder.getBaseEnvironment(settings),
            WorkItemBuilder.getDeltaTestrig(settings),
            WorkItemBuilder.getDeltaEnvironment(settings),
            WorkItemBuilder.isDifferential(workItem),
            workType);

    return details;
  }

  /**
   * Create, update, or truncate an analysis with provided questions or and/or question names
   *
   * @param containerName The container in which the analysis resides
   * @param newAnalysis Whether or not to create a new analysis. Incompatible with {@code
   *     delQuestionsStr}.
   * @param aName The name of the analysis
   * @param questionsToAdd The questions to be added to or initially populate the analysis.
   * @param questionsToDelete A list of question names to be deleted from the analysis. Incompatible
   *     with {@code newAnalysis}.
   * @param suggested An optional Boolean indicating whether analysis is suggested (default: false).
   */
  public void configureAnalysis(
      String containerName,
      boolean newAnalysis,
      String aName,
      Map<String, String> questionsToAdd,
      List<String> questionsToDelete,
      @Nullable Boolean suggested) {
    NetworkId networkId = _idManager.getNetworkId(containerName);
    this.configureAnalysisValidityCheck(
        containerName, newAnalysis, aName, questionsToAdd, questionsToDelete);
    AnalysisId analysisId =
        newAnalysis ? _idManager.generateAnalysisId() : _idManager.getAnalysisId(aName, networkId);

    // Create metadata if it's a new analysis, or update it if suggested is not null
    if (newAnalysis || suggested != null) {
      AnalysisMetadata metadata;
      if (newAnalysis) {
        metadata = new AnalysisMetadata(Instant.now(), (suggested != null) && suggested);
      } else if (!_storage.hasAnalysisMetadata(
          networkId, _idManager.getAnalysisId(aName, networkId))) {
        // Configuring an old analysis with no metadata file; create one. Know suggested != null
        metadata = new AnalysisMetadata(Instant.MIN, suggested);
      } else {
        try {
          metadata = AnalysisMetadataMgr.readMetadata(networkId, analysisId);
          metadata.setSuggested(suggested);
        } catch (IOException e) {
          throw new BatfishException(
              "Unable to read metadata file for analysis '" + aName + "'", e);
        }
      }
      // Write metadata to file
      try {
        AnalysisMetadataMgr.writeMetadata(metadata, networkId, analysisId);
      } catch (IOException e) {
        throw new BatfishException("Could not write analysisMetadata", e);
      }
    }

    /* Delete questionsToDelete and add questionsToAdd */
    for (String qName : questionsToDelete) {
      _idManager.deleteQuestion(qName, networkId, analysisId);
    }
    for (Entry<String, String> entry : questionsToAdd.entrySet()) {
      String qName = entry.getKey();
      String qText = entry.getValue();
      QuestionId questionId = _idManager.generateQuestionId();
      _storage.storeQuestion(qText, networkId, questionId, analysisId);
      _idManager.assignQuestion(qName, networkId, questionId, analysisId);
    }
    if (newAnalysis) {
      _idManager.assignAnalysis(aName, networkId, analysisId);
    }
  }

  private void configureAnalysisValidityCheck(
      String containerName,
      boolean newAnalysis,
      String analysisName,
      Map<String, String> questionsToAdd,
      List<String> questionsToDelete) {
    NetworkId networkId = _idManager.getNetworkId(containerName);
    // Reasons to throw error for a new analysis:
    // 1. Analysis with same name already exists
    // 2. questionsToDelete is not empty
    if (newAnalysis) {
      if (_idManager.hasAnalysisId(analysisName, networkId)) {
        throw new BatfishException(
            String.format(
                "Analysis '%s' already exists for container '%s'", analysisName, containerName));
      } else if (!questionsToDelete.isEmpty()) {
        throw new BatfishException("Cannot delete questions from a new analysis");
      }
    } else {
      // Reasons to throw error for an existing analysis:
      // 1. Analysis directory does not exist
      // 2. questionsToDelete includes a question that doesn't exist in the analysis
      // 3. questionsToAdd includes a question that already exists and won't be deleted
      if (!_idManager.hasAnalysisId(analysisName, networkId)) {
        throw new BatfishException(
            String.format(
                "Analysis '%s' does not exist for container '%s'", analysisName, containerName));
      }
      AnalysisId analysisId = _idManager.getAnalysisId(analysisName, networkId);
      for (String qName : questionsToDelete) {
        if (!_idManager.hasQuestionId(qName, networkId, analysisId)) {
          throw new BatfishException(
              String.format("Question '%s' does not exist for analysis '%s'", qName, analysisName));
        }
      }
      for (Entry<String, String> entry : questionsToAdd.entrySet()) {
        String qName = entry.getKey();
        if (!questionsToDelete.contains(qName)
            && _idManager.hasQuestionId(qName, networkId, analysisId)) {
          throw new BatfishException(
              String.format(
                  "Question '%s' already exists for analysis '%s'", entry.getKey(), analysisName));
        }
      }
    }
  }

  public void delAnalysis(String network, String aName) {
    NetworkId networkId = _idManager.getNetworkId(network);
    _idManager.deleteAnalysis(aName, networkId);
  }

  public boolean delNetwork(String network) {
    if (!_idManager.hasNetworkId(network)) {
      return false;
    }
    _idManager.deleteNetwork(network);
    return true;
  }

  public void delEnvironment(String network, String snapshot, String envName) {
    Path envDir = getdirEnvironment(network, snapshot, envName);
    CommonUtil.deleteDirectory(envDir);
  }

  public void delSnapshot(String network, String snapshot) {
    NetworkId networkId = _idManager.getNetworkId(network);
    _idManager.deleteSnapshot(snapshot, networkId);
  }

  public void delQuestion(String network, String qName) {
    NetworkId networkId = _idManager.getNetworkId(network);
    _idManager.deleteQuestion(qName, networkId, null);
  }

  public String getAnswer(
      String network,
      String snapshot,
      String question,
      @Nullable String referenceSnapshot,
      @Nullable String analysis)
      throws JsonProcessingException, FileNotFoundException {
    String answer = "unknown";
    try {
      NetworkId networkId = _idManager.getNetworkId(network);
      AnalysisId analysisId =
          analysis != null ? _idManager.getAnalysisId(analysis, networkId) : null;
      QuestionId questionId = _idManager.getQuestionId(question, networkId, analysisId);
      SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, networkId);
      SnapshotId referenceSnapshotId =
          referenceSnapshot != null ? _idManager.getSnapshotId(referenceSnapshot, networkId) : null;
      QuestionSettingsId questionSettingsId =
          getOrCreateQuestionSettingsId(networkId, questionId, analysisId);
      AnswerId baseAnswerId =
          _idManager.getBaseAnswerId(
              networkId,
              snapshotId,
              questionId,
              questionSettingsId,
              referenceSnapshotId,
              analysisId);
      if (!_storage.hasAnswerMetadata(baseAnswerId)) {
        Answer ans = Answer.failureAnswer("Not answered", null);
        ans.setStatus(AnswerStatus.NOTFOUND);
        return BatfishObjectMapper.writePrettyString(ans);
      }
      AnswerMetadata baseAnswerMetadata = _storage.loadAnswerMetadata(baseAnswerId);
      AnswerId finalAnswerId =
          computeFinalAnswerAndId(
              baseAnswerMetadata,
              networkId,
              snapshotId,
              questionId,
              baseAnswerId,
              referenceSnapshotId,
              analysisId);
      return _storage.loadAnswer(finalAnswerId);
    } catch (IOException e) {
      String message =
          String.format(
              "Could not get answer: network=%s, snapshot=%s, question=%s, referenceSnapshot=%s, analysis=%s: %s",
              network,
              snapshot,
              question,
              referenceSnapshot,
              analysis,
              Throwables.getStackTraceAsString(e));
      Answer ans = Answer.failureAnswer(message, null);
      ans.setStatus(AnswerStatus.FAILURE);
      answer = BatfishObjectMapper.writePrettyString(ans);
      return answer;
    }
  }

  private @Nonnull AnswerId computeFinalAnswerAndId(
      @Nonnull AnswerMetadata baseAnswerMetadata,
      @Nonnull NetworkId networkId,
      @Nonnull SnapshotId snapshotId,
      @Nonnull QuestionId questionId,
      @Nonnull AnswerId baseAnswerId,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId)
      throws IOException {
    Set<IssueSettingsId> issueSettingsIds =
        getOrCreateIssueSettingsIds(networkId, baseAnswerMetadata);
    AnswerId finalAnswerId = _idManager.getFinalAnswerId(baseAnswerId, issueSettingsIds);
    if (!_storage.hasAnswerMetadata(finalAnswerId)) {
      Metrics metrics = baseAnswerMetadata.getMetrics();
      if (metrics == null) {
        _storage.storeAnswer(_storage.loadAnswer(baseAnswerId), finalAnswerId);
        _storage.storeAnswerMetadata(baseAnswerMetadata, finalAnswerId);
      } else {
        rebuildFinalAnswerAndMetadata(
            metrics.getMajorIssueConfigs(),
            networkId,
            snapshotId,
            questionId,
            baseAnswerId,
            finalAnswerId,
            issueSettingsIds,
            referenceSnapshotId,
            analysisId);
      }
    }
    return finalAnswerId;
  }

  private void rebuildFinalAnswerAndMetadata(
      @Nonnull Map<String, MajorIssueConfig> baseAnswerMajorIssueConfigs,
      @Nonnull NetworkId networkId,
      @Nonnull SnapshotId snapshotId,
      @Nonnull QuestionId questionId,
      @Nonnull AnswerId baseAnswerId,
      @Nonnull AnswerId finalAnswerId,
      @Nonnull Set<IssueSettingsId> issueSettingsIds,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId)
      throws IOException {
    Map<String, MajorIssueConfig> combinedMajorIssueConfigs =
        new HashMap<>(baseAnswerMajorIssueConfigs);
    for (IssueSettingsId issueSettingsId : issueSettingsIds) {
      MajorIssueConfig networkMajorIssueConfig =
          _storage.loadMajorIssueConfig(networkId, issueSettingsId);
      String majorIssueType = networkMajorIssueConfig.getMajorIssue();
      MajorIssueConfig combinedMajorIssueConfig =
          overlayMajorIssueConfig(
              baseAnswerMajorIssueConfigs.get(majorIssueType), networkMajorIssueConfig);
      combinedMajorIssueConfigs.put(majorIssueType, combinedMajorIssueConfig);
    }
    applyIssuesConfiguration(
        combinedMajorIssueConfigs,
        networkId,
        snapshotId,
        questionId,
        baseAnswerId,
        finalAnswerId,
        referenceSnapshotId,
        analysisId);
  }

  private MajorIssueConfig overlayMajorIssueConfig(
      MajorIssueConfig baseMajorIssueConfig, MajorIssueConfig networkMajorIssueConfig) {
    Map<String, MinorIssueConfig> networkMinorIssues =
        networkMajorIssueConfig.getMinorIssueConfigsMap();
    ImmutableList.Builder<MinorIssueConfig> combinedMinorIssues = ImmutableList.builder();
    // note there is no need to address minor issues not mentioned in base answer
    baseMajorIssueConfig
        .getMinorIssueConfigsMap()
        .forEach(
            (minorIssueType, baseMinorIssueConfig) -> {
              MinorIssueConfig networkMinorIssueConfig = networkMinorIssues.get(minorIssueType);
              if (networkMinorIssueConfig == null) {
                combinedMinorIssues.add(baseMinorIssueConfig);
                return;
              }
              Integer networkSeverity = networkMinorIssueConfig.getSeverity();
              Integer severity =
                  networkSeverity != null ? networkSeverity : baseMinorIssueConfig.getSeverity();
              String networkUrl = networkMinorIssueConfig.getUrl();
              String url =
                  networkUrl != null && !networkUrl.isEmpty()
                      ? networkUrl
                      : baseMinorIssueConfig.getUrl();
              combinedMinorIssues.add(new MinorIssueConfig(minorIssueType, severity, url));
            });
    return new MajorIssueConfig(baseMajorIssueConfig.getMajorIssue(), combinedMinorIssues.build());
  }

  private Set<IssueSettingsId> getOrCreateIssueSettingsIds(
      NetworkId networkId, AnswerMetadata baseAnswerMetadata) throws IOException {
    Metrics metrics = baseAnswerMetadata.getMetrics();
    if (metrics == null) {
      return ImmutableSet.of();
    }
    ImmutableSet.Builder<IssueSettingsId> ids = ImmutableSet.builder();
    for (String majorIssueType : metrics.getMajorIssueConfigs().keySet()) {
      ids.add(getOrCreateIssueSettingsId(networkId, majorIssueType));
    }
    return ids.build();
  }

  public @Nonnull Map<String, String> getAnalysisAnswers(
      String network,
      String snapshot,
      String baseEnv,
      String referenceSnapshot,
      String deltaEnv,
      String analysis,
      Set<String> analysisQuestions)
      throws JsonProcessingException, FileNotFoundException {
    Set<String> questions =
        analysisQuestions.isEmpty() ? listAnalysisQuestions(network, analysis) : analysisQuestions;
    ImmutableSortedMap.Builder<String, String> result = ImmutableSortedMap.naturalOrder();
    for (String questionName : questions) {
      result.put(
          questionName, getAnswer(network, snapshot, questionName, referenceSnapshot, analysis));
    }
    return result.build();
  }

  public @Nonnull Map<String, AnswerMetadata> getAnalysisAnswersMetadata(
      String network,
      String snapshot,
      String referenceSnapshot,
      String analysis,
      Set<String> analysisQuestions)
      throws JsonProcessingException, FileNotFoundException {
    Set<String> questions =
        analysisQuestions.isEmpty() ? listAnalysisQuestions(network, analysis) : analysisQuestions;
    ImmutableSortedMap.Builder<String, AnswerMetadata> result = ImmutableSortedMap.naturalOrder();
    for (String question : questions) {
      result.put(
          question, getAnswerMetadata(network, snapshot, question, referenceSnapshot, analysis));
    }
    return result.build();
  }

  public @Nonnull AnswerMetadata getAnswerMetadata(
      @Nonnull String network,
      @Nonnull String snapshot,
      @Nonnull String question,
      @Nullable String referenceSnapshot,
      @Nullable String analysis)
      throws JsonProcessingException, FileNotFoundException {
    try {
      NetworkId networkId = _idManager.getNetworkId(network);
      AnalysisId analysisId =
          analysis != null ? _idManager.getAnalysisId(analysis, networkId) : null;
      QuestionId questionId = _idManager.getQuestionId(question, networkId, analysisId);
      SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, networkId);
      SnapshotId referenceSnapshotId =
          referenceSnapshot != null ? _idManager.getSnapshotId(referenceSnapshot, networkId) : null;
      QuestionSettingsId questionSettingsId =
          getOrCreateQuestionSettingsId(networkId, questionId, analysisId);
      AnswerId baseAnswerId =
          _idManager.getBaseAnswerId(
              networkId,
              snapshotId,
              questionId,
              questionSettingsId,
              referenceSnapshotId,
              analysisId);
      if (!_storage.hasAnswerMetadata(baseAnswerId)) {
        return AnswerMetadata.forStatus(AnswerStatus.NOTFOUND);
      }
      AnswerMetadata baseAnswerMetadata = _storage.loadAnswerMetadata(baseAnswerId);
      AnswerId finalAnswerId =
          computeFinalAnswerAndId(
              baseAnswerMetadata,
              networkId,
              snapshotId,
              questionId,
              baseAnswerId,
              referenceSnapshotId,
              analysisId);
      return _storage.loadAnswerMetadata(finalAnswerId);
    } catch (IOException e) {
      _logger.errorf(
          "Could not get answer metadata: network=%s, snapshot=%s, question=%s, referenceSnapshot=%s, analysis=%s: %s",
          network,
          snapshot,
          question,
          referenceSnapshot,
          analysis,
          Throwables.getStackTraceAsString(e));
      return AnswerMetadata.forStatus(AnswerStatus.FAILURE);
    }
  }

  private QuestionSettingsId getOrCreateQuestionSettingsId(
      NetworkId networkId, QuestionId questionId, AnalysisId analysisId) throws IOException {
    String questionClassId = _storage.loadQuestionClassId(networkId, questionId, analysisId);
    if (!_idManager.hasQuestionSettingsId(questionClassId, networkId)) {
      QuestionSettingsId questionSettingsId = _idManager.generateQuestionSettingsId();
      _storage.storeQuestionSettings("{}", networkId, questionClassId);
      _idManager.assignQuestionSettingsId(questionClassId, networkId, questionSettingsId);
      return questionSettingsId;
    } else {
      return _idManager.getQuestionSettingsId(questionClassId, networkId);
    }
  }

  @VisibleForTesting
  boolean answerIssueConfigMatchesConfiguredIssues(
      MajorIssueConfig answerIssueConfig, Map<String, MajorIssueConfig> configuredMajorIssues) {
    MajorIssueConfig configuredMajorIssue =
        configuredMajorIssues.get(answerIssueConfig.getMajorIssue());
    Map<String, MinorIssueConfig> answerMinorIssues = answerIssueConfig.getMinorIssueConfigsMap();
    Map<String, MinorIssueConfig> configuredMinorIssues =
        configuredMajorIssue.getMinorIssueConfigsMap();
    return Sets.intersection(answerMinorIssues.keySet(), configuredMinorIssues.keySet())
        .stream()
        .allMatch(minor -> answerMinorIssues.get(minor).equals(configuredMinorIssues.get(minor)));
  }

  @VisibleForTesting
  void applyIssuesConfiguration(
      @Nonnull Map<String, MajorIssueConfig> majorIssueConfigs,
      @Nonnull NetworkId networkId,
      @Nonnull SnapshotId snapshotId,
      @Nonnull QuestionId questionId,
      @Nonnull AnswerId baseAnswerId,
      @Nonnull AnswerId finalAnswerId,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId)
      throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {
    Answer oldAnswer =
        BatfishObjectMapper.mapper()
            .readValue(_storage.loadAnswer(baseAnswerId), new TypeReference<Answer>() {});
    TableAnswerElement oldTable = (TableAnswerElement) oldAnswer.getAnswerElements().get(0);
    TableMetadata tableMetadata = oldTable.getMetadata();
    Set<String> issueColumns =
        tableMetadata
            .getColumnMetadata()
            .stream()
            .filter(cm -> cm.getSchema().equals(Schema.ISSUE))
            .map(ColumnMetadata::getName)
            .collect(ImmutableSet.toImmutableSet());
    // apply issue configuration to all rows and excluded rows, then collect them
    ImmutableList.Builder<Row> allRows = ImmutableList.builder();
    applyIssuesConfigurationToRows(oldTable.getRowsList(), issueColumns, majorIssueConfigs)
        .forEach(allRows::add);
    applyIssuesConfigurationToAllExcludedRows(
            oldTable.getExcludedRows(), issueColumns, majorIssueConfigs)
        .map(ExcludedRows::getRowsList)
        .flatMap(Collection::stream)
        .forEach(allRows::add);

    // grab the question for its exclusions
    String questionStr = _storage.loadQuestion(networkId, questionId, analysisId);
    Question questionObj = Question.parseQuestion(questionStr);

    // postprocess using question exclusions, collected rows
    TableAnswerElement newTable = new TableAnswerElement(tableMetadata);
    newTable.postProcessAnswer(questionObj, allRows.build());
    Answer newAnswer = new Answer();

    // Apply new info to answer
    newAnswer.setStatus(AnswerStatus.SUCCESS);
    newAnswer.setQuestion(questionObj);
    newAnswer.setSummary(newTable.getSummary());
    newAnswer.setAnswerElements(ImmutableList.of(newTable));

    // Compute and store new answer and answer metdata
    AnswerMetadata newAnswerMetadata = AnswerMetadataUtil.computeAnswerMetadata(newAnswer, _logger);
    String answerStr = BatfishObjectMapper.writePrettyString(newAnswer);
    _storage.storeAnswer(answerStr, finalAnswerId);
    _storage.storeAnswerMetadata(newAnswerMetadata, finalAnswerId);
  }

  private Stream<ExcludedRows> applyIssuesConfigurationToAllExcludedRows(
      List<ExcludedRows> allExcludedRows,
      Set<String> issueColumns,
      Map<String, MajorIssueConfig> issueConfigs) {
    return allExcludedRows
        .stream()
        .map(
            excludedRows ->
                applyIssuesConfigurationToExcludedRows(excludedRows, issueColumns, issueConfigs));
  }

  private ExcludedRows applyIssuesConfigurationToExcludedRows(
      ExcludedRows oldExcludedRows,
      Set<String> issueColumns,
      Map<String, MajorIssueConfig> issueConfigs) {
    ExcludedRows newExcludedRows = new ExcludedRows(oldExcludedRows.getExclusionName());
    applyIssuesConfigurationToRows(oldExcludedRows.getRowsList(), issueColumns, issueConfigs)
        .forEach(newExcludedRows::addRow);
    return newExcludedRows;
  }

  private Stream<Row> applyIssuesConfigurationToRows(
      List<Row> rowsList, Set<String> issueColumns, Map<String, MajorIssueConfig> issueConfigs) {
    return rowsList
        .stream()
        .map(row -> applyRowIssuesConfiguration(row, issueColumns, issueConfigs));
  }

  @VisibleForTesting
  @Nonnull
  Row applyRowIssuesConfiguration(
      Row oldRow, Set<String> issueColumns, Map<String, MajorIssueConfig> issueConfigs) {
    Row.RowBuilder builder = Row.builder();
    oldRow
        .getColumnNames()
        .forEach(
            column -> {
              if (!issueColumns.contains(column)) {
                builder.put(column, oldRow.get(column));
                return;
              }
              Issue oldIssue = oldRow.getIssue(column);
              MajorIssueConfig config = issueConfigs.get(oldIssue.getType().getMajor());
              Issue newIssue;
              if (config == null) {
                newIssue = oldIssue;
              } else {
                String minorIssue = oldIssue.getType().getMinor();
                Optional<MinorIssueConfig> optionalMinorConfig =
                    config.getMinorIssueConfig(minorIssue);
                if (optionalMinorConfig.isPresent()) {
                  MinorIssueConfig minorConfig = optionalMinorConfig.get();
                  newIssue =
                      new Issue(
                          oldIssue.getExplanation(),
                          minorConfig.getSeverity(),
                          oldIssue.getType(),
                          minorConfig.getUrl());
                } else {
                  newIssue = oldIssue;
                }
              }
              builder.put(column, newIssue);
            });
    return builder.build();
  }

  /** Return a {@link Container container} contains all testrigs directories inside it. */
  public Container getContainer(String containerName) {
    if (!_idManager.hasNetworkId(containerName)) {
      throw new IllegalArgumentException(
          String.format("Network '%s' does not exist", containerName));
    }
    NetworkId networkId = _idManager.getNetworkId(containerName);
    SortedSet<String> testrigs = ImmutableSortedSet.copyOf(_idManager.listSnapshots(networkId));
    return Container.of(containerName, testrigs);
  }

  @Override
  public Path getdirNetwork(String networkName) {
    return getdirNetwork(networkName, true);
  }

  @Override
  public BatfishLogger getLogger() {
    return _logger;
  }

  @Override
  public Set<String> getNetworkNames() {
    return _idManager.listNetworks();
  }

  private static Path getdirNetwork(String containerName, boolean errIfNotExist) {
    FileBasedStorageDirectoryProvider dirProvider =
        new FileBasedStorageDirectoryProvider(Main.getSettings().getContainersLocation());
    if (errIfNotExist && !Main.getWorkMgr().getIdManager().hasNetworkId(containerName)) {
      throw new BatfishException("Container '" + containerName + "' does not exist");
    }
    NetworkId networkId = Main.getWorkMgr().getIdManager().getNetworkId(containerName);
    return dirProvider.getNetworkDir(networkId).toAbsolutePath();
  }

  private Path getdirEnvironment(String containerName, String testrigName, String envName) {
    Path testrigDir = getdirSnapshot(containerName, testrigName);
    Path envDir =
        testrigDir.resolve(
            Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_ENVIRONMENTS_DIR, envName));
    if (!Files.exists(envDir)) {
      throw new BatfishException("Environment '" + envName + "' does not exist");
    }
    return envDir;
  }

  public Path getdirSnapshot(String network, String snapshot) {
    FileBasedStorageDirectoryProvider dirProvider =
        new FileBasedStorageDirectoryProvider(Main.getSettings().getContainersLocation());
    NetworkId networkId = _idManager.getNetworkId(network);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, networkId);
    Path snapshotDir = dirProvider.getSnapshotDir(networkId, snapshotId).toAbsolutePath();
    if (!Files.exists(snapshotDir)) {
      throw new BatfishException("Snapshot '" + snapshot + "' does not exist");
    }
    return snapshotDir;
  }

  @Override
  public Path getdirSnapshots(String networkName) {
    return getdirNetwork(networkName).resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR));
  }

  private IssueSettingsId getOrCreateIssueSettingsId(NetworkId networkId, String majorIssueType)
      throws IOException {
    if (!_idManager.hasIssueSettingsId(majorIssueType, networkId)) {
      IssueSettingsId issueSettingsId = _idManager.generateIssueSettingsId();
      _storage.storeMajorIssueConfig(
          networkId, issueSettingsId, new MajorIssueConfig(majorIssueType, ImmutableList.of()));
      _idManager.assignIssueSettingsId(majorIssueType, networkId, issueSettingsId);
      return issueSettingsId;
    } else {
      return _idManager.getIssueSettingsId(majorIssueType, networkId);
    }
  }

  /**
   * Returns the latest testrig in the container.
   *
   * @return An {@link Optional} object with the latest testrig or empty if no testrigs exist
   */
  public Optional<String> getLatestTestrig(String container) {
    NetworkId networkId = _idManager.getNetworkId(container);
    Function<String, Instant> toTestrigTimestamp =
        t ->
            TestrigMetadataMgr.getTestrigCreationTimeOrMin(
                networkId, _idManager.getSnapshotId(t, networkId));
    return listSnapshots(container)
        .stream()
        .max(
            Comparator.comparing(
                toTestrigTimestamp, Comparator.nullsFirst(Comparator.naturalOrder())));
  }

  /**
   * Gets the set of nodes in this container and testrig. Extracts the set based on the topology
   * file that is generated as part of the testrig initialization.
   *
   * @param container The container
   * @param testrig The testrig
   * @return The set of nodes
   * @throws IOException If the contents of the topology file cannot be mapped to the topology
   *     object
   */
  public Set<String> getNodes(String container, String testrig) throws IOException {
    Path pojoTopologyPath =
        getdirSnapshot(container, testrig)
            .resolve(
                Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_TESTRIG_POJO_TOPOLOGY_PATH));
    Topology topology =
        BatfishObjectMapper.mapper().readValue(pojoTopologyPath.toFile(), Topology.class);
    return topology.getNodes().stream().map(Node::getName).collect(Collectors.toSet());
  }

  public JSONObject getParsingResults(String containerName, String testrigName)
      throws JsonProcessingException, JSONException {

    ParseVendorConfigurationAnswerElement pvcae =
        deserializeObject(
            getdirSnapshot(containerName, testrigName)
                .resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_PARSE_ANSWER_PATH)),
            ParseVendorConfigurationAnswerElement.class);
    JSONObject warnings = new JSONObject();
    SortedMap<String, Warnings> warningsMap = pvcae.getWarnings();
    ObjectWriter writer = BatfishObjectMapper.prettyWriter();
    for (String s : warningsMap.keySet()) {
      warnings.put(s, writer.writeValueAsString(warningsMap.get(s)));
    }
    return warnings;
  }

  /**
   * Gets the {@link ReferenceLibrary} for the {@code container}.
   *
   * @throws IOException The contents of reference library file cannot be converted to {@link
   *     ReferenceLibrary}
   */
  public ReferenceLibrary getReferenceLibrary(String container) throws IOException {
    return ReferenceLibrary.read(getReferenceLibraryPath(container));
  }

  /** Gets the path of the reference library file */
  public Path getReferenceLibraryPath(String container) {
    return getdirNetwork(container).resolve(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH);
  }

  public JSONObject getStatusJson() throws JSONException {
    return _workQueueMgr.getStatusJson();
  }

  public String getTestrigInfo(String containerName, String testrigName) {
    Path testrigDir = getdirSnapshot(containerName, testrigName);
    Path submittedTestrigDir =
        testrigDir.resolve(Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_TEST_RIG_DIR));
    if (!Files.exists(submittedTestrigDir)) {
      return "Missing folder '"
          + BfConsts.RELPATH_TEST_RIG_DIR
          + "' for snapshot '"
          + testrigName
          + "'\n";
    }
    StringBuilder retStringBuilder = new StringBuilder();
    SortedSet<Path> entries = CommonUtil.getEntries(submittedTestrigDir);
    for (Path entry : entries) {
      retStringBuilder.append(entry.getFileName());
      if (Files.isDirectory(entry)) {
        String[] subdirEntryNames =
            CommonUtil.getEntries(entry)
                .stream()
                .map(subdirEntry -> subdirEntry.getFileName().toString())
                .collect(Collectors.toList())
                .toArray(new String[] {});
        retStringBuilder.append("/\n");
        // now append a maximum of MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES
        for (int index = 0;
            index < subdirEntryNames.length && index < MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES;
            index++) {
          retStringBuilder.append("  " + subdirEntryNames[index] + "\n");
        }
        if (subdirEntryNames.length > 10) {
          retStringBuilder.append(
              "  ...... "
                  + (subdirEntryNames.length - MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES)
                  + " more entries\n");
        }
      } else {
        retStringBuilder.append("\n");
      }
    }
    return retStringBuilder.toString();
  }

  @Nullable
  public Path getTestrigObject(String containerName, String testrigName, String objectName) {
    Path testrigDir = getdirSnapshot(containerName, testrigName);
    Path file = testrigDir.resolve(Paths.get(BfConsts.RELPATH_OUTPUT, objectName));
    /*
     * Check if we got an object name outside of the testrig folder, perhaps because of ".." in the
     * name; disallow it
     */
    if (!CommonUtil.getCanonicalPath(file).startsWith(CommonUtil.getCanonicalPath(testrigDir))) {
      throw new BatfishException("Illegal object name: '" + objectName + "'");
    }

    // Check in output then input directories for backward compatibility
    // Since inputs and outputs used to be stored together, in the testrig dir
    if (!file.toFile().exists()) {
      file = testrigDir.resolve(Paths.get(BfConsts.RELPATH_INPUT, objectName));
    }

    if (Files.isRegularFile(file)) {
      return file;
    } else if (Files.isDirectory(file)) {
      Path zipfile = Paths.get(file + ".zip");
      if (Files.exists(zipfile)) {
        CommonUtil.deleteIfExists(zipfile);
      }
      ZipUtility.zipFiles(file, zipfile);

      // TODO: delete the zipfile

      return zipfile;
    }
    return null;
  }

  public String getQuestion(String network, String questionName, @Nullable String analysisName) {
    NetworkId networkId = _idManager.getNetworkId(network);
    AnalysisId analysisId =
        analysisName != null ? _idManager.getAnalysisId(analysisName, networkId) : null;
    QuestionId questionId = _idManager.getQuestionId(questionName, networkId, analysisId);
    return _storage.loadQuestion(networkId, questionId, analysisId);
  }

  public QueuedWork getMatchingWork(WorkItem workItem, QueueType qType) {
    return _workQueueMgr.getMatchingWork(resolveIds(workItem), qType);
  }

  public QueuedWork getWork(UUID workItemId) {
    return _workQueueMgr.getWork(workItemId);
  }

  public String initNetwork(@Nullable String network, @Nullable String networkPrefix) {
    String newNetworkName =
        isNullOrEmpty(network) ? networkPrefix + "_" + UUID.randomUUID() : network;
    if (_idManager.hasNetworkId(newNetworkName)) {
      throw new BatfishException(String.format("Network '%s' already exists!", newNetworkName));
    }
    NetworkId networkId = _idManager.generateNetworkId();
    _storage.initNetwork(networkId);
    _idManager.assignNetwork(newNetworkName, networkId);
    return newNetworkName;
  }

  @Override
  public void initSnapshot(
      String networkName, String snapshotName, Path srcDir, boolean autoAnalyze) {
    /*
     * Sanity check what we got:
     *    There should be just one top-level folder.
     */
    SortedSet<Path> srcDirEntries = CommonUtil.getEntries(srcDir);
    if (srcDirEntries.size() != 1 || !Files.isDirectory(srcDirEntries.iterator().next())) {
      throw new BatfishException(
          "Unexpected packaging of snapshot. There should be just one top-level folder");
    }

    NetworkId networkId = _idManager.getNetworkId(networkName);
    SnapshotId snapshotId = _idManager.generateSnapshotId();

    Path srcSubdir = srcDirEntries.iterator().next();
    SortedSet<Path> subFileList = CommonUtil.getEntries(srcSubdir);

    Path containerDir = getdirNetwork(networkName);
    Path testrigDir =
        containerDir.resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, snapshotId.getId()));

    if (!testrigDir.resolve(BfConsts.RELPATH_OUTPUT).toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + testrigDir + "'");
    }

    // Now that the directory exists, we must also create the metadata.
    try {
      TestrigMetadataMgr.writeMetadata(
          new TestrigMetadata(Instant.now(), BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME),
          networkId,
          snapshotId);
    } catch (Exception e) {
      BatfishException metadataError = new BatfishException("Could not write testrigMetadata", e);
      try {
        CommonUtil.deleteDirectory(testrigDir);
      } catch (Exception inner) {
        metadataError.addSuppressed(inner);
      }
      throw metadataError;
    }

    Path srcTestrigDir =
        testrigDir.resolve(Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_TEST_RIG_DIR));

    // create empty default environment
    Path defaultEnvironmentLeafDir =
        testrigDir.resolve(
            Paths.get(
                BfConsts.RELPATH_OUTPUT,
                BfConsts.RELPATH_ENVIRONMENTS_DIR,
                BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
                BfConsts.RELPATH_ENV_DIR));
    defaultEnvironmentLeafDir.toFile().mkdirs();

    // things look ok, now make the move
    boolean routingTables = false;
    boolean bgpTables = false;
    boolean roleData = false;
    boolean referenceLibraryData = false;
    for (Path subFile : subFileList) {
      String name = subFile.getFileName().toString();
      if (isEnvFile(subFile)) {
        // copy environment level files to the environment directory
        if (name.equals(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES)) {
          routingTables = true;
        }
        if (name.equals(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES)) {
          bgpTables = true;
        }
        CommonUtil.copy(subFile, defaultEnvironmentLeafDir.resolve(subFile.getFileName()));
      } else if (isContainerFile(subFile)) {
        // derive and write the new container level file from the input
        if (name.equals(BfConsts.RELPATH_NODE_ROLES_PATH)) {
          roleData = true;
          try {
            NodeRolesData testrigData =
                BatfishObjectMapper.mapper()
                    .readValue(CommonUtil.readFile(subFile), NodeRolesData.class);
            NodeRolesData.mergeNodeRoleDimensions(
                () -> getNodeRolesDataWrapped(networkName),
                nodeRolesData -> writeNodeRolesWrapped(nodeRolesData, networkName),
                testrigData.getNodeRoleDimensions(),
                testrigData.getDefaultDimension(),
                false);
          } catch (IOException e) {
            // lets not stop the upload because that file is busted.
            // TODO: figure out a way to surface this error to the user
            _logger.errorf("Could not process node role data: %s", e);
          }
        }
        if (name.equals(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH)) {
          referenceLibraryData = true;
          try {
            ReferenceLibrary testrigData = ReferenceLibrary.read(subFile);
            Path path = containerDir.resolve(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH);
            ReferenceLibrary.mergeReferenceBooks(path, testrigData.getReferenceBooks());
          } catch (IOException e) {
            // lets not stop the upload because that file is busted.
            // TODO: figure out a way to surface this error to the user
            _logger.errorf("Could not process reference library data: %s", e);
          }
        }
      } else {
        // rest is plain copy
        CommonUtil.copy(subFile, srcTestrigDir.resolve(subFile.getFileName()));
      }
    }
    _logger.infof(
        "Environment data for snapshot:%s; bgpTables:%s, routingTables:%s, nodeRoles:%s referenceBooks:%s\n",
        snapshotName, bgpTables, routingTables, roleData, referenceLibraryData);
    _idManager.assignSnapshot(snapshotName, networkId, snapshotId);
    if (autoAnalyze) {
      for (WorkItem workItem : getAutoWorkQueue(networkName, snapshotName)) {
        boolean queued = queueWork(workItem);
        if (!queued) {
          _logger.errorf("Unable to queue work while auto processing: %s", workItem);
        }
      }
    }
  }

  private void writeNodeRolesWrapped(NodeRolesData nodeRolesData, String networkName) {
    try {
      writeNodeRoles(nodeRolesData, networkName);
    } catch (IOException e) {
      throw new BatfishException("error writing node roles", e);
    }
  }

  List<WorkItem> getAutoWorkQueue(String containerName, String testrigName) {
    List<WorkItem> autoWorkQueue = new LinkedList<>();

    WorkItem parseWork = WorkItemBuilder.getWorkItemParse(containerName, testrigName);
    autoWorkQueue.add(parseWork);

    Set<String> analysisNames = listAnalyses(containerName, AnalysisType.ALL);
    for (String analysis : analysisNames) {
      WorkItem analyzeWork =
          WorkItemBuilder.getWorkItemRunAnalysis(
              analysis,
              containerName,
              testrigName,
              BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
              null,
              null,
              false,
              false);
      autoWorkQueue.add(analyzeWork);
    }
    return autoWorkQueue;
  }

  private boolean isContainerFile(Path path) {
    String name = path.getFileName().toString();
    return CONTAINER_FILENAMES.contains(name);
  }

  private boolean isEnvFile(Path path) {
    String name = path.getFileName().toString();
    return ENV_FILENAMES.contains(name);
  }

  public boolean killWork(QueuedWork work) {
    String worker = work.getAssignedWorker();

    if (worker != null) {
      return killWork(work, worker);
    }

    // (worker = null) => this work was not assigned in the first place
    boolean killed = false;
    Task fakeTask = new Task(TaskStatus.TerminatedByUser, "Killed unassigned work");
    try {
      _workQueueMgr.processTaskCheckResult(work, fakeTask);
      killed = true;
    } catch (Exception e) {
      _logger.errorf("exception: %s\n", Throwables.getStackTraceAsString(e));
    }
    return killed;
  }

  private boolean killWork(QueuedWork work, String worker) {
    Client client = null;
    boolean killed = false;

    SpanContext queueWorkSpan = work.getWorkItem().getSourceSpan();
    try (ActiveSpan killTaskSpan =
        GlobalTracer.get()
            .buildSpan("Checking Task Status")
            .addReference(References.FOLLOWS_FROM, queueWorkSpan)
            .startActive()) {
      assert killTaskSpan != null; // avoid unused warning
      client =
          CommonUtil.createHttpClientBuilder(
                  _settings.getSslPoolDisable(),
                  _settings.getSslPoolTrustAllCerts(),
                  _settings.getSslPoolKeystoreFile(),
                  _settings.getSslPoolKeystorePassword(),
                  _settings.getSslPoolTruststoreFile(),
                  _settings.getSslPoolTruststorePassword(),
                  true)
              .build();

      String protocol = _settings.getSslPoolDisable() ? "http" : "https";
      WebTarget webTarget =
          client
              .target(
                  String.format(
                      "%s://%s%s/%s",
                      protocol, worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_KILL_TASK_RSC))
              .queryParam(
                  BfConsts.SVC_TASKID_KEY,
                  UriComponent.encode(
                      work.getId().toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.errorf("WM:KillTask: Got non-OK response %s\n", response.getStatus());
      } else {
        try {
          String sobj = response.readEntity(String.class);
          JSONArray array = new JSONArray(sobj);
          _logger.infof("response: %s [%s] [%s]\n", array, array.get(0), array.get(1));
          if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
            _logger.errorf("Got error while killing task: %s %s\n", array.get(0), array.get(1));
          } else {
            Task task = BatfishObjectMapper.mapper().readValue(array.getString(1), Task.class);
            _workQueueMgr.processTaskCheckResult(work, task);
            killed = true;
          }
        } catch (IllegalStateException e) {
          // can happen if the worker dies before we could finish reading; let's assume success
          _logger.infof("worker appears dead before response completion\n");
          Task fakeTask =
              new Task(TaskStatus.TerminatedByUser, "worker appears dead before responding");
          _workQueueMgr.processTaskCheckResult(work, fakeTask);
          killed = true;
        }
      }
    } catch (ProcessingException e) {
      _logger.errorf("unable to connect to %s: %s\n", worker, Throwables.getStackTraceAsString(e));
    } catch (Exception e) {
      _logger.errorf("exception: %s\n", Throwables.getStackTraceAsString(e));
    } finally {
      if (client != null) {
        client.close();
      }
    }
    return killed;
  }

  /**
   * Returns the Analysis names which exist in the container and match the {@link AnalysisType}
   *
   * @param network Container name
   * @param analysisType {@link AnalysisType} requested
   * @return {@link Set} of container names
   */
  public SortedSet<String> listAnalyses(String network, AnalysisType analysisType) {
    NetworkId networkId = _idManager.getNetworkId(network);
    SortedSet<String> analyses =
        _idManager
            .listAnalyses(networkId)
            .stream()
            .filter(
                aName ->
                    selectAnalysis(
                        _idManager.getAnalysisId(aName, networkId), analysisType, networkId))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    return analyses;
  }

  private boolean selectAnalysis(
      AnalysisId analysisId, AnalysisType analysisType, NetworkId networkId) {
    if (analysisType == AnalysisType.ALL) {
      return true;
    }
    boolean suggested = AnalysisMetadataMgr.getAnalysisSuggestedOrFalse(networkId, analysisId);
    return (analysisType == AnalysisType.SUGGESTED && suggested
        || analysisType == AnalysisType.USER && !suggested);
  }

  public SortedSet<String> listAnalysisQuestions(String network, String analysisName) {
    NetworkId networkId = _idManager.getNetworkId(network);
    AnalysisId analysisId = _idManager.getAnalysisId(analysisName, networkId);
    return ImmutableSortedSet.copyOf(_idManager.listQuestions(networkId, analysisId));
  }

  public SortedSet<String> listContainers(String apiKey) {
    Path containersDir = Main.getSettings().getContainersLocation();
    if (!Files.exists(containersDir)) {
      containersDir.toFile().mkdirs();
    }
    SortedSet<String> authorizedContainers =
        _idManager
            .listNetworks()
            .stream()
            .filter(
                container -> Main.getAuthorizer().isAccessibleContainer(apiKey, container, false))
            .collect(toCollection(TreeSet::new));
    return authorizedContainers;
  }

  public List<Container> getContainers(@Nullable String apiKey) {
    return listContainers(apiKey).stream().map(this::getContainer).collect(Collectors.toList());
  }

  public SortedSet<String> listEnvironments(String containerName, String testrigName) {
    Path testrigDir = getdirSnapshot(containerName, testrigName);
    Path environmentsDir =
        testrigDir.resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_ENVIRONMENTS_DIR));
    if (!Files.exists(environmentsDir)) {
      return new TreeSet<>();
    }
    SortedSet<String> environments =
        CommonUtil.getSubdirectories(environmentsDir)
            .stream()
            .map(dir -> dir.getFileName().toString())
            .collect(toCollection(TreeSet::new));
    return environments;
  }

  public List<QueuedWork> listIncompleteWork(
      String containerName, @Nullable String testrigName, @Nullable WorkType workType) {
    return _workQueueMgr.listIncompleteWork(containerName, testrigName, workType);
  }

  public SortedSet<String> listQuestions(String network, boolean verbose) {
    NetworkId networkId = _idManager.getNetworkId(network);
    Set<String> questions = _idManager.listQuestions(networkId, null);
    if (!verbose) {
      questions =
          questions
              .stream()
              .filter(name -> !name.startsWith("__"))
              .collect(ImmutableSet.toImmutableSet());
    }
    return ImmutableSortedSet.copyOf(questions);
  }

  public List<String> listSnapshots(String network) {
    NetworkId networkId = _idManager.getNetworkId(network);
    List<String> testrigs =
        _idManager
            .listSnapshots(networkId)
            .stream()
            .sorted(
                (t1, t2) -> { // reverse sorting by creation-time, name
                  SnapshotId snapshotId1 = _idManager.getSnapshotId(t1, networkId);
                  SnapshotId snapshotId2 = _idManager.getSnapshotId(t2, networkId);
                  String key1 =
                      TestrigMetadataMgr.getTestrigCreationTimeOrMin(networkId, snapshotId1) + t1;
                  String key2 =
                      TestrigMetadataMgr.getTestrigCreationTimeOrMin(networkId, snapshotId2) + t2;
                  return key2.compareTo(key1);
                })
            .collect(Collectors.toList());
    return testrigs;
  }

  /** Writes the {@code MajorIssueConfig} for the given network and major issue type. */
  public void putMajorIssueConfig(String network, String majorIssueType, MajorIssueConfig config)
      throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network);
    IssueSettingsId issueSettingsId = _idManager.generateIssueSettingsId();
    _storage.storeMajorIssueConfig(networkId, issueSettingsId, config);
    _idManager.assignIssueSettingsId(majorIssueType, networkId, issueSettingsId);
  }

  public void putObject(
      String containerName, String testrigName, String objectName, InputStream fileStream) {
    Path testrigDir = getdirSnapshot(containerName, testrigName);
    Path file = testrigDir.resolve(objectName);
    // check if we got an object name outside of the testrig folder,
    // perhaps because of ".." in the name; disallow it
    if (!CommonUtil.getCanonicalPath(file).startsWith(CommonUtil.getCanonicalPath(testrigDir))) {
      throw new BatfishException("Illegal object name: '" + objectName + "'");
    }
    Path parentFolder = file.getParent();
    if (!Files.exists(parentFolder)) {
      if (!parentFolder.toFile().mkdirs()) {
        throw new BatfishException("Failed to create directory: '" + parentFolder + "'");
      }
    } else {
      if (!Files.isDirectory(parentFolder)) {
        throw new BatfishException(parentFolder + " already exists but is not a folder");
      }
    }
    CommonUtil.writeStreamToFile(fileStream, file);
  }

  public boolean queueWork(WorkItem workItem) {
    NetworkId networkId = _idManager.getNetworkId(requireNonNull(workItem.getContainerName()));
    boolean success;
    try {
      workItem.setSourceSpan(GlobalTracer.get().activeSpan());
      WorkDetails workDetails = computeWorkDetails(workItem);
      if (TestrigMetadataMgr.getEnvironmentMetadata(
              networkId,
              _idManager.getSnapshotId(workDetails.baseTestrig, networkId),
              workDetails.baseEnv)
          == null) {
        throw new BatfishException(
            String.format(
                "Snapshot/environment metadata not found for %s/%s",
                workDetails.baseTestrig, workDetails.baseEnv));
      }
      if (workDetails.isDifferential
          && TestrigMetadataMgr.getEnvironmentMetadata(
                  networkId,
                  _idManager.getSnapshotId(workDetails.deltaTestrig, networkId),
                  workDetails.deltaEnv)
              == null) {
        throw new BatfishException(
            String.format(
                "Snapshot/environment metadata not found for %s/%s",
                workDetails.deltaTestrig, workDetails.deltaEnv));
      }
      success = _workQueueMgr.queueUnassignedWork(resolvedQueuedWork(workItem, workDetails));
    } catch (Exception e) {
      throw new BatfishException(String.format("Failed to queue work: %s", e.getMessage()), e);
    }
    // as an optimization trigger AssignWork to see if we can schedule this (or another) work
    if (success) {
      Thread thread = new Thread(this::assignWork);
      thread.start();
    }
    return success;
  }

  private static @Nonnull QueuedWork resolvedQueuedWork(
      WorkItem workItem, WorkDetails workDetails) {
    WorkItem resolvedWorkItem = resolveIds(workItem);
    return new QueuedWork(resolvedWorkItem, resolveIds(resolvedWorkItem, workDetails));
  }

  private static @Nonnull WorkDetails resolveIds(
      WorkItem resolvedWorkItem, WorkDetails workDetails) {
    return new WorkDetails(
        resolvedWorkItem.getTestrigName(),
        workDetails.baseEnv,
        resolvedWorkItem.getRequestParams().get(BfConsts.ARG_DELTA_TESTRIG),
        workDetails.deltaEnv,
        workDetails.isDifferential,
        workDetails.workType);
  }

  static @Nonnull WorkItem resolveIds(WorkItem workItem) {
    IdManager idManager = Main.getWorkMgr().getIdManager();
    Map<String, String> params = new HashMap<>(workItem.getRequestParams());

    // network
    String network = workItem.getContainerName();
    if (network == null) {
      return workItem;
    }
    NetworkId networkId = idManager.getNetworkId(network);
    params.put(BfConsts.ARG_CONTAINER, networkId.getId());

    // snapshot
    String snapshot = workItem.getTestrigName();
    if (snapshot == null) {
      return workItem;
    }
    SnapshotId snapshotId = idManager.getSnapshotId(snapshot, networkId);
    params.put(BfConsts.ARG_TESTRIG, snapshotId.getId());

    // referenceSnapshot
    String referenceSnapshot = params.get(BfConsts.ARG_DELTA_TESTRIG);
    if (referenceSnapshot != null) {
      SnapshotId referenceSnapshotId = idManager.getSnapshotId(referenceSnapshot, networkId);
      params.put(BfConsts.ARG_DELTA_TESTRIG, referenceSnapshotId.getId());
    }

    // analysis
    AnalysisId analysisId = null;
    String analysis = params.get(BfConsts.ARG_ANALYSIS_NAME);
    if (analysis != null) {
      analysisId = idManager.getAnalysisId(analysis, networkId);
      params.put(BfConsts.ARG_ANALYSIS_NAME, analysisId.getId());
    }

    // question
    String question = params.get(BfConsts.ARG_QUESTION_NAME);
    if (question != null) {
      QuestionId questionId = idManager.getQuestionId(question, networkId, analysisId);
      params.put(BfConsts.ARG_QUESTION_NAME, questionId.getId());
    }

    return new WorkItem(workItem.getId(), networkId.getId(), snapshotId.getId(), params);
  }

  public void startWorkManager() {
    // for some bizarre reason, this ordering of scheduling checktask before
    // assignwork, is important
    // in the other order, assignwork never fires
    // TODO: track this down
    // _checkWorkTask = new CheckTaskTask();
    // _checkService = Executors.newScheduledThreadPool(1);
    // _checkFuture = _checkService.scheduleAtFixedRate(_checkWorkTask, 0,
    // Main.getSettings().getPeriodCheckWorkMs(),
    // TimeUnit.MILLISECONDS);

    loadPlugins();

    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(
            new AssignWorkTask(),
            0,
            Main.getSettings().getPeriodAssignWorkMs(),
            TimeUnit.MILLISECONDS);
  }

  public int syncTestrigsSyncNow(String containerName, String pluginId, boolean force) {
    if (!_testrigSyncers.containsKey(pluginId)) {
      throw new BatfishException(
          "PluginId " + pluginId + " not found." + " (Are SyncTestrigs plugins loaded?)");
    }
    return _testrigSyncers.get(pluginId).syncNow(containerName, force);
  }

  public boolean syncTestrigsUpdateSettings(
      String containerName, String pluginId, Map<String, String> settings) {
    if (!_testrigSyncers.containsKey(pluginId)) {
      throw new BatfishException(
          "PluginId " + pluginId + " not found." + " (Are SyncTestrigs plugins loaded?)");
    }
    return _testrigSyncers.get(pluginId).updateSettings(containerName, settings);
  }

  /**
   * Upload a new environment to an existing testrig.
   *
   * @param containerName The container in which the testrig resides
   * @param testrigName The testrig in which the (optional base environment and) new environment
   *     reside
   * @param baseEnvName The name of an optional base environment. The new environment is initialized
   *     with files from this base if it is provided.
   * @param newEnvName The name of the new environment to be created
   * @param fileStream A stream providing the zip file containing the file structure of the new
   *     environment.
   */
  public void uploadEnvironment(
      String containerName,
      String testrigName,
      String baseEnvName,
      String newEnvName,
      InputStream fileStream) {
    Path testrigDir = getdirSnapshot(containerName, testrigName);
    Path environmentsDir =
        testrigDir.resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_ENVIRONMENTS_DIR));
    Path newEnvDir = environmentsDir.resolve(newEnvName);
    Path dstDir = newEnvDir.resolve(BfConsts.RELPATH_ENV_DIR);
    if (Files.exists(newEnvDir)) {
      throw new BatfishException(
          "Environment: '" + newEnvName + "' already exists for snapshot: '" + testrigName + "'");
    }
    if (!dstDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + dstDir + "'");
    }
    Path zipFile = CommonUtil.createTempFile("coord_up_env_", ".zip");
    CommonUtil.writeStreamToFile(fileStream, zipFile);

    /* First copy base environment if it is set */
    if (baseEnvName.length() > 0) {
      Path baseEnvPath = environmentsDir.resolve(Paths.get(baseEnvName, BfConsts.RELPATH_ENV_DIR));
      if (!Files.exists(baseEnvPath)) {
        CommonUtil.delete(zipFile);
        throw new BatfishException(
            "Base environment for copy does not exist: '" + baseEnvName + "'");
      }
      SortedSet<Path> baseFileList = CommonUtil.getEntries(baseEnvPath);
      dstDir.toFile().mkdirs();
      for (Path baseFile : baseFileList) {
        Path target;
        if (isEnvFile(baseFile)) {
          target = dstDir.resolve(baseFile.getFileName());
          CommonUtil.copy(baseFile, target);
        }
      }
    }

    // now unzip
    Path unzipDir = CommonUtil.createTempDirectory("coord_up_env_unzip_dir_");
    UnzipUtility.unzip(zipFile, unzipDir);

    /*-
     *  Sanity check what we got:
     *    There should be just one top-level folder
     */
    SortedSet<Path> unzipDirEntries = CommonUtil.getEntries(unzipDir);
    if (unzipDirEntries.size() != 1 || !Files.isDirectory(unzipDirEntries.iterator().next())) {
      CommonUtil.deleteDirectory(newEnvDir);
      CommonUtil.deleteDirectory(unzipDir);
      throw new BatfishException(
          "Unexpected packaging of environment. There should be just one top-level folder");
    }
    Path unzipSubdir = unzipDirEntries.iterator().next();
    SortedSet<Path> subFileList = CommonUtil.getEntries(unzipSubdir);

    // things look ok, now make the move
    for (Path subdirFile : subFileList) {
      Path target = dstDir.resolve(subdirFile.getFileName());
      CommonUtil.moveByCopy(subdirFile, target);
    }

    try {
      NetworkId networkId = _idManager.getNetworkId(containerName);
      SnapshotId snapshotId = _idManager.getSnapshotId(testrigName, networkId);
      TestrigMetadataMgr.initializeEnvironment(networkId, snapshotId, newEnvName);
    } catch (IOException e) {
      throw new BatfishException("Could not initialize environmentMetadata", e);
    }

    // delete the empty directory and the zip file
    CommonUtil.deleteDirectory(unzipDir);
    CommonUtil.deleteIfExists(zipFile);
  }

  public void uploadQuestion(String network, String question, String questionJson) {
    uploadQuestion(network, question, questionJson, true);
  }

  @VisibleForTesting
  void uploadQuestion(String network, String question, String questionJson, boolean validate) {
    if (validate) {
      // Validate the question before saving it to disk.
      try {
        Question.parseQuestion(questionJson);
      } catch (Exception e) {
        throw new BatfishException(
            String.format("Invalid question %s/%s: %s", network, question, e.getMessage()), e);
      }
    }
    NetworkId networkId;
    try {
      networkId = _idManager.getNetworkId(network);
    } catch (IllegalArgumentException e) {
      throw new InternalServerErrorException(
          String.format("Error uploading question: network=%s, question=%s", network, question), e);
    }
    QuestionId questionId = _idManager.generateQuestionId();
    _storage.storeQuestion(questionJson, networkId, questionId, null);
    _idManager.assignQuestion(question, networkId, questionId, null);
  }

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSS")
          .withLocale(Locale.getDefault())
          .withZone(ZoneOffset.UTC);

  static String generateFileDateString(String base, Instant instant) {
    return base + "_" + FORMATTER.format(instant);
  }

  private static String generateFileDateString(String base) {
    return generateFileDateString(base, Instant.now());
  }

  /**
   * Upload a new snapshot to the specified network.
   *
   * @param networkName Name of the network to upload the snapshot to.
   * @param snapshotName Name of the new snapshot.
   * @param fileStream {@link InputStream} of the snapshot zip.
   * @param autoAnalyze Boolean determining if the snapshot analysis should be triggered on upload.
   */
  public void uploadSnapshot(
      String networkName, String snapshotName, InputStream fileStream, boolean autoAnalyze) {
    Path networkDir = getdirNetwork(networkName);
    NetworkId networkId = _idManager.getNetworkId(networkName);

    // Fail early if the snapshot already exists
    if (_idManager.hasSnapshotId(snapshotName, networkId)) {
      throw new BatfishException("Snapshot with name: '" + snapshotName + "' already exists");
    }

    // Save uploaded zip for troubleshooting
    Path originalDir =
        networkDir
            .resolve(BfConsts.RELPATH_ORIGINAL_DIR)
            .resolve(generateFileDateString(snapshotName));
    if (!originalDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + originalDir + "'");
    }
    Path snapshotZipFile = originalDir.resolve(BfConsts.RELPATH_SNAPSHOT_ZIP_FILE);
    CommonUtil.writeStreamToFile(fileStream, snapshotZipFile);
    Path unzipDir = CommonUtil.createTempDirectory("tr");
    UnzipUtility.unzip(snapshotZipFile, unzipDir);

    try {
      initSnapshot(networkName, snapshotName, unzipDir, autoAnalyze);
    } catch (Exception e) {
      throw new BatfishException("Error initializing snapshot", e);
    } finally {
      CommonUtil.deleteDirectory(unzipDir);
    }
  }

  public boolean checkContainerExists(String containerName) {
    if (!_idManager.hasNetworkId(containerName)) {
      return false;
    }
    NetworkId id = _idManager.getNetworkId(containerName);
    return _storage.checkNetworkExists(id);
  }

  /**
   * Filter and sort {@code rawAnswers} according to options specified in {@code
   * analysisAnswersOptions}
   */
  public Map<String, Answer> processAnalysisAnswers(
      Map<String, String> rawAnswers, Map<String, AnswerRowsOptions> answersRowsOptions) {
    return CommonUtil.toImmutableMap(
        rawAnswers,
        Entry::getKey,
        rawAnswersEntry ->
            processAnswerRows(
                rawAnswersEntry.getValue(), answersRowsOptions.get(rawAnswersEntry.getKey())));
  }

  @VisibleForTesting
  @Nonnull
  Answer processAnswerRows(String rawAnswerStr, AnswerRowsOptions options) {
    if (rawAnswerStr == null) {
      Answer answer = Answer.failureAnswer("Not found", null);
      answer.setStatus(AnswerStatus.NOTFOUND);
      return answer;
    }
    try {
      Answer rawAnswer =
          BatfishObjectMapper.mapper().readValue(rawAnswerStr, new TypeReference<Answer>() {});
      // If the AnswerStatus is not SUCCESS, the answer cannot have any AnswerElements related to
      // actual answers (but, e.g., it might have a BatfishStackTrace). Return that as-is.
      if (rawAnswer.getStatus() != AnswerStatus.SUCCESS) {
        return rawAnswer;
      }
      TableAnswerElement rawTable = (TableAnswerElement) rawAnswer.getAnswerElements().get(0);
      Answer answer = new Answer();
      answer.setStatus(rawAnswer.getStatus());
      answer.addAnswerElement(processAnswerTable(rawTable, options));
      return answer;
    } catch (Exception e) {
      _logger.errorf("Failed to convert answer string to Answer: %s", e.getMessage());
      return Answer.failureAnswer(e.getMessage(), null);
    }
  }

  @VisibleForTesting
  @Nonnull
  TableAnswerElement processAnswerTable(TableAnswerElement rawTable, AnswerRowsOptions options) {
    Map<String, ColumnMetadata> rawColumnMap = rawTable.getMetadata().toColumnMap();
    List<Row> filteredRows =
        rawTable
            .getRowsList()
            .stream()
            .filter(row -> options.getFilters().stream().allMatch(filter -> filter.matches(row)))
            .collect(ImmutableList.toImmutableList());

    Stream<Row> sortedStream =
        options.getSortOrder().isEmpty()
            ? filteredRows.stream()
            : filteredRows.stream().sorted(buildComparator(rawColumnMap, options.getSortOrder()));
    Stream<Row> projectedStream;
    TableAnswerElement table;
    if (options.getColumns().isEmpty()) {
      projectedStream = sortedStream;
      table = new TableAnswerElement(rawTable.getMetadata());
    } else {
      projectedStream =
          sortedStream.map(rawRow -> Row.builder().putAll(rawRow, options.getColumns()).build());
      Map<String, ColumnMetadata> columnMap = new LinkedHashMap<>(rawColumnMap);
      columnMap.keySet().retainAll(options.getColumns());
      List<ColumnMetadata> columnMetadata =
          columnMap.values().stream().collect(ImmutableList.toImmutableList());
      table =
          new TableAnswerElement(
              new TableMetadata(columnMetadata, rawTable.getMetadata().getTextDesc()));
    }
    Stream<Row> uniquifiedStream = projectedStream;
    if (options.getUniqueRows()) {
      uniquifiedStream = uniquifiedStream.distinct();
    }
    Stream<Row> truncatedStream =
        uniquifiedStream.skip(options.getRowOffset()).limit(options.getMaxRows());
    truncatedStream.forEach(table::addRow);
    table.setSummary(rawTable.getSummary() != null ? rawTable.getSummary() : new AnswerSummary());
    table.getSummary().setNumResults(filteredRows.size());
    return table;
  }

  @VisibleForTesting
  @Nonnull
  Comparator<Row> buildComparator(
      Map<String, ColumnMetadata> rawColumnMap, List<ColumnSortOption> sortOrder) {
    ColumnSortOption firstColumnSortOption = sortOrder.get(0);
    ColumnMetadata firstMetadata = rawColumnMap.get(firstColumnSortOption.getColumn());
    Comparator<Row> comparator = columnComparator(firstMetadata);
    if (firstColumnSortOption.getReversed()) {
      comparator = comparator.reversed();
    }
    for (int i = 1; i < sortOrder.size(); i++) {
      ColumnSortOption columnSortOption = sortOrder.get(i);
      Comparator<Row> nextComparator =
          columnComparator(rawColumnMap.get(columnSortOption.getColumn()));
      if (columnSortOption.getReversed()) {
        nextComparator = nextComparator.reversed();
      }
      comparator = comparator.thenComparing(nextComparator);
    }
    return comparator;
  }

  @VisibleForTesting
  Comparator<Row> columnComparator(ColumnMetadata columnMetadata) {
    Schema schema = columnMetadata.getSchema();
    String column = columnMetadata.getName();
    if (schema.equals(Schema.INTEGER)) {
      return Comparator.comparing(r -> r.getInteger(column));
    } else if (schema.equals(Schema.ISSUE)) {
      return Comparator.comparing(r -> r.getIssue(column).getSeverity());
    } else if (schema.equals(Schema.STRING)) {
      return Comparator.comparing(r -> r.getString(column));
    } else {
      String message = String.format("Unsupported Schema for sorting: %s", schema);
      _logger.error(message);
      throw new UnsupportedOperationException(message);
    }
  }

  /**
   * Return the JSON-serialized settings for the specified question class for the specified network;
   * or {@code null} if either no custom settings exist for the question or no value is present at
   * the path produced from the sepcified components.
   *
   * @param network The name of the network
   * @param questionClassId The ID of the class of questions whose settings are to be returned
   * @param components The components to traverse from the root of the question settings to reach
   *     the desired section or value
   * @throws IOException if there is an error reading the settings
   */
  public @Nullable String getQuestionSettings(
      String network, String questionClassId, List<String> components) throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network);
    String questionSettings;
    questionSettings = _storage.loadQuestionSettings(networkId, questionClassId);
    if (questionSettings == null) {
      return null;
    }
    if (components.isEmpty()) {
      return questionSettings;
    }
    JsonNode root = BatfishObjectMapper.mapper().readTree(questionSettings);
    JsonNode current = root;
    for (String component : components) {
      if (!current.has(component)) {
        return null;
      }
      current = current.get(component);
    }
    return BatfishObjectMapper.writeString(current);
  }

  /**
   * Write the JSON settings for the specified question class for the specified network at the end
   * of the path computed from the specified components. Any absent components will be created.
   *
   * @param network The name of the network
   * @param questionClass The fully-qualified class name of the question
   * @param components The components to traverse from the root of the question settings to reach
   *     the desired section or value
   * @param value The settings value to write at the end of the path
   * @throws IOException if there is an error writing the settings
   */
  public synchronized void writeQuestionSettings(
      String network, String questionClass, List<String> components, JsonNode value)
      throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network);
    String questionSettings;
    questionSettings = _storage.loadQuestionSettings(networkId, questionClass);
    JsonNodeFactory factory = BatfishObjectMapper.mapper().getNodeFactory();
    JsonNode root;
    if (!components.isEmpty()) {
      root =
          questionSettings != null
              ? (ObjectNode) BatfishObjectMapper.mapper().readTree(questionSettings)
              : new ObjectNode(factory);
      ObjectNode current = (ObjectNode) root;
      for (String component : components.subList(0, components.size() - 1)) {
        if (!current.has(component)) {
          current.set(component, new ObjectNode(factory));
        }
        current = (ObjectNode) current.get(component);
      }
      current.set(components.get(components.size() - 1), value);
    } else {
      root = value;
    }
    _storage.storeQuestionSettings(
        BatfishObjectMapper.writePrettyString(root), networkId, questionClass);
  }

  public MajorIssueConfig getMajorIssueConfig(String network, String majorIssueType)
      throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network);
    return _storage.loadMajorIssueConfig(
        networkId, getOrCreateIssueSettingsId(networkId, majorIssueType));
  }

  @VisibleForTesting
  public IdManager getIdManager() {
    return _idManager;
  }

  public TestrigMetadata getTestrigMetadata(String networkName, String snapshot)
      throws IOException {
    NetworkId networkId = _idManager.getNetworkId(networkName);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, networkId);
    return TestrigMetadataMgr.readMetadata(networkId, snapshotId);
  }

  public void writeNodeRoles(NodeRolesData nodeRolesData, String network) throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network);
    _storage.storeNodeRoles(nodeRolesData, networkId);
  }

  /**
   * Reads the {@link NodeRolesData} object for the provided network. If none exists, initializes a
   * new object.
   *
   * @param network The name of the network
   * @return The read data
   * @throws IOException If there is an error
   */
  public NodeRolesData getNodeRolesData(String network) throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network);
    if (_storage.hasNodeRoles(networkId)) {
      return BatfishObjectMapper.mapper()
          .readValue(_storage.loadNodeRoles(networkId), NodeRolesData.class);
    } else {
      return new NodeRolesData(null, new Date().toInstant(), null);
    }
  }

  private NodeRolesData getNodeRolesDataWrapped(String network) {
    try {
      return getNodeRolesData(network);
    } catch (IOException e) {
      throw new BatfishException("error reading node roles", e);
    }
  }
}
