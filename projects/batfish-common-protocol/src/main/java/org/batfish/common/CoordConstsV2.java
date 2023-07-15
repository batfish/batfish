package org.batfish.common;

/** Constants used in the coordinator service version 2. */
public class CoordConstsV2 {
  ////// HTTP Headers that clients are expected to configure. //////

  /** The HTTP Header containing the client's API Key. */
  public static final String HTTP_HEADER_BATFISH_APIKEY = "X-Batfish-Apikey";

  public static final String QP_MAX_SUGGESTIONS = "maxsuggestions";
  public static final String QP_NAME = "name";
  public static final String QP_NAME_PREFIX = "name_prefix";
  public static final String QP_TYPE = "type";
  public static final String QP_VERBOSE = "verbose";
  public static final String QP_QUERY = "query";

  public static final String RSC_ANSWER = "answer";
  public static final String RSC_AUTOCOMPLETE = "autocomplete";
  public static final String RSC_COMPLETED_WORK = "completed_work";
  public static final String RSC_CONTAINER = "container";
  public static final String RSC_CONTAINERS = "containers";
  public static final String RSC_FILTER = "filter";
  public static final String RSC_FORK = "fork";
  public static final String RSC_INFERRED_NODE_ROLES = "inferred_node_roles";
  public static final String RSC_INPUT = "input";
  public static final String RSC_ISSUES = "issues";
  public static final String RSC_LIST = "list";
  public static final String RSC_NETWORK = "network";
  public static final String RSC_NETWORKS = "networks";
  public static final String RSC_NODE_ROLES = "noderoles";
  public static final String RSC_OBJECTS = "objects";
  public static final String RSC_POJO_TOPOLOGY = "pojo_topology";
  public static final String RSC_QUESTION_TEMPLATES = "question_templates";
  public static final String RSC_QUESTIONS = "questions";
  public static final String RSC_REFERENCE_LIBRARY = "referencelibrary";
  public static final String RSC_SETTINGS = "settings";
  public static final String RSC_SNAPSHOTS = "snapshots";
  public static final String RSC_TOPOLOGY = "topology";
  public static final String RSC_VERSION = "version";
  public static final String RSC_WORK = "work";
  public static final String RSC_WORK_LOG = "worklog";
  public static final String RSC_WORK_JSON = "workjson";
}
