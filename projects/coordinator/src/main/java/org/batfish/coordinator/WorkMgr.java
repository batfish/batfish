package org.batfish.coordinator;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UnzipUtility;
import org.batfish.common.util.ZipUtility;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.uri.UriComponent;

public class WorkMgr {

   final class AssignWorkTask implements Runnable {
      @Override
      public void run() {
         Main.getWorkMgr().checkTask();
         Main.getWorkMgr().assignWork();
      }
   }

   final class CheckTaskTask implements Runnable {
      @Override
      public void run() {
         Main.getWorkMgr().checkTask();
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
      _settings = settings;
      _logger = logger;
      _workQueueMgr = new WorkQueueMgr();
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
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error("Got exception in assignWork: " + stackTrace);
      }
   }

   private void assignWork(QueuedWork work, String worker) {

      _logger.info("WM:AssignWork: Trying to assign " + work + " to " + worker
            + " \n");

      boolean assignmentError = false;
      boolean assigned = false;

      try {
         // get the task and add other standard stuff
         JSONObject task = work.getWorkItem().toTask();
         Path containerDir = Main.getSettings().getContainersLocation()
               .resolve(work.getWorkItem().getContainerName());
         String testrigName = work.getWorkItem().getTestrigName();
         Path testrigBaseDir = containerDir.resolve(testrigName)
               .toAbsolutePath();
         task.put(
               BfConsts.ARG_CONTAINER_DIR,
               containerDir.toAbsolutePath().toString());
         task.put(BfConsts.ARG_TESTRIG, testrigName);
         task.put(
               BfConsts.ARG_LOG_FILE,
               testrigBaseDir
                     .resolve(
                           work.getId().toString() + BfConsts.SUFFIX_LOG_FILE)
                     .toString());
         task.put(
               BfConsts.ARG_ANSWER_JSON_PATH,
               testrigBaseDir.resolve(
                     work.getId().toString() + BfConsts.SUFFIX_ANSWER_JSON_FILE)
                     .toString());

         // Client client = ClientBuilder.newClient();
         Client client = CommonUtil
               .createHttpClientBuilder(
                     _settings.getSslWorkDisable(),
                     _settings.getSslWorkTrustAllCerts(),
                     _settings.getSslWorkKeystoreFile(),
                     _settings.getSslWorkKeystorePassword(),
                     _settings.getSslWorkTruststoreFile(),
                     _settings.getSslWorkTruststorePassword())
               .build();
         String protocol = _settings.getSslWorkDisable() ? "http" : "https";
         WebTarget webTarget = client
               .target(String.format("%s://%s%s/%s", protocol, worker,
                     BfConsts.SVC_BASE_RSC, BfConsts.SVC_RUN_TASK_RSC))
               .queryParam(
                     BfConsts.SVC_TASKID_KEY,
                     UriComponent.encode(
                           work.getId().toString(),
                           UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
               .queryParam(
                     BfConsts.SVC_TASK_KEY,
                     UriComponent.encode(
                           task.toString(),
                           UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.error("WM:AssignWork: Got non-OK response "
                  + response.getStatus() + "\n");
         }
         else {
            String sobj = response.readEntity(String.class);
            JSONArray array = new JSONArray(sobj);
            _logger
                  .info(String.format("WM:AssignWork: response: %s [%s] [%s]\n",
                        array.toString(), array.get(0), array.get(1)));

            if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
               _logger.error(String.format("ERROR in assigning task: %s %s\n",
                     array.get(0), array.get(1)));

               assignmentError = true;
            }
            else {
               assigned = true;
            }
         }
      }
      catch (ProcessingException e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error(String.format("Unable to connect to worker at %s: %s\n",
               worker, stackTrace));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error(
               String.format("Exception assigning work: %s\n", stackTrace));
      }

      // mark the assignment results for both work and worker
      if (assignmentError) {
         _workQueueMgr.markAssignmentError(work);
      }
      else if (assigned) {
         _workQueueMgr.markAssignmentSuccess(work, worker);
      }
      else {
         _workQueueMgr.markAssignmentFailure(work);
      }

      Main.getPoolMgr().markAssignmentResult(worker, assigned);
   }

   private void checkTask() {
      try {
         QueuedWork work = _workQueueMgr.getWorkForChecking();
         if (work == null) {
            // _logger.info("WM:checkTask: No assigned work\n");
            return;
         }
         String assignedWorker = work.getAssignedWorker();
         if (assignedWorker == null) {
            _logger.error("WM:CheckWork no assinged worker for " + work + "\n");
            _workQueueMgr.makeWorkUnassigned(work);
            return;
         }
         checkTask(work, assignedWorker);
      }
      catch (Exception e) {
         _logger.error("Got exception in assignWork: " + e.getMessage());
      }
   }

   private void checkTask(QueuedWork work, String worker) {
      _logger.info(
            "WM:CheckWork: Trying to check " + work + " on " + worker + " \n");

      Task task = new Task();
      task.setStatus(TaskStatus.UnreachableOrBadResponse);

      try {
         Client client = ClientBuilder.newClient();
         String protocol = _settings.getSslWorkDisable() ? "http" : "https";
         WebTarget webTarget = client
               .target(String.format("%s://%s%s/%s", protocol, worker,
                     BfConsts.SVC_BASE_RSC, BfConsts.SVC_GET_TASKSTATUS_RSC))
               .queryParam(
                     BfConsts.SVC_TASKID_KEY,
                     UriComponent.encode(
                           work.getId().toString(),
                           UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.error("WM:CheckTask: Got non-OK response "
                  + response.getStatus() + "\n");
         }
         else {
            String sobj = response.readEntity(String.class);
            JSONArray array = new JSONArray(sobj);
            _logger.info(String.format("response: %s [%s] [%s]\n",
                  array.toString(), array.get(0), array.get(1)));

            if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
               _logger.error(
                     String.format("got error while refreshing status: %s %s\n",
                           array.get(0), array.get(1)));
            }
            else {
               String taskStr = array.get(1).toString();
               BatfishObjectMapper mapper = new BatfishObjectMapper();
               task = mapper.readValue(taskStr, Task.class);
               if (task.getStatus() == null) {
                  _logger.error(String
                        .format("did not see status key in json response\n"));
               }
            }
         }
      }
      catch (ProcessingException e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error(String.format("unable to connect to %s: %s\n", worker,
               stackTrace));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error(String.format("exception: %s\n", stackTrace));
      }

      _workQueueMgr.processTaskCheckResult(work, task);

      // if the task ended, send a hint to the pool manager to look up worker
      // status
      if (task.getStatus() == TaskStatus.TerminatedAbnormally
            || task.getStatus() == TaskStatus.TerminatedNormally) {
         Main.getPoolMgr().refreshWorkerStatus(worker);
      }
   }

   /**
    * Create, update, or truncate an analysis with provided questions or and/or
    * question names
    *
    * @param containerName
    *           The container in which the analysis resides
    * @param newAnalysis
    *           Whether or not to create a new analysis. Incompatible with
    *           {@code delQuestionsStr}.
    * @param aName
    *           The name of the analysis
    * @param addQuestionsFileStream
    *           The questions to be added to or initially populate the analysis.
    * @param delQuestionsStr
    *           A string representation of a JSON array of names of questions to
    *           be deleted from the analysis. Incompatible with
    *           {@code newAnalysis}.
    */
   public void configureAnalysis(
         String containerName, boolean newAnalysis,
         String aName, InputStream addQuestionsFileStream,
         String delQuestionsStr) {
      Path containerDir = getdirContainer(containerName);
      Path aDir = containerDir
            .resolve(Paths.get(BfConsts.RELPATH_ANALYSES_DIR, aName));
      if (Files.exists(aDir) && newAnalysis) {
         throw new BatfishException("Analysis '" + aName
               + "' already exists for container '" + containerName);
      }
      if (!Files.exists(aDir)) {
         if (!newAnalysis) {
            throw new BatfishException("Analysis '" + aName
                  + "' does not exist for container '" + containerName + "'");
         }
         if (!aDir.toFile().mkdirs()) {
            throw new BatfishException("Failed to create analysis directory '"
                  + aDir.toString() + "'");
         }
      }
      Path questionsDir = aDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      if (addQuestionsFileStream != null) {
         JSONObject jObject = CommonUtil
               .writeStreamToJSONObject(addQuestionsFileStream);
         Iterator<?> keys = jObject.keys();
         while (keys.hasNext()) {
            String qName = (String) keys.next();
            JSONObject qJson;
            try {
               qJson = jObject.getJSONObject(qName);
            }
            catch (JSONException e) {
               throw new BatfishException(
                     "Provided questions lack a question named '" + qName + "'",
                     e);
            }
            Path qDir = questionsDir.resolve(qName);
            if (Files.exists(qDir)) {
               throw new BatfishException("Question '" + qName
                     + "' already exists for analysis '" + aName + "'");
            }
            if (!qDir.toFile().mkdirs()) {
               throw new BatfishException(
                     "Failed to create question directory '" + qDir.toString()
                           + "'");
            }
            Path qFile = qDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
            String qOutput;
            try {
               qOutput = qJson.toString(1);
            }
            catch (JSONException e) {
               throw new BatfishException(
                     "Failed to convert question JSON to string", e);
            }
            CommonUtil.writeFile(qFile, qOutput);
         }
      }

      /**
       * Delete questions
       */
      if (delQuestionsStr != null && !delQuestionsStr.equals("")) {
         JSONArray delQuestionsArray;
         try {
            delQuestionsArray = new JSONArray(delQuestionsStr);
         }
         catch (JSONException e) {
            throw new BatfishException(
                  "The string of questions to be deleted does not encode a valid JSON array: "
                        + delQuestionsStr,
                  e);
         }
         for (int index = 0; index < delQuestionsArray.length(); index++) {
            String qName;
            try {
               qName = delQuestionsArray.getString(index);
            }
            catch (JSONException e) {
               throw new BatfishException(
                     "Could not get name of question to be deleted at index "
                           + index,
                     e);
            }
            Path qDir = questionsDir.resolve(qName);
            if (!Files.exists(qDir)) {
               throw new BatfishException("Question " + qName
                     + " does not exist for analysis " + aName);
            }
            CommonUtil.deleteDirectory(qDir);
         }
      }
   }

   public void delAnalysis(String containerName, String aName) {
      Path aDir = getdirContainerAnalysis(containerName, aName);
      CommonUtil.deleteDirectory(aDir);
   }

   public void delContainer(String containerName) {
      Path containerDir = getdirContainer(containerName);
      CommonUtil.deleteDirectory(containerDir);
   }

   public void delEnvironment(
         String containerName, String testrigName,
         String envName) {
      Path envDir = getdirEnvironment(containerName, testrigName, envName);
      CommonUtil.deleteDirectory(envDir);
   }

   public void delTestrig(String containerName, String testrigName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      CommonUtil.deleteDirectory(testrigDir);
   }

   public void delTestrigQuestion(
         String containerName, String testrigName,
         String qName) {
      Path qDir = getdirTestrigQuestion(containerName, testrigName, qName);
      CommonUtil.deleteDirectory(qDir);
   }

   public Map<String, String> getAnalysisAnswers(
         String containerName,
         String baseTestrig, String baseEnv, String deltaTestrig,
         String deltaEnv, String analysisName, boolean pretty)
         throws JsonProcessingException {
      Path analysisDir = getdirContainerAnalysis(containerName, analysisName);
      Path testrigDir = getdirTestrig(containerName, baseTestrig);
      SortedSet<String> questions = listAnalysisQuestions(
            containerName,
            analysisName);
      Map<String, String> retMap = new TreeMap<>();
      for (String questionName : questions) {
         String answer = "unknown";
         Path questionFile = analysisDir
               .resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, questionName,
                     BfConsts.RELPATH_QUESTION_FILE));
         if (!Files.exists(questionFile)) {
            throw new BatfishException(
                  "Question file for question " + questionName + "not found");
         }
         String answerFilename = pretty ? BfConsts.RELPATH_ANSWER_PRETTY_JSON
               : BfConsts.RELPATH_ANSWER_JSON;
         Path answerDir = testrigDir
               .resolve(Paths.get(BfConsts.RELPATH_ANALYSES_DIR, analysisName,
                     BfConsts.RELPATH_QUESTIONS_DIR, questionName,
                     BfConsts.RELPATH_ENVIRONMENTS_DIR, baseEnv));
         if (deltaTestrig != null) {
            answerDir = answerDir.resolve(
                  Paths.get(BfConsts.RELPATH_DELTA, deltaTestrig, deltaEnv));
         }
         Path answerFile = answerDir.resolve(answerFilename);
         if (!Files.exists(answerFile)) {
            if (pretty) {
               answer = "Not answered";
            }
            else {
               Answer ans = Answer.failureAnswer("Not answered", null);
               ans.setStatus(AnswerStatus.NOTFOUND);

               BatfishObjectMapper mapper = new BatfishObjectMapper();
               answer = mapper.writeValueAsString(ans);
            }
         }
         else {
            boolean answerIsStale;
            answerIsStale = CommonUtil.getLastModifiedTime(questionFile)
                  .compareTo(CommonUtil.getLastModifiedTime(answerFile)) > 0;
            if (answerIsStale) {
               if (pretty) {
                  answer = "Not fresh";
               }
               else {
                  Answer ans = Answer.failureAnswer("Not answered", null);
                  ans.setStatus(AnswerStatus.STALE);

                  BatfishObjectMapper mapper = new BatfishObjectMapper();
                  answer = mapper.writeValueAsString(ans);
               }
            }
            else {
               answer = CommonUtil.readFile(answerFile);
            }
         }

         retMap.put(questionName, answer);
      }
      return retMap;
   }

