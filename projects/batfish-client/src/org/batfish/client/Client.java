package org.batfish.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.client.Settings.RunMode;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Pair;
import org.batfish.common.WorkItem;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.plugin.AbstractClient;
import org.batfish.common.plugin.IClient;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ZipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.questions.IEnvironmentCreationQuestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;

public class Client extends AbstractClient implements IClient {

   private static final String DEFAULT_CONTAINER_PREFIX = "cp";
   private static final String DEFAULT_DELTA_ENV_PREFIX = "env_";
   private static final String DEFAULT_ENV_NAME = BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME;
   private static final String DEFAULT_QUESTION_PREFIX = "q";
   private static final String DEFAULT_TESTRIG_PREFIX = "tr_";

   private static final String FLAG_FAILING_TEST = "-error";
   private static final int NUM_TRIES_WARNING_THRESHOLD = 5;

   private Map<String, String> _additionalBatfishOptions;

   private String _currContainerName = null;
   private String _currDeltaEnv = null;
   private String _currDeltaTestrig;
   private String _currEnv = null;
   private String _currTestrig = null;

   private BatfishLogger _logger;

   @SuppressWarnings("unused")
   private BfCoordPoolHelper _poolHelper;

   private ConsoleReader _reader;

   private Settings _settings;

   private BfCoordWorkHelper _workHelper;

   public Client(Settings settings) {
      super(false, settings.getPluginDirs());
      _additionalBatfishOptions = new HashMap<>();
      _settings = settings;

      switch (_settings.getRunMode()) {
      case batch:
         if (_settings.getBatchCommandFile() == null) {
            System.err.println(
                  "org.batfish.client: Command file not specified while running in batch mode.");
            System.err.printf(
                  "Use '-%s <cmdfile>' if you want batch mode, or '-%s interactive' if you want interactive mode\n",
                  Settings.ARG_COMMAND_FILE, Settings.ARG_RUN_MODE);
            System.exit(1);
         }
         _logger = new BatfishLogger(_settings.getLogLevel(), false,
               _settings.getLogFile(), false, false);
         break;
      case gendatamodel:
         _logger = new BatfishLogger(_settings.getLogLevel(), false,
               _settings.getLogFile(), false, false);
         break;
      case genquestions:
         if (_settings.getQuestionsDir() == null) {
            System.err.println(
                  "org.batfish.client: Out dir not specified while running in genquestions mode.");
            System.err.printf("Use '-%s <cmdfile>'\n",
                  Settings.ARG_QUESTIONS_DIR);
            System.exit(1);
         }
         _logger = new BatfishLogger(_settings.getLogLevel(), false,
               _settings.getLogFile(), false, false);
         break;
      case interactive:
         try {
            _reader = new ConsoleReader();
            _reader.setPrompt("batfish> ");
            _reader.setExpandEvents(false);

            List<Completer> completors = new LinkedList<>();
            completors.add(new CommandCompleter());

            for (Completer c : completors) {
               _reader.addCompleter(c);
            }

            PrintWriter pWriter = new PrintWriter(_reader.getOutput(), true);
            OutputStream os = new WriterOutputStream(pWriter);
            PrintStream ps = new PrintStream(os, true);
            _logger = new BatfishLogger(_settings.getLogLevel(), false, ps);
         }
         catch (Exception e) {
            System.err.printf("Could not initialize client: %s\n",
                  e.getMessage());
            e.printStackTrace();
            System.exit(1);
         }
         break;
      default:
         System.err.println("org.batfish.client: Unknown run mode.");
         System.exit(1);
      }

   }

   public Client(String[] args) throws Exception {
      this(new Settings(args));
   }

   private boolean answerFile(String questionFile, String paramsLine,
         boolean isDelta, FileWriter outWriter) throws Exception {

      if (!new File(questionFile).exists()) {
         throw new FileNotFoundException(
               "Question file not found: " + questionFile);
      }

      String questionName = DEFAULT_QUESTION_PREFIX + "_"
            + UUID.randomUUID().toString();

      File paramsFile = createTempFile("parameters", paramsLine);
      paramsFile.deleteOnExit();

      // upload the question
      boolean resultUpload = _workHelper.uploadQuestion(_currContainerName,
            _currTestrig, questionName, questionFile,
            paramsFile.getAbsolutePath());

      if (!resultUpload) {
         return false;
      }

      _logger.debug("Uploaded question. Answering now.\n");

      // delete the temporary params file
      if (paramsFile != null) {
         paramsFile.delete();
      }

      // answer the question
      WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(questionName,
            _currContainerName, _currTestrig, _currEnv, _currDeltaTestrig,
            _currDeltaEnv, isDelta);

      return execute(wItemAs, outWriter);
   }

   private boolean answerType(String questionType, String paramsLine,
         boolean isDelta, FileWriter outWriter) throws Exception {

      Map<String, String> parameters = parseParams(paramsLine);

      String questionString;
      String parametersString = "";
      if (questionType.startsWith(QuestionHelper.MACRO_PREFIX)) {
         try {
            questionString = QuestionHelper.resolveMacro(questionType,
                  paramsLine, _questions);
         }
         catch (BatfishException e) {
            _logger.errorf("Could not resolve macro: %s\n", e.getMessage());
            return false;
         }
      }
      else {
         questionString = QuestionHelper.getQuestionString(questionType,
               _questions, false);
         _logger.debugf("Question Json:\n%s\n", questionString);

         parametersString = QuestionHelper.getParametersString(parameters);
         _logger.debugf("Parameters Json:\n%s\n", parametersString);
      }

      File questionFile = createTempFile("question", questionString);

      boolean result = answerFile(questionFile.getAbsolutePath(),
            parametersString, isDelta, outWriter);

      if (questionFile != null) {
         questionFile.delete();
      }

      return result;
   }

