package org.batfish.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.output.WriterOutputStream;
import org.batfish.client.config.ConfigurationLocator;
import org.batfish.common.BfConsts;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Util;
import org.batfish.common.WorkItem;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.ZipUtility;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

public class Client {

   private static final String COMMAND_ANSWER = "answer";
   private static final String COMMAND_ANSWER_DIFF = "answer-diff";
   private static final String COMMAND_CAT = "cat";
   // private static final String COMMAND_CHANGE_DIR = "cd";
   private static final String COMMAND_CLEAR_SCREEN = "cls";
   private static final String COMMAND_DEL_CONTAINER = "del-container";
   private static final String COMMAND_DEL_ENVIRONMENT = "del-environment";
   private static final String COMMAND_DEL_QUESTION = "del-question";
   private static final String COMMAND_DEL_TESTRIG = "del-testrig";
   private static final String COMMAND_DIR = "dir";
   private static final String COMMAND_ECHO = "echo";
   private static final String COMMAND_GEN_DIFF_DP = "generate-diff-dataplane";
   private static final String COMMAND_GEN_DP = "generate-dataplane";
   private static final String COMMAND_HELP = "help";
   private static final String COMMAND_INIT_CONTAINER = "init-container";
   private static final String COMMAND_INIT_DIFF_ENV = "init-diff-environment";
   private static final String COMMAND_INIT_TESTRIG = "init-testrig";
   private static final String COMMAND_LIST_CONTAINERS = "list-containers";
   private static final String COMMAND_LIST_ENVIRONMENTS = "list-environments";
   private static final String COMMAND_LIST_QUESTIONS = "list-questions";
   private static final String COMMAND_LIST_TESTRIGS = "list-testrigs";
   private static final String COMMAND_PROMPT = "prompt";
   private static final String COMMAND_PWD = "pwd";
   private static final String COMMAND_QUIT = "quit";
   private static final String COMMAND_SET_CONTAINER = "set-container";
   private static final String COMMAND_SET_DIFF_ENV = "set-diff-environment";
   private static final String COMMAND_SET_LOGLEVEL = "set-loglevel";
   private static final String COMMAND_SET_TESTRIG = "set-testrig";
   private static final String COMMAND_UPLOAD_CUSTOM_OBJECT = "upload-custom";

   private static final String DEFAULT_CONTAINER_PREFIX = "cp";
   private static final String DEFAULT_DIFF_ENV_PREFIX = "delta";
   private static final String DEFAULT_ENV_NAME = "default";
   private static final String DEFAULT_QUESTION_PREFIX = "q";
   private static final String DEFAULT_TESTRIG_PREFIX = "tr";

   private static final Map<String, String> MAP_COMMANDS = initCommands();

