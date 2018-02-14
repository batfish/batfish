package org.batfish.coordinator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Pair;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.plugin.AbstractCoordinator;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UnzipUtility;
import org.batfish.common.util.WorkItemBuilder;
import org.batfish.common.util.ZipUtility;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkQueueMgr.QueueType;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.AnalysisMetadata;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.questions.Question;
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

  private static final Set<String> ENV_FILENAMES = initEnvFilenames();

  private static final int MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES = 10;

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

  private final BatfishLogger _logger;

  private final Settings _settings;

  private WorkQueueMgr _workQueueMgr;

  public WorkMgr(Settings settings, BatfishLogger logger) {
    super(false);
    _settings = settings;
    _logger = logger;
    _workQueueMgr = new WorkQueueMgr(logger);
    loadPlugins();
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
      _logger.errorf("Got exception in assignWork: %s\n", ExceptionUtils.getFullStackTrace(e));
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
      Path containerDir =
          Main.getSettings().getContainersLocation().resolve(work.getWorkItem().getContainerName());
      String testrigName = work.getWorkItem().getTestrigName();
      Path testrigBaseDir =
          containerDir
              .resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, testrigName))
              .toAbsolutePath();
      task.put(BfConsts.ARG_CONTAINER_DIR, containerDir.toAbsolutePath().toString());
      task.put(BfConsts.ARG_TESTRIG, testrigName);
      task.put(
          BfConsts.ARG_LOG_FILE,
          testrigBaseDir.resolve(work.getId() + BfConsts.SUFFIX_LOG_FILE).toString());
      task.put(
          BfConsts.ARG_ANSWER_JSON_PATH,
          testrigBaseDir.resolve(work.getId() + BfConsts.SUFFIX_ANSWER_JSON_FILE).toString());

      client =
          CommonUtil.createHttpClientBuilder(
                  _settings.getSslPoolDisable(),
                  _settings.getSslPoolTrustAllCerts(),
                  _settings.getSslPoolKeystoreFile(),
                  _settings.getSslPoolKeystorePassword(),
                  _settings.getSslPoolTruststoreFile(),
                  _settings.getSslPoolTruststorePassword())
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
      String stackTrace = ExceptionUtils.getFullStackTrace(e);
      _logger.error(String.format("Unable to connect to worker at %s: %s\n", worker, stackTrace));
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getFullStackTrace(e);
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
        String stackTrace = ExceptionUtils.getFullStackTrace(e);
        _logger.errorf("Unable to markAssignmentError for work %s: %s\n", work, stackTrace);
      }
    } else if (assigned) {
      try {
        _workQueueMgr.markAssignmentSuccess(work, worker);
      } catch (Exception e) {
        String stackTrace = ExceptionUtils.getFullStackTrace(e);
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
      _logger.errorf("Got exception in checkTasks: %s\n", ExceptionUtils.getFullStackTrace(e));
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
                  _settings.getSslPoolTruststorePassword())
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
          BatfishObjectMapper mapper = new BatfishObjectMapper();
          task = mapper.readValue(taskStr, Task.class);
          if (task.getStatus() == null) {
            _logger.error("did not see status key in json response\n");
          }
        }
      }
    } catch (ProcessingException e) {
      String stackTrace = ExceptionUtils.getFullStackTrace(e);
      _logger.error(String.format("unable to connect to %s: %s\n", worker, stackTrace));
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getFullStackTrace(e);
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
      _logger.errorf("exception: %s\n", ExceptionUtils.getFullStackTrace(e));
    }

    // if the task ended, send a hint to the pool manager to look up worker status
    if (task.getStatus().isTerminated()) {
      Main.getPoolMgr().refreshWorkerStatus(worker);
    }
  }

  private WorkDetails computeWorkDetails(WorkItem workItem) {

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
      String qName = WorkItemBuilder.getQuestionName(workItem);
      if (qName == null) {
        throw new BatfishException("Question name not provided for ANSWER work");
      }
      Path qFile = getpathContainerQuestion(workItem.getContainerName(), qName);
      Question question = Question.parseQuestion(qFile, getCurrentClassLoader());
      workType =
          question.getDataPlane()
              ? WorkType.DATAPLANE_DEPENDENT_ANSWERING
              : WorkType.DATAPLANE_INDEPENDENT_ANSWERING;
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
      workType = WorkType.DATAPLANE_INDEPENDENT_ANSWERING;
      for (String qName : qNames) {
        Path qFile = getpathAnalysisQuestion(workItem.getContainerName(), aName, qName);
        Question question = Question.parseQuestion(qFile, getCurrentClassLoader());
        if (question.getDataPlane()) {
          workType = WorkType.DATAPLANE_DEPENDENT_ANSWERING;
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
    Path containerDir = getdirContainer(containerName);
    Path aDir = containerDir.resolve(Paths.get(BfConsts.RELPATH_ANALYSES_DIR, aName));
    if (Files.exists(aDir) && newAnalysis) {
      throw new BatfishException(
          "Analysis '" + aName + "' already exists for container '" + containerName);
    }
    if (!Files.exists(aDir)) {
      if (!newAnalysis) {
        throw new BatfishException(
            "Analysis '" + aName + "' does not exist for container '" + containerName + "'");
      }
      if (!aDir.toFile().mkdirs()) {
        throw new BatfishException("Failed to create analysis directory '" + aDir + "'");
      }
    }

    // Create metadata if it's a new analysis, or update it if suggested is not null
    if (newAnalysis || suggested != null) {
      AnalysisMetadata metadata;
      if (newAnalysis) {
        metadata = new AnalysisMetadata(Instant.now(), (suggested != null) && suggested);
      } else if (!Files.exists(getpathAnalysisMetadata(containerName, aName))) {
        // Configuring an old analysis with no metadata file; create one. Know suggested != null
        metadata = new AnalysisMetadata(Instant.MIN, suggested);
      } else {
        try {
          metadata = AnalysisMetadataMgr.readMetadata(containerName, aName);
          metadata.setSuggested(suggested);
        } catch (IOException e) {
          throw new BatfishException(
              "Unable to read metadata file for analysis '" + aName + "'", e);
        }
      }
      // Write metadata to file
      try {
        AnalysisMetadataMgr.writeMetadata(metadata, containerName, aName);
      } catch (JsonProcessingException e) {
        throw new BatfishException("Could not write analysisMetadata", e);
      }
    }

    Path questionsDir = aDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    for (Entry<String, String> entry : questionsToAdd.entrySet()) {
      Path qDir = questionsDir.resolve(entry.getKey());
      if (Files.exists(qDir)) {
        throw new BatfishException(
            String.format("Question '%s' already exists for analysis '%s'", entry.getKey(), aName));
      }
      if (!qDir.toFile().mkdirs()) {
        throw new BatfishException(String.format("Failed to create question directory '%s'", qDir));
      }
      Path qFile = qDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      CommonUtil.writeFile(qFile, entry.getValue());
    }

    /** Delete questions */
    for (String qName : questionsToDelete) {
      Path qDir = questionsDir.resolve(qName);
      if (!Files.exists(qDir)) {
        throw new BatfishException("Question " + qName + " does not exist for analysis " + aName);
      }
      CommonUtil.deleteDirectory(qDir);
    }
  }

  public void delAnalysis(String containerName, String aName) {
    Path aDir = getdirContainerAnalysis(containerName, aName);
    CommonUtil.deleteDirectory(aDir);
  }

  public boolean delContainer(String containerName) {
    Path containerDir = getdirContainer(containerName, false);
    if (Files.exists(containerDir)) {
      CommonUtil.deleteDirectory(containerDir);
      return true;
    }
    return false;
  }

  public void delEnvironment(String containerName, String testrigName, String envName) {
    Path envDir = getdirEnvironment(containerName, testrigName, envName);
    CommonUtil.deleteDirectory(envDir);
  }

  public void delTestrig(String containerName, String testrigName) {
    Path testrigDir = getdirTestrig(containerName, testrigName);
    CommonUtil.deleteDirectory(testrigDir);
  }

  public void delQuestion(String containerName, String qName) {
    Path qDir = getdirContainerQuestion(containerName, qName);
    CommonUtil.deleteDirectory(qDir);
  }

  public Map<String, String> getAnalysisAnswers(
      String containerName,
      String baseTestrig,
      String baseEnv,
      String deltaTestrig,
      String deltaEnv,
      String analysisName)
      throws JsonProcessingException {
    Path analysisDir = getdirContainerAnalysis(containerName, analysisName);
    Path testrigDir = getdirTestrig(containerName, baseTestrig);
    SortedSet<String> questions = listAnalysisQuestions(containerName, analysisName);
    Map<String, String> retMap = new TreeMap<>();
    for (String questionName : questions) {
      String answer = "unknown";
      Path questionFile =
          analysisDir.resolve(
              Paths.get(
                  BfConsts.RELPATH_QUESTIONS_DIR, questionName, BfConsts.RELPATH_QUESTION_FILE));
      if (!Files.exists(questionFile)) {
        throw new BatfishException("Question file for question " + questionName + "not found");
      }
      Path answerDir =
          testrigDir.resolve(
              Paths.get(
                  BfConsts.RELPATH_ANALYSES_DIR,
                  analysisName,
                  BfConsts.RELPATH_QUESTIONS_DIR,
                  questionName,
                  BfConsts.RELPATH_ENVIRONMENTS_DIR,
                  baseEnv));
      if (deltaTestrig != null) {
        answerDir = answerDir.resolve(Paths.get(BfConsts.RELPATH_DELTA, deltaTestrig, deltaEnv));
      }
      Path answerFile = answerDir.resolve(BfConsts.RELPATH_ANSWER_JSON);
      if (!Files.exists(answerFile)) {
        Answer ans = Answer.failureAnswer("Not answered", null);
        ans.setStatus(AnswerStatus.NOTFOUND);
        BatfishObjectMapper mapper = new BatfishObjectMapper();
        answer = mapper.writeValueAsString(ans);
      } else {
        boolean answerIsStale;
        answerIsStale =
            CommonUtil.getLastModifiedTime(questionFile)
                    .compareTo(CommonUtil.getLastModifiedTime(answerFile))
                > 0;
        if (answerIsStale) {
          Answer ans = Answer.failureAnswer("Not fresh", null);
          ans.setStatus(AnswerStatus.STALE);
          BatfishObjectMapper mapper = new BatfishObjectMapper();
          answer = mapper.writeValueAsString(ans);
        } else {
          answer = CommonUtil.readFile(answerFile);
        }
      }

      retMap.put(questionName, answer);
    }
    return retMap;
  }

  public String getAnalysisQuestion(
      String containerName, String analysisName, String questionName) {
    Path qFile = getpathAnalysisQuestion(containerName, analysisName, questionName);
    if (!Files.exists(qFile)) {
      throw new BatfishException("Question file not found for " + questionName);
    }
    return CommonUtil.readFile(qFile);
  }

  public String getAnswer(
      String containerName,
      String baseTestrig,
      String baseEnv,
      String deltaTestrig,
      String deltaEnv,
      String questionName)
      throws JsonProcessingException {
    Path questionDir = getdirContainerQuestion(containerName, questionName);
    Path questionFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
    if (!Files.exists(questionFile)) {
      throw new BatfishException("Question file not found for " + questionName);
    }
    Path testrigDir = getdirTestrig(containerName, baseTestrig);
    Path answerDir =
        testrigDir.resolve(Paths.get(BfConsts.RELPATH_ANSWERS_DIR, questionName, baseEnv));
    if (deltaTestrig != null) {
      answerDir = answerDir.resolve(Paths.get(BfConsts.RELPATH_DIFF_DIR, deltaTestrig, deltaEnv));
    } else {
      answerDir = answerDir.resolve(Paths.get(BfConsts.RELPATH_STANDARD_DIR));
    }
    Path answerFile = answerDir.resolve(BfConsts.RELPATH_ANSWER_JSON);
    String answer = "unknown";
    if (!Files.exists(answerFile)) {
      Answer ans = Answer.failureAnswer("Not answered", null);
      ans.setStatus(AnswerStatus.NOTFOUND);
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      answer = mapper.writeValueAsString(ans);
    } else {
      if (CommonUtil.getLastModifiedTime(questionFile)
              .compareTo(CommonUtil.getLastModifiedTime(answerFile))
          > 0) {
        Answer ans = Answer.failureAnswer("Not fresh", null);
        ans.setStatus(AnswerStatus.STALE);
        BatfishObjectMapper mapper = new BatfishObjectMapper();
        answer = mapper.writeValueAsString(ans);
      } else {
        answer = CommonUtil.readFile(answerFile);
      }
    }
    return answer;
  }

  /**
   * Returns a string representation of the content of configuration file {@code configName}.
   *
   * @throws BatfishException if the configuration file {@code configName} does not exist, or there
   *     are more than one file with name {@code configName}, or failed to read content from the
   *     file.
   */
  public String getConfiguration(String containerName, String testrigName, String configName) {
    Path testrigPath = getdirTestrig(containerName, testrigName);
    try (Stream<Path> paths = Files.walk(testrigPath.resolve(BfConsts.RELPATH_TEST_RIG_DIR))) {
      List<Path> configPaths =
          paths
              .filter(x -> x.getFileName().toString().equals(configName))
              .collect(Collectors.toList());
      if (configPaths.isEmpty()) {
        throw new BatfishException(
            String.format(
                "Configuration file %s does not exist in testrig %s for container %s",
                configName, testrigName, containerName));
      } else if (configPaths.size() > 1) {
        throw new BatfishException(
            String.format(
                "More than one configuration file with name %s in testrig %s for container %s",
                configName, testrigName, containerName));
      }
      String configContent = "";
      try {
        configContent = new String(Files.readAllBytes(configPaths.get(0)));
      } catch (IOException e) {
        throw new BatfishException(
            String.format(
                "Failed to read configuration file %s in testrig %s for container %s",
                configName, testrigName, containerName),
            e);
      }
      return configContent;
    } catch (IOException e) {
      throw new BatfishException(
          String.format(
              "Failed to list directory %s", testrigPath.resolve(BfConsts.RELPATH_TEST_RIG_DIR)));
    }
  }

  /** Return a {@link Container container} contains all testrigs directories inside it. */
  public Container getContainer(String containerName) {
    return getContainer(getdirContainer(containerName));
  }

  /** Return a {@link Container container} contains all testrigs directories inside it */
  public Container getContainer(Path containerDir) {
    SortedSet<String> testrigs =
        new TreeSet<>(
            CommonUtil.getSubdirectories(containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR))
                .stream()
                .map(dir -> dir.getFileName().toString())
                .collect(Collectors.toSet()));

    return Container.of(containerDir.toFile().getName(), testrigs);
  }

  private Path getdirAnalysisQuestion(String containerName, String analysisName, String qName) {
    Path analysisDir = getdirContainerAnalysis(containerName, analysisName);
    Path qDir = analysisDir.resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, qName));
    if (!Files.exists(qDir)) {
      throw new BatfishException("Question '" + qName + "' does not exist");
    }
    return qDir;
  }

  @Override
  public Path getdirContainer(String containerName) {
    return getdirContainer(containerName, true);
  }

  @Override
  public BatfishLogger getLogger() {
    return _logger;
  }

  @Override
  public Set<String> getContainerNames() {
    Path containersDir = Main.getSettings().getContainersLocation();
    if (!Files.exists(containersDir)) {
      containersDir.toFile().mkdirs();
    }
    SortedSet<String> containers =
        new TreeSet<>(
            CommonUtil.getSubdirectories(containersDir)
                .stream()
                .map(dir -> dir.getFileName().toString())
                .collect(Collectors.toSet()));
    return containers;
  }

  private static Path getdirContainer(String containerName, boolean errIfNotEixst) {
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve(containerName).toAbsolutePath();
    if (errIfNotEixst && !Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' does not exist");
    }
    return containerDir;
  }

  private Path getdirContainerAnalysis(String containerName, String analysisName) {
    Path containerDir = getdirContainer(containerName);
    Path aDir = containerDir.resolve(Paths.get(BfConsts.RELPATH_ANALYSES_DIR, analysisName));
    if (!Files.exists(aDir)) {
      throw new BatfishException(
          "Analysis '" + analysisName + "' does not exists for container '" + containerName + "'");
    }
    return aDir;
  }

  private Path getdirContainerQuestion(String containerName, String qName) {
    Path containerDir = getdirContainer(containerName);
    Path qDir = containerDir.resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, qName));
    if (!Files.exists(qDir)) {
      throw new BatfishException("Question '" + qName + "' does not exist");
    }
    return qDir;
  }

  private Path getdirEnvironment(String containerName, String testrigName, String envName) {
    Path testrigDir = getdirTestrig(containerName, testrigName);
    Path envDir = testrigDir.resolve(Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName));
    if (!Files.exists(envDir)) {
      throw new BatfishException("Environment '" + envName + "' does not exist");
    }
    return envDir;
  }

  public Path getdirTestrig(String containerName, String testrigName) {
    Path testrigDir = getdirTestrigs(containerName).resolve(Paths.get(testrigName));
    if (!Files.exists(testrigDir)) {
      throw new BatfishException("Testrig '" + testrigName + "' does not exist");
    }
    return testrigDir;
  }

  @Override
  public Path getdirTestrigs(String containerName) {
    return getdirContainer(containerName).resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR));
  }

  public Path getpathAnalysisQuestion(
      String containerName, String analysisName, String questionName) {
    Path questionDir = getdirAnalysisQuestion(containerName, analysisName, questionName);
    return questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
  }

  public Path getpathContainerQuestion(String containerName, String questionName) {
    Path questionDir = getdirContainerQuestion(containerName, questionName);
    return questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
  }

  // this function should build on others but some overrides are getting in the way
  // TODO: cleanup later
  public static Path getpathAnalysisMetadata(String container, String analysis) {
    return Main.getSettings()
        .getContainersLocation()
        .resolve(container)
        .resolve(BfConsts.RELPATH_ANALYSES_DIR)
        .resolve(analysis)
        .resolve(BfConsts.RELPATH_METADATA_FILE);
  }

  // this function should build on others but some overrides are getting in the way
  // TODO: cleanup later
  public static Path getpathTestrigMetadata(String container, String testrig) {
    return Main.getSettings()
        .getContainersLocation()
        .resolve(container)
        .resolve(BfConsts.RELPATH_TESTRIGS_DIR)
        .resolve(testrig)
        .resolve(BfConsts.RELPATH_METADATA_FILE);
  }

  public JSONObject getStatusJson() throws JSONException {
    return _workQueueMgr.getStatusJson();
  }

  public String getTestrigInfo(String containerName, String testrigName) {
    Path testrigDir = getdirTestrig(containerName, testrigName);
    Path submittedTestrigDir = testrigDir.resolve(BfConsts.RELPATH_TEST_RIG_DIR);
    if (!Files.exists(submittedTestrigDir)) {
      return "Missing folder '"
          + BfConsts.RELPATH_TEST_RIG_DIR
          + "' for testrig '"
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

  public TestrigMetadata getTestrigMetadata(String containerName, String testrigName)
      throws IOException {
    return TestrigMetadataMgr.readMetadata(getpathTestrigMetadata(containerName, testrigName));
  }

  @Nullable
  public Path getTestrigObject(String containerName, String testrigName, String objectName) {
    Path testrigDir = getdirTestrig(containerName, testrigName);
    Path file = testrigDir.resolve(objectName);
    /**
     * Check if we got an object name outside of the testrig folder, perhaps because of ".." in the
     * name; disallow it
     */
    if (!CommonUtil.getCanonicalPath(file).startsWith(CommonUtil.getCanonicalPath(testrigDir))) {
      throw new BatfishException("Illegal object name: '" + objectName + "'");
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

  public String getQuestion(String containerName, String questionName) {
    Path containerDir = getdirContainer(containerName, true);
    Path questionDir =
        containerDir.resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, questionName));
    Path qFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
    if (!Files.exists(qFile)) {
      throw new BatfishException("Question file not found for " + questionName);
    }
    return CommonUtil.readFile(qFile);
  }

  public QueuedWork getMatchingWork(WorkItem workItem, QueueType qType) {
    return _workQueueMgr.getMatchingWork(workItem, qType);
  }

  public QueuedWork getWork(UUID workItemId) {
    return _workQueueMgr.getWork(workItemId);
  }

  public String initContainer(@Nullable String containerName, @Nullable String containerPrefix) {
    if (containerName == null || containerName.equals("")) {
      containerName = containerPrefix + "_" + UUID.randomUUID();
    }
    Path containerDir = Main.getSettings().getContainersLocation().resolve(containerName);
    if (Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' already exists!");
    }
    if (!containerDir.toFile().mkdirs()) {
      throw new BatfishException("failed to create directory '" + containerDir + "'");
    }
    Path testrigsDir = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR);
    if (!testrigsDir.toFile().mkdir()) {
      throw new BatfishException("failed to create directory '" + testrigsDir + "'");
    }
    Path analysesDir = containerDir.resolve(BfConsts.RELPATH_ANALYSES_DIR);
    if (!analysesDir.toFile().mkdir()) {
      throw new BatfishException("failed to create directory '" + analysesDir + "'");
    }
    Path questionsDir = containerDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    if (!questionsDir.toFile().mkdir()) {
      throw new BatfishException("failed to create directory '" + questionsDir + "'");
    }
    return containerName;
  }

  @Override
  public void initTestrig(
      String containerName, String testrigName, Path srcDir, boolean autoAnalyze) {
    /*
     * Sanity check what we got:
     *    There should be just one top-level folder.
     */
    SortedSet<Path> srcDirEntries = CommonUtil.getEntries(srcDir);
    if (srcDirEntries.size() != 1 || !Files.isDirectory(srcDirEntries.iterator().next())) {
      throw new BatfishException(
          "Unexpected packaging of testrig. There should be just one top-level folder");
    }

    Path srcSubdir = srcDirEntries.iterator().next();
    SortedSet<Path> subFileList = CommonUtil.getEntries(srcSubdir);

    Path containerDir = getdirContainer(containerName);
    Path testrigDir = containerDir.resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, testrigName));

    if (!testrigDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + testrigDir + "'");
    }

    // Now that the directory exists, we must also create the metadata.
    try {
      TestrigMetadataMgr.writeMetadata(
          new TestrigMetadata(Instant.now(), BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME),
          testrigDir.resolve(BfConsts.RELPATH_METADATA_FILE));
    } catch (Exception e) {
      BatfishException metadataError = new BatfishException("Could not write testrigMetadata", e);
      try {
        CommonUtil.deleteDirectory(testrigDir);
      } catch (Exception inner) {
        metadataError.addSuppressed(inner);
      }
      throw metadataError;
    }

    Path srcTestrigDir = testrigDir.resolve(BfConsts.RELPATH_TEST_RIG_DIR);

    // create empty default environment
    Path defaultEnvironmentLeafDir =
        testrigDir.resolve(
            Paths.get(
                BfConsts.RELPATH_ENVIRONMENTS_DIR,
                BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
                BfConsts.RELPATH_ENV_DIR));
    defaultEnvironmentLeafDir.toFile().mkdirs();

    // things look ok, now make the move
    boolean routingTables = false;
    boolean bgpTables = false;
    for (Path subFile : subFileList) {
      Path target;
      if (isEnvFile(subFile)) {
        String name = subFile.getFileName().toString();
        if (name.equals(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES)) {
          routingTables = true;
        }
        if (name.equals(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES)) {
          bgpTables = true;
        }
        target = defaultEnvironmentLeafDir.resolve(subFile.getFileName());
      } else {
        target = srcTestrigDir.resolve(subFile.getFileName());
      }
      CommonUtil.copy(subFile, target);
    }
    _logger.infof(
        "Environment data for testrig:%s; bgpTables:%s, routingTables:%s\n",
        testrigName, bgpTables, routingTables);

    if (autoAnalyze) {
      List<WorkItem> autoWorkQueue = new LinkedList<>();

      WorkItem parseWork = WorkItemBuilder.getWorkItemParse(containerName, testrigName);
      autoWorkQueue.add(parseWork);

      Set<String> analysisNames = listAnalyses(containerName, null);
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

      for (WorkItem workItem : autoWorkQueue) {
        if (!queueWork(workItem)) {
          throw new BatfishException("Unable to queue work while auto processing: " + workItem);
        }
      }
    }
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
      _logger.errorf("exception: %s\n", ExceptionUtils.getFullStackTrace(e));
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
                  _settings.getSslPoolTruststorePassword())
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
            Task task = new BatfishObjectMapper().readValue(array.getString(1), Task.class);
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
      _logger.errorf("unable to connect to %s: %s\n", worker, ExceptionUtils.getFullStackTrace(e));
    } catch (Exception e) {
      _logger.errorf("exception: %s\n", ExceptionUtils.getFullStackTrace(e));
    } finally {
      if (client != null) {
        client.close();
      }
    }
    return killed;
  }

  public SortedSet<String> listAnalyses(String containerName, @Nullable Boolean suggested) {
    Path containerDir = getdirContainer(containerName);
    Path analysesDir = containerDir.resolve(BfConsts.RELPATH_ANALYSES_DIR);
    if (!Files.exists(analysesDir)) {
      return new TreeSet<>();
    }
    SortedSet<String> analyses =
        new TreeSet<>(
            CommonUtil.getSubdirectories(analysesDir)
                .stream()
                .map(subdir -> subdir.getFileName().toString())
                .filter(
                    aName -> {
                      // Include the analysis if suggested is null or matches metadata.suggested
                      return suggested == null
                          || AnalysisMetadataMgr.getAnalysisSuggestedOrFalse(containerName, aName)
                              == suggested;
                    })
                .collect(Collectors.toSet()));
    return analyses;
  }

  public SortedSet<String> listAnalysisQuestions(String containerName, String analysisName) {
    Path analysisDir = getdirContainerAnalysis(containerName, analysisName);
    Path questionsDir = analysisDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    if (!Files.exists(questionsDir)) {
      /** TODO: Something better than returning empty set? */
      return new TreeSet<>();
    }
    SortedSet<Path> subdirectories = CommonUtil.getSubdirectories(questionsDir);
    SortedSet<String> subdirectoryNames =
        new TreeSet<>(
            subdirectories
                .stream()
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toSet()));
    return subdirectoryNames;
  }

  public SortedSet<String> listContainers(String apiKey) {
    Path containersDir = Main.getSettings().getContainersLocation();
    if (!Files.exists(containersDir)) {
      containersDir.toFile().mkdirs();
    }
    SortedSet<String> authorizedContainers =
        new TreeSet<>(
            CommonUtil.getSubdirectories(containersDir)
                .stream()
                .map(dir -> dir.getFileName().toString())
                .filter(
                    container ->
                        Main.getAuthorizer().isAccessibleContainer(apiKey, container, false))
                .collect(Collectors.toSet()));
    return authorizedContainers;
  }

  public List<Container> getContainers(@Nullable String apiKey) {
    return listContainers(apiKey).stream().map(this::getContainer).collect(Collectors.toList());
  }

  public SortedSet<String> listEnvironments(String containerName, String testrigName) {
    Path testrigDir = getdirTestrig(containerName, testrigName);
    Path environmentsDir = testrigDir.resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR);
    if (!Files.exists(environmentsDir)) {
      return new TreeSet<>();
    }
    SortedSet<String> environments =
        new TreeSet<>(
            CommonUtil.getSubdirectories(environmentsDir)
                .stream()
                .map(dir -> dir.getFileName().toString())
                .collect(Collectors.toSet()));
    return environments;
  }

  public List<QueuedWork> listIncompleteWork(
      String containerName, @Nullable String testrigName, @Nullable WorkType workType) {
    return _workQueueMgr.listIncompleteWork(containerName, testrigName, workType);
  }

  public SortedSet<String> listQuestions(String containerName) {
    Path containerDir = getdirContainer(containerName);
    Path questionsDir = containerDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    if (!Files.exists(questionsDir)) {
      return new TreeSet<>();
    }
    SortedSet<String> questions =
        new TreeSet<>(
            CommonUtil.getSubdirectories(questionsDir)
                .stream()
                .map(dir -> dir.getFileName().toString())
                .collect(Collectors.toSet()));
    return questions;
  }

  public List<String> listTestrigs(String containerName) {
    Path containerDir = getdirContainer(containerName);
    Path testrigsDir = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR);
    if (!Files.exists(testrigsDir)) {
      return new ArrayList<>();
    }
    List<String> testrigs =
        CommonUtil.getSubdirectories(testrigsDir)
            .stream()
            .map(dir -> dir.getFileName().toString())
            .sorted(
                (t1, t2) -> { // reverse sorting by creation-time, name
                  String key1 =
                      TestrigMetadataMgr.getTestrigCreationTimeOrMin(containerName, t1) + t1;
                  String key2 =
                      TestrigMetadataMgr.getTestrigCreationTimeOrMin(containerName, t2) + t2;
                  return key2.compareTo(key1);
                })
            .collect(Collectors.toList());
    return testrigs;
  }

  public void putObject(
      String containerName, String testrigName, String objectName, InputStream fileStream) {
    Path testrigDir = getdirTestrig(containerName, testrigName);
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
    Path testrigDir = getdirTestrig(workItem.getContainerName(), workItem.getTestrigName());
    if (workItem.getTestrigName().isEmpty() || !Files.exists(testrigDir)) {
      throw new BatfishException("Non-existent testrig: '" + testrigDir.getFileName() + "'");
    }
    boolean success;
    try {
      workItem.setSourceSpan(GlobalTracer.get().activeSpan());
      WorkDetails workDetails = computeWorkDetails(workItem);
      try {
        if (TestrigMetadataMgr.getEnvironmentMetadata(
                    workItem.getContainerName(), workDetails.baseTestrig, workDetails.baseEnv)
                == null
            || (workDetails.isDifferential
                && TestrigMetadataMgr.getEnvironmentMetadata(
                        workItem.getContainerName(), workDetails.deltaTestrig, workDetails.deltaEnv)
                    == null)) {
          throw new BatfishException("Environment metadata not found");
        }
      } catch (Exception e) {
        throw new BatfishException("Testrig/environment metadata not found.");
      }
      success = _workQueueMgr.queueUnassignedWork(new QueuedWork(workItem, workDetails));
    } catch (Exception e) {
      throw new BatfishException("Failed to queue work", e);
    }
    // as an optimization trigger AssignWork to see if we can schedule this (or another) work
    if (success) {
      Thread thread =
          new Thread() {
            @Override
            public void run() {
              assignWork();
            }
          };
      thread.start();
    }
    return success;
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
    Path testrigDir = getdirTestrig(containerName, testrigName);
    Path environmentsDir = testrigDir.resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR);
    Path newEnvDir = environmentsDir.resolve(newEnvName);
    Path dstDir = newEnvDir.resolve(BfConsts.RELPATH_ENV_DIR);
    if (Files.exists(newEnvDir)) {
      throw new BatfishException(
          "Environment: '" + newEnvName + "' already exists for testrig: '" + testrigName + "'");
    }
    if (!dstDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + dstDir + "'");
    }
    Path zipFile = CommonUtil.createTempFile("coord_up_env_", ".zip");
    CommonUtil.writeStreamToFile(fileStream, zipFile);

    /** First copy base environment if it is set */
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
      TestrigMetadataMgr.initializeEnvironment(containerName, testrigName, newEnvName);
    } catch (IOException e) {
      throw new BatfishException("Could not initialize environmentMetadata", e);
    }

    // delete the empty directory and the zip file
    CommonUtil.deleteDirectory(unzipDir);
    CommonUtil.deleteIfExists(zipFile);
  }

  public void uploadQuestion(
      String containerName, String qName, InputStream fileStream, InputStream paramFileStream) {
    Path containerDir = getdirContainer(containerName);
    Path qDir = containerDir.resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, qName));
    if (Files.exists(qDir)) {
      throw new BatfishException(
          "Question: '" + qName + "' already exists in container '" + containerName + "'");
    }
    if (!qDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + qDir + "'");
    }
    Path file = qDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
    CommonUtil.writeStreamToFile(fileStream, file);
  }

  public void uploadTestrig(
      String containerName, String testrigName, InputStream fileStream, boolean autoAnalyze) {
    Path containerDir = getdirContainer(containerName);

    // Fail early if the testrig already exists.
    if (Files.exists(containerDir.resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, testrigName)))) {
      throw new BatfishException("Testrig with name: '" + testrigName + "' already exists");
    }

    // Persist the user's upload to a directory inside the container, named for the testrig,
    // where we save the original upload for later analysis.
    Path originalDir = containerDir.resolve(BfConsts.RELPATH_ORIGINAL_DIR).resolve(testrigName);
    if (!originalDir.toFile().mkdirs()) {
      throw new BatfishException("Failed to create directory: '" + originalDir + "'");
    }
    Path zipFile = originalDir.resolve(BfConsts.RELPATH_TESTRIG_ZIP_FILE);
    CommonUtil.writeStreamToFile(fileStream, zipFile);

    // Now unzip the user's data to a temporary folder, from which we'll parse and copy it.
    Path unzipDir = CommonUtil.createTempDirectory("tr");
    UnzipUtility.unzip(zipFile, unzipDir);

    try {
      initTestrig(containerName, testrigName, unzipDir, autoAnalyze);
    } catch (Exception e) {
      throw new BatfishException("Error initializing testrig", e);
    } finally {
      CommonUtil.deleteDirectory(unzipDir);
    }
  }

  /** Returns true if the container {@code containerName} exists, false otherwise. */
  public boolean checkContainerExists(String containerName) {
    Path containerDir = getdirContainer(containerName, false);
    return Files.exists(containerDir);
  }
}
