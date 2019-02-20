package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm monitor http configurations */
@ParametersAreNonnullByDefault
public enum BuiltinMonitorHttp implements BuiltinMonitor {
  HTTP("http"),
  HTTP_HEAD_F5("http_head_f5");

  private static final Map<String, BuiltinMonitorHttp> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinMonitorHttp::getName, Function.identity()));

  public static @Nullable BuiltinMonitorHttp forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinMonitorHttp(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.MONITOR_HTTP;
  }
}
