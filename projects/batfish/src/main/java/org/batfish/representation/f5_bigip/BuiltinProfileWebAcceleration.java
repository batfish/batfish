package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile web-security configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileWebAcceleration implements BuiltinProfile {
  APM_ENDUSER_IF_CACHE("apm-enduser-if-cache"),
  OPTIMIZED_ACCELERATION("optimized-acceleration"),
  OPTIMIZED_CACHING("optimized-caching"),
  WEBACCELERATION("webacceleration"),
  WEBSECURITY("websecurity");

  private static final Map<String, BuiltinProfileWebAcceleration> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(
                  BuiltinProfileWebAcceleration::getName, Function.identity()));

  public static @Nullable BuiltinProfileWebAcceleration forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileWebAcceleration(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_WEB_SECURITY;
  }
}
