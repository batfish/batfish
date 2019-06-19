package org.batfish.datamodel.eigrp;

public enum EigrpProcessMode {
  /**
   * EIGRP scales the metrics before sending over the network. All arithmetic operations truncate,
   * so scaling and unscaling results in precision loss.
   */
  CLASSIC,
  /** EIGRP sends unscaled metrics. */
  NAMED
}
