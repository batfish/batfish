package org.batfish.datamodel.eigrp;

/**
 * Identifies how to summarize an {@link EigrpMetric} as a single value.
 *
 * <p>Is implementation-dependent. E.g., IOS-XE uses V1 and NX-OS uses V2.
 */
public enum EigrpMetricVersion {
  /** IOS-XE -like. */
  V1,
  /** NX-OS -like. Higher precision than IOS-XE. */
  V2,
}
