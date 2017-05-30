package org.batfish.coordinator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.Task;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UnzipUtility;
import org.batfish.common.util.ZipUtility;
import org.batfish.common.WorkItem;
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

   // private Runnable _checkWorkTask;
   // private Runnable _assignWorkTask;
   //
   // private ScheduledExecutorService _checkService;
   // private ScheduledExecutorService _assignService;
   //
   // private ScheduledFuture<?> _checkFuture;
   // private ScheduledFuture<?> _assignFuture;

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

   private BatfishLogger _logger;

   private WorkQueueMgr _workQueueMgr;

   public WorkMgr(BatfishLogger logger) {
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
         Path containerDir = Paths.get(
               Main.getSettings().getContainersLocation(),
               work.getWorkItem().getContainerName());
         String testrigName = work.getWorkItem().getTestrigName();
         Path testrigBaseDir = containerDir.resolve(testrigName)
               .toAbsolutePath();
         task.put(BfConsts.ARG_CONTAINER_DIR,
               containerDir.toAbsolutePath().toString());
         task.put(BfConsts.ARG_TESTRIG, testrigName);
         task.put(BfConsts.ARG_LOG_FILE,
               testrigBaseDir
                     .resolve(
                           work.getId().toString() + BfConsts.SUFFIX_LOG_FILE)
                     .toString());
         task.put(BfConsts.ARG_ANSWER_JSON_PATH,
               testrigBaseDir.resolve(
                     work.getId().toString() + BfConsts.SUFFIX_ANSWER_JSON_FILE)
                     .toString());

         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client
               .target(String.format("http://%s%s/%s", worker,
                     BfConsts.SVC_BASE_RSC, BfConsts.SVC_RUN_TASK_RSC))
               .queryParam(BfConsts.SVC_TASKID_KEY,
                     UriComponent.encode(work.getId().toString(),
                           UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
               .queryParam(BfConsts.SVC_TASK_KEY,
                     UriComponent.encode(task.toString(),
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
         _logger.error(String.format("unable to connect to %s: %s\n", worker,
               stackTrace));
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         _logger.error(String.format("exception: %s\n", stackTrace));
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
         WebTarget webTarget = client
               .target(String.format("http://%s%s/%s", worker,
                     BfConsts.SVC_BASE_RSC, BfConsts.SVC_GET_TASKSTATUS_RSC))
               .queryParam(BfConsts.SVC_TASKID_KEY,
                     UriComponent.encode(work.getId().toString(),
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

   public void configureAnalysis(String containerName, boolean newAnalysis,
         String aName, InputStream addQuestionsFileStream,
         String delQuestionsStr) throws Exception {

      File containerDir = getdirContainer(containerName);

      File aDir = Paths.get(containerDir.getAbsolutePath(),
            BfConsts.RELPATH_ANALYSES_DIR, aName).toFile();

      if (aDir.exists() && newAnalysis) {
         throw new FileExistsException("Analysis " + aName
               + " already exists for container " + containerName);
      }

      if (!aDir.exists()) {
         if (!newAnalysis) {
            throw new FileExistsException("Analysis " + aName
                  + " does not exists for container " + containerName);
         }
         if (!aDir.mkdirs()) {
            throw new Exception("failed to create analyses directory "
                  + aDir.getAbsolutePath());
         }
      }

      File questionsDir = Paths
            .get(aDir.getAbsolutePath(), BfConsts.RELPATH_QUESTIONS_DIR)
            .toFile();

      if (addQuestionsFileStream != null) {
         ByteArrayOutputStream questions = new ByteArrayOutputStream();

         int read = 0;
         final byte[] buffer = new byte[1024];
         while ((read = addQuestionsFileStream.read(buffer)) != -1) {
            questions.write(buffer, 0, read);
         }

         JSONObject jObject = new JSONObject(questions.toString("UTF-8"));

         Iterator<?> keys = jObject.keys();

         while (keys.hasNext()) {
            String qName = (String) keys.next();
            JSONObject qJson = jObject.getJSONObject(qName);

            File qDir = Paths.get(questionsDir.getAbsolutePath(), qName)
                  .toFile();

            if (qDir.exists()) {
               throw new FileExistsException("Question " + qName
                     + " already exists for analysis " + aName);
            }

            if (!qDir.mkdirs()) {
               throw new Exception("failed to create question directory "
                     + qDir.getAbsolutePath());
            }

            File qFile = Paths
                  .get(qDir.getAbsolutePath(), BfConsts.RELPATH_QUESTION_FILE)
                  .toFile();

            FileUtils.writeStringToFile(qFile, qJson.toString(1));
         }
      }

      if (delQuestionsStr != null && !delQuestionsStr.equals("")) {
         JSONArray delQuestionsArray = new JSONArray(delQuestionsStr);

         for (int index = 0; index < delQuestionsArray.length(); index++) {
            String qName = delQuestionsArray.getString(index);

            File qDir = Paths.get(questionsDir.getAbsolutePath(), qName)
                  .toFile();

            if (!qDir.exists()) {
               throw new FileExistsException("Question " + qName
                     + " does not exist for analysis " + aName);
            }

            FileUtils.deleteDirectory(qDir);
         }
      }
   }

   public void delAnalysis(String containerName, String aName)
         throws Exception {
      File aDir = getdirContainerAnalysis(containerName, aName);
      FileUtils.deleteDirectory(aDir);
   }

   public void delContainer(String containerName) throws Exception {
      File containerDir = getdirContainer(containerName);
      FileUtils.deleteDirectory(containerDir);
   }

   public void delEnvironment(String containerName, String testrigName,
         String envName) throws Exception {
      File envDir = getdirEnvironment(containerName, testrigName, envName);
      FileUtils.deleteDirectory(envDir);
   }

   public void delTestrig(String containerName, String testrigName)
         throws Exception {
      File testrigDir = getdirTestrig(containerName, testrigName);
      FileUtils.deleteDirectory(testrigDir);
   }

   public void delTestrigQuestion(String containerName, String testrigName,
         String qName) throws Exception {
      File qDir = getdirTestrigQuestion(containerName, testrigName, qName);
      FileUtils.deleteDirectory(qDir);
   }

   // public String getAnswer(String containerName, String testrigName,
   // String analysisName, String questionName) throws FileNotFoundException {
   //
   // File questionDir = getExistingTestrigQuestionDir(containerName,
   // analysisName, questionName);
   //
   // File questionFile = Paths.get(questionDir.getAbsolutePath(),
   // BfConsts.RELPATH_QUESTION_FILE).toFile();
   // if (!questionFile.exists()) {
   // throw new FileNotFoundException(
   // "Question file not found for " + questionName);
   // }
   //
   // File answerDir = getExistingAnalysisAnswerDir(containerName, testrigName,
   // analysisName, questionName);
   //
   // File answerFile = Paths.get(answerDir.getAbsolutePath(),
   // BfConsts.RELPATH_ANSWER_JSON).toFile();
   // if (!answerFile.exists()) {
   // return null;
   // }
   //
   // if (answerFile.lastModified() < questionFile.lastModified()) {
   // throw new FileNotFoundException("The answer file is stale");
   // }
   //
   // return CommonUtil.readFile(answerFile.toPath());
   // }

   public Map<String, String> getAnalysisAnswers(String containerName,
         String baseTestrig, String baseEnv, String deltaTestrig,
         String deltaEnv, String analysisName, boolean pretty)
         throws FileNotFoundException {

      File analysisDir = getdirContainerAnalysis(containerName, analysisName);
      File testrigDir = getdirTestrig(containerName, baseTestrig);
      String[] questions = listAnalysisQuestions(containerName, analysisName);

      Map<String, String> retMap = new TreeMap<>();

      for (String questionName : questions) {

         String answer = "unknown";

         File questionFile = Paths.get(analysisDir.getAbsolutePath(),
               BfConsts.RELPATH_QUESTIONS_DIR, questionName,
               BfConsts.RELPATH_QUESTION_FILE).toFile();
         if (!questionFile.exists()) {
            throw new FileNotFoundException(
                  "Question file for question " + questionName + "not found");
         }

         String answerFilename = pretty ? BfConsts.RELPATH_ANSWER_PRETTY_JSON
               : BfConsts.RELPATH_ANSWER_JSON;

         Path answerDir = Paths.get(testrigDir.getAbsolutePath(),
               BfConsts.RELPATH_ANALYSES_DIR, analysisName,
               BfConsts.RELPATH_QUESTIONS_DIR, questionName,
               BfConsts.RELPATH_ENVIRONMENTS_DIR, baseEnv);
         if (deltaTestrig != null) {
            answerDir = answerDir.resolve(
                  Paths.get(BfConsts.RELPATH_DELTA, deltaTestrig, deltaEnv)
                        .toString());
         }

         File answerFile = answerDir.resolve(answerFilename).toFile();

         if (!answerFile.exists()) {
            answer = "Not answered";
         }
         else {
            if (questionFile.lastModified() > answerFile.lastModified()) {
               answer = "Not fresh";
            }
            else {
               answer = CommonUtil.readFile(answerFile.toPath());
            }
         }

         retMap.put(questionName, answer);
      }

      return retMap;
   }

   public String getAnalysisQuestion(String containerName, String analysisName,
         String questionName) throws Exception {

      File questionDir = getdirAnalysisQuestion(containerName, analysisName,
            questionName);

      Path qFile = Paths.get(questionDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTION_FILE);
      if (!qFile.toFile().exists()) {
         throw new FileExistsException(
               "Question file not found for " + questionName);
      }

      return CommonUtil.readFile(qFile);
   }

   public String getAnswer(String containerName, String baseTestrig,
         String baseEnv, String deltaTestrig, String deltaEnv,
         String questionName, boolean pretty) throws FileNotFoundException {

      File questionDir = getdirTestrigQuestion(containerName, baseTestrig,
            questionName);

      File questionFile = Paths
            .get(questionDir.getAbsolutePath(), BfConsts.RELPATH_QUESTION_FILE)
            .toFile();
      if (!questionFile.exists()) {
         throw new FileNotFoundException(
               "Question file not found for " + questionName);
      }

      Path answerDir = Paths.get(questionDir.getAbsolutePath(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR, baseEnv);
      if (deltaTestrig != null) {
         answerDir = answerDir.resolve(Paths.get(answerDir.toString(),
               BfConsts.RELPATH_DELTA, deltaTestrig, deltaEnv));
      }

      String answerFilename = pretty ? BfConsts.RELPATH_ANSWER_PRETTY_JSON
            : BfConsts.RELPATH_ANSWER_JSON;

      File answerFile = answerDir.resolve(answerFilename).toFile();

      String answer = "unknown";

      if (!answerFile.exists()) {
         answer = "Not answered";
      }
      else {
         if (questionFile.lastModified() > answerFile.lastModified()) {
            answer = "Not fresh";
         }
         else {
            answer = CommonUtil.readFile(answerFile.toPath());
         }
      }

      return answer;
   }

   private File getdirAnalysisQuestion(String containerName,
         String analysisName, String qName) throws FileNotFoundException {
      File analysisDir = getdirContainerAnalysis(containerName, analysisName);

      File qDir = Paths.get(analysisDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTIONS_DIR, qName).toFile();
      if (!qDir.exists()) {
         throw new FileNotFoundException(
               "Question " + qName + " does not exist");
      }

      return qDir;
   }

   private File getdirContainer(String containerName)
         throws FileNotFoundException {
      File containerDir = Paths
            .get(Main.getSettings().getContainersLocation(), containerName)
            .toFile();
      if (!containerDir.exists()) {
         throw new FileNotFoundException(
               "Container " + containerName + " does not exist");
      }

      return containerDir;
   }

   private File getdirContainerAnalysis(String containerName,
         String analysisName) throws FileNotFoundException {

      File containerDir = getdirContainer(containerName);

      File aDir = Paths.get(containerDir.getAbsolutePath(),
            BfConsts.RELPATH_ANALYSES_DIR, analysisName).toFile();
      if (!aDir.exists()) {
         throw new FileNotFoundException("Analysis " + analysisName
               + " does not exists for container " + containerName);
      }

      return aDir;
   }

   private File getdirEnvironment(String containerName, String testrigName,
         String envName) throws FileNotFoundException {

      File testrigDir = getdirTestrig(containerName, testrigName);

      File envDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR, envName).toFile();
      if (!envDir.exists()) {
         throw new FileNotFoundException(
               "Environment " + envName + " does not exist");
      }

      return envDir;
   }

   private File getdirTestrig(String containerName, String testrigName)
         throws FileNotFoundException {
      File containerDir = getdirContainer(containerName);

      File testrigDir = Paths.get(containerDir.getAbsolutePath(),
            BfConsts.RELPATH_TESTRIGS_DIR, testrigName).toFile();
      if (!testrigDir.exists()) {
         throw new FileNotFoundException(
               "testrig " + testrigName + " does not exist");
      }

      return testrigDir;
   }

   private File getdirTestrigQuestion(String containerName, String testrigName,
         String qName) throws FileNotFoundException {
      File testrigDir = getdirTestrig(containerName, testrigName);

      File qDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTIONS_DIR, qName).toFile();
      if (!qDir.exists()) {
         throw new FileNotFoundException(
               "Question " + qName + " does not exist");
      }

      return qDir;
   }

   public JSONObject getStatusJson() throws JSONException {
      return _workQueueMgr.getStatusJson();
   }

   public String getTestrigInfo(String containerName, String testrigName)
         throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);

      File submittedTestrigDir = Paths
            .get(testrigDir.getAbsolutePath(), BfConsts.RELPATH_TEST_RIG_DIR)
            .toFile();

      if (!submittedTestrigDir.exists()) {
         return "Missing folder " + BfConsts.RELPATH_TEST_RIG_DIR
               + " for testrig " + testrigName + "\n";
      }

      StringBuilder retStringBuilder = new StringBuilder();

      File[] subFiles = submittedTestrigDir.listFiles();
      Arrays.sort(subFiles);

      for (File subFile : subFiles) {
         retStringBuilder.append(subFile.getName());
         if (subFile.isDirectory()) {
            File[] subSubFiles = subFile.listFiles();
            Arrays.sort(subSubFiles);

            retStringBuilder.append("/\n");

            // now append a maximum of 10
            for (int index = 0; index < subSubFiles.length
                  && index < 10; index++) {
               retStringBuilder
                     .append("  " + subSubFiles[index].getName() + "\n");
            }

            if (subSubFiles.length > 10) {
               retStringBuilder.append("  ...... " + (subSubFiles.length - 10)
                     + " more entries\n");
            }
         }
         else {
            retStringBuilder.append("\n");
         }
      }

      return retStringBuilder.toString();
   }

   public File getTestrigObject(String containerName, String testrigName,
         String objectName) throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);
      File file = Paths.get(testrigDir.getAbsolutePath(), objectName).toFile();

      // check if we got an object name outside of the testrig folder,
      // perhaps because of ".." in the name; disallow it
      if (!file.getCanonicalPath().contains(testrigDir.getCanonicalPath())) {
         throw new AccessControlException("Illegal object name: " + objectName);
      }

      if (file.isFile()) {
         return file;
      }
      else if (file.isDirectory()) {
         File zipfile = new File(file.getAbsolutePath() + ".zip");

         if (zipfile.exists()) {
            zipfile.delete();
         }

         // AppZip appZip = new AppZip();
         // appZip.zip();
         ZipUtility.zipFiles(file.getAbsolutePath(), zipfile.getAbsolutePath());

         // TODO: delete the zipfile

         return zipfile;
      }

      return null;
   }

   public QueuedWork getWork(UUID workItemId) {
      return _workQueueMgr.getWork(workItemId);
   }

   public String initContainer(String containerPrefix) throws Exception {

      String containerName = containerPrefix + "_" + UUID.randomUUID();

      File containerDir = Paths
            .get(Main.getSettings().getContainersLocation(), containerName)
            .toFile();

      if (containerDir.exists()) {
         throw new FileExistsException(
               "Container " + containerName + " already exists!");
      }

      if (!containerDir.mkdirs()) {
         throw new Exception(
               "failed to create directory " + containerDir.getAbsolutePath());
      }

      return containerName;
   }

   private boolean isEnvFile(File file) {
      String name = file.getName();
      return ENV_FILENAMES.contains(name);
   }

   public String[] listAnalyses(String containerName) throws Exception {

      File containerDir = getdirContainer(containerName);

      File analysesDir = Paths
            .get(containerDir.getAbsolutePath(), BfConsts.RELPATH_ANALYSES_DIR)
            .toFile();
      if (!analysesDir.exists()) {
         return new String[0];
      }

      String[] directories = analysesDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
         }
      });

      return directories;
   }

   public String[] listAnalysisQuestions(String containerName,
         String analysisName) throws FileNotFoundException {

      File analysisDir = getdirContainerAnalysis(containerName, analysisName);
      File questionsDir = Paths
            .get(analysisDir.getAbsolutePath(), BfConsts.RELPATH_QUESTIONS_DIR)
            .toFile();
      if (!questionsDir.exists()) {
         return new String[0];
      }

      String[] directories = questionsDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
         }
      });

      return directories;
   }

   public String[] listContainers(String apiKey) throws Exception {

      File containersDir = new File(Main.getSettings().getContainersLocation());

      if (!containersDir.exists()) {
         containersDir.mkdirs();
      }

      List<String> containers = new ArrayList<>();

      File[] files = containersDir.listFiles();
      Arrays.sort(files);

      for (File file : files) {
         if (file.isDirectory() && Main.getAuthorizer()
               .isAccessibleContainer(apiKey, file.getName(), false)) {
            containers.add(file.getName());
         }
      }

      return containers.toArray(new String[containers.size()]);
   }

   public String[] listEnvironments(String containerName, String testrigName)
         throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);
      File environmentsDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR).toFile();

      if (!environmentsDir.exists()) {
         return new String[0];
      }

      String[] directories = environmentsDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
         }
      });

      return directories;
   }

   public String[] listQuestions(String containerName, String testrigName)
         throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);
      File questionsDir = Paths
            .get(testrigDir.getAbsolutePath(), BfConsts.RELPATH_QUESTIONS_DIR)
            .toFile();

      if (!questionsDir.exists()) {
         return new String[0];
      }

      String[] directories = questionsDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
         }
      });

      return directories;
   }

   public String[] listTestrigs(String containerName) throws Exception {

      File containerDir = getdirContainer(containerName);
      File testrigsDir = Paths
            .get(containerDir.getAbsolutePath(), BfConsts.RELPATH_TESTRIGS_DIR)
            .toFile();
      if (!testrigsDir.exists()) {
         return new String[0];
      }

      String[] directories = containerDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
         }
      });

      return directories;

   }

   private void moveByCopy(File srcFile, File destFile) throws IOException {
      if (srcFile.isDirectory()) {
         FileUtils.copyDirectory(srcFile, destFile);
         FileUtils.deleteDirectory(srcFile);
      }
      else {
         FileUtils.copyFile(srcFile, destFile);
         if (!srcFile.delete()) {
            throw new IOException(
                  "Failed to delete srcFile: " + srcFile.toString());
         }
      }
   }

   public void putObject(String containerName, String testrigName,
         String objectName, InputStream fileStream) throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);
      File file = Paths.get(testrigDir.getAbsolutePath(), objectName).toFile();

      // check if we got an object name outside of the testrig folder,
      // perhaps because of ".." in the name; disallow it
      if (!file.getCanonicalPath().contains(testrigDir.getCanonicalPath())) {
         throw new Exception("Illegal object name: " + objectName);
      }

      File parentFolder = file.getParentFile();

      if (!parentFolder.exists()) {
         if (!parentFolder.mkdirs()) {
            throw new Exception("failed to create directory "
                  + parentFolder.getAbsolutePath());
         }
      }
      else {
         if (!parentFolder.isDirectory()) {
            throw new Exception(parentFolder.getAbsolutePath()
                  + " already exists but is not a folder");
         }
      }

      try (OutputStream fileOutputStream = new FileOutputStream(file)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }
   }

   public boolean queueWork(WorkItem workItem) throws Exception {

      File testrigDir = Paths
            .get(Main.getSettings().getContainersLocation(),
                  workItem.getContainerName(), workItem.getTestrigName())
            .toFile();

      if (workItem.getTestrigName().isEmpty() || !testrigDir.exists()) {
         throw new Exception("Non-existent testrig");
      }

      boolean success = _workQueueMgr
            .queueUnassignedWork(new QueuedWork(workItem));

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

   public void uploadEnvironment(String containerName, String testrigName,
         String envName, InputStream fileStream) throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);

      File envDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR, envName).toFile();

      if (envDir.exists()) {
         throw new FileExistsException(
               "environment " + envName + " exists for testrig " + testrigName);
      }

      if (!envDir.mkdirs()) {
         throw new Exception(
               "failed to create directory " + envDir.getAbsolutePath());
      }

      File zipFile = Files
            .createTempFile("coordinatortmpuploadenvironment", ".zip").toFile();

      try (OutputStream fileOutputStream = new FileOutputStream(zipFile)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }

      // now unzip
      File unzipDir = Paths
            .get(envDir.getAbsolutePath(), BfConsts.RELPATH_ENV_DIR).toFile();
      UnzipUtility unzipper = new UnzipUtility();
      unzipper.unzip(zipFile, unzipDir.getAbsolutePath());

      // sanity check what we got
      // 1. there should be just one top-level folder
      File[] fileList = unzipDir.listFiles();

      if (fileList.length != 1 || !fileList[0].isDirectory()) {
         FileUtils.deleteDirectory(envDir);
         throw new Exception(
               "Unexpected packaging of environment. There should be just one top-level folder");
      }

      File[] subFileList = fileList[0].listFiles();

      // things look ok, now make the move
      for (File file : subFileList) {
         File target = Paths.get(unzipDir.toString(), file.getName()).toFile();
         moveByCopy(file, target);
      }

      // delete the empty directory and the zip file
      fileList[0].delete();
      zipFile.delete();
   }

   public void uploadQuestion(String containerName, String testrigName,
         String qName, InputStream fileStream, InputStream paramFileStream)
         throws Exception {

      File testrigDir = getdirTestrig(containerName, testrigName);

      File qDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTIONS_DIR, qName).toFile();

      if (qDir.exists()) {
         throw new FileExistsException(
               "question " + qName + " exists for testrig " + testrigName);
      }

      if (!qDir.mkdirs()) {
         throw new Exception(
               "failed to create directory " + qDir.getAbsolutePath());
      }

      File file = Paths
            .get(qDir.getAbsolutePath(), BfConsts.RELPATH_QUESTION_FILE)
            .toFile();

      try (OutputStream fileOutputStream = new FileOutputStream(file)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }

   }

   public void uploadTestrig(String containerName, String testrigName,
         InputStream fileStream) throws Exception {

      File containerDir = getdirContainer(containerName);

      File testrigDir = Paths.get(containerDir.getAbsolutePath(),
            BfConsts.RELPATH_TESTRIGS_DIR, testrigName).toFile();

      if (testrigDir.exists()) {
         throw new FileExistsException(
               "Testrig with name " + testrigName + " already exists");
      }

      if (!testrigDir.mkdirs()) {
         throw new Exception(
               "failed to create directory " + testrigDir.getAbsolutePath());
      }

      File zipFile = Files.createTempFile("testrig", ".zip").toFile();
      try (OutputStream fileOutputStream = new FileOutputStream(zipFile)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }

      // now unzip
      File unzipDir = Paths
            .get(testrigDir.getAbsolutePath(), BfConsts.RELPATH_TEST_RIG_DIR)
            .toFile();
      UnzipUtility unzipper = new UnzipUtility();
      unzipper.unzip(zipFile, unzipDir.getAbsolutePath());

      // sanity check what we got
      // 1. there should be just one top-level folder
      // 2. there should be a directory called configs in that folder
      File[] fileList = unzipDir.listFiles();

      if (fileList.length != 1 || !fileList[0].isDirectory()) {
         FileUtils.deleteDirectory(testrigDir);
         throw new Exception(
               "Unexpected packaging of test rig. There should be just one top-level folder. Got "
                     + fileList.length);
      }

      File[] subFileList = fileList[0].listFiles();

      // boolean foundConfigs = false;
      // for (File file : subFileList) {
      // if (file.isDirectory() && file.getName().equals("configs")) {
      // foundConfigs = true;
      // break;
      // }
      // }
      //
      // if (!foundConfigs) {
      // FileUtils.deleteDirectory(testrigDir);
      // throw new Exception(
      // "Unexpected packaging of test rig. Did not find configs folder inside
      // the top-level folder");
      // }

      // create empty default environment
      File defaultEnvironmentLeafDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR,
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME, BfConsts.RELPATH_ENV_DIR)
            .toFile();
      defaultEnvironmentLeafDir.mkdirs();

      // things look ok, now make the move
      for (File file : subFileList) {
         File target;
         if (isEnvFile(file)) {
            target = Paths.get(defaultEnvironmentLeafDir.getAbsolutePath(),
                  file.getName()).toFile();
         }
         else {
            target = Paths.get(unzipDir.toString(), file.getName()).toFile();
         }
         moveByCopy(file, target);
      }

      // delete the empty directory and the zip file
      fileList[0].delete();
      zipFile.delete();

   }

}
