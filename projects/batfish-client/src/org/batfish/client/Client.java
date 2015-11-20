package org.batfish.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.WriterOutputStream;
import org.batfish.common.BfConsts;
import org.batfish.common.BatfishLogger;
import org.batfish.common.WorkItem;
import org.batfish.common.CoordConsts.WorkStatusCode;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

public class Client {

   private static final String COMMAND_ANSWER = "answer";
   private static final String COMMAND_ANSWER_DIFF = "answer-diff";
   private static final String COMMAND_CLEAR_SCREEN = "cls";
   private static final String COMMAND_GEN_DIFF_DP = "generate-diff-dataplane";
   private static final String COMMAND_GEN_DP = "generate-dataplane";
   private static final String COMMAND_HELP = "help";
   private static final String COMMAND_INIT_DIFF_ENV = "init-diff-environment";
   private static final String COMMAND_INIT_TESTRIG = "init-testrig";
   private static final String COMMAND_QUIT = "quit";
   private static final String COMMAND_SET_DIFF_ENV = "set-diff-environment";
   private static final String COMMAND_SET_LOGLEVEL = "set-loglevel";
   private static final String COMMAND_SET_TESTRIG = "set-testrig";

   private static final Map<String, String> MAP_COMMANDS = initCommands();

   private static Map<String, String> initCommands() {
      Map<String, String> descs = new HashMap<String, String>();
      descs.put(COMMAND_ANSWER, COMMAND_ANSWER
            + " <question-name> <question-file>\n"
            + "\t Answer the question for the default environment");
      descs.put(COMMAND_ANSWER_DIFF, COMMAND_ANSWER_DIFF
            + " <question-name> <question-file>\n"
            + "\t Answer the question for the differential environment");
      descs.put(COMMAND_CLEAR_SCREEN, COMMAND_CLEAR_SCREEN + "\n"
            + "\t Clear screen");
      descs.put(COMMAND_GEN_DIFF_DP, COMMAND_GEN_DIFF_DP + "\n"
            + "\t Generate dataplane for the differential environment");
      descs.put(COMMAND_GEN_DP, COMMAND_GEN_DP + "\n"
            + "\t Generate dataplane for the default environment");
      descs.put(COMMAND_HELP, "help\n"
            + "\t Print the list of supported commands");
      descs.put(COMMAND_INIT_DIFF_ENV, COMMAND_INIT_DIFF_ENV
            + " <environment-name> <environment-file>\n"
            + "\t Initialize the differential environment");
      descs.put(COMMAND_INIT_TESTRIG, COMMAND_INIT_TESTRIG
            + " <environment-name> <environment-file>\n"
            + "\t Initialize the testrig with default environment");
      descs.put(COMMAND_QUIT, COMMAND_QUIT + "\n" + "\t Clear screen");
      descs.put(COMMAND_SET_DIFF_ENV, COMMAND_SET_DIFF_ENV
            + " <environment-name>\n"
            + "\t Set the current differential environment");
      descs.put(COMMAND_SET_LOGLEVEL, COMMAND_SET_LOGLEVEL
            + " <debug|info|output|warn|error>\n"
            + "\t Set the loglevel. Default is output");
      descs.put(COMMAND_SET_TESTRIG, COMMAND_SET_TESTRIG + " <testrig-name>\n"
            + "\t Set the current testrig");
      return descs;
   }

   private static String joinStrings(String delimiter, String[] parts) {
      StringBuilder sb = new StringBuilder();
      for (String part : parts) {
         sb.append(part + delimiter);
      }
      String joined = sb.toString();
      int joinedLength = joined.length();
      String result;
      if (joinedLength > 0) {
         result = joined.substring(0, joinedLength - delimiter.length());
      }
      else {
         result = joined;
      }
      return result;
   }

   private String _currDiffEnv = null;
   private String _currEnv = null;

   private String _currTestrigName = null;

   private BatfishLogger _logger;
   private BfCoordPoolHelper _poolHelper;

   private BfCoordWorkHelper _workHelper;

   public Client(Settings settings) {
      if (settings.getCommandFile() != null) {
         RunBatchMode(settings);
      }
      else {
         RunInteractiveMode(settings);
      }

   }

