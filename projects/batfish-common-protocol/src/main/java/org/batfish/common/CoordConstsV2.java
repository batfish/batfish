package org.batfish.common;

/** Constants used in the coordinator service version 2. */
public class CoordConstsV2 {
  ////// HTTP Headers that clients are expected to configure. //////

  /** The HTTP Header containing the client's API Key. */
  public static final String HTTP_HEADER_BATFISH_APIKEY = "X-Batfish-Apikey";
  /** The HTTP Header containing the client's version. */
  public static final String HTTP_HEADER_BATFISH_VERSION = "X-Batfish-Version";

  public static final String RSC_CONTAINER = "container";
  public static final String RSC_CONTAINERS = "containers";
  public static final String RSC_INFERRED_NODE_ROLES = "inferred_node_roles";
  public static final String RSC_INPUT = "input";
  public static final String RSC_ISSUES = "issues";
  public static final String RSC_NETWORK = "network";
  public static final String RSC_NETWORKS = "networks";
  public static final String RSC_NODE_ROLES = "noderoles";
  public static final String RSC_OBJECTS = "objects";
  public static final String RSC_POJO_TOPOLOGY = "pojo_topology";
  public static final String RSC_QUESTIONS = "questions";
  public static final String RSC_REFERENCE_LIBRARY = "referencelibrary";
  public static final String RSC_SETTINGS = "settings";
  public static final String RSC_SNAPSHOTS = "snapshots";
  public static final String RSC_TOPOLOGY = "topology";
}
