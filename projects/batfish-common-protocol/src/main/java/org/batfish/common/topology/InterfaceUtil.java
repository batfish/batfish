package org.batfish.common.topology;

import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/** A helper class for topology-related functions for interfaces. */
public final class InterfaceUtil {
  /**
   * Returns the name of an interface matching the query, or {@link Optional#empty()} if none found.
   */
  public static Optional<String> matchingInterfaceName(String query, Set<String> knownInterfaces) {
    if (knownInterfaces.contains(query)) {
      return Optional.of(query);
    }
    Optional<String> firstMatchingLowercase =
        knownInterfaces.stream().filter(i -> i.equalsIgnoreCase(query)).findFirst();
    // TODO: check for canonicalizations?
    return firstMatchingLowercase;
  }

  /** Returns the interface matching the query, or {@link Optional#empty()} if none found. */
  public static Optional<Interface> matchingInterface(String query, Configuration c) {
    return matchingInterfaceName(query, c.getAllInterfaces().keySet())
        .map(c.getAllInterfaces()::get);
  }

  private InterfaceUtil() {} // prevent instantiation of utility clas
}
