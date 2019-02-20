package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface Builtin {
  @Nonnull
  F5BigipStructureType getType();

  public static final String COMMON_PREFIX = "/Common/";

  /** Remove '/Common/' prefix from {@code name} if present */
  public static @Nonnull String unqualify(String name) {
    return name.startsWith(COMMON_PREFIX) ? name.substring(COMMON_PREFIX.length()) : name;
  }
}
