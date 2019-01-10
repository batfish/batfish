package org.batfish.datamodel.questions;

/** Compatibility statuses for BgpSessionCompatibilityAnswerer */
public enum ConfiguredSessionStatus {
  // ordered by how we evaluate status
  /** Local peer is passive with at least one compatible remote peer */
  DYNAMIC_MATCH,
  /** Local peer is passive with no compatible remote peers */
  NO_MATCH_FOUND,
  /** No local IP is configured on active peer; session type is IBGP or EBGP multihop */
  LOCAL_IP_UNKNOWN_STATICALLY,
  /** No local IP is configured on active peer; session type is not IBGP or EBGP multihop */
  NO_LOCAL_IP,
  /** No local AS is configured */
  NO_LOCAL_AS,
  /** Local peer is active with no remote IP configured */
  NO_REMOTE_IP,
  /** Local peer is passive with no remote prefix configured */
  NO_REMOTE_PREFIX,
  /** Local peer has no remote AS configured */
  NO_REMOTE_AS,
  /** Local IP is not associated with a known interface */
  INVALID_LOCAL_IP,
  /** Remote IP is not associated with a known interface */
  UNKNOWN_REMOTE,
  /** Local peer is active with no compatible remote peers */
  HALF_OPEN,
  /** Local peer is active with multiple remote peers configured compatibly */
  MULTIPLE_REMOTES,
  /** Local peer is active with exactly one compatible remote peer */
  UNIQUE_MATCH
}
