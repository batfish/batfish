package org.batfish.datamodel.ospf;

/** Enum for possible status values for OSPF sessions */
public enum OspfSessionStatus {
  /** Specified area is invalid/does not exist */
  AREA_INVALID,
  /** Local and remote area numbers do not match */
  AREA_MISMATCH,
  /** Area types (stub type) mismatch between local and remote peer */
  AREA_TYPE_MISMATCH,
  /** Dead interval does not match between local and remote peer */
  DEAD_INTERVAL_MISMATCH,
  /** Router ID is the same between local and remote OSPF process */
  DUPLICATE_ROUTER_ID,
  /** Peers are compatible and the session should be established */
  ESTABLISHED,
  /** Hello interval does not match between local and remote peer */
  HELLO_INTERVAL_MISMATCH,
  /** MTU does not match between local and remote peer */
  MTU_MISMATCH,
  /** Network type does not match between local and remote peer */
  NETWORK_TYPE_MISMATCH,
  /**
   * No session is even attempted, e.g. both peers are passive and do not try to establish a session
   * in the first place
   */
  NO_SESSION,
  /** Local or remote peer is configured in passive mode, but the other is active */
  PASSIVE_MISMATCH,
  /** Specified process is invalid/does not exist */
  PROCESS_INVALID,
  /**
   * Catch-all for compatibility issues, e.g. for those arising from invalid/missing data model
   * components
   */
  UNKNOWN_COMPATIBILITY_ISSUE,
}
