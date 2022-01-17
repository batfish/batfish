package org.batfish.representation.juniper;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.DscpType;

/**
 * A helper class for DSCP related functionality. Currently, converts named-aliases to their values.
 */
@ParametersAreNonnullByDefault
public final class DscpUtil {

  /** Returns the default value for builtin DSCP aliases. */
  // Aliases and values are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-default-value-alias-security.html
  public static @Nonnull Optional<Integer> defaultValue(String alias) {
    switch (alias) {
      case "ef":
        return Optional.of(DscpType.EF.number());
      case "af11":
        return Optional.of(DscpType.AF11.number());
      case "af12":
        return Optional.of(DscpType.AF12.number());
      case "af13":
        return Optional.of(DscpType.AF13.number());
      case "af21":
        return Optional.of(DscpType.AF21.number());
      case "af22":
        return Optional.of(DscpType.AF22.number());
      case "af23":
        return Optional.of(DscpType.AF23.number());
      case "af31":
        return Optional.of(DscpType.AF31.number());
      case "af32":
        return Optional.of(DscpType.AF32.number());
      case "af33":
        return Optional.of(DscpType.AF33.number());
      case "af41":
        return Optional.of(DscpType.AF41.number());
      case "af42":
        return Optional.of(DscpType.AF42.number());
      case "af43":
        return Optional.of(DscpType.AF43.number());
      case "be":
        return Optional.of(DscpType.DEFAULT.number());
      case "cs1":
        return Optional.of(DscpType.CS1.number());
      case "cs2":
        return Optional.of(DscpType.CS2.number());
      case "cs3":
        return Optional.of(DscpType.CS3.number());
      case "cs4":
        return Optional.of(DscpType.CS4.number());
      case "cs5":
        return Optional.of(DscpType.CS5.number());
      case "nc1":
      case "cs6":
        return Optional.of(DscpType.CS6.number());
      case "nc2":
      case "cs7":
        return Optional.of(DscpType.CS7.number());
      default:
        return Optional.empty();
    }
  }

  private DscpUtil() {}
}
