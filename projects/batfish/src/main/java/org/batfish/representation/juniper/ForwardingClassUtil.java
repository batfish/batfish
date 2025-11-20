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
   */
  // Built-in forwarding classes and default queue assignments are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-qos-forwarding-classes-overview.html
  // Table 12: Default Forwarding Class Queue Assignments
  private static final Map<String, Integer> DEFAULT_QUEUE_ASSIGNMENTS =
      Map.of(
          "best-effort", 0,
          "expedited-forwarding", 1,
          "assured-forwarding", 2,
          "network-control", 3);

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
