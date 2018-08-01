package org.batfish.client;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;

public enum Command {
  ADD_ANALYSIS_QUESTIONS("add-analysis-questions"),
  ADD_BATFISH_OPTION("add-batfish-option"),
  ANSWER("answer"),
  ANSWER_DELTA("answer-delta"),
  AUTOCOMPLETE("autocomplete"),
  CAT("cat"),
  CHECK_API_KEY("checkapikey"),
  CLEAR_SCREEN("cls"),
  CONFIGURE_TEMPLATE("configure-template"),
  DEL_ANALYSIS("del-analysis"),
  DEL_ANALYSIS_QUESTIONS("del-analysis-questions"),
  DEL_BATFISH_OPTION("del-batfish-option"),
  DEL_CONTAINER("del-container"),
  DEL_ENVIRONMENT("del-environment"),
  DEL_NETWORK("del-network"),
  DEL_QUESTION("del-question"),
  DEL_SNAPSHOT("del-snapshot"),
  DEL_TESTRIG("del-testrig"),
  DIR("dir"),
  ECHO("echo"),
  EXIT("exit"),
  GEN_DELTA_DP("generate-delta-dataplane"),
  GEN_DP("generate-dataplane"),
  GET("get"),
  GET_ANALYSIS_ANSWERS("get-analysis-answers"),
  GET_ANALYSIS_ANSWERS_DELTA("get-analysis-answers-delta"),
  GET_ANALYSIS_ANSWERS_DIFFERENTIAL("get-analysis-answers-differential"),
  GET_ANSWER("get-answer"),
  GET_ANSWER_DELTA("get-answer-delta"),
  GET_ANSWER_DIFFERENTIAL("get-answer-differential"),
  GET_CONFIGURATION("get-configuration"),
  GET_CONTAINER("get-container"),
  GET_DELTA("get-delta"),
  GET_NETWORK("get-network"),
  GET_OBJECT("get-object"),
  GET_OBJECT_DELTA("get-delta-object"),
  GET_QUESTION("get-question"),
  GET_QUESTION_TEMPLATES("get-question-templates"),
  GET_WORK_STATUS("get-work-status"),
  HELP("help"),
  INIT_ANALYSIS("init-analysis"),
  INIT_CONTAINER("init-container"),
  INIT_DELTA_SNAPSHOT("init-delta-snapshot"),
  INIT_DELTA_TESTRIG("init-delta-testrig"),
  INIT_ENVIRONMENT("init-environment"),
  INIT_NETWORK("init-network"),
  INIT_SNAPSHOT("init-snapshot"),
  INIT_TESTRIG("init-testrig"),
  KILL_WORK("kill-work"),
  LIST_ANALYSES("list-analyses"),
  LIST_CONTAINERS("list-containers"),
  LIST_ENVIRONMENTS("list-environments"),
  LIST_INCOMPLETE_WORK("list-incomplete-work"),
  LIST_NETWORKS("list-networks"),
  LIST_QUESTIONS("list-questions"),
  LIST_SNAPSHOTS("list-snapshots"),
  LIST_TESTRIGS("list-testrigs"),
  LOAD_QUESTIONS("load-questions"),
  POLL_WORK("poll-work"),
  PROMPT("prompt"),
  PWD("pwd"),
  QUIT("quit"),
  RUN_ANALYSIS("run-analysis"),
  RUN_ANALYSIS_DELTA("run-analysis-delta"),
  RUN_ANALYSIS_DIFFERENTIAL("run-analysis-differential"),
  SET_BACKGROUND_EXECUCTION("set-background-execution"),
  SET_BATFISH_LOGLEVEL("set-batfish-loglevel"),
  SET_CONTAINER("set-container"),
  SET_DELTA_ENV("set-delta-environment"),
  SET_DELTA_SNAPSHOT("set-delta-snapshot"),
  SET_DELTA_TESTRIG("set-delta-testrig"),
  SET_ENV("set-environment"),
  SET_FIXED_WORKITEM_ID("set-fixed-workitem-id"),
  SET_LOGLEVEL("set-loglevel"),
  SET_NETWORK("set-network"),
  SET_PRETTY_PRINT("set-pretty-print"),
  SET_SNAPSHOT("set-snapshot"),
  SET_TESTRIG("set-testrig"),
  SHOW_API_KEY("show-api-key"),
  SHOW_BATFISH_LOGLEVEL("show-batfish-loglevel"),
  SHOW_BATFISH_OPTIONS("show-batfish-options"),
  SHOW_CONTAINER("show-container"),
  SHOW_COORDINATOR_HOST("show-coordinator-host"),
  SHOW_DELTA_SNAPSHOT("show-delta-snapshot"),
  SHOW_DELTA_TESTRIG("show-delta-testrig"),
  SHOW_LOGLEVEL("show-loglevel"),
  SHOW_NETWORK("show-network"),
  SHOW_SNAPSHOT("show-snapshot"),
  SHOW_TESTRIG("show-testrig"),
  SHOW_VERSION("show-version"),
  SYNC_SNAPSHOTS_SYNC_NOW("sync-snapshots-sync-now"),
  SYNC_TESTRIGS_SYNC_NOW("sync-testrigs-sync-now"),
  SYNC_SNAPSHOTS_UPDATE_SETTINGS("sync-snapshots-update-settings"),
  SYNC_TESTRIGS_UPDATE_SETTINGS("sync-testrigs-update-settings"),
  TEST("test"),
  UPLOAD_CUSTOM_OBJECT("upload-custom"),
  VALIDATE_TEMPLATE("validate-template");

