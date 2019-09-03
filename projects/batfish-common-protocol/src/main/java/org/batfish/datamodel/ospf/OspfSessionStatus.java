package org.batfish.datamodel.ospf;

/** Enum for possible status values for OSPF sessions */
public enum OspfSessionStatus {
  AREA_INVALID,
  AREA_MISMATCH,
  AREA_TYPE_MISMATCH,
  DEAD_INTERVAL_MISMATCH,
  DUPLICATE_ROUTER_ID,
  /** Peers are compatible and the session should be established */
  ESTABLISHED,
  HELLO_INTERVAL_MISMATCH,
  MTU_MISMATCH,
  NETWORK_TYPE_MISMATCH,
  /**
   * No session is even attempted, e.g. both peers are passive and do not try to establish the
   * session in the first place
   */
  NO_SESSION,
  PASSIVE_MISMATCH,
  PROCESS_INVALID,
  /**
   * Catch-all for compatibility issues, e.g. for those arising from invalid/missing data model
   * components
   */
  UNKNOWN_COMPATIBILITY_ISSUE,
}
