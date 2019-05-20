package org.batfish.client;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;

@SuppressWarnings("deprecation") // todo
public enum Command {
  ADD_BATFISH_OPTION("add-batfish-option"),
  ANSWER("answer"),
  ANSWER_REFERENCE("answer-reference"),
  AUTOCOMPLETE("autocomplete"),
  CHECK_API_KEY("checkapikey"),
  CONFIGURE_TEMPLATE("configure-template"),
  DEBUG_DELETE("debug-delete"),
  DEBUG_GET("debug-get"),
  DEBUG_POST("debug-post"),
  DEBUG_PUT("debug-put"),
  DEL_BATFISH_OPTION("del-batfish-option"),
  DEL_NETWORK("del-network"),
  DEL_QUESTION("del-question"),
  DEL_SNAPSHOT("del-snapshot"),
  EXIT("exit"),
  GEN_DP("generate-dataplane"),
  GEN_REFERENCE_DP("generate-reference-dataplane"),
  GET("get"),
  GET_ANSWER("get-answer"),
  GET_ANSWER_DIFFERENTIAL("get-answer-differential"),
  GET_ANSWER_REFERENCE("get-answer-reference"),
  GET_CONFIGURATION("get-configuration"),
  GET_NETWORK("get-network"),
  GET_OBJECT("get-object"),
  GET_OBJECT_REFERENCE("get-reference-object"),
  GET_QUESTION_TEMPLATES("get-question-templates"),
  GET_REFERENCE("get-reference"),
  GET_WORK_STATUS("get-work-status"),
  HELP("help"),
  INIT_NETWORK("init-network"),
  INIT_REFERENCE_SNAPSHOT("init-reference-snapshot"),
  INIT_SNAPSHOT("init-snapshot"),
  KILL_WORK("kill-work"),
  LIST_INCOMPLETE_WORK("list-incomplete-work"),
  LIST_NETWORKS("list-networks"),
  LIST_QUESTIONS("list-questions"),
  LIST_SNAPSHOTS("list-snapshots"),
  LOAD_QUESTIONS("load-questions"),
  QUIT("quit"),
  SET_BACKGROUND_EXECUCTION("set-background-execution"),
  SET_BATFISH_LOGLEVEL("set-batfish-loglevel"),
  SET_FIXED_WORKITEM_ID("set-fixed-workitem-id"),
  SET_LOGLEVEL("set-loglevel"),
  SET_NETWORK("set-network"),
  SET_PRETTY_PRINT("set-pretty-print"),
  SET_REFERENCE_SNAPSHOT("set-reference-snapshot"),
  SET_SNAPSHOT("set-snapshot"),
  SHOW_API_KEY("show-api-key"),
  SHOW_BATFISH_LOGLEVEL("show-batfish-loglevel"),
  SHOW_BATFISH_OPTIONS("show-batfish-options"),
  SHOW_COORDINATOR_HOST("show-coordinator-host"),
  SHOW_LOGLEVEL("show-loglevel"),
  SHOW_NETWORK("show-network"),
  SHOW_REFERENCE_SNAPSHOT("show-reference-snapshot"),
  SHOW_SNAPSHOT("show-snapshot"),
  SHOW_VERSION("show-version"),
  TEST("test"),
  UPLOAD_CUSTOM_OBJECT("upload-custom"),
  VALIDATE_TEMPLATE("validate-template");

  public enum TestComparisonMode {
    COMPAREANSWER,
    COMPAREALL,
    COMPAREFAILURES,
    COMPARESUMMARY,
    JSON,
    RAW
  }

  private static final Map<String, Command> _nameMap = buildNameMap();

  @SuppressWarnings("deprecation") // Client is deprecated anyway
  private static final Map<Command, org.batfish.common.Pair<String, String>> _usageMap =
      buildUsageMap();

  private static Map<String, Command> buildNameMap() {
    ImmutableMap.Builder<String, Command> map = ImmutableMap.builder();
    for (Command value : Command.values()) {
      String name = value._name;
      map.put(name, value);
    }
    return map.build();
  }

