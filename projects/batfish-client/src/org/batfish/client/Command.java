package org.batfish.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;

public enum Command {
   ANSWER("answer"),
   ANSWER_DELTA("answer-delta"),
   CAT("cat"),
   CHECK_API_KEY("checkapikey"),
   CLEAR_SCREEN("cls"),
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
      Map<String, Command> map = new HashMap<String, Command>();
      for (Command value : Command.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   private static Map<Command, Pair<String, String>> buildUsageMap() {
      Map<Command, Pair<String, String>> descs = new TreeMap<Command, Pair<String, String>>();
      descs.put(ANSWER, new Pair<String,String>(
            "<question-file> [param1=value1 [param2=value2] ...]",
            "Answer the question in the file for the default environment"));
      descs.put(ANSWER_DELTA, new Pair<String,String>(
            "<question-file>  [param1=value1 [param2=value2] ...]",
            "Answer the question in the file for the delta environment"));
      descs.put(CAT, new Pair<String,String>(
            "<filename>",
            "Print the contents of the file"));
      descs.put(CHECK_API_KEY, new Pair<String,String>(
            "",
            "Check if API Key is valid"));
      // descs.put(CHANGE_DIR, CHANGE_DIR
      // + " <dirname>\n"
      // + "\t Change the working directory");
      descs.put(CLEAR_SCREEN, new Pair<String,String>(
            "",
            "Clear screen"));
      descs.put(DEL_CONTAINER, new Pair<String,String>(
            "<container-name>",
            "Delete the specified container"));
      descs.put(DEL_ENVIRONMENT, new Pair<String,String>(
            "<environment-name>",
            "Delete the specified environment"));
      descs.put(DEL_QUESTION, new Pair<String,String>(
            "<question-name>",
            "Delete the specified question"));
      descs.put(DEL_TESTRIG, new Pair<String,String>(
            "<testrig-name>",
            "Delete the specified testrig"));
      descs.put(DIR, new Pair<String,String>(
            "<dir>",
            "List directory contents"));
      descs.put(ECHO, new Pair<String,String>(
            "<message>",
            "Echo the message"));
      descs.put(EXIT, new Pair<String,String>(
            "",
            "Terminate interactive client session"));
      descs.put(GEN_DELTA_DP, new Pair<String,String>(
            "",
            "Generate dataplane for the delta environment"));
      descs.put(GEN_DP, new Pair<String,String>(
            "",
            "Generate dataplane for the default environment"));
      descs.put(GET, new Pair<String,String>(
            "<question-type>  [param1=value1 [param2=value2] ...]",
            "Answer the question by type for the delta environment"));
      descs.put(GET_ANSWER, new Pair<String,String>(
            "[-html] <question-name>",
            "Get the answer for a previously answered question"));
      descs.put(GET_DELTA, new Pair<String,String>(
            "<question-file>  [param1=value1 [param2=value2] ...]",
            "Answer the question by type for the delta environment"));
      descs.put(GET_QUESTION, new Pair<String,String>(
            "<question-name>",
            "Get the question and parameter files"));
      descs.put(HELP, new Pair<String,String>(
            "[command]",
            "Print the list of supported commands"));
      descs.put(INIT_CONTAINER, new Pair<String,String>(
            "[<container-name-prefix>]",
            "Initialize a new container"));
      descs.put(INIT_DELTA_ENV, new Pair<String,String>(
            "[-nodataplane] <environment zipfile or directory> [<environment-name>]",
            "Initialize the delta environment"));
      descs.put(INIT_DELTA_TESTRIG,new Pair<String,String>(
            "[-nodataplane] <testrig zipfile or directory> [<environment name>]",
            "Initialize the delta testrig with default environment"));
      descs.put(INIT_TESTRIG, new Pair<String,String>(
            "[-nodataplane] <testrig zipfile or directory> [<environment name>]",
            "Initialize the testrig with default environment"));
      descs.put(LIST_CONTAINERS, new Pair<String,String>( 
            "",
            "List the containers to which you have access"));
      descs.put(LIST_ENVIRONMENTS, new Pair<String,String>(
            "",
            "List the environments under current container and testrig"));
      descs.put(LIST_QUESTIONS, new Pair<String,String>(
            "",
            "List the questions under current container and testrig"));
      descs.put(LIST_TESTRIGS, new Pair<String,String>(
            "",
            "List the testrigs within the current container"));
      descs.put(PROMPT, new Pair<String,String>(
            "",
            "Prompts for user to press enter"));
      descs.put(PWD, new Pair<String,String>(
            "",
            "Prints the working directory"));
      descs.put(QUIT, new Pair<String,String>(
            "",
            "Terminate interactive client session"));
      descs.put(SET_BATFISH_LOGLEVEL, new Pair<String,String>(
            "<debug|info|output|warn|error>",
            "Set the batfish loglevel. Default is warn"));
      descs.put(SET_CONTAINER, new Pair<String,String>(
            "<container-name>",
            "Set the current container"));
      descs.put(SET_DELTA_ENV, new Pair<String,String>(
            "<environment-name>",
            "Set the delta environment"));
      descs.put(SET_DELTA_TESTRIG, new Pair<String,String>(
            "<testrig-name> [environment name]",
            "Set the delta testrig"));
      descs.put(SET_ENV, new Pair<String,String>(
            "<environment-name>",
            "Set the current base environment"));
      descs.put(SET_LOGLEVEL, new Pair<String,String>(
            "<debug|info|output|warn|error>",
            "Set the client loglevel. Default is output"));
      descs.put(SET_PRETTY_PRINT, new Pair<String,String>(
            "<true|false>",
            "Whether to pretty print answers"));
      descs.put(SET_TESTRIG, new Pair<String,String>(
            "<testrig-name> [environment name]",
            "Set the base testrig"));
      descs.put(SHOW_API_KEY, new Pair<String,String>(
            "",
            "Show API Key"));
      descs.put(SHOW_BATFISH_LOGLEVEL, new Pair<String,String>(
            "",
            "Show current batfish loglevel"));
      descs.put(SHOW_CONTAINER, new Pair<String,String>(
            "",
            "Show active container"));
      descs.put(SHOW_COORDINATOR_HOST, new Pair<String,String>(
            "",
            "Show coordinator host"));
      descs.put(SHOW_LOGLEVEL, new Pair<String,String>(
            "",
            "Show current client loglevel"));
      descs.put(SHOW_DELTA_TESTRIG, new Pair<String,String>(
            "",
            "Show delta testrig and environment"));
      descs.put(SHOW_TESTRIG, new Pair<String,String>(
            "",
            "Show base testrig and environment"));
      descs.put(TEST, new Pair<String,String>(
            "<reference file> <command>",
            "Show base testrig and environment"));
      descs.put(UPLOAD_CUSTOM_OBJECT, new Pair<String,String>(
            "<object-name> <object-file>",
            "Uploads a custom object"));
      return descs;
   }

   public static Command fromName(String name) {
      Command instance = _nameMap.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("Not a valid QuestionType: \"" + name
               + "\"");
      }
      return instance;
   }

   private final String _name;

   private Command(String name) {
      _name = name;
   }

   public String commandName() {
      return _name;
   }

   public static Map<String, Command> getNameMap() {
      return _nameMap;
   }
   
   public static Map<Command, Pair<String,String>> getUsageMap() {
      return _usageMap;
   }
}