   private File createTempFile(String filePrefix, String content)
         throws IOException {

      File tempFile = Files.createTempFile(filePrefix, null).toFile();
      tempFile.deleteOnExit();

      _logger.debugf("Creating temporary %s file: %s\n", filePrefix,
            tempFile.getAbsolutePath());

      FileWriter writer = new FileWriter(tempFile);
      writer.write(content + "\n");
      writer.close();

      return tempFile;
   }

   private boolean execute(WorkItem wItem, FileWriter outWriter)
         throws Exception {

      _logger.info("work-id is " + wItem.getId() + "\n");

      wItem.addRequestParam(BfConsts.ARG_LOG_LEVEL,
            _settings.getBatfishLogLevel());

      for (String option : _additionalBatfishOptions.keySet()) {
         wItem.addRequestParam(option, _additionalBatfishOptions.get(option));
      }

      boolean queueWorkResult = _workHelper.queueWork(wItem);
      _logger.info("Queuing result: " + queueWorkResult + "\n");

      if (!queueWorkResult) {
         return queueWorkResult;
      }

      WorkStatusCode status = _workHelper.getWorkStatus(wItem.getId());

      while (status != WorkStatusCode.TERMINATEDABNORMALLY
            && status != WorkStatusCode.TERMINATEDNORMALLY
            && status != WorkStatusCode.ASSIGNMENTERROR) {

         _logger.info(". ");
         _logger.infof("status: %s\n", status);

         Thread.sleep(1 * 1000);

         status = _workHelper.getWorkStatus(wItem.getId());
      }

      _logger.info("\n");
      _logger.infof("final status: %s\n", status);

      // get the answer
      String ansFileName = wItem.getId() + BfConsts.SUFFIX_ANSWER_JSON_FILE;
      String downloadedAnsFile = _workHelper.getObject(wItem.getContainerName(),
            wItem.getTestrigName(), ansFileName);

      if (downloadedAnsFile == null) {
         _logger.errorf(
               "Failed to get answer file %s. Fix batfish and remove the statement below this line\n",
               ansFileName);
         // return false;
      }
      else {
         String answerString = CommonUtil
               .readFile(Paths.get(downloadedAnsFile));

         // Check if we need to make things pretty
         // Don't if we are writing to FileWriter, because we need valid JSON in
         // that case
         String answerStringToPrint = answerString;
         if (outWriter == null && _settings.getPrettyPrintAnswers()) {
            ObjectMapper mapper = new BatfishObjectMapper(
                  getCurrentClassLoader());
            Answer answer = mapper.readValue(answerString, Answer.class);
            answerStringToPrint = answer.prettyPrint();
         }

         if (outWriter == null) {
            _logger.output(answerStringToPrint + "\n");
         }
         else {
            outWriter.write(answerStringToPrint);
         }

         // tests serialization/deserialization when running in debug mode
         if (_logger.getLogLevel() >= BatfishLogger.LEVEL_DEBUG) {
            try {
               ObjectMapper mapper = new BatfishObjectMapper(
                     getCurrentClassLoader());
               Answer answer = mapper.readValue(answerString, Answer.class);

               String newAnswerString = mapper.writeValueAsString(answer);
               JsonNode tree = mapper.readTree(answerString);
               JsonNode newTree = mapper.readTree(newAnswerString);
               if (!CommonUtil.checkJsonEqual(tree, newTree)) {
                  // if (!tree.equals(newTree)) {
                  _logger.errorf(
                        "Original and recovered Json are different. Recovered = %s\n",
                        newAnswerString);
               }
            }
            catch (Exception e) {
               _logger.outputf("Could NOT deserialize Json to Answer: %s\n",
                     e.getMessage());
            }
         }
      }

      // get and print the log when in debugging mode
      if (_logger.getLogLevel() >= BatfishLogger.LEVEL_DEBUG) {
         _logger.output("---------------- Service Log --------------\n");
         String logFileName = wItem.getId() + BfConsts.SUFFIX_LOG_FILE;
         String downloadedFile = _workHelper.getObject(wItem.getContainerName(),
               wItem.getTestrigName(), logFileName);

         if (downloadedFile == null) {
            _logger.errorf("Failed to get log file %s\n", logFileName);
            return false;
         }
         else {
            try (BufferedReader br = new BufferedReader(
                  new FileReader(downloadedFile))) {
               String line = null;
               while ((line = br.readLine()) != null) {
                  _logger.output(line + "\n");
               }
            }
         }
      }

      if (status == WorkStatusCode.TERMINATEDNORMALLY) {
         return true;
      }
      else {
         // _logger.errorf("WorkItem failed: %s", wItem);
         return false;
      }
   }

