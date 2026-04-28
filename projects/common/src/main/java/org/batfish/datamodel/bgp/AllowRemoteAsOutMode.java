package org.batfish.datamodel.bgp;

/**
 * How a BGP speaker decides whether to export an outgoing advertisement to a peer whose AS appears
 * in the AS-path of the advertisement.
 */
public enum AllowRemoteAsOutMode {
  /**
   * Always send, regardless of whether the peer's AS appears in the AS-path of the advertisement.
   * Disables both AS-path-based and per-peer loop prevention. Only use this when the sending peer
   * is known to be included in the same-AS group — otherwise use {@link #EXCEPT_RECEIVED_FROM}.
   */
  ALWAYS,
  /**
   * Disable AS-path-based loop prevention, but still do not re-advertise a route back to the
   * specific peer it was received from. This is the Junos {@code advertise-peer-as} behavior.
   */
  EXCEPT_RECEIVED_FROM,
  /** Never send if the the peer's AS appears in the AS-path of the advertisement. */
  NEVER,
  /**
   * Send unless the first AS-set of the advertisement's AS-path (the next AS to be visited by
   * traffic, and typically the latest added) contains the peer's AS.
   */
  EXCEPT_FIRST;
}
