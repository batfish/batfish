package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableMap;
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
      ImmutableMap.<String, Integer>builder()
          .put("ef", DscpType.EF.number())
          .put("af11", DscpType.AF11.number())
          .put("af12", DscpType.AF12.number())
          .put("af13", DscpType.AF13.number())
          .put("af21", DscpType.AF21.number())
          .put("af22", DscpType.AF22.number())
          .put("af23", DscpType.AF23.number())
          .put("af31", DscpType.AF31.number())
          .put("af32", DscpType.AF32.number())
          .put("af33", DscpType.AF33.number())
          .put("af41", DscpType.AF41.number())
          .put("af42", DscpType.AF42.number())
          .put("af43", DscpType.AF43.number())
          .put("be", DscpType.DEFAULT.number())
          .put("cs1", DscpType.CS1.number())
          .put("cs2", DscpType.CS2.number())
          .put("cs3", DscpType.CS3.number())
          .put("cs4", DscpType.CS4.number())
          .put("cs5", DscpType.CS5.number())
          .put("nc1", DscpType.CS6.number())
          .put("cs6", DscpType.CS6.number())
          .put("nc2", DscpType.CS7.number())
          .put("cs7", DscpType.CS7.number())
          .build();

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
