package org.batfish.common.topology;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    if (firstMatchingLowercase.isPresent()) {
      return firstMatchingLowercase;
    }
    Matcher m = CANONICAL_PREFIXES.matcher(query);
    if (!m.find()) {
      return Optional.empty();
    }
    String prefix = m.group(1).toLowerCase();
    Set<String> confusions = CONFUSION_MAP.get(prefix);
    if (confusions == null) {
      return Optional.empty();
    }
    String suffix = m.group(2);
    return knownInterfaces.stream()
        .filter(i -> confusions.stream().anyMatch(c -> i.equalsIgnoreCase(c + suffix)))
        .findFirst();
  }

  /** Returns the interface matching the query, or {@link Optional#empty()} if none found. */
  public static Optional<Interface> matchingInterface(String query, Configuration c) {
    return matchingInterfaceName(query, c.getAllInterfaces().keySet())
        .map(c.getAllInterfaces()::get);
  }

  /** Extracts the interface prefix (all characters up to a digit) and suffix (everything after). */
  private static final Pattern CANONICAL_PREFIXES =
      Pattern.compile("^([^\\d]+)(.*)", Pattern.CASE_INSENSITIVE);

  /**
   * A set of plausibly-confused interface prefixes. Only this map need be modified to add more
   * alternatives.
   */
  private static final Set<Set<String>> CONFUSIONS =
      ImmutableSet.of(
          ImmutableSet.of("Ethernet", "Eth"),
          ImmutableSet.of("GigabitEthernet", "GigE", "Ge"),
          ImmutableSet.of("FastEthernet", "FastEth", "Fe"),
          ImmutableSet.of("TenGigabitEthernet", "TenGigE", "Te"),
          ImmutableSet.of("TwentyFiveGigE", "TwentyFiveGigabitEthernet"),
          ImmutableSet.of("FortyGigabitEthernet", "FortyGigE", "FoE"),
          ImmutableSet.of("FiftyGigabitEthernet", "FiftyGigE"),
          ImmutableSet.of("HundredGigE", "HundredGigabitEthernet", "Hu"),
          ImmutableSet.of("FourHundredGigE", "FourHundredGigabitEthernet"),
          ImmutableSet.of("port-channel", "Po"),
          ImmutableSet.of("Loopback", "Lo"),
          ImmutableSet.of("Vlan", "Vl"),
          ImmutableSet.of("Tunnel", "Tu"),
          ImmutableSet.of("Management", "Mgmt", "Ma"),
          ImmutableSet.of("ae", "aggregate-ethernet"));

  private static final SetMultimap<String, String> CONFUSION_MAP = computeConfusionMap(CONFUSIONS);

  /**
   * Helper to convert {@link #CONFUSIONS} (for authoring) into {@link #CONFUSION_MAP} for lookup.
   */
  private static SetMultimap<String, String> computeConfusionMap(Set<Set<String>> confusions) {
    ImmutableSetMultimap.Builder<String, String> ret = ImmutableSetMultimap.builder();
    confusions.forEach(
        c ->
            c.forEach(
                s -> ret.putAll(s.toLowerCase(), Sets.difference(c, Collections.singleton(s)))));
    return ret.build();
  }

  private InterfaceUtil() {} // prevent instantiation of utility clas
}