  @SuppressWarnings("deprecation") // Client is deprecated anyway
  private static Map<Command, org.batfish.common.Pair<String, String>> buildUsageMap() {
    Map<Command, org.batfish.common.Pair<String, String>> descs = new TreeMap<>();
    descs.put(
        ADD_BATFISH_OPTION,
        new org.batfish.common.Pair<>(
            "<option-key> [<option-value> [<option-value>] ... ]",
            "Additional options to pass to Batfish"));
    descs.put(
        ANSWER,
        new org.batfish.common.Pair<>(
            "<template-name> [differential={true,false}] [questionName=name] [param1=value1 [param2=value2] ...]",
            "Answer the template by name for the current snapshot"));
    descs.put(
        ANSWER_REFERENCE,
        new org.batfish.common.Pair<>(
            "<template-name> [differential={true,false}] [questionName=name] [param1=value1 [param2=value2] ...]",
            "Answer the template by name for the reference snapshot"));
    descs.put(
        AUTOCOMPLETE,
        new org.batfish.common.Pair<>(
            "[-maxSuggestions] <completion-type> <query>",
            "Autocomplete information of question parameters"));
    descs.put(CHECK_API_KEY, new org.batfish.common.Pair<>("", "Check if API Key is valid"));
    descs.put(
        CONFIGURE_TEMPLATE,
        new org.batfish.common.Pair<>(
            "<new-template-name> <old-template-name> [exceptions=[...],] [assertion={..}]",
            "Create a new template from the old template with provided exceptions and assertion"));
    descs.put(
        DEL_BATFISH_OPTION,
        new org.batfish.common.Pair<>("<option-key>", "Stop passing this option to Batfish"));
    descs.put(
        DEL_NETWORK,
        new org.batfish.common.Pair<>("<network-name>", "Delete the specified network"));
    descs.put(
        DEL_QUESTION,
        new org.batfish.common.Pair<>("<question-name>", "Delete the specified question"));
    descs.put(
        DEL_SNAPSHOT,
        new org.batfish.common.Pair<>("<snapshot-name>", "Delete the specified snapshot"));
    descs.put(EXIT, new org.batfish.common.Pair<>("", "Terminate interactive client session"));
    descs.put(
        GEN_DP, new org.batfish.common.Pair<>("", "Generate dataplane for the current snapshot"));
    descs.put(
        GEN_REFERENCE_DP,
        new org.batfish.common.Pair<>("", "Generate dataplane for the reference snapshot"));
    descs.put(
        GET,
        new org.batfish.common.Pair<>(
            "<question-type>  [param1=value1 [param2=value2] ...]",
            "Answer the question by type for the current snapshot"));
    descs.put(
        GET_ANSWER,
        new org.batfish.common.Pair<>(
            "<question-name>", "Get the answer for a previously answered question"));
    descs.put(
        GET_ANSWER_REFERENCE,
        new org.batfish.common.Pair<>(
            "<question-name>",
            "Get the answer for a question previously answered on the reference snapshot"));
    descs.put(
        GET_CONFIGURATION,
        new org.batfish.common.Pair<>(
            "<network-name> <snapshot-name> <configuration-name>",
            "Get the file content of the configuration file"));
    descs.put(
        GET_NETWORK,
        new org.batfish.common.Pair<>("<network-name>", "Get the information of the network"));
    descs.put(GET_OBJECT, new org.batfish.common.Pair<>("<object path>", "Get the object"));
    descs.put(
        GET_OBJECT_REFERENCE,
        new org.batfish.common.Pair<>("<object path>", "Get the object from reference snapshot"));
    descs.put(
        GET_QUESTION_TEMPLATES,
        new org.batfish.common.Pair<>("", "Get question templates from coordinator"));
    descs.put(
        GET_REFERENCE,
        new org.batfish.common.Pair<>(
            "<question-file>  [param1=value1 [param2=value2] ...]",
            "Answer the question by type for the reference snapshot"));
    descs.put(
        GET_WORK_STATUS,
        new org.batfish.common.Pair<>("<work-id>", "Get the status of the specified work id"));
    descs.put(
        HELP, new org.batfish.common.Pair<>("[command]", "Print the list of supported commands"));
    descs.put(
        INIT_NETWORK,
        new org.batfish.common.Pair<>(
            "[-setname <network-name> | <network-name-prefix>]", "Initialize a new network"));
    descs.put(
        INIT_REFERENCE_SNAPSHOT,
        new org.batfish.common.Pair<>(
            "[-autoanalyze] <snapshot zipfile or directory> [<snapshot-name>]",
            "Initialize the reference snapshot"));
    descs.put(
        INIT_SNAPSHOT,
        new org.batfish.common.Pair<>(
            "[-autoanalyze] <snapshot zipfile or directory> [<snapshot-name>]",
            "Initialize the snapshot"));
    descs.put(KILL_WORK, new org.batfish.common.Pair<>("<guid>", "Kill work with the given GUID"));
    descs.put(
        LIST_INCOMPLETE_WORK,
        new org.batfish.common.Pair<>("", "List all incomplete works for the active network"));
    descs.put(
        LIST_NETWORKS,
        new org.batfish.common.Pair<>("", "List the networks to which you have access"));
    descs.put(
        LIST_QUESTIONS,
        new org.batfish.common.Pair<>("", "List the questions under current network and snapshot"));
    descs.put(
        LIST_SNAPSHOTS,
        new org.batfish.common.Pair<>(
            "[-nometadata]", "List the snapshots within the current network"));
    descs.put(
        LOAD_QUESTIONS,
        new org.batfish.common.Pair<>(
            "[-loadremote] [path to local directory containing question json files]",
            "Load questions from local directory, -loadremote loads questions from coordinator, "
                + "if both are specified, questions from local directory overwrite the remote "
                + "questions"));
    descs.put(QUIT, new org.batfish.common.Pair<>("", "Terminate interactive client session"));
    descs.put(
        SET_BACKGROUND_EXECUCTION,
        new org.batfish.common.Pair<>(
            "<true|false>", "Whether to wait for commands to finish before returning"));
    descs.put(
        SET_BATFISH_LOGLEVEL,
        new org.batfish.common.Pair<>(
            "<debug|info|output|warn|error>", "Set the batfish loglevel. Default is warn"));
    descs.put(
        SET_FIXED_WORKITEM_ID,
        new org.batfish.common.Pair<>(
            "<uuid>", "Fix the UUID for WorkItems. Useful for testing; use carefully"));
    descs.put(
        SET_LOGLEVEL,
        new org.batfish.common.Pair<>(
            "<debug|info|output|warn|error>", "Set the client loglevel. Default is output"));
    descs.put(
        SET_NETWORK, new org.batfish.common.Pair<>("<network-name>", "Set the current network"));
    descs.put(
        SET_PRETTY_PRINT,
        new org.batfish.common.Pair<>("<true|false>", "Whether to pretty print answers"));
    descs.put(
        SET_REFERENCE_SNAPSHOT,
        new org.batfish.common.Pair<>("<snapshot-name>", "Set the reference snapshot"));
    descs.put(
        SET_SNAPSHOT, new org.batfish.common.Pair<>("<snapshot-name>", "Set the current snapshot"));
    descs.put(SHOW_API_KEY, new org.batfish.common.Pair<>("", "Show API Key"));
    descs.put(
        SHOW_BATFISH_LOGLEVEL, new org.batfish.common.Pair<>("", "Show current batfish loglevel"));
    descs.put(
        SHOW_BATFISH_OPTIONS,
        new org.batfish.common.Pair<>(
            "", "Show the additional options that will be sent to batfish"));
    descs.put(SHOW_COORDINATOR_HOST, new org.batfish.common.Pair<>("", "Show coordinator host"));
    descs.put(SHOW_LOGLEVEL, new org.batfish.common.Pair<>("", "Show current client loglevel"));
    descs.put(SHOW_NETWORK, new org.batfish.common.Pair<>("", "Show active network"));
    descs.put(
        SHOW_REFERENCE_SNAPSHOT, new org.batfish.common.Pair<>("", "Show reference snapshot"));
    descs.put(SHOW_SNAPSHOT, new org.batfish.common.Pair<>("", "Show current snapshot"));
    descs.put(
        SHOW_VERSION, new org.batfish.common.Pair<>("", "Show the version of Client and Service"));
    descs.put(
        TEST,
        new org.batfish.common.Pair<>(
            "["
                + Arrays.stream(TestComparisonMode.values())
                    .map(v -> '-' + v.toString())
                    .collect(Collectors.joining("|"))
                + "] <ref file> <command>",
            "Run the command and compare its output to the ref file (used for testing)"));
    descs.put(
        UPLOAD_CUSTOM_OBJECT,
        new org.batfish.common.Pair<>("<object-name> <object-file>", "Uploads a custom object"));
    descs.put(
        VALIDATE_TEMPLATE,
        new org.batfish.common.Pair<>(
            "<template-file> [param1=value1 [param2=value2] ...]", "Validate the template"));
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

  public static Map<Command, org.batfish.common.Pair<String, String>> getUsageMap() {
    return _usageMap;
  }

  private final String _name;

  Command(String name) {
    _name = name;
  }

  public String commandName() {
    return _name;
  }
}
