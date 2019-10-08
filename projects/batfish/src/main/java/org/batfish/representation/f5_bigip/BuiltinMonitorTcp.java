package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm monitor tcp configurations */
@ParametersAreNonnullByDefault
public enum BuiltinMonitorTcp implements BuiltinMonitor {
  TCP("tcp"),
  TCP_HALF_OPEN("tcp_half_open");

  private static final Map<String, BuiltinMonitorTcp> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinMonitorTcp::getName, Function.identity()));

  public static @Nullable BuiltinMonitorTcp forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinMonitorTcp(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.MONITOR_TCP;
  }
}
