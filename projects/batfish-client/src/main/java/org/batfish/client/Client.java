package org.batfish.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.history.FileHistory;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.client.answer.LoadQuestionAnswerElement;
import org.batfish.client.config.Settings;
import org.batfish.client.config.Settings.RunMode;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Pair;
import org.batfish.common.Task;
import org.batfish.common.Task.Batch;
import org.batfish.common.Version;
import org.batfish.common.WorkItem;
import org.batfish.common.plugin.AbstractClient;
import org.batfish.common.plugin.IClient;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ZipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.questions.IEnvironmentCreationQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.Question.InstanceData;
import org.batfish.datamodel.questions.Question.InstanceData.Variable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

public class Client extends AbstractClient implements IClient {

   private static final Set<String> COMPARATORS = new HashSet<>(
         Arrays.asList(">", ">=", "==", "!=", "<", "<="));

   private static final String DEFAULT_CONTAINER_PREFIX = "cp";

   private static final String DEFAULT_DELTA_ENV_PREFIX = "env_";

   private static final String DEFAULT_ENV_NAME = BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME;

   private static final String DEFAULT_QUESTION_PREFIX = "q";

   private static final String DEFAULT_TESTRIG_PREFIX = "tr_";

   private static final String DIFF_NOT_READY_MSG =
         "Cannot ask differential question without first setting delta testrig/environment\n";

   private static final String ENV_HOME = "HOME";

   private static final String FLAG_FAILING_TEST = "-error";

   private static final String HISTORY_FILE = ".batfishclient_history";

   private static final int NUM_TRIES_WARNING_THRESHOLD = 5;

   private static final String STARTUP_FILE = ".batfishclientrc";

   /**
    * Verify that every non-optional variable has value assigned to it.
    *
    * @throws BatfishException
    *            when there exists a missing parameter: it is not optional in
    *            {@code variable}, but the user failed to provide it.
    */
   static void checkVariableState(Map<String, Variable> variables)
         throws BatfishException {
      for (Entry<String, Variable> e : variables.entrySet()) {
         String variableName = e.getKey();
         Variable variable = e.getValue();
         if (!variable.getOptional() && variable.getValue() == null) {
            throw new BatfishException(
                  String.format("Missing parameter: %s", variableName));
         }
      }
   }

   /**
    * Parse contents of {@code parameterValue} to build a {@link JsonNode
    * JsonNode}.
    *
    * @return a json-encoded {@link JsonNode node} that contains the information
    *         in {@code parameterValue}.
    *
    * @throws BatfishException
    *            if contents of {@code parameterValue} is not valid JSON.
    */
   static JsonNode parseParaValue(String parameterName, String parameterValue)
         throws BatfishException {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode value;
      try {
         value = mapper.readTree(parameterValue);
      }
      catch (IOException e1) {
         try {
            value = mapper.valueToTree(parameterValue);
         }
         catch (IllegalArgumentException e2) {
            throw new BatfishException(
                  String.format("Variable value \"%s\" is not valid JSON", parameterValue), e2);
         }
      }
      return value;
   }

   /**
    * For each key in {@code parameters}, validate that its value satisfies the
    * requirements specified by {@code variables} for that specific key. Set
    * value to {@code variables} if validation passed.
    *
    * @throws BatfishException
    *            if the key in parameters does not exist in variable, or the
    *            values in {@code parameters} do not match the requirements in
    *            {@code variables} for that specific key.
    */
   static void validateAndSet(
         Map<String, String> parameters,
         Map<String, Variable> variables) throws BatfishException {
      for (Entry<String, String> e : parameters.entrySet()) {
         String parameterName = e.getKey();
         String parameterValue = e.getValue();
         Variable variable = variables.get(parameterName);
         if (variable == null) {
            throw new BatfishException("No variable named: '" + parameterName
                  + "' in supplied question template");
         }
         JsonNode value = parseParaValue(parameterName, parameterValue);
         if (variable.getMinElements() != null) {
            // Value is an array, check size and validate each elements in it
            if (!value.isArray() || value.size() < variable.getMinElements()) {
               throw new BatfishException(String.format(
                     "Invalid value for parameter %s: %s. "
                           + "Expecting a JSON array of at least %d "
                           + "elements",
                     parameterName, parameterValue, variable.getMinElements()));
            }
            for (JsonNode node : value) {
               validateNode(node, variable, parameterName);
            }
         }
         else {
            validateNode(value, variable, parameterName);
         }
         // validation passed.
         variable.setValue(value);
      }
   }

   /**
    * Validate that json-encoded {@code jsonPath} is a valid jsonPath dictionary
    * (A valid jsonPath contains key 'path' which mapping to a String, and an
    * optional key 'suffix' which mapping to a boolean value).
    *
    * @throws BatfishException
    *            if {@code jsonPath} is not a valid jsonPath dictionary.
    */
   static void validateJsonPath(JsonNode jsonPath) throws BatfishException {
      if (!jsonPath.isContainerNode()) {
         throw new BatfishException(
               String.format(
                     "Expecting a JSON dictionary for a Batfish %s",
                     Variable.Type.JSON_PATH.getName()));
      }
      if (jsonPath.get("path") == null) {
         throw new BatfishException(
               String.format(
                     "Missing 'path' element of %s",
                     Variable.Type.JSON_PATH.getName()));
      }
      if (!jsonPath.get("path").isTextual()) {
         throw new BatfishException(
               String.format(
                     "'path' element of %s must be a JSON string",
                     Variable.Type.JSON_PATH.getName()));
      }
      if (jsonPath.get("suffix") != null
            && !jsonPath.get("suffix").isBoolean()) {
         throw new BatfishException(
               String.format(
                     "'suffix' element of %s must be a JSON boolean",
                     Variable.Type.JSON_PATH.getName()));
      }
   }

