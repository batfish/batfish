package org.batfish.datamodel.questions;

/** Compatibility statuses for BgpSessionCompatibilityAnswerer */
public enum ConfiguredSessionStatus {
  // ordered by how we evaluate status
  // should all be upper case for parsing to work
  /** Active peer with no local IP configured; session type is IBGP or EBGP multihop */
  LOCAL_IP_UNKNOWN_STATICALLY,
  /** Active peer with no local IP configured; session type is EBGP single-hop */
  NO_LOCAL_IP,
  /** No local AS configured */
  NO_LOCAL_AS,
  /** Active peer with no remote IP configured */
  NO_REMOTE_IP,
  /** Passive peer with no remote prefix configured */
  NO_REMOTE_PREFIX,
  /** No remote AS configured */
  NO_REMOTE_AS,
  /** Local IP is not associated with a known interface */
  INVALID_LOCAL_IP,
  /** Remote IP is not associated with a known interface */
  UNKNOWN_REMOTE,
  /** Point-to-point peer (active or unnumbered) with no compatible remote peers */
  HALF_OPEN,
  /** Point-to-point peer (active or unnumbered) with multiple remote peers configured compatibly */
  MULTIPLE_REMOTES,
  /** Point-to-point peer (active or unnumbered) with exactly one compatible remote peer */
  UNIQUE_MATCH,
  /** Passive peer with one or more compatible remote peers */
  DYNAMIC_MATCH,
  /** Passive peer with no compatible remote peers */
  NO_MATCH_FOUND;

  public static ConfiguredSessionStatus parse(String status) {
    return Enum.valueOf(ConfiguredSessionStatus.class, status.toUpperCase());
  }
}
