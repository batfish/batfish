package org.batfish.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;

public enum Command {
   ADD_BATFISH_OPTION("add-batfish-option"),
   ANSWER("answer"),
   ANSWER_DELTA("answer-delta"),
   CAT("cat"),
   CHECK_API_KEY("checkapikey"),
   CLEAR_SCREEN("cls"),
   DEL_BATFISH_OPTION("del-batfish-option"),
   DEL_CONTAINER("del-container"),
   DEL_ENVIRONMENT("del-environment"),
   DEL_QUESTION("del-question"),
   DEL_TESTRIG("del-testrig"),
   DIR("dir"),
   ECHO("echo"),
   EXIT("exit"),
   GEN_DELTA_DP("generate-delta-dataplane"),
   GEN_DP("generate-dataplane"),
   GET("get"),
   GET_ANSWER("get-answer"),
   GET_DELTA("get-delta"),
   GET_QUESTION("get-question"),
   HELP("help"),
   INIT_CONTAINER("init-container"),
   INIT_DELTA_ENV("init-delta-environment"),
   INIT_DELTA_TESTRIG("init-delta-testrig"),
   INIT_TESTRIG("init-testrig"),
   LIST_CONTAINERS("list-containers"),
   LIST_ENVIRONMENTS("list-environments"),
   LIST_QUESTIONS("list-questions"),
   LIST_TESTRIGS("list-testrigs"),
   PROMPT("prompt"),
   PWD("pwd"),
   QUIT("quit"),
   REINIT_DELTA_TESTRIG("reinit-delta-testrig"),
   REINIT_TESTRIG("reinit-testrig"),
   SET_BATFISH_LOGLEVEL("set-batfish-loglevel"),
   SET_CONTAINER("set-container"),
   SET_DELTA_ENV("set-delta-environment"),
   SET_DELTA_TESTRIG("set-delta-testrig"),
   SET_ENV("set-environment"),
   SET_LOGLEVEL("set-loglevel"),
   SET_PRETTY_PRINT("set-pretty-print"),
   SET_TESTRIG("set-testrig"),
   SHOW_API_KEY("show-api-key"),
   SHOW_BATFISH_LOGLEVEL("show-batfish-loglevel"),
   SHOW_BATFISH_OPTIONS("show-batfish-options"),
   SHOW_CONTAINER("show-container"),
   SHOW_COORDINATOR_HOST("show-coordinator-host"),
   SHOW_DELTA_TESTRIG("show-delta-testrig"),
   SHOW_LOGLEVEL("show-loglevel"),
   SHOW_TESTRIG("show-testrig"),
   TEST("test"),
   UPLOAD_CUSTOM_OBJECT("upload-custom");

   private final static Map<String, Command> _nameMap = buildNameMap();

   private final static Map<Command, Pair<String, String>> _usageMap = buildUsageMap();

