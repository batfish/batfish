package org.batfish.coordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.apache.logging.log4j.Logger;
import org.batfish.common.BfConsts;
import org.batfish.common.UnzipUtility;
import org.batfish.common.WorkItem;
import org.batfish.common.AppZip;
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

   // private Runnable _checkWorkTask;
   // private Runnable _assignWorkTask;
   //
   // private ScheduledExecutorService _checkService;
   // private ScheduledExecutorService _assignService;
   //
   // private ScheduledFuture<?> _checkFuture;
   // private ScheduledFuture<?> _assignFuture;

   private Logger _logger;

   private WorkQueueMgr _workQueueMgr;

   public WorkMgr() {
      _logger = Main.initializeLogger();
      _workQueueMgr = new WorkQueueMgr();

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
            new AssignWorkTask(), 0,
            Main.getSettings().getPeriodAssignWorkMs(), TimeUnit.MILLISECONDS);

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
         _logger.error("Got exception in assignWork: " + e.getMessage());
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
         File autobasedir = Paths.get(
               Main.getSettings().getTestrigStorageLocation(),
               work.getWorkItem().getTestrigName()).toFile();
         task.put("autobasedir", autobasedir.getAbsolutePath());
         task.put(
               "logfile",
               Paths.get(autobasedir.getAbsolutePath(),
                     work.getId().toString() + ".log").toString());

         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client
               .target(
                     String.format("http://%s%s/%s", worker,
                           BfConsts.SVC_BASE_RSC, BfConsts.SVC_RUN_TASK_RSC))
               .queryParam(
                     BfConsts.SVC_TASKID_KEY,
                     UriComponent.encode(work.getId().toString(),
                           UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
               .queryParam(
                     BfConsts.SVC_TASK_KEY,
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
            _logger.info(String.format(
                  "WM:AssignWork: response: %s [%s] [%s]\n", array.toString(),
                  array.get(0), array.get(1)));

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
      _logger.info("WM:CheckWork: Trying to check " + work + " on " + worker
            + " \n");

      BfConsts.TaskStatus status = BfConsts.TaskStatus.UnreachableOrBadResponse;

      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", worker, BfConsts.SVC_BASE_RSC,
                     BfConsts.SVC_GET_TASKSTATUS_RSC)).queryParam(
               BfConsts.SVC_TASKID_KEY,
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
               _logger.error(String.format(
                     "got error while refreshing status: %s %s\n",
                     array.get(0), array.get(1)));
            }
            else {

               JSONObject jObj = new JSONObject(array.get(1).toString());

               if (!jObj.has("status")) {
                  _logger.error(String
                        .format("did not see status key in json response\n"));
               }
               else {
                  status = BfConsts.TaskStatus
                        .valueOf(jObj.getString("status"));
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

      _workQueueMgr.processStatusCheckResult(work, status);
   }

   public File getObject(String testrigName, String objectName) {
      File file = Paths.get(Main.getSettings().getTestrigStorageLocation(),
            testrigName, objectName).toFile();

      if (file.isFile()) {
         return file;
      }
      else if (file.isDirectory()) {
         File zipfile = new File(file.getAbsolutePath() + ".zip");

         if (zipfile.exists()) {
            zipfile.delete();
         }

         AppZip appZip = new AppZip();
         appZip.zip(file.getAbsolutePath(), zipfile.getAbsolutePath());

         return zipfile;
      }

      return null;
   }

   public JSONObject getStatusJson() throws JSONException {
      return _workQueueMgr.getStatusJson();
   }

   public QueuedWork getWork(UUID workItemId) {
      return _workQueueMgr.getWork(workItemId);
   }

   private void moveByCopy(File srcFile, File destFile) throws IOException {
      if (srcFile.isDirectory()) {
         FileUtils.copyDirectory(srcFile, destFile);
         FileUtils.deleteDirectory(srcFile);
      }
      else {
         FileUtils.copyFile(srcFile, destFile);
         if (!srcFile.delete()) {
            throw new IOException("Failed to delete srcFile: "
                  + srcFile.toString());
         }
      }
   }

   public boolean queueWork(WorkItem workItem) throws Exception {

      File testrigDir = Paths.get(
            Main.getSettings().getTestrigStorageLocation(),
            workItem.getTestrigName()).toFile();

      if (workItem.getTestrigName().isEmpty() || !testrigDir.exists()) {
         throw new Exception("Non-existent testrig");
      }

      boolean success = _workQueueMgr.queueUnassignedWork(new QueuedWork(
            workItem));

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

   public void uploadEnvironment(String testrigName, String envName,
         InputStream fileStream) throws Exception {

      File testrigDir = Paths.get(
            Main.getSettings().getTestrigStorageLocation(), testrigName)
            .toFile();

      if (!testrigDir.exists()) {
         throw new FileNotFoundException("testrig " + testrigName
               + " does not exist");
      }

      File envDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_ENVIRONMENTS_DIR, envName).toFile();

      if (envDir.exists()) {
         throw new FileExistsException("environment " + envName
               + " exists for testrig " + testrigName);
      }

      if (!envDir.mkdirs()) {
         throw new Exception("failed to create directory "
               + envDir.getAbsolutePath());
      }

      File zipFile = Files.createTempFile("coordinatortmpuploadenvironment",
            ".zip").toFile();

      try (OutputStream fileOutputStream = new FileOutputStream(zipFile)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }

      // now unzip
      File unzipDir = Paths.get(envDir.getAbsolutePath(),
            BfConsts.RELPATH_ENV_DIR).toFile();
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

   public void uploadQuestion(String testrigName, String qName,
         InputStream fileStream, InputStream paramFileStream) throws Exception {

      File testrigDir = Paths.get(
            Main.getSettings().getTestrigStorageLocation(), testrigName)
            .toFile();

      if (!testrigDir.exists()) {
         throw new FileNotFoundException("testrig " + testrigName
               + " does not exist");
      }

      File qDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTIONS_DIR, qName).toFile();

      if (qDir.exists()) {
         throw new FileExistsException("question " + qName
               + " exists for testrig " + testrigName);
      }

      if (!qDir.mkdirs()) {
         throw new Exception("failed to create directory "
               + qDir.getAbsolutePath());
      }

      File file = Paths.get(qDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTION_FILE).toFile();

      try (OutputStream fileOutputStream = new FileOutputStream(file)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = fileStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, read);
         }
      }

      File paramFile = Paths.get(qDir.getAbsolutePath(),
            BfConsts.RELPATH_QUESTION_PARAM_FILE).toFile();

      System.out.println("parameters file = "
            + BfConsts.RELPATH_QUESTION_PARAM_FILE + "\n");

      try (OutputStream paramFileOutputStream = new FileOutputStream(paramFile)) {
         int read = 0;
         final byte[] bytes = new byte[1024];
         while ((read = paramFileStream.read(bytes)) != -1) {
            paramFileOutputStream.write(bytes, 0, read);
         }
      }
   }

   public void uploadTestrig(String name, InputStream fileStream)
         throws Exception {

      File testrigDir = Paths.get(
            Main.getSettings().getTestrigStorageLocation(), name).toFile();

      if (testrigDir.exists()) {
         throw new FileExistsException("Testrig with name " + name
               + " already exists");
      }

      if (!testrigDir.mkdirs()) {
         throw new Exception("failed to create directory "
               + testrigDir.getAbsolutePath());
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
      File unzipDir = Paths.get(testrigDir.getAbsolutePath(),
            BfConsts.RELPATH_TEST_RIG_DIR).toFile();
      UnzipUtility unzipper = new UnzipUtility();
      unzipper.unzip(zipFile, unzipDir.getAbsolutePath());

      // sanity check what we got
      // 1. there should be just one top-level folder
      // 2. there should be a directory called configs in that folder
      File[] fileList = unzipDir.listFiles();

      if (fileList.length != 1 || !fileList[0].isDirectory()) {
         FileUtils.deleteDirectory(testrigDir);
         throw new Exception(
               "Unexpected packaging of test rig. There should be just one top-level folder");
      }

      File[] subFileList = fileList[0].listFiles();

      boolean foundConfigs = false;
      for (File file : subFileList) {
         if (file.isDirectory() && file.getName().equals("configs")) {
            foundConfigs = true;
            break;
         }
      }

      if (!foundConfigs) {
         FileUtils.deleteDirectory(testrigDir);
         throw new Exception(
               "Unexpected packaging of test rig. Did not find configs folder inside the top-level folder");
      }

      // things look ok, now make the move
      for (File file : subFileList) {
         File target = Paths.get(unzipDir.toString(), file.getName()).toFile();
         moveByCopy(file, target);
      }

      // delete the empty directory and the zip file
      fileList[0].delete();
      zipFile.delete();
   }
}