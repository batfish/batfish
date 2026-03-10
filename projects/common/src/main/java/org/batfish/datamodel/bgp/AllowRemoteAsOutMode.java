package org.batfish.datamodel.bgp;

/**
 * How a BGP speaker decides whether to export an outgoing advertisement to a peer whose AS appears
 * in the AS-path of the advertisement.
 */
public enum AllowRemoteAsOutMode {
  /**
   * Always send, regardless of whether the peer's AS appears in the AS-path of the advertisement.
   */
  ALWAYS,
  /** Never send if the the peer's AS appears in the AS-path of the advertisement. */
  NEVER,
  /**
   * Send unless the first AS-set of the advertisement's AS-path (the next AS to be visited by
   * traffic, and typically the latest added) contains the peer's AS.
   */
  EXCEPT_FIRST;
}