   /**
    * Validate that {@code jsonPathRegex} contains a valid Java regular
    * expression of a {@code JsonPath} (Starts with "/", ends with either "/" or
    * "/i", contains a valid Java regular expression between "/").
    *
    * <p>
    * As written, this function will accept the strings "/" and "/i" as complete
    * expressions – resulting in an empty inner Java regular expression.
    * </p>
    *
    * @throws BatfishException
    *            if the content of {@code jsonPathRegex} is not a valid Java
    *            regular expression of a JsonPath.
    */
   static void validateJsonPathRegex(String jsonPathRegex)
         throws BatfishException {
      if (!jsonPathRegex.startsWith("/")) {
         throw new BatfishException(
               String.format(
                     "A Batfish %s must start with \"/\"",
                     Variable.Type.JSON_PATH_REGEX.getName()));
      }
      if (!(jsonPathRegex.endsWith("/") || jsonPathRegex.endsWith("/i"))) {
         throw new BatfishException(
               String.format(
                     "A Batfish %s must end in either \"/\" or \"/i\"",
                     Variable.Type.JSON_PATH_REGEX.getName()));
      }
      String innerPath = "";
      if (jsonPathRegex.lastIndexOf('/') > 0) {
         innerPath = jsonPathRegex.substring(1, jsonPathRegex.lastIndexOf('/'));
      }
      try {
         Pattern.compile(innerPath);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               String.format(
                     "Invalid %s at interior of %s",
                     Variable.Type.JAVA_REGEX.getName(),
                     Variable.Type.JSON_PATH_REGEX.getName()),
               e);
      }
   }

   /**
    * This method calls {@link Client#validateType(JsonNode, Variable)} to check
    * that the contents encoded in {@code value} match the requirement specified
    * in {@code variable}.
    */
   static void validateNode(
         JsonNode value, Variable variable,
         String parameterName) throws BatfishException {
      try {
         validateType(value, variable);
      }
      catch (BatfishException e) {
         String errorMessage = String.format(
               "Invalid value for parameter %s: %s", parameterName, value);
         throw new BatfishException(errorMessage, e);
      }
   }

   /**
    * Validate the contents contained in json-encoded {@code value} matches the
    * type required by {@code variable}, and the length of input string meets
    * the requirement of minimum length if specified in {@code variable}. Call
    * {@link Variable#getType()} on {@code variable} gives the expected type.
    *
    * @throws BatfishException
    *            if the content encoded in input {@code value} does not satisfy
    *            the requirements specified in {@code variable}.
    */
   static void validateType(JsonNode value, Variable variable)
         throws BatfishException {
      int minLength = variable.getMinLength() == null ? 0
            : variable.getMinLength();
      if (value.isTextual() && value.textValue().length() < minLength) {
         throw new BatfishException(String
               .format("Must be at least %s characters in length", minLength));
      }
      Variable.Type expectedType = variable.getType();
      switch (expectedType) {
      case BOOLEAN:
         if (!value.isBoolean()) {
            throw new BatfishException(String.format(
                  "It is not a valid JSON %s value", expectedType.getName()));
         }
         break;
      case COMPARATOR:
         if (!(COMPARATORS.contains(value.textValue()))) {
            throw new BatfishException(String.format(
                  "It is not a known %s. Valid options are:" + " %s",
                  expectedType.getName(), COMPARATORS));
         }
         break;
      case DOUBLE:
         if (!value.isDouble()) {
            throw new BatfishException(String.format(
                  "It is not a valid JSON %s value", expectedType.getName()));
         }
         break;
      case FLOAT:
         if (!value.isFloat()) {
            throw new BatfishException(String.format(
                  "It is not a valid JSON %s value", expectedType.getName()));
         }
         break;
      case INTEGER:
         if (!value.isInt()) {
            throw new BatfishException(String.format(
                  "It is not a valid JSON %s value", expectedType.getName()));
         }
         break;
      case LONG:
         if (!value.isLong()) {
            throw new BatfishException(String.format(
                  "It is not a valid JSON %s value", expectedType.getName()));
         }
         break;
      case IP:
         // TODO: Need to double check isInetAddress()
         if (!(value.isTextual())) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         new Ip(value.textValue());
         break;
      case IP_PROTOCOL:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         try {
            IpProtocol.fromString(value.textValue());
         }
         catch (IllegalArgumentException e) {
            throw new BatfishException(
                  String.format("Unknown %s string", expectedType.getName()));
         }

         break;
      case IP_WILDCARD:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         new IpWildcard(value.textValue());
         break;
      case JAVA_REGEX:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         try {
            Pattern.compile(value.textValue());
         }
         catch (PatternSyntaxException e) {
            throw new BatfishException(
                  "It is not a valid Java regular " + "expression", e);
         }
         break;
      case JSON_PATH_REGEX:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         validateJsonPathRegex(value.textValue());
         break;
      case PREFIX:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         new Prefix(value.textValue());
         break;
      case PREFIX_RANGE:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         new PrefixRange(value.textValue());
         break;
      case QUESTION:
         // TODO: Implement
         break;

      case STRING:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         break;
      case SUBRANGE:
         if (!(value.isTextual() || value.isInt())) {
            throw new BatfishException(String.format(
                  "A Batfish %s must be a JSON string or " + "integer",
                  expectedType.getName()));
         }
         Object actualValue = value.isTextual() ? value.textValue()
               : value.asInt();
         new SubRange(actualValue);
         break;
      case PROTOCOL:
         if (!value.isTextual()) {
            throw new BatfishException(
                  String.format(
                        "A Batfish %s must be a JSON string",
                        expectedType.getName()));
         }
         Protocol.fromString(value.textValue());
         break;
      case JSON_PATH:
         validateJsonPath(value);
         break;
      default:
         throw new BatfishException(
               String.format("Unsupported parameter type: %s", expectedType));
      }
   }

   private Map<String, String> _additionalBatfishOptions;

   private final Map<String, String> _bfq;

   private String _currContainerName = null;

   private String _currDeltaEnv = null;

   private String _currDeltaTestrig;

   private String _currEnv = null;

   private String _currTestrig = null;

   private boolean _exit;

   private BatfishLogger _logger;

   @SuppressWarnings("unused")
   private BfCoordPoolHelper _poolHelper;

   private ConsoleReader _reader;

   private Settings _settings;

   private BfCoordWorkHelper _workHelper;

   public Client(Settings settings) {
      super(false, settings.getPluginDirs());
      _additionalBatfishOptions = new HashMap<>();
      _bfq = new TreeMap<>();
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
            System.err.printf(
                  "Use '-%s <cmdfile>'\n",
                  Settings.ARG_QUESTIONS_DIR);
            System.exit(1);
         }
         _logger = new BatfishLogger(_settings.getLogLevel(), false,
               _settings.getLogFile(), false, false);
         break;
      case interactive:
         try {
            _reader = new ConsoleReader();
            Path historyPath = Paths.get(System.getenv(ENV_HOME), HISTORY_FILE);
            historyPath.toFile().createNewFile();
            FileHistory history = new FileHistory(historyPath.toFile());
            _reader.setHistory(history);
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
            System.err.printf(
                  "Could not initialize client: %s\n",
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

   private boolean addBatfishOption(
         String[] words, List<String> options,
         List<String> parameters) {
      String optionKey = parameters.get(0);
      String optionValue = String.join(
            " ",
            Arrays.copyOfRange(words, 2 + options.size(), words.length));
      _additionalBatfishOptions.put(optionKey, optionValue);
      return true;
   }

   private boolean answer(
         String questionTemplateName, String paramsLine,
         boolean isDelta, FileWriter outWriter) {
      String questionName = DEFAULT_QUESTION_PREFIX + "_"
            + UUID.randomUUID().toString();
      String questionContentUnmodified = _bfq
            .get(questionTemplateName.toLowerCase());
      if (questionContentUnmodified == null) {
         throw new BatfishException("Invalid question template name: '"
               + questionTemplateName + "'");
      }
      Map<String, String> parameters = parseParams(paramsLine);
      JSONObject questionJson;
      try {
         questionJson = new JSONObject(questionContentUnmodified);
      }
      catch (JSONException e) {
         throw new BatfishException("Question content is not valid JSON", e);
      }
      JSONObject instanceJson;
      try {
         instanceJson = questionJson.getJSONObject(BfConsts.INSTANCE_VAR);
      }
      catch (JSONException e) {
         throw new BatfishException("Question is missing instance data", e);
      }
      String instanceDataStr = instanceJson.toString();
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      InstanceData instanceData;
      try {
         instanceData = mapper.<InstanceData>readValue(
               instanceDataStr,
               new TypeReference<InstanceData>() {
               });
      }
      catch (IOException e) {
         throw new BatfishException("Invalid instance data (JSON)", e);
      }
      Map<String, Variable> variables = instanceData.getVariables();
      validateAndSet(parameters, variables);
      checkVariableState(variables);

      String modifiedInstanceDataStr;
      try {
         modifiedInstanceDataStr = mapper.writeValueAsString(instanceData);
         JSONObject modifiedInstanceData = new JSONObject(
               modifiedInstanceDataStr);
         questionJson.put(BfConsts.INSTANCE_VAR, modifiedInstanceData);
      }
      catch (JSONException | JsonProcessingException e) {
         throw new BatfishException(
               "Could not process modified instance data",
               e);
      }
      String modifiedQuestionStr = questionJson.toString();
      boolean questionJsonDifferential;
      // check whether question is valid modulo instance data
      try {
         questionJsonDifferential = questionJson.has(BfConsts.DIFFERENTIAL_VAR)
               && questionJson.getBoolean(BfConsts.DIFFERENTIAL_VAR);
      }
      catch (JSONException e) {
         throw new BatfishException(
               "Could not find whether question is explicitly differential", e);
      }
      if (questionJsonDifferential
            && (_currDeltaEnv == null || _currDeltaTestrig == null)) {
         _logger.output(DIFF_NOT_READY_MSG);
         return false;
      }
      Path questionFile = createTempFile(
            BfConsts.RELPATH_QUESTION_FILE,
            modifiedQuestionStr);
      questionFile.toFile().deleteOnExit();
      // upload the question
      boolean resultUpload = _workHelper.uploadQuestion(_currContainerName,
            isDelta ? _currDeltaTestrig : _currTestrig, questionName,
            questionFile.toAbsolutePath().toString());
      if (!resultUpload) {
         return false;
      }
      _logger.debug("Uploaded question. Answering now.\n");
      // delete the temporary params file
      if (questionFile != null) {
         CommonUtil.deleteIfExists(questionFile);
      }
      // answer the question
      WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(questionName,
            _currContainerName, _currTestrig, _currEnv, _currDeltaTestrig,
            _currDeltaEnv, isDelta);
      return execute(wItemAs, outWriter);
   }

   private boolean answer(
         String[] words, FileWriter outWriter,
         List<String> options, List<String> parameters, boolean isDelta) {
      if (!isSetTestrig() || !isSetContainer(true)
            || (isDelta && !isSetDeltaEnvironment())) {
         return false;
      }
      String qTypeStr = parameters.get(0);
      String paramsLine = String.join(
            " ",
            Arrays.copyOfRange(words, 2 + options.size(), words.length));
      return answer(qTypeStr, paramsLine, isDelta, outWriter);
   }

   private boolean answerFile(
         Path questionFile, boolean isDelta,
         FileWriter outWriter) {

      if (!Files.exists(questionFile)) {
         throw new BatfishException("Question file not found: " + questionFile);
      }

      String questionName = DEFAULT_QUESTION_PREFIX + "_"
            + UUID.randomUUID().toString();

      // upload the question
      boolean resultUpload = _workHelper.uploadQuestion(_currContainerName,
            isDelta ? _currDeltaTestrig : _currTestrig, questionName,
            questionFile.toAbsolutePath().toString());

      if (!resultUpload) {
         return false;
      }

      _logger.debug("Uploaded question. Answering now.\n");

      // answer the question
      WorkItem wItemAs = _workHelper.getWorkItemAnswerQuestion(questionName,
            _currContainerName, _currTestrig, _currEnv, _currDeltaTestrig,
            _currDeltaEnv, isDelta);

      return execute(wItemAs, outWriter);
   }

   private boolean answerType(
         String questionType, String paramsLine,
         boolean isDelta, FileWriter outWriter) {
      JSONObject questionJson;
      if (questionType.startsWith(QuestionHelper.MACRO_PREFIX)) {
         try {
            String questionString = QuestionHelper.resolveMacro(questionType,
                  paramsLine, _questions);
            questionJson = new JSONObject(questionString);
         }
         catch (JSONException e) {
            throw new BatfishException(
                  "Failed to convert unmodified question string to JSON", e);
         }
         catch (BatfishException e) {
            _logger.errorf("Could not resolve macro: %s\n", e.getMessage());
            return false;
         }
      }
      else {
         try {
            String questionString = QuestionHelper.getQuestionString(questionType, _questions, false);
            questionJson = new JSONObject(questionString);

            Map<String, String> parameters = parseParams(paramsLine);
            for (Entry<String, String> e : parameters.entrySet()) {
               String parameterName = e.getKey();
               String parameterValue = e.getValue();
               Object parameterObj;
               try {
                  parameterObj = new JSONTokener(parameterValue).nextValue();
                  questionJson.put(parameterName, parameterObj);
               }
               catch (JSONException e1) {
                  throw new BatfishException("Failed to apply parameter: '"
                        + parameterName + "' with value: '" + parameterValue
                        + "' to question JSON", e1);
               }
            }

         }
         catch (JSONException e) {
            throw new BatfishException(
                  "Failed to convert unmodified question string to JSON", e);
         }
         catch (BatfishException e) {
            _logger.errorf("Could not construct a question: %s\n", e.getMessage());
            return false;
         }
      }

      String modifiedQuestionJson = questionJson.toString();
      BatfishObjectMapper mapper = new BatfishObjectMapper(
            getCurrentClassLoader());
      Question modifiedQuestion = null;
      try {
         modifiedQuestion = mapper.readValue(
               modifiedQuestionJson,
               Question.class);
      }
      catch (IOException e) {
         throw new BatfishException(
               "Modified question is no longer valid, likely due to invalid parameters",
               e);
      }
      if (modifiedQuestion.getDifferential()
            && (_currDeltaEnv == null || _currDeltaTestrig == null)) {
         _logger.output(DIFF_NOT_READY_MSG);
         return false;
      }
      // if no exception is thrown, then the modifiedQuestionJson is good
      Path questionFile = createTempFile("question", modifiedQuestionJson);
      questionFile.toFile().deleteOnExit();
      boolean result = answerFile(questionFile, isDelta, outWriter);
      if (questionFile != null) {
         CommonUtil.deleteIfExists(questionFile);
      }
      return result;
   }

   private boolean cat(String[] words)
         throws IOException, FileNotFoundException {
      String filename = words[1];

      try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
         String line = null;
         while ((line = br.readLine()) != null) {
            _logger.output(line + "\n");
         }
      }

      return true;
   }

   private boolean checkApiKey() {
      String isValid = _workHelper.checkApiKey();
      _logger.outputf("Api key validitiy: %s\n", isValid);
      return true;
   }

   private boolean clearScreen() throws IOException {
      _reader.clearScreen();
      return false;
   }

   private Path createTempFile(String filePrefix, String content) {
      Path tempFilePath;
      try {
         tempFilePath = Files.createTempFile(filePrefix, null);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to create temporary file", e);
      }
      File tempFile = tempFilePath.toFile();
      tempFile.deleteOnExit();
      _logger.debugf("Creating temporary %s file: %s\n", filePrefix,
            tempFilePath.toAbsolutePath().toString());
      FileWriter writer;
      try {
         writer = new FileWriter(tempFile);
         writer.write(content + "\n");
         writer.close();
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to write content to temporary file",
               e);
      }
      return tempFilePath;
   }

   private boolean delAnalysis(
         FileWriter outWriter, List<String> options,
         List<String> parameters) {
      if (!isSetContainer(true)) {
         return false;
      }
      if (options.size() != 0 || parameters.size() != 1) {
         _logger.errorf("Invalid arguments: %s %s\n", options.toString(),
               parameters.toString());
         printUsage(Command.DEL_ANALYSIS);
         return false;
      }

      String analysisName = parameters.get(0);

      boolean result = _workHelper.delAnalysis(
            _currContainerName,
            analysisName);

      logOutput(outWriter, "Result of deleting analysis " + analysisName + ": "
            + result + "\n");
      return result;
   }

   private boolean delAnalysisQuestions(
         FileWriter outWriter,
         List<String> options, List<String> parameters) {
      if (!isSetContainer(true)) {
         return false;
      }
      if (options.size() != 0 || parameters.size() < 2) {
         _logger.errorf("Invalid arguments: %s %s\n", options.toString(),
               parameters.toString());
         printUsage(Command.DEL_ANALYSIS_QUESTIONS);
         return false;
      }

      String analysisName = parameters.get(0);

      String delQuestionsStr = "[]";

      try {
         JSONArray delQuestionsArray = new JSONArray();
         for (int index = 1; index < parameters.size(); index++) {
            delQuestionsArray.put(parameters.get(index));
         }
         delQuestionsStr = delQuestionsArray.toString(1);
      }
      catch (JSONException e) {
         throw new BatfishException("Failed to get JSONObject for analysis", e);
      }

      boolean result = _workHelper.configureAnalysis(_currContainerName, false,
            analysisName, null, delQuestionsStr);

      logOutput(
            outWriter,
            "Result of deleting analysis questions: " + result + "\n");
      return result;
   }

   private boolean delBatfishOption(List<String> parameters) {
      String optionKey = parameters.get(0);

      if (!_additionalBatfishOptions.containsKey(optionKey)) {
         _logger.outputf("Batfish option %s does not exist\n", optionKey);
         return false;
      }
      _additionalBatfishOptions.remove(optionKey);
      return true;
   }

   private boolean delContainer(List<String> parameters) {
      String containerName = parameters.get(0);
      boolean result = _workHelper.delContainer(containerName);
      _logger.outputf("Result of deleting container: %s\n", result);
      return true;
   }

   private boolean delEnvironment(List<String> parameters) {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      String envName = parameters.get(0);
      boolean result = _workHelper.delEnvironment(_currContainerName,
            _currTestrig, envName);
      _logger.outputf("Result of deleting environment: %s\n", result);
      return true;
   }

   private boolean delQuestion(List<String> parameters) {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      String qName = parameters.get(0);
      boolean result = _workHelper.delQuestion(_currContainerName, _currTestrig,
            qName);
      _logger.outputf("Result of deleting question: %s\n", result);
      return true;
   }

   private boolean delTestrig(FileWriter outWriter, List<String> parameters) {
      if (!isSetContainer(true)) {
         return false;
      }

      String testrigName = parameters.get(0);
      boolean result = _workHelper.delTestrig(_currContainerName, testrigName);
      logOutput(outWriter, "Result of deleting testrig: " + result + "\n");
      return true;
   }

   private boolean dir(List<String> parameters) {
      String dirname = (parameters.size() == 1) ? parameters.get(0) : ".";
      File currDirectory = new File(dirname);
      for (File file : currDirectory.listFiles()) {
         _logger.output(file.getName() + "\n");
      }
      return true;
   }

   private boolean echo(String[] words) {
      _logger.outputf(
            "%s\n",
            String.join(" ", Arrays.copyOfRange(words, 1, words.length)));
      return true;
   }

   private boolean execute(WorkItem wItem, FileWriter outWriter) {
      _logger.info("work-id is " + wItem.getId() + "\n");
      wItem.addRequestParam(
            BfConsts.ARG_LOG_LEVEL,
            _settings.getBatfishLogLevel());
      for (String option : _additionalBatfishOptions.keySet()) {
         wItem.addRequestParam(option, _additionalBatfishOptions.get(option));
      }
      boolean queueWorkResult = _workHelper.queueWork(wItem);
      _logger.info("Queuing result: " + queueWorkResult + "\n");
      if (!queueWorkResult) {
         return queueWorkResult;
      }
      Pair<WorkStatusCode, String> response = _workHelper
            .getWorkStatus(wItem.getId());
      if (response == null) {
         return false;
      }
      WorkStatusCode status = response.getFirst();
      while (status != WorkStatusCode.TERMINATEDABNORMALLY
            && status != WorkStatusCode.TERMINATEDNORMALLY
            && status != WorkStatusCode.ASSIGNMENTERROR) {
         printWorkStatusResponse(response);
         try {
            Thread.sleep(1 * 1000);
         }
         catch (InterruptedException e) {
            throw new BatfishException(
                  "Interrupted while waiting for response",
                  e);
         }
         response = _workHelper.getWorkStatus(wItem.getId());
         if (response == null) {
            return false;
         }
         status = response.getFirst();
      }
      printWorkStatusResponse(response);
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
            Answer answer;
            try {
               answer = mapper.readValue(answerString, Answer.class);
            }
            catch (IOException e) {
               throw new BatfishException(
                     "Response does not appear to be valid JSON representation of "
                           + Answer.class.getSimpleName(),
                     e);
            }
            answerStringToPrint = answer.prettyPrint();
         }

         logOutput(outWriter, answerStringToPrint);

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
               _logger.outputf(
                     "Could NOT deserialize Json to Answer: %s\n",
                     e.getMessage());
            }
         }
      }
      // get and print the log when in debugging mode
      if (_logger.getLogLevel() >= BatfishLogger.LEVEL_DEBUG) {
         _logger.output("---------------- Service Log --------------\n");
         String logFileName = wItem.getId() + BfConsts.SUFFIX_LOG_FILE;
         String downloadedFileStr = _workHelper.getObject(
               wItem.getContainerName(), wItem.getTestrigName(), logFileName);

         if (downloadedFileStr == null) {
            _logger.errorf("Failed to get log file %s\n", logFileName);
            return false;
         }
         else {
            Path downloadedFile = Paths.get(downloadedFileStr);
            CommonUtil.outputFileLines(downloadedFile, _logger::output);
         }
      }
      if (response.getFirst() == WorkStatusCode.TERMINATEDNORMALLY) {
         return true;
      }
      else {
         // _logger.errorf("WorkItem failed: %s", wItem);
         return false;
      }
   }

   private boolean exit() {
      _exit = true;
      return true;
   }

   private void generateDatamodel() {
      try {
         ObjectMapper mapper = new BatfishObjectMapper();
         JsonSchemaGenerator schemaGenNew = new JsonSchemaGenerator(mapper);
         JsonNode schemaNew = schemaGenNew
               .generateJsonSchema(Configuration.class);
         _logger.output(mapper.writeValueAsString(schemaNew));

         // Reflections reflections = new Reflections("org.batfish.datamodel");
         // Set<Class<? extends AnswerElement>> classes =
         // reflections.getSubTypesOf(AnswerElement.class);
         // _logger.outputf("Found %d classes that inherit %s\n",
         // classes.toArray().length, "AnswerElement");
         //
         // File dmDir = Paths.get(_settings.getDatamodelDir()).toFile();
         // if (!dmDir.exists()) {
         // if (!dmDir.mkdirs()) {
         // throw new BatfishException("Could not create directory: " +
         // dmDir.getAbsolutePath());
         // }
         // }
         //
         // for (Class c : classes) {
         // String className = c.getCanonicalName()
         // .replaceAll("org\\.batfish\\.datamodel\\.", "")
         // .replaceAll("\\.", "-")
         // + ".json";
         // _logger.outputf("%s --> %s\n", c, className);
         // Path file = Paths.get(dmDir.getAbsolutePath(), className);
         // try (PrintWriter out = new
         // PrintWriter(file.toAbsolutePath().toString())) {
         // ObjectMapper mapper = new BatfishObjectMapper();
         // JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
         // JsonNode schema = schemaGen.generateJsonSchema(c);
         // String schemaString = mapper.writeValueAsString(schema);
         // out.println(schemaString);
         // }
         // }

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
            _logger.errorf(
                  "Could not create questions dir %s\n",
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

   private boolean get(
         String[] words, FileWriter outWriter,
         List<String> options, List<String> parameters, boolean isDelta)
         throws Exception {
      if (!isSetTestrig() || !isSetContainer(true)
            || (isDelta && !isSetDeltaEnvironment())) {
         return false;
      }
      String qTypeStr = parameters.get(0).toLowerCase();
      String paramsLine = String.join(
            " ",
            Arrays.copyOfRange(words, 2 + options.size(), words.length));
      // TODO: make environment creation a command, not a question
      if (!qTypeStr.startsWith(QuestionHelper.MACRO_PREFIX)
            && qTypeStr.equals(IEnvironmentCreationQuestion.NAME)) {

         String deltaEnvName = DEFAULT_DELTA_ENV_PREFIX
               + UUID.randomUUID().toString();

         String prefixString = (paramsLine.trim().length() > 0) ? ", " : "";
         paramsLine += String.format("%s %s='%s'", prefixString,
               IEnvironmentCreationQuestion.ENVIRONMENT_NAME_KEY, deltaEnvName);

         if (!answerType(qTypeStr, paramsLine, isDelta, outWriter)) {
            unsetTestrig(true);
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

   private boolean getAnalysisAnswers(
         FileWriter outWriter,
         List<String> options, List<String> parameters, boolean delta,
         boolean differential) {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }
      if (options.size() != 0 || parameters.size() != 1) {
         _logger.errorf("Invalid arguments: %s %s\n", options.toString(),
               parameters.toString());
         printUsage(Command.GET_ANALYSIS_ANSWERS);
         return false;
      }

      String analysisName = parameters.get(0);

      String baseTestrig;
      String baseEnvironment;
      String deltaTestrig;
      String deltaEnvironment;
      if (differential) {
         baseTestrig = _currTestrig;
         baseEnvironment = _currEnv;
         deltaTestrig = _currDeltaTestrig;
         deltaEnvironment = _currDeltaEnv;
      }
      else if (delta) {
         baseTestrig = _currDeltaTestrig;
         baseEnvironment = _currDeltaEnv;
         deltaTestrig = null;
         deltaEnvironment = null;
      }
      else {
         baseTestrig = _currTestrig;
         baseEnvironment = _currEnv;
         deltaTestrig = null;
         deltaEnvironment = null;
      }
      String answer = _workHelper.getAnalysisAnswers(_currContainerName,
            baseTestrig, baseEnvironment, deltaTestrig, deltaEnvironment,
            analysisName);

      if (answer == null) {
         return false;
      }

      logOutput(outWriter, answer + "\n");

      return true;
   }

   private boolean getAnswer(
         FileWriter outWriter, List<String> parameters,
         boolean delta, boolean differential) {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }
      if (parameters.size() != 1) {
         _logger.error("Invalid arguments: " + parameters.toString());
         printUsage(Command.GET_ANSWER);
         return false;
      }

      String questionName = parameters.get(0);

      String baseTestrig;
      String baseEnvironment;
      String deltaTestrig;
      String deltaEnvironment;
      if (differential) {
         baseTestrig = _currTestrig;
         baseEnvironment = _currEnv;
         deltaTestrig = _currDeltaTestrig;
         deltaEnvironment = _currDeltaEnv;
      }
      else if (delta) {
         baseTestrig = _currDeltaTestrig;
         baseEnvironment = _currDeltaEnv;
         deltaTestrig = null;
         deltaEnvironment = null;
      }
      else {
         baseTestrig = _currTestrig;
         baseEnvironment = _currEnv;
         deltaTestrig = null;
         deltaEnvironment = null;
      }
      String answerString = _workHelper.getAnswer(_currContainerName,
            baseTestrig, baseEnvironment, deltaTestrig, deltaEnvironment,
            questionName);

      String answerStringToPrint = answerString;
      if (outWriter == null && _settings.getPrettyPrintAnswers()) {
         ObjectMapper mapper = new BatfishObjectMapper(getCurrentClassLoader());
         Answer answer;
         try {
            answer = mapper.readValue(answerString, Answer.class);
         }
         catch (IOException e) {
            throw new BatfishException(
                  "Response does not appear to be valid JSON representation of "
                        + Answer.class.getSimpleName());
         }
         answerStringToPrint = answer.prettyPrint();
      }

      logOutput(outWriter, answerStringToPrint + "\n");

      return true;
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

   private boolean getQuestion(List<String> parameters) {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      String questionName = parameters.get(0);

      String questionFileName = String.format("%s/%s/%s",
            BfConsts.RELPATH_QUESTIONS_DIR, questionName,
            BfConsts.RELPATH_QUESTION_FILE);

      String downloadedQuestionFile = _workHelper.getObject(_currContainerName,
            _currTestrig, questionFileName);
      if (downloadedQuestionFile == null) {
         _logger.errorf("Failed to get question file %s\n", questionFileName);
         return false;
      }

      String questionString = CommonUtil
            .readFile(Paths.get(downloadedQuestionFile));
      _logger.outputf("Question:\n%s\n", questionString);

      return true;
   }

   public Settings getSettings() {
      return _settings;
   }

   private boolean help(List<String> parameters) {
      if (parameters.size() == 1) {
         Command cmd = Command.fromName(parameters.get(0));
         printUsage(cmd);
      }
      else {
         printUsage();
      }
      return true;
   }

   private boolean initContainer(String[] words) {
      String containerPrefix = (words.length > 1) ? words[1]
            : DEFAULT_CONTAINER_PREFIX;
      _currContainerName = _workHelper.initContainer(null, containerPrefix);
      if (_currContainerName == null) {
         _logger.errorf("Could not init container\n");
         return false;
      }
      _logger.output("Active container is set");
      _logger.infof(" to  %s\n", _currContainerName);
      _logger.output("\n");
      return true;
   }

   private boolean initDeltaEnv(FileWriter outWriter, List<String> parameters)
         throws Exception {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      String deltaEnvLocation = parameters.get(0);
      String deltaEnvName = (parameters.size() > 1) ? parameters.get(1)
            : DEFAULT_DELTA_ENV_PREFIX + UUID.randomUUID().toString();
      String baseEnvName = (parameters.size() > 2) ? parameters.get(2) : "";

      if (!uploadEnv(deltaEnvLocation, deltaEnvName, baseEnvName)) {
         return false;
      }

      _currDeltaEnv = deltaEnvName;
      _currDeltaTestrig = _currTestrig;

      _logger.output("Active delta testrig->environment is set");
      _logger.infof("to %s->%s\n", _currDeltaTestrig, _currDeltaEnv);
      _logger.output("\n");

      WorkItem wItemGenDdp = _workHelper.getWorkItemCompileDeltaEnvironment(
            _currContainerName, _currDeltaTestrig, _currEnv, _currDeltaEnv);

      if (!execute(wItemGenDdp, outWriter)) {
         return false;
      }

      return true;
   }

   private void initHelpers() {
      switch (_settings.getRunMode()) {
      case batch:
      case interactive:
         break;

      case gendatamodel:
      case genquestions:
      default:
         return;
      }

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
                  _logger.outputf(
                        "Connected to coordinator after %d tries\n",
                        numTries);
               }
               break;
            }
            Thread.sleep(1 * 1000); // 1 second
         }
         catch (Exception e) {
            _logger.errorf(
                  "Exeption while checking reachability to coordinator: %s",
                  ExceptionUtils.getStackTrace(e));
            System.exit(1);
         }
      }
   }

   private boolean initOrAddAnalysis(
         FileWriter outWriter, List<String> options,
         List<String> parameters, boolean newAnalysis) {
      if (!isSetContainer(true)) {
         return false;
      }
      if (options.size() != 0 || parameters.size() != 2) {
         _logger.errorf("Invalid arguments: %s %s", options.toString(),
               parameters.toString());
         printUsage(Command.INIT_ANALYSIS);
         return false;
      }

      String analysisName = parameters.get(0);
      String questionsPathStr = parameters.get(1);

      Map<String, String> questionMap = new TreeMap<>();

      if (!loadQuestions(null, questionsPathStr, questionMap)) {
         return false;
      }

      String analysisJsonString = "{}";

      try {
         JSONObject jObject = new JSONObject();
         for (String qName : questionMap.keySet()) {
            jObject.put(qName, new JSONObject(questionMap.get(qName)));
         }
         analysisJsonString = jObject.toString(1);
      }
      catch (JSONException e) {
         throw new BatfishException("Failed to get JSONObject for analysis", e);
      }

      Path analysisFile = createTempFile("analysis", analysisJsonString);

      boolean result = _workHelper.configureAnalysis(_currContainerName,
            newAnalysis, analysisName, analysisFile.toAbsolutePath().toString(),
            null);

      if (analysisFile != null) {
         CommonUtil.deleteIfExists(analysisFile);
      }

      logOutput(outWriter, "Output of configuring analysis " + analysisName
            + ": " + result + "\n");
      return result;
   }

   private boolean initTestrig(
         FileWriter outWriter, List<String> parameters,
         boolean doDelta) throws Exception {
      String testrigLocation = parameters.get(0);
      String testrigName = (parameters.size() > 1) ? parameters.get(1)
            : DEFAULT_TESTRIG_PREFIX + UUID.randomUUID().toString();

      // initialize the container if it hasn't been init'd before
      if (!isSetContainer(false)) {
         _currContainerName = _workHelper.initContainer(
               null,
               DEFAULT_CONTAINER_PREFIX);
         if (_currContainerName == null) {
            _logger.errorf("Could not init container\n");
            return false;
         }
         _logger.output("Init'ed and set active container");
         _logger.infof(" to %s\n", _currContainerName);
         _logger.output("\n");
      }

      if (!uploadTestrig(testrigLocation, testrigName)) {
         unsetTestrig(doDelta);
         return false;
      }

      _logger.output("Uploaded testrig. Parsing now.\n");

      WorkItem wItemParse = _workHelper.getWorkItemParse(_currContainerName,
            testrigName, false);

      if (!execute(wItemParse, outWriter)) {
         unsetTestrig(doDelta);
         return false;
      }

      if (!doDelta) {
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

   private boolean listAnalyses(
         FileWriter outWriter, List<String> options,
         List<String> parameters) {
      if (!isSetContainer(true)) {
         return false;
      }
      if (options.size() != 0 || parameters.size() != 0) {
         _logger.errorf("Invalid arguments: %s %s\n", options.toString(),
               parameters.toString());
         printUsage(Command.LIST_TESTRIGS);
         return false;
      }

      JSONObject analysisList = _workHelper.listAnalyses(_currContainerName);
      logOutput(
            outWriter,
            String.format("Found %d analyses\n", analysisList.length()));

      try {
         logOutput(outWriter, analysisList.toString(1));
      }
      catch (JSONException e) {
         throw new BatfishException("Failed to print analysis list", e);
      }

      // if (analysisList != null) {
      // Iterator<?> aIterator = analysisList.keys();
      // while (aIterator.hasNext()) {
      // String aName = (String) aIterator.next();
      // _logger.outputf("Analysis: %s\n", aName);
      //
      // try {
      // JSONObject questionList = analysisList.getJSONObject(aName);
      // _logger.outputf("Found %d questions\n", questionList.length());
      //
      // Iterator<?> qIterator = questionList.keys();
      // while (qIterator.hasNext()) {
      // String qName = (String) qIterator.next();
      // _logger.outputf(" Question: %s\n", qName);
      //
      // JSONObject questionJson = questionList.getJSONObject(qName);
      // _logger.outputf("%s\n", questionJson.toString(1));
      // }
      //
      // }
      // catch (JSONException e) {
      // throw new BatfishException("Failed to process analysis list", e);
      // }
      // }
      // }

      return true;
   }

   private boolean listContainers() {
      String[] containerList = _workHelper.listContainers();
      _logger.outputf("Containers: %s\n", Arrays.toString(containerList));
      return true;
   }

   private boolean listEnvironments() {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      String[] environmentList = _workHelper
            .listEnvironments(_currContainerName, _currTestrig);
      _logger.outputf("Environments: %s\n", Arrays.toString(environmentList));

      return true;
   }

   private boolean listQuestions() {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }
      String[] questionList = _workHelper.listQuestions(
            _currContainerName,
            _currTestrig);
      _logger.outputf("Questions: %s\n", Arrays.toString(questionList));
      return true;
   }

   private boolean listTestrigs(
         FileWriter outWriter, List<String> options,
         List<String> parameters) {
      if (options.size() != 0 || parameters.size() != 0) {
         _logger.errorf("Invalid arguments: %s %s\n", options.toString(),
               parameters.toString());
         printUsage(Command.LIST_TESTRIGS);
         return false;
      }

      Map<String, String> testrigs = _workHelper
            .listTestrigs(_currContainerName);
      if (testrigs != null) {
         for (String testrigName : testrigs.keySet()) {
            logOutput(outWriter, String.format("Testrig: %s\n%s\n", testrigName,
                  testrigs.get(testrigName)));
         }
      }
      return true;
   }

   private String loadQuestion(Path file, Map<String, String> bfq) {
      String questionText = CommonUtil.readFile(file);
      try {
         JSONObject questionObj = new JSONObject(questionText);
         if (questionObj.has(BfConsts.INSTANCE_VAR)
               && !questionObj.isNull(BfConsts.INSTANCE_VAR)) {
            JSONObject instanceDataObj = questionObj
                  .getJSONObject(BfConsts.INSTANCE_VAR);
            String instanceDataStr = instanceDataObj.toString();
            BatfishObjectMapper mapper = new BatfishObjectMapper(
                  getCurrentClassLoader());
            InstanceData instanceData = mapper.<InstanceData>readValue(
                  instanceDataStr, new TypeReference<InstanceData>() {
                  });
            validateInstanceData(instanceData);
            String name = instanceData.getInstanceName();
            bfq.put(name.toLowerCase(), questionText);
            return name;
         }
         else {
            throw new BatfishException("Question in file: '" + file.toString()
                  + "' has no instance name");
         }
      }
      catch (JSONException | IOException e) {
         throw new BatfishException("Failed to process question", e);
      }
   }

   private boolean loadQuestions(
         FileWriter outWriter, List<String> parameters,
         Map<String, String> bfq) {
      if (parameters.size() != 1) {
         _logger.error("Invalid arguments: " + parameters.toString());
         printUsage(Command.LOAD_QUESTIONS);
         return false;
      }
      String questionsPathStr = parameters.get(0);
      return loadQuestions(outWriter, questionsPathStr, bfq);
   }

   private boolean loadQuestions(
         FileWriter outWriter, String questionsPathStr,
         Map<String, String> bfq) {
      Path questionsPath = Paths.get(questionsPathStr);
      int numLoaded = 0;
      Answer answer = new Answer();
      LoadQuestionAnswerElement ae = new LoadQuestionAnswerElement();
      answer.addAnswerElement(ae);
      SortedSet<Path> jsonQuestionFiles = new TreeSet<>();
      try {
         Files.walkFileTree(questionsPath, Collections.emptySet(), 1,
               new SimpleFileVisitor<Path>() {
                  @Override
                  public FileVisitResult visitFile(
                        Path file,
                        BasicFileAttributes attrs) throws IOException {
                     String filename = file.getFileName().toString();
                     if (filename.endsWith(".json")) {
                        jsonQuestionFiles.add(file);
                     }
                     return FileVisitResult.CONTINUE;
                  }
               });
      }
      catch (IOException e) {
         throw new BatfishException("Failed to visit questions dir", e);
      }
      for (Path jsonQuestionFile : jsonQuestionFiles) {
         int numBefore = bfq.size();
         String name = loadQuestion(jsonQuestionFile, bfq);
         int numAfter = bfq.size();
         if (numBefore == numAfter) {
            ae.getReplaced().add(name);
         }
         else {
            ae.getAdded().add(name);
         }
         numLoaded++;
      }
      ae.setNumLoaded(numLoaded);
      ObjectMapper mapper = new BatfishObjectMapper(getCurrentClassLoader());
      String answerStringToPrint;
      try {
         answerStringToPrint = mapper.writeValueAsString(answer);
      }
      catch (JsonProcessingException e) {
         throw new BatfishException(
               "Could not write answer element as string",
               e);
      }
      if (outWriter == null && _settings.getPrettyPrintAnswers()) {
         answerStringToPrint = answer.prettyPrint();
      }

      logOutput(outWriter, answerStringToPrint);
      return true;
   }

   private void logOutput(FileWriter outWriter, String message) {
      if (outWriter == null) {
         _logger.output(message);
      }
      else {
         try {
            outWriter.write(message);
         }
         catch (IOException e) {
            throw new BatfishException("Failed to log output to outWriter", e);
         }
      }
   }

   private Map<String, String> parseParams(String paramsLine) {
      Map<String, String> parameters = new HashMap<>();
      String jsonParamsStr = "{ " + paramsLine + " }";
      try {
         JSONObject jsonParamsObject = new JSONObject(jsonParamsStr);

         Iterator<?> keys = jsonParamsObject.keys();
         while( keys.hasNext() ) {
            String key = (String)keys.next();
            String value = jsonParamsObject.get(key).toString();
            _logger.debugf("key=%s value=%s\n", key, value);

            parameters.put(key, value);
         }
         return parameters;
      }
      catch (JSONException e){
         throw new BatfishException("Failed to parse parameters. (Are all key-value pairs separated by commas? Are all values valid JSON?)", e);
      }
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

   private void printWorkStatusResponse(Pair<WorkStatusCode, String> response) {

      if (_logger.getLogLevel() >= BatfishLogger.LEVEL_INFO) {
         WorkStatusCode status = response.getFirst();
         _logger.infof("status: %s\n", status);

         BatfishObjectMapper mapper = new BatfishObjectMapper();
         Task task;
         try {
            task = mapper.readValue(response.getSecond(), Task.class);
         }
         catch (IOException e) {
            _logger.errorf("Could not deserialize task object: %s\n", e);
            return;
         }

         if (task == null) {
            _logger.infof(".... no task information\n");
            return;
         }

         List<Batch> batches = task.getBatches();

         // when log level is INFO, we only print the last batch
         // else print all
         for (int i = 0; i < batches.size(); i++) {
            if (i == batches.size() - 1
                  || status == WorkStatusCode.TERMINATEDNORMALLY
                  || status == WorkStatusCode.TERMINATEDABNORMALLY) {
               _logger.infof(".... %s\n", batches.get(i).toString());
            }
            else {
               _logger.debugf(".... %s\n", batches.get(i).toString());
            }
         }
         if (status == WorkStatusCode.TERMINATEDNORMALLY
               || status == WorkStatusCode.TERMINATEDABNORMALLY) {
            _logger.infof(".... %s: %s\n", task.getTerminated().toString(),
                  status);
         }
      }
   }

   private boolean processCommand(String command) {
      String line = command.trim();
      if (line.length() == 0 || line.startsWith("#")) {
         return true;
      }
      _logger.debug("Doing command: " + line + "\n");
      String[] words = line.split("\\s+");
      if (words.length > 0) {
         if (!validCommandUsage(words)) {
            return false;
         }
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
         case ADD_ANALYSIS_QUESTIONS:
            return initOrAddAnalysis(outWriter, options, parameters, false);
         case ADD_BATFISH_OPTION:
            return addBatfishOption(words, options, parameters);
         case ANSWER:
            return answer(words, outWriter, options, parameters, false);
         case ANSWER_DELTA:
            return answer(words, outWriter, options, parameters, true);
         case CAT:
            return cat(words);
         case CHECK_API_KEY:
            return checkApiKey();
         case CLEAR_SCREEN:
            return clearScreen();
         case DEL_ANALYSIS:
            return delAnalysis(outWriter, options, parameters);
         case DEL_ANALYSIS_QUESTIONS:
            return delAnalysisQuestions(outWriter, options, parameters);
         case DEL_BATFISH_OPTION:
            return delBatfishOption(parameters);
         case DEL_CONTAINER:
            return delContainer(parameters);
         case DEL_ENVIRONMENT:
            return delEnvironment(parameters);
         case DEL_QUESTION:
            return delQuestion(parameters);
         case DEL_TESTRIG:
            return delTestrig(outWriter, parameters);
         case DIR:
            return dir(parameters);
         case ECHO:
            return echo(words);
         case GEN_DP:
            return generateDataplane(outWriter);
         case GEN_DELTA_DP:
            return generateDeltaDataplane(outWriter);
         case GET:
            return get(words, outWriter, options, parameters, false);
         case GET_DELTA:
            return get(words, outWriter, options, parameters, true);
         case GET_ANALYSIS_ANSWERS:
            return getAnalysisAnswers(outWriter, options, parameters, false,
                  false);
         case GET_ANALYSIS_ANSWERS_DELTA:
            return getAnalysisAnswers(outWriter, options, parameters, true,
                  false);
         case GET_ANALYSIS_ANSWERS_DIFFERENTIAL:
            return getAnalysisAnswers(outWriter, options, parameters, false,
                  true);
         case GET_ANSWER:
            return getAnswer(outWriter, parameters, false, false);
         case GET_ANSWER_DELTA:
            return getAnswer(outWriter, parameters, true, false);
         case GET_ANSWER_DIFFERENTIAL:
            return getAnswer(outWriter, parameters, false, true);
         case GET_QUESTION:
            return getQuestion(parameters);
         case HELP:
            return help(parameters);
         case INIT_ANALYSIS:
            return initOrAddAnalysis(outWriter, options, parameters, true);
         case INIT_CONTAINER:
            return initContainer(words);
         case INIT_DELTA_ENV:
            return initDeltaEnv(outWriter, parameters);
         case INIT_DELTA_TESTRIG:
            return initTestrig(outWriter, parameters, true);
         case INIT_TESTRIG:
            return initTestrig(outWriter, parameters, false);
         case LIST_ANALYSES:
            return listAnalyses(outWriter, options, parameters);
         case LIST_CONTAINERS:
            return listContainers();
         case LIST_ENVIRONMENTS:
            return listEnvironments();
         case LIST_QUESTIONS:
            return listQuestions();
         case LIST_TESTRIGS:
            return listTestrigs(outWriter, options, parameters);
         case LOAD_QUESTIONS:
            return loadQuestions(outWriter, parameters, _bfq);
         case PROMPT:
            return prompt();
         case PWD:
            return pwd();
         case REINIT_DELTA_TESTRIG:
            return reinitTestrig(outWriter, true);
         case RUN_ANALYSIS:
            return runAnalysis(outWriter, options, parameters, false, false);
         case RUN_ANALYSIS_DELTA:
            return runAnalysis(outWriter, options, parameters, true, false);
         case RUN_ANALYSIS_DIFFERENTIAL:
            return runAnalysis(outWriter, options, parameters, false, true);
         case REINIT_TESTRIG:
            return reinitTestrig(outWriter, false);
         case SET_BATFISH_LOGLEVEL:
            return setBatfishLogLevel(parameters);
         case SET_CONTAINER:
            return setContainer(parameters);
         case SET_DELTA_ENV:
            return setDeltaEnv(parameters);
         case SET_ENV:
            return setEnv(parameters);
         case SET_DELTA_TESTRIG:
            return setDeltaTestrig(parameters);
         case SET_LOGLEVEL:
            return setLogLevel(parameters);
         case SET_PRETTY_PRINT:
            return setPrettyPrint(parameters);
         case SET_TESTRIG:
            return setTestrig(parameters);
         case SHOW_API_KEY:
            return showApiKey();
         case SHOW_BATFISH_LOGLEVEL:
            return showBatfishLogLevel();
         case SHOW_BATFISH_OPTIONS:
            return showBatfishOptions();
         case SHOW_CONTAINER:
            return showContainer();
         case SHOW_COORDINATOR_HOST:
            return showCoordinatorHost();
         case SHOW_DELTA_TESTRIG:
            return showDeltaTestrig();
         case SHOW_LOGLEVEL:
            return showLogLevel();
         case SHOW_TESTRIG:
            return showTestrig();
         case SHOW_VERSION:
            return showVersion();
         case TEST:
            return test(parameters);
         case UPLOAD_CUSTOM_OBJECT:
            return uploadCustomObject(parameters);

         case EXIT:
         case QUIT:
            return exit();

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

   private boolean prompt() throws IOException {
      if (_settings.getRunMode() == RunMode.interactive) {
         _logger.output("\n\n[Press enter to proceed]\n\n");
         BufferedReader in = new BufferedReader(
               new InputStreamReader(System.in));
         in.readLine();
      }
      return true;
   }

   private boolean pwd() {
      final String dir = System.getProperty("user.dir");
      _logger.output("working directory = " + dir + "\n");
      return true;
   }

   private List<String> readCommands(Path startupFilePath) {
      List<String> commands = null;
      try {
         commands = Files.readAllLines(
               startupFilePath,
               StandardCharsets.US_ASCII);
      }
      catch (Exception e) {
         System.err.printf("Exception reading command file %s: %s\n",
               _settings.getBatchCommandFile(), e.getMessage());
         System.exit(1);
      }
      return commands;
   }

   private boolean reinitTestrig(FileWriter outWriter, boolean isDelta)
         throws Exception {
      String testrig;
      if (!isDelta) {
         _logger.output("Reinitializing testrig. Parsing now.\n");
         testrig = _currTestrig;
      }
      else {
         _logger.output("Reinitializing delta testrig. Parsing now.\n");
         testrig = _currDeltaTestrig;
      }

      WorkItem wItemParse = _workHelper.getWorkItemParse(_currContainerName,
            testrig, isDelta);

      if (!execute(wItemParse, outWriter)) {
         return false;
      }

      return true;
   }

   public void run(List<String> initialCommands) {
      loadPlugins();
      initHelpers();

      _logger.debugf(
            "Will use coordinator at %s://%s\n",
            (_settings.getSslDisable()) ? "http" : "https",
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

      case batch: {
         runBatchFile();
         break;
      }

      case gendatamodel:
         generateDatamodel();
         break;

      case genquestions:
         generateQuestions();
         break;

      case interactive: {
         runStartupFile();
         runInteractive();
         break;
      }

      default:
         System.err.println("org.batfish.client: Unknown run mode.");
         System.exit(1);
      }

   }

   private boolean runAnalysis(
         FileWriter outWriter, List<String> options,
         List<String> parameters, boolean delta, boolean differential) {
      if (!isSetContainer(true) || !isSetTestrig()) {
         return false;
      }
      if (options.size() != 0 || parameters.size() != 1) {
         _logger.errorf("Invalid arguments: %s %s", options.toString(),
               parameters.toString());
         printUsage(Command.RUN_ANALYSIS);
         return false;
      }

      String analysisName = parameters.get(0);

      // answer the question
      WorkItem wItemAs = _workHelper.getWorkItemRunAnalysis(analysisName,
            _currContainerName, _currTestrig, _currEnv, _currDeltaTestrig,
            _currDeltaEnv, delta, differential);

      return execute(wItemAs, outWriter);
   }

   private void runBatchFile() {
      Path batchCommandFilePath = Paths.get(_settings.getBatchCommandFile());
      List<String> commands = readCommands(batchCommandFilePath);
      boolean result = processCommands(commands);
      if (!result) {
         System.exit(1);
      }
   }

   private void runInteractive() {
      try {
         String rawLine;
         while (!_exit && (rawLine = _reader.readLine()) != null) {
            processCommand(rawLine);
         }
      }
      catch (Throwable t) {
         t.printStackTrace();
      }
      finally {
         FileHistory history = (FileHistory) _reader.getHistory();
         try {
            history.flush();
         }
         catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void runStartupFile() {
      Path startupFilePath = Paths.get(System.getenv(ENV_HOME), STARTUP_FILE);
      if (Files.exists(startupFilePath)) {
         List<String> commands = readCommands(startupFilePath);
         boolean result = processCommands(commands);
         if (!result) {
            System.exit(1);
         }
      }
   }

   private boolean setBatfishLogLevel(List<String> parameters) {
      String logLevelStr = parameters.get(0).toLowerCase();
      if (!BatfishLogger.isValidLogLevel(logLevelStr)) {
         _logger.errorf("Undefined loglevel value: %s\n", logLevelStr);
         return false;
      }
      _settings.setBatfishLogLevel(logLevelStr);
      _logger.output("Changed batfish loglevel to " + logLevelStr + "\n");
      return true;
   }

   private boolean setContainer(List<String> parameters) {
      _currContainerName = parameters.get(0);
      _logger.outputf(
            "Active container is now set to %s\n",
            _currContainerName);
      return true;
   }

   private boolean setDeltaEnv(List<String> parameters) {
      _currDeltaEnv = parameters.get(0);
      if (_currDeltaTestrig == null) {
         _currDeltaTestrig = _currTestrig;
      }
      _logger.outputf("Active delta testrig->environment is now %s->%s\n",
            _currDeltaTestrig, _currDeltaEnv);
      return true;
   }

   private boolean setDeltaTestrig(List<String> parameters) {
      _currDeltaTestrig = parameters.get(0);
      _currDeltaEnv = (parameters.size() > 1) ? parameters.get(1)
            : DEFAULT_ENV_NAME;
      _logger.outputf("Delta testrig->env is now %s->%s\n", _currDeltaTestrig,
            _currDeltaEnv);
      return true;
   }

   private boolean setEnv(List<String> parameters) {
      if (!isSetTestrig()) {
         return false;
      }
      _currEnv = parameters.get(0);
      _logger.outputf("Base testrig->env is now %s->%s\n", _currTestrig,
            _currEnv);
      return true;
   }

   private boolean setLogLevel(List<String> parameters) {
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

   private boolean setPrettyPrint(List<String> parameters) {
      String ppStr = parameters.get(0).toLowerCase();
      boolean prettyPrint = Boolean.parseBoolean(ppStr);
      _settings.setPrettyPrintAnswers(prettyPrint);
      _logger.output("Set pretty printing answers to " + ppStr + "\n");
      return true;
   }

   private boolean setTestrig(List<String> parameters) {
      if (!isSetContainer(true)) {
         return false;
      }

      _currTestrig = parameters.get(0);
      _currEnv = (parameters.size() > 1) ? parameters.get(1) : DEFAULT_ENV_NAME;
      _logger.outputf("Base testrig->env is now %s->%s\n", _currTestrig,
            _currEnv);
      return true;
   }

   private boolean showApiKey() {
      _logger.outputf("Current API Key is %s\n", _settings.getApiKey());
      return true;
   }

   private boolean showBatfishLogLevel() {
      _logger.outputf(
            "Current batfish log level is %s\n",
            _settings.getBatfishLogLevel());
      return true;
   }

   private boolean showBatfishOptions() {
      _logger.outputf(
            "There are %d additional batfish options\n",
            _additionalBatfishOptions.size());
      for (String option : _additionalBatfishOptions.keySet()) {
         _logger.outputf("    %s : %s \n", option,
               _additionalBatfishOptions.get(option));
      }
      return true;
   }

   private boolean showContainer() {
      _logger.outputf("Current container is %s\n", _currContainerName);
      return true;
   }

   private boolean showCoordinatorHost() {
      _logger.outputf(
            "Current coordinator host is %s\n",
            _settings.getCoordinatorHost());
      return true;
   }

   private boolean showDeltaTestrig() {
      if (!isSetDeltaEnvironment()) {
         return false;
      }
      _logger.outputf("Delta testrig->environment is %s->%s\n",
            _currDeltaTestrig, _currDeltaEnv);
      return true;
   }

   private boolean showLogLevel() {
      _logger.outputf(
            "Current client log level is %s\n",
            _logger.getLogLevelStr());
      return true;
   }

   private boolean showTestrig() {
      if (!isSetTestrig()) {
         return false;
      }
      _logger.outputf("Base testrig->environment is %s->%s\n", _currTestrig,
            _currEnv);
      return true;
   }

   private boolean showVersion() {
      _logger.outputf("Client version is %s\n", Version.getVersion());

      Map<String, String> map = _workHelper.getInfo();

      if (!map.containsKey(CoordConsts.SVC_KEY_VERSION)) {
         _logger.errorf(
               "key '%s' not found in Info\n",
               CoordConsts.SVC_KEY_VERSION);
         return false;
      }

      String version = map.get(CoordConsts.SVC_KEY_VERSION);
      _logger.outputf("Service version is %s\n", version);
      return true;
   }

   private boolean test(List<String> parameters) throws IOException {
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
      _logger.debugf("Test command is %s\n", Arrays.toString(testCommand));

      File referenceFile = new File(referenceFileName);

      if (!referenceFile.exists()) {
         _logger.errorf(
               "Reference file does not exist: %s\n",
               referenceFileName);
         missingReferenceFile = true;
      }

      File testoutFile = Files.createTempFile("test", "out").toFile();
      testoutFile.deleteOnExit();

      FileWriter testoutWriter = new FileWriter(testoutFile);

      boolean testCommandSucceeded = processCommand(testCommand, testoutWriter);
      testoutWriter.close();

      if (!failingTest && testCommandSucceeded) {
         try {

            ObjectMapper mapper = new BatfishObjectMapper(
                  getCurrentClassLoader());

            // rewrite new answer string using local implementation
            String testOutput = CommonUtil
                  .readFile(Paths.get(testoutFile.getAbsolutePath()));

            String testAnswerString = testOutput;

            try {
               Answer testAnswer = mapper.readValue(testOutput, Answer.class);
               testAnswerString = mapper.writeValueAsString(testAnswer);
            }
            catch (JsonParseException | UnrecognizedPropertyException e) {
               // not all outputs of process command are of Answer.class type
               // in that case, we use the exact string as initialized above for
               // comparison
            }

            if (!missingReferenceFile) {
               String referenceOutput = CommonUtil
                     .readFile(Paths.get(referenceFileName));

               String referenceAnswerString = referenceOutput;

               // rewrite reference string using local implementation
               Answer referenceAnswer;
               try {
                  referenceAnswer = mapper.readValue(
                        referenceOutput,
                        Answer.class);
                  referenceAnswerString = mapper
                        .writeValueAsString(referenceAnswer);
               }
               catch (JsonParseException | UnrecognizedPropertyException e) {
                  // throw new BatfishException(
                  // "Error reading reference output using current schema
                  // (reference output is likely obsolete)",
                  // e);
                  // not all outputs of process command are of Answer.class type
                  // in that case, we use the exact string as initialized above
                  // for comparison
               }

               // due to options chosen in BatfishObjectMapper, if json
               // outputs were equal, then strings should be equal

               if (referenceAnswerString.equals(testAnswerString)) {
                  testPassed = true;
               }
            }
         }
         catch (Exception e) {
            _logger.error("Exception in comparing test results: "
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
            Files.move(
                  Paths.get(testoutFile.getAbsolutePath()),
                  Paths.get(referenceFile + ".testout"),
                  StandardCopyOption.REPLACE_EXISTING);
            _logger.outputf("Copied output to %s\n", outFileName);
         }
      }
      return true;
   }

   private void unsetTestrig(boolean doDelta) {
      if (doDelta) {
         _currDeltaTestrig = null;
         _currDeltaEnv = null;
         _logger.info("Delta testrig and environment are now unset\n");
      }
      else {
         _currTestrig = null;
         _currEnv = null;
         _logger.info("Base testrig and environment are now unset\n");
      }
   }

   private boolean uploadCustomObject(List<String> parameters) {
      if (!isSetTestrig() || !isSetContainer(true)) {
         return false;
      }

      String objectName = parameters.get(0);
      String objectFile = parameters.get(1);

      // upload the object
      return _workHelper.uploadCustomObject(_currContainerName, _currTestrig,
            objectName, objectFile);
   }

   private boolean uploadEnv(
         String fileOrDir, String envName,
         String baseEnvName) throws Exception {
      Path initialUploadTarget = Paths.get(fileOrDir);
      Path uploadTarget = initialUploadTarget;
      boolean createZip = Files.isDirectory(initialUploadTarget);
      if (createZip) {
         uploadTarget = CommonUtil.createTempFile("testrigOrEnv", ".zip");
         ZipUtility.zipFiles(
               initialUploadTarget.toAbsolutePath(),
               uploadTarget.toAbsolutePath());
      }
      try {
         boolean result = _workHelper.uploadEnvironment(_currContainerName,
               _currTestrig, baseEnvName, envName, uploadTarget.toString());
         return result;
      }
      finally {
         if (createZip) {
            CommonUtil.delete(uploadTarget);
         }
      }
   }

   private boolean uploadTestrig(String fileOrDir, String testrigName) {
      Path initialUploadTarget = Paths.get(fileOrDir);
      Path uploadTarget = initialUploadTarget;
      boolean createZip = Files.isDirectory(initialUploadTarget);
      if (createZip) {
         uploadTarget = CommonUtil.createTempFile("testrigOrEnv", "zip");
         ZipUtility.zipFiles(
               initialUploadTarget.toAbsolutePath(),
               uploadTarget.toAbsolutePath());
      }
      try {
         boolean result = _workHelper.uploadTestrig(_currContainerName,
               testrigName, uploadTarget.toString());
         return result;
      }
      finally {
         if (createZip) {
            CommonUtil.delete(uploadTarget);
         }
      }
   }

   private void validateInstanceData(InstanceData instanceData) {
      String description = instanceData.getDescription();
      String q = "Question: '" + instanceData.getInstanceName() + "'";
      if (description == null || description.length() == 0) {
         throw new BatfishException(q + " is missing question description");
      }
      for (Entry<String, Variable> e : instanceData.getVariables().entrySet()) {
         String variableName = e.getKey();
         Variable variable = e.getValue();
         String v = "Variable: '" + variableName + "' in " + q;
         String variableDescription = variable.getDescription();
         if (variableDescription == null || variableDescription.length() == 0) {
            throw new BatfishException(v + " is missing description");
         }
      }
   }

   private boolean validCommandUsage(String[] words) {
      return true;
   }

}