   private static Map<String, Command> buildNameMap() {
      Map<String, Command> map = new HashMap<>();
      for (Command value : Command.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   private static Map<Command, Pair<String, String>> buildUsageMap() {
      Map<Command, Pair<String, String>> descs = new TreeMap<>();
      descs.put(ADD_BATFISH_OPTION, new Pair<>("<option-key> <option-value>",
            "Additional options to pass to Batfish"));
      descs.put(ANSWER, new Pair<>(
            "<question-file> [param1=value1 [param2=value2] ...]",
            "Answer the question in the file for the default environment"));
      descs.put(ADD_BATFISH_OPTION, new Pair<>("<option-key> [<option-value>]",
            "Additional options to pass to Batfish"));
      descs.put(ANSWER_DELTA,
            new Pair<>("<question-file>  [param1=value1 [param2=value2] ...]",
                  "Answer the question in the file for the delta environment"));
      descs.put(CAT,
            new Pair<>("<filename>", "Print the contents of the file"));
      descs.put(CHECK_API_KEY, new Pair<>("", "Check if API Key is valid"));
      // descs.put(CHANGE_DIR, CHANGE_DIR
      // + " <dirname>\n"
      // + "\t Change the working directory");
      descs.put(CLEAR_SCREEN, new Pair<>("", "Clear screen"));
      descs.put(DEL_BATFISH_OPTION,
            new Pair<>("<option-key>", "Stop passing this option to Batfish"));
      descs.put(DEL_CONTAINER,
            new Pair<>("<container-name>", "Delete the specified container"));
      descs.put(DEL_ENVIRONMENT, new Pair<>("<environment-name>",
            "Delete the specified environment"));
      descs.put(DEL_QUESTION,
            new Pair<>("<question-name>", "Delete the specified question"));
      descs.put(DEL_TESTRIG,
            new Pair<>("<testrig-name>", "Delete the specified testrig"));
      descs.put(DIR, new Pair<>("<dir>", "List directory contents"));
      descs.put(ECHO, new Pair<>("<message>", "Echo the message"));
      descs.put(EXIT, new Pair<>("", "Terminate interactive client session"));
      descs.put(GEN_DELTA_DP,
            new Pair<>("", "Generate dataplane for the delta environment"));
      descs.put(GEN_DP,
            new Pair<>("", "Generate dataplane for the default environment"));
      descs.put(GET,
            new Pair<>("<question-type>  [param1=value1 [param2=value2] ...]",
                  "Answer the question by type for the base environment"));
      descs.put(GET_ANSWER, new Pair<>("[-html] <question-name>",
            "Get the answer for a previously answered question"));
      descs.put(GET_DELTA,
            new Pair<>("<question-file>  [param1=value1 [param2=value2] ...]",
                  "Answer the question by type for the delta environment"));
      descs.put(GET_QUESTION, new Pair<>("<question-name>",
            "Get the question and parameter files"));
      descs.put(HELP,
            new Pair<>("[command]", "Print the list of supported commands"));
      descs.put(INIT_CONTAINER, new Pair<>("[<container-name-prefix>]",
            "Initialize a new container"));
      descs.put(INIT_DELTA_ENV,
            new Pair<>(
                  "<environment zipfile or directory> [<environment-name>]",
                  "Initialize the delta environment"));
      descs.put(INIT_DELTA_TESTRIG,
            new Pair<>("<testrig zipfile or directory> [<environment name>]",
                  "Initialize the delta testrig with default environment"));
      descs.put(INIT_TESTRIG,
            new Pair<>("<testrig zipfile or directory> [<environment name>]",
                  "Initialize the testrig with default environment"));
      descs.put(LIST_CONTAINERS,
            new Pair<>("", "List the containers to which you have access"));
      descs.put(LIST_ENVIRONMENTS, new Pair<>("",
            "List the environments under current container and testrig"));
      descs.put(LIST_QUESTIONS, new Pair<>("",
            "List the questions under current container and testrig"));
      descs.put(LIST_TESTRIGS,
            new Pair<>("", "List the testrigs within the current container"));
      descs.put(PROMPT, new Pair<>("", "Prompts for user to press enter"));
      descs.put(PWD, new Pair<>("", "Prints the working directory"));
      descs.put(QUIT, new Pair<>("", "Terminate interactive client session"));
      descs.put(REINIT_DELTA_TESTRIG, new Pair<>("",
            "Reinitialize the delta testrig with default environment"));
      descs.put(REINIT_TESTRIG, new Pair<>("",
            "Reinitialize the testrig with default environment"));
      descs.put(SET_BATFISH_LOGLEVEL,
            new Pair<>("<debug|info|output|warn|error>",
                  "Set the batfish loglevel. Default is warn"));
      descs.put(SET_CONTAINER,
            new Pair<>("<container-name>", "Set the current container"));
      descs.put(SET_DELTA_ENV,
            new Pair<>("<environment-name>", "Set the delta environment"));
      descs.put(SET_DELTA_TESTRIG, new Pair<>(
            "<testrig-name> [environment name]", "Set the delta testrig"));
      descs.put(SET_ENV, new Pair<>("<environment-name>",
            "Set the current base environment"));
      descs.put(SET_LOGLEVEL, new Pair<>("<debug|info|output|warn|error>",
            "Set the client loglevel. Default is output"));
      descs.put(SET_PRETTY_PRINT,
            new Pair<>("<true|false>", "Whether to pretty print answers"));
      descs.put(SET_TESTRIG, new Pair<>("<testrig-name> [environment name]",
            "Set the base testrig"));
      descs.put(SHOW_API_KEY, new Pair<>("", "Show API Key"));
      descs.put(SHOW_BATFISH_LOGLEVEL,
            new Pair<>("", "Show current batfish loglevel"));
      descs.put(SHOW_BATFISH_OPTIONS, new Pair<>("",
            "Show the additional options that will be sent to batfish"));
      descs.put(SHOW_CONTAINER, new Pair<>("", "Show active container"));
      descs.put(SHOW_COORDINATOR_HOST, new Pair<>("", "Show coordinator host"));
      descs.put(SHOW_LOGLEVEL, new Pair<>("", "Show current client loglevel"));
      descs.put(SHOW_DELTA_TESTRIG,
            new Pair<>("", "Show delta testrig and environment"));
      descs.put(SHOW_TESTRIG,
            new Pair<>("", "Show base testrig and environment"));
      descs.put(TEST, new Pair<>("<reference file> <command>",
            "Show base testrig and environment"));
      descs.put(UPLOAD_CUSTOM_OBJECT, new Pair<>("<object-name> <object-file>",
            "Uploads a custom object"));
      return descs;
   }

   public static Command fromName(String name) {
      Command instance = _nameMap.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("Not a valid command: \"" + name + "\"");
      }
      return instance;
   }

   public static Map<String, Command> getNameMap() {
      return _nameMap;
   }

   public static Map<Command, Pair<String, String>> getUsageMap() {
      return _usageMap;
   }

   private final String _name;

   private Command(String name) {
      _name = name;
   }

   public String commandName() {
      return _name;
   }
}
