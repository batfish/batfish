package org.batfish.dataplane.traceroute;

/**
 * Allows flexible interaction between different stages of the traceroute pipeline (at a single
 * hop).
 */
interface TracePipelineMediator<T> {

  /**
   * Check if this flow should be accepted on this device (destined to one of the IPs this device
   * owns).
   */
  T acceptOnDevice(PipelineContext context);

  /**
   * Perform destination-based forwarding. This includes the following steps:
   *
   * <ol>
   *   <li>Checking if the flow is destined to this device/hop
   *   <li>a FIB lookup based on flow destination IP
   *   <li>Creating either a null-route trace or forwarding the flow to all outgoing interfaces
   *       (based on the fib lookup)
   * </ol>
   */
  T destinationBasedLookup(PipelineContext context);

  /** Perform loop detection. */
  T detectLoops(PipelineContext context);

  /** Send the flow out all interfaces as specified by {@code fibEntries}. */
  T sendOutInterfaces(PipelineContext context);
}