   public String getAnalysisQuestion(
         String containerName, String analysisName,
         String questionName) {
      Path questionDir = getdirAnalysisQuestion(containerName, analysisName,
            questionName);
      Path qFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      if (!Files.exists(qFile)) {
         throw new BatfishException(
               "Question file not found for " + questionName);
      }
      return CommonUtil.readFile(qFile);
   }

   public String getAnswer(
         String containerName, String baseTestrig,
         String baseEnv, String deltaTestrig, String deltaEnv,
         String questionName, boolean pretty) throws JsonProcessingException {
      Path questionDir = getdirTestrigQuestion(containerName, baseTestrig,
            questionName);
      Path questionFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      if (!Files.exists(questionFile)) {
         throw new BatfishException(
               "Question file not found for " + questionName);
      }
      Path answerDir = questionDir
            .resolve(Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, baseEnv));
      if (deltaTestrig != null) {
         answerDir = answerDir.resolve(
               Paths.get(BfConsts.RELPATH_DELTA, deltaTestrig, deltaEnv));
      }
      String answerFilename = pretty ? BfConsts.RELPATH_ANSWER_PRETTY_JSON
            : BfConsts.RELPATH_ANSWER_JSON;
      Path answerFile = answerDir.resolve(answerFilename);
      String answer = "unknown";
      if (!Files.exists(answerFile)) {
         Answer ans = Answer.failureAnswer("Not answered", null);
         ans.setStatus(AnswerStatus.NOTFOUND);
         if (pretty) {
            ans = ans.prettyPrintAnswer();
         }
         BatfishObjectMapper mapper = new BatfishObjectMapper();
         answer = mapper.writeValueAsString(ans);
      }
      else {
         if (CommonUtil.getLastModifiedTime(questionFile)
               .compareTo(CommonUtil.getLastModifiedTime(answerFile)) > 0) {
            Answer ans = Answer.failureAnswer("Not fresh", null);
            ans.setStatus(AnswerStatus.STALE);
            if (pretty) {
               ans = ans.prettyPrintAnswer();
            }
            BatfishObjectMapper mapper = new BatfishObjectMapper();
            answer = mapper.writeValueAsString(ans);
         }
         else {
            answer = CommonUtil.readFile(answerFile);
         }
      }
      return answer;
   }

   private Path getdirAnalysisQuestion(
         String containerName,
         String analysisName, String qName) {
      Path analysisDir = getdirContainerAnalysis(containerName, analysisName);
      Path qDir = analysisDir
            .resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, qName));
      if (!Files.exists(qDir)) {
         throw new BatfishException("Question '" + qName + "' does not exist");
      }
      return qDir;
   }

   private Path getdirContainer(String containerName) {
      Path containerDir = Main.getSettings().getContainersLocation()
            .resolve(containerName).toAbsolutePath();
      if (!Files.exists(containerDir)) {
         throw new BatfishException(
               "Container '" + containerName + "' does not exist");
      }
      return containerDir;
   }

   private Path getdirContainerAnalysis(
         String containerName,
         String analysisName) {
      Path containerDir = getdirContainer(containerName);
      Path aDir = containerDir
            .resolve(Paths.get(BfConsts.RELPATH_ANALYSES_DIR, analysisName));
      if (!Files.exists(aDir)) {
         throw new BatfishException("Analysis '" + analysisName
               + "' does not exists for container '" + containerName + "'");
      }
      return aDir;
   }

   private Path getdirEnvironment(
         String containerName, String testrigName,
         String envName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path envDir = testrigDir
            .resolve(Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName));
      if (!Files.exists(envDir)) {
         throw new BatfishException(
               "Environment '" + envName + "' does not exist");
      }
      return envDir;
   }

   private Path getdirTestrig(String containerName, String testrigName) {
      Path containerDir = getdirContainer(containerName);
      Path testrigDir = containerDir
            .resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, testrigName));
      if (!Files.exists(testrigDir)) {
         throw new BatfishException(
               "Testrig '" + testrigName + "' does not exist");
      }
      return testrigDir;
   }

   private Path getdirTestrigQuestion(
         String containerName, String testrigName,
         String qName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path qDir = testrigDir
            .resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, qName));
      if (!Files.exists(qDir)) {
         throw new BatfishException("Question '" + qName + "' does not exist");
      }
      return qDir;
   }

   public JSONObject getStatusJson() throws JSONException {
      return _workQueueMgr.getStatusJson();
   }

   public String getTestrigInfo(String containerName, String testrigName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path submittedTestrigDir = testrigDir
            .resolve(BfConsts.RELPATH_TEST_RIG_DIR);
      if (!Files.exists(submittedTestrigDir)) {
         return "Missing folder '" + BfConsts.RELPATH_TEST_RIG_DIR
               + "' for testrig '" + testrigName + "'\n";
      }
      StringBuilder retStringBuilder = new StringBuilder();
      SortedSet<Path> entries = CommonUtil.getEntries(submittedTestrigDir);
      for (Path entry : entries) {
         retStringBuilder.append(entry.getFileName().toString());
         if (Files.isDirectory(entry)) {
            String[] subdirEntryNames = CommonUtil.getEntries(entry).stream()
                  .map(subdirEntry -> subdirEntry.getFileName().toString())
                  .collect(Collectors.toList()).toArray(new String[]{});
            retStringBuilder.append("/\n");
            // now append a maximum of MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES
            for (int index = 0; index < subdirEntryNames.length
                  && index < MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES; index++) {
               retStringBuilder.append("  " + subdirEntryNames[index] + "\n");
            }
            if (subdirEntryNames.length > 10) {
               retStringBuilder.append("  ...... "
                     + (subdirEntryNames.length
                     - MAX_SHOWN_TESTRIG_INFO_SUBDIR_ENTRIES)
                     + " more entries\n");
            }
         }
         else {
            retStringBuilder.append("\n");
         }
      }
      return retStringBuilder.toString();
   }

   public Path getTestrigObject(
         String containerName, String testrigName,
         String objectName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path file = testrigDir.resolve(objectName);
      /**
       * Check if we got an object name outside of the testrig folder, perhaps
       * because of ".." in the name; disallow it
       */
      if (!CommonUtil.getCanonicalPath(file)
            .startsWith(CommonUtil.getCanonicalPath(testrigDir))) {
         throw new BatfishException(
               "Illegal object name: '" + objectName + "'");
      }
      if (Files.isRegularFile(file)) {
         return file;
      }
      else if (Files.isDirectory(file)) {
         Path zipfile = Paths.get(file.toString() + ".zip");
         if (Files.exists(zipfile)) {
            CommonUtil.deleteIfExists(zipfile);
         }
         ZipUtility.zipFiles(file, zipfile);

         // TODO: delete the zipfile

         return zipfile;
      }

      return null;
   }

   public String getTestrigQuestion(
           String containerName, String testrigName,
           String questionName) {
      Path questionDir = getdirTestrigQuestion(containerName, testrigName,
              questionName);
      Path qFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      if (!Files.exists(qFile)) {
         throw new BatfishException(
                 "Question file not found for " + questionName);
      }
      return CommonUtil.readFile(qFile);
   }

   public QueuedWork getWork(UUID workItemId) {
      return _workQueueMgr.getWork(workItemId);
   }

   public String initContainer(String containerName, String containerPrefix) {
      if (containerName == null || containerName.equals("")) {
         containerName = containerPrefix + "_" + UUID.randomUUID();
      }
      Path containerDir = Main.getSettings().getContainersLocation()
            .resolve(containerName);
      if (Files.exists(containerDir)) {
         throw new BatfishException(
               "Container '" + containerName + "' already exists!");
      }
      if (!containerDir.toFile().mkdirs()) {
         throw new BatfishException(
               "failed to create directory '" + containerDir.toString() + "'");
      }
      return containerName;
   }

   private boolean isEnvFile(Path path) {
      String name = path.getFileName().toString();
      return ENV_FILENAMES.contains(name);
   }

   public SortedSet<String> listAnalyses(String containerName) {
      Path containerDir = getdirContainer(containerName);
      Path analysesDir = containerDir.resolve(BfConsts.RELPATH_ANALYSES_DIR);
      if (!Files.exists(analysesDir)) {
         return new TreeSet<>();
      }
      SortedSet<String> analyses = new TreeSet<>(
            CommonUtil.getSubdirectories(analysesDir).stream()
                  .map(subdir -> subdir.getFileName().toString())
                  .collect(Collectors.toSet()));
      return analyses;
   }

   public SortedSet<String> listAnalysisQuestions(
         String containerName,
         String analysisName) {
      Path analysisDir = getdirContainerAnalysis(containerName, analysisName);
      Path questionsDir = analysisDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      if (!Files.exists(questionsDir)) {
         /**
          * TODO: Something better than returning empty set?
          */
         return new TreeSet<>();
      }
      SortedSet<Path> subdirectories = CommonUtil
            .getSubdirectories(questionsDir);
      SortedSet<String> subdirectoryNames = new TreeSet<>(
            subdirectories.stream().map(path -> path.getFileName().toString())
                  .collect(Collectors.toSet()));
      return subdirectoryNames;
   }

   public SortedSet<String> listContainers(String apiKey) {
      Path containersDir = Main.getSettings().getContainersLocation();
      if (!Files.exists(containersDir)) {
         containersDir.toFile().mkdirs();
      }
      SortedSet<String> authorizedContainers = new TreeSet<>(
            CommonUtil.getSubdirectories(containersDir).stream()
                  .map(dir -> dir.getFileName().toString())
                  .filter(container -> Main.getAuthorizer()
                        .isAccessibleContainer(apiKey, container, false))
                  .collect(Collectors.toSet()));
      return authorizedContainers;
   }

   public SortedSet<String> listEnvironments(
         String containerName,
         String testrigName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path environmentsDir = testrigDir
            .resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR);
      if (!Files.exists(environmentsDir)) {
         return new TreeSet<>();
      }
      SortedSet<String> environments = new TreeSet<>(
            CommonUtil.getSubdirectories(environmentsDir).stream()
                  .map(dir -> dir.getFileName().toString())
                  .collect(Collectors.toSet()));
      return environments;
   }

   public SortedSet<String> listQuestions(
         String containerName,
         String testrigName) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path questionsDir = testrigDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      if (!Files.exists(questionsDir)) {
         return new TreeSet<>();
      }
      SortedSet<String> questions = new TreeSet<>(
            CommonUtil.getSubdirectories(questionsDir).stream()
                  .map(dir -> dir.getFileName().toString())
                  .collect(Collectors.toSet()));
      return questions;
   }

   public SortedSet<String> listTestrigs(String containerName) {
      Path containerDir = getdirContainer(containerName);
      Path testrigsDir = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR);
      if (!Files.exists(testrigsDir)) {
         return new TreeSet<>();
      }
      SortedSet<String> testrigs = new TreeSet<>(
            CommonUtil.getSubdirectories(testrigsDir).stream()
                  .map(dir -> dir.getFileName().toString())
                  .collect(Collectors.toSet()));
      return testrigs;
   }

   public void putObject(
         String containerName, String testrigName,
         String objectName, InputStream fileStream) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path file = testrigDir.resolve(objectName);
      // check if we got an object name outside of the testrig folder,
      // perhaps because of ".." in the name; disallow it
      if (!CommonUtil.getCanonicalPath(file)
            .startsWith(CommonUtil.getCanonicalPath(testrigDir))) {
         throw new BatfishException(
               "Illegal object name: '" + objectName + "'");
      }
      Path parentFolder = file.getParent();
      if (!Files.exists(parentFolder)) {
         if (!parentFolder.toFile().mkdirs()) {
            throw new BatfishException("Failed to create directory: '"
                  + parentFolder.toString() + "'");
         }
      }
      else {
         if (!Files.isDirectory(parentFolder)) {
            throw new BatfishException(parentFolder.toString()
                  + " already exists but is not a folder");
         }
      }
      CommonUtil.writeStreamToFile(fileStream, file);
   }

   public boolean queueWork(WorkItem workItem) {
      Path testrigDir = Main.getSettings().getContainersLocation().resolve(
            Paths.get(workItem.getContainerName(), workItem.getTestrigName()));
      if (workItem.getTestrigName().isEmpty() || !Files.exists(testrigDir)) {
         throw new BatfishException("Non-existent testrig: '"
               + testrigDir.getFileName().toString() + "'");
      }
      boolean success;
      try {
         success = _workQueueMgr.queueUnassignedWork(new QueuedWork(workItem));
      }
      catch (Exception e) {
         throw new BatfishException("Failed to queue work", e);
      }
      // as an optimization trigger AssignWork to see if we can schedule this
      // (or another) work
      if (success) {
         Thread thread = new Thread() {
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

      Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
            new AssignWorkTask(), 0, Main.getSettings().getPeriodAssignWorkMs(),
            TimeUnit.MILLISECONDS);

   }

   /**
    * Upload a new environment to an existing testrig.
    *
    * @param containerName
    *           The container in which the testrig resides
    * @param testrigName
    *           The testrig in which the (optional base environment and) new
    *           environment reside
    * @param baseEnvName
    *           The name of an optional base environment. The new environment is
    *           initialized with files from this base if it is provided.
    * @param newEnvName
    *           The name of the new environment to be created
    * @param fileStream
    *           A stream providing the zip file containing the file structure of
    *           the new environment.
    */
   public void uploadEnvironment(
         String containerName, String testrigName,
         String baseEnvName, String newEnvName, InputStream fileStream) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path environmentsDir = testrigDir
            .resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR);
      Path newEnvDir = environmentsDir.resolve(newEnvName);
      Path dstDir = newEnvDir.resolve(BfConsts.RELPATH_ENV_DIR);
      if (Files.exists(newEnvDir)) {
         throw new BatfishException("Environment: '" + newEnvName
               + "' already exists for testrig: '" + testrigName + "'");
      }
      if (!dstDir.toFile().mkdirs()) {
         throw new BatfishException(
               "Failed to create directory: '" + dstDir.toString() + "'");
      }
      Path zipFile = CommonUtil.createTempFile("coord_up_env_", ".zip");
      CommonUtil.writeStreamToFile(fileStream, zipFile);

      /**
       * First copy base environment if it is set
       */
      if (baseEnvName.length() > 0) {
         Path baseEnvPath = environmentsDir
               .resolve(Paths.get(baseEnvName, BfConsts.RELPATH_ENV_DIR));
         if (!Files.exists(baseEnvPath)) {
            CommonUtil.delete(zipFile);
            throw new BatfishException(
                  "Base environment for copy does not exist: '" + baseEnvName
                        + "'");
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
      if (unzipDirEntries.size() != 1
            || !Files.isDirectory(unzipDirEntries.iterator().next())) {
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

      // delete the empty directory and the zip file
      CommonUtil.deleteDirectory(unzipDir);
      CommonUtil.deleteIfExists(zipFile);
   }

   public void uploadQuestion(
         String containerName, String testrigName,
         String qName, InputStream fileStream, InputStream paramFileStream) {
      Path testrigDir = getdirTestrig(containerName, testrigName);
      Path qDir = testrigDir
            .resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, qName));
      if (Files.exists(qDir)) {
         throw new BatfishException("Question: '" + qName
               + "' already exists for testrig '" + testrigName + "'");
      }
      if (!qDir.toFile().mkdirs()) {
         throw new BatfishException(
               "Failed to create directory: '" + qDir.toString() + "'");
      }
      Path file = qDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      CommonUtil.writeStreamToFile(fileStream, file);
   }

   public void uploadTestrig(
         String containerName, String testrigName,
         InputStream fileStream) {
      Path containerDir = getdirContainer(containerName);
      Path testrigDir = containerDir
            .resolve(Paths.get(BfConsts.RELPATH_TESTRIGS_DIR, testrigName));
      if (Files.exists(testrigDir)) {
         throw new BatfishException(
               "Testrig with name: '" + testrigName + "' already exists");
      }
      if (!testrigDir.toFile().mkdirs()) {
         throw new BatfishException(
               "Failed to create directory: '" + testrigDir.toString() + "'");
      }
      Path zipFile = CommonUtil.createTempFile("testrig", ".zip");
      CommonUtil.writeStreamToFile(fileStream, zipFile);
      Path unzipDir = testrigDir.resolve(BfConsts.RELPATH_TEST_RIG_DIR);
      UnzipUtility.unzip(zipFile, unzipDir);

      /*-
       * Sanity check what we got:
       *    There should be just one top-level folder.
       */
      SortedSet<Path> unzipDirEntries = CommonUtil.getEntries(unzipDir);
      if (unzipDirEntries.size() != 1
            || !Files.isDirectory(unzipDirEntries.iterator().next())) {
         CommonUtil.deleteDirectory(testrigDir);
         throw new BatfishException(
               "Unexpected packaging of environment. There should be just one top-level folder");
      }
      Path unzipSubdir = unzipDirEntries.iterator().next();
      SortedSet<Path> subFileList = CommonUtil.getEntries(unzipSubdir);

      // create empty default environment
      Path defaultEnvironmentLeafDir = testrigDir
            .resolve(Paths.get(
                  BfConsts.RELPATH_ENVIRONMENTS_DIR,
                  BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
                  BfConsts.RELPATH_ENV_DIR));
      defaultEnvironmentLeafDir.toFile().mkdirs();

      // things look ok, now make the move
      for (Path subFile : subFileList) {
         Path target;
         if (isEnvFile(subFile)) {
            target = defaultEnvironmentLeafDir.resolve(subFile.getFileName());
         }
         else {
            target = unzipDir.resolve(subFile.getFileName());
         }
         CommonUtil.moveByCopy(subFile, target);
      }

      // delete the empty directory and the zip file
      CommonUtil.deleteDirectory(unzipSubdir);
      CommonUtil.delete(zipFile);
   }

}
