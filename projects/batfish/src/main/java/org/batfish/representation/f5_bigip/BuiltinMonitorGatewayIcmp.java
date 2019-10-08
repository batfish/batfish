package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm monitor gateway-icmp configurations */
@ParametersAreNonnullByDefault
public enum BuiltinMonitorGatewayIcmp implements BuiltinMonitor {
  GATEWAY_ICMP("gateway_icmp");

  private static final Map<String, BuiltinMonitorGatewayIcmp> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(BuiltinMonitorGatewayIcmp::getName, Function.identity()));

  public static @Nullable BuiltinMonitorGatewayIcmp forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinMonitorGatewayIcmp(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.MONITOR_GATEWAY_ICMP;
  }
}