  public enum TestComparisonMode {
    COMPAREANSWER,
    COMPAREALL,
    COMPAREFAILURES,
    COMPARESUMMARY,
    RAW
  }

  private static final Map<String, Command> _nameMap = buildNameMap();

  private static final Map<Command, String> _deprecatedMap = buildDeprecatedMap();

  private static final Map<Command, Pair<String, String>> _usageMap = buildUsageMap();

  private static Map<String, Command> buildNameMap() {
    ImmutableMap.Builder<String, Command> map = ImmutableMap.builder();
    for (Command value : Command.values()) {
      String name = value._name;
      map.put(name, value);
    }
    return map.build();
  }

  private static Map<Command, String> buildDeprecatedMap() {
    ImmutableMap.Builder<Command, String> map = ImmutableMap.builder();

    String containerMsg = "The term \"container\" has been replaced with \"network\".";
    String testrigMsg = "The term \"testrig\" has been replaced with \"snapshot\".";

    map.put(DEL_CONTAINER, getDeprecatedMsg(containerMsg, DEL_NETWORK));
    map.put(DEL_TESTRIG, getDeprecatedMsg(testrigMsg, DEL_SNAPSHOT));
    map.put(GET_CONTAINER, getDeprecatedMsg(containerMsg, GET_NETWORK));
    map.put(INIT_CONTAINER, getDeprecatedMsg(containerMsg, INIT_NETWORK));
    map.put(INIT_DELTA_TESTRIG, getDeprecatedMsg(testrigMsg, INIT_DELTA_SNAPSHOT));
    map.put(INIT_TESTRIG, getDeprecatedMsg(testrigMsg, INIT_SNAPSHOT));
    map.put(LIST_CONTAINERS, getDeprecatedMsg(containerMsg, LIST_NETWORKS));
    map.put(LIST_TESTRIGS, getDeprecatedMsg(testrigMsg, LIST_SNAPSHOTS));
    map.put(SET_CONTAINER, getDeprecatedMsg(containerMsg, SET_NETWORK));
    map.put(SET_DELTA_TESTRIG, getDeprecatedMsg(testrigMsg, SET_DELTA_SNAPSHOT));
    map.put(SET_TESTRIG, getDeprecatedMsg(testrigMsg, SET_SNAPSHOT));
    map.put(SHOW_CONTAINER, getDeprecatedMsg(containerMsg, SHOW_NETWORK));
    map.put(SHOW_DELTA_TESTRIG, getDeprecatedMsg(testrigMsg, SHOW_DELTA_SNAPSHOT));
    map.put(SHOW_TESTRIG, getDeprecatedMsg(testrigMsg, SHOW_SNAPSHOT));
    map.put(SYNC_TESTRIGS_SYNC_NOW, getDeprecatedMsg(testrigMsg, SYNC_SNAPSHOTS_SYNC_NOW));
    map.put(
        SYNC_TESTRIGS_UPDATE_SETTINGS,
        getDeprecatedMsg(testrigMsg, SYNC_SNAPSHOTS_UPDATE_SETTINGS));
    return map.build();
  }