   private void generateDatamodel() {
      try {
         ObjectMapper mapper = new BatfishObjectMapper();

         JsonSchemaGenerator schemaGenNew = new JsonSchemaGenerator(mapper);
         JsonNode schemaNew = schemaGenNew
               .generateJsonSchema(Configuration.class);
         _logger.output(mapper.writeValueAsString(schemaNew));

         // JsonSchemaGenerator schemaGenNew = new JsonSchemaGenerator(mapper,
         // true, JsonSchemaConfig.vanillaJsonSchemaDraft4());
         // JsonNode schemaNew =
         // schemaGenNew.generateJsonSchema(Configuration.class);
         // _logger.output(mapper.writeValueAsString(schemaNew));

         // _logger.output("\n");
         // JsonNode schemaNew2 =
         // schemaGenNew.generateJsonSchema(SchemaTest.Parent.class);
         // _logger.output(mapper.writeValueAsString(schemaNew2));
      }
      catch (Exception e) {
         _logger.errorf("Could not generate data model: " + e.getMessage());
         e.printStackTrace();
      }
   }

   private boolean generateDataplane(FileWriter outWriter) throws Exception {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      // generate the data plane
      WorkItem wItemGenDp = _workHelper.getWorkItemGenerateDataPlane(
            _currContainerName, _currTestrig, _currEnv);

      return execute(wItemGenDp, outWriter);
   }

   private boolean generateDeltaDataplane(FileWriter outWriter)
         throws Exception {
      if (!isSetDeltaEnvironment() || !isSetTestrig()
            || !isSetContainer(true)) {
         return false;
      }

      WorkItem wItemGenDdp = _workHelper.getWorkItemGenerateDeltaDataPlane(
            _currContainerName, _currTestrig, _currEnv, _currDeltaTestrig,
            _currDeltaEnv);

      return execute(wItemGenDdp, outWriter);
   }

   private void generateQuestions() {

      File questionsDir = Paths.get(_settings.getQuestionsDir()).toFile();

      if (!questionsDir.exists()) {
         if (!questionsDir.mkdirs()) {
            _logger.errorf("Could not create questions dir %s\n",
                  _settings.getQuestionsDir());
            System.exit(1);
         }
      }

      _questions.forEach((qName, supplier) -> {
         try {
            String questionString = QuestionHelper.getQuestionString(qName,
                  _questions, true);
            String qFile = Paths
                  .get(_settings.getQuestionsDir(), qName + ".json").toFile()
                  .getAbsolutePath();

            PrintWriter writer = new PrintWriter(qFile);
            writer.write(questionString);
            writer.close();
         }
         catch (Exception e) {
            _logger.errorf("Could not write question %s: %s\n", qName,
                  e.getMessage());
         }
      });
   }

   private List<String> getCommandOptions(String[] words) {
      List<String> options = new LinkedList<>();

      int currIndex = 1;

      while (currIndex < words.length && words[currIndex].startsWith("-")) {
         options.add(words[currIndex]);
         currIndex++;
      }

      return options;
   }

   private List<String> getCommandParameters(String[] words, int numOptions) {
      List<String> parameters = new LinkedList<>();

      for (int index = numOptions + 1; index < words.length; index++) {
         parameters.add(words[index]);
      }

      return parameters;
   }

   @Override
   public BatfishLogger getLogger() {
      return _logger;
   }

   public Settings getSettings() {
      return _settings;
   }

   private void initHelpers() {

      String workMgr = _settings.getCoordinatorHost() + ":"
            + _settings.getCoordinatorWorkPort();
      String poolMgr = _settings.getCoordinatorHost() + ":"
            + _settings.getCoordinatorPoolPort();

      _workHelper = new BfCoordWorkHelper(workMgr, _logger, _settings);
      _poolHelper = new BfCoordPoolHelper(poolMgr);

      int numTries = 0;

      while (true) {
         try {
            numTries++;
            boolean exceededNumTriesWarningThreshold = numTries > NUM_TRIES_WARNING_THRESHOLD;
            if (_workHelper.isReachable(exceededNumTriesWarningThreshold)) {
               // print this message only we might have printed unable to
               // connect message earlier
               if (exceededNumTriesWarningThreshold) {
                  _logger.outputf("Connected to coordinator after %d tries\n",
                        numTries);
               }
               break;
            }
            Thread.sleep(1 * 1000); // 1 second
         }
         catch (Exception e) {
            _logger.errorf(
                  "Exeption while checking reachability to coordinator: ",
                  e.getMessage());
            System.exit(1);
         }
      }
   }

   private boolean isSetContainer(boolean printError) {
      if (!_settings.getSanityCheck()) {
         return true;
      }

      if (_currContainerName == null) {
         if (printError) {
            _logger.errorf("Active container is not set\n");
         }
         return false;
      }

      return true;
   }

   private boolean isSetDeltaEnvironment() {
      if (!_settings.getSanityCheck()) {
         return true;
      }

      if (_currDeltaTestrig == null) {
         _logger.errorf("Active delta testrig is not set\n");
         return false;
      }

      if (_currDeltaEnv == null) {
         _logger.errorf("Active delta environment is not set\n");
         return false;
      }
      return true;
   }

   private boolean isSetTestrig() {
      if (!_settings.getSanityCheck()) {
         return true;
      }

      if (_currTestrig == null) {
         _logger.errorf("Active testrig is not set.\n");
         _logger.errorf(
               "Specify testrig on command line (-%s <testrigdir>) or use command (%s <testrigdir>)\n",
               Settings.ARG_TESTRIG_DIR, Command.INIT_TESTRIG);
         return false;
      }
      return true;
   }

