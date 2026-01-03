package org.batfish.datamodel;

/**
 * {@link MultipathEquivalentAsPathMatchMode} is used when determining which BGP paths are
 * equivalent to the best path. It is used only if multipath routing is enabled.
 *
 * <p>All candidates filtered by this function must have the same AS path length as the best path.
 */
public enum MultipathEquivalentAsPathMatchMode {
  /** Only alternative paths that exactly match the AS path of the best path are equivalent. */
  EXACT_PATH,
  /** Relaxed form of {@link #EXACT_PATH} that requires only the first AS to be the same. */
  FIRST_AS,
  /**
   * Accepts all candidate paths. Since they have already been filtered to the same length, this is
   * a no-op.
   */
  PATH_LENGTH
}