   private static Map<String, String> initCommands() {
      Map<String, String> descs = new TreeMap<String, String>();
      descs.put(COMMAND_ANSWER, COMMAND_ANSWER
            + " <question-file> [param1=value1 [param2=value2] ...]\n"
            + "\t Answer the question for the default environment");
      descs.put(COMMAND_ANSWER_DIFF, COMMAND_ANSWER_DIFF
            + " <question-file>  [param1=value1 [param2=value2] ...]\n"
            + "\t Answer the question for the differential environment");
      descs.put(COMMAND_CAT, COMMAND_CAT + " <filename>\n"
            + "\t Print the contents of the file");
      // descs.put(COMMAND_CHANGE_DIR, COMMAND_CHANGE_DIR
      // + " <dirname>\n"
      // + "\t Change the working directory");
      descs.put(COMMAND_CLEAR_SCREEN, COMMAND_CLEAR_SCREEN + "\n"
            + "\t Clear screen");
      descs.put(COMMAND_DEL_CONTAINER, COMMAND_DEL_CONTAINER
            + "<container-name>" + "\t Delete the specified container");
      descs.put(COMMAND_DEL_ENVIRONMENT, COMMAND_DEL_ENVIRONMENT
            + "<environment-name>" + "\t Delete the specified environment");
      descs.put(COMMAND_DEL_QUESTION, COMMAND_DEL_QUESTION + "<question-name>"
            + "\t Delete the specified question");
      descs.put(COMMAND_DEL_TESTRIG, COMMAND_DEL_TESTRIG + "<testrig-name>"
            + "\t Delete the specified testrig");
      descs.put(COMMAND_DIR, COMMAND_DIR + "<dir>"
            + "\t List directory contents");
      descs.put(COMMAND_ECHO, COMMAND_ECHO + "<message>"
            + "\t Echo the message");
      descs.put(COMMAND_GEN_DIFF_DP, COMMAND_GEN_DIFF_DP + "\n"
            + "\t Generate dataplane for the differential environment");
      descs.put(COMMAND_GEN_DP, COMMAND_GEN_DP + "\n"
            + "\t Generate dataplane for the default environment");
      descs.put(COMMAND_HELP, COMMAND_HELP + "\n"
            + "\t Print the list of supported commands");
      descs.put(COMMAND_INIT_CONTAINER, COMMAND_INIT_CONTAINER
            + " [<container-name-prefix>]\n" + "\t Initialize a new container");
      descs.put(COMMAND_INIT_DIFF_ENV, COMMAND_INIT_DIFF_ENV
            + " [-nodataplane] <environment zipfile or directory> [<environment-name>]\n"
            + "\t Initialize the differential environment");
      descs.put(COMMAND_INIT_TESTRIG, COMMAND_INIT_TESTRIG
            + " [-nodataplane] <testrig zipfile or directory> [<environment name>]\n"
            + "\t Initialize the testrig with default environment");
      descs.put(COMMAND_LIST_CONTAINERS, COMMAND_LIST_CONTAINERS + "\n"
            + "\t List the containers to which you have access");
      descs.put(COMMAND_LIST_ENVIRONMENTS, COMMAND_LIST_ENVIRONMENTS + "\n"
            + "\t List the environments under current container and testrig");
      descs.put(COMMAND_LIST_QUESTIONS, COMMAND_LIST_QUESTIONS + "\n"
            + "\t List the questions under current container and testrig");
      descs.put(COMMAND_LIST_TESTRIGS, COMMAND_LIST_TESTRIGS + "\n"
            + "\t List the testrigs within the current container");
      descs.put(COMMAND_PROMPT, COMMAND_PROMPT + "\n"
            + "\t Prompts for user to press enter");
      descs.put(COMMAND_PWD, COMMAND_PWD + "\n"
            + "\t Prints the working directory");
      descs.put(COMMAND_QUIT, COMMAND_QUIT + "\n" + "\t Clear screen");
      descs.put(COMMAND_SET_CONTAINER, COMMAND_SET_CONTAINER
            + " <container-name>\n" + "\t Set the current container");
      descs.put(COMMAND_SET_DIFF_ENV, COMMAND_SET_DIFF_ENV
            + " <environment-name>\n"
            + "\t Set the current differential environment");
      descs.put(COMMAND_SET_LOGLEVEL, COMMAND_SET_LOGLEVEL
            + " <debug|info|output|warn|error>\n"
            + "\t Set the loglevel. Default is output");
      descs.put(COMMAND_SET_TESTRIG, COMMAND_SET_TESTRIG + " <testrig-name>\n"
            + "\t Set the current testrig");
      descs.put(COMMAND_UPLOAD_CUSTOM_OBJECT, COMMAND_UPLOAD_CUSTOM_OBJECT
            + " <object-name> <object-file>\n" + "\t Uploads a custom object");
      return descs;
   }

   private String _currContainerName = null;
   private String _currDiffEnv = null;
   private String _currEnv = null;
   private String _currTestrigName = null;

   private BatfishLogger _logger;
   private BfCoordPoolHelper _poolHelper;
   private ConsoleReader _reader;
   
   private Settings _settings;

   private BfCoordWorkHelper _workHelper;

   public Client(String[] args) throws Exception {
      this(new Settings(args));
   }

