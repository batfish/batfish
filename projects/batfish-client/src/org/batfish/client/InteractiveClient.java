package org.batfish.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.output.WriterOutputStream;
import org.batfish.common.BfConsts;
import org.batfish.common.BatfishLogger;
import org.batfish.common.WorkItem;
import org.batfish.common.CoordConsts.WorkStatusCode;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

public class InteractiveClient {

   private String _currDiffEnv = null;
   private String _currEnv = null;
   private String _currTestrigName = null;

   private BatfishLogger _logger;
   private BfCoordPoolHelper _poolHelper;

   private BfCoordWorkHelper _workHelper;

   public InteractiveClient(String workMgr, String poolMgr) {
      try {

         ConsoleReader reader = new ConsoleReader();
         reader.setPrompt("batfish> ");

         List<Completer> completors = new LinkedList<Completer>();
         completors.add(new StringsCompleter("foo", "bar", "baz"));

         for (Completer c : completors) {
            reader.addCompleter(c);
         }

         String line;

         PrintWriter pWriter = new PrintWriter(reader.getOutput(), true);
         OutputStream os = new WriterOutputStream(pWriter);
         PrintStream ps = new PrintStream(os, true);
         _logger = new BatfishLogger(
               BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT), false,
               ps);

         _workHelper = new BfCoordWorkHelper(workMgr, _logger);
         _poolHelper = new BfCoordPoolHelper(poolMgr);

         while ((line = reader.readLine()) != null) {

            // skip over empty lines
            if (line.trim().length() == 0) {
               continue;
            }

            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
               break;
            }

            if (line.equalsIgnoreCase("cls")) {
               reader.clearScreen();
               continue;
            }

            String[] words = line.split("\\s+");

            if (words.length > 0) {
               if (validCommandUsage(words)) {
                  processCommand(words);
               }
            }
         }
      }
      catch (Throwable t) {
         t.printStackTrace();
      }
   }

   private boolean execute(WorkItem wItem) throws Exception {

      wItem.addRequestParam(BfConsts.ARG_LOG_LEVEL, _logger.getLogLevelStr());
      _logger.info("work-id is " + wItem.getId() + "\n");

      boolean queueWorkResult = _workHelper.queueWork(wItem);
      _logger.info("Queuing result: " + queueWorkResult + "\n");

      if (!queueWorkResult) {
         return queueWorkResult;
      }

      WorkStatusCode status = _workHelper.getWorkStatus(wItem.getId());

      while (status != WorkStatusCode.TERMINATEDABNORMALLY
            && status != WorkStatusCode.TERMINATEDNORMALLY
            && status != WorkStatusCode.ASSIGNMENTERROR) {

         _logger.output(". ");
         _logger.infof("status: %s\n", status);

         Thread.sleep(1 * 1000);

         status = _workHelper.getWorkStatus(wItem.getId());
      }

      _logger.output("\n");
      _logger.infof("final status: %s\n", status);

      // get the results
      String logFileName = wItem.getId() + ".log";
      String downloadedFile = _workHelper.getObject(wItem.getTestrigName(),
            logFileName);

      if (downloadedFile == null) {
         _logger.errorf("Failed to get output file %s\n", logFileName);
         return false;
      }
      else {
         try (BufferedReader br = new BufferedReader(new FileReader(
               downloadedFile))) {
            String line = null;
            while ((line = br.readLine()) != null) {
               _logger.output(line + "\n");
            }
         }
      }

      // TODO: remove the log file?

      if (status == WorkStatusCode.TERMINATEDNORMALLY) {
         return true;
      }
      else {
         //_logger.errorf("WorkItem failed: %s", wItem);
         return false;
      }
   }

   private void processCommand(String[] words) {

      try {
         switch (words[0]) {
         case "add-worker": {
            boolean result = _poolHelper.addBatfishWorker(words[1]);
            _logger.output("Result: " + result + "\n");
            break;
         }
         case "upload-testrig": {
            // OLD command
            boolean result = _workHelper.uploadTestrig(words[1], words[2]);
            _logger.output("Result: " + result + "\n");
            break;
         }
         case "parse-vendor-specific": {
            // OLD command
            WorkItem wItem = _workHelper
                  .getWorkItemParseVendorSpecific(words[1]);
            wItem.addRequestParam(BfConsts.ARG_LOG_LEVEL,
                  _logger.getLogLevelStr());
            _logger.info("work-id is " + wItem.getId() + "\n");
            boolean result = _workHelper.queueWork(wItem);
            _logger.info("Queuing result: " + result + "\n");
            break;
         }
         case "init-diff-environment": {
            String diffEnvName = words[1];
            String diffEnvFile = words[2];

            if (_currTestrigName == null) {
               _logger.errorf("Active testrig is not set\n");
               break;
            }

            // upload the environment
            boolean resultUpload = _workHelper.uploadEnvironment(
                  _currTestrigName, diffEnvName, diffEnvFile);

            if (resultUpload) {
               _logger.output("Successfully uploaded environment.\n");
            }
            else {
               break;
            }

            _currDiffEnv = diffEnvName;

            _logger.outputf(
                  "Active differential environment is now set to %s\n",
                  _currDiffEnv);

            break;
         }
         case "init-testrig": {
            String testrigName = words[1];
            String testrigFile = words[2];

            // upload the testrig
            boolean resultUpload = _workHelper.uploadTestrig(testrigName,
                  testrigFile);

            if (resultUpload) {
               _logger
                     .output("Successfully uploaded testrig. Starting parsing\n");
            }
            else {
               break;
            }

            // vendor specific parsing
            WorkItem wItemPvs = _workHelper
                  .getWorkItemParseVendorSpecific(testrigName);
            boolean resultPvs = execute(wItemPvs);

            if (!resultPvs) {
               break;
            }

            // vendor independent parsing
            WorkItem wItemPvi = _workHelper
                  .getWorkItemParseVendorIndependent(testrigName);
            boolean resultPvi = execute(wItemPvi);

            if (!resultPvi) {
               break;
            }

            // upload a default environment
            boolean resultUploadEnv = _workHelper.uploadEnvironment(
                  testrigName, "default", testrigFile);
            _logger.info("Result of uploading default environment: "
                  + resultUploadEnv);

            // set the name of the current testrig
            _currTestrigName = testrigName;
            _currEnv = "default";
            _logger.outputf(
                  "Active (testrig, environment) is now set to (%s, %s)\n",
                  _currTestrigName, _currEnv);

            break;
         }
         case "set-testrig": {
            String testrigName = words[1];
            String environmentName = words[2];

            _currTestrigName = testrigName;
            _currEnv = environmentName;

            _logger.outputf(
                  "Active (testrig, environment) is now set to (%s, %s)\n",
                  _currTestrigName, _currEnv);

            break;
         }
         case "set-diff-environment": {
            String diffEnvName = words[1];

            _currDiffEnv = diffEnvName;

            _logger.outputf(
                  "Active differential environment is now set to %s\n",
                  _currDiffEnv);

            break;
         }
         case "generate-dataplane": {

            if (_currTestrigName == null || _currEnv == null) {
               _logger
                     .errorf(
                           "Active testrig name or environment is not set (%s, %s)\n",
                           _currTestrigName, _currEnv);
               break;
            }

            // generate the data plane
            WorkItem wItemGenDp = _workHelper.getWorkItemGenerateDataPlane(
                  _currTestrigName, _currEnv);
            boolean resultGenDp = execute(wItemGenDp);

            if (!resultGenDp) {
               break;
            }

            // get the data plane
            WorkItem wItemGetDp = _workHelper.getWorkItemGetDataPlane(
                  _currTestrigName, _currEnv);
            boolean resultGetDp = execute(wItemGetDp);

            if (!resultGetDp) {
               break;
            }

            break;
         }
         case "generate-diff-dataplane": {

            if (_currTestrigName == null || _currEnv == null
                  || _currDiffEnv == null) {
               _logger
                     .errorf(
                           "Active testrig, environment, or differential environment is not set (%s, %s, %s)\n",
                           _currTestrigName, _currEnv, _currDiffEnv);
               break;
            }

            // generate the data plane
            WorkItem wItemGenDdp = _workHelper
                  .getWorkItemGenerateDiffDataPlane(_currTestrigName, _currEnv,
                        _currDiffEnv);
            boolean resultGenDdp = execute(wItemGenDdp);

            if (!resultGenDdp) {
               break;
            }

            // get the data plane
            WorkItem wItemGetDdp = _workHelper.getWorkItemGetDiffDataPlane(
                  _currTestrigName, _currEnv, _currDiffEnv);
            boolean resultGetDdp = execute(wItemGetDdp);

            if (!resultGetDdp) {
               break;
            }

            break;
         }
         case "answer": {
            String questionName = words[1];
            String questionFile = words[2];

            if (_currTestrigName == null || _currEnv == null) {
               _logger
                     .errorf(
                           "Active testrig name or environment is not set: (%s, %s)\n",
                           _currTestrigName, _currEnv);
               break;
            }

            // upload the question
            boolean resultUpload = _workHelper.uploadQuestion(_currTestrigName,
                  questionName, questionFile);

            if (resultUpload) {
               _logger
                     .output("Successfully uploaded question. Starting to answer\n");
            }
            else {
               break;
            }

            // answer the question
            WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(
                  questionName, _currTestrigName, _currEnv, _currDiffEnv);
            execute(wItemAs);

            break;
         }
         case "get-work-status": {
            WorkStatusCode status = _workHelper.getWorkStatus(UUID
                  .fromString(words[1]));
            _logger.output("Result: " + status + "\n");
            break;
         }
         case "get-object": {
            String file = _workHelper.getObject(words[1], words[2]);
            _logger.output("Result: " + file + "\n");
            break;
         }
         case "set-loglevel": {
            String logLevelStr = words[1];
            try {
               _logger.setLogLevel(logLevelStr);
               _logger.output("Changed loglevel to " + logLevelStr + "\n");
            }
            catch (Exception e) {
               _logger.errorf("Undefined loglevel value: %s\n", logLevelStr);
            }
            break;
         }
         default:
            _logger.error("Unsupported command " + words[0] + "\n");
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private boolean validCommandUsage(String[] words) {
      return true;
   }
}