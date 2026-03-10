package org.batfish.datamodel.bgp;

/**
 * Tie-breaker for locally-originated BGP routes redistributed either via a {@code network} or
 * {@code redistribute} statement. In general, separate tie-breakers should be used for {@code
 * network} and {@code redistribute}.
 */
public enum NextHopIpTieBreaker {
  HIGHEST_NEXT_HOP_IP,
  LOWEST_NEXT_HOP_IP
}
