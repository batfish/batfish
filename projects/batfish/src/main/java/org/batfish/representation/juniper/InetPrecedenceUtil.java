package org.batfish.representation.juniper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A helper class for IP Precedence related functionality. Currently, converts named-aliases to
 * their values.
 */
@ParametersAreNonnullByDefault
public final class InetPrecedenceUtil {

  /** Built-in IP Precedence aliases and their default values. */
  // Aliases and values are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-default-value-alias-security.html
  // Table 40: Standard CoS Aliases and Bit Values
  private static final Map<String, Integer> DEFAULT_VALUES =
      Map.ofEntries(
          Map.entry("be", 0),
          Map.entry("be1", 1),
          Map.entry("ef", 2),
          Map.entry("ef1", 3),
          Map.entry("af11", 4),
          Map.entry("af12", 5),
          Map.entry("nc1", 6),
          Map.entry("cs6", 6),
          Map.entry("nc2", 7),
          Map.entry("cs7", 7));

  /** Returns the default value for builtin IP Precedence aliases. */
  public static @Nonnull Optional<Integer> defaultValue(String alias) {
    return Optional.ofNullable(DEFAULT_VALUES.get(alias));
  }

  /** Returns the set of built-in IP Precedence alias names. */
  public static @Nonnull Set<String> builtinNames() {
    return DEFAULT_VALUES.keySet();
  }

  private InetPrecedenceUtil() {}
}
