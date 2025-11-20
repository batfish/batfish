package org.batfish.representation.juniper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A helper class for forwarding class related functionality. Provides information about built-in
 * forwarding classes.
 */
@ParametersAreNonnullByDefault
public final class ForwardingClassUtil {

  /**
   * Returns the default queue number for built-in forwarding classes.
   *
   * <p>Juniper Networks devices have eight queues built into hardware. By default, four queues are
   * assigned to four built-in forwarding classes. Queue numbers 4-7 have no default assignments.
   *
   * <p>Additional platform-specific forwarding classes:
   *
   * <ul>
   *   <li>mcast: multidestination forwarding class on QFX switches (except QFX10000), queue 8
   *   <li>mcast-*: multicast variants on EX4300 switches, using same queues as base classes
   *   <li>be/be1/nc: QFX short aliases for forwarding classes
   *   <li>fcoe/no-loss: QFX forwarding classes with platform-specific queue assignments (marked
   *       with queue -1)
   * </ul>
   *
   * <p>TODO: if we need to care about the queue numbers for defaults, we'll have to figure out how
   * to make them chassis-aware
   */
  // Built-in forwarding classes and default queue assignments are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-qos-forwarding-classes-overview.html
  // Table 12: Default Forwarding Class Queue Assignments
  // https://www.juniper.net/documentation/us/en/software/junos/cos-ex/topics/concept/cos-ex-series-forwarding-classes-understanding.html
  // https://www.juniper.net/documentation/us/en/software/junos/traffic-mgmt-qfx/topics/example/forwarding-classes-cos-configuring.html
  private static final Map<String, Integer> DEFAULT_QUEUE_ASSIGNMENTS =
      Map.ofEntries(
          Map.entry("best-effort", 0),
          Map.entry("expedited-forwarding", 1),
          Map.entry("assured-forwarding", 2),
          Map.entry("network-control", 3),
          // Platform-specific: QFX switches (except QFX10000)
          Map.entry("mcast", 8),
          // Platform-specific: EX4300 switches, use same queues as base classes
          Map.entry("mcast-be", 0),
          Map.entry("mcast-ef", 1),
          Map.entry("mcast-af", 2),
          Map.entry("mcast-nc", 3),
          // Platform-specific: QFX switches - short aliases
          Map.entry("be", 0),
          Map.entry("be1", 1),
          Map.entry("nc", 7),
          // Platform-specific: QFX switches - Fibre Channel over Ethernet
          Map.entry("fcoe", -1), // queue assignment varies by platform
          // Platform-specific: QFX switches - lossless traffic
          Map.entry("no-loss", -1)); // queue assignment varies by platform

  /** Returns the default queue number for a built-in forwarding class name. */
  public static @Nonnull Optional<Integer> defaultQueueNumber(String forwardingClassName) {
    return Optional.ofNullable(DEFAULT_QUEUE_ASSIGNMENTS.get(forwardingClassName));
  }

  /** Returns true if the given name is a built-in forwarding class. */
  public static boolean isBuiltin(String forwardingClassName) {
    return DEFAULT_QUEUE_ASSIGNMENTS.containsKey(forwardingClassName);
  }

  /** Returns the set of built-in forwarding class names. */
  public static @Nonnull Set<String> builtinNames() {
    return DEFAULT_QUEUE_ASSIGNMENTS.keySet();
  }

  private ForwardingClassUtil() {}
}