  private static String getDeprecatedMsg(String reason, Command replacement) {
    return String.format("%s Use %s instead.", reason, replacement.commandName());
  }

  private static Map<Command, Pair<String, String>> buildUsageMap() {
    Map<Command, Pair<String, String>> descs = new TreeMap<>();
    descs.put(
        ADD_ANALYSIS_QUESTIONS,
        new Pair<>(
            "<analysis-name> <question-directory>",
            "Add questions from the directory to the analysis"));
    descs.put(
        ADD_BATFISH_OPTION,
        new Pair<>(
            "<option-key> [<option-value> [<option-value>] ... ]",
            "Additional options to pass to Batfish"));
    descs.put(
        ANSWER,
        new Pair<>(
            "<template-name>  [questionName=name] [param1=value1 [param2=value2] ...]",
            "Answer the template by name for the base environment"));
    descs.put(
        ANSWER_DELTA,
        new Pair<>(
            "<template-name>   [questionName=name] [param1=value1 [param2=value2] ...]",
            "Answer the template by name for the delta environment"));
    descs.put(
        AUTOCOMPLETE,
        new Pair<>(
            "[-maxSuggestions] <completion-type> <query>",
            "Autocomplete information of question parameters"));
    descs.put(CAT, new Pair<>("<filename>", "Print the contents of the file"));
    descs.put(CHECK_API_KEY, new Pair<>("", "Check if API Key is valid"));
    descs.put(CLEAR_SCREEN, new Pair<>("", "Clear screen"));
    descs.put(
        CONFIGURE_TEMPLATE,
        new Pair<>(
            "<new-template-name> <old-template-name> [exceptions=[...],] [assertion={..}]",
            "Create a new template from the old template with provided exceptions and assertion"));
    descs.put(DEL_ANALYSIS, new Pair<>("<analysis-name>", "Delete the analysis completely"));
    descs.put(
        DEL_ANALYSIS_QUESTIONS,
        new Pair<>(
            "<analysis-name> qname1 [qname2 [qname3] ...]", "Delete questions from the analysis"));
    descs.put(
        DEL_BATFISH_OPTION, new Pair<>("<option-key>", "Stop passing this option to Batfish"));
    descs.put(DEL_CONTAINER, new Pair<>("<network-name>", "Delete the specified network"));
    descs.put(
        DEL_ENVIRONMENT, new Pair<>("<environment-name>", "Delete the specified environment"));
    descs.put(DEL_NETWORK, new Pair<>("<network-name>", "Delete the specified network"));
    descs.put(DEL_QUESTION, new Pair<>("<question-name>", "Delete the specified question"));
    descs.put(DEL_SNAPSHOT, new Pair<>("<snapshot-name>", "Delete the specified snapshot"));
    descs.put(DEL_TESTRIG, new Pair<>("<snapshot-name>", "Delete the specified snapshot"));
    descs.put(DIR, new Pair<>("<dir>", "List directory contents"));
    descs.put(ECHO, new Pair<>("<message>", "Echo the message"));
    descs.put(EXIT, new Pair<>("", "Terminate interactive client session"));
    descs.put(GEN_DELTA_DP, new Pair<>("", "Generate dataplane for the delta environment"));
    descs.put(GEN_DP, new Pair<>("", "Generate dataplane for the default environment"));
    descs.put(
        GET,
        new Pair<>(
            "<question-type>  [param1=value1 [param2=value2] ...]",
            "Answer the question by type for the base environment"));
    descs.put(
        GET_ANALYSIS_ANSWERS,
        new Pair<>("<analysis-name>", "Get the answers for a previously run analysis"));
    descs.put(
        GET_ANSWER,
        new Pair<>("<question-name>", "Get the answer for a previously answered question"));
    descs.put(
        GET_CONFIGURATION,
        new Pair<>(
            "<network-name> <snapshot-name> <configuration-name>",
            "Get the file content of the configuration file"));
    descs.put(GET_CONTAINER, new Pair<>("<network-name>", "Get the information of the network"));
    descs.put(
        GET_DELTA,
        new Pair<>(
            "<question-file>  [param1=value1 [param2=value2] ...]",
            "Answer the question by type for the delta environment"));
    descs.put(GET_NETWORK, new Pair<>("<network-name>", "Get the information of the network"));
    descs.put(GET_OBJECT, new Pair<>("<object path>", "Get the object"));
    descs.put(GET_OBJECT_DELTA, new Pair<>("<object path>", "Get the object from delta snapshot"));
    descs.put(GET_QUESTION, new Pair<>("<question-name>", "Get the question and parameter files"));
    descs.put(GET_QUESTION_TEMPLATES, new Pair<>("", "Get question templates from coordinator"));
    descs.put(GET_WORK_STATUS, new Pair<>("<work-id>", "Get the status of the specified work id"));
    descs.put(HELP, new Pair<>("[command]", "Print the list of supported commands"));
    descs.put(
        INIT_ANALYSIS,
        new Pair<>(
            "<analysis-name> <question-directory>", "Initialize a new analysis for the network"));
    descs.put(
        INIT_CONTAINER,
        new Pair<>(
            "[-setname <network-name> | <network-name-prefix>]", "Initialize a new network"));
    descs.put(
        INIT_DELTA_SNAPSHOT,
        new Pair<>(
            "[-autoanalyze] <snapshot zipfile or directory> [<snapshot-name>]",
            "Initialize the delta snapshot with default environment"));
    descs.put(
        INIT_DELTA_TESTRIG,
        new Pair<>(
            "[-autoanalyze] <snapshot zipfile or directory> [<snapshot-name>]",
            "Initialize the delta snapshot with default environment"));
    descs.put(
        INIT_ENVIRONMENT,
        new Pair<>(
            "[sourcePath=path], [newEnvironmentName=string], [newEnvironmentPrefix=string], "
                + "[sourceEnvironmentName=string], [nodeBlacklist=string_set], "
                + "[interfaceBlacklist=map_of_strings_to_string_sets], [edgeBlacklist=edge_set]",
            "    Initialize a new delta environment\n"
                + "\n"
                + "    Arguments:\n"
                + "\n"
                + "    sourcePath\n"
                + "        Either a directory or zip containing the environment to initialize.\n"
                + "        These files override those in the environment identified by "
                + "'sourceEnvironmentName',\n"
                + "        and are overridden by values passed to node/interface/edgeBlacklist.\n"
                + "\n"
                + "    newEnvironmentName\n"
                + "        The name to assign the new environment. If not specified, a name is "
                + "generated.\n"
                + "\n"
                + "    sourceEnvironmentName\n"
                + "        The name of an environment in the current snapshot from which to clone "
                + "a new environment.\n"
                + "        Files in the source environment are overriden by those in envDirOrZip "
                + "(if specified),\n"
                + "        as well as by values passed to node/interface/edgeBlacklist.\n"
                + "\n"
                + "    nodeBlacklist\n"
                + "        A list of nodes whose interfaces will be turned off in the new "
                + "environment.\n"
                + "\n"
                + "    interfaceBlacklist\n"
                + "        A list of interfaces that will be turned off in the new environment.\n"
                + "\n"
                + "    edgeBlacklist\n"
                + "        For maximum granularity, a list of edges to be disabled in the new "
                + "environment. This\n"
                + "        option should rarely be used. The interfaces making up the edge will "
                + "not necessarily\n"
                + "        be disabled. This feature is experimental, and may not always yield "
                + "expected results.\n"
                + "\n"
                + "    doDelta\n"
                + "        Whether the sourceEnvironment and newEnvironment should be chosen "
                + "from/created in the\n"
                + "        current base(false) or delta(true) snapshot. Defaults to false.\n"
                + "\n"));
    /*                + "    update\n"
    + "        Whether to update the current base(doDelta=false)/delta(doDelta=true) "
    + "environment\n"
    + "        pointer after this action has completed. Regardless of the value of "
    + "this option,\n"
    + "        bf_session.scenarios appended with a new scenario corresponding to the "
    + "newly-created\n"
    + "        environment.\n"));*/
    descs.put(
        INIT_NETWORK,
        new Pair<>(
            "[-setname <network-name> | <network-name-prefix>]", "Initialize a new network"));
    descs.put(
        INIT_SNAPSHOT,
        new Pair<>(
            "[-autoanalyze] <snapshot zipfile or directory> [<snapshot-name>]",
            "Initialize the snapshot with default environment"));
    descs.put(
        INIT_TESTRIG,
        new Pair<>(
            "[-autoanalyze] <snapshot zipfile or directory> [<snapshot-name>]",
            "Initialize the snapshot with default environment"));
    descs.put(KILL_WORK, new Pair<>("<guid>", "Kill work with the given GUID"));
    descs.put(LIST_ANALYSES, new Pair<>("", "List the analyses and their configuration"));
    descs.put(LIST_CONTAINERS, new Pair<>("", "List the networks to which you have access"));
    descs.put(
        LIST_INCOMPLETE_WORK, new Pair<>("", "List all incomplete works for the active network"));
    descs.put(
        LIST_ENVIRONMENTS,
        new Pair<>("", "List the environments under current network and snapshot"));
    descs.put(LIST_NETWORKS, new Pair<>("", "List the networks to which you have access"));
    descs.put(
        LIST_QUESTIONS, new Pair<>("", "List the questions under current network and snapshot"));
    descs.put(
        LIST_SNAPSHOTS,
        new Pair<>("[-nometadata]", "List the snapshots within the current network"));
    descs.put(
        LIST_TESTRIGS,
        new Pair<>("[-nometadata]", "List the snapshots within the current network"));
    descs.put(
        LOAD_QUESTIONS,
        new Pair<>(
            "[-loadremote] [path to local directory containing question json files]",
            "Load questions from local directory, -loadremote loads questions from coordinator, "
                + "if both are specified, questions from local directory overwrite the remote "
                + "questions"));
    descs.put(POLL_WORK, new Pair<>("<guid>", "Poll work with the given GUID until done"));
    descs.put(PROMPT, new Pair<>("", "Prompts for user to press enter"));
    descs.put(PWD, new Pair<>("", "Prints the working directory"));
    descs.put(QUIT, new Pair<>("", "Terminate interactive client session"));
    descs.put(
        RUN_ANALYSIS, new Pair<>("<analysis-name>", "Run the (previously configured) analysis"));
    descs.put(
        SET_BACKGROUND_EXECUCTION,
        new Pair<>("<true|false>", "Whether to wait for commands to finish before returning"));
    descs.put(
        SET_BATFISH_LOGLEVEL,
        new Pair<>("<debug|info|output|warn|error>", "Set the batfish loglevel. Default is warn"));
    descs.put(SET_CONTAINER, new Pair<>("<network-name>", "Set the current network"));
    descs.put(SET_DELTA_ENV, new Pair<>("<environment-name>", "Set the delta environment"));
    descs.put(
        SET_DELTA_SNAPSHOT,
        new Pair<>("<snapshot-name> [environment name]", "Set the delta snapshot"));
    descs.put(
        SET_DELTA_TESTRIG,
        new Pair<>("<snapshot-name> [environment name]", "Set the delta snapshot"));
    descs.put(SET_ENV, new Pair<>("<environment-name>", "Set the current base environment"));
    descs.put(
        SET_FIXED_WORKITEM_ID,
        new Pair<>("<uuid>", "Fix the UUID for WorkItems. Useful for testing; use carefully"));
    descs.put(
        SET_LOGLEVEL,
        new Pair<>("<debug|info|output|warn|error>", "Set the client loglevel. Default is output"));
    descs.put(SET_NETWORK, new Pair<>("<network-name>", "Set the current network"));
    descs.put(SET_PRETTY_PRINT, new Pair<>("<true|false>", "Whether to pretty print answers"));
    descs.put(
        SET_SNAPSHOT, new Pair<>("<snapshot-name> [environment name]", "Set the base snapshot"));
    descs.put(
        SET_TESTRIG, new Pair<>("<snapshot-name> [environment name]", "Set the base snapshot"));
    descs.put(SHOW_API_KEY, new Pair<>("", "Show API Key"));
    descs.put(SHOW_BATFISH_LOGLEVEL, new Pair<>("", "Show current batfish loglevel"));
    descs.put(
        SHOW_BATFISH_OPTIONS,
        new Pair<>("", "Show the additional options that will be sent to batfish"));
    descs.put(SHOW_CONTAINER, new Pair<>("", "Show active network"));
    descs.put(SHOW_COORDINATOR_HOST, new Pair<>("", "Show coordinator host"));
    descs.put(SHOW_DELTA_SNAPSHOT, new Pair<>("", "Show delta snapshot and environment"));
    descs.put(SHOW_DELTA_TESTRIG, new Pair<>("", "Show delta snapshot and environment"));
    descs.put(SHOW_LOGLEVEL, new Pair<>("", "Show current client loglevel"));
    descs.put(SHOW_NETWORK, new Pair<>("", "Show active network"));
    descs.put(SHOW_SNAPSHOT, new Pair<>("", "Show base snapshot and environment"));
    descs.put(SHOW_TESTRIG, new Pair<>("", "Show base snapshot and environment"));
    descs.put(SHOW_VERSION, new Pair<>("", "Show the version of Client and Service"));
    descs.put(
        SYNC_SNAPSHOTS_SYNC_NOW,
        new Pair<>(
            "[-force] <plugin-id>",
            "Sync snapshots now (settings must have been configured before)"));
    descs.put(
        SYNC_TESTRIGS_SYNC_NOW,
        new Pair<>(
            "[-force] <plugin-id>",
            "Sync snapshots now (settings must have been configured before)"));
    descs.put(
        SYNC_SNAPSHOTS_UPDATE_SETTINGS,
        new Pair<>(
            "<plugin-id> [key1=value1, [key2=value2], ...], ",
            "Update the settings for sync snapshots plugin"));
    descs.put(
        SYNC_TESTRIGS_UPDATE_SETTINGS,
        new Pair<>(
            "<plugin-id> [key1=value1, [key2=value2], ...], ",
            "Update the settings for sync snapshots plugin"));
    descs.put(
        TEST,
        new Pair<>(
            "["
                + Arrays.stream(TestComparisonMode.values())
                    .map(v -> '-' + v.toString())
                    .collect(Collectors.joining("|"))
                + "] <ref file> <command>",
            "Run the command and compare its output to the ref file (used for testing)"));
    descs.put(
        UPLOAD_CUSTOM_OBJECT, new Pair<>("<object-name> <object-file>", "Uploads a custom object"));
    descs.put(
        VALIDATE_TEMPLATE,
        new Pair<>("<template-file> [param1=value1 [param2=value2] ...]", "Validate the template"));
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

  public static Map<Command, String> getDeprecatedMap() {
    return _deprecatedMap;
  }

  public static Map<Command, Pair<String, String>> getUsageMap() {
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
