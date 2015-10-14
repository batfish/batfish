package org.batfish.client;

import java.io.BufferedReader;
import java.io.File;
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

   private String _currentEnvironment = null;
   private String _currentTestrigName = null;

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

         _logger.infof("status: %s\n", status);

         Thread.sleep(10 * 1000);

         status = _workHelper.getWorkStatus(wItem.getId());
      }

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
               _logger.output(line);
            }
         }
      }

      // TODO: remove the log file?

      if (status == WorkStatusCode.TERMINATEDNORMALLY) {
         return true;
      }
      else {
         _logger.errorf("WorkItem failed: %s", wItem);
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
            boolean result = _workHelper.uploadTestrig(words[1], words[2]);
            _logger.output("Result: " + result + "\n");
            break;
         }
         case "parse-vendor-specific": {
            WorkItem wItem = _workHelper
                  .getWorkItemParseVendorSpecific(words[1]);
            wItem.addRequestParam(BfConsts.ARG_LOG_LEVEL,
                  _logger.getLogLevelStr());
            _logger.info("work-id is " + wItem.getId() + "\n");
            boolean result = _workHelper.queueWork(wItem);
            _logger.info("Queuing result: " + result + "\n");
            break;
         }
         case "init-testrig": {
            String testrigName = words[1];
            String testrigFile = words[2];

            // upload the testrig
            boolean resultUpload = _workHelper.uploadTestrig(testrigName,
                  testrigFile);
            _logger.output("Result of uploading testrig: " + resultUpload
                  + "\n");

            if (!resultUpload) {
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
            _currentTestrigName = testrigName;
            _currentEnvironment = "default";
            _logger.outputf("Set active testrig to %s and environment to %s\n",
                  _currentTestrigName, _currentEnvironment);

            break;
         }
         case "set-testrig": {
            String testrigName = words[1];
            String environmentName = words[2];

            _currentTestrigName = testrigName;
            _currentEnvironment = environmentName;

            _logger.outputf("Set active testrig to %s and environment to %s\n",
                  _currentTestrigName, _currentEnvironment);

            break;
         }
         case "generate-dataplane": {

            if (_currentTestrigName == null || _currentEnvironment == null) {
               _logger
                     .errorf(
                           "Active testrig name or environment is not set (%s, %s)\n",
                           _currentTestrigName, _currentEnvironment);
               break;
            }

            // generate facts
            WorkItem wItemGf = _workHelper.getWorkItemGenerateFacts(
                  _currentTestrigName, _currentEnvironment);
            boolean resultGf = execute(wItemGf);

            if (!resultGf) {
               break;
            }

            // generate the data plane
            WorkItem wItemGenDp = _workHelper.getWorkItemGenerateDataPlane(
                  _currentTestrigName, _currentEnvironment);
            boolean resultGenDp = execute(wItemGenDp);

            if (!resultGenDp) {
               break;
            }

            // get the data plane
            WorkItem wItemGetDp = _workHelper.getWorkItemGetDataPlane(
                  _currentTestrigName, _currentEnvironment);
            boolean resultGetDp = execute(wItemGetDp);

            if (!resultGetDp) {
               break;
            }

            // create z3 encoding
            WorkItem wItemCz3e = _workHelper.getWorkItemCreateZ3Encoding(
                  _currentTestrigName, _currentEnvironment);
            boolean resultCz3e = execute(wItemCz3e);

            if (!resultCz3e) {
               break;
            }
         }
         case "answer": {
            String questionName = words[1];
            String questionFile = words[2];

            if (_currentTestrigName == null || _currentEnvironment == null) {
               _logger
                     .errorf(
                           "Active testrig name or environment is not set (%s, %s)\n",
                           _currentTestrigName, _currentEnvironment);
               break;
            }

            // upload the question
            boolean resultUpload = _workHelper.uploadQuestion(
                  _currentTestrigName, questionName, questionFile);
            _logger.output("Result of uploading question: " + resultUpload
                  + "\n");

            if (!resultUpload) {
               break;
            }

            // answer the question
            WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(
                  _currentTestrigName, _currentEnvironment, questionName);
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