   public Client(Settings settings) {
      _settings = settings;
   
      switch (_settings.getRunMode()) {
      case "batch":
         _logger =  new BatfishLogger(_settings.getLogLevel(), false,
               _settings.getLogFile(), false, false);
         break;
      case "interactive":
         try {
            _reader = new ConsoleReader();
            _reader.setPrompt("batfish> ");

            List<Completer> completors = new LinkedList<Completer>();
            completors.add(new StringsCompleter(MAP_COMMANDS.keySet()));

            for (Completer c : completors) {
               _reader.addCompleter(c);
            }

            PrintWriter pWriter = new PrintWriter(_reader.getOutput(), true);
            OutputStream os = new WriterOutputStream(pWriter);
            PrintStream ps = new PrintStream(os, true);
            _logger = new BatfishLogger(_settings.getLogLevel(), false, ps);
         }
         catch (Exception e) {
            System.err.printf("Could not initialize client: %s\n", e.getMessage());
            System.exit(1);
         }
         break;
      default:
         System.err.println("org.batfish.client: Unknown run mode. Expect {batch, interactive}");
         System.exit(1);
      }
   
   }

   private File createParamsFile(String[] words, int startIndex, int endIndex)
         throws IOException {

      String paramsLine = Util.joinStrings(" ",
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
      String downloadedFile = _workHelper.getObject(wItem.getContainerName(),
            wItem.getTestrigName(), logFileName);

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

   private boolean isSetContainer(boolean printError) {
      if (_currContainerName == null) {
         if (printError)
            _logger.errorf("Active container is not set\n");
         return false;
      }
      return true;
   }

   private boolean isSetDiffEnvironment() {
      if (_currDiffEnv == null) {
         _logger.errorf("Active diff environment is not set\n");
         return false;
      }
      return true;
   }

   private boolean isSetTestrig() {
      if (_currTestrigName == null) {
         _logger.errorf("Active testrig is not set\n");
         return false;
      }
      return true;
   }

   private void printUsage() {
      for (Map.Entry<String, String> entry : MAP_COMMANDS.entrySet()) {
         _logger.output(entry.getValue() + "\n\n");
      }
   }

   private void processCommand(String[] words) {
      try {
         List<String> options = getCommandOptions(words);
         List<String> parameters = getCommandParameters(words, options.size());

         switch (words[0]) {
         // this is a hidden command for testing
         case "add-worker": {
            boolean result = _poolHelper.addBatfishWorker(words[1]);
            _logger.output("Result: " + result + "\n");
            break;
         }
         case COMMAND_ANSWER: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            String questionFile = parameters.get(0);
            
            if (! new File(questionFile).exists()) {
               _logger.errorf("Question file not found: %s\n", questionFile);
               break;
            }
            
            String questionName = DEFAULT_QUESTION_PREFIX + "_" + UUID.randomUUID().toString();

            File paramsFile = null;

            try {
               paramsFile = createParamsFile(words, 2 + options.size(), words.length - 1);
            }
            catch (Exception e) {
               _logger.error("Could not create params file\n");
               break;
            }

            // upload the question
            boolean resultUpload = _workHelper.uploadQuestion(
                  _currContainerName, _currTestrigName, questionName,
                  questionFile, paramsFile.getAbsolutePath());

            if (!resultUpload)
                break;

            _logger.debug("Uploaded question. Answering now.\n");

            // delete the temporary params file
            if (paramsFile != null) {
               paramsFile.delete();
            }

            // answer the question
            WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(
                  questionName, _currContainerName, _currTestrigName, _currEnv,
                  _currDiffEnv);
            execute(wItemAs);

            break;
         }
         case COMMAND_ANSWER_DIFF: {
            if (!isSetDiffEnvironment() || !isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            String questionFile = parameters.get(0);
            
            if (! new File(questionFile).exists()) {
               _logger.errorf("Question file not found: %s\n", questionFile);
               break;
            }
            
            String questionName = DEFAULT_QUESTION_PREFIX + "_" + UUID.randomUUID().toString();

            File paramsFile = null;

            try {
               paramsFile = createParamsFile(words, options.size() + 2, words.length - 1);
            }
            catch (Exception e) {
               _logger.error("Could not create params file\n");
               break;
            }

            // upload the question
            boolean resultUpload = _workHelper.uploadQuestion(
                  _currContainerName, _currTestrigName, questionName,
                  questionFile, paramsFile.getAbsolutePath());

            if (!resultUpload)
               break;

            _logger.debug("Uploaded question. Answering now.\n");

               // delete the temporary params file
            if (paramsFile != null) {
               paramsFile.delete();
            }

            // answer the question
            WorkItem wItemAs = _workHelper.getWorkItemAnswerDiffQuestion(
                  questionName, _currContainerName, _currTestrigName, _currEnv,
                  _currDiffEnv);
            execute(wItemAs);

            break;
         }
         case COMMAND_CAT: {
            String filename = words[1];

            try (BufferedReader br = new BufferedReader(
                  new FileReader(filename))) {
               String line = null;
               while ((line = br.readLine()) != null) {
                  _logger.output(line + "\n");
               }
            }

            break;
         }
         case COMMAND_DEL_CONTAINER: {
            String containerName = parameters.get(0);
            boolean result = _workHelper.delContainer(containerName);
            _logger.outputf("Result of deleting container: %s\n", result);
            break;
         }
         case COMMAND_DEL_ENVIRONMENT: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            String envName = parameters.get(0);
            boolean result = _workHelper.delEnvironment(_currContainerName,
                  _currTestrigName, envName);
            _logger.outputf("Result of deleting environment: %s\n", result);
            break;
         }
         case COMMAND_DEL_QUESTION: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            String qName = parameters.get(0);
            boolean result = _workHelper.delQuestion(_currContainerName,
                  _currTestrigName, qName);
            _logger.outputf("Result of deleting question: %s\n", result);
            break;
         }
         case COMMAND_DEL_TESTRIG: {
            if (!isSetContainer(true)) {
               break;
            }

            String testrigName = parameters.get(0);
            boolean result = _workHelper.delTestrig(_currContainerName,
                  testrigName);
            _logger.outputf("Result of deleting testrig: %s\n", result);
            break;
         }
         case COMMAND_DIR: {
            String dirname = (parameters.size() == 1) ? parameters.get(0) : ".";

            File currDirectory = new File(dirname);
            for (File file : currDirectory.listFiles()) {
               _logger.output(file.getName() + "\n");
            }
            break;
         }
         case COMMAND_ECHO: {
            _logger.outputf("%s\n", Util.joinStrings(" ",  Arrays.copyOfRange(words, 1, words.length)));
            break;
         }
         case COMMAND_GEN_DP: {
            generateDataplane();
            break;
         }
         case COMMAND_GEN_DIFF_DP: {
            generateDiffDataplane();
            break;
         }
         case COMMAND_HELP: {
            printUsage();
            break;
         }
         case COMMAND_INIT_CONTAINER: {
            String containerPrefix = (words.length > 1) ? words[1]
                  : DEFAULT_CONTAINER_PREFIX;
            _currContainerName = _workHelper.initContainer(containerPrefix);
            _logger.outputf("Active container set to %s\n", _currContainerName);
            break;
         }
         case COMMAND_INIT_DIFF_ENV: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            //check if we are being asked to not generate the dataplane
            boolean generateDiffDataplane = true;

            if (options.size() == 1) {
               if (options.get(0).equals("-nodataplane"))
                  generateDiffDataplane = false;
               else {
                  _logger.outputf("Unknown option %s\n", options.get(0));
                  break;
               }
            }

            String diffEnvLocation = parameters.get(0);
            String diffEnvName = (parameters.size() > 1) ? parameters.get(1)
                  : DEFAULT_DIFF_ENV_PREFIX + UUID.randomUUID().toString();

            if (!uploadTestrigOrEnv(diffEnvLocation, diffEnvName, false))
               break;

            _currDiffEnv = diffEnvName;

            _logger.outputf(
                  "Active delta environment is now %s\n", _currDiffEnv);

            if (generateDiffDataplane) {
               _logger.output("Generating delta dataplane\n");

               if (!generateDiffDataplane())
                  break;

               _logger.output("Generated delta dataplane\n");
            }

            break;
         }
         case COMMAND_INIT_TESTRIG: {
            boolean generateDataplane = true;

            if (options.size() == 1) {
               if (options.get(0).equals("-nodataplane"))
                  generateDataplane = false;
               else {
                  _logger.outputf("Unknown option %s\n", options.get(0));
                  break;
               }
            }

            String testrigLocation = parameters.get(0);
            String testrigName = (parameters.size() > 1) ? parameters.get(1)
                  : DEFAULT_TESTRIG_PREFIX + "_" + UUID.randomUUID().toString();

            //initialize the container if it hasn't been init'd before
            if (!isSetContainer(false)) {
               _currContainerName = _workHelper.initContainer(DEFAULT_CONTAINER_PREFIX);
               _logger.outputf("Init'ed and set active container to %s\n",
                     _currContainerName);
            }

            if (!uploadTestrigOrEnv(testrigLocation, testrigName, true))
               break;

            _logger.output("Uploaded testrig. Parsing now.\n");

            WorkItem wItemParse = _workHelper.getWorkItemParse(
                  _currContainerName, testrigName);

            if (!execute(wItemParse))
               break;

            // set the name of the current testrig
            _currTestrigName = testrigName;
            _currEnv = DEFAULT_ENV_NAME;
            _logger.outputf(
                  "Active testrig is now %s\n", _currTestrigName);

            if (generateDataplane) {
               _logger.output("Generating dataplane now\n");

               if (!generateDataplane())
                  break;

               _logger.output("Generated dataplane\n");
            }

            break;
         }
         case COMMAND_LIST_CONTAINERS: {
            String[] containerList = _workHelper.listContainers();
            _logger.outputf("Containers: %s\n", Arrays.toString(containerList));
            break;
         }
         case COMMAND_LIST_ENVIRONMENTS: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            String[] environmentList = _workHelper.listEnvironments(
                  _currContainerName, _currTestrigName);
            _logger.outputf("Environments: %s\n",
                  Arrays.toString(environmentList));

            break;

         }
         case COMMAND_LIST_QUESTIONS: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }
            String[] questionList = _workHelper.listQuestions(
                  _currContainerName, _currTestrigName);
            _logger.outputf("Questions: %s\n", Arrays.toString(questionList));
            break;
         }
         case COMMAND_LIST_TESTRIGS: {
            Map<String,String> testrigs = _workHelper.listTestrigs(_currContainerName);
            if (testrigs != null)
               for (String testrigName : testrigs.keySet())
                  _logger.outputf("Testrig: %s\n%s\n", testrigName, testrigs.get(testrigName));
            break;
         }
         case COMMAND_PROMPT: {
            if (_settings.getRunMode() == "interactive") {
               _logger.output("\n\n[Press enter to proceed]\n\n");
               BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
               in.readLine();
            }
            break;
         }
         case COMMAND_PWD: {
            final String dir = System.getProperty("user.dir");
            _logger.output("working directory = " + dir + "\n");
            break;
         }
         case COMMAND_QUIT: {
            System.exit(0);
            break;
         }
         case COMMAND_SET_CONTAINER: {
            _currContainerName = parameters.get(0);
            _logger.outputf("Active container is now set to %s\n",
                  _currContainerName);
            break;
         }
         case COMMAND_SET_TESTRIG: {
            _currTestrigName = parameters.get(0);
            _currEnv = DEFAULT_ENV_NAME;
            _logger.outputf(
                  "Active testrig is now %s\n", _currTestrigName);
            break;
         }
         case COMMAND_SET_DIFF_ENV: {
            _currDiffEnv = parameters.get(0);
            _logger.outputf(
                  "Active differential environment is now set to %s\n",
                  _currDiffEnv);
            break;
         }
         case COMMAND_SET_LOGLEVEL: {
            String logLevelStr = parameters.get(0);
            try {
               _logger.setLogLevel(logLevelStr);
               _logger.output("Changed loglevel to " + logLevelStr + "\n");
            }
            catch (Exception e) {
               _logger.errorf("Undefined loglevel value: %s\n", logLevelStr);
            }
            break;
         }
         case COMMAND_UPLOAD_CUSTOM_OBJECT: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               break;
            }

            String objectName = parameters.get(0);
            String objectFile = parameters.get(1);

            // upload the object
            _workHelper.uploadCustomObject(
                  _currContainerName, _currTestrigName, objectName, objectFile);

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

   private boolean uploadTestrigOrEnv(String fileOrDir, String testrigOrEnvName, boolean isTestrig) throws Exception {

      File filePointer = new File(fileOrDir);

      String uploadFilename = fileOrDir;

      if (filePointer.isDirectory()) {
         uploadFilename = File.createTempFile("testrigOrEnv", "zip")
               .getAbsolutePath();
         ZipUtility.zipFiles(filePointer.getAbsolutePath(),
               uploadFilename);
      }

      boolean result = (isTestrig)?
              _workHelper.uploadTestrig(
                    _currContainerName, testrigOrEnvName, uploadFilename):
              _workHelper.uploadEnvironment(
                          _currContainerName, _currTestrigName, testrigOrEnvName,
                          uploadFilename);

      // unequal means we must have created a temporary file
      if (uploadFilename != fileOrDir)
         new File(uploadFilename).delete();

      return result;
   }

   private List<String> getCommandParameters(String[] words, int numOptions) {
      List<String> parameters = new LinkedList<String>();

      for (int index=numOptions+1; index < words.length; index++)
         parameters.add(words[index]);

      return parameters;
   }

   private List<String> getCommandOptions(String[] words) {
      List<String> options = new LinkedList<String>();

      int currIndex = 1;

      while (currIndex < words.length &&
            words[currIndex].startsWith("-")) {
         options.add(words[currIndex]);
         currIndex++;
      }

      return options;
   }

   private boolean generateDataplane() throws Exception {
      if (!isSetTestrig() || !isSetContainer(true))
         return false;

      // generate the data plane
      WorkItem wItemGenDp = _workHelper.getWorkItemGenerateDataPlane(
            _currContainerName, _currTestrigName, _currEnv);

      return execute(wItemGenDp);
   }

   private boolean generateDiffDataplane() throws Exception {
      if (!isSetDiffEnvironment() ||
          !isSetTestrig()   || 
          !isSetContainer(true))  
          return false;

      WorkItem wItemGenDdp = _workHelper
            .getWorkItemGenerateDiffDataPlane(_currContainerName,
                  _currTestrigName, _currEnv, _currDiffEnv);

      return execute(wItemGenDdp);
   }

   public BatfishLogger getLogger() {      
      return _logger;
   }
   
   private void initHelpers() {

      String workMgr = _settings.getCoordinatorHost() + ":"
            + _settings.getCoordinatorWorkPort();
      String poolMgr = _settings.getCoordinatorHost() + ":"
            + _settings.getCoordinatorPoolPort();

      _workHelper = new BfCoordWorkHelper(workMgr, _logger, _settings);
      _poolHelper = new BfCoordPoolHelper(poolMgr);

      while (true) {
         try {
            if (_workHelper.isReachabile()) {
               break;
            }
            Thread.sleep(1 * 1000); // 1 second
         } catch (Exception e) {
            _logger.errorf("Exeption while checking reachability to coordinator: ", e.getMessage());
            System.exit(1);
         }
      }
   }
   
   public void run(List<String> initialCommands) {
      initHelpers();

      _logger.debugf("Will use coordinator at %s://%s\n",
            (_settings.getUseSsl())? "https" : "http",
            _settings.getCoordinatorHost());

      processCommands(initialCommands);
      
      switch (_settings.getRunMode()) {
      case "batch":
         if (_settings.getBatchCommandFile() == null) {
            System.err.println("org.batfish.client: Command file not specified while running in batch mode. Did you mean to run in interactive mode (-runmode interactive)?");
            System.exit(1);
         }
         List<String> commands = null;
         try {
            commands = Files.readAllLines(
                  Paths.get(_settings.getBatchCommandFile()), StandardCharsets.US_ASCII);
         } catch (Exception e) {
            System.err.printf("Exception in reading command file %s: %s",
                  _settings.getBatchCommandFile(), e.getMessage());
            System.exit(1);
         }
         processCommands(commands);

         break;
      case "interactive":
         runInteractive();
         break;
      default:
         System.err.println("org.batfish.client: Unknown run mode. Expect {batch, interactive}");
         System.exit(1);
      }
   }

   private void processCommands(List<String> commands) {
       
      for (String rawLine : commands) {
         String line = rawLine.trim();
         if (line.length() == 0 || line.startsWith("#")) {
            continue;
         }

         _logger.debug("Doing command: " + line + "\n");

         String[] words = line.split("\\s+");

         if (words.length > 0) {
            if (validCommandUsage(words)) {
               processCommand(words);
            }
         }
      }
   }

   private void runInteractive() {
      try {

         String rawLine;
         while ((rawLine = _reader.readLine()) != null) {
            String line = rawLine.trim();
            if (line.length() == 0)
               continue;

            if (line.equals(COMMAND_CLEAR_SCREEN)) {
               _reader.clearScreen();
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
