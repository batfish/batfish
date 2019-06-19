package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm monitor https configurations */
@ParametersAreNonnullByDefault
public enum BuiltinMonitorHttps implements BuiltinMonitor {
  HTTPS("https"),
  HTTPS_443("https_443"),
  HTTPS_HEAD_F5("https_head_f5");

  private static final Map<String, BuiltinMonitorHttps> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinMonitorHttps::getName, Function.identity()));

  public static @Nullable BuiltinMonitorHttps forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinMonitorHttps(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.MONITOR_HTTPS;
  }
}
