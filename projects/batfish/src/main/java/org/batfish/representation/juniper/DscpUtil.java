package org.batfish.representation.juniper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.DscpType;

/**
 * A helper class for DSCP related functionality. Currently, converts named-aliases to their values.
 */
@ParametersAreNonnullByDefault
public final class DscpUtil {

  /** Built-in DSCP aliases and their default values. */
  // Aliases and values are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-default-value-alias-security.html
  private static final Map<String, Integer> DEFAULT_VALUES =
      Map.ofEntries(
          Map.entry("ef", DscpType.EF.number()),
          Map.entry("af11", DscpType.AF11.number()),
          Map.entry("af12", DscpType.AF12.number()),
          Map.entry("af13", DscpType.AF13.number()),
          Map.entry("af21", DscpType.AF21.number()),
          Map.entry("af22", DscpType.AF22.number()),
          Map.entry("af23", DscpType.AF23.number()),
          Map.entry("af31", DscpType.AF31.number()),
          Map.entry("af32", DscpType.AF32.number()),
          Map.entry("af33", DscpType.AF33.number()),
          Map.entry("af41", DscpType.AF41.number()),
          Map.entry("af42", DscpType.AF42.number()),
          Map.entry("af43", DscpType.AF43.number()),
          Map.entry("be", DscpType.DEFAULT.number()),
          Map.entry("cs1", DscpType.CS1.number()),
          Map.entry("cs2", DscpType.CS2.number()),
          Map.entry("cs3", DscpType.CS3.number()),
          Map.entry("cs4", DscpType.CS4.number()),
          Map.entry("cs5", DscpType.CS5.number()),
          Map.entry("nc1", DscpType.CS6.number()),
          Map.entry("cs6", DscpType.CS6.number()),
          Map.entry("nc2", DscpType.CS7.number()),
          Map.entry("cs7", DscpType.CS7.number()));

  /** Returns the default value for builtin DSCP aliases. */
  public static @Nonnull Optional<Integer> defaultValue(String alias) {
    return Optional.ofNullable(DEFAULT_VALUES.get(alias));
  }

  /** Returns the set of built-in DSCP alias names. */
  public static @Nonnull Set<String> builtinNames() {
    return DEFAULT_VALUES.keySet();
  }

  private DscpUtil() {}
}
