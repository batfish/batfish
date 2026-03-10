package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A helper class for IEEE 802.1p related functionality. Currently, converts named-aliases to their
 * values.
 */
@ParametersAreNonnullByDefault
public final class Ieee8021pUtil {

  /** Built-in IEEE 802.1p aliases and their default values. */
  // Aliases and values are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-default-value-alias-security.html
  // Table 40: Standard CoS Aliases and Bit Values
  private static final Map<String, Integer> DEFAULT_VALUES =
      ImmutableMap.<String, Integer>builder()
          .put("be", 0)
          .put("be1", 1)
          .put("ef", 2)
          .put("ef1", 3)
          .put("af11", 4)
          .put("af12", 5)
          .put("nc1", 6)
          .put("cs6", 6)
          .put("nc2", 7)
          .put("cs7", 7)
          .build();

  /** Returns the default value for builtin IEEE 802.1p aliases. */
  public static @Nonnull Optional<Integer> defaultValue(String alias) {
    return Optional.ofNullable(DEFAULT_VALUES.get(alias));
  }

  /** Returns the set of built-in IEEE 802.1p alias names. */
  public static @Nonnull Set<String> builtinNames() {
    return DEFAULT_VALUES.keySet();
  }

  private Ieee8021pUtil() {}
}
