package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile fastl4 configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileFastL4 implements BuiltinProfile {
  APM_FORWARDING_FASTL4("apm-forwarding-fastL4"),
  FASTL4("fastL4"),
  FULL_ACCELERATION("full-acceleration");

  private static final Map<String, BuiltinProfileFastL4> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinProfileFastL4::getName, Function.identity()));

  public static @Nullable BuiltinProfileFastL4 forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileFastL4(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_FASTL4;
  }
}