   private Map<String, String> parseParams(String paramsLine) {
      Map<String, String> parameters = new HashMap<>();

      Pattern pattern = Pattern.compile("([\\w_]+)\\s*=\\s*(.+)");

      String[] params = paramsLine.split("\\|");

      _logger.debugf("Found %d parameters\n", params.length);

      for (String param : params) {
         Matcher matcher = pattern.matcher(param);

         while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            _logger.debugf("key=%s value=%s\n", key, value);

            parameters.put(key, value);
         }
      }

      return parameters;
   }

   private void printUsage() {
      for (Command cmd : Command.getUsageMap().keySet()) {
         printUsage(cmd);
      }
   }

   private void printUsage(Command command) {
      Pair<String, String> usage = Command.getUsageMap().get(command);
      _logger.outputf("%s %s\n\t%s\n\n", command.commandName(),
            usage.getFirst(), usage.getSecond());
   }

   private boolean processCommand(String command) {
      String line = command.trim();
      if (line.length() == 0 || line.startsWith("#")) {
         return true;
      }
      _logger.debug("Doing command: " + line + "\n");

      String[] words = line.split("\\s+");

      if (!validCommandUsage(words)) {
         return false;
      }

      return processCommand(words, null);
   }

   private boolean processCommand(String[] words, FileWriter outWriter) {
      try {
         List<String> options = getCommandOptions(words);
         List<String> parameters = getCommandParameters(words, options.size());

         Command command;
         try {
            command = Command.fromName(words[0]);
         }
         catch (BatfishException e) {
            _logger.errorf("Command failed: %s\n", e.getMessage());
            return false;
         }

         switch (command) {
         // this is a hidden command for testing

         // case "add-worker": {
         // boolean result = _poolHelper.addBatfishWorker(words[1]);
         // _logger.output("Result: " + result + "\n");
         // return true;
         // }
         case ADD_BATFISH_OPTION: {
            String optionKey = parameters.get(0);
            String optionValue = CommonUtil.joinStrings(" ",
                  Arrays.copyOfRange(words, 2 + options.size(), words.length));
            _additionalBatfishOptions.put(optionKey, optionValue);
            return true;
         }
         case ANSWER: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String questionFile = parameters.get(0);
            String paramsLine = CommonUtil.joinStrings(" ",
                  Arrays.copyOfRange(words, 2 + options.size(), words.length));

            return answerFile(questionFile, paramsLine, false, outWriter);
         }
         case ANSWER_DELTA: {
            if (!isSetDeltaEnvironment() || !isSetTestrig()
                  || !isSetContainer(true)) {
               return false;
            }

            String questionFile = parameters.get(0);
            String paramsLine = CommonUtil.joinStrings(" ",
                  Arrays.copyOfRange(words, 2 + options.size(), words.length));

            return answerFile(questionFile, paramsLine, true, outWriter);
         }
         case CAT: {
            String filename = words[1];

            try (BufferedReader br = new BufferedReader(
                  new FileReader(filename))) {
               String line = null;
               while ((line = br.readLine()) != null) {
                  _logger.output(line + "\n");
               }
            }

            return true;
         }
         case CLEAR_SCREEN: {
            // this should have taken care of before coming in here
            return false;
         }
         case DEL_BATFISH_OPTION: {
            String optionKey = parameters.get(0);

            if (!_additionalBatfishOptions.containsKey(optionKey)) {
               _logger.outputf("Batfish option %s does not exist\n", optionKey);
               return false;
            }
            _additionalBatfishOptions.remove(optionKey);
            return true;
         }

         case DEL_CONTAINER: {
            String containerName = parameters.get(0);
            boolean result = _workHelper.delContainer(containerName);
            _logger.outputf("Result of deleting container: %s\n", result);
            return true;
         }
         case DEL_ENVIRONMENT: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String envName = parameters.get(0);
            boolean result = _workHelper.delEnvironment(_currContainerName,
                  _currTestrig, envName);
            _logger.outputf("Result of deleting environment: %s\n", result);
            return true;
         }
         case DEL_QUESTION: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String qName = parameters.get(0);
            boolean result = _workHelper.delQuestion(_currContainerName,
                  _currTestrig, qName);
            _logger.outputf("Result of deleting question: %s\n", result);
            return true;
         }
         case DEL_TESTRIG: {
            if (!isSetContainer(true)) {
               return false;
            }

            String testrigName = parameters.get(0);
            boolean result = _workHelper.delTestrig(_currContainerName,
                  testrigName);
            _logger.outputf("Result of deleting testrig: %s\n", result);
            return true;
         }
         case DIR: {
            String dirname = (parameters.size() == 1) ? parameters.get(0) : ".";

            File currDirectory = new File(dirname);
            for (File file : currDirectory.listFiles()) {
               _logger.output(file.getName() + "\n");
            }
            return true;
         }
         case ECHO: {
            _logger.outputf("%s\n", CommonUtil.joinStrings(" ",
                  Arrays.copyOfRange(words, 1, words.length)));
            return true;
         }
         case EXIT:
         case QUIT: {
            System.exit(0);
            return true;
         }
         case GEN_DP: {
            return generateDataplane(outWriter);
         }
         case GEN_DELTA_DP: {
            return generateDeltaDataplane(outWriter);
         }
         case GET:
         case GET_DELTA: {
            boolean isDelta = (command == Command.GET_DELTA);

            if (!isSetTestrig() || !isSetContainer(true)
                  || (isDelta && !isSetDeltaEnvironment())) {
               return false;
            }

            String qTypeStr = parameters.get(0).toLowerCase();
            String paramsLine = CommonUtil.joinStrings(" ",
                  Arrays.copyOfRange(words, 2 + options.size(), words.length));

            // TODO: make environment creation a command, not a question
            if (!qTypeStr.startsWith(QuestionHelper.MACRO_PREFIX)
                  && qTypeStr.equals(IEnvironmentCreationQuestion.NAME)) {

               String deltaEnvName = DEFAULT_DELTA_ENV_PREFIX
                     + UUID.randomUUID().toString();

               String prefixString = (paramsLine.trim().length() > 0) ? " | "
                     : "";
               paramsLine += String.format("%s %s=%s", prefixString,
                     IEnvironmentCreationQuestion.ENVIRONMENT_NAME_KEY,
                     deltaEnvName);

               if (!answerType(qTypeStr, paramsLine, isDelta, outWriter)) {
                  return false;
               }

               _currDeltaEnv = deltaEnvName;
               _currDeltaTestrig = _currTestrig;

               _logger.output("Active delta testrig->environment is set ");
               _logger.infof("to %s->%s\n", _currDeltaTestrig, _currDeltaEnv);
               _logger.output("\n");

               return true;
            }
            else {
               return answerType(qTypeStr, paramsLine, isDelta, outWriter);
            }
         }
         case GET_ANSWER: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            boolean formatJson = true;

            if (options.size() == 1) {
               if (options.get(0).equals("-html")) {
                  formatJson = false;
               }
               else {
                  _logger.outputf(
                        "Unknown option: %s (note that json does not need a flag)\n",
                        options.get(0));
                  return false;
               }
            }

            String questionName = parameters.get(0);

            String answerFileName = String.format("%s/%s/%s",
                  BfConsts.RELPATH_QUESTIONS_DIR, questionName,
                  (formatJson) ? BfConsts.RELPATH_ANSWER_JSON
                        : BfConsts.RELPATH_ANSWER_HTML);

            String downloadedAnsFile = _workHelper.getObject(_currContainerName,
                  _currTestrig, answerFileName);
            if (downloadedAnsFile == null) {
               _logger.errorf("Failed to get answer file %s\n", answerFileName);
               return false;
            }

            String answerString = CommonUtil
                  .readFile(Paths.get(downloadedAnsFile));
            _logger.output(answerString);
            _logger.output("\n");

            return true;
         }
         case GET_QUESTION: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String questionName = parameters.get(0);

            String questionFileName = String.format("%s/%s/%s",
                  BfConsts.RELPATH_QUESTIONS_DIR, questionName,
                  BfConsts.RELPATH_QUESTION_FILE);

            String downloadedQuestionFile = _workHelper.getObject(
                  _currContainerName, _currTestrig, questionFileName);
            if (downloadedQuestionFile == null) {
               _logger.errorf("Failed to get question file %s\n",
                     questionFileName);
               return false;
            }

            String questionString = CommonUtil
                  .readFile(Paths.get(downloadedQuestionFile));
            _logger.outputf("Question:\n%s\n", questionString);

            String paramsFileName = String.format("%s/%s/%s",
                  BfConsts.RELPATH_QUESTIONS_DIR, questionName,
                  BfConsts.RELPATH_QUESTION_PARAM_FILE);

            String downloadedParamsFile = _workHelper
                  .getObject(_currContainerName, _currTestrig, paramsFileName);
            if (downloadedParamsFile == null) {
               _logger.errorf("Failed to get parameters file %s\n",
                     paramsFileName);
               return false;
            }

            String paramsString = CommonUtil
                  .readFile(Paths.get(downloadedParamsFile));
            _logger.outputf("Parameters:\n%s\n", paramsString);

            return true;
         }
         case HELP: {
            if (parameters.size() == 1) {
               Command cmd = Command.fromName(parameters.get(0));
               printUsage(cmd);
            }
            else {
               printUsage();
            }
            return true;
         }
         case CHECK_API_KEY: {
            String isValid = _workHelper.checkApiKey();
            _logger.outputf("Api key validitiy: %s\n", isValid);
            return true;
         }
         case INIT_CONTAINER: {
            String containerPrefix = (words.length > 1) ? words[1]
                  : DEFAULT_CONTAINER_PREFIX;
            _currContainerName = _workHelper.initContainer(containerPrefix);
            if (_currContainerName == null) {
               _logger.errorf("Could not init container\n");
               return false;
            }
            _logger.output("Active container is set");
            _logger.infof(" to  %s\n", _currContainerName);
            _logger.output("\n");
            return true;
         }
         case INIT_DELTA_ENV: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String deltaEnvLocation = parameters.get(0);
            String deltaEnvName = (parameters.size() > 1) ? parameters.get(1)
                  : DEFAULT_DELTA_ENV_PREFIX + UUID.randomUUID().toString();

            if (!uploadTestrigOrEnv(deltaEnvLocation, deltaEnvName, false)) {
               return false;
            }

            _currDeltaEnv = deltaEnvName;
            _currDeltaTestrig = _currTestrig;

            _logger.output("Active delta testrig->environment is set");
            _logger.infof("to %s->%s\n", _currDeltaTestrig, _currDeltaEnv);
            _logger.output("\n");

            WorkItem wItemGenDdp = _workHelper
                  .getWorkItemCompileDeltaEnvironment(_currContainerName,
                        _currDeltaTestrig, _currEnv, _currDeltaEnv);

            if (!execute(wItemGenDdp, outWriter)) {
               return false;
            }

            return true;
         }
         case INIT_DELTA_TESTRIG:
         case INIT_TESTRIG: {

            String testrigLocation = parameters.get(0);
            String testrigName = (parameters.size() > 1) ? parameters.get(1)
                  : DEFAULT_TESTRIG_PREFIX + UUID.randomUUID().toString();

            // initialize the container if it hasn't been init'd before
            if (!isSetContainer(false)) {
               _currContainerName = _workHelper
                     .initContainer(DEFAULT_CONTAINER_PREFIX);
               if (_currContainerName == null) {
                  _logger.errorf("Could not init container\n");
                  return false;
               }
               _logger.outputf("Init'ed and set active container");
               _logger.infof(" to %s\n", _currContainerName);
               _logger.output("\n");
            }

            if (!uploadTestrigOrEnv(testrigLocation, testrigName, true)) {
               return false;
            }

            _logger.output("Uploaded testrig. Parsing now.\n");

            WorkItem wItemParse = _workHelper
                  .getWorkItemParse(_currContainerName, testrigName);

            if (!execute(wItemParse, outWriter)) {
               return false;
            }

            if (command == Command.INIT_TESTRIG) {
               _currTestrig = testrigName;
               _currEnv = DEFAULT_ENV_NAME;
               _logger.infof("Base testrig is now %s\n", _currTestrig);
            }
            else {
               _currDeltaTestrig = testrigName;
               _currDeltaEnv = DEFAULT_ENV_NAME;
               _logger.infof("Delta testrig is now %s\n", _currDeltaTestrig);
            }

            return true;
         }
         case LIST_CONTAINERS: {
            String[] containerList = _workHelper.listContainers();
            _logger.outputf("Containers: %s\n", Arrays.toString(containerList));
            return true;
         }
         case LIST_ENVIRONMENTS: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String[] environmentList = _workHelper
                  .listEnvironments(_currContainerName, _currTestrig);
            _logger.outputf("Environments: %s\n",
                  Arrays.toString(environmentList));

            return true;
         }
         case LIST_QUESTIONS: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }
            String[] questionList = _workHelper
                  .listQuestions(_currContainerName, _currTestrig);
            _logger.outputf("Questions: %s\n", Arrays.toString(questionList));
            return true;
         }
         case LIST_TESTRIGS: {
            Map<String, String> testrigs = _workHelper
                  .listTestrigs(_currContainerName);
            if (testrigs != null) {
               for (String testrigName : testrigs.keySet()) {
                  _logger.outputf("Testrig: %s\n%s\n", testrigName,
                        testrigs.get(testrigName));
               }
            }
            return true;
         }
         case PROMPT: {
            if (_settings.getRunMode() == RunMode.interactive) {
               _logger.output("\n\n[Press enter to proceed]\n\n");
               BufferedReader in = new BufferedReader(
                     new InputStreamReader(System.in));
               in.readLine();
            }
            return true;
         }
         case PWD: {
            final String dir = System.getProperty("user.dir");
            _logger.output("working directory = " + dir + "\n");
            return true;
         }
         case REINIT_TESTRIG:
         case REINIT_DELTA_TESTRIG: {

            String testrig;
            if (command == Command.REINIT_TESTRIG) {
               _logger.output("Reinitializing testrig. Parsing now.\n");
               testrig = _currTestrig;
            }
            else {
               _logger.output("Reinitializing delta testrig. Parsing now.\n");
               testrig = _currDeltaTestrig;
            }

            WorkItem wItemParse = _workHelper
                  .getWorkItemParse(_currContainerName, testrig);

            if (!execute(wItemParse, outWriter)) {
               return false;
            }

            return true;
         }

         case SET_BATFISH_LOGLEVEL: {
            String logLevelStr = parameters.get(0).toLowerCase();
            if (!BatfishLogger.isValidLogLevel(logLevelStr)) {
               _logger.errorf("Undefined loglevel value: %s\n", logLevelStr);
               return false;
            }
            _settings.setBatfishLogLevel(logLevelStr);
            _logger.output("Changed batfish loglevel to " + logLevelStr + "\n");
            return true;
         }
         case SET_CONTAINER: {
            _currContainerName = parameters.get(0);
            _logger.outputf("Active container is now set to %s\n",
                  _currContainerName);
            return true;
         }
         case SET_DELTA_ENV: {
            _currDeltaEnv = parameters.get(0);
            if (_currDeltaTestrig == null) {
               _currDeltaTestrig = _currTestrig;
            }
            _logger.outputf("Active delta testrig->environment is now %s->%s\n",
                  _currDeltaTestrig, _currDeltaEnv);
            return true;
         }
         case SET_ENV: {
            if (!isSetTestrig()) {
               return false;
            }
            _currEnv = parameters.get(0);
            _logger.outputf("Base testrig->env is now %s->%s\n", _currTestrig,
                  _currEnv);
            return true;
         }
         case SET_DELTA_TESTRIG: {
            _currDeltaTestrig = parameters.get(0);
            _currDeltaEnv = (parameters.size() > 1) ? parameters.get(1)
                  : DEFAULT_ENV_NAME;
            _logger.outputf("Delta testrig->env is now %s->%s\n",
                  _currDeltaTestrig, _currDeltaEnv);
            return true;
         }
         case SET_LOGLEVEL: {
            String logLevelStr = parameters.get(0).toLowerCase();
            if (!BatfishLogger.isValidLogLevel(logLevelStr)) {
               _logger.errorf("Undefined loglevel value: %s\n", logLevelStr);
               return false;
            }
            _logger.setLogLevel(logLevelStr);
            _settings.setLogLevel(logLevelStr);
            _logger.output("Changed client loglevel to " + logLevelStr + "\n");
            return true;
         }
         case SET_PRETTY_PRINT: {
            String ppStr = parameters.get(0).toLowerCase();
            boolean prettyPrint = Boolean.parseBoolean(ppStr);
            _settings.setPrettyPrintAnswers(prettyPrint);
            _logger.output("Set pretty printing answers to " + ppStr + "\n");
            return true;
         }
         case SET_TESTRIG: {
            if (!isSetContainer(true)) {
               return false;
            }

            _currTestrig = parameters.get(0);
            _currEnv = (parameters.size() > 1) ? parameters.get(1)
                  : DEFAULT_ENV_NAME;
            _logger.outputf("Base testrig->env is now %s->%s\n", _currTestrig,
                  _currEnv);
            return true;
         }
         case SHOW_API_KEY: {
            _logger.outputf("Current API Key is %s\n", _settings.getApiKey());
            return true;
         }
         case SHOW_BATFISH_LOGLEVEL: {
            _logger.outputf("Current batfish log level is %s\n",
                  _settings.getBatfishLogLevel());
            return true;
         }
         case SHOW_BATFISH_OPTIONS: {
            _logger.outputf("There are %d additional batfish options\n",
                  _additionalBatfishOptions.size());
            for (String option : _additionalBatfishOptions.keySet()) {
               _logger.outputf("    %s : %s \n", option,
                     _additionalBatfishOptions.get(option));
            }
            return true;
         }
         case SHOW_CONTAINER: {
            _logger.outputf("Current container is %s\n", _currContainerName);
            return true;
         }
         case SHOW_COORDINATOR_HOST: {
            _logger.outputf("Current coordinator host is %s\n",
                  _settings.getCoordinatorHost());
            return true;
         }
         case SHOW_LOGLEVEL: {
            _logger.outputf("Current client log level is %s\n",
                  _logger.getLogLevelStr());
            return true;
         }
         case SHOW_DELTA_TESTRIG: {
            if (!isSetDeltaEnvironment()) {
               return false;
            }
            _logger.outputf("Delta testrig->environment is %s->%s\n",
                  _currDeltaTestrig, _currDeltaEnv);
            return true;
         }
         case SHOW_TESTRIG: {
            if (!isSetTestrig()) {
               return false;
            }
            _logger.outputf("Base testrig->environment is %s->%s\n",
                  _currTestrig, _currEnv);
            return true;
         }
         case TEST: {
            boolean failingTest = false;
            boolean missingReferenceFile = false;
            boolean testPassed = false;
            int testCommandIndex = 1;
            if (parameters.get(testCommandIndex).equals(FLAG_FAILING_TEST)) {
               testCommandIndex++;
               failingTest = true;
            }
            String referenceFileName = parameters.get(0);

            String[] testCommand = parameters
                  .subList(testCommandIndex, parameters.size())
                  .toArray(new String[0]);

            _logger.debugf("Ref file is %s. \n", referenceFileName,
                  parameters.size());
            _logger.debugf("Test command is %s\n",
                  Arrays.toString(testCommand));

            File referenceFile = new File(referenceFileName);

            if (!referenceFile.exists()) {
               _logger.errorf("Reference file does not exist: %s\n",
                     referenceFileName);
               missingReferenceFile = true;
            }

            File testoutFile = Files.createTempFile("test", "out").toFile();
            testoutFile.deleteOnExit();

            FileWriter testoutWriter = new FileWriter(testoutFile);

            boolean testCommandSucceeded = processCommand(testCommand,
                  testoutWriter);
            testoutWriter.close();

            if (!failingTest && testCommandSucceeded) {
               try {

                  ObjectMapper mapper = new BatfishObjectMapper(
                        getCurrentClassLoader());

                  // rewrite new answer string using local implementation
                  String testOutput = CommonUtil
                        .readFile(Paths.get(testoutFile.getAbsolutePath()));

                  Answer testAnswer = mapper.readValue(testOutput,
                        Answer.class);
                  String testAnswerString = mapper
                        .writeValueAsString(testAnswer);

                  if (!missingReferenceFile) {
                     String referenceOutput = CommonUtil
                           .readFile(Paths.get(referenceFileName));

                     // rewrite reference string using local implementation
                     Answer referenceAnswer;
                     try {
                        referenceAnswer = mapper.readValue(referenceOutput,
                              Answer.class);
                     }
                     catch (Exception e) {
                        throw new BatfishException(
                              "Error reading reference output using current schema (reference output is likely obsolete)",
                              e);
                     }
                     String referenceAnswerString = mapper
                           .writeValueAsString(referenceAnswer);

                     // due to options chosen in BatfishObjectMapper, if json
                     // outputs were equal, then strings should be equal

                     if (referenceAnswerString.equals(testAnswerString)) {
                        testPassed = true;
                     }
                  }
               }
               catch (Exception e) {
                  _logger.errorf("Exception in comparing test results: "
                        + ExceptionUtils.getStackTrace(e));
               }
            }
            else if (failingTest) {
               testPassed = !testCommandSucceeded;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("'" + testCommand[0]);
            for (int i = 1; i < testCommand.length; i++) {
               sb.append(" " + testCommand[i]);
            }
            sb.append("'");
            String testCommandText = sb.toString();

            String message = "Test: " + testCommandText
                  + (failingTest ? " results in error as expected"
                        : " matches " + referenceFileName)
                  + (testPassed ? ": Pass\n" : ": Fail\n");

            _logger.output(message);
            if (!failingTest) {
               if (!testPassed) {
                  String outFileName = referenceFile + ".testout";
                  Files.move(Paths.get(testoutFile.getAbsolutePath()),
                        Paths.get(referenceFile + ".testout"),
                        StandardCopyOption.REPLACE_EXISTING);
                  _logger.outputf("Copied output to %s\n", outFileName);
               }
            }
            return true;
         }
         case UPLOAD_CUSTOM_OBJECT: {
            if (!isSetTestrig() || !isSetContainer(true)) {
               return false;
            }

            String objectName = parameters.get(0);
            String objectFile = parameters.get(1);

            // upload the object
            return _workHelper.uploadCustomObject(_currContainerName,
                  _currTestrig, objectName, objectFile);
         }
         default:
            _logger.error("Unsupported command " + words[0] + "\n");
            _logger.error("Type 'help' to see the list of valid commands\n");
            return false;
         }
      }
      catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   private boolean processCommands(List<String> commands) {
      for (String command : commands) {
         if (!processCommand(command)) {
            return false;
         }
      }
      return true;
   }

   public void run(List<String> initialCommands) {
      loadPlugins();
      initHelpers();

      _logger.debugf("Will use coordinator at %s://%s\n",
            (_settings.getUseSsl()) ? "https" : "http",
            _settings.getCoordinatorHost());

      if (!processCommands(initialCommands)) {
         return;
      }

      // set container if specified
      if (_settings.getContainerId() != null) {
         if (!processCommand(Command.SET_CONTAINER.commandName() + "  "
               + _settings.getContainerId())) {
            return;
         }
      }

      // set testrig if dir or id is specified
      if (_settings.getTestrigDir() != null) {
         if (_settings.getTestrigId() != null) {
            System.err.println(
                  "org.batfish.client: Cannot supply both testrigDir and testrigId.");
            System.exit(1);
         }
         if (!processCommand(Command.INIT_TESTRIG.commandName() + " "
               + _settings.getTestrigDir())) {
            return;
         }
      }
      if (_settings.getTestrigId() != null) {
         if (!processCommand(Command.SET_TESTRIG.commandName() + "  "
               + _settings.getTestrigId())) {
            return;
         }
      }

      switch (_settings.getRunMode()) {
      case batch:
         List<String> commands = null;
         try {
            commands = Files.readAllLines(
                  Paths.get(_settings.getBatchCommandFile()),
                  StandardCharsets.US_ASCII);
         }
         catch (Exception e) {
            System.err.printf("Exception reading command file %s: %s\n",
                  _settings.getBatchCommandFile(), e.getMessage());
            System.exit(1);
         }
         boolean result = processCommands(commands);

         if (!result) {
            System.exit(1);
         }

         break;
      case gendatamodel:
         generateDatamodel();
         break;
      case genquestions:
         generateQuestions();
         break;
      case interactive:
         runInteractive();
         break;
      default:
         System.err.println("org.batfish.client: Unknown run mode.");
         System.exit(1);
      }
   }

   private void runInteractive() {
      try {

         String rawLine;
         while ((rawLine = _reader.readLine()) != null) {
            String line = rawLine.trim();
            if (line.length() == 0 || line.startsWith("#")) {
               continue;
            }

            if (line.equals(Command.CLEAR_SCREEN)) {
               _reader.clearScreen();
               continue;
            }

            String[] words = line.split("\\s+");

            if (words.length > 0) {
               if (validCommandUsage(words)) {
                  processCommand(words, null);
               }
            }
         }
      }
      catch (Throwable t) {
         t.printStackTrace();
      }
   }

   private boolean uploadTestrigOrEnv(String fileOrDir, String testrigOrEnvName,
         boolean isTestrig) throws Exception {

      File filePointer = new File(fileOrDir);

      String uploadFilename = fileOrDir;

      if (filePointer.isDirectory()) {
         File uploadFile = File.createTempFile("testrigOrEnv", "zip");
         uploadFile.deleteOnExit();
         uploadFilename = uploadFile.getAbsolutePath();
         ZipUtility.zipFiles(filePointer.getAbsolutePath(), uploadFilename);
      }

      boolean result = (isTestrig)
            ? _workHelper.uploadTestrig(_currContainerName, testrigOrEnvName,
                  uploadFilename)
            : _workHelper.uploadEnvironment(_currContainerName, _currTestrig,
                  testrigOrEnvName, uploadFilename);

      // unequal means we must have created a temporary file
      if (uploadFilename != fileOrDir) {
         new File(uploadFilename).delete();
      }

      return result;
   }

   private boolean validCommandUsage(String[] words) {
      return true;
   }

}