   private File createParamsFile(String[] words, int startIndex, int endIndex)
         throws IOException {

      String paramsLine = joinStrings(" ",
            Arrays.copyOfRange(words, startIndex, endIndex + 1));

      File paramFile = Files.createTempFile("params", null).toFile();
      _logger.debugf("Creating temporary params file: %s\n",
            paramFile.getAbsolutePath());

      BufferedWriter writer = new BufferedWriter(new FileWriter(paramFile));
      writer.write("#parameters for the question\n");

      writer.write(paramsLine + "\n");

      // for (int index = startIndex; index <= endIndex; index++) {
      // writer.write(words[index] + "\n");
      // }

      writer.close();

      return paramFile;
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
         // _logger.errorf("WorkItem failed: %s", wItem);
         return false;
      }
   }

   private void printUsage() {
      for (Map.Entry<String, String> entry : MAP_COMMANDS.entrySet()) {
         _logger.output(entry.getValue() + "\n\n");
      }
   }

   private void processCommand(String[] words) {

      try {
         switch (words[0]) {
         // this is almost a hidden command; it should not be invoked through
         // here
         case "add-worker": {
            boolean result = _poolHelper.addBatfishWorker(words[1]);
            _logger.output("Result: " + result + "\n");
            break;
         }
         case COMMAND_ANSWER: {
            String questionName = words[1];
            String questionFile = words[2];

            if (_currTestrigName == null || _currEnv == null) {
               _logger
                     .errorf(
                           "Active testrig name or environment is not set: (%s, %s)\n",
                           _currTestrigName, _currEnv);
               break;
            }

            File paramsFile = null;

            try {
               paramsFile = createParamsFile(words, 3, words.length - 1);
            }
            catch (Exception e) {
               _logger.error("Could not create params file\n");
               break;
            }

            // upload the question
            boolean resultUpload = _workHelper.uploadQuestion(_currTestrigName,
                  questionName, questionFile, paramsFile);

            if (resultUpload) {
               _logger
                     .output("Successfully uploaded question. Starting to answer\n");
            }
            else {
               break;
            }

            // delete the temporary params file
            if (paramsFile != null) {
               paramsFile.delete();
            }

            // answer the question
            WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(
                  questionName, _currTestrigName, _currEnv, _currDiffEnv);
            execute(wItemAs);

            break;
         }
         case COMMAND_ANSWER_DIFF: {

            if (_currTestrigName == null || _currEnv == null
                  || _currDiffEnv == null) {
               _logger
                     .errorf(
                           "Active testrig, environment, or differential environment is not set (%s, %s, %s)\n",
                           _currTestrigName, _currEnv, _currDiffEnv);
               break;
            }

            String questionName = words[1];
            String questionFile = words[2];

            if (_currTestrigName == null || _currEnv == null) {
               _logger
                     .errorf(
                           "Active testrig name or environment is not set: (%s, %s)\n",
                           _currTestrigName, _currEnv);
               break;
            }

            File paramsFile = null;

            try {
               paramsFile = createParamsFile(words, 3, words.length - 1);
            }
            catch (Exception e) {
               _logger.error("Could not create params file\n");
               break;
            }

            // upload the question
            boolean resultUpload = _workHelper.uploadQuestion(_currTestrigName,
                  questionName, questionFile, paramsFile);

            if (resultUpload) {
               _logger
                     .output("Successfully uploaded question. Starting to answer\n");
            }
            else {
               break;
            }

            // delete the temporary params file
            if (paramsFile != null) {
               paramsFile.delete();
            }

            // answer the question
            WorkItem wItemAs = _workHelper.getWorkItemAnswerDiffQuestion(
                  questionName, _currTestrigName, _currEnv, _currDiffEnv);
            execute(wItemAs);

            break;
         }
         case COMMAND_GEN_DP: {

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
         case COMMAND_GEN_DIFF_DP: {
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
         case COMMAND_HELP: {
            printUsage();
            break;
         }
         case COMMAND_INIT_DIFF_ENV: {
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
         case COMMAND_INIT_TESTRIG: {
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
         case COMMAND_SET_TESTRIG: {
            String testrigName = words[1];

            _currTestrigName = testrigName;
            _currEnv = "default";

            _logger.outputf(
                  "Active (testrig, environment) is now set to (%s, %s)\n",
                  _currTestrigName, _currEnv);

            break;
         }
         case COMMAND_SET_DIFF_ENV: {
            String diffEnvName = words[1];

            _currDiffEnv = diffEnvName;

            _logger.outputf(
                  "Active differential environment is now set to %s\n",
                  _currDiffEnv);

            break;
         }
         case COMMAND_SET_LOGLEVEL: {
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
            _logger.error("Type 'help' to see the list of valid commands\n");
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void RunBatchMode(Settings settings) {

      _logger = new BatfishLogger(settings.getLogLevel(), false,
            settings.getLogFile(), false);

      String workMgr = settings.getServiceHost() + ":"
            + settings.getServiceWorkPort();
      String poolMgr = settings.getServiceHost() + ":"
            + settings.getServicePoolPort();

      _workHelper = new BfCoordWorkHelper(workMgr, _logger);
      _poolHelper = new BfCoordPoolHelper(poolMgr);

      try (BufferedReader br = new BufferedReader(new FileReader(
            settings.getCommandFile()))) {
         String line = null;
         while ((line = br.readLine()) != null) {

            if (line.startsWith("#")) {
               continue;
            }

            _logger.output("Doing command: " + line + "\n");

            String[] words = line.split("\\s+");

            if (words.length > 0) {
               if (validCommandUsage(words)) {
                  processCommand(words);
               }
            }
         }
      }
      catch (FileNotFoundException e) {
         _logger.errorf("Command file not found: %s\n",
               settings.getCommandFile());
      }
      catch (Exception e) {
         _logger.errorf("Exception while reading command file: %s\n", e);
      }
   }

   private void RunInteractiveMode(Settings settings) {
      try {

         ConsoleReader reader = new ConsoleReader();
         reader.setPrompt("batfish> ");

         List<Completer> completors = new LinkedList<Completer>();
         completors.add(new StringsCompleter(MAP_COMMANDS.keySet()));

         for (Completer c : completors) {
            reader.addCompleter(c);
         }

         String line;

         PrintWriter pWriter = new PrintWriter(reader.getOutput(), true);
         OutputStream os = new WriterOutputStream(pWriter);
         PrintStream ps = new PrintStream(os, true);
         _logger = new BatfishLogger(settings.getLogLevel(), false, ps);

         String workMgr = settings.getServiceHost() + ":"
               + settings.getServiceWorkPort();
         String poolMgr = settings.getServiceHost() + ":"
               + settings.getServicePoolPort();

         _workHelper = new BfCoordWorkHelper(workMgr, _logger);
         _poolHelper = new BfCoordPoolHelper(poolMgr);

         while ((line = reader.readLine()) != null) {

            // skip over empty lines
            if (line.trim().length() == 0) {
               continue;
            }

            if (line.equals(COMMAND_QUIT)) {
               break;
            }

            if (line.equals(COMMAND_CLEAR_SCREEN)) {
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

   private boolean validCommandUsage(String[] words) {
      return true;
   }
}
