package org.batfish.datamodel.bgp;

/**
 * Tie-breaker that gives preference to either BGP rib entries originated from {@code network}
 * statements, {@code redistribute} statements, or neither.
 */
public enum LocalOriginationTypeTieBreaker {
  NO_PREFERENCE,
  PREFER_NETWORK,
  PREFER_REDISTRIBUTE
}
