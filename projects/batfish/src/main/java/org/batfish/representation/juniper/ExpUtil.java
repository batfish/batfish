package org.batfish.representation.juniper;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A helper class for MPLS EXP related functionality. Currently, converts named-aliases to their
 * values.
 */
@ParametersAreNonnullByDefault
public final class ExpUtil {

  /** Returns the default value for builtin MPLS EXP aliases. */
  // Aliases and values are from
  // https://www.juniper.net/documentation/us/en/software/junos/cos-security-devices/topics/concept/cos-default-value-alias-security.html
  // Table 40: Standard CoS Aliases and Bit Values
  public static @Nonnull Optional<Integer> defaultValue(String alias) {
    switch (alias) {
      case "be":
        return Optional.of(0);
      case "be1":
        return Optional.of(1);
      case "ef":
        return Optional.of(2);
      case "ef1":
        return Optional.of(3);
      case "af11":
        return Optional.of(4);
      case "af12":
        return Optional.of(5);
      case "nc1":
      case "cs6":
        return Optional.of(6);
      case "nc2":
      case "cs7":
        return Optional.of(7);
      default:
        return Optional.empty();
    }
  }

  private ExpUtil() {}
}
