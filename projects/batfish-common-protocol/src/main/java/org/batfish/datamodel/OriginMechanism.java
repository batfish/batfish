package org.batfish.datamodel;

/**
 * Configuration-wise, how a route got into the BGP RIB. Used in tie-breaking on some vendors, and
 * to distinguish which next-hop-IP preference to use for locally-originated routes.
 */
public enum OriginMechanism {
  /** Created on the device independently of any existing route with the same prefix. */
  GENERATED,
  /** Learned from a peer. */
  LEARNED,
  /** Locally originated via a {@code network} statement. */
  NETWORK,
  /**
   * Locally originated via a {@code redistribute} statement, or via export from main RIB on devices
   * that do so.
   */
  REDISTRIBUTE;
}